package dimes.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.log4j.Logger;

//import dimes.measurements.operation.TracerouteOp;
//import dimes.measurements.results.PingTracerouteResults;
import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**A collection of IP address-related methods that was previously located in 
 *dimes.Measurements. 
 * 
 * 
 * @author BoazH
 *
 */
public class IPUtils {
	
	
	
	private static Logger logger = Loggers.getLogger(IPUtils.class);

	/**returns IP address of local machine, except in the case where it's both a private address
	 * and behind a proxy. should be called only after registration - so proxy setting are already known.
	 * 
	 * @return String IP
	 * @throws UnknownHostException
	 */
	static public String getHostIP() throws UnknownHostException
	{
		InetAddress localhost = InetAddress.getLocalHost();
		String IP = localhost.getHostAddress();
		logger.debug("IP: "+IP);
		logger.debug("localhost: "+localhost);
		logger.debug("isPrivateIP(IP): "+isPrivateIP(IP) +" isBehindProxy(): "+isBehindProxy());
//		if (isPrivateIP(IP) && isBehindProxy())//private but no proxy can be resolved by server
//		{
//			String nextPublicIP = getNextPublicIP();
//			logger.debug("nextPublicIP"+nextPublicIP);
//
//		}
		return IP;
	}

	/** Determins if Ip is private (begins with 10. or 192.168, or equal 127.0.0.1) 
	 * @param anIP
	 * @return
	 */
	static public boolean isPrivateIP(String anIP) //Is also used by DisplayServer, so leave it public
	{
		if (anIP.startsWith("10.") || anIP.startsWith("192.168") || anIP.equals("127.0.0.1"))
			return true;
		return false;
	}

	static public boolean isValidAddress(String addr)
	{
		StringTokenizer t = new StringTokenizer(addr,"."); //following two lines match scheme for IP4
		if (t.countTokens()<3) return false;   
		boolean isValidIP = true;
		if (addr == null)
			isValidIP = false;
		else
			try
			{
				InetAddress.getAllByName(addr);
			}
			catch (UnknownHostException e1)
			{
				isValidIP = false;
			}
		return isValidIP;
	}

	static private boolean isBehindProxy()
	{
		boolean useProxy;
		try
		{
			useProxy = Boolean.valueOf(PropertiesBean.getProperty(PropertiesNames.USE_PROXY/*"comm.useProxy"*/)).booleanValue();
		}
		catch (Exception e)
		{
			logger.warn(e.toString());
			useProxy = false;
		}
		return useProxy;
	}

//	/**
//	 * @return
//	 */
//	static public String getNextPublicIP()
//	{
//		String destName;
//		
//		try
//		{
//			destName = PropertiesBean.getProperty(PropertiesNames.BASIC_TRACE_DEST + "1"/*"urls.basicTraceDest1"*/);
//		}
//		catch (NoSuchPropertyException e)
//		{
//			logger.warning(e.toString());
//			destName = "www.netdimes.org";//just as default
//		}
//		
//		//PingTracerouteResults result = Measurements.getMeasurementsMTR().executeMTR(destName, MeasurementType.TRACEROUTE, Protocol.getDefault(), Measurements.getMTR_PING_NUM(), 33435);
//		PingTracerouteResults result = Measurements.executeMeasurement(destName, MeasurementType.TRACEROUTE, Protocol.getDefault(), Measurements.getMTR_PING_NUM(), (short)33331, (short)33435);
//		String srcIP = result.getSourceIp();
//		
//		if (!isPrivateIP(srcIP))
//			return srcIP;
//		
//		String currIP = srcIP; //initialize
//		Vector<NetHost> details = result.getRawVector();
//		
//		for (int i = 0; isPrivateIP(currIP) && i < details.size(); ++i)
//		{
//			NetHost aDetail = details.get(i);
//			if (aDetail.getHopAddress() != 0) //valid address - otherwise, it's an unknown host
//				currIP = aDetail.getHopAddressStr();
//		}
//		
//		if (isPrivateIP(currIP)) //if only returned private IPs, return first on (source)
//			return srcIP;
//		
//		return currIP;
//	}

//	static public boolean isProtocolBlocked(int proto)
//	{
//		/*System.err.println*/logger.finest("-------- checking protocol: " + Protocol.getName(proto) + " --------");//debug
//		for (int i = 1; i < 3; ++i)//3 basicTraceDests exist in the propFile - checking 2
//		{
//			String destName;
//			try
//			{
//				destName = PropertiesBean.getProperty(PropertiesNames.BASIC_TRACE_DEST/*"urls.basicTraceDest"*/+ i);
//			}
//			catch (NoSuchPropertyException e)
//			{
//				logger.warning(e.toString());
//				destName = "www.netdimes.org";//just as default
//			}
//
//			/*System.err.println*/logger.finest("measuring to " + destName);//debug
//			//PingTracerouteResults result = Measurements.getMeasurementsMTR().executeMTR(destName, MeasurementType.TRACEROUTE, proto, Measurements.getMTR_PING_NUM(), TracerouteOp.initialPort);
//			PingTracerouteResults result = Measurements.executeMeasurement(destName, MeasurementType.TRACEROUTE, proto, Measurements.getMTR_PING_NUM(), TracerouteOp.sourcePort, TracerouteOp.destPort);
//			Vector<NetHost> hops = result.getRawVector();
//			/*System.err.println*/logger.finest("no. of hops is " + hops.size());//debug
//			if (result.isReachedDest() && (hops.size() > 2))
//			{
//				/*System.err.println*/logger.finest("protocol " + Protocol.getName(proto) + " is NOT blocked");
//				return false;
//			}
//			if (hops.size() == 0)
//				continue;
//
//			String currIP = "";
//			int publicNum = 0;
//			for (int j = 0; j < hops.size(); ++j)
//			{
//				NetHost aHop = (NetHost) hops.get(j);
//				if (aHop.getHopAddress() != 0) //valid address - otherwise, it's an unknown host
//				{
//					currIP = aHop.getHopAddressStr();
//					if (!isPrivateIP(currIP))
//					{
//						++publicNum;
//						if (publicNum > 2)
//						{
//							/*System.err.println*/logger.finest("protocol " + Protocol.getName(proto) + " is NOT blocked");
//							return false;
//						}
//					}
//					else
//						/*System.err.println*/logger.finest("private: " + currIP);//debug
//				}
//				else
//					/*System.err.println*/logger.finest("invalid ip");//debug
//			}
//			/*System.err.println*/logger.finest("public num: " + publicNum);//debug
//
//		}
//
//		/*System.err.println*/logger.finest("protocol " + Protocol.getName(proto) + " is blocked");
//		return true;
//	}
	/**************************************************************************************/

}
