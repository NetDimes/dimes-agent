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
public class URLCrawlUserTaskSource implements UserTaskSource
{

	private final String sourceURL;
	private String commandString = "";
	private String measurementType = MeasurementType.getName(MeasurementType.TRACEROUTE);
	private String protocol = Protocol.getName(Protocol.UDP);
	private final int crawlLevel;
	private static final int DEFAULT_CRAWL_DEPTH = 2;
	private Logger logger = Loggers.getUserScriptsLogger();
	private final int ipsLimit;
	private static final int DEFAULT_IP_LIMIT = 100;

	public URLCrawlUserTaskSource(String url)
	{
		this.sourceURL = url;
		ipsLimit = DEFAULT_IP_LIMIT;
		crawlLevel = DEFAULT_CRAWL_DEPTH;
	}

	/**
	 * @param resource
	 * @param defaultOperation
	 * @param defaultProtocol
	 * @param ipsLimit
	 */
	public URLCrawlUserTaskSource(String resource, int defaultOperation, int defaultProtocol, int crawlLevel, int ipsLimit)
	{
		this.sourceURL = resource;
		this.crawlLevel = crawlLevel;
		this.ipsLimit = ipsLimit;
		protocol = Protocol.getName(defaultProtocol);
		measurementType = MeasurementType.getName(defaultOperation);
	}

	public void parse() throws UserTaskPerserException
	{
		HashSet ipList = null;
		try
		{
			ipList = LinkExtractor.extractIPsFromLinksSet(LinkExtractor.crawlAndExtractHtmlLinks(sourceURL, crawlLevel));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new UserTaskPerserException("Couldnt read URL :" + e.getMessage());
		}

		Iterator i = ipList.iterator();
		// TODO : change this so that filtering by the number of IPs will be done
		// at the LinkExtractor.
		int counter = 0;
		while (i.hasNext() && counter < ipsLimit)
		{
			counter++;
			String ip = (String) i.next();
			commandString += measurementType + " ";
			commandString += ip + " ";
			commandString += protocol + " ";
			commandString += "\n";
		}
		logger.info("Creating user script with following commands:\n");
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