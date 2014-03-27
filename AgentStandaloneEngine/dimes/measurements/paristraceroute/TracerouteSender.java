package dimes.measurements.paristraceroute;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import dimes.measurements.MeasurementType;
import dimes.measurements.Protocol;
import dimes.measurements.nio.NetworkStack;
import dimes.measurements.nio.RawNetworkStack;
import dimes.measurements.nio.error.MeasurementInitializationException;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.builder.PacketBuilder;
import dimes.measurements.nio.packet.header.Header;
import dimes.measurements.nio.packet.header.ICMPv4Header;
import dimes.measurements.nio.packet.header.IPv4Header;
import dimes.measurements.nio.packet.header.UDPHeader;
import dimes.measurements.results.PingTracerouteResults;
import dimes.util.CommUtils;
import dimes.util.time.TimeUtils;

public class TracerouteSender {
	private NetworkStack networkStack;
	private PacketBuilder packetBuilder;
	@SuppressWarnings("unused")
	private byte maxTTL;
	TracerouteReceiver receiver;
	IPv4Header   ipHeader = new IPv4Header();
	UDPHeader    udpHeader = new UDPHeader();
	ICMPv4Header icmpv4Header = new ICMPv4Header();
	PingTracerouteResults results;
	//PacketBuffer packetBuffer;
	InetAddress  localAddress;
	
	public TracerouteSender(byte maxRouteLength) throws MeasurementInitializationException {
		networkStack = RawNetworkStack.getInstance();
		//String test="";
		
		try {
			localAddress=InetAddress.getLocalHost();
			if (localAddress.isLoopbackAddress()){
				NetworkInterface iface = null;

				 for(Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();ifaces.hasMoreElements();){
					   iface = ifaces.nextElement();
					   //System.out.println("Interface:"+ iface.getDisplayName());
					   InetAddress ia = null;
					   for(Enumeration<InetAddress> ips =    iface.getInetAddresses();ips.hasMoreElements();){
						   ia = ips.nextElement();
						   if (ia.isLoopbackAddress()) {
							   continue;
						   }
						   localAddress = ia;
					   }
				  }
			}

			// test= InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
			networkStack.init(new String[]{localAddress.getHostAddress()});
			}catch(MeasurementInitializationException mie){
				networkStack.close();
			}
		packetBuilder = networkStack.getPacketBuilder();
		maxTTL = maxRouteLength;
		receiver = new TracerouteReceiver(maxRouteLength, networkStack);
		//packetBuffer = new PacketBuffer(this.networkStack);
	}	
	
