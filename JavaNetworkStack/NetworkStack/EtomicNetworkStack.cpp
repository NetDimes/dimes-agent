/**************************************************************************
 *   Copyright (C) 2006 by Ohad Serfaty , DIMES Team                       *
 *   ohad@eng.tau.ac.il  , support@netdimes.org                            *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 ***************************************************************************/
#ifdef DIMES_ETOMIC
#include "EtomicNetworkStack.h"

EtomicNetworkStack::EtomicNetworkStack(void)
{
	ready=false;
	receiveDagDevice = NULL;
	sendsock=INVALID_SOCKET;
}

EtomicNetworkStack::~EtomicNetworkStack(void)
{
}


bool EtomicNetworkStack::initialize(char **argv , int argc){
	// open two devices for send and receive :
	char *interfaceName = argv[0];
	receiveDagDevice = ever_dag_open(interfaceName , O_RECV);	
	if (receiveDagDevice == NULL)
		return false;
	sendsock = socket(AF_INET, SOCK_RAW, IPPROTO_RAW);
	if(sendsock == INVALID_SOCKET)
	{
		printf("LEVEL_SEVERE : SendSock is invalid ");
		ever_dag_close(receiveDagDevice);
		return false;
	}
	
	// allocate a packet buffer :
	packetsBuffer =  ever_dag_recv_alloc_packets(1024*1024);
	
	//allocate buffer for timestamp conversion
	timestamp = ever_dag_ts_alloc(1);
#ifndef COMPILE_STANDALONE
	JavaLog::javalogf(LEVEL_INFO,"Etomic DAG Card initialization completed succesfully");
#else
	printf("Etomic DAG Card initialization completed succesfully");
#endif

	 // set socket onptions :
	 int on = 1;
	 if (setsockopt(sendsock , IPPROTO_IP, IP_HDRINCL, (char*)&on, sizeof(on)) == SOCKET_ERROR)
	 {
		printf( "LEVEL_SEVERE  :Error while setting Socket Option :");
		return false;
	 }
	ready=true;
	return true;
}

int EtomicNetworkStack::close(){
	printf("Closing Etomic Dag Card");
	// release memory allocated by libeverdag
	ever_dag_recv_free_packets(packetsBuffer);
	ever_dag_ts_free(timestamp);
	// close open interface
	ever_dag_close(receiveDagDevice);
	//close(sendsock);
	ready=false;
	return 0;
}

jlong EtomicNetworkStack::sendPacket(jbyte *packetBytesArray , int packetSize ){

    	struct sockaddr_in remoteaddress;
	remoteaddress.sin_family=AF_INET;
	remoteaddress.sin_addr.s_addr = *((__int32*)(packetBytesArray + 16));		
	jlong nanoSendTime = JavaTime::javaTimeNanos();
	
	int returnCode = sendto(sendsock, (char*)packetBytesArray, packetSize , 0,(struct sockaddr *)&remoteaddress, 
											sizeof(remoteaddress));
	if (returnCode == SOCKET_ERROR)
	{		
		printf("send failed with error:");
		return (jlong)-1;
	}
	return nanoSendTime;
}

jlong EtomicNetworkStack::sendPacket(PacketDetails packetDetails){
	return sendPacket(packetDetails.packetBytes , packetDetails.packetSize ,packetDetails.timestampPosition, packetDetails.ipHeaderPosition);
}

jlong EtomicNetworkStack::sendPacket(jbyte *packetBytesArray , int packetSize , int timestampPosition , int chksumPos){
	if (timestampPosition != -1)
	{
		jlong sendTime = JavaTime::javaTimeNanos();
		memcpy(packetBytesArray + timestampPosition , (void*)(&sendTime) , 8);
		if (chksumPos!= -1 ){
			ip_checksum(packetBytesArray+chksumPos, packetSize-chksumPos );
		}
//		fflush(stdout);
	}
	return sendPacket(packetBytesArray , packetSize);
}


int EtomicNetworkStack::readPacket(struct timeval selectTime , jbyte *packetBuffer , jlong *receiveTimeMilisec , jlong *receiveTimeMicrosec)
{
	int waitTimeMs = 0; 
	// wait more than zero miliseconds only if the queue is empty :
	if (packetBytesQueue.empty())
		waitTimeMs = selectTime.tv_sec*1000 + selectTime.tv_usec/1000;
	printf("Waiting for : %d milisecs...\n" , waitTimeMs);
	// receive the packets from the DAG :
	int receivedPackets = ever_dag_recv_packets(receiveDagDevice, 1 , waitTimeMs , packetsBuffer);
	if (receivedPackets == -1)
	{
		printf("Error while listening to Etomic packets");
		return 0;
	}

	// copy all received packets to memory :
	printf("Copying %d packets...\n" , receivedPackets);
	for (int i=0; i<receivedPackets; i++){
		printf("iteration %d\n",i);
		dag_record_t  *packet = packetsBuffer[i];
		*timestamp = get_erf_timestamp (packet);
                int type = packet->rec.eth.etype;
		if (type != 8) 
			continue;         // don't process non-ip frames
		int copiedPacketSize = ntohs(packet->rlen) -sizeof(eth_rec_t) -ETHER_HDR_LEN;
		jbyte* copiedPacket = new jbyte[copiedPacketSize];
		memcpy(packet->rec.eth.pload , copiedPacket , copiedPacketSize);
		// TODO : create a structure for this and have just one list...
		packetBytesQueue.push_back(copiedPacket);
		packetBytesSizes.push_back(copiedPacketSize);
		packetRecevieTimesQueue.push_back((jlong)*timestamp);
	}
	ever_dag_recv_flush(receiveDagDevice);
	if (!packetBytesQueue.empty())
	{
		jbyte *firstPacket =  packetBytesQueue.front();
		int packetSize =  packetBytesSizes.front();
		printf("copying %d bytes...\n",packetSize);
		memcpy(packetBuffer , firstPacket , packetSize);
		*receiveTimeMicrosec = packetRecevieTimesQueue.front();
		packetBytesQueue.pop_front(); packetBytesSizes.pop_front(); packetRecevieTimesQueue.pop_front();
		*receiveTimeMilisec  = *receiveTimeMicrosec;
		delete firstPacket;
		return packetSize;
	}
	return 0;
}

#endif // DIMES_ETOMIC

