package dimes.measurements.basicmeasurements;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import dimes.measurements.NetHost;
import dimes.measurements.nio.CallbackContext;
import dimes.measurements.nio.NetworkStack;
import dimes.measurements.nio.error.DeserializeException;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.header.Header;
import dimes.measurements.nio.packet.header.ICMPv4Header;
import dimes.measurements.nio.packet.header.IPv4Header;
import dimes.measurements.nio.packet.header.InnerIPv4Header;
import dimes.measurements.results.ComplexNetHost;

public class MeasurementReceiver implements CallbackContext {
	private NetworkStack networkStack;
	private MeasurementCycle measurementData;
	private short destTTL;
	private LinkedList<MeasurementCycle> measurements;
	private short maxLength;
	private boolean ignoreAfterDestReached;
	
	public MeasurementReceiver(short maxRouteLength, NetworkStack myStack) {
		networkStack = myStack;
		destTTL = maxRouteLength;
		measurements = new LinkedList<MeasurementCycle>();
		maxLength = maxRouteLength;
		ignoreAfterDestReached = true;
	}
	
	public void startNewTrace(MeasurementCycle mc) {
		if (measurementData != null) {
			measurements.addFirst(measurementData);
		}
		measurementData = mc;
	}
	
	public boolean isDestReached() {
		return measurementData.isDestReached();
	}
	
	public void setGetAllMode() {
		ignoreAfterDestReached = false;
	}
	
	public boolean isDestReachedAtLeastOnce() {
		boolean destReached = measurementData.isDestReached();
		Iterator<MeasurementCycle> itr = measurements.iterator();
		while (destReached == false && itr.hasNext()) {
			MeasurementCycle mc = itr.next();
			destReached |= mc.isDestReached();
		} 
		return destReached;
	}
	
	public short getDestTTL() {
		return destTTL;
	}
	
	public boolean callback() {
		return false;
	}

	public boolean callback(byte[] p, long milisecReceiveTime, long microsecReceiveTime) {
		Packet receivedPacket = new Packet(networkStack);
		try 
		{
			receivedPacket.fromByteArray(p);
			Header receivedIcmpHeader = receivedPacket.getHeaderById(ICMPv4Header.IPv4_PROTO_ICMP);
			IPv4Header receivedIpHeader = (IPv4Header) receivedPacket.getHeaderById(IPv4Header.IPv4_PROTO_IP);
			int ipID = receivedIpHeader.getIdentity();
			
			if (receivedIcmpHeader != null && receivedIcmpHeader instanceof ICMPv4Header)
			{
				int errorCode = -1;
				
				ICMPv4Header mainIcmpHeader = (ICMPv4Header) receivedIcmpHeader;
				errorCode = mainIcmpHeader.code;
				boolean isValidReply = false;
				short id = -1000;
				if (mainIcmpHeader.type == ICMPv4Header.ICMP_TIME_EXCEEDED) {
					InnerIPv4Header innerIpHeader = (InnerIPv4Header) receivedPacket.getHeaderById(InnerIPv4Header.INNER_IP_HEADER);
					if (innerIpHeader != null) {
						id = (short) (innerIpHeader.identity - measurementData.getFirstID());
						isValidReply = true;
					} 
				} else if (mainIcmpHeader.type== ICMPv4Header.ICMP_ECHO_REPLY) {
					id = (short) (mainIcmpHeader.sequence - measurementData.getFirstID());
					isValidReply = true;
					if (ignoreAfterDestReached && id >= 0 && id < destTTL) {
						measurementData.setDestReached();
					}
				} else if (mainIcmpHeader.type== ICMPv4Header.ICMP_DEST_UNREACHABLE) {
					InnerIPv4Header innerIpHeader = (InnerIPv4Header) receivedPacket.getHeaderById(InnerIPv4Header.INNER_IP_HEADER);
					if (innerIpHeader != null) {
						// check source, dest, code here.
						// sometimes we get garbage... why?
						if (innerIpHeader.sourceIP.equals(measurementData.getSourceAddress()) &&
								innerIpHeader.destIP.equals(measurementData.getDestAddress())) {
							id = (short) (innerIpHeader.identity - measurementData.getFirstID());
							isValidReply = true;
							if (ignoreAfterDestReached && id >= 0 && id < destTTL) {
								measurementData.setDestReached();
							}
						} 
					}
				} else {
					System.err.println("Got packet with type = " + mainIcmpHeader.type);
				}
				
				if (isValidReply == false) {
					return false;
				}
				
				if (id >= 0 && id < destTTL) {
					if (measurementData.isDestReached()) {
						if (destTTL == maxLength && id < (maxLength-3)) {
							destTTL = (short) (id+3);
						}
					}
					measurementData.addResult(id, receivedIpHeader.sourceIP, microsecReceiveTime, mainIcmpHeader.type, errorCode, ipID, receivedIpHeader.ttl);
				} else {
					int origID = measurementData.getFirstID() + id;
					Iterator<MeasurementCycle> itr = measurements.iterator();
					while (itr.hasNext()) {
						MeasurementCycle mc = itr.next();
						if (origID >= mc.getFirstID()) {
							id = (short) (origID - mc.getFirstID());
							if (id >= 0 && id <= mc.getLastID()) {
								mc.addResult(id, receivedIpHeader.sourceIP, microsecReceiveTime, mainIcmpHeader.type, errorCode, ipID, receivedIpHeader.ttl);
								break;
							}
						}
					}
				}
			}
		}
		catch (DeserializeException e) 
		{
			e.printStackTrace();
		}
		return measurementData.allReceived();
	}
	
