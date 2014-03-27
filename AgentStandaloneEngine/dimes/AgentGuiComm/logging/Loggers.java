package dimes.AgentGuiComm.logging;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import dimes.util.FileHandlerBean;
import dimes.util.logging.MyXMLFormatter;
import dimes.util.logging.NakedFormatter;
import dimes.util.logging.ResultsMemoryHandler;
import dimes.util.logging.RotatingFileHandler;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;
import dimes.AgentGuiComm.logging.*;


/**Replaces dimes.util.logging.Loggers
 * 
 * @author Anat, BoazH(0.6)
 *
 */
public class Loggers {
	private static MyXMLFormatter xmlFormatter = new MyXMLFormatter(); // Formats log messages
	private static NakedFormatter nakedFormatter = new NakedFormatter(); //Format results messges
//	private static Logger asInfoLogger = null;
//	private static RotatingFileHandler logHandler = null;
	private static RemoteLogHandler logHandler = null;
	
	//TODO:private static ResultsMemoryHandler resultHandler = null;
//	private static FileHandlerBean fileHandler = null;
	private static ResultSenderHandler resultHandler= null;
	private static Logger userScriptsLogger = null;
	private static Logger measurementsConsoleLogger = null;
	private static Logger resultWriter=null;
	
	/* configure loggers once
	 * 2 loggers:
	 * agentLogger - normal logger, writes to an XML file - agentLog.xml
	 * resultWriter - writes all results to XML file - results.xml
	 */
	static
	{
		/* default properties file */
		try
		{
//			fileHandler = new FileHandlerBean();
	//TODO:		Loggers.initResultWriter();
			Loggers.initLogger();
//			Loggers.initASInfoLogger();
			Loggers.initUserScriptsLogger();
			Loggers.initMeasurementsConsoleLogger();
			
			boolean debug = Boolean.valueOf(PropertiesBean.getProperty(PropertiesNames.DEBUG_STATE)).booleanValue();
			Loggers.debugMode(debug);
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (PropertiesBean.NoSuchPropertyException e)
		{
			e.printStackTrace();
		}
	}

	
	private static void initLogger() throws IOException, NoSuchPropertyException
	{
//		String logDirName = PropertiesBean.getProperty(PropertiesNames.LOG_DIR/*"log_dir"*/);

		// String suffix = Loggers.fileHandler.getOutgoingSuffix(logDirName);
		//	  Loggers.logHandler = new RotatingFileHandler(logDirName, suffix);
//		String suffix = "log";
		Loggers.logHandler = new RemoteLogHandler();
		Loggers.logHandler.setFormatter(Loggers.xmlFormatter);//bugfix for xml header in log files
		String loggerName = PropertiesBean.getProperty(PropertiesNames.LOGGER_NAME/*"names.logger"*/);
		Logger agentLogger = Logger.getLogger(loggerName);
		agentLogger.addHandler(logHandler);

		boolean debug = Boolean.valueOf(PropertiesBean.getProperty(PropertiesNames.DEBUG_STATE)).booleanValue();
		Loggers.debugMode(debug);
	}
	
	private static synchronized void initResultWriter() throws IOException, NoSuchPropertyException
	{
//		String resultsDirName = PropertiesBean.getProperty(PropertiesNames.RESULTS_DIR/*"results_dir"*/);
//		String suffix = Loggers.fileHandler.getOutgoingSuffix(resultsDirName);

		Loggers.resultHandler =  ResultSenderHandler.getInstance();//new ResultsMemoryHandler();
		Loggers.resultHandler.setFormatter(Loggers.nakedFormatter);
		String resultWriterName = PropertiesBean.getProperty(PropertiesNames.RESULT_WRITER_NAME/*"names.resultWriter"*/);
//		Logger resultWriter = Logger.getLogger(resultWriterName);
		Loggers.resultWriter = Logger.getLogger(resultWriterName);
		resultWriter.setUseParentHandlers(false);//check
		resultWriter.addHandler(resultHandler);
		resultWriter.addHandler(dimes.util.logging.ResultsMemoryHandler.getInstance());
		resultWriter.setLevel(Level.FINEST);
	}
	
	public static synchronized Logger getLogger()
	{
		String loggerName;
		try
		{
			loggerName = PropertiesBean.getProperty(PropertiesNames.LOGGER_NAME/*"names.logger"*/);
			if (Loggers.logHandler == null)
			{
				System.out.println("logger wasn't initialized");//debug
				Loggers.initLogger();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Loggers.getSTDoutLogger();
		}
		Logger result = Logger.getLogger(loggerName);
		
		return result;
	}
	

	@SuppressWarnings("unchecked")
	public static Logger getLogger(Class src)
	{
		try
		{
			if (Loggers.logHandler == null)
			{
				System.out.println("logger wasn't initialized when " + src + " asked for logger");//debug
				Loggers.initLogger();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Loggers.getSTDoutLogger();
		}
		//		return Logger.getLogger(src.getName());//check!
		return Loggers.getLogger();//check!!!
	}
	
	public static void closeLog()
	{

		Loggers.logHandler.close();
		String loggerName;
		try
		{
			loggerName = PropertiesBean.getProperty(PropertiesNames.LOGGER_NAME/*"names.logger"*/);
		}
		catch (NoSuchPropertyException e)
		{
			e.printStackTrace();
			loggerName = "dimes";
		}
		Logger.getLogger(loggerName).removeHandler(Loggers.logHandler);//check- should use class name - Loggers.class?
		Loggers.logHandler = null;//check
	}
	
	public static Logger getSTDoutLogger()
	{
		Logger out = Logger.getAnonymousLogger();
		out.addHandler(new ConsoleHandler());
		return out;
	}

//	public static Logger getASInfoLogger()
//	{
//		if (asInfoLogger == null)
//			initASInfoLogger();
//		return asInfoLogger;
//	}

	public static Logger getUserScriptsLogger()
	{
		if (userScriptsLogger == null)
			initUserScriptsLogger();
		return userScriptsLogger;
	}
	

	public static Logger getMeasurementConsoleLogger() {
		if (measurementsConsoleLogger == null)
			initMeasurementsConsoleLogger();
		return measurementsConsoleLogger;
	}
	
	
	/**
	 * 
	 */
//	private static void initASInfoLogger()
//	{
//		asInfoLogger = Logger.getLogger("dimes.ASInfo.logger");
//		asInfoLogger.setLevel(Level.ALL);
//	}

	public static void debugMode(boolean debug)
	{
		if (debug)
			Loggers.getLogger().setLevel(Level.ALL);
		else
			Loggers.getLogger().setLevel(Level.FINE);
	}
	
	/**
	 * 
	 */
	private static void initUserScriptsLogger()
	{
		userScriptsLogger = Logger.getLogger("userScripts.logger");
		userScriptsLogger.setLevel(Level.ALL);
	}
	
	private static void initMeasurementsConsoleLogger() {
		measurementsConsoleLogger = Logger.getLogger("measurements.console.logger");
		measurementsConsoleLogger.setLevel(Level.ALL);
	}
	
	public static Handler getResultsMemoryHandler(){
		if (null==resultHandler)
			try {
				initResultWriter();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchPropertyException e) {
				e.printStackTrace();
			}
		return resultHandler;
	}
	
	public static void resetLoggers()
	{
		System.out.println("Reseting Loggers...");

		try
		{
//			asInfoLogger = null;
			closeLog();
			closeResults();
			userScriptsLogger = null;
			measurementsConsoleLogger=null;
			initResultWriter();
			initLogger();
//			Loggers.initASInfoLogger();
			Loggers.initUserScriptsLogger();
			Loggers.initMeasurementsConsoleLogger();

			boolean debug = Boolean.valueOf(PropertiesBean.getProperty(PropertiesNames.DEBUG_STATE)).booleanValue();
			Loggers.debugMode(debug);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchPropertyException e)
		{
			e.printStackTrace();
		}

		//logHandler=null;

		//resultHandler=null;
		//fileHandler = null;

	}
	
	public static synchronized Logger getResultWriter()
	{
		//resultWriter has basically turned into a singalton, so this is the 
		//getInstance() method. BoazH 0.5.5
		
/*		String resultWriterName;
		try
		{
			resultWriterName = PropertiesBean.getProperty(PropertiesNames.RESULT_WRITER_NAME"names.resultWriter");
			if (Loggers.resultHandler == null)
				Loggers.initResultWriter();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Loggers.getSTDoutLogger();
		}
		Logger result = Logger.getLogger(resultWriterName);
		return (Logger) (result!=null?result:ResultsMemoryHandler.getInstance());*/
		if (null==Loggers.resultWriter)
			try {
				initResultWriter();
			} catch (IOException e) {
			
				e.printStackTrace();
			} catch (NoSuchPropertyException e) {
			
				e.printStackTrace();
			}
		return Loggers.resultWriter;
	}
	
	public static void closeResults() throws IOException, NoSuchPropertyException
	{
		//		Loggers.getResultWriter().finest("\n</Results>");//check
		Loggers.resultHandler.close();
		getResultWriter().removeHandler(Loggers.resultHandler);//check- should use class name - Loggers.class?
		Loggers.resultHandler = null;
		//		fileHandler.turnToActive(Loggers.resultFile);
	}
}
