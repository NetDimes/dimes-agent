#ifndef _PACKET_BUILDER_H_
#define _PACKET_BUILDER_H_
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
#include <jni.h>
#include "EthernetUtilities.h"

/***********************
 *
 * A packet building interface
 * Please note that this is not a full abstraction of a packet building mechanism , 
 * And so it can be initialized staticly in case of a needed extension. sorry...
 *
 * TODO : Add a udp and tcp building mechanism.
 */
class PacketBuilder
{
public:
	PacketBuilder(void);
	~PacketBuilder(void);

	// IP V4 Header :
	void buildRawIpHeader(jbyte *header , char* sourceIP , char* destIP , 
							   uint8_t ttl , uint8_t tos, u_short ipIdentification , int  protocol , int dataSize);
	
	// Icmp Header :
	void buildRawIcmpHeader(jbyte *header , char icmpType , short id , short sequence);
	void fillChecksum(jbyte *buf , int bufferSize);
	int getIpHeaderPosition();


	// Ethernet initialization :
	void initEthernetAddresses(u8* aSourceMACAddress , u8* aDestMACAddress);
	void setPPPoE(u8* pppOeFrame , u8* pppFrame);
	void setPPPoE(u8* pppOeFrameAsWhole);


	// Ethernet Header :
	void buildEthernetHeader(char* packet , int expectedPacketSize);
	int getEthernetHeaderSize();
	

protected:	
	// Vars :
	u8* srcMAC[6];
	u8* destMAC[6];
	u8* pppOeHeader[8];
	u8* pppHeader[2];
	bool buildingEthernetPackets;
	bool isPPPoE;


};

#endif
