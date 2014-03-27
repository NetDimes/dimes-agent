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
#ifndef __MAC_LEVEL_NETWORK_STACK_H
#define __MAC_LEVEL_NETWORK_STACK_H
#ifdef DIMES_WINDOWS

#include <stdio.h>
#include "NetworkStackBase.h"
#include "JNIOutputCallback.h"
#include "JavaTime.h"
#include "DimesDriverLoader.h"
#include "DimesPacket.h"
#include "AdapterManager.h"
#include "PacketBuilder.h"

#include <WinSock2.h>
#include <WS2tcpip.h>
#include <list>


using namespace std;

/***********************
 *
 * Implementation of a Mac Level network stack . 
 *
 */
class MacLevelNetworkStack : public NetworkStackBase
{
public:
	MacLevelNetworkStack(void);
	~MacLevelNetworkStack(void);

	// virtual implemented functions :
	bool initialize(char **argv , int argc);
	int close();

	// read /write methods :
	jlong sendPacket(PacketDetails packetDetails);
	jlong sendPacket(jbyte *packetBytesArray, int packetSize);
	jlong sendPacket(jbyte *packetBytesArray , int packetSize , int timeStampPosition , int chksumPos);
	int readPacket(struct timeval selectTime , jbyte *packet , jlong *receiveTimeMilisec , jlong *receiveTimeMicrosec);

	// special auxilery functions :
	void serPacketFilter(struct bpf_program *filterProgram);	// set a packet filter
	void openListener();										// open for listening
	boolean configuredPPPoE(){ return isPPPoE; }				// check if pppoe is configured

	// a static packet builder for building packets :
	PacketBuilder *staticPacketBuilder;

	// device properties :
public:
	char ethetnetDeviceName[512];
	char ipAddress[20];
	eth_addr_t srcMAC;
	eth_addr_t destMAC;
	bool isPPPoE;
	bool readEventInitialized;

	// Ethernet device :
	LPADAPTER ethernetDevice;

	

};

#endif
#endif
