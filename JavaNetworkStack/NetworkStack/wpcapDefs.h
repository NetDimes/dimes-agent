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
/***************************************************************************
 * Copyright (c) 1999 - 2003
 * NetGroup, Politecnico di Torino (Italy)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Politecnico di Torino nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

/************************************************************/
/*															*/
/*	Ohad , 1/10/2005 :										*/
/*   Those definitions were added in order to support		*/
/*	 the execution and compilation of several Packet32		*/
/*	 related functions	.									*/
/*															*/
/*	Those definitions are general definitions ment for		*/
/*  accesing the DimesPacket driver and several helpful		*/
/*  winpcap functions.										*/
/************************************************************/

#ifndef __PACKET32
#define __PACKET32
#ifdef DIMES_WINDOWS
#include <WinSock2.h>


// Working modes
#define PACKET_MODE_CAPT 0x0 ///< Capture mode
#define PACKET_MODE_STAT 0x1 ///< Statistical mode
#define PACKET_MODE_MON 0x2 ///< Monitoring mode
#define PACKET_MODE_DUMP 0x10 ///< Dump mode
#define PACKET_MODE_STAT_DUMP MODE_DUMP | MODE_STAT ///< Statistical dump Mode

// ioctls
#define FILE_DEVICE_PROTOCOL        0x8000

#define IOCTL_PROTOCOL_STATISTICS   CTL_CODE(FILE_DEVICE_PROTOCOL, 2 , METHOD_BUFFERED, FILE_ANY_ACCESS)
#define IOCTL_PROTOCOL_RESET        CTL_CODE(FILE_DEVICE_PROTOCOL, 3 , METHOD_BUFFERED, FILE_ANY_ACCESS)
#define IOCTL_PROTOCOL_READ         CTL_CODE(FILE_DEVICE_PROTOCOL, 4 , METHOD_BUFFERED, FILE_ANY_ACCESS)
#define IOCTL_PROTOCOL_WRITE        CTL_CODE(FILE_DEVICE_PROTOCOL, 5 , METHOD_BUFFERED, FILE_ANY_ACCESS)
#define IOCTL_PROTOCOL_MACNAME      CTL_CODE(FILE_DEVICE_PROTOCOL, 6 , METHOD_BUFFERED, FILE_ANY_ACCESS)
#define IOCTL_OPEN                  CTL_CODE(FILE_DEVICE_PROTOCOL, 7 , METHOD_BUFFERED, FILE_ANY_ACCESS)
#define IOCTL_CLOSE                 CTL_CODE(FILE_DEVICE_PROTOCOL, 8 , METHOD_BUFFERED, FILE_ANY_ACCESS)

#define	 pBIOCSETBUFFERSIZE 9592		///< IOCTL code: set kernel buffer size.
#define	 pBIOCSETF 9030					///< IOCTL code: set packet filtering program.
#define  pBIOCGSTATS 9031				///< IOCTL code: get the capture stats.
#define	 pBIOCSRTIMEOUT 7416			///< IOCTL code: set the read timeout.
#define	 pBIOCSMODE 7412				///< IOCTL code: set working mode.
#define	 pBIOCSWRITEREP 7413			///< IOCTL code: set number of physical repetions of every packet written by the app.
#define	 pBIOCSMINTOCOPY 7414			///< IOCTL code: set minimum amount of data in the kernel buffer that unlocks a read call.
#define	 pBIOCSETOID 2147483648			///< IOCTL code: set an OID value.
#define	 pBIOCQUERYOID 2147483652		///< IOCTL code: get an OID value.
#define	 pATTACHPROCESS 7117			///< IOCTL code: attach a process to the driver. Used in Win9x only.
#define	 pDETACHPROCESS 7118			///< IOCTL code: detach a process from the driver. Used in Win9x only.
#define  pBIOCSETDUMPFILENAME 9029		///< IOCTL code: set the name of a the file used by kernel dump mode.
#define  pBIOCEVNAME 7415				///< IOCTL code: get the name of the event that the driver signals when some data is present in the buffer.
#define  pBIOCSENDPACKETSNOSYNC 9032	///< IOCTL code: Send a buffer containing multiple packets to the network, ignoring the timestamps associated with the packets.
#define  pBIOCSENDPACKETSSYNC 9033		///< IOCTL code: Send a buffer containing multiple packets to the network, respecting the timestamps associated with the packets.
#define  pBIOCSETDUMPLIMITS 9034		///< IOCTL code: Set the dump file limits. See the PacketSetDumpLimits() function.
#define  pBIOCISDUMPENDED 7411			///< IOCTL code: Get the status of the kernel dump process. See the PacketIsDumpEnded() function.

#define  pBIOCSTIMEZONE 7471			///< IOCTL code: set time zone. Used in Win9x only.


/// Alignment macro. Defines the alignment size.
#define Packet_ALIGNMENT sizeof(int)
/// Alignment macro. Rounds up to the next even multiple of Packet_ALIGNMENT. 
#define Packet_WORDALIGN(x) (((x)+(Packet_ALIGNMENT-1))&~(Packet_ALIGNMENT-1))


#define NdisMediumNull	-1		// Custom linktype: NDIS doesn't provide an equivalent
#define NdisMediumCHDLC	-2		// Custom linktype: NDIS doesn't provide an equivalent
#define NdisMediumPPPSerial	-3	// Custom linktype: NDIS doesn't provide an equivalent


#define        DOSNAMEPREFIX   TEXT("Packet_")	///< Prefix added to the adapters device names to create the WinPcap devices
#define        MAX_LINK_NAME_LENGTH	64			//< Maximum length of the devices symbolic links
#define        NMAX_PACKET 65535


