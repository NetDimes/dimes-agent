package dimes.measurements;
public class MultiRoute {}
//import java.lang.reflect.InvocationTargetException;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Vector;
//
//import dimes.measurements.nio.CallbackContext;
//import dimes.measurements.nio.NetworkStack;
//import dimes.measurements.nio.RawNetworkStack;
//import dimes.measurements.nio.error.MalfromedPacketException;
//import dimes.measurements.nio.error.MeasurementException;
//import dimes.measurements.nio.error.MeasurementInitializationException;
//import dimes.measurements.nio.packet.Packet;
//import dimes.measurements.nio.packet.PacketBuffer;
//import dimes.measurements.nio.packet.analysis.IcmpProtocolAnalyzer;
//import dimes.measurements.nio.packet.analysis.PacketAnalyzer;
//import dimes.measurements.nio.packet.builder.IcmpPacketBuilder;
//import dimes.measurements.nio.packet.header.IPHeaderDescriptor;
//import dimes.measurements.nio.packet.header.IPProtocol;
//import dimes.measurements.nio.packet.header.IcmpHeaderDescriptor;
//import dimes.measurements.nio.packet.header.IcmpProtocol;
//import dimes.measurements.nio.platform.NetworkStackLibraryLoader;
//
///**
// * @author Ohad Serfaty
// *
// */
//public class MultiRoute {
//
//	private static String localIP;
//	NetworkStack networkStack;
//	IcmpPacketBuilder icmpPacketBuilder;
//	PacketAnalyzer packetAnalyzer;
//	private IcmpProtocolAnalyzer icmpPacketAnalyzer;
//	private PacketAnalyzer innerIpAnalyzer;
//	public static final int MAX_TTL = 50;
//	private int matchedPackets;
//	
//	public void setup(Class networkStackClass, String[] arguments) throws MeasurementInitializationException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
////		if (arguments == null)
//		networkStack = (NetworkStack) networkStackClass.getConstructor(null).newInstance(null);
////		else
////			networkStack = (NetworkStack) networkStackClass.getConstructor(new Class[]{String[].class}).newInstance(new Object[]{arguments});
//		if (arguments == null)
//			networkStack.init();
//		else
//			networkStack.init(arguments);
//		icmpPacketBuilder = new IcmpPacketBuilder(networkStack);
//		packetAnalyzer = networkStack.getPacketAnalyzer();
//		icmpPacketAnalyzer = (IcmpProtocolAnalyzer) packetAnalyzer.getProtocolAnalyzer(IPProtocol.IP_PROTO_ICMP);
//		innerIpAnalyzer = icmpPacketAnalyzer.getInnerIpHeaderAnalyzer();
//	}
//
//	public void executeTrace(final String destIPs[]) throws MeasurementException
//	{
//		// build packets :
//		final int numberOfDests = destIPs.length;
//		IPHeaderDescriptor ipDescriptor = new IPHeaderDescriptor();
//		if(localIP!=null)
//			ipDescriptor.sourceIP = localIP;
//		IcmpHeaderDescriptor icmpDescriptor = new IcmpHeaderDescriptor();
//		
//		icmpDescriptor.type = IcmpProtocol.ICMP_ECHO;
//		final HashMap traceroutesMap = new HashMap();
//		final HashMap delaysMap = new HashMap();
//		for (int j=0; j<numberOfDests; j++)
//		{
//			traceroutesMap.put(destIPs[j] ,new Vector());
//			delaysMap.put(destIPs[j] , new Vector());
//		}
//		
//		for (int i=1; i<20; i++)
//		{
//			PacketBuffer buffer = new PacketBuffer();
//			final HashMap ipIdMap = new HashMap();
//			for (int j=0; j<numberOfDests; j++)
//			{
//				// build packet and send :
//				ipDescriptor.destIP = destIPs[j];
//				ipDescriptor.ipID = (short) (100 + j*i);
//				ipDescriptor.ttl = (byte) i;
//				icmpDescriptor.id = ipDescriptor.ipID;
//				icmpDescriptor.sequence = (short) (300+j*i);
//				ipIdMap.put(new Short(ipDescriptor.ipID) , destIPs[j]);
//				Packet packetToSend = icmpPacketBuilder.buildPacket(ipDescriptor , icmpDescriptor);
//				
//				buffer.add(packetToSend);
//			}
//			final long[] sendTimes = networkStack.send(buffer);
//			final long sendTime = sendTimes[0];
//			this.matchedPackets = 0;
//			// receive a reply (listen 2.5 seconds max ):
//			networkStack.receive(2500 ,new CallbackContext(){
//
//				
//
//				public boolean callback() {
//					return false;
//				}
//
//				public boolean callback(byte[] p, long milisecReceiveTime, long microsecReceiveTime) {
//					
//					try 
//					{
//						System.out.println("received packet from " + packetAnalyzer.getSourceIP(p));
//						if (packetAnalyzer.getProtocol(p) == IPProtocol.IP_PROTO_ICMP){
//							if (icmpPacketAnalyzer.hasInnerIcmpHeader(p))
//							{
//								int innerIpId = innerIpAnalyzer.getIpID(p);
//								String destIP = (String) ipIdMap.get(new Short((short) innerIpId));
//								if (destIP!=null)
//								{
//									Vector tracerouteVector = (Vector) traceroutesMap.get(destIP);
//									Vector delaysVector = (Vector) delaysMap.get(destIP);
//									tracerouteVector.add(packetAnalyzer.getSourceIP(p));
//									delaysVector.add(new Long( microsecReceiveTime - sendTime));
//									matchedPackets++;
//									return matchedPackets == numberOfDests;
//								}
//									
//							}
//							else
//								if (icmpPacketAnalyzer.isIcmpEchoReply(p))
//								{
//									
//								}
//						}
//					}
//					catch (MalfromedPacketException e) 
//					{
//						e.printStackTrace();
//					}
//					return false;
//				}
//				
//			});
//		}
//		
//		for (int k=0; k<numberOfDests; k++){
//			String destIP = destIPs[k];
//			Iterator i = ((Vector)traceroutesMap.get(destIP)).iterator();
//			Iterator j = ((Vector)delaysMap.get(destIP)).iterator();
//			System.out.println("Traceroute results for IP ="+destIP);
//			while (i.hasNext())
//				System.out.println(i.next() + " - " + (double)(((Long)j.next()).longValue())/1000.0 + " milisec");
//		}
//		
//		
//	}
//	
//	public static void main(String[] args) throws Exception {
//		String[] destIPs = new String[]{ "216.55.44.33" , "132.66.48.22" , "51.3.22.3" };
//		String[] arguments = null;
//		Class networkStackClass = RawNetworkStack.class;
////		if (args.length > 0){
////			if (args[0].equalsIgnoreCase("--help"))
////				usage();
////			destIP = args[0];
////			if (args.length > 1)
////				if (args[1].equals("-etomic"))
////				{
////					networkStackClass = EtomicNetworkStack.class;
////					if (args.length > 2)
////						arguments = new String[]{args[2]};
////				}
////				if (args[1].equals("-ip"))
////				{
////					localIP = args[2];
////					arguments = new String[]{args[2]};
////				}
////					
////		}
//		NetworkStackLibraryLoader.loadLibrary();
//		MultiRoute tracert = new MultiRoute();
//		tracert.setup(networkStackClass , arguments);
//		tracert.executeTrace(destIPs);
//	}
//
//
//}