	public void printResults(short length) {
		int maxTTL = measurementData.printRoute(length);
		int minTTL = maxTTL;
		Iterator<MeasurementCycle> itr = measurements.iterator();
		while (itr.hasNext()) {
			MeasurementCycle mc = itr.next();
			int x = mc.printRoute(length);
			if (x < minTTL) {
				minTTL = x;
			} else if (x > maxTTL) {
				maxTTL = x;
			}
		}
		if (maxTTL - minTTL > 2) {
			System.err.println("*** some route too short!!! " + (maxTTL - minTTL) + "***");
		}
	}
	
	public Vector<NetHost> getQBEResults() {
		Vector<NetHost> results = new Vector<NetHost>();
		MeasurementCycle mc = measurementData;
		Iterator<MeasurementCycle> cycleItr = measurements.iterator();
		do {
			LinkedList<NetHostLocal> route = measurementData.getRoute();
			Iterator<NetHostLocal> routeItr1 = route.iterator();
			while (routeItr1.hasNext()) {
				NetHostLocal nhl = (NetHostLocal) routeItr1.next();
				ComplexNetHost nh = new ComplexNetHost(nhl.getTTL(), nhl.getDelayInt(), nhl.getHostAddress(), 
						                               nhl.getHostName(), nhl.getReplyType(), nhl.getErrorCode(), nhl.getIPID());
				results.add(nh);
			}
			mc = cycleItr.next();
		} while (mc != null);

		return results;
	}
		
