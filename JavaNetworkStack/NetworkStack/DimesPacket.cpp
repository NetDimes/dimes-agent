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
#include <stdio.h>
#include <stdlib.h>
#include "DimesPacket.h"
#include "AdapterManager.h"

/***********
 * 
 * a nice way to print an error : can be plaved in javalog(..)
 *
 */
void printError(DWORD lastError)
{
	LPVOID lpMsgBuf;
	FormatMessage( 
		FORMAT_MESSAGE_ALLOCATE_BUFFER | 
		FORMAT_MESSAGE_FROM_SYSTEM | 
		FORMAT_MESSAGE_IGNORE_INSERTS,
		NULL,
		lastError,
		MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
		(LPTSTR) &lpMsgBuf,
		0,
		NULL 
		);
	printf("Error : %s\n",(LPCTSTR)lpMsgBuf);

}


void printFormat(unsigned char * hexArray , int arraySize){
	int i;
	if (arraySize == 0 )
		return;
	for (i=0; i<arraySize-1; i++)
		printf("%02X-",hexArray[i]);
	printf("%02X",hexArray[arraySize-1]);
}


PCHAR WChar2SChar(PWCHAR string)
{
	PCHAR TmpStr;
	TmpStr = (CHAR*) GlobalAllocPtr(GMEM_MOVEABLE | GMEM_ZEROINIT, (wcslen(string)+2));

	// Conver to ASCII
	WideCharToMultiByte(
		CP_ACP,
		0,
		string,
		-1,
		TmpStr,
		(wcslen(string)+2),          // size of buffer
		NULL,
		NULL);

	return TmpStr;
}


BOOLEAN DimesPacketSetReadEvt(LPADAPTER AdapterObject)
{
	DWORD BytesReturned;
	char EventName[100];
	DWORD lastError;

	if (LOWORD(GetVersion()) == 4)
	{
		// retrieve the name of the shared event from the driver without the "Global\\" prefix
		if(DeviceIoControl(AdapterObject->hFile,pBIOCEVNAME,NULL,0,EventName,3*13*sizeof(TCHAR),&BytesReturned,NULL)==FALSE) 
			return FALSE;

		EventName[BytesReturned/sizeof(TCHAR)]=0; // terminate the string
	}
	else
	{		
		PCHAR name;
		// this tells the terminal service to retrieve the event from the global namespace
		// retrieve the name of the shared event from the driver with the "Global\\" prefix
		if(DeviceIoControl(AdapterObject->hFile,pBIOCEVNAME,NULL,0,EventName + 7,93,&BytesReturned,NULL)==FALSE) 
			return FALSE;
		void* str2 = (void*)(EventName+7);
		PWCHAR string = (PWCHAR)str2;
		name = WChar2SChar(/*EventName+7*/string);
		name[BytesReturned/2]='\0';
		sprintf(EventName,"Global\\%s",name);
		GlobalFreePtr(name);
	}
	
	JavaLog::javalogf(LEVEL_INFO,"event name :%s" , EventName);

	// open the shared event
	AdapterObject->ReadEvent=CreateEvent(NULL,
		TRUE,
		FALSE,
		EventName);

	lastError = GetLastError();

	if(AdapterObject->ReadEvent==NULL || lastError!=ERROR_ALREADY_EXISTS){
		printf("PacketSetReadEvt: error retrieving the event from the kernel\n");
		printError(lastError);
		return FALSE;
	}
	else
		JavaLog::javalogf(LEVEL_INFO,"Read event success\n");

	AdapterObject->ReadTimeOut=0;

	return TRUE;
}

BOOLEAN DimesPacketRequest(LPADAPTER  AdapterObject,BOOLEAN Set,PPACKET_OID_DATA  OidData)
{
	DWORD		BytesReturned;
	BOOLEAN		Result;

	Result=DeviceIoControl(AdapterObject->hFile,(DWORD) Set ? (DWORD)pBIOCSETOID : (DWORD)pBIOCQUERYOID,
		OidData,sizeof(PACKET_OID_DATA)-1+OidData->Length,OidData,
		sizeof(PACKET_OID_DATA)-1+OidData->Length,&BytesReturned,NULL);

	// output some debug info
	//printf("PacketRequest, OID=%d ", OidData->Oid);
	//printf("Length=%d ", OidData->Length);
	//printf("Set=%d ", Set);
	//printf("Res=%d\n", Result);

	return Result;
}

