package dimes.measurements;

//import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
//import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Set;

import dimes.measurements.nio.CallbackContext;
//import dimes.measurements.nio.MacLevelNetworkStack;
import dimes.measurements.nio.NetworkStack;
//import dimes.measurements.nio.PublicIPInquirer;
import dimes.measurements.nio.RawNetworkStack;
import dimes.measurements.nio.error.DeserializeException;
import dimes.measurements.nio.error.MeasurementException;
//import dimes.measurements.nio.error.MeasurementInitializationException;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.builder.PacketBuilder;
import dimes.measurements.nio.packet.header.Header;
//import dimes.measurements.nio.packet.header.HeadersRepository;
import dimes.measurements.nio.packet.header.ICMPv4Header;
import dimes.measurements.nio.packet.header.IPv4Header;
import dimes.measurements.nio.packet.header.InnerICMPHeader;
import dimes.measurements.nio.packet.header.InnerIPv4Header;
import dimes.measurements.nio.platform.NetworkStackLibraryLoader;

/**
 * @author Ohad Serfaty
 *
 * A Simple Implementation of Traceroute in Java , using the JavaNetworkStack API.
 *
 */
public class Traceroute {

	private static String localIP;
	NetworkStack networkStack;
	private short currentIpId;
	public static final int MAX_TTL = 50;
	private TreeMap tracerouteVector = new TreeMap();
	private TreeMap delaysVector = new TreeMap();
	private PacketBuilder packetBuilder;
	private InetAddress sourceIp;
	
	public void setup(String[] arguments) throws Exception{
		// Start with a Raw entwork stack :
		networkStack = RawNetworkStack.getInstance();
		networkStack.init();
		packetBuilder = networkStack.getPacketBuilder();
		sourceIp = InetAddress.getLocalHost();
		// use the default packet builder and packet analyzer :
	}

