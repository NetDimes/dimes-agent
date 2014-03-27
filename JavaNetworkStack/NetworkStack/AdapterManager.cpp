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
#include "AdapterManager.h"
#ifdef DIMES_WINDOWS
#include <stdio.h>
#include <tchar.h>

/******************
 * get the destination address of a gateway using ARP API.
 * Note that there are cases that this won't work , so 
 * there will probably have to be a usage of a function that
 * listens to the network connection and monitors the gateway IP and dest MAC.
 *
 */
bool __cdecl AdapterManager::getDestMAC(char *gatewayAddress ,unsigned int *destMAC )
{
	DWORD status;
	IPAddr destIP = inet_addr(gatewayAddress);
	ULONG   ulLen=6;
	void* voidPtr = (void*)&ulLen;
	PULONG ulongPtr = (PULONG)(voidPtr);
	void* voidPtr2 = (void*)destMAC;
	PULONG ulongPtr2 = (PULONG)(voidPtr2);
	status = SendARP(destIP , 0 , ulongPtr2 , ulongPtr);
	if (status!=NO_ERROR)
		return false;
	return true;
}

/********************
 * query the device for it's MAC address.
 *
 */
bool AdapterManager::getSrcMAC(LPADAPTER adapter , unsigned char *macAddress)
{
	PPACKET_OID_DATA  OidData;
	LONG		Status;

	if (adapter == NULL)
	{
		printf("adapter is null. returning.\n");
		return false;
	}

	// allocate packet data structure :
	OidData = ((PPACKET_OID_DATA)(void*)GlobalAllocPtr(GMEM_MOVEABLE | GMEM_ZEROINIT,512));
    if (OidData == NULL) {
        ODS("PacketGetLinkLayerFromRegistry failed\n");
        return false;
    }

	// Query MAC address from NIC :
	OidData->Oid = OID_802_3_CURRENT_ADDRESS;	// XXX At the moment only Ethernet is supported.
	// Waiting a patch to support other Link Layers
	OidData->Length = 256;
	ZeroMemory(OidData->Data, 256);

	Status = DimesPacketRequest(adapter, FALSE, OidData);
	if(Status)
	{
		memcpy(macAddress, OidData->Data, 6);
	}
	else
	{
		return false;
	}
	GlobalFreePtr (OidData);
	return true;
}

/*************
 * 
 * Find the best suitable Adapter (index) for sending packets.
 *
 * Parameters are an array of adapters ,and the size of it.
 */

int AdapterManager::getBestAdapterIndex(LPADAPTER_INTERFACE adaptersArray , int interfacesNum)
{
	//printf("Getting best interface\n");
	for (int i=0; i<interfacesNum; i++)
	{
		//printf("Adapter type : %d\n",adaptersArray[i].type);
		// Check for ethernet type :
		if (adaptersArray[i].type == MIB_IF_TYPE_ETHERNET)
		{
			// return the first with valid IP , source MAC and dest MAC
			unsigned long ip = inet_addr(adaptersArray[i].ipAddress);
			unsigned long gw = inet_addr(adaptersArray[i].gatewayAddress);
			if (ip!=0 && ip!=INADDR_NONE &&
				//gw!=0 && gw!=INADDR_NONE &&
				adaptersArray[i].srcMacValid &&
				adaptersArray[i].destMacValid)
			{
				return i;			
			}
		}
		
	}
	// return -1 if no device found...
	return -1;
}

/********************
 *
 * Query for all the interfaces and find their parameters.
 *
 */
