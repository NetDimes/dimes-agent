package dimes.util.logging;


import org.apache.log4j.Logger;


/** A stripped down version of loggers.java
 * try to use Apache log4j?
 * @author user
 *
 */
public class Loggers {

	public static Logger getLogger(Class class1) {
		Logger logger = Logger.getLogger(class1);// Logger(class1.getCanonicalName())	
		return logger;
	}

	public static Logger getLogger() {
		return Logger.getRootLogger();
	}

	public static java.util.logging.Logger getUserScriptsLogger() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
