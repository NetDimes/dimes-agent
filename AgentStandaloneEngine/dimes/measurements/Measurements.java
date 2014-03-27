package dimes.measurements;
/*
 * Created on 22/01/2004
 */

//import java.net.InetAddress;
//import java.net.MalformedURLException;
//import java.net.UnknownHostException;
//import java.util.Vector;
//import java.sql.Timestamp;
import java.util.LinkedList;
//import java.util.Vector;
import java.util.logging.Logger;

//import dimes.comm2server.ConnectionHandlerFactory;
//import dimes.comm2server.StandardCommunicator;
//import dimes.measurements.newOps.MeasurementsSender;
//import dimes.measurements.newOps.TrainMeasurementOp;
import dimes.measurements.basicmeasurements.Measurement;
import dimes.measurements.basicmeasurements.BasicMeasurementSender;
import dimes.measurements.operation.MeasurementOp;
//import dimes.measurements.operation.PackettrainOp;
//import dimes.measurements.operation.PeerPacketTrainOp;
import dimes.measurements.operation.ParisTracerouteOp;
import dimes.measurements.operation.PingOp;
import dimes.measurements.operation.QbeOp;
import dimes.measurements.operation.TracerouteOp;
//import dimes.measurements.operation.TreerouteOp;
import dimes.measurements.qbe.QBESender;
import dimes.measurements.results.DimesQBEResults;
import dimes.measurements.results.PingTracerouteResults;
import dimes.measurements.results.Results;
//import dimes.measurements.results.TreerouteResults;
//import dimes.measurements.treeroute.TreerouteAgent;
//import dimes.rendezvous.net.HttpProxiedPeerCommincator;
import dimes.scheduler.Priority;
import dimes.scheduler.Task;
import dimes.util.Lock;
import dimes.util.comState.ComStateChangeEvent;
import dimes.util.comState.ComStateDetector;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
//import dimes.util.properties.PropertiesBean;
//import dimes.util.properties.PropertiesNames;
//import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/*
 * Galit - you should use this class to activate the different modules
 * The Scheduler calls these methods when it executes an operation.
 */

/**
 * @author anat
 * This class is used as a facade for the measurements module. It is like a singleton,
 * since all its methods are static. External access to all measurement modules is
 * done only through it.
 */
public final class Measurements
{
//	private static final int MAX_HOSTS = 50;
	private static final int MTR_PING_NUM = 4;
	private static final int MIN_MEASUREMENT_TIMEOUT = 10000;
	private static final int TRACEROUTE_PACKET_TIMEOUT = 15;
	private static final int PING_TTL = 50;
	private static final int TRACEROUTE_TTL = -1;
	
	private static boolean internetConnectionExist = true;
	private static boolean executing = false;
	private static boolean agentIndexSet=false;
//	private static boolean isStandalone=false;
	private static int agentIndex= 0x3; /*268435471; /*0x0fff ffff*///used as a unique index for measurements like packetTrain

	private static ComStateDetector comStateWatch;
	private static Lock executingLock = new Lock();
	private static Logger logger = Loggers.getLogger(Measurements.class);
//	private static TreerouteAgent treerouteAgent=null;
//	private static MeasurementsSender measurementsSender=MeasurementsSender.getInstance();


	static
	{
		comStateWatch = ComStateDetector.getInstance();
		internetConnectionExist = comStateWatch.connectionExists();
	}

//		String rendezvousCommString;
//		StandardCommunicator rendezvousCommunicator=null;
//		
// Removed 0.6  - No Treeroute anymore
//		try 
//		{
//			rendezvousCommString = PropertiesBean.getProperty(PropertiesNames.RENDEZVOUS_URL);
//		}
//		catch (NoSuchPropertyException e) 
//		{
//			e.printStackTrace();
//			rendezvousCommString = HttpProxiedPeerCommincator.RENDEZVOUS_URL;
//		}
//		
//
//		
//		try 
//		{
//			rendezvousCommunicator = new StandardCommunicator(rendezvousCommString,ConnectionHandlerFactory.NONSECURE_CONNECTION);
//			treerouteAgent = new TreerouteAgent(rendezvousCommunicator); //moved here to avoid a situation where the treerouteAgent is initialized even though an exception was thrown 
//		}
//		catch (MalformedURLException e) 
//		{
//			logger.warning("Couldnt initialize Rendezvous Communicator :" + e.getMessage());
//			e.printStackTrace();
//		}
//		
//		
//	}