BOOLEAN DimesPacketSetMaxLookaheadsize (LPADAPTER AdapterObject)
{
	BOOLEAN    Status;
	ULONG      IoCtlBufferLength=(sizeof(PACKET_OID_DATA)+sizeof(ULONG)-1);
	PPACKET_OID_DATA  OidData;

	LPVOID OidDataPtr = GlobalAllocPtr(GMEM_MOVEABLE | GMEM_ZEROINIT,IoCtlBufferLength);
	void* OidDataVoidPtr = (void*)OidDataPtr;
	OidData = (PPACKET_OID_DATA)OidDataVoidPtr ;
	if (OidData == NULL) {
		//printf("PacketSetMaxLookaheadsize failed\n");
		return FALSE;
	}

	//set the size of the lookahead buffer to the maximum available by the the NIC driver
	OidData->Oid=OID_GEN_MAXIMUM_LOOKAHEAD;
	OidData->Length=sizeof(ULONG);
	Status=DimesPacketRequest(AdapterObject,FALSE,OidData);
	OidData->Oid=OID_GEN_CURRENT_LOOKAHEAD;
	Status=DimesPacketRequest(AdapterObject,TRUE,OidData);
	GlobalFreePtr(OidData);
	return Status;
}


LPADAPTER DimesPacketOpenAdapter(PCHAR AdapterName)
{
	LPADAPTER lpAdapter;
	DWORD error;
	SC_HANDLE svcHandle = NULL;
	SC_HANDLE scmHandle = NULL;
	
	WCHAR SymbolicLink[128];

	lpAdapter=(LPADAPTER)GlobalAllocPtr(GMEM_MOVEABLE | GMEM_ZEROINIT, sizeof(ADAPTER));
	if (lpAdapter==NULL)
	{
		printf("PacketOpenAdapterNPF: GlobalAlloc Failed\n");
		error=GetLastError();
		//set the error to the one on which we failed
		SetLastError(error);
		printf("PacketOpenAdapterNPF: Failed to allocate the adapter structure\n");
		return NULL;
	}
	lpAdapter->NumWrites=1;
	void *SymbolicLinkVoid = (void*)SymbolicLink;
	LPCTSTR SymbolicLinkLPCTSTR = (LPCTSTR)SymbolicLinkVoid;
	char* SymbolicLinkPCHAR = (char*)SymbolicLinkVoid;

	/*if (LOWORD(GetVersion()) == 4)
		wsprintf(SymbolicLink,TEXT("\\\\.\\%s"),&AdapterName[16]);
	else
		wsprintf(SymbolicLink,TEXT("\\\\.\\Global\\%s"),&AdapterName[0]);*/
	if (LOWORD(GetVersion()) == 4)
		sprintf(SymbolicLinkPCHAR,TEXT("\\\\.\\%s"),&AdapterName[16]);
	else
		sprintf(SymbolicLinkPCHAR,TEXT("\\\\.\\Global\\%s"),&AdapterName[0]);

	JavaLog::javalogf(LEVEL_INFO , "attempting symbolic link : %s",SymbolicLink);
	// Copy  only the bytes that fit in the adapter structure.
	// Note that lpAdapter->SymbolicLink is present for backward compatibility but will
	// never be used by the apps
	memcpy(lpAdapter->SymbolicLink, (PCHAR)SymbolicLink, MAX_LINK_NAME_LENGTH);
	//try if it is possible to open the adapter immediately
	lpAdapter->hFile=CreateFile(SymbolicLinkLPCTSTR,GENERIC_WRITE | GENERIC_READ,
		0,NULL,OPEN_EXISTING,0,0);

	if (lpAdapter->hFile != INVALID_HANDLE_VALUE) 
	{

		if(DimesPacketSetReadEvt(lpAdapter)==FALSE){
			error=GetLastError();
			JavaLog::javalogf(LEVEL_SEVERE , "PacketOpenAdapterNPF: Unable to open the read event");
			GlobalFreePtr(lpAdapter);
			//set the error to the one on which we failed
			SetLastError(error);
			JavaLog::javalogf(LEVEL_SEVERE , "PacketOpenAdapterNPF: PacketSetReadEvt failed, LastError=%d",error);
			return NULL;
		}		

		DimesPacketSetMaxLookaheadsize(lpAdapter);

		_snprintf(lpAdapter->Name, ADAPTER_NAME_LENGTH, "%S", AdapterName);

		return lpAdapter;
	}
	error=GetLastError();
	GlobalFreePtr(lpAdapter);
	//set the error to the one on which we failed
	//printf("PacketOpenAdapterNPF: CreateFile failed, LastError= %d\n",error);
	//printError(error);
	SetLastError(error);
	return NULL;
}

BOOLEAN DimesPacketSetHwFilter(LPADAPTER  AdapterObject,ULONG Filter)
{
    BOOLEAN    Status;
    ULONG      IoCtlBufferLength=(sizeof(PACKET_OID_DATA)+sizeof(ULONG)-1);
    PPACKET_OID_DATA  OidData;

	LPVOID OidDataPtr = GlobalAllocPtr(GMEM_MOVEABLE | GMEM_ZEROINIT,IoCtlBufferLength);
	void* OidDataVoidPtr = (void*)OidDataPtr;
	OidData = (PPACKET_OID_DATA)OidDataVoidPtr ;
	//OidData=GlobalAllocPtr(GMEM_MOVEABLE | GMEM_ZEROINIT,IoCtlBufferLength);
    if (OidData == NULL) {
        printf("PacketSetHwFilter: GlobalAlloc Failed\n");
        return FALSE;
    }
    OidData->Oid=OID_GEN_CURRENT_PACKET_FILTER;
    OidData->Length=sizeof(ULONG);
    *((PULONG)OidData->Data)=Filter;
    Status=DimesPacketRequest(AdapterObject,TRUE,OidData);
    GlobalFreePtr(OidData);
    return Status;
}

