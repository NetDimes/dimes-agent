package dimes.measurements.nio;



/**
 * @author Ohad Serfaty
 * <p>
 * <br>
   A interface for classes that :
   	<li>1. Have a native function linked with their code
   	<li>2. Wish to perform logging calls from that native code
    <br>
    <br>
    A class with those features should implement that interface. 
    <br>
    The usage within the native code is :
    <br><br>
    Java Code :
    <br>
    <code>
    public class Example implements NativeLogger{ <br>
    	public native void logExample(String strLog);<br> 
    }<br>
    <br>
    public void log(String logLevelString , String logMessage){<br>
    	System.out.println("["+logLevelString+"]:"+logMessage);<br>
    }<br>
    </code>
    <br>
    C++ Code :<br>
    <code>
    #include "JNIOutputCallback.h"<br>
    <br>
  ... some code here ...<br>
  <br>
    JNIEXPORT void JNICALL Java_Example_logExample (JNIEnv * env , jobject obj , jstring logStr){<br>
    	// Set the enviroment variable and object :<br>
  			setJavalogEnviroment(env , obj);<br>
  			char* logStrCharArray = (char*)env->GetStringUTFChars(logStr,0);<br>
  			javalog(LEVEL_INFO , "Hello World!");	<br>
  			javalogf(LEVEL_FINE , "Here is some printf like code. This is your log message : %s",logStrCharArray);<br>
  			env->ReleaseStringUTFChars(logStr, logStrCharArray);<br>
  	<br>
  }<br>
  </code>  			

 *  
 */
public interface NativeLogger {
	
	/**
	 * log a message from within a native code
	 * 
	 * @param logLevelString the log level as a string
	 * @param logMessage a log message
	 */
	public void log(String logLevelString , String logMessage);
	
}
