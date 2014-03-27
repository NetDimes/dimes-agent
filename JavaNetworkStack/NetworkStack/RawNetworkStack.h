#ifndef __raw_network_stack_h
#define __raw_network_stack_h
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

#include <stdio.h>
#include "NetworkStackBase.h"
#include "JNIOutputCallback.h"
#include "PacketBuilder.h"
#include "JavaTime.h"
#ifdef DIMES_WINDOWS
#include <WinSock2.h>
#include <WS2tcpip.h>
#else
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
// Specific definitions for Linux implementation of networking :
#define SOCKET int
#define INVALID_SOCKET -1
#define WSADATA int
#define MAKEWORD(x,y) 1
#define WSAStartup(x,y) 0
#define WSAGetLastError() 0
#define SOCKADDR struct sockaddr
#define SOCKET_ERROR -1
#define WSACleanup()
#define closesocket close

#define __int32 unsigned int

#endif
#include <list>


using namespace std;
/***********************
 *
 * a raw socket implementation of a Network Stack.
 * 
 * TODO :  add a way to define the socket type that will be opened.
 * For example , we can't send udp packets on this socket yet , 
 * first because there is no udp packet builder , and second , the 
 * sendsock is opened to send icmp packets ( or is it ? )
 */
class RawNetworkStack : public NetworkStackBase
{
public:
	// construxtors :
	RawNetworkStack(void);
	~RawNetworkStack(void);

	// virtual implemented functions :
	bool initialize(char **argv , int argc);
	int close();
	int readPacket(struct timeval selectTime , jbyte *packetBuffer , jlong *receiveTimeMilisec , jlong *receiveTimeMicrosec);
	jlong sendPacket(PacketDetails packetDetails);
	jlong sendPacket(jbyte *packetBytesArray, int packetSize);
	jlong sendPacket(jbyte *packetBytesArray , int packetSize , int timeStampPosition , int chksumPos);


private:
	// sockets :
	bool rawNetworkStackInitialized;
	SOCKET recvsock;
	SOCKET sendsock;

};


#endif
