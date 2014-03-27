/**
 * 
 */
package dimes.measurements;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Logger;

//import sun.security.x509.NetscapeCertTypeExtension;
import dimes.measurements.nio.CallbackContext;
import dimes.measurements.nio.MacLevelNetworkStack;
import dimes.measurements.nio.NativeLogger;
//import dimes.measurements.nio.error.DeserializeException;
import dimes.measurements.nio.error.MeasurementException;
import dimes.measurements.nio.error.MeasurementInitializationException;
import dimes.measurements.nio.packet.CodedPacketFilter;
import dimes.measurements.nio.packet.Packet;
//import dimes.measurements.nio.packet.builder.PacketBuilder;
import dimes.measurements.nio.packet.header.Header;
import dimes.measurements.nio.packet.header.IPv4Header;
import dimes.measurements.nio.packet.header.Payload;
import dimes.measurements.nio.packet.header.UDPHeader;
import dimes.measurements.nio.platform.NetworkStackLibraryLoader;

/**
 * @author Ohad Serfaty
 *
 */
public class QBEReceiver {

	private static String localName;
	private static String localIP;
	private static int localIPAddress;
	private MacLevelNetworkStack netStack;

	private final QBEResults results = new QBEResults("TestExperient" , "TestExperimentScriptID");

	static
	{
		// locate the machine local name :
		try 
		{
			localName = InetAddress.getLocalHost().getHostName();
			localIP = InetAddress.getLocalHost().getHostAddress();
			localIPAddress = ipToInteger(localIP);
		}
		catch (Exception e) 
		{
			System.err.println("Caught An error while locatine machine name :");
			e.printStackTrace();
		}
	}
	
	private static int ipToInteger(String ip) throws Exception {
		String[] integers = ip.split("\\.");
		if (integers.length != 4)
			throw new Exception ("Malformed IP String :" + ip);
		int result=0;
		for (int i=0; i<4; i++)
		{
			int powerOf = (int)Math.round(Math.pow(256.0 , (double)(3-i)));
//			System.out.println("--"+integers[i]+"--" + powerOf);
			result += powerOf * Integer.parseInt(integers[i]);
		}
		return result;
	}
	
	/**
	 * @throws MeasurementInitializationException 
	 * 
	 */
	public QBEReceiver() throws MeasurementInitializationException {
		Logger logger = Logger.getAnonymousLogger();
		
		netStack = MacLevelNetworkStack.getInstance();
		netStack.setNativeLogger(new NativeLogger(){

			public void log(String logLevelString, String logMessage) {
				
			}
			
		});
		netStack.init();
		
	}
	
	

	private void receive() throws MeasurementException {
		netStack.setPacketFilter(CodedPacketFilter.UDP_FILTER);
		netStack.openListener();
		System.out.println("Receiving");
		netStack.receive(25000 , new CallbackContext(){

			public boolean callback() {
				return false;
			}

			public boolean callback(byte[] p, long milisecReceiveTime, long microsecReceiveTime) {
				Packet packet = new Packet(null , netStack);
				try 
				{
					packet.fromByteArray(p);
					Header ipHeader = packet.getHeaderById(IPv4Header.IPv4_PROTO_IP);
					Header header = packet.getHeaderById(UDPHeader.IP_PROTO_UDP);
					
					if (ipHeader!=null && ipHeader instanceof IPv4Header && header != null)
					{
						UDPHeader udpHeader =  (UDPHeader) header;
						if (udpHeader.destPort == 7777)
						{
//							System.out.println("Packet received...");
							// acquire the payload :
							Header payloadHeader = udpHeader.getHeaderById(0);
							Payload payload = (Payload) payloadHeader;
							QBEPacket qbePacket = this.interpretQBEPacket((IPv4Header)ipHeader , payload,milisecReceiveTime);
							results.addPacket(qbePacket.agentIndex , qbePacket,((IPv4Header)ipHeader).sourceIP);
						}
//						else
//							System.out.println("Port:" +udpHeader.destPort );
					}
				}
				catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println(packet);
				return false;
			}

			private QBEPacket interpretQBEPacket(IPv4Header ipHeader, Payload payload , long receivedTimeStamp) {
				QBEPacket result= new QBEPacket();
				result.perPacketRceiveDetails = new QBEPacketDetails();
				result.perPacketSendDetails = new QBEPacketDetails();
				
				// analyze the received information from the IP Header :
				result.perPacketRceiveDetails.tos = ipHeader.getTypeOfService();
				result.perPacketRceiveDetails.ttl = ipHeader.getTTL();
				result.perPacketRceiveDetails.protocol = ipHeader.getNextHeaderId();
				result.perPacketRceiveDetails.packetSize = ipHeader.getHeadersTotalLength();
				
				// analyze received information from the Payload :
				ByteBuffer buffer = ByteBuffer.allocate(payload.getBytes().length);
				buffer.put(payload.getBytes());
				buffer.position(0);
				
//				 analyze the sent information from the packet :
				result.agentIndex = (int)buffer.getShort();
				result.perPacketSendDetails.tos = (byte) buffer.getShort();
				result.perPacketSendDetails.protocol = buffer.getShort();
				result.perPacketSendDetails.ttl = (byte) buffer.getShort();
				
				// general packet IDs : 
				result.trainId =  buffer.getInt();
				result.sendSequenceId =  buffer.getInt();
				
				// time stamps :
				result.perPacketSendDetails.timestamp = buffer.getLong();		// read from the packet.
				result.perPacketRceiveDetails.timestamp = receivedTimeStamp;
				
				
				
				return result;
			}
			
		});
		
	}
	
	
	private class QBEPacketDetails {
		