	/**
	 * should only be called once - when agent starts running - by parser.parseHeader
	 * @param anAgentIndex
	 */
	static public void setAgentIndex(int anAgentIndex)
	{
		/*System.out.println*/logger.finest("set agent index to " + anAgentIndex);//debug
		agentIndexSet = true;
		agentIndex = anAgentIndex;
	}
	
	public static int getAgentIndex(){
		return agentIndex;
	}
	
	public static boolean agentIndexSet(){
		return agentIndexSet;
	}

	/**
	 * Default implementation - used to catch all <execute> calls with an Operation type
	 * that is not supported. Supported methods (Ping, for now) override this method,
	 * and use it to activate the relevant module.
	 */
	static public boolean execute(MeasurementOp op) throws NoSuchOperationException
	{
		logger.info(op.toString());
		logger.info("Executing default op.execute");
		if (!internetConnectionExist)
		{
			logger.warning("Measurments Denied - no communication");
			return false; // Action wasn't Performed.
		}
		return op.execute();
	}
	/**
	 * Implementation for the New Bi-Directional QBE based measurements.
	 * 
	 * @param op
	 * @return
	 */
/*	static public boolean execute(TrainMeasurementOp op){
		System.out.println("Executing NewOp default");
		if (!internetConnectionExist)
		{
			logger.warning("Measurments Denied - no communication");
			return false; // Action wasn't Performed.
		}
		try {
			return op.execute(); 
		}catch (Exception e){
			logger.warning("Operation failed :" + e.getMessage());
			e.printStackTrace();
			return false;
		}		
	}*/
	