#define ADAPTER_NAME_LENGTH 256 + 12	///<  Maximum length for the name of an adapter. The value is the same used by the IP Helper API.
#define ADAPTER_DESC_LENGTH 128			///<  Maximum length for the description of an adapter. The value is the same used by the IP Helper API.
#define MAX_MAC_ADDR_LENGTH 8			///<  Maximum length for the link layer address of an adapter. The value is the same used by the IP Helper API.
#define MAX_NETWORK_ADDRESSES 16		///<  Maximum length for the link layer address of an adapter. The value is the same used by the IP Helper API.


typedef struct WAN_ADAPTER_INT WAN_ADAPTER; ///< Describes an opened wan (dialup, VPN...) network adapter using the NetMon API
typedef WAN_ADAPTER *PWAN_ADAPTER; ///< Describes an opened wan (dialup, VPN...) network adapter using the NetMon API

#define INFO_FLAG_NDIS_ADAPTER		0	///< Flag for ADAPTER_INFO: this is a traditional ndis adapter
#define INFO_FLAG_NDISWAN_ADAPTER	1	///< Flag for ADAPTER_INFO: this is a NdisWan adapter
#define INFO_FLAG_DAG_CARD			2	///< Flag for ADAPTER_INFO: this is a DAG card
#define INFO_FLAG_DAG_FILE			6	///< Flag for ADAPTER_INFO: this is a DAG file
#define INFO_FLAG_DONT_EXPORT		8	///< Flag for ADAPTER_INFO: when this flag is set, the adapter will not be listed or openend by winpcap. This allows to prevent exporting broken network adapters, like for example FireWire ones.

typedef struct _ADAPTER  { 
	HANDLE hFile;				///< \internal Handle to an open instance of the NPF driver.
	CHAR  SymbolicLink[MAX_LINK_NAME_LENGTH]; ///< \internal A string containing the name of the network adapter currently opened.
	int NumWrites;				///< \internal Number of times a packets written on this adapter will be repeated 
								///< on the wire.
	HANDLE ReadEvent;			///< A notification event associated with the read calls on the adapter.
								///< It can be passed to standard Win32 functions (like WaitForSingleObject
								///< or WaitForMultipleObjects) to wait until the driver's buffer contains some 
								///< data. It is particularly useful in GUI applications that need to wait 
								///< concurrently on several events. In Windows NT/2000 the PacketSetMinToCopy()
								///< function can be used to define the minimum amount of data in the kernel buffer
								///< that will cause the event to be signalled. 
	
	UINT ReadTimeOut;			///< \internal The amount of time after which a read on the driver will be released and 
								///< ReadEvent will be signaled, also if no packets were captured
	CHAR Name[ADAPTER_NAME_LENGTH];
	PWAN_ADAPTER pWanAdapter;
	UINT Flags;					///< Adapter's flags. Tell if this adapter must be treated in a different way, using the Netmon API or the dagc API.

}  ADAPTER, *LPADAPTER;

/*!
  \brief Structure that contains a group of packets coming from the driver.

  This structure defines the header associated with every packet delivered to the application.
*/
typedef struct _PACKET {  
	HANDLE       hEvent;		///< \deprecated Still present for compatibility with old applications.
	OVERLAPPED   OverLapped;	///< \deprecated Still present for compatibility with old applications.
	PVOID        Buffer;		///< Buffer with containing the packets. See the PacketReceivePacket() for
								///< details about the organization of the data in this buffer
	UINT         Length;		///< Length of the buffer
	DWORD        ulBytesReceived;	///< Number of valid bytes present in the buffer, i.e. amount of data
									///< received by the last call to PacketReceivePacket()
	BOOLEAN      bIoComplete;	///< \deprecated Still present for compatibility with old applications.
}  PACKET, *LPPACKET;

/*!
  \brief Structure containing an OID request.

  It is used by the PacketRequest() function to send an OID to the interface card driver. 
  It can be used, for example, to retrieve the status of the error counters on the adapter, its MAC address, 
  the list of the multicast groups defined on it, and so on.
*/
struct _PACKET_OID_DATA {
    ULONG Oid;					///< OID code. See the Microsoft DDK documentation or the file ntddndis.h
								///< for a complete list of valid codes.
    ULONG Length;				///< Length of the data field
    UCHAR Data[1];				///< variable-lenght field that contains the information passed to or received 
								///< from the adapter.
}; 
typedef struct _PACKET_OID_DATA PACKET_OID_DATA, *PPACKET_OID_DATA;


#if _DBG
#define ODS(_x) OutputDebugString(TEXT(_x))
#define ODSEx(_x, _y)
#else
#ifdef _DEBUG_TO_FILE
/*! 
  \brief Macro to print a debug string. The behavior differs depending on the debug level
*/
#define ODS(_x) { \
	FILE *f; \
	f = fopen("winpcap_debug.txt", "a"); \
	fprintf(f, "%s", _x); \
	fclose(f); \
}
/*! 
  \brief Macro to print debug data with the printf convention. The behavior differs depending on 
  the debug level
*/
#define ODSEx(_x, _y) { \
	FILE *f; \
	f = fopen("winpcap_debug.txt", "a"); \
	fprintf(f, _x, _y); \
	fclose(f); \
}



LONG PacketDumpRegistryKey(PCHAR KeyName, PCHAR FileName);
#else
#define ODS(_x)		
#define ODSEx(_x, _y)
#endif
#endif



#ifdef __cplusplus
extern "C" {
#endif


// The following is used to check the adapter name in PacketOpenAdapterNPF and prevent 
// opening of firewire adapters 
#define FIREWIRE_SUBSTR L"1394"


#ifdef __cplusplus
}
#endif 
#endif // DIMES_WINDOWS
#endif //__PACKET32