VOID DimesPacketCloseAdapter(LPADAPTER lpAdapter)
{
	if(!lpAdapter)
	{
        printf("PacketCloseAdapter: attempt to close a NULL adapter\n");
		return;
	}
	
	CloseHandle(lpAdapter->hFile);
	SetEvent(lpAdapter->ReadEvent);
    CloseHandle(lpAdapter->ReadEvent);
    GlobalFreePtr(lpAdapter);
}

LPPACKET DimesPacketAllocatePacket(void)
{

    LPPACKET    lpPacket;
    lpPacket=(LPPACKET)GlobalAllocPtr(GMEM_MOVEABLE | GMEM_ZEROINIT,sizeof(PACKET));
    if (lpPacket==NULL)
    {
        ODS("PacketAllocatePacket: GlobalAlloc Failed\n");
        return NULL;
    }
    return lpPacket;
}


VOID DimesPacketFreePacket(LPPACKET lpPacket)
{
    GlobalFreePtr(lpPacket);
}

VOID DimesPacketInitPacket(LPPACKET lpPacket,PVOID Buffer,UINT Length)

{
    lpPacket->Buffer = Buffer;
    lpPacket->Length = Length;
	lpPacket->ulBytesReceived = 0;
	lpPacket->bIoComplete = FALSE;
}


BOOLEAN DimesPacketSetBuff(LPADAPTER AdapterObject,int dim)
{
	DWORD BytesReturned;
    return DeviceIoControl(AdapterObject->hFile,pBIOCSETBUFFERSIZE,&dim,4,NULL,0,&BytesReturned,NULL);
}

BOOLEAN DimesPacketSetReadTimeout(LPADAPTER AdapterObject,int timeout)
{
	DWORD BytesReturned;
	int DriverTimeOut=-1;
	AdapterObject->ReadTimeOut=timeout;
    return DeviceIoControl(AdapterObject->hFile,pBIOCSRTIMEOUT,&DriverTimeOut,4,NULL,0,&BytesReturned,NULL);
}


BOOLEAN DimesPacketSendPacket(LPADAPTER AdapterObject,LPPACKET lpPacket,BOOLEAN Sync)
{
    DWORD        BytesTransfered;
    return WriteFile(AdapterObject->hFile,lpPacket->Buffer,lpPacket->Length,&BytesTransfered,NULL);
}

BOOLEAN DimesPacketReceivePacket(LPADAPTER AdapterObject,LPPACKET lpPacket,BOOLEAN Sync)
{
	BOOLEAN res;
	if((int)AdapterObject->ReadTimeOut != -1)
		WaitForSingleObject(AdapterObject->ReadEvent, (AdapterObject->ReadTimeOut==0)?INFINITE:AdapterObject->ReadTimeOut);
    res = ReadFile(AdapterObject->hFile, lpPacket->Buffer, lpPacket->Length, &lpPacket->ulBytesReceived,NULL);
	return res;
}

BOOLEAN DimesPacketSetBpf(LPADAPTER AdapterObject, struct bpf_program *fp)
{
	DWORD BytesReturned;
   return DeviceIoControl(AdapterObject->hFile,pBIOCSETF,(char*)fp->bf_insns,fp->bf_len*sizeof(struct bpf_insn),NULL,0,&BytesReturned,NULL);
}


//int main(int argc, char* argv[])
//{
//	ADAPTER_INTERFACE adapters[20];
//	int maxAdapters = 20;
//
//	 AdapterManager::getInterfaces(&adapters[0] , &maxAdapters);
//	printf("found %d Adapters...\n",maxAdapters);
//	for (int i=0; i<maxAdapters; i++)
//	{
//			printf("Source MAC : ");printFormat(adapters[i].srcMAC,6);printf("\n");
//			printf("Dest MAC : ");printFormat(adapters[i].destMAC,6);printf("\n");
//			printf("\tDriver name : \t%s\n",adapters[i].protocolDriverName);
//			printf("\tAdapter Name: \t%s\n", adapters[i].adapterName);
//			printf("\tAdapter Desc: \t%s\n", adapters[i].description);
//			printf("\tIP Address: \t%s\n", adapters[i].ipAddress);
//			printf("\tGateway: \t%s\n",adapters[i].gatewayAddress);
//			printf("\tIs Ethernet : %s\n",(adapters[i].type == MIB_IF_TYPE_ETHERNET ?
//				"TRUE":"FALSE"));
//	}
//	system("pause");
//	return 0;
//}
//
#endif
