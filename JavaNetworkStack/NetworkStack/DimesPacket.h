#ifndef _DIMES_PACHET_H_
#define _DIMES_PACHET_H_
#ifdef DIMES_WINDOWS
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

#include "wpcapDefs.h"
#include <windows.h>
#include <Iphlpapi.h>
#include <IPIfCons.h>
#include <stdio.h>
#include <ntddndis.h>
#include <windowsx.h>

struct bpf_insn 
{
	USHORT	code;		///< Instruction type and addressing mode.
	UCHAR 	jt;			///< Jump if true
	UCHAR 	jf;			///< Jump if false
	int k;				///< Generic field used for various purposes.
};

struct bpf_program 
{
	UINT bf_len;				///< Indicates the number of instructions of the program, i.e. the number of struct bpf_insn that will follow.
	struct bpf_insn *bf_insns;	///< A pointer to the first instruction of the program.
};



void printError(DWORD lastError);
void printFormat(unsigned char * hexArray , int arraySize);
BOOLEAN DimesPacketSetReadEvt(LPADAPTER AdapterObject);
BOOLEAN DimesPacketRequest(LPADAPTER  AdapterObject,BOOLEAN Set,PPACKET_OID_DATA  OidData);
BOOLEAN DimesPacketSetMaxLookaheadsize (LPADAPTER AdapterObject);
LPADAPTER DimesPacketOpenAdapter(PCHAR AdapterName);
BOOLEAN DimesPacketSetHwFilter(LPADAPTER  AdapterObject,ULONG Filter);
VOID DimesPacketCloseAdapter(LPADAPTER lpAdapter);
BOOLEAN DimesPacketSetBuff(LPADAPTER AdapterObject,int dim);
VOID DimesPacketInitPacket(LPPACKET lpPacket,PVOID Buffer,UINT Length);
VOID DimesPacketFreePacket(LPPACKET lpPacket);
LPPACKET DimesPacketAllocatePacket(void);
BOOLEAN DimesPacketSetBpf(LPADAPTER AdapterObject, struct bpf_program *fp);

// read  write :
BOOLEAN DimesPacketSetReadTimeout(LPADAPTER AdapterObject,int timeout);
BOOLEAN DimesPacketSendPacket(LPADAPTER AdapterObject,LPPACKET lpPacket,BOOLEAN Sync);
BOOLEAN DimesPacketReceivePacket(LPADAPTER AdapterObject,LPPACKET lpPacket,BOOLEAN Sync);

#endif
#endif
