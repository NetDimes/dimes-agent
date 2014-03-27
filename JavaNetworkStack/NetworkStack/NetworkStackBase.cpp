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
#include "NetworkStackBase.h"

NetworkStackBase::NetworkStackBase(void)
{
	// default callback buffer size :
	callbackBufferSize=10;
	// java callbacks :
	javaEnviroment=0;
	callbackContext=0;
	addPacketContext=0;
	addPacketMid=0;
	callbackEmptyMid=0;
	callbackWithPacketMid=0;
	clearPacketsMid=0;
	// options :
	bufferPackets=false;
	performCallback=false;

}

NetworkStackBase::~NetworkStackBase(void)
{
}


// Initialize the callback and the packet buffer function :
void NetworkStackBase::initializeCallback (JNIEnv * anEnvirmonet , jobject buffer , jobject aCallbackContext){
	javaEnviroment = anEnvirmonet;
	callbackContext= aCallbackContext;
	addPacketContext = buffer;
	// reset :
	bufferPackets=false;
	performCallback=false;
	// initialize buffer context :
	if (buffer != NULL)
	{
		//JavaLog::javalog(LEVEL_INFO , "Initializing buffer...");
		bufferPackets = true;
		jclass cls = javaEnviroment->GetObjectClass(buffer);	
		addPacketMid = javaEnviroment->GetMethodID(cls, "addPacket", "([BIJJ)V");
		clearPacketsMid = javaEnviroment->GetMethodID(cls, "clear", "()V");
	}
	// initialize callback context :
	if (callbackContext != NULL)
	{
		//JavaLog::javalog(LEVEL_INFO , "Initializing callback...");
		performCallback=true;
		jclass cls = javaEnviroment->GetObjectClass(callbackContext);	
		callbackEmptyMid = javaEnviroment->GetMethodID(cls, "callback", "()Z");
		callbackWithPacketMid = javaEnviroment->GetMethodID(cls, "callback", "([BJJ)Z");		
	}	
}

void NetworkStackBase::registerPacket(jlong sendTime , jbyte *packetBytesArray , int packetSize ){
	registerPacket(sendTime , packetBytesArray , packetSize , -1 , -1);
}

void NetworkStackBase::registerPacket(jlong sendTime , jbyte *packetBytesArray , int packetSize , int timeStampPosition)
{
	registerPacket(sendTime , packetBytesArray , packetSize , timeStampPosition , -1);
}

// Register a packet in the packets list to send :
// Please note that all packets must arrive sorted according to the send time
// TODO :  add a clear option ?
void NetworkStackBase::registerPacket(jlong sendTime , jbyte *packetBytesArray , int packetSize ,
	int aTimeStampPosition , int anIpHeaderPosition)
{
//	JavaLog::javalogf(LEVEL_INFO,"Registering Packet length : %d time: %d ", packetSize,sendTime , 
//		aTimeStampPosition,anIpHeaderPosition);
	// New School : 
	pair<jlong , PacketDetails > packetToAdd;
	packetToAdd.first = sendTime;
	packetToAdd.second.packetBytes = new jbyte[packetSize];
	memcpy(packetToAdd.second.packetBytes, packetBytesArray, packetSize);
	packetToAdd.second.packetSize= packetSize;
	packetToAdd.second.timeStampPacket = (aTimeStampPosition >0 );
	packetToAdd.second.calculateIPChecksum= (anIpHeaderPosition >0 );
	packetToAdd.second.timestampPosition = aTimeStampPosition;
	packetToAdd.second.ipHeaderPosition = anIpHeaderPosition;


	// Old school :
	//pair<jlong , pair<jbyte*,int> > pakcetToAdd;
	//pakcetToAdd.first = sendTime;
	//pakcetToAdd.second.first = packetBytesArray;
	//pakcetToAdd.second.second= packetSize;
	// push that back :
	packetsList.push_back(packetToAdd);
}

// return the number of registered packets to send
int NetworkStackBase::getRegisteredPacketsNum(){
	return (int)packetsList.size();
}