		public byte tos;
		public byte ttl;
		public int protocol;
		public int packetSize;
		public long timestamp;
		
	}
	
	private class QBEPacket {
		
		int agentIndex;
		int trainId;
		int sendSequenceId;
		
		public QBEPacketDetails perPacketSendDetails;
		public QBEPacketDetails perPacketRceiveDetails;
		public int receiverSequenceId;
		
		public String toString(){
			return "TrnId:"+trainId + " Seq:" + sendSequenceId;
		}

		public String toXml() 
		{
			return 
			"\t\t\t\t<SenderSequenceId>" +sendSequenceId +  "</SendSequenceId>\n" + 
			"\t\t\t\t<ReceivedSequenceId>" +receiverSequenceId +  "</ReceivedSequenceId>\n" + 
			"\t\t\t\t<SenderTos>" +perPacketSendDetails.tos +  "</SenderTos>\n" +
			"\t\t\t\t<ReceivedTos>" +perPacketRceiveDetails.tos +  "</ReceivedTos>\n" +
			"\t\t\t\t<SenderTTL>" +perPacketSendDetails.ttl +  "</SenderTTL>\n" +
			"\t\t\t\t<ReceiverTTL>" +perPacketRceiveDetails.ttl +  "</ReceiverTTL>\n" +
			"\t\t\t\t<SenderPacketSize>" +perPacketSendDetails.packetSize +  "</SenderPacketSize>\n" +
			"\t\t\t\t<ReceiverPacketSize>" +perPacketRceiveDetails.packetSize +  "</ReceiverPacketSize>\n"+
			"\t\t\t\t<SenderTimestamp>" +perPacketSendDetails.timestamp +  "</SenderTimestamp>\n" +
			"\t\t\t\t<ReceiverTimestamp>" +perPacketRceiveDetails.timestamp +  "</ReceiverTimestamp>\n"
			;
		}
		
	}
	
	private class QBEPacketTrain {
		
		public int trainId;
		int nextSequence = 0;
		Vector packetsVector = new Vector();
		private final int sendingAgentIndex;
		

		public QBEPacketTrain(int agentIndex) {
			this.sendingAgentIndex = agentIndex;
		}

		public boolean equals(Object obj){
			return ((QBEPacketTrain)obj).trainId == this.trainId;
		}
		
		public int hashCode(){
			return 773*trainId;
		}

		public void addPacket(QBEPacket packet) {
			packet.receiverSequenceId = nextSequence;
			nextSequence++;
			packetsVector.add(packet);
		}

		public String toXml() {
			String packetResults = "";
			for (Iterator j =  packetsVector.iterator(); j.hasNext();){
				QBEPacket packet = (QBEPacket) j.next();
				packetResults += packet.toXml();
			}
			return "\t\t<Detail>\n" 
			+"<TrainID>"+trainId+"</TrainID>\n"+
			"<AgentIndex>" + sendingAgentIndex + "</AgentIndex>\n"+
			packetResults+
			"\t\t</Detail>";
		}
		
	}
	
	private class QBEResults /*implements Iterable*/{
		
		private final String experimentId;
		private final String scriptID;

		public QBEResults(String experimentId , String scriptID){
			this.experimentId = experimentId;
			this.scriptID = scriptID;
		}
		
		HashMap agentPackets = new HashMap();
		HashMap agentIPs = new HashMap();
		HashMap agentTimeStamps = new HashMap();
		
