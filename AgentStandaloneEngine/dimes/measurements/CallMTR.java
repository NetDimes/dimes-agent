package dimes.measurements;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Vector;

import dimes.measurements.results.PingTracerouteResults;
import dimes.measurements.results.Results;
import dimes.platform.PlatformDependencies;
import dimes.util.CommUtils;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;
import dimes.util.time.TimeUtils;

/**
 * @author galith
 * 
 */
public class CallMTR
{
	
	static
	{
		try
		{
			// currently required only in windows :
			if (PlatformDependencies.os == PlatformDependencies.WINDOWS)
			{
				String resourcesDir = PropertiesBean.getProperty(PropertiesNames.RESOURCES_DIR);
				//System.out.println("Loading shared libraries from :" + resourcesDir);
				boolean sharedLibrariesLoaded = loadLibraries(resourcesDir);
				if (!sharedLibrariesLoaded)
					Loggers.getLogger().warning("Could not load Shared cpp libraries from shared.dll at " + resourcesDir);
			}
		}
		catch (NoSuchPropertyException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public native boolean executeNativeMTR(NetHost[] test, String host, int measurementType, int protocol, int num, int initialPort, MyBoolean reachedDest);
	
	public native boolean PacketTrain(String[] IParray, int Protocol, int Robin, int port, int Packetsize, int dt, int AgentId);// steger -- packettrain added
	
	public native boolean setDefaultNetworkInterface(String name, String string);//A function for setting the default network interface with which to perform measurements.
	
	public native static boolean loadLibraries(String resourcesDirName);

	/**Executes MTR PacketTrain(QBE) measurement.
	 * 
	 * @param IPlist
	 * @param proto
	 * @param robin
	 * @param port
	 * @param packetsize
	 * @param delay_usec
	 * @param AgentNo
	 * @return
	 */
	public Results executeMTR(String[] IPlist, int proto, int robin, int port, int packetsize, int delay_usec, int AgentNo)
	{
		PacketTrain(IPlist, proto, robin, port, packetsize, delay_usec, AgentNo);
		return null;
	}

	//todo- get as parameter - done. 
	//private static final int MAX_HOSTS = 50;

	/**Executes MTR Ping or Traceroute measurement with default value of Max hosts = 50
	 * 
	 * @param host
	 * @param measurementType
	 * @param protocol
	 * @param num
	 * @param initialPort
	 * @return
	 */
	public PingTracerouteResults executeMTR(String host, int measurementType, int protocol, int num, int initialPort)
	{
		return  executeMTR(host, measurementType, protocol, num, initialPort, 50);
	}
	
	/**Executes MTR, Ping or Traceroute measurement takes max hosts as a paramater. 
	 * 
	 * @param host
	 * @param measurementType
	 * @param protocol
	 * @param num
	 * @param initialPort
	 * @param maxHosts
	 * @return
	 */ 
	public PingTracerouteResults executeMTR(String host, int measurementType, int protocol, int num, int initialPort, int maxHosts)
	{
		boolean severExcept = false;
		boolean success = false;
		String source = null;
		String resultExcept = "";//global for the tracerout
		String traceResultExcept = "";//local for everyhop
		String source_ip = null;
		String destIp = "";//ip address of destination
		String comm = MeasurementType.getName(measurementType);
		NetHost[] allNetHosts;
		
		if (measurementType == MeasurementType.PING)
			allNetHosts = new NetHost[1];
		else
			allNetHosts = new NetHost[maxHosts];

		for (int i = 0; i < allNetHosts.length; ++i)
			allNetHosts[i] = new NetHost();				
		Timestamp ts = TimeUtils.getTimeStamp();
		String localTime = TimeUtils.getLocalTime();
		
		try
		{
			InetAddress localhost = java.net.InetAddress.getLocalHost();
			source = localhost.getHostName();
			source_ip = localhost.getHostAddress();
		}
		catch (UnknownHostException e)
		{
			resultExcept = "Name of local host cannot be resolved\n";
			if (source == null)
				source = "localhost";
			if (source_ip == null)
				source_ip = "127.0.0.1";
		}
		
		InetAddress DestAddress = null;
		
		try
		{
			DestAddress = InetAddress.getByName(host);
			destIp = DestAddress.getHostAddress();
		}
		catch (UnknownHostException e)
		{
			resultExcept = resultExcept + "Name of destination host cannot be resolved\n";
			severExcept = true;
		}
		
		if (severExcept)
		{
			PingTracerouteResults resultsMTR = new PingTracerouteResults(source, source_ip, host, "destination unknown", -1, -1, comm, Protocol.getName(protocol), ts, localTime, num);
			resultsMTR.setException(resultExcept);
			resultsMTR.setSF(false);
			Vector<NetHost> details = new Vector<NetHost>();
			NetHost aNetHost = new NetHost();
			details.add(aNetHost);
			resultsMTR.setRawVector(details);
			return resultsMTR;
		}

		// set the default interface name before the measurement :
		String defaultNetworkInterfaceName;
		MeasureableNetworkInterface networkInterface;
		
		try
		{
			defaultNetworkInterfaceName = PropertiesBean.getProperty(PropertiesNames.NETWORK_INTERFACE);
		}
		catch (NoSuchPropertyException e1)
		{
			// property doesn't xist : take the default :
			defaultNetworkInterfaceName = "default";
		}
		
		if (defaultNetworkInterfaceName.equals("default"))
			networkInterface = MeasureableNetworkInterface.getDefultNetworkInterface();
		else
		{
			try
			{
				networkInterface = new MeasureableNetworkInterface(NetworkInterface.getByName(defaultNetworkInterfaceName));
			}
			catch (Exception e2)
			{
				// worst case : take the default :
				networkInterface = MeasureableNetworkInterface.getDefultNetworkInterface();
			}
		}
		
		// supported feature only on windows 
		// TODO: fill in linux/mac support
		if (PlatformDependencies.getCurSysType() == PlatformDependencies.WINDOWS)
			this.setDefaultNetworkInterface(networkInterface.getHostName(), networkInterface.getIP());

		MyBoolean reachedDest = new MyBoolean(false);//check
		boolean traceSucc = this.executeNativeMTR(allNetHosts, destIp, measurementType, protocol, num, initialPort, reachedDest); //This is the actual measurement call

		PingTracerouteResults resultsMTR = new PingTracerouteResults(source, source_ip, host, destIp, CommUtils.ipToLong(destIp), -1, comm, Protocol.getName(protocol), ts, localTime, num);
		Vector details = new Vector();

		long l = 0;
		long org = 0;
		InetAddress IPAddress;
		int valid_index = 0;
		
		for (int i = 0; i < allNetHosts.length; ++i)
		{
			if (allNetHosts[i].getHopAddress() != 0)
			{
				valid_index = i;
			}
		}
		
		for (int i = 0; i < valid_index + 1; ++i)
		{
			if (!allNetHosts[i].valid)
			{
				break;
			}
			
			
			NetHost netHost = allNetHosts[i];
			traceResultExcept = this.fixAddress(netHost);
			netHost.setException(traceResultExcept);
			Vector alternatives = netHost.getAlternatives();
			
			if (alternatives != null)
			{
				for (int j = 0; j < alternatives.size(); ++j)
					this.fixAddress((NetHost) alternatives.get(j));
			}

			details.add(i, allNetHosts[i]);
		}
		
		if (!traceSucc)
		{
			resultsMTR.setSF(false);
			resultExcept = resultExcept + "Measurement to destination cannot be completed\n";
			resultsMTR.setException(resultExcept);
			if (details.size() == 0)//measurement failed in the init part
				details.add(0, new NetHost());//check
		}
		else
		{
			resultsMTR.setSF(true);
			//			if (reachedDest.value)
			//				if (!resultsMTR.getDestIp().equalsIgnoreCase(allNetHosts[valid_index].getHopAddressStr()))
			//				    System.out.println("--------> sent to IP: "+resultsMTR.getDestIp()+" ---- received from: "+allNetHosts[valid_index].getHopAddressStr());//debug
		}
		
		resultsMTR.setReachedDest(reachedDest.value);
		resultsMTR.setRawVector(details);
		return resultsMTR;
		
	}

	private String fixAddress(NetHost aHost)
	{
		String traceResultExcept = "";
		if (aHost.getHopAddress() != 0)
		{
			try
			{
				InetAddress IPAddress = InetAddress.getByName(aHost.getHopAddressStr());
				aHost.setHopNameStr(IPAddress.getHostName());
			}
			catch (UnknownHostException e)
			{
				traceResultExcept = traceResultExcept + "Name of hop cannot be resolved\n";
			}
		}

		if (aHost.getHopAddress() < 0)
		{
			long x = aHost.getHopAddress();
			long y = 2 ^ 32 - x;
			aHost.setHopAddress(y);
		}
		return traceResultExcept;
	}

	public static void main(String[] args) throws Exception
	{
		//        try
		//        {
		System.load("C:\\dimes\\GeneralDevelopmentCPP\\Measurements\\MTR_dll\\Release\\MTR.dll");
		System.out.println("C:\\dimes\\GeneralDevelopmentCPP\\Measurements\\MTR_dll\\Release\\MTR.dll");
		CallMTR call = new CallMTR();
		call.loadLibraries("C:\\dimes\\eclipse\\workspace\\DevelopmentAgent\\resources");
		System.out.println("C:\\dimes\\eclipse\\workspace\\DevelopmentAgent\\resources");
		//call.setDefaultNetworkInterface(null, null);

		//            Runtime.getRuntime().addShutdownHook(new Thread() {
		//    			
		//    			public void run(){
		//    				System.out.println("Shutdown hook activated!");
		//    			}
		//
		//    		});
		//    		
		//			MyBoolean bool1 = new MyBoolean(false);;
		//			for (int m = 49; m<255;m++)
		//			{
		//				for (int i = 1; i < 255; i++) {
		//					NetHost[] allNetHosts;
		//					Random rand = new Random();
		//					int measurementType = MeasurementType.TRACEROUTE;
		//					if (measurementType == MeasurementType.PING)
		//						allNetHosts = new NetHost[1];
		//					else
		//						allNetHosts = new NetHost[MAX_HOSTS];
		//					for (int k = 0; k < allNetHosts.length; ++k)
		//						allNetHosts[k] = new NetHost();
		//					long startTime = System.currentTimeMillis();
		//					System.out.println("Executing to :" + i + "."+m);
		//					boolean result = call.executeNativeMTR(allNetHosts, i
		//							+ "."+m+"."+rand.nextInt(256)+".193", MeasurementType.TRACEROUTE,
		//							Protocol.UDP, 1, 35445, bool1);
		//					long endTime = System.currentTimeMillis();
		//					System.out.println("Execution lasted :" + (endTime-startTime) + "ms");
		//
		//					for (int j = 0; j < MAX_HOSTS; j++) {
		//						if (allNetHosts[j].valid)
		//							System.out.println(allNetHosts[j]
		//									.getHopAddressStr()
		//									+ ":" +allNetHosts[j].getHopAddress()+":"+ allNetHosts[j].getBestTime());
		//					}
		//					System.out.println("***********************************");
		//				}
		//			}
		//           
		//        }
		//        catch (RuntimeException e)
		//        {
		//            // TODO Auto-generated catch block
		//            e.printStackTrace();
		//        }

	}

}