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
#include "PacketBuilder.h"

PacketBuilder::PacketBuilder(void)
{
	buildingEthernetPackets=false;
	isPPPoE=false;
}

PacketBuilder::~PacketBuilder(void)
{
}



void PacketBuilder::buildRawIpHeader(jbyte *header , char* sourceAddress , char* destAddress , 
							   uint8_t ttl , uint8_t tos, u_short ipIdentification , int  protocol , int dataSize){
		unsigned long sourceIPAddr = inet_addr (sourceAddress);
		unsigned long destIPAddr = inet_addr (destAddress);
		struct ip_hdr *ip;
		// Position of the IP Header is according to MAC protocol :
		ip = (struct ip_hdr *)(header);
		ip->ip_hl = 5;
		ip->ip_v = 4;
		ip->ip_tos = 0;
		ip->ip_id = htons(ipIdentification);
		ip->ip_len =htons(sizeof(struct ip_hdr)+dataSize);
		ip->ip_off = 0;
		ip->ip_ttl = ttl;
		ip->ip_p = protocol;	
		ip->ip_src = sourceIPAddr;
		ip->ip_dst = destIPAddr;
}

void PacketBuilder::buildRawIcmpHeader(jbyte *header , char icmpType , short id , short sequence ){
	struct ICMPHeader * icmp = (struct ICMPHeader *)(header);

	icmp->type = icmpType;			
	icmp->code = 0;
	icmp->id = id;
	icmp->sequence = sequence;
}

void PacketBuilder::initEthernetAddresses(u8* aSourceMACAddress , u8* aDestMACAddress){
		memcpy(srcMAC , aSourceMACAddress , 6);
		memcpy(destMAC , aDestMACAddress , 6);
		buildingEthernetPackets=true;
	}

void PacketBuilder::setPPPoE(u8* pppOeFrame , u8* pppFrame){
		isPPPoE =true;
		memcpy(pppOeHeader , pppOeFrame , 6);
		memcpy(pppHeader , pppFrame, 2);
	}

void PacketBuilder::setPPPoE(u8* pppOeFrameAsWhole)
	{
		setPPPoE(pppOeFrameAsWhole , pppOeFrameAsWhole+6);
	}

void PacketBuilder::buildEthernetHeader(char* packet , int expectedPacketSize)
	{
		if (isPPPoE)
		{
			// Regular ethernet packet :
			eth_pack_hdr(packet ,  destMAC , srcMAC  , ETH_TYPE_PPPOE);
			// PPPOE : 
			memcpy(packet+14,pppOeHeader,6);
			u16 hex2 = htons(expectedPacketSize + 2);
			memcpy(packet+18,&hex2 , 2);	// packet size is PPP + expectedPacketSize.

			// PPP : 
			memcpy(packet+20,pppHeader,2);	//( Usually 0x0021 = IP)
		}
		else
			// Ordinary ethernet packet :
			eth_pack_hdr(packet ,  destMAC , srcMAC  , ETH_TYPE_IP);
	}

	int PacketBuilder::getEthernetHeaderSize(){
		if (isPPPoE)
			return 22;
		else
			return 14;
	}

	int PacketBuilder::getIpHeaderPosition(){
		if (!buildingEthernetPackets)
			return 0;
		return getEthernetHeaderSize();
	}

	void PacketBuilder::fillChecksum(jbyte *buf ,int bufLength)
	{
		int ipHeaderPos = getIpHeaderPosition();
		ip_checksum((void*)(buf + ipHeaderPos) , bufLength-ipHeaderPos);
	}
