/*
 * Created on 25/08/2005
 *
 */
package dimes.scheduler.usertask;

import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import dimes.measurements.MeasurementType;
import dimes.measurements.Protocol;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;

/**
 * @author Ohad Serfaty
 *
 */
public class URLUserTaskSource implements UserTaskSource
{

	private final String sourceURL;
	private String commandString = "";
	private String measurementType = MeasurementType.getName(MeasurementType.TRACEROUTE);
	private String protocol = Protocol.getName(Protocol.UDP);
	private Logger logger = Loggers.getUserScriptsLogger();

	public URLUserTaskSource(String url)
	{
		this.sourceURL = url;

	}

	/**
	 * @param resource
	 * @param defaultOperation
	 * @param defaultProtocol
	 */
	public URLUserTaskSource(String url, int defaultOperation, int defaultProtocol)
	{
		this.sourceURL = url;
		protocol = Protocol.getName(defaultProtocol);
		measurementType = MeasurementType.getName(defaultOperation);
	}

	public void parse() throws UserTaskPerserException
	{
		HashSet ipList = null;
		try
		{
			ipList = LinkExtractor.extractHtmlIPs(sourceURL);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new UserTaskPerserException("Couldnt read URL :" + e.getMessage());
		}

		Iterator i = ipList.iterator();
		while (i.hasNext())
		{
			String ip = (String) i.next();
			commandString += measurementType + " ";
			commandString += ip + " ";
			commandString += protocol + " ";
			commandString += "\n";
		}
		logger.info("Creating user script with following commands:");
		logger.info(commandString);
	}

	public String getCommandsString()
	{
		return commandString;
	}

	public String getScriptID()
	{
		return sourceURL;
	}

}