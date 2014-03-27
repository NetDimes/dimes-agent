#ifndef _Included_dimes_measurements_
#define _Included_dimes_measurements_
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
#include <stdio.h>
#include <stdlib.h>
#include "JNIOutputCallback.h"
#include "RawNetworkStack.h"
#include "MacLevelNetworkStack.h"
#include "EtomicNetworkStack.h"
#include "PacketBuilder.h"
#ifdef DIMES_WINDOWS
#include <windows.h>
#endif
#ifdef __cplusplus
extern "C" {
#endif

// -----------------------------------------------------------------------//
//
//	This files contains the exported functions to the dll/shared object
// 
// -----------------------------------------------------------------------//

//-----------------   Raw Network Stack  ---------------------//


JNIEXPORT void JNICALL Java_dimes_measurements_nio_RawNetworkStack_init
  (JNIEnv *, jobject , jobjectArray);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_RawNetworkStack_close
  (JNIEnv * env , jobject obj);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_RawNetworkStack_receive
  (JNIEnv * env , jobject obj , jlong listenTimeout , jobject buffer , jobject callbackContext);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_RawNetworkStack_registerPacket
  (JNIEnv * env , jobject obj , jlong sendTime , jbyteArray packetBuffer , jint ,  jint);

JNIEXPORT jlongArray JNICALL Java_dimes_measurements_nio_RawNetworkStack_send
  (JNIEnv * env , jobject obj );

JNIEXPORT jlong JNICALL Java_dimes_measurements_nio_RawNetworkStack_sendPacket
  (JNIEnv * env , jobject obj , jbyteArray , jint timestampPosition , jint chksumPosition);

JNIEXPORT jlong JNICALL Java_dimes_measurements_nio_RawNetworkStack_sendTimedPacket
  (JNIEnv * env , jobject obj , jbyteArray , jlong timeToSleep);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_RawNetworkStack_setCallbackBufferSize
  (JNIEnv * env , jobject obj , jint bufferSize);

// --------------- Mac Level Network stack  -----------------//

JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_init
  (JNIEnv * env , jobject obj , jobjectArray);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_close
  (JNIEnv * env , jobject obj);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_receive
  (JNIEnv * env , jobject obj , jlong listenTimeout , jobject buffer , jobject callbackContext);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_registerPacket
  (JNIEnv * env , jobject obj , jlong sendTime , jbyteArray packetBuffer , jint timeStampPosition,  jint checkSumPosition);

JNIEXPORT jlongArray JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_send
  (JNIEnv * env , jobject obj );

JNIEXPORT jlong JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_sendPacket
  (JNIEnv * env , jobject obj , jbyteArray , jint , jint);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_setCallbackBufferSize
  (JNIEnv * env , jobject obj , jint bufferSize);

JNIEXPORT jlong JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_sendTimedPacket
  (JNIEnv * env , jobject obj , jbyteArray , jlong timeToSleep);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_setPacketFilter
  (JNIEnv *, jobject, jobjectArray);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_openListener
  (JNIEnv *, jobject);

JNIEXPORT jboolean JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_isPPPoE
  (JNIEnv *, jobject);

JNIEXPORT jbyteArray JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_getSourceMac
  (JNIEnv *, jobject);

JNIEXPORT jbyteArray JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_getDestMac
  (JNIEnv *, jobject);

JNIEXPORT jbyteArray JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_getPPPoEHeader
  (JNIEnv *, jobject);

//----------------- Packet Builder  -------------------------//
JNIEXPORT jbyteArray JNICALL Java_dimes_measurements_nio_packet_builder_RawIPHeaderBuilder_buildHeader
(JNIEnv * env , jobject obj , jstring sourceIP, jstring destIP,
	 jbyte ttl, jbyte tos , jbyte protocol , jshort ipID , jint dataSize);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_packet_builder_RawIPHeaderBuilder_fillIPChecksum
(JNIEnv * env , jobject obj ,jbyteArray);

JNIEXPORT jbyteArray JNICALL Java_dimes_measurements_nio_packet_builder_MacLevelIPHeaderBuilder_buildHeader
(JNIEnv * env , jobject obj , jstring sourceIP, jstring destIP,
	 jbyte ttl, jbyte tos , jbyte protocol , jshort ipID , jint dataSize);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_packet_builder_MacLevelIPHeaderBuilder_fillIPChecksum
(JNIEnv * env , jobject obj ,jbyteArray);

 JNIEXPORT jbyteArray JNICALL Java_dimes_measurements_nio_packet_builder_IcmpPacketBuilder_buildIcmpHeader
(JNIEnv * env , jobject obj ,jbyte icmpType , jshort id, jshort sequenceNumber);

//-----------------   Etomic Network Stack  ---------------------//

JNIEXPORT void JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_init
  (JNIEnv *, jobject , jobjectArray);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_close
  (JNIEnv * env , jobject obj);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_receive
  (JNIEnv * env , jobject obj , jlong listenTimeout , jobject buffer , jobject callbackContext);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_registerPacket
  (JNIEnv * env , jobject obj , jlong sendTime , jbyteArray packetBuffer  , jint ,  jint);

JNIEXPORT jlongArray JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_send
  (JNIEnv * env , jobject obj );

JNIEXPORT jlong JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_sendPacket
  (JNIEnv * env , jobject obj , jbyteArray, jint , jint );

JNIEXPORT jlong JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_sendTimedPacket
  (JNIEnv * env , jobject obj , jbyteArray , jlong timeToSleep);

JNIEXPORT void JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_setCallbackBufferSize
  (JNIEnv * env , jobject obj , jint bufferSize);



#ifdef __cplusplus
}
#endif
#endif
