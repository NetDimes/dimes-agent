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
#include "NetworkStack.h"
#ifdef DIMES_WINDOWS
BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
	//printf("started NetworkStack dll...");
    return TRUE;
}
#endif 

static RawNetworkStack rawNetworkStack;
static PacketBuilder packetBuilder;

// Mac level Network stack is defined only for windows at the moment :
#ifdef DIMES_WINDOWS
static MacLevelNetworkStack macLevelStack;
#else
static RawNetworkStack macLevelStack;
#endif 
// Etomic Network Stack is treated as a raw network stack on other platforms :
#ifdef DIMES_ETOMIC
static EtomicNetworkStack etomicNetworkStack;
#else
static RawNetworkStack etomicNetworkStack;
#endif

// Exception constants :
const char *MEASUREMENT_EXCEPTION="dimes/measurements/nio/error/MeasurementException";
const char *MEASUREMENT_INITIALIZATION_EXCEPTION="dimes/measurements/nio/error/MeasurementInitializationException";
const char *BUILD_EXCEPTION="dimes/measurements/nio/error/PacketBuildException";

// Generate an exception :
void generateException(JNIEnv * env , const char *exceptionName , char *message){
	env->ExceptionDescribe();
	env->ExceptionClear();

	jclass newExcCls = env->FindClass(exceptionName);
	if (newExcCls == 0) /* Unable to find the new exception class, give up. */
	{
		JavaLog::javalogf(LEVEL_SEVERE , "Couldn't find class to throw Exception.");
		return;
	}
	env->ThrowNew( newExcCls, message);
}

void generateException(JNIEnv * env ,  char *message ){
	generateException(env,MEASUREMENT_EXCEPTION,message);
}

// --------------------   Init ----------------------//

