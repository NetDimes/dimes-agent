#ifndef __ADAPTER_MAGAER_H__
#define __ADAPTER_MAGAER_H__
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
#include "DimesPacket.h"
#include "EthernetUtilities.h"
#include "JNIOutputCallback.h"

/**********************
 *
 * ADAPTER INTERFACE : maintains important information about 
 * the available network adapters.
 */
typedef struct _ADAPTER_INTERFACE
{  
	char adapterName[256];
	char ipAddress[16];
	char description[132];
	char gatewayAddress[16];
	char protocolDriverName[256];
	unsigned int type;
	unsigned char srcMAC[6];
	unsigned char destMAC[6];

	// PPPOE Frame : 
	bool isPPPOE;
	unsigned char pppoeFrame[8];

	bool srcMacValid;
	bool destMacValid;

} ADAPTER_INTERFACE, *LPADAPTER_INTERFACE;

/********************************************
 *
 * AdapterManager
 * Contains STATIC functions and definitions to help the discovery
 * and management of adapters and network interfaces.
 *
 * use this class in order to get the best available 
 * network adapter for the Treeroute measurement.
 *
 */
class AdapterManager
{
public:
	AdapterManager(void);
	~AdapterManager(void);
	static __declspec(dllexport) void getInterfaces(LPADAPTER_INTERFACE adaptersArray , int* numberOfInterfaces);
	static int getBestAdapterIndex(LPADAPTER_INTERFACE bestAdapter , int interfcaesNum);
	static void testAdaptersOID(char *adapterDeviceName);

private:
	static bool getDestMAC(char *gatewayAddress ,unsigned int *destMAC );
	static bool getSrcMAC(LPADAPTER adapter , unsigned char *macAddress);


};
#endif
#endif
