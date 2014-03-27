/**
 * 
 */
package dimes.measurements;

//import java.nio.ByteBuffer;
//import java.util.HashMap;
//import java.util.Vector;
//import java.util.logging.Logger;

//import sun.security.x509.NetscapeCertTypeExtension;
import dimes.measurements.nio.CallbackContext;
import dimes.measurements.nio.EtomicNetworkStack;
//import dimes.measurements.nio.MacLevelNetworkStack;
import dimes.measurements.nio.NativeLogger;
//import dimes.measurements.nio.error.DeserializeException;
import dimes.measurements.nio.error.MeasurementException;
import dimes.measurements.nio.error.MeasurementInitializationException;
//import dimes.measurements.nio.packet.CodedPacketFilter;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.builder.PacketBuilder;
//import dimes.measurements.nio.packet.header.Header;
//import dimes.measurements.nio.packet.header.IPv4Header;
//import dimes.measurements.nio.packet.header.Payload;
//import dimes.measurements.nio.packet.header.UDPHeader;
import dimes.measurements.nio.platform.NetworkStackLibraryLoader;

/**
 * @author Ohad Serfaty
 *
 */
public class EtomicQBEReceiver {
	



	private EtomicNetworkStack netStack;
	private PacketBuilder packetBuilder;

////	private final QBEResults results = new QBEResults();
//
	/**
	 * @throws MeasurementInitializationException 
	 * 
	 */
	public EtomicQBEReceiver() throws MeasurementInitializationException {
//		Logger logger = Logger.getAnonymousLogger();
		
		netStack = EtomicNetworkStack.getInstance();
		netStack.setNativeLogger(new NativeLogger(){

			public void log(String logLevelString, String logMessage) {
				
			}
			
		});
		netStack.init();
		
		packetBuilder = netStack.getPacketBuilder();
		if (packetBuilder != null)
		{
			System.err.println("packetBuilder != null");
			}
}
//	
//	
//
//	private void receive() throws MeasurementException {
//		System.out.println("Receiving");
//		netStack.receive(25000 , new CallbackContext(){
//
//			public boolean callback() {
//				return false;
//			}
//
//			public boolean callback(byte[] p, long milisecReceiveTime, long microsecReceiveTime) {
//				Packet packet = new Packet(null , netStack);
//				try 
//				{
//					packet.fromByteArray(p);
//					System.out.println("Packet : " + p);
////					Header ipHeader = packet.getHeaderById(IPv4Header.IPv4_PROTO_IP);
////					Header header = packet.getHeaderById(UDPHeader.IP_PROTO_UDP);
////					
////					if (ipHeader!=null && ipHeader instanceof IPv4Header && header != null)
////					{
////						UDPHeader udpHeader =  (UDPHeader) header;
////						if (udpHeader.destPort == 7777)
////						{
//////							System.out.println("Packet received...");
////							// acquire the payload :
////							Header payloadHeader = udpHeader.getHeaderById(0);
////							Payload payload = (Payload) payloadHeader;
////							QBEPacket qbePacket = this.interpretQBEPacket((IPv4Header)ipHeader , payload,milisecReceiveTime);
////							results.addPacket(qbePacket.agentIndex , qbePacket);
////						}
//////						else
//////							System.out.println("Port:" +udpHeader.destPort );
////					}
//				}
//				catch (Exception e) 
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				//System.out.println(packet);
//				return false;
//			}
//		});
//		

//			private QBEPacket interpretQBEPacket(IPv4Header ipHeader, Payload payload , long receivedTimeStamp) {
//				QBEPacket result= new QBEPacket();
//				result.receiveDetails = new QBEPacketDetails();
//				result.sendDetails = new QBEPacketDetails();
//				
//				// analyze the received information from the IP Header :
//				result.receiveDetails.tos = ipHeader.getTypeOfService();
//				result.receiveDetails.ttl = ipHeader.getTTL();
//				result.receiveDetails.protocol = ipHeader.getNextHeaderId();
//				result.receiveDetails.packetSize = ipHeader.getHeadersTotalLength();
//				
//				// analyze received information from the Payload :
//				ByteBuffer buffer = ByteBuffer.allocate(payload.getBytes().length);
//				buffer.put(payload.getBytes());
//				buffer.position(0);
//				
////				 analyze the sent information from the packet :
//				result.agentIndex = (int)buffer.getShort();
//				result.sendDetails.tos = (byte) buffer.getShort();
//				result.sendDetails.protocol = buffer.getShort();
//				result.sendDetails.ttl = (byte) buffer.getShort();
//				
//				// general packet IDs : 
//				result.trainId =  buffer.getInt();
//				result.sequenceId =  buffer.getInt();
//				
//				// time stamps :
//				result.sendDetails.timestamp = buffer.getLong();		// read from the packet.
//				result.receiveDetails.timestamp = receivedTimeStamp;
//				
//				
//				
//				return result;
//			}
//			
//		});
//		
//	}
//	
//	
//	private class QBEPacketDetails {
//		
//		public byte tos;
//		public byte ttl;
//		public int protocol;
//		public int packetSize;
//		
//		
//		public long timestamp;
//		
//	}
//	
//	private class QBEPacket {
//		
//		int agentIndex;
//		int trainId;
//		int sequenceId;
//		
//		public QBEPacketDetails sendDetails;
//		public QBEPacketDetails receiveDetails;
//		
//		public String toString(){
//			return "TrnId:"+trainId + " Seq:" + sequenceId;
//		}
//		
//	}
//	
//	private class QBEResults{
//		
//		HashMap agentPackets = new HashMap();
//		
//		public void addPacket(int agentIndex , QBEPacket packet){
//			System.out.println("Agent #" + agentIndex + " Adding Packet :" + packet);
//			Integer agentIndexKey = new Integer(agentIndex);
//			Vector packetsVector = (Vector) agentPackets.get(agentIndexKey);
//			if (packetsVector == null)
//			{
//				packetsVector = new Vector();
//				agentPackets.put(agentIndexKey , packetsVector);
//			}
//			packetsVector.add(packet);
//		}
//		
//	}
	
	
	private void receive() throws MeasurementException {
		System.out.println("EtomicQBEReceiver -> receive()");
		netStack.receive(25000 , new CallbackContext(){

			public boolean callback() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean callback(byte[] p, long milisecReceiveTime, long microsecReceiveTime) {
				try
				{
					Packet packet = new Packet(netStack);
					packet.fromByteArray(p);
					System.out.println("***********************\n Accepted packet : \n" + packet + "\n*******************");
				}
				catch (Exception e){
					e.printStackTrace();
				}
				return false;
			}});
	}
	
	public static void main(String[] args) throws Exception {
		NetworkStackLibraryLoader.loadLibrary();
		EtomicQBEReceiver receiver = new EtomicQBEReceiver();
		receiver.receive();
	}

	
	
}