	public Vector<NetHost> getPingTracerouteResults() {
		int nTrials = measurements.size()+1;
		int halfThreshold = (nTrials+1)/2;
		LinkedList<NetHostLocal> route1 = measurementData.getRoute();
		LinkedList<ComplexNetHost> results = new LinkedList<ComplexNetHost>();
		Iterator<NetHostLocal> routeItr1 = route1.iterator();
		while (routeItr1.hasNext()) {
			NetHostLocal nhl = routeItr1.next(); //nh1 - NetHost1, the first itiration of the measurement. 
			ComplexNetHost nh = new ComplexNetHost(nhl.getTTL(), nhl.getDelayInt(), nhl.getHostAddress(), 
					                               nhl.getHostName(), nhl.getReplyType(), nhl.getErrorCode(), nhl.getIPID()); 
			results.add(nh);
		}
		
		Iterator<MeasurementCycle> measurementCycleIterator = measurements.iterator();
		
		boolean foundAlternativeFlag;
		
		while (measurementCycleIterator.hasNext()) {
			MeasurementCycle measuremetCycle = measurementCycleIterator.next();
			LinkedList<NetHostLocal> routeCycle = measuremetCycle.getRoute();
			// merge
			Iterator<NetHostLocal> routeCycleIterator = routeCycle.iterator();	//The next iteration
			Iterator<ComplexNetHost> mainRouteItr = results.iterator(); //The first iteration is now stored in results, so we get it from there. 
			while (routeCycleIterator.hasNext()) {
				if (mainRouteItr.hasNext()) {
					NetHostLocal alternateNetHost = routeCycleIterator.next();
					ComplexNetHost mainNetHost = mainRouteItr.next();
					if (mainNetHost.getHopAddressStr().equalsIgnoreCase(alternateNetHost.getHostAddress())) { //If the first iteration matches the next one, we just add the delay info
						mainNetHost.addMeasurement(alternateNetHost.getDelayInt());
					} else {
						//reset the alternative flag
						foundAlternativeFlag = false;						
						// should see if to swap main with alt
						if (mainNetHost.getAlternatives().size() > 0 ) {
							Iterator<NetHost> altItr = mainNetHost.getAlternatives().iterator();
							while (altItr.hasNext()) {
								ComplexNetHost altHost = (ComplexNetHost) altItr.next();
								// swap if alternative was met more than twice or it has an informative 
								// address (to make sure the main hop is not an "empty" one)
								if (altHost.getHopAddressStr().equalsIgnoreCase(alternateNetHost.getHostAddress())) {
									//we found a matching alternative
									foundAlternativeFlag = true;
									altHost.addMeasurement(alternateNetHost.getDelayInt());
									if ((altHost.getOccurences() > halfThreshold) ||
										((altHost.getOccurences() == halfThreshold) && (altHost.getHopAddressStr() != null) && 
										 (altHost.getHopNameStr() != null) && (!altHost.getHopNameStr().equalsIgnoreCase("unknown")))) {
										// i should be the main!
										altHost.addAlternative(mainNetHost);
										// yes, we are each other's alternative, this is right!
										mainNetHost.removeAlternative(altHost);
										mainNetHost.swap(altHost);
									}
									break;
								}
							}
							//checks if no alternative matches current one and then adds it to the alternatives list
							if (!foundAlternativeFlag) {
								//checks if the alternative is not null
								if ((alternateNetHost.getHostAddress() != null) && (alternateNetHost.getHostName() != null) && (!alternateNetHost.getHostName().equalsIgnoreCase("unknown")))
									mainNetHost.addAlternative(new ComplexNetHost(alternateNetHost.getTTL(), alternateNetHost.getDelayInt(), alternateNetHost.getHostAddress(), 
										alternateNetHost.getHostName(), alternateNetHost.getReplyType(), alternateNetHost.getErrorCode(), alternateNetHost.getIPID()));
							}
						} else {
							//if mainNetHost is a null then swap it with the alternative and throw away the mainNetHost
							if ((mainNetHost.getHopAddressStr() == null) || (mainNetHost.getHopNameStr() == null) || (mainNetHost.getHopNameStr().equalsIgnoreCase("unknown"))) {
								mainNetHost.swap(new ComplexNetHost(alternateNetHost.getTTL(), alternateNetHost.getDelayInt(), alternateNetHost.getHostAddress(), 
										alternateNetHost.getHostName(), alternateNetHost.getReplyType(), alternateNetHost.getErrorCode(), alternateNetHost.getIPID()));
							}
							else
								//if mainNetHost isn't a null then check if the alternative is null and only then add it
								if ((alternateNetHost.getHostAddress() != null) && (alternateNetHost.getHostName() != null) && (!alternateNetHost.getHostName().equalsIgnoreCase("unknown"))) {
									mainNetHost.addAlternative(new ComplexNetHost(alternateNetHost.getTTL(), alternateNetHost.getDelayInt(), alternateNetHost.getHostAddress(), 
										alternateNetHost.getHostName(), alternateNetHost.getReplyType(), alternateNetHost.getErrorCode(), alternateNetHost.getIPID()));
								}
						}
					}
				} else {
					do {
						NetHostLocal nhl2 = routeCycleIterator.next();
						// main route is shorter, init to the new hop
						ComplexNetHost nh = new ComplexNetHost(nhl2.getTTL(), nhl2.getDelayInt(), nhl2.getHostAddress(), 
															   nhl2.getHostName(), nhl2.getReplyType(), nhl2.getErrorCode(), nhl2.getIPID());
						nh.setReplyType(nhl2.getReplyType());
						System.out.println(nhl2.getReplyType());
						nh.setErrorCode(nhl2.getErrorCode());
						System.out.println(nhl2.getErrorCode());
						results.add(nh);
					} while (routeCycleIterator.hasNext());
					// the flow goes same way without the "break" instruction too, just for conc.mod. exception 
				//	break;
				}
			}
		}
		
		Vector<NetHost> pingTracerouteResults = new Vector<NetHost>();
		
		Iterator<ComplexNetHost> mainRouteItr = results.iterator();
		while (mainRouteItr.hasNext()) {
			ComplexNetHost nh = mainRouteItr.next();
			nh.setLostNum();
			pingTracerouteResults.add(nh);
			Vector<NetHost> nha = nh.getAlternatives();
			Iterator<NetHost> altRouteItr = nha.iterator();
			while (altRouteItr.hasNext()) {
				ComplexNetHost alt = (ComplexNetHost) altRouteItr.next();
				alt.setLostNum();
			}
		}
		
		return pingTracerouteResults;
	}
}