// send all the registered packets in their relative times :
void NetworkStackBase::sendRegisteredPackets(jlong *sendTimesArray){
	//JavaLog::javalogf(LEVEL_INFO,"Sending %d Packets...",packetsList.size());
	PacketList::iterator iEnd=packetsList.end();
	__int64 startTime = JavaTime::javaTimeNanos();
	int counter=0;
	int lastSendTime = 0;
	list<jbyte* > buffers;

	// // iterate over the packets and send :
	for(PacketList::iterator iter = packetsList.begin(); iter != iEnd; iter++)
	{
		//JavaLog::javalogf(LEVEL_INFO,"Sending %dth packet",counter);
		// determine how much to sleep
		pair<jlong , PacketDetails > packetDetails = *iter;
		//JavaLog::javalogf(LEVEL_INFO,"Checking time"); 
		if (lastSendTime != packetDetails.first)
		{
			__int64 sendTime = startTime + packetDetails.first*1000;
			__int64 currentTime = JavaTime::javaTimeNanos();
			int sleepTime = ((int)((sendTime - currentTime)/1000));
			//JavaLog::javalogf(LEVEL_INFO , "Sleeping for %d miliseconds..." , sleepTime) ;
			if (sleepTime > 0 )
				Sleep(sleepTime);		
			//JavaLog::javalogf(LEVEL_INFO,"Sending in length %d Packet at time %d", packetDetails.second.packetSize,packetDetails.first );
		}		
		//JavaLog::javalogf(LEVEL_INFO,"reading last send time");
		lastSendTime = packetDetails.first;
		// send packet and grab send time :		
		//JavaLog::javalogf(LEVEL_INFO,"sending packet");
		sendTimesArray[counter++] = sendPacket(packetDetails.second);
		buffers.push_front(packetDetails.second.packetBytes);
		//JavaLog::javalogf(LEVEL_INFO,"sending packet done");
		//delete packetDetails.second.packetBytes;
		//packetDetails.second.packetBytes = NULL;
	}
	while (buffers.size() != 0) {
		jbyte* buf = buffers.front();
		buffers.pop_front();
		delete buf;
	}

	// in the end : clear the packets list :
	packetsList.clear();
	//JavaLog::javalogf(LEVEL_INFO,"done sending packets");
}

// receive packets for a maximum of listen period :
// Please note that the CallbackContext can determine whether a packet 
// finished the listening period.
void NetworkStackBase::receive(jlong listenPeriod){
	__int64 listenStartTime;
	__int64 listenTimeout;
	jbyte packet[2048];

	listenStartTime = JavaTime::javaTimeNanos();
	//JavaTime::getMicroSecs(&listenStartTime , frequency);
	listenTimeout = listenStartTime + (__int64)(listenPeriod*1000);	// wait for this period 
	bool doneReceive = false;
	int bufferedPackets=0;
	
	while(!doneReceive)
	{
		// determine how long to wait for a packet :
		__int64 now;
		struct timeval selectTime;
		now = JavaTime::javaTimeNanos();		
		JavaTime::getMicroSecDiff(now , listenTimeout, &selectTime);
		//JavaLog::javalogf(LEVEL_FINE , "Testing select for %d sec %d usecs..." , selectTime.tv_sec,selectTime.tv_usec);
		if ( selectTime.tv_sec < 0 || (selectTime.tv_sec == 0 && selectTime.tv_usec<100000))
			break;
//		JavaLog::javalogf(LEVEL_FINE , "selecting for %d sec %d usecs..." , selectTime.tv_sec,selectTime.tv_usec);
		jlong packetReceiveTime , packetMicrosecReceiveTime;
		int packetSize = readPacket(selectTime , packet , &packetReceiveTime , &packetMicrosecReceiveTime);
		// If a valid packet has returned : act in the configured way :
		if (packetSize > 0)
		{
			// initialize the packet jbyte array :
//			JavaLog::javalogf(LEVEL_INFO,"Accepted packet with size :%d...",packetSize);
			jbyteArray packetByteArray = javaEnviroment->NewByteArray(packetSize);
			javaEnviroment->SetByteArrayRegion(packetByteArray , 0 , packetSize , packet);
			// if buffering was ordered :
			if (bufferPackets)
			{
				// call add function :
				javaEnviroment->CallVoidMethod(addPacketContext , addPacketMid , packetByteArray , packetSize , (jlong)packetReceiveTime , (jlong)packetMicrosecReceiveTime);
				bufferedPackets++;
				// check if the buffer size exceeded :
				if (performCallback && bufferedPackets > callbackBufferSize)
				{
					// callback :
					doneReceive = (bool)javaEnviroment->CallBooleanMethod(callbackContext , callbackEmptyMid);
					// clear all packets :
					javaEnviroment->CallVoidMethod(callbackContext , clearPacketsMid );
					bufferedPackets=0;
				}
			}
			else 
			{
				//JavaLog::javalog(LEVEL_INFO , "calling back function...");
				// callback :
				doneReceive = (bool)javaEnviroment->CallBooleanMethod(callbackContext , callbackWithPacketMid ,packetByteArray, (jlong)packetReceiveTime , (jlong)packetMicrosecReceiveTime);
				//JavaLog::javalogf(LEVEL_INFO , "done calling back function... doneReceive = %d" , doneReceive );
			}
		}
		if ( selectTime.tv_sec < 0 || (selectTime.tv_sec == 0 && selectTime.tv_usec<100000))
			doneReceive=true;
	}
	//JavaLog::javalog(LEVEL_INFO , "Done Listening...");
	// perform a last callback to clear resources :
	if (bufferPackets && performCallback)
	{
		// callback :
		doneReceive = (bool)javaEnviroment->CallBooleanMethod(callbackContext , callbackEmptyMid);
		// clear all packets :
		javaEnviroment->CallVoidMethod(callbackContext , clearPacketsMid );
		bufferedPackets=0;
	}
	
}
