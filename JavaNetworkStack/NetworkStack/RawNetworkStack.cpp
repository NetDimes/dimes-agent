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
#include "RawNetworkStack.h"

RawNetworkStack::RawNetworkStack(void)
{
	rawNetworkStackInitialized = false;
	recvsock=INVALID_SOCKET;
	sendsock=INVALID_SOCKET;
	ready=false;
}

RawNetworkStack::~RawNetworkStack(void)
{
}


bool RawNetworkStack::initialize(char **argv , int argc){
	char* localAddress = argv[0];

#ifdef DIMES_WINDOWS
	WSADATA WinsockData;
	if (WSAStartup(MAKEWORD(2, 2), &WinsockData) != 0) {
		JavaLog::javalog(LEVEL_SEVERE," Failed to find Winsock 2.2!");
		return false;
	}
#endif

	JavaLog::javalogf(LEVEL_INFO , "Initializing Listener with Address %s",localAddress);
	recvsock = socket(AF_INET, SOCK_RAW, IPPROTO_ICMP);

	if(recvsock == INVALID_SOCKET)
	{
		JavaLog::javalogf(LEVEL_SEVERE ,"recvsock is invalid (%d)",WSAGetLastError());//debug
		return false;
	}
	
	sendsock = socket(AF_INET, SOCK_RAW, IPPROTO_RAW);
	if(sendsock == INVALID_SOCKET)
	{
		JavaLog::javalogf(LEVEL_SEVERE ,"SendSock is invalid (%d)",WSAGetLastError());//debug
		return false;
	}
	// bind :
	sockaddr_in localaddress;
	localaddress.sin_family = AF_INET;
	localaddress.sin_addr.s_addr = inet_addr(localAddress);//is in network order
	int on = 1;
	
	if (bind(recvsock, (SOCKADDR*)&localaddress, sizeof(localaddress)) == SOCKET_ERROR)
	{
		JavaLog::javalogf(LEVEL_SEVERE,"bind failed for recvsock %s",localAddress);//debug
		return false;
	}

	JavaLog::javalogf(LEVEL_INFO,"Socket initialization completed succesfully");
	if (setsockopt(sendsock , IPPROTO_IP, IP_HDRINCL, (char*)&on, sizeof(on)) == SOCKET_ERROR) //include our own ip header
	{
		JavaLog::javalogf(LEVEL_SEVERE , "Error while setting Socket Option : %d" , WSAGetLastError());
		return false;
	}
	ready=true;
	return true;
}

int auxClose(SOCKET sock){
	return closesocket(sock);
}

int RawNetworkStack::close(){
	JavaLog::javalogf(LEVEL_INFO , "Closing socket %d.",recvsock);
	int result = auxClose(recvsock);
	auxClose(sendsock);
	WSACleanup();
	ready=false;
	return result;
}

jlong RawNetworkStack::sendPacket(PacketDetails packetDetails){
	return sendPacket(packetDetails.packetBytes , packetDetails.packetSize ,packetDetails.timestampPosition, packetDetails.ipHeaderPosition);
}

jlong RawNetworkStack::sendPacket(jbyte *packetBytesArray , int packetSize , int timestampPosition , int chksumPos){
	if (timestampPosition != -1)
	{
		jlong sendTime = JavaTime::javaTimeNanos();
		memcpy(packetBytesArray + timestampPosition , (void*)(&sendTime) , 4);
		if (chksumPos!= -1 ){
			ip_checksum(packetBytesArray, packetSize);
		}
	}
	return sendPacket(packetBytesArray , packetSize);
}



jlong RawNetworkStack::sendPacket(jbyte *packetBytesArray , int packetSize){
	struct sockaddr_in remoteaddress;
	remoteaddress.sin_family=AF_INET;
	remoteaddress.sin_addr.s_addr = *((__int32*)(packetBytesArray + 16));		
	jlong nanoSendTime = JavaTime::javaTimeNanos();

	int returnCode = sendto(sendsock, (char*)packetBytesArray, packetSize , 0,(struct sockaddr *)&remoteaddress, 
											sizeof(remoteaddress));
	if (returnCode == SOCKET_ERROR)
	{
		JavaLog::javalogf(LEVEL_SEVERE , "send failed with error: %d", WSAGetLastError());
		return (jlong)-1;
	}
	return nanoSendTime;
}


int RawNetworkStack::readPacket(struct timeval selectTime , jbyte *packetBuffer , jlong *receiveTimeMilisec , jlong *receiveTimeMicrosec){
	fd_set readfd;
	FD_ZERO(&readfd);
	FD_SET(recvsock, &readfd);
	
	select(recvsock+1, &readfd, NULL, NULL, &selectTime);

	if(FD_ISSET(recvsock, &readfd)) 
	{	
		*receiveTimeMicrosec=JavaTime::javaTimeNanos();	
		*receiveTimeMilisec= JavaTime::getJavaTime();		

		struct sockaddr_in fromaddr;
		socklen_t fromaddrsize;
		fromaddrsize = sizeof(fromaddr);
		// read Packet from socket:
		int packetSize = recvfrom(	recvsock, (char*)packetBuffer, 2048, 0, 
			(struct sockaddr *)&fromaddr, &fromaddrsize);

		if (packetSize == SOCKET_ERROR)
		{
			JavaLog::javalogf(LEVEL_SEVERE , "Socket Error : %d",WSAGetLastError());				
			return 0;
		}
		return packetSize;
	} 
	return 0;
}
