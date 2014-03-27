package dimes.util.logging;



/**
 * @author Ohad Serfaty
 *
 *  A interface for classes that :
 *  	1. Have a native function linked with their code
 *  	2. Wish to perform logging calls from that native code
 *  
 *   A class with those features should implement that interface. 
 *   The usage within the native code is :
 * 
 *   Java Code :
 *   public class Example implements NativeLogger{
 *   	public native void logExample(String strLog);
 *   }
 *   
 *   public void log(String logLevelString , String logMessage){
 *   	System.out.println("["+logLevelString+"]:"+logMessage);
 *   }
 *   
 *   C++ Code :
 *   #include "JNIOutputCallback.h"
 *   
 * ... some code here ...
 * 
 *   JNIEXPORT void JNICALL Java_Example_logExample (JNIEnv * env , jobject obj , jstring logStr){
 *   	// Set the enviroment variable and object :
  			setJavalogEnviroment(env , obj);
  			char* logStrCharArray = (char*)env->GetStringUTFChars(logStr,0);
  			javalog(LEVEL_INFO , "Hello World!");	
  			javalogf(LEVEL_FINE , "Here is some printf like code. This is your log message : %s",logStrCharArray);
  			env->ReleaseStringUTFChars(logStr, logStrCharArray);  			
  }

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
