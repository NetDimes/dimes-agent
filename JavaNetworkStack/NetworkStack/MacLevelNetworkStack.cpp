/***************************************************************************
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
#include "MacLevelNetworkStack.h"
#ifdef DIMES_WINDOWS
MacLevelNetworkStack::MacLevelNetworkStack(void)
{
	isPPPoE=false;
	ethernetDevice=NULL;
	readEventInitialized=false;
	ready=false;
}

MacLevelNetworkStack::~MacLevelNetworkStack(void)
{
}

void MacLevelNetworkStack::serPacketFilter(struct bpf_program *filterProgram){
//
//	struct bpf_program program;
//program.bf_len=6;
//
//struct bpf_insn arrays[6] = {
//	{ 0x28, 0, 0, 0x0000000c },
//{ 0x15, 0, 3, 0x00000800 },
//{ 0x30, 0, 0, 0x00000017 },
//{ 0x15, 0, 1, 0x00000001 },
//{ 0x6, 0, 0, 0x00000060 },
//	{ 0x6, 0, 0, 0x00000000 } } ;
//	program.bf_insns = arrays;
//	for (int i=0; i<6; i++)
//	{
//		printf("%d %d\n" , program.bf_insns[i].code , filterProgram->bf_insns[i].code);
//		printf("%d %d\n" , program.bf_insns[i].jf , filterProgram->bf_insns[i].jf);
//		printf("%d %d\n" , program.bf_insns[i].jt , filterProgram->bf_insns[i].jt);
//		printf("%d %d\n" , program.bf_insns[i].k , filterProgram->bf_insns[i].k);
//	}
//	fflush(stdout);
	DimesPacketSetBpf(this->ethernetDevice , filterProgram);
}

bool MacLevelNetworkStack::initialize(char **argv , int argc){
	// start the driver :
	char *localAddress = argv[0];
	DimesDriverLoader::startDimesService();	
	int interfacesNum=20;
	ADAPTER_INTERFACE adaptersArray[20];
	// query interfaces :
	AdapterManager::getInterfaces(adaptersArray,&interfacesNum);

	// get the best inetrface
	int bestIntfIndex = AdapterManager::getBestAdapterIndex(adaptersArray , interfacesNum);
	if (bestIntfIndex == -1)
	{
		JavaLog::javalog(LEVEL_SEVERE ,"Couldn't get appropriate interface.");
		return false;
	}

	// copy the relevant information :
	strcpy(this->ethetnetDeviceName , adaptersArray[bestIntfIndex].protocolDriverName);
	strcpy(this->ipAddress,adaptersArray[bestIntfIndex].ipAddress);
	memcpy(this->srcMAC.data,adaptersArray[bestIntfIndex].srcMAC , 6);
	memcpy(this->destMAC.data,adaptersArray[bestIntfIndex].destMAC , 6);
	this->isPPPoE = adaptersArray[bestIntfIndex].isPPPOE;

	if (this->isPPPoE){
		JavaLog::javalog(LEVEL_INFO ,"Configuring as PPPoE interface..");
		staticPacketBuilder->setPPPoE(adaptersArray[bestIntfIndex].pppoeFrame);
	}
	this->ethernetDevice = DimesPacketOpenAdapter(this->ethetnetDeviceName);
	if (this->ethernetDevice ==  NULL)
	{
		JavaLog::javalog(LEVEL_SEVERE ,"Ethernet Device failed to open.");
		return false;
	}
	// locate the Source and Dest MAC Addresses :
//	printf("source mac :");printFormat((u8*)(&this->srcMAC),6);printf("\n");
//	printf("dest mac :");printFormat((u8*)(&this->destMAC),6);printf("\n");
	staticPacketBuilder->initEthernetAddresses((u8*)(&this->srcMAC) , (u8*)(&this->destMAC));
	ready=true;
	return true;
}

int MacLevelNetworkStack::close()
{
	DimesPacketCloseAdapter(this->ethernetDevice);
	DimesDriverLoader::stopDimesService();
	ready=false;
	return 0;
}

jlong MacLevelNetworkStack::sendPacket(jbyte *packetBytesArray , int packetSize ){
	LPPACKET packet = DimesPacketAllocatePacket();	

	DimesPacketInitPacket(packet , packetBytesArray , packetSize);
	// register send time :

	// send packet and free:

	DimesPacketSendPacket(this->ethernetDevice , packet,FALSE);
    //jlong sendTime1 = JavaTime::javaTimeNanos();

	DimesPacketFreePacket(packet);
	return 0 ;//sendTime;JavaTime::javaTimeNanos();
}



jlong MacLevelNetworkStack::sendPacket(PacketDetails packetDetails){
	return sendPacket(packetDetails.packetBytes , packetDetails.packetSize ,packetDetails.timestampPosition, packetDetails.ipHeaderPosition);
}

jlong MacLevelNetworkStack::sendPacket(jbyte *packetBytesArray , int packetSize , int timestampPosition , int chksumPos){
	if (timestampPosition != -1)
	{
		jlong tmpsendTime;
		jlong sendTime = JavaTime::javaTimeNanos();
		char swappedsendTime[8];
		int i;

//		printf("1Time stamp : "); printFormat((unsigned char*)(&sendTime) , 8);printf("<-1\n");
		tmpsendTime= sendTime;
		for (i=7; i>=0; i--)
		{
			swappedsendTime[i]=(char)tmpsendTime%256;
			tmpsendTime	=tmpsendTime/256;
		}
//		printf(" swappedsendTime : ((%d)) ",i); printFormat((unsigned char*)(swappedsendTime), 8);printf("<-\n");

		memcpy(packetBytesArray + timestampPosition , swappedsendTime , 8);

//		printFormat((unsigned char*)packetBytesArray , packetSize);
//		printf("<--\n");
//		if (chksumPos!= -1 ){
//			printf("Re-calculating checksum in position : %d\n" ,chksumPos );
//			ip_checksum(packetBytesArray+chksumPos, packetSize-chksumPos );
//		}
//		fflush(stdout);
	}
	return sendPacket(packetBytesArray , packetSize);
}

void MacLevelNetworkStack::openListener(){
	if (!readEventInitialized)
	{
		JavaLog::javalog(LEVEL_INFO,"Initializing Dimes driver Read event.");
		DimesPacketSetHwFilter(this->ethernetDevice,NDIS_PACKET_TYPE_PROMISCUOUS);
		DimesPacketSetBuff(this->ethernetDevice , 2048);
		DimesPacketSetMaxLookaheadsize(this->ethernetDevice);
		readEventInitialized=true;
	}
}

int MacLevelNetworkStack::readPacket(struct timeval selectTime , jbyte *packetBuffer , jlong *receiveTimeMilisec , jlong *receiveTimeMicrosec){
	char packet[2048];
	this->openListener();
	// set timeout and listen...
	int timeout = (int)(selectTime.tv_sec*1000 + selectTime.tv_usec/1000);
	DimesPacketSetReadTimeout(this->ethernetDevice , timeout);
	LPPACKET receviedPacket =  DimesPacketAllocatePacket();
	DimesPacketInitPacket(receviedPacket , packet , 1500);
	DimesPacketReceivePacket(this->ethernetDevice , receviedPacket  , TRUE);
	// First : grab the timestamp
	*receiveTimeMicrosec = JavaTime::javaTimeNanos();
	*receiveTimeMilisec = JavaTime::getJavaTime();	
	int packetSize = receviedPacket->ulBytesReceived-22;
	
	if (packetSize>0)
		memcpy(packetBuffer , packet+20 , packetSize);
	DimesPacketFreePacket(receviedPacket);		
//	JavaLog::javalogf(LEVEL_INFO,"Done reading packet with size %d .",packetSize);
/*	printf("Done reading packet with size %d .",packetSize);
//	fflush(stdout);
*/
	return packetSize;
}

#endif