	static public boolean execute(ParisTracerouteOp op)
    {
        System.out.println("Executing ParisTrace operation...");
        PingTracerouteResults results = executeMeasurement(op.getHostIP(), MeasurementType.PARIS_TRACEROUTE, op.getProtocol(), MTR_PING_NUM, op.getSourcePort(), op.getDestPort());
		if (results == null) {
			return false;
		}
		Logger resultWriter = Loggers.getResultWriter();
		Task containing = op.getContainingTask();
		String resultStr = Measurements.toXML(containing.getExID(), containing.getID(), Priority.getName(containing.getPriority()), results);
		resultWriter.finer(resultStr);
        return true;
    } 
	//removed 0.6 BoazH
/*	static public boolean execute(PeerPacketTrainOp op)
	{
		System.out.println("Executing a QBE operation...");
		try 
		{
			//  fill this if client or server...
		}
		catch (Exception e) 
		{
			logger.warning("QBE Operation failed :" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	*/
	static public boolean execute(QbeOp op)
	{
		System.out.println("Executing a QBE operation...");
		try 
		{
			// Galina: add bidirectional flag to QBESender construction 
			int maxTrainSize = op.getMaxTrainSize();
			QBESender sender = new QBESender(true, maxTrainSize);
			sender.sendPackets(op);
			
			Logger resultWriter = Loggers.getResultWriter();
			Task containing = op.getContainingTask();
			LinkedList<DimesQBEResults> results = sender.getResults(op);
			while (results.size() > 0) {
				DimesQBEResults res = (DimesQBEResults) results.removeFirst();
				String resultStr = Measurements.toXML(containing.getExID(), containing.getID(), Priority.getName(containing.getPriority()), res);
				resultWriter.finer(resultStr);
			}
		} catch (Exception e) {
			logger.warning("QBE Operation failed :" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	static public boolean execute(PingOp op)
	{
		PingTracerouteResults results = executeMeasurement(op.getHostIP(), MeasurementType.PING, op.getProtocol(), MTR_PING_NUM,  op.getSourcePort(), op.getDestPort());
		if (results == null) {
			return false;
		}
		Logger resultWriter = Loggers.getResultWriter();
		Task containing = op.getContainingTask();
		String resultStr = Measurements.toXML(containing.getExID(), containing.getID(), Priority.getName(containing.getPriority()), results);
		resultWriter.finer(resultStr);
		return true; // Action was Performed.
	}

	static public boolean execute(TracerouteOp op)
	{
		PingTracerouteResults results = executeMeasurement(op.getHostIP(), MeasurementType.TRACEROUTE, op.getProtocol(), MTR_PING_NUM, op.getSourcePort(), op.getDestPort());
		if (results == null) {
			return false;
		}
		Logger resultWriter = Loggers.getResultWriter();
		Task containing = op.getContainingTask();
		String resultStr = Measurements.toXML(containing.getExID(), containing.getID(), Priority.getName(containing.getPriority()), results);
		resultWriter.finer(resultStr);
        return true;
	}
	
	static PingTracerouteResults executeMeasurement(String destName, int measurementType, int protocol, int nTrials, short srcPort, short dstPort) {
		try {
			BasicMeasurementSender sender = new BasicMeasurementSender((byte)PING_TTL);
	        LinkedList<Measurement> measurements = new LinkedList<Measurement>();
	        int ttl = measurementType == MeasurementType.PING ? PING_TTL : TRACEROUTE_TTL;
	        Measurement m = new Measurement(destName, protocol, srcPort, dstPort, ttl, 0, TRACEROUTE_PACKET_TIMEOUT);
	        measurements.add(m);
	        sender.execute(measurementType, measurements, MTR_PING_NUM, MIN_MEASUREMENT_TIMEOUT);
	        PingTracerouteResults results = m.getResults();
	     // temporary print for debugging
			sender.printResults((short)PING_TTL);
	        return results;
		} catch (Exception e)
        {
            logger.warning("Traceroute Operation failed :" + e.getMessage());
            e.printStackTrace();
            return null;
        }
	}

//	
//	// steger -- packettrain measurement execution point inserted
//	static public boolean execute(PackettrainOp op)
//	{
//		Results result = Measurements.measurementsMTR.executeMTR(op.ipList, op.protocol, op.numberOfRobins, op.port, op.packetsize, op.delay_usec, agentIndex);
//		return true;
//	}
	
//	static public boolean execute(TreerouteOp op){
//		
//		TreerouteResults treeResults=null;
//		
//		if(treerouteAgent != null)
//			treeResults = treerouteAgent.execute(op); //run the actual measurement
//		
//		if (treeResults != null)
//		{
//			Logger resultWriter = Loggers.getResultWriter();
//			String resultStr = Measurements.toXML(op.getContainingTask().getExID() , op.getContainingTask().getID(), 
//					Priority.getName(op.getContainingTask().getPriority()) , treeResults);
//			resultWriter.finer(resultStr);
//			System.out.println("Results :" + resultStr);
//		}
//		
//		return true;
//		
//	}
	
	public static int getMTR_PING_NUM() {
		return MTR_PING_NUM;
	}

	/* ************* todo - move these methods somewhere appropriate ************************//*
		//6/2009 moved to dimes.measurements.IPUtils - Boazh
	
	*//*returns IP address of local machine, except in the case where it's both a private address
	 * and behind a proxy. should be called only after registration - so proxy setting are already known.
	 * 
	 * @return String IP
	 * @throws UnknownHostException
	 *//*
	static public String getHostIP() throws UnknownHostException
	{
		InetAddress localhost = InetAddress.getLocalHost();
		String IP = localhost.getHostAddress();
		logger.fine("IP: "+IP);
		logger.fine("localhost: "+localhost);
		logger.fine("isPrivateIP(IP): "+isPrivateIP(IP) +" isBehindProxy(): "+isBehindProxy());
		if (isPrivateIP(IP) && isBehindProxy())//private but no proxy can be resolved by server
		{
			String nextPublicIP = getNextPublicIP();
			logger.fine("nextPublicIP"+nextPublicIP);

		}
		return IP;
	}

	static public boolean isPrivateIP(String anIP) //Is also used by DisplayServer, so leave it public
	{
		if (anIP.startsWith("10.") || anIP.startsWith("192.168") || anIP.equals("127.0.0.1"))
			return true;
		return false;
	}

	static public boolean isValidAddress(String addr)
	{
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
			useProxy = Boolean.valueOf(PropertiesBean.getProperty(PropertiesNames.USE_PROXY"comm.useProxy")).booleanValue();
		}
		catch (Exception e)
		{
			logger.warning(e.toString());
			useProxy = false;
		}
		return useProxy;
	}

	*//**
	 * @return
	 *//*
	static public String getNextPublicIP()
	{
		String destName;
		
		try
		{
			destName = PropertiesBean.getProperty(PropertiesNames.BASIC_TRACE_DEST + "1""urls.basicTraceDest1");
		}
		catch (NoSuchPropertyException e)
		{
			logger.warning(e.toString());
			destName = "www.netdimes.org";//just as default
		}
		
		PingTracerouteResults result = Measurements.measurementsMTR.executeMTR(destName, MeasurementType.TRACEROUTE, Protocol.getDefault(), MTR_PING_NUM, 33435);
		String srcIP = result.getSourceIp();
		
		if (!isPrivateIP(srcIP))
			return srcIP;
		
		String currIP = srcIP; //initialize
		Vector details = result.getRawVector();
		
		for (int i = 0; isPrivateIP(currIP) && i < details.size(); ++i)
		{
			NetHost aDetail = (NetHost) details.get(i);
			if (aDetail.getHopAddress() != 0) //valid address - otherwise, it's an unknown host
				currIP = aDetail.getHopAddressStr();
		}
		
		if (isPrivateIP(currIP)) //if only returned private IPs, return first on (source)
			return srcIP;
		
		return currIP;
	}

	static public boolean isProtocolBlocked(int proto)
	{
		System.err.printlnlogger.finest("-------- checking protocol: " + Protocol.getName(proto) + " --------");//debug
		for (int i = 1; i < 3; ++i)//3 basicTraceDests exist in the propFile - checking 2
		{
			String destName;
			try
			{
				destName = PropertiesBean.getProperty(PropertiesNames.BASIC_TRACE_DEST"urls.basicTraceDest"+ i);
			}
			catch (NoSuchPropertyException e)
			{
				logger.warning(e.toString());
				destName = "www.netdimes.org";//just as default
			}

			System.err.printlnlogger.finest("measuring to " + destName);//debug
			PingTracerouteResults result = Measurements.measurementsMTR.executeMTR(destName, MeasurementType.TRACEROUTE, proto, MTR_PING_NUM, TracerouteOp.initialPort);
			Vector hops = result.getRawVector();
			System.err.printlnlogger.finest("no. of hops is " + hops.size());//debug
			if (result.isReachedDest() && (hops.size() > 2))
			{
				System.err.printlnlogger.finest("protocol " + Protocol.getName(proto) + " is NOT blocked");
				return false;
			}
			if (hops.size() == 0)
				continue;

			String currIP = "";
			int publicNum = 0;
			for (int j = 0; j < hops.size(); ++j)
			{
				NetHost aHop = (NetHost) hops.get(j);
				if (aHop.getHopAddress() != 0) //valid address - otherwise, it's an unknown host
				{
					currIP = aHop.getHopAddressStr();
					if (!isPrivateIP(currIP))
					{
						++publicNum;
						if (publicNum > 2)
						{
							System.err.printlnlogger.finest("protocol " + Protocol.getName(proto) + " is NOT blocked");
							return false;
						}
					}
					else
						System.err.printlnlogger.finest("private: " + currIP);//debug
				}
				else
					System.err.printlnlogger.finest("invalid ip");//debug
			}
			System.err.printlnlogger.finest("public num: " + publicNum);//debug

		}

		System.err.printlnlogger.finest("protocol " + Protocol.getName(proto) + " is blocked");
		return true;
	}
	*//**************************************************************************************//*
*/
	public static String toXML(String exID, String scriptId, String priority, Results res){
		return toXML(exID, scriptId, priority, res,"\t\t");
	}
	
	public static String toXML(String exID, String scriptId, String priority, Results res,String tabs)
	{
		String result = "";
//		Vector rawVector = res.getRawVector();

//		String exception = res.getException();
//		if (rawVector == null)
//			exception = "Raw vector is null.";
//		boolean hasRawDetails = res.hasRawDetails();

		result += tabs + "<ExID>" + exID + "</ExID>" + "\n";
		result += tabs + "<ScriptID>" + scriptId + "</ScriptID>" + "\n";
		result += tabs + "<Priority>" + priority + "</Priority>" + "\n";
		result += res.formatOperationDetails(tabs);
		result += tabs + "<RawDetails>" + "\n";
		tabs += "\t";

		if (!res.hasRawDetails()) //raw vector is null
			return result;

		result += res.formatRawDetails(tabs);
		tabs = tabs.substring(1);
		result += tabs + "</RawDetails>" + "\n";
		tabs = tabs.substring(1);
		result = tabs + "<OperationResult>" + "\n" + result + tabs + "</OperationResult>" + "\n";
		return result;
	}
	

	public static void ComStatceChangeOccurred(ComStateChangeEvent evt)
	{
		internetConnectionExist = evt.isConnected;
	}

	public static boolean isExecuting()
	{
		return executing;
	}

	/** Blocks while there are active measurements (IE while it waits for the execution lock.) 
	 * 
	 */
	public static void waitWhileExecuting()
	{
		while (isExecuting())
			executingLock.waitFor();
		setExecuting(true);//check
	}

	/**Sets the executing flag and releases teh lock if executing==false
	 * @param isExecuting
	 */
	public synchronized static void setExecuting(boolean isExecuting)
	{
		executing = isExecuting;
		if (!executing)
			executingLock.release();//check
	}


}