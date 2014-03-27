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
#ifndef __ETOMIC_NETWORK_STACH_H__
#define __ETOMIC_NETWORK_STACH_H__
#ifdef DIMES_ETOMIC
extern "C"
{
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <ever_dag.h>
#include <net/ethernet.h>
}
#ifndef COMPILE_STANDALONE
#include "NetworkStackBase.h"
#include "JNIOutputCallback.h"
#endif
#include "JavaTime.h"
#include "PacketBuilder.h"
// Specific definitions for Linux implementation of networking :
#define __int32 unsigned int
#define SOCKET int
#define INVALID_SOCKET -1
#define SOCKET_ERROR -1

#include <list>


using namespace std;
/***********************
 *
 * a raw socket implementation of an Etomic Network Stack.
 * 
 * TODO :  add a way to define the socket type that will be opened.
 * For example , we can't send udp packets on this socket yet , 
 * first because there is no udp packet builder , and second , the 
 * sendsock is opened to send icmp packets ( or is it ? )
 */
#ifndef COMPILE_STANDALONE
class EtomicNetworkStack : public NetworkStackBase
#else
// when compiling standalone , the EtomicNetworkStack is a baseless class :
class EtomicNetworkStack
#endif
{
public:
	// construxtors :
	EtomicNetworkStack(void);
	~EtomicNetworkStack(void);

	// virtual implemented functions :
	bool initialize(char **argv , int argc);
	int close();
	int readPacket(struct timeval selectTime , jbyte *packetBuffer , jlong *receiveTimeMilisec , jlong *receiveTimeMicrosec);
	jlong sendPacket(PacketDetails packetDetails);
	jlong sendPacket(jbyte *packetBytesArray, int packetSize);
	jlong sendPacket(jbyte *packetBytesArray , int packetSize , int timeStampPosition , int chksumPos);



private:
	// sockets :
	ever_dag_t *receiveDagDevice;
	SOCKET sendsock;
	dag_record_t **packetsBuffer;
	ever_dag_ts_t *timestamp;
	
	list<jbyte*> packetBytesQueue;
	list<jlong>  packetRecevieTimesQueue;
	list<int> packetBytesSizes;
	
	bool rawNetworkStackInitialized;

	// When compiling standalone , this boolean is not inherited , so i add it :
	#ifdef COMPILE_STANDALONE
	bool ready;
	#endif	

	
};

#endif // DIMES_ETOMIC
#endif // __ETOMIC_NETWORK_STACH_H__
