#include <windows.h>
#include <wininet.h>
#include "dimes_util_comState_CommunicationDetector.h"

JNIEXPORT jboolean JNICALL Java_dimes_util_comState_CommunicationDetector_gotInetrnetConnection
  (JNIEnv *, jobject){
  jboolean m_bConnected = TRUE;

DWORD connection;

	if(InternetGetConnectedState(&connection,0))
	{
		m_bConnected=TRUE;//set our status to connected
	}
	else//we are not connected
	{
		m_bConnected=FALSE;//set our connected status to false
	}	
	return m_bConnected;
}