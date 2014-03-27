package dimes.util.comState;

/**
 * @author Ohad
 */
public class CommunicationDetector
{

	public native boolean gotInetrnetConnection();

	public synchronized boolean isConnected()
	{
		return gotInetrnetConnection();
	}
}

/* 
 * copmiled with :
 * 
 * 
 C:\j2sdk1.4.2_05\bin\javah -jni -d C:\Ohad\DevelopmentAgent\dimes\comState -classpath C:\Ohad\DevelopmentAgent dimes.comState.CommunicationDetector 
 */