void NetworkStack_init(JNIEnv * env , jobject obj , jobjectArray initArgs , NetworkStackBase *networkStack){
	  JavaLog::setJavalogEnviroment(env,obj);
	  // Check that the nwtwork stack is not initialized :
	  if (networkStack->isReady())
	  {
		  generateException(env,MEASUREMENT_INITIALIZATION_EXCEPTION,"Network Stack must close before re initialization");
		  return;
	  }
	  
	  int argc = env->GetArrayLength(initArgs);
	  char** argv = new char*[argc];
	  for (int i=0; i<argc; i++)
	  {
		  jstring arg = (jstring)env->GetObjectArrayElement(initArgs , i);
		  argv[i] = (char*)env->GetStringUTFChars(arg,0);
	  }
  	  //char* localAddress = (char*)env->GetStringUTFChars(localAddressStr,0);
	  bool success = networkStack->initialize(argv , argc);
	  //env->ReleaseStringUTFChars(localAddressStr, localAddress);
	  for (int i=0; i<argc; i++)
	  {
		  jstring arg = (jstring)env->GetObjectArrayElement(initArgs , i);
		  env->ReleaseStringUTFChars(arg,argv[i]);
	  }
	  delete argv;
	  if (!success)
		generateException(env,MEASUREMENT_INITIALIZATION_EXCEPTION,"Uninitialized network stack");
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_RawNetworkStack_init
  (JNIEnv * env , jobject obj , jobjectArray initArgs){
	  NetworkStack_init(env,obj,initArgs , &rawNetworkStack);
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_init
  (JNIEnv * env , jobject obj , jobjectArray initArgs){
	  NetworkStack_init(env,obj,initArgs , &etomicNetworkStack);
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_init
  (JNIEnv * env , jobject obj , jobjectArray initArgs){
  #ifdef DIMES_WINDOWS
	  macLevelStack.staticPacketBuilder = &packetBuilder;
  #endif
	  NetworkStack_init(env,obj,initArgs , &macLevelStack);
}

  // -------------------------------  close ------------------------------//

JNIEXPORT void JNICALL Java_dimes_measurements_nio_RawNetworkStack_close
  (JNIEnv * env , jobject obj){
	  if (!rawNetworkStack.isReady())
		  return ;
	  JavaLog::setJavalogEnviroment(env,obj);
	  rawNetworkStack.close();
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_close
  (JNIEnv * env , jobject obj){
	  if (!etomicNetworkStack.isReady())
		  return ;
	  JavaLog::setJavalogEnviroment(env,obj);
	  etomicNetworkStack.close();
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_close
(JNIEnv * env , jobject obj)
{
	if (!macLevelStack.isReady())
		return ;
	  JavaLog::setJavalogEnviroment(env,obj);
	  macLevelStack.close();
}

//-------------------------------  receive    -----------------------------//

void NetworkStack_receive(JNIEnv * env , jobject obj , jlong listenTimeout , jobject buffer , jobject callbackContext,
					    NetworkStackBase *networkStack)
{
	if (!networkStack->isReady())
	{
		generateException(env,MEASUREMENT_INITIALIZATION_EXCEPTION,"Uninitialized network stack");
		return ;
	}
	JavaLog::setJavalogEnviroment(env,obj);
	if (buffer == NULL && callbackContext == NULL)
	{
		generateException(env,"Native : No Buffer or callback context.");
		return;
	}
	
	networkStack->initializeCallback(env , buffer , callbackContext);
	networkStack->receive(listenTimeout);
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_RawNetworkStack_receive
(JNIEnv * env , jobject obj , jlong listenTimeout , jobject buffer , jobject callbackContext){
	NetworkStack_receive(env,obj,listenTimeout , buffer , callbackContext , &rawNetworkStack);
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_receive
(JNIEnv * env , jobject obj , jlong listenTimeout , jobject buffer , jobject callbackContext){
	NetworkStack_receive(env,obj,listenTimeout , buffer , callbackContext , &etomicNetworkStack);
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_receive
(JNIEnv * env , jobject obj , jlong listenTimeout , jobject buffer , jobject callbackContext){
	NetworkStack_receive(env,obj,listenTimeout , buffer , callbackContext , &macLevelStack);
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_RawNetworkStack_setCallbackBufferSize
(JNIEnv * env , jobject obj , jint bufferSize){
	rawNetworkStack.setCallbackBufferSize((int)bufferSize);
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_setCallbackBufferSize
(JNIEnv * env , jobject obj , jint bufferSize){
	etomicNetworkStack.setCallbackBufferSize((int)bufferSize);
}

//------------------------------  register packet -----------------------//

void NetworkStack_registerPacket(JNIEnv * env , jobject obj , jlong sendTime , 
								 jbyteArray packetBuffer , jint timestampPosition , jint chksumPosition , 
								 NetworkStackBase *networkStack)
{ 
	if (!networkStack->isReady())
	{
		generateException(env,MEASUREMENT_INITIALIZATION_EXCEPTION,"Uninitialized network stack");
		return ;
	}
	JavaLog::setJavalogEnviroment(env,obj);
	int arrayLength = (int)env->GetArrayLength(packetBuffer);
	jboolean isCopy;
	jbyte *packetBytesArrayJava = env->GetByteArrayElements(packetBuffer,&isCopy);
	jbyte *packetBytesArray = new jbyte[arrayLength];
	memcpy(packetBytesArray, packetBytesArrayJava, arrayLength);
	networkStack->registerPacket(sendTime , packetBytesArray,arrayLength,(int)timestampPosition,(int)chksumPosition);
	if (isCopy) {
		env->ReleaseByteArrayElements(packetBuffer, packetBytesArrayJava, JNI_ABORT);
	}
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_RawNetworkStack_registerPacket
  (JNIEnv * env , jobject obj , jlong sendTime , jbyteArray packetBuffer,
  jint timestampPosition , jint checksumPosition)
{
	NetworkStack_registerPacket(env , obj , sendTime , packetBuffer , timestampPosition , checksumPosition , &rawNetworkStack);
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_registerPacket
  (JNIEnv * env , jobject obj , jlong sendTime , jbyteArray packetBuffer,
  jint timestampPosition , jint checksumPosition)
{
	NetworkStack_registerPacket(env , obj , sendTime , packetBuffer ,  timestampPosition , checksumPosition ,&etomicNetworkStack);
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_registerPacket
  (JNIEnv * env , jobject obj , jlong sendTime , jbyteArray packetBuffer , 
  jint timestampPosition , jint checksumPosition)
{
	NetworkStack_registerPacket(env , obj , sendTime , packetBuffer , timestampPosition , checksumPosition , &macLevelStack);
}

// -------------------------------  Send  -------------------------------

jlongArray NetworkStack_send(JNIEnv * env , jobject obj ,NetworkStackBase *networkStack){
	if (!networkStack->isReady())
	{
		generateException(env,MEASUREMENT_INITIALIZATION_EXCEPTION,"Uninitialized network stack");
		return NULL;
	}
	JavaLog::setJavalogEnviroment(env,obj);
	int packetsNumber = networkStack->getRegisteredPacketsNum();
	//JavaLog::javalogf(LEVEL_INFO , "Initializing %d send times...",packetsNumber);
	jlong *tempArray = new jlong[packetsNumber];
	networkStack->sendRegisteredPackets(tempArray);
	jlongArray sendTimesArray = env->NewLongArray(packetsNumber);
	env->SetLongArrayRegion(sendTimesArray , 0 , packetsNumber , tempArray);
	delete tempArray;
	return sendTimesArray;
}

JNIEXPORT jlongArray JNICALL Java_dimes_measurements_nio_RawNetworkStack_send
(JNIEnv * env , jobject obj)
{	
	return NetworkStack_send(env,obj,&rawNetworkStack);
}

JNIEXPORT jlongArray JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_send
(JNIEnv * env , jobject obj)
{	
	return NetworkStack_send(env,obj,&etomicNetworkStack);
}

JNIEXPORT jlongArray JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_send
(JNIEnv * env , jobject obj)
{	
	return NetworkStack_send(env,obj,&macLevelStack);
}

//-----------------------  send packet  -------------------------//

jlong NetworkStack_sendPacket(JNIEnv * env , jobject obj , jbyteArray array , 
							  jint timestampPosition , jint chksumPosition , 
							  NetworkStackBase *networkStack ){
	// check for initialization :
	if (!networkStack->isReady())
	{
		generateException(env,MEASUREMENT_INITIALIZATION_EXCEPTION,"Uninitialized network stack");
		return -1;
	}
	JavaLog::setJavalogEnviroment(env,obj);
	int arrayLength = env->GetArrayLength(array);
	jbyte *packetBytesArray = env->GetByteArrayElements(array,NULL);
	jlong sendTime = networkStack->sendPacket(packetBytesArray,arrayLength,(int)timestampPosition , (int)chksumPosition);
	return sendTime;
}


JNIEXPORT jlong JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_sendPacket
(JNIEnv * env , jobject obj , jbyteArray array,jint timestampPosition , jint checksumPosition)
{
	return NetworkStack_sendPacket(env,obj,array,timestampPosition , checksumPosition , &macLevelStack);
}

JNIEXPORT jlong JNICALL Java_dimes_measurements_nio_RawNetworkStack_sendPacket
(JNIEnv * env , jobject obj , jbyteArray array,jint timestampPosition , jint checksumPosition)
{
	return NetworkStack_sendPacket(env,obj,array,timestampPosition , checksumPosition,&rawNetworkStack);
}

JNIEXPORT jlong JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_sendPacket
(JNIEnv * env , jobject obj , jbyteArray array , jint timestampPosition , jint checksumPosition)
{
	return NetworkStack_sendPacket(env,obj,array,timestampPosition , checksumPosition,&etomicNetworkStack);
}

// ----------------------   Send Timed Packet  -----------------------------//

JNIEXPORT jlong JNICALL Java_dimes_measurements_nio_RawNetworkStack_sendTimedPacket
(JNIEnv * env , jobject obj , jbyteArray packet, jlong timeToSleep){
	Sleep((int)timeToSleep);
	return NetworkStack_sendPacket(env,obj,packet,-1,-1,&rawNetworkStack);
}

JNIEXPORT jlong JNICALL Java_dimes_measurements_nio_EtomicNetworkStack_sendTimedPacket
(JNIEnv * env , jobject obj , jbyteArray packet, jlong timeToSleep){
	Sleep((int)timeToSleep);
	return NetworkStack_sendPacket(env,obj,packet,-1,-1,&etomicNetworkStack);
}

JNIEXPORT jlong JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_sendTimedPacket
(JNIEnv * env , jobject obj , jbyteArray packet, jlong timeToSleep){
	Sleep(timeToSleep);
	return NetworkStack_sendPacket(env,obj,packet,-1,-1,&macLevelStack);
}

//   -------------------  utility functions  --------------------//

JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_setPacketFilter
(JNIEnv *env , jobject obj , jobjectArray filterIntArray){
	#ifdef DIMES_WINDOWS
	if (!macLevelStack.isReady())
	{
		generateException(env,"Network Stack must be initialized before setting Packet filter");
		return;
	}
	
	int arraySize = env->GetArrayLength(filterIntArray);
	if (arraySize == 0)
	{
		generateException(env,"Empty Filter accepted.");
		return;
	}

	struct bpf_program filterProgram;
	filterProgram.bf_len=arraySize;
	filterProgram.bf_insns = new struct bpf_insn [arraySize];

	
	for (int i=0; i<arraySize; i++)
	{
		jobject anObject = env->GetObjectArrayElement(filterIntArray , i);
		jintArray internalArray = (jintArray)anObject;
		int internalArraySize = env->GetArrayLength(internalArray);	
		if (internalArraySize!=4)
		{
			generateException(env,"Malformed Filter instruction. Each instruction must have 4 integers.");
			return;
		}
		jint* instructions = env->GetIntArrayElements(internalArray , NULL);
		filterProgram.bf_insns[i].code =  (USHORT)instructions[0];
		filterProgram.bf_insns[i].jt = (UCHAR)instructions[1];
		filterProgram.bf_insns[i].jf = (UCHAR)instructions[2];
		filterProgram.bf_insns[i].k = (int)instructions[3];
	}
	
	macLevelStack.serPacketFilter(&filterProgram);
	
	delete filterProgram.bf_insns;
	#endif
}


JNIEXPORT void JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_openListener
(JNIEnv * env , jobject obj )
{
	#ifdef DIMES_WINDOWS
	JavaLog::setJavalogEnviroment(env,obj);
	macLevelStack.openListener();
	#endif
}

JNIEXPORT jboolean JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_isPPPoE
(JNIEnv *, jobject)
{
	#ifdef DIMES_WINDOWS
	return (jboolean)macLevelStack.configuredPPPoE();
	#else
	return (jboolean)0;
	#endif
}

JNIEXPORT jbyteArray JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_getSourceMac
(JNIEnv *env, jobject)
{
	#ifdef DIMES_WINDOWS
	printf("Getting source mac... : ");printFormat( macLevelStack.srcMAC.data , 6) ; printf("\n");
	fflush(stdout);
	jbyteArray result = env->NewByteArray(6);
	env->SetByteArrayRegion(result , 0 , 6 , (const jbyte*) macLevelStack.srcMAC.data);
	return result;
	#else
	//return env->NewByteArray(0);
	#endif
}

JNIEXPORT jbyteArray JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_getDestMac
(JNIEnv *env, jobject)
{
	#ifdef DIMES_WINDOWS
	printf("Getting dest mac... : ");printFormat( macLevelStack.destMAC.data , 6) ; printf("\n");
	fflush(stdout);
	jbyteArray result = env->NewByteArray(6);
	env->SetByteArrayRegion(result , 0 , 6 , (const jbyte*) macLevelStack.destMAC.data);
	return result;
	#else
	//return env->NewByteArray(0);
	#endif
}

JNIEXPORT jbyteArray JNICALL Java_dimes_measurements_nio_MacLevelNetworkStack_getPPPoEHeader
(JNIEnv *env, jobject)
{
	#ifdef DIMES_WINDOWS
	return env->NewByteArray(0);
	#else
	//return env->NewByteArray(0);
	#endif
}


// -----------------------   packet and header building  ---------------------------------//


JNIEXPORT jbyteArray JNICALL Java_dimes_measurements_nio_packet_builder_RawIPHeaderBuilder_buildHeader
(JNIEnv * env , jobject obj , jstring sourceIP, jstring destIP,
	 jbyte ttl, jbyte tos , jbyte protocol , jshort ipID , jint dataSize){
	//setJavalogEnviroment(env,obj);

	char* sourceAddress = (char*)env->GetStringUTFChars(sourceIP,0);
	char* destAddress = (char*)env->GetStringUTFChars(destIP,0);
	jbyte header[sizeof(struct ip_hdr)];
	packetBuilder.buildRawIpHeader(header , sourceAddress,destAddress , ttl , tos,ipID , protocol ,dataSize); 
	
	env->ReleaseStringUTFChars(sourceIP , sourceAddress);
	env->ReleaseStringUTFChars(destIP , destAddress);	
	
	jbyteArray result = env->NewByteArray(sizeof(struct ip_hdr));
	env->SetByteArrayRegion(result , 0 , sizeof(struct ip_hdr) , header);

	return result;
}


JNIEXPORT jbyteArray JNICALL Java_dimes_measurements_nio_packet_builder_MacLevelIPHeaderBuilder_buildHeader
(JNIEnv * env , jobject obj , jstring sourceIP, jstring destIP,
	 jbyte ttl, jbyte tos , jbyte protocol , jshort ipID , jint dataSize){

	char* sourceAddress = (char*)env->GetStringUTFChars(sourceIP,0);
	char* destAddress = (char*)env->GetStringUTFChars(destIP,0);
	int ethHeaderSize = packetBuilder.getEthernetHeaderSize();
	int packetSize =  sizeof(struct ip_hdr) + ethHeaderSize;
	jbyte *header = new jbyte[packetSize];

	packetBuilder.buildEthernetHeader((char*)header , sizeof(struct ip_hdr) + dataSize);
	packetBuilder.buildRawIpHeader(header+ethHeaderSize , sourceAddress,destAddress , ttl , tos,ipID , protocol ,dataSize); 
	
	env->ReleaseStringUTFChars(sourceIP , sourceAddress);
	env->ReleaseStringUTFChars(destIP , destAddress);	
	
	jbyteArray result = env->NewByteArray(packetSize);
	env->SetByteArrayRegion(result , 0 , packetSize , header);
	delete header;
	return result;
}

 JNIEXPORT jbyteArray JNICALL Java_dimes_measurements_nio_packet_builder_IcmpPacketBuilder_buildIcmpHeader
(JNIEnv * env , jobject obj ,jbyte icmpType , jshort id, jshort sequenceNumber){

	jbyte header[sizeof(struct ICMPHeader)];
	packetBuilder.buildRawIcmpHeader(header , icmpType,id,sequenceNumber);

	jbyteArray result = env->NewByteArray(sizeof(struct ICMPHeader));
	env->SetByteArrayRegion(result , 0 , sizeof(struct ICMPHeader) , header);
	
	return result;
}

void NetworkStack_fillChecksum(JNIEnv * env , jobject obj ,jbyteArray array){
	int arrayLength = env->GetArrayLength(array);
	jbyte *newArray = env->GetByteArrayElements(array,NULL);
	packetBuilder.fillChecksum(newArray,arrayLength);
	env->SetByteArrayRegion(array , 0,arrayLength,newArray);
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_packet_builder_RawIPHeaderBuilder_fillIPChecksum
(JNIEnv * env , jobject obj ,jbyteArray array)
{
	NetworkStack_fillChecksum(env,obj,array);
}

JNIEXPORT void JNICALL Java_dimes_measurements_nio_packet_builder_MacLevelIPHeaderBuilder_fillIPChecksum
(JNIEnv * env , jobject obj ,jbyteArray array)
{
	NetworkStack_fillChecksum(env,obj,array);
}