	/**
	 * Execute a traceroute to a specific destination IP :
	 * 
	 * @param destIP
	 * @throws MeasurementException
	 */
	public void executeTrace(final InetAddress destIP) throws Exception
	{
		
		
		
		
		for (int i=1 ; i<MAX_TTL; i++)
		{
			// set the packet details :
			IPv4Header ipHeader = new IPv4Header();
			ipHeader.sourceIP = sourceIp;
			ipHeader.destIP = destIP;
			ICMPv4Header icmpHeader = new ICMPv4Header();
			
			ipHeader.ttl = (byte) i;
			ipHeader.identity = (byte) i;
			
			Packet packet = packetBuilder.buildPacket(new Header[]{ipHeader , icmpHeader});
			// send :
			final long sendTime = networkStack.sendPacket(packet.toByteArray());
			// and wait for an answer :
			networkStack.receive(2500 ,new CallbackContext(){

				public boolean callback() {
					return false;
				}

				public boolean callback(byte[] p, long milisecReceiveTime, long microsecReceiveTime) {
					Packet receivedPacket = new Packet(networkStack);
					try 
					{
						receivedPacket.fromByteArray(p);
						//System.out.println(receivedPacket);
						Header receivedIcmpHeader = receivedPacket.getHeaderById(ICMPv4Header.IPv4_PROTO_ICMP);
						IPv4Header receivedIpHeader = (IPv4Header) receivedPacket.getHeaderById(IPv4Header.IPv4_PROTO_IP);
						if (receivedIcmpHeader != null && receivedIcmpHeader instanceof ICMPv4Header)
						{
							ICMPv4Header mainIcmpHeader = (ICMPv4Header) receivedIcmpHeader;
							if (mainIcmpHeader.type == ICMPv4Header.ICMP_TIME_EXCEEDED){
								InnerIPv4Header innerIpHeader;
								InnerICMPHeader innerIcmpHeader;
								Header testHeader1 = receivedPacket.getHeaderById(InnerIPv4Header.INNER_IP_HEADER);
								Header testHeader2 = receivedPacket.getHeaderById(InnerICMPHeader.INNER_ICMP_HEADER);
								if (testHeader1 != null && testHeader2!=null){
									innerIpHeader = (InnerIPv4Header) testHeader1;
									innerIcmpHeader = (InnerICMPHeader) testHeader2;
//									System.out.println("source : "+innerIpHeader.sourceIP + " ->" +innerIpHeader.sourceIP.equals(sourceIp));
//									System.out.println("dest   : "+innerIpHeader.destIP +" ->" +innerIpHeader.destIP.equals(destIP));
									if (innerIpHeader.sourceIP.equals(sourceIp)) {
										if (innerIpHeader.destIP.equals(destIP)) {
											System.out.println("Received Valid Packet from " +receivedIpHeader.sourceIP );
											byte ttl = (byte) innerIpHeader.identity;
											if (ttl < 0) {
												ttl *= -1;
											}
											Byte ttlKey = new Byte(ttl);
											InetAddress add = (InetAddress) tracerouteVector.get(ttlKey);
											if (add != null) {
												System.out.println("entry with ttl " + ttl + " exist, ip = " + add);
											} else {
												System.out.println("inserting ttl " + ttl);
											}
											tracerouteVector.put(ttlKey, receivedIpHeader.sourceIP);
											delaysVector.put(ttlKey, new Long( microsecReceiveTime - sendTime));
											System.out.println(receivedIpHeader.sourceIP + " - " + (double)(microsecReceiveTime - sendTime)/1000.0 + " milisec");
											return true;
										} 
									}
									
								}
								
								
							}
							else
								if (mainIcmpHeader.code== ICMPv4Header.ICMP_ECHO_REPLY){
									
									return true;
								}
								
						}
					}
					catch (DeserializeException e) 
					{
						e.printStackTrace();
					}
					return false;
				}
				
			});
			
		}
//		
//		icmpDescriptor.type = IcmpProtocol.ICMP_ECHO;
//		ipDescriptor.destIP = destIP;
//		for (int i=1; i<20; i++)
//		{
//			// build packet and send :
//			ipDescriptor.ipID = (short) (564 + i);
//			currentIpId = ipDescriptor.ipID;
//			ipDescriptor.ttl = (byte) i;
//			icmpDescriptor.id = ipDescriptor.ipID;
//			icmpDescriptor.sequence = (short) (675+i);			
//			Packet packetToSend = icmpPacketBuilder.buildPacket(ipDescriptor , icmpDescriptor);
//			final long sendTime = networkStack.sendPacket(packetToSend.buffer);
//			
//			// receive a reply (listen 2.5 seconds max ):
//			// Callback functions are called whenever a Packet is received
//			// and matches the filter :
//			networkStack.receive(2500 ,new CallbackContext(){
//
//				public boolean callback() {
//					return false;
//				}
//
//				public boolean callback(byte[] p, long milisecReceiveTime, long microsecReceiveTime) {
//					System.out.println("received packet...");
//					try 
//					{
//						if (packetAnalyzer.getProtocol(p) == IPProtocol.IP_PROTO_ICMP){
//							if (icmpPacketAnalyzer.hasInnerIcmpHeader(p))
//							{
//								int innerIpId = innerIpAnalyzer.getIpID(p);
//								if (innerIpId == currentIpId)
//								{
//									tracerouteVector.add(packetAnalyzer.getSourceIP(p));
//									delaysVector.add(new Long( microsecReceiveTime - sendTime));
//									return true;
//								}
//									
//							}
//							else
//								if (icmpPacketAnalyzer.isIcmpEchoReply(p))
//								{
//									// TODO : ...
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
		
		int maxTtl = ((Byte) tracerouteVector.lastKey()).intValue();
		for (byte i=0; i<=maxTtl; i++) {
			Byte keyttl = new Byte(i);
			Long delay = (Long) delaysVector.get(keyttl);
			if (delay != null) {
				InetAddress addr = (InetAddress) tracerouteVector.get(keyttl);
				System.out.println(addr + " - " + (double)(delay.longValue())/1000.0 + " milisec");
			}
		}
		/*
		Iterator i = this.tracerouteVector.iterator();
		Iterator j = this.delaysVector.iterator();
		System.out.println("Traceroute results :");
		while (i.hasNext())
			System.out.println(i.next() + " - " + (double)(((Long)j.next()).longValue())/1000.0 + " milisec");
			*/		
	}
	
	public static void main(String[] args) throws Exception {
		String destIP = "216.55.44.33";
		NetworkStackLibraryLoader.loadLibrary();
		Traceroute tracert = new Traceroute();
		tracert.setup(args);
		tracert.executeTrace(InetAddress.getByName(destIP));
	}

}
