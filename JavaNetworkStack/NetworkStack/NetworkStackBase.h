#ifndef __NETWORK_STACK_BASE__
#define __NETWORK_STACK_BASE__
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
#include "JNIOutputCallback.h"
#include "JavaTime.h"
#ifdef DIMES_WINDOWS
#include <WinSock2.h>
#include <WS2tcpip.h>
#else
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#define Sleep usleep
#endif
#include <list>

using namespace std;

typedef struct PacketDetails {
	
	jbyte* packetBytes;
	bool isCopy;
	int packetSize;
	bool timeStampPacket;
	bool calculateIPChecksum;
	int timestampPosition; // Length is fixed = 8 bytes.
	int ipHeaderPosition;  // For checksum.

} PacketDetails;

typedef list<pair<jlong , PacketDetails > > PacketList;

//typedef list<pair<jlong , pair<jbyte*,int> > > PacketList ;

/***********************
 *
 * an abstract class representing a general Network stack
 * in order to implement an instance of this class one must define the following :
 * 1. a way to open the device (TODO : convert the init function to get more than 1 parameter)
 * 2. a method to close the device
 * 3. a method to send a packet and determine the send time
 * 4. a method to read a packet (select...) and determine the receive time
 *
 * Other than that , the packet builder should be adjusted accoring
 * to the device ( fro example , IPv6 packets will be build in a different way)
 *
 */
class NetworkStackBase
{
public:
	NetworkStackBase(void);
	~NetworkStackBase(void);

	// open / close / init: (virtual)
	virtual bool initialize(char **argv , int argc)=0;
	virtual int close()=0;
    
	// packet and callback registration : (non-virtual)
	void initializeCallback (JNIEnv * anEnvirmonet , jobject buffer , jobject callbackContext);	
	int getRegisteredPacketsNum();
	void setCallbackBufferSize(int bufferSize){ callbackBufferSize = bufferSize; }

	void registerPacket(jlong sendTime , jbyte *packetBytesArray , int packetSize);
	void registerPacket(jlong sendTime , jbyte *packetBytesArray , int packetSize , int timeStampPosition);
	void registerPacket(jlong sendTime , jbyte *packetBytesArray , int packetSize , int timeStampPosition , int ipChecksumPosition );

	// send-receive a single packet(virtual) - with options
	virtual jlong sendPacket(PacketDetails packet)=0;	
	virtual jlong sendPacket(jbyte *packetBytesArray , int packetSize )=0;	
	virtual jlong sendPacket(jbyte *packetBytesArray , int packetSize , int timeStampPosition , int chksumPos)=0;
	virtual int readPacket(struct timeval selectTime , jbyte *packet , jlong *receiveTimeMilisec , jlong *receiveTimeMicrosec)=0;

	// send the registered packets and general purpose receive (non-virtual) 
	void sendRegisteredPackets(jlong *sendTimesArray);
	void receive(jlong listenTimeout);

	// initialized indicat :
	bool isReady(){ return ready;}

protected:

	// buffer size :
	int callbackBufferSize;
	// java callbacks :
	JNIEnv *javaEnviroment;
	jobject callbackContext;
	jobject addPacketContext;
	jmethodID addPacketMid;
	jmethodID callbackEmptyMid;
	jmethodID callbackWithPacketMid;
	jmethodID clearPacketsMid;
	PacketList packetsList;
	// callback and buffer options :
	bool bufferPackets;
	bool performCallback;

	bool ready;

};

#endif