__declspec(dllexport) void AdapterManager::getInterfaces(LPADAPTER_INTERFACE adaptersArray , int* numberOfInterfaces){
	PIP_ADAPTER_INFO pAdapterInfo;
	LPADAPTER adapter;
	PIP_ADAPTER_INFO pAdapter = NULL;
	DWORD dwRetVal = 0;
	ULONG ulOutBufLen;
	int maxInterfaces = *numberOfInterfaces;

	// allocate memory :
	pAdapterInfo = (IP_ADAPTER_INFO *) malloc( sizeof(IP_ADAPTER_INFO) );
	 ulOutBufLen = sizeof(IP_ADAPTER_INFO);

	// Make an initial call to GetAdaptersInfo to get
	// the necessary size into the ulOutBufLen variable
	if (GetAdaptersInfo( pAdapterInfo, &ulOutBufLen) == ERROR_BUFFER_OVERFLOW) {
		free(pAdapterInfo);
		pAdapterInfo = (IP_ADAPTER_INFO *) malloc (ulOutBufLen); 
	}

	if ((dwRetVal = GetAdaptersInfo( pAdapterInfo, &ulOutBufLen)) == NO_ERROR) {
		int adapterCounter = 0;
		pAdapter = pAdapterInfo;		
		// For Each queried adapter :
		while (pAdapter && adapterCounter<maxInterfaces) 
		{
			LPADAPTER_INTERFACE currentInterface = &adaptersArray[adapterCounter];

			//PIP_ADDR_STRING gateways = &pAdapter->GatewayList;
			//do
			//{
			//	printf("Gateway : %s\n",gateways->IpAddress.String);
			//	gateways = gateways->Next;
			//}
			//while (gateways != NULL);
			
			// copy the given parameters from the LPADAPTER_INTERFACE struct :
			strcpy(currentInterface->adapterName , pAdapter->AdapterName);
			strcpy(currentInterface->gatewayAddress , pAdapter->GatewayList.IpAddress.String);
			strcpy(currentInterface->ipAddress , pAdapter->IpAddressList.IpAddress.String);
			strcpy(currentInterface->description , pAdapter->Description);
			// set the driver name as requested by windows drivers :
			sprintf(currentInterface->protocolDriverName,"DIMESPACKET_%s",pAdapter->AdapterName);
			currentInterface->type = pAdapter->Type;

			// Open adapter for query :
			adapter = DimesPacketOpenAdapter(currentInterface->protocolDriverName);
			if (adapter != NULL)
			{
				// query source MAC :
				currentInterface->srcMacValid = getSrcMAC(adapter , &(currentInterface->srcMAC[0]));
				printf("Source MAC :");printFormat(currentInterface->srcMAC , 6);printf("\n");
				// get the dest MAC
				void *destMacPtr = (void*)&(currentInterface->destMAC[0]);
				unsigned int *destMacPtrUI8 = (unsigned int *)destMacPtr;
				currentInterface->destMacValid = getDestMAC(currentInterface->gatewayAddress ,destMacPtrUI8 );			
JavaLog::javalogf(LEVEL_INFO , "gateway address : %s\n",currentInterface->gatewayAddress);
				currentInterface->isPPPOE = false;
				// try and find out if the device is PPP :
				if (!currentInterface->destMacValid)
				{				
					JavaLog::javalog(LEVEL_INFO , "--->Listening for PPP dest MAC...\n");	
					DimesPacketSetHwFilter(adapter,NDIS_PACKET_TYPE_PROMISCUOUS);
					DimesPacketSetBuff(adapter , 2048);
					char packetBuffer[2048];
					DimesPacketSetReadTimeout(adapter , 2500);
					LPPACKET receviedPacket =  DimesPacketAllocatePacket();
					DimesPacketInitPacket(receviedPacket , &packetBuffer , 2048);
					// TODO :generate some traffic :					
					DimesPacketReceivePacket(adapter , receviedPacket , TRUE);						
					JavaLog::javalogf(LEVEL_INFO ,"Packet Size : %d" , receviedPacket->ulBytesReceived );
					
					// IF a packet was intercepted : check the parameters ...
					if (receviedPacket->ulBytesReceived >=24)
					{
						unsigned char* srcMac = (unsigned char*)(packetBuffer+20);
						unsigned char* destMac = (unsigned char*)(packetBuffer+26);
						u8 ppp[2] = { 0X00 , 0x21};
						u8 pppoeFrameType[2] = { 0x11 , 0x00 };
						unsigned char* pppFrameBegining = (unsigned char*)(packetBuffer+34);
						//printf("idial : "); printFormat(pppoeFrameType,2);printf("\n");
						//printf("actual: ");printFormat((unsigned char*)pppFrameBegining,2);printf("\n"); 
						if (memicmp(pppFrameBegining,pppoeFrameType,2) == 0)
						{
							JavaLog::javalog(LEVEL_FINE , "Euricka!!!!! - found ppp device!");
							currentInterface->isPPPOE = true;
							memcpy(currentInterface->pppoeFrame , packetBuffer+34 , 8);
						}						

						//printFormat(srcMac,6);printf("\n");
						
						printFormat((unsigned char*)receviedPacket->Buffer , receviedPacket->ulBytesReceived);
						if (memicmp(currentInterface->srcMAC , packetBuffer+20,6) == 0)
							memcpy(currentInterface->destMAC  , packetBuffer+26 , 6);
						else
							if (memicmp(currentInterface->srcMAC , packetBuffer+26,6) == 0)
								memcpy(currentInterface->destMAC , packetBuffer+20 , 6);
						currentInterface->destMacValid=true;
						//currentInterface->
						//printf("dest mac:");printFormat(currentInterface->destMAC,6);printf("\n");
						//system("pause");
					}
					DimesPacketFreePacket(receviedPacket);				
				}				
				DimesPacketCloseAdapter(adapter);
			}


			pAdapter = pAdapter->Next;
			adapterCounter++;
		}
		*numberOfInterfaces = adapterCounter;
	}
	else 
	{
		JavaLog::javalogf(LEVEL_SEVERE , "Call to GetAdaptersInfo failed.");
	}
	free(pAdapterInfo);
	fflush(stdout);
}
#endif