		public void addPacket(int agentIndex , QBEPacket packet , InetAddress sourceIP )
		{
			System.out.println("Agent #" + agentIndex + " Adding Packet :" + packet);
			Integer agentIndexKey = new Integer(agentIndex);
			HashMap agentPacketTrains = (HashMap) agentPackets.get(agentIndexKey);
			if (agentPacketTrains == null)
			{
				agentPacketTrains = new HashMap();
				agentPackets.put(agentIndexKey,agentPacketTrains);
				agentIPs.put(agentIndexKey , sourceIP);
				agentTimeStamps.put(agentIndexKey , new Long(System.currentTimeMillis()));
			}
			Integer trainKey = new Integer(packet.trainId);
			QBEPacketTrain agentPacketTrain = (QBEPacketTrain) agentPacketTrains.get(trainKey);
			if (agentPacketTrain==null)
			{
				agentPacketTrain = new QBEPacketTrain(agentIndex);
				agentPacketTrains.put(trainKey,agentPacketTrain);
			}
			agentPacketTrain.addPacket(packet);
		}
		
		private String getTimeStamp(long timestamp) {
			TimeZone tz = TimeZone.getDefault();
			int offset = tz.getRawOffset(); 
			Timestamp ts = new Timestamp(timestamp - offset); //for GMT
			return ts.toString();
		}

		private String getLocalTime(long timestamp) {
			Calendar cal = Calendar.getInstance(TimeZone.getDefault());
			cal.setTimeInMillis(timestamp);
			String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
			java.text.SimpleDateFormat sdf = 
				new java.text.SimpleDateFormat(DATE_FORMAT);
			sdf.setTimeZone(TimeZone.getDefault());          
			String localTime =  sdf.format(cal.getTime());
			return localTime;
		}
		
		public Iterator iterator() {
			
			return new Iterator(){
				
				Iterator agentsIterator = agentPackets.keySet().iterator();

				public boolean hasNext() {
					return agentsIterator.hasNext();
				}

				public Object next() {
					Integer agentIndexKey = (Integer) agentsIterator.next();
					String sourceIP = ((InetAddress)agentIPs.get(agentIndexKey)).getHostAddress();
					String sourceName = ((InetAddress)agentIPs.get(agentIndexKey)).getHostName();
					long timestamp = ((Long)agentTimeStamps.get(agentIndexKey)).longValue();
					String startTimeStamp = getTimeStamp(timestamp);
					String localStartTime = getLocalTime(timestamp);
					StringBuffer result = new StringBuffer();
					result.append("\t<OperationResult>\n");
					result.append("\t\t\t<ExID>"+experimentId+"</ExID>\n");
					result.append("\t\t\t<ScriptID>"+scriptID+"</ScriptID>\n");
					result.append("\t\t\t<Priority>NORMAL</Priority>\n");
					result.append("\t\t\t<ScriptLineNum>-1</ScriptLineNum>\n");
					result.append("\t\t\t<StartTime>"+startTimeStamp+"</StartTime>\n");
					result.append("\t\t\t<LocalStartTime>"+localStartTime+"</LocalStartTime>\n");
					result.append("\t\t\t<CommandType>PACKETTRAIN</CommandType>\n");
					result.append("\t\t\t<Protocol>UDP</Protocol>\n");
					result.append("\t\t\t<SourceName>"+sourceName+"</SourceName>\n");
					result.append("\t\t\t<SourceIP>"+sourceIP+"</SourceIP>\n");
					result.append("\t\t\t<DestName>"+localName+"</DestName>\n");
					result.append("\t\t\t<DestIP>"+localIP+"</DestIP>\n");
					result.append("\t\t\t<DestAddress>"+localIPAddress+"</DestAddress>\n");
					result.append("\t\t\t<NumOfTrials>1</NumOfTrials>\n");
					result.append("\t\t\t<Success>true</Success>\n");
					result.append("\t\t\t<reachedDest>true</reachedDest>\n");
					result.append("\t\t\t<Exceptions></Exceptions>\n");
					result.append("\t\t<RawDetails>\n");
					HashMap agentPacketTraints = (HashMap) agentPackets.get(agentIndexKey);
					for (Iterator i = agentPacketTraints.values().iterator(); i.hasNext();){
						QBEPacketTrain packetTrain = ((QBEPacketTrain)i.next());
						result.append(packetTrain.toXml());
					}
					result.append("\t\t</RawDetails>\n");
					result.append("\t</OperationResult>\n");
					return result.toString();
				}

				public void remove() {
				}
				
			};
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		NetworkStackLibraryLoader.loadLibrary();
		QBEReceiver receiver = new QBEReceiver();
		receiver.receive();
	}
	
}