	// For this list of measurements, run each measurement nTrials times
	// the whole thing is limited to timeOut milis
	// measurementType is needed only to fill the results form and goes straight to the database
	public void execute(int measurementType, LinkedList<Measurement> measurements, int nTrials, long timeOut) throws Exception {

		//InetAddress sourceIP = InetAddress.getLocalHost();
		ipHeader.sourceIP = localAddress;
		
		// all packets for this list of measurements will get different IDs
		short idx=1;
		long startTime = System.currentTimeMillis();
		long finishTime = startTime+timeOut;
		long timeIntervalCycles = timeOut/nTrials/2;
		long finishTimeCycle = startTime+timeIntervalCycles;
		long nextSendTime = startTime;
			
		Iterator<Measurement> itr = measurements.iterator();
		while (itr.hasNext()) {
			Measurement m = itr.next();
			String destIP = m.getDestAddress();
			ipHeader.destIP = InetAddress.getByName(destIP);
			int protocol = m.getProtocol();
			long timeInterval = m.getTimeToWait();
			maxTTL = (byte) m.getLastTTL();
				
			// should we check timeOut here? 0 = no results allocation?
			results = new PingTracerouteResults(localAddress.getHostName(), localAddress.getHostAddress(), ipHeader.destIP.getHostName(),
						ipHeader.destIP.getHostAddress(), CommUtils.ipToLong(ipHeader.destIP.getHostAddress()), 
					    -1, MeasurementType.getName(measurementType), Protocol.getName(protocol), TimeUtils.getTimeStamp(), TimeUtils.getLocalTime(), nTrials);
				
			for (byte i=0; i<nTrials; i++, idx++) {
				MeasurementCycle currentMeasurement = new MeasurementCycle(idx, m.getLastTTL(), localAddress, ipHeader.destIP);
			
//				boolean done = false;
				receiver.startNewTrace(currentMeasurement);
				for (int j=m.getFirstTTL(); j<=m.getLastTTL(); j++, idx++) {
					ipHeader.ttl = (byte)j;
					ipHeader.identity = idx;
					ipHeader.tos = (byte)m.getTos();
						
					Packet packetToSend = null;
					
					if (protocol == Protocol.ICMP) {
						icmpv4Header.type =ICMPv4Header.ICMP_ECHO_REQUEST;
						icmpv4Header.code =0;
						icmpv4Header.checksum =0x0;
						icmpv4Header.sequence=idx;
						icmpv4Header.identifier=0x0400;
						
						if (measurementType == MeasurementType.PARIS_TRACEROUTE) {
							icmpv4Header.identifier -= idx;
						}
						
						if (m.getPayload() == null) {
							packetToSend = packetBuilder.buildPacket(new Header[]{ipHeader , icmpv4Header});
						} else {
							packetToSend = packetBuilder.buildPacket(new Header[]{ipHeader , icmpv4Header, m.getPayload()});
						}
					} else if (protocol == Protocol.UDP) {
						udpHeader.sourcePort = m.getSourcePort(); //(short) 33331;
						udpHeader.destPort = m.getDestPort();
						udpHeader.checksum = 0;
						udpHeader.udpLength = (short) udpHeader.getHeaderLength();
						if (m.getPayload() == null) {
							packetToSend = packetBuilder.buildPacket(new Header[]{ipHeader , udpHeader});
						} else {
							packetToSend = packetBuilder.buildPacket(new Header[]{ipHeader , udpHeader, m.getPayload()});
						}
					} else {
						// not supported operation - or should we take the default instead?
						continue;
					}
				
					long timeToWait = nextSendTime-System.currentTimeMillis();
					if (timeToWait > 0) {
							/*
							if (packetBuffer.size() != 0) {
								long[] sendTimes = networkStack.send(packetBuffer);
								Packet[] packets = packetBuffer.getOrderedPackets();
								for (int p=0; p<packets.length; p++) {
									currentMeasurement.addPacket((byte)(packets[p].+1-m.getFirstTTL()), sendTimes[p]);
								}
								packetBuffer.clear();
							}*/
						// use this time to receive packets instead of sleep - will get packages too late otherwise
						networkStack.receive(timeToWait, receiver);
					} 
					
					long sendTime = networkStack.sendPacket(packetToSend.toByteArray());
					// do we need currentPacket or we can use j instead ? 
					currentMeasurement.addPacket((byte)(j+1-m.getFirstTTL()), sendTime);
					// 50 mili since the time we tried to send, not the actual send 
					nextSendTime += timeInterval;
				
					// need condition here
					if (receiver.isDestReached()) {
						break;
					}
				}
				
				if (!receiver.isDestReached()) {
					//long timeToWait = finishTimeCycle - System.currentTimeMillis();
					long timeToWait = 1000; //  1s
					if (timeToWait > 0) {
						//System.err.println("*** CYCLE " + i + " WAITING FOR " + timeToWait + " ms ");
						networkStack.receive(timeToWait, receiver);
					}
				}
				finishTimeCycle += timeIntervalCycles;
				maxTTL = (byte) receiver.getDestTTL();
			}
			long timeToWait = finishTime - System.currentTimeMillis();
			if (timeToWait > 0) {
				//System.err.println("*** ALL SENT : WAITING FOR " + timeToWait + " ms ");
				networkStack.receive(timeToWait, receiver);
			}
			// set results
			results.setRawVector(receiver.getPingTracerouteResults());
			// dest reached - not correct so far!
			results.setReachedDest(receiver.isDestReachedAtLeastOnce());
			results.setSF(true);
			m.setResults(results);
		}
		networkStack.close();
	}
	
	public void printResults(short length) {
		receiver.printResults(length);
	}
}
