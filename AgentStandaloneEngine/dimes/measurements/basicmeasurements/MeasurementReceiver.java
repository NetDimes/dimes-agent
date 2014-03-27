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
//			System.out.println(receivedPacket.getHeader().getHeaderId(null));
			Header receivedIcmpHeader = receivedPacket.getHeaderById(ICMPv4Header.IPv4_PROTO_ICMP);
			IPv4Header receivedIpHeader = (IPv4Header) receivedPacket.getHeaderById(IPv4Header.IPv4_PROTO_IP);
//			System.out.println(receivedIpHeader.toString());
//			System.out.println(receivedIcmpHeader.toString());
//			int protocol = receivedIpHeader.getNextHeaderId();
			int ipID = receivedIpHeader.getIdentity();//receivedPacket.getHeader().getHeaderId(null);
			
			if (receivedIcmpHeader != null && receivedIcmpHeader instanceof ICMPv4Header)
			{
				int errorCode = -1;
				
				ICMPv4Header mainIcmpHeader = (ICMPv4Header) receivedIcmpHeader;
				errorCode = mainIcmpHeader.code;
				boolean isValidReply = false;
				short id = -1000;
				if (mainIcmpHeader.type == ICMPv4Header.ICMP_TIME_EXCEEDED) {
					String msg = "Got packet ICMP_TIME_EXCEEDED";
					InnerIPv4Header innerIpHeader = (InnerIPv4Header) receivedPacket.getHeaderById(InnerIPv4Header.INNER_IP_HEADER);
					if (innerIpHeader != null) {
						id = (short) (innerIpHeader.identity - measurementData.getFirstID());
						msg += (" with id = " + id + " from " + receivedIpHeader.sourceIP);
						isValidReply = true;
					} 
				//	System.out.println(msg);
				} else if (mainIcmpHeader.type== ICMPv4Header.ICMP_ECHO_REPLY) {
					String msg = "Got packet ECHO REPLY ";
					id = (short) (mainIcmpHeader.sequence - measurementData.getFirstID());
					msg += (" with id = " + id + " from " + receivedIpHeader.sourceIP);
					isValidReply = true;
			//		System.out.println(msg);
					if (ignoreAfterDestReached && id >= 0 && id < destTTL) {
						measurementData.setDestReached();
					}
				} else if (mainIcmpHeader.type== ICMPv4Header.ICMP_DEST_UNREACHABLE) {
					String msg = "Got packet DEST UNREACHABLE ";
					InnerIPv4Header innerIpHeader = (InnerIPv4Header) receivedPacket.getHeaderById(InnerIPv4Header.INNER_IP_HEADER);
					if (innerIpHeader != null) {
						// check source, dest, code here.
						// sometimes we get garbage... why?
						if (innerIpHeader.sourceIP.equals(measurementData.getSourceAddress()) &&
								innerIpHeader.destIP.equals(measurementData.getDestAddress())) {
							id = (short) (innerIpHeader.identity - measurementData.getFirstID());
							msg += (" with id = " + id + " code " + mainIcmpHeader.code + " from " + receivedIpHeader.sourceIP + " and matching src ( " + innerIpHeader.sourceIP + " ),  dst = " + innerIpHeader.destIP);
							msg += " seq " + mainIcmpHeader.sequence;
							isValidReply = true;
//							errorCode = mainIcmpHeader.code;
							if (ignoreAfterDestReached && id >= 0 && id < destTTL) {
								measurementData.setDestReached();
							}
						} 
					}
			//		System.out.println(msg);
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
					measurementData.addResult(id, receivedIpHeader.sourceIP, microsecReceiveTime, mainIcmpHeader.type, errorCode, ipID);
				} else {
					int origID = measurementData.getFirstID() + id;
					Iterator<MeasurementCycle> itr = measurements.iterator();
					while (itr.hasNext()) {
						MeasurementCycle mc = itr.next();
						if (origID >= mc.getFirstID()) {
							id = (short) (origID - mc.getFirstID());
							if (id >= 0 && id <= mc.getLastID()) {
								//System.err.println("***** LATE ARRIVALS! Adding result for " + id + "( " + mc.getFirstTTL() + " )");
								mc.addResult(id, receivedIpHeader.sourceIP, microsecReceiveTime, mainIcmpHeader.type, errorCode, ipID);
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
		//return measurementData.isDestReached();
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
//			System.out.println("------------------------");
			NetHostLocal nhl = routeItr1.next(); //nh1 - NetHost1, the first itiration of the measurement. 
			ComplexNetHost nh = new ComplexNetHost(nhl.getTTL(), nhl.getDelayInt(), nhl.getHostAddress(), 
					                               nhl.getHostName(), nhl.getReplyType(), nhl.getErrorCode(), nhl.getIPID()); 
//TODO: Debug
/*			
			System.out.println("-------------TYPE: "+nh.getReplyType());
			System.out.println(nhl.getIPID());
			System.out.println("-------------CODE: "+nh.getErrorCode());
			*/
			results.add(nh);
			//System.err.println("adding host at " + nhl.getTTL() + " (1)");
		}
		
		Iterator<MeasurementCycle> cycleItr = measurements.iterator();
		while (cycleItr.hasNext()) {
			MeasurementCycle mc = cycleItr.next();
			LinkedList<NetHostLocal> route2 = mc.getRoute();
			// merge
			Iterator<NetHostLocal> routeItr2 = route2.iterator();	//The next itiration
			Iterator<ComplexNetHost> mainRouteItr = results.iterator(); //The first intiration is now stored in results, so we get it from there. 
			while (routeItr2.hasNext()) {
				if (mainRouteItr.hasNext()) {
					NetHostLocal nhl2 = routeItr2.next();
					ComplexNetHost nh = mainRouteItr.next();
					if (nh.getHopAddressStr().equalsIgnoreCase(nhl2.getHostAddress())) { //If the first itiration matches the next one, we just add the delay info
						nh.addMeasurement(nhl2.getDelayInt());
					} else {
						// should see if to swap main with alt
						if (nh.getAlternatives().size() > 0) {
							Iterator<NetHost> altItr = nh.getAlternatives().iterator();
							while (altItr.hasNext()) {
								ComplexNetHost altHost = (ComplexNetHost) altItr.next();
								// swap if alternative was met more than twice or it has an infomative 
								// address (to make sure the main hop is not an "empty" one)
								if (altHost.getHopAddressStr().equalsIgnoreCase(nhl2.getHostAddress())) {
									altHost.addMeasurement(nhl2.getDelayInt());
									if ((altHost.getOccurences() > halfThreshold) ||
										((altHost.getOccurences() == halfThreshold) && (altHost.getHopAddressStr() != null) && 
										 (altHost.getHopNameStr() != null) && (!altHost.getHopNameStr().equalsIgnoreCase("unknown")))) {
										// i should be the main!
										altHost.addAlternative(nh);
										// yes, we are each other's alternative, this is right!
										nh.removeAlternative(altHost);
										nh.swap(altHost);
										//System.err.println("*** SWAPPED! ***");
									}
									break;
								}
							}
						} else {
							nh.addAlternative(new ComplexNetHost(nhl2.getTTL(), nhl2.getDelayInt(), nhl2.getHostAddress(), 
															     nhl2.getHostName(), nhl2.getReplyType(), nhl2.getErrorCode(), nhl2.getIPID()));
						}
					}
				} else {
					do {
						NetHostLocal nhl2 = routeItr2.next();
						// main route is shorter, init to the new hop
						ComplexNetHost nh = new ComplexNetHost(nhl2.getTTL(), nhl2.getDelayInt(), nhl2.getHostAddress(), 
															   nhl2.getHostName(), nhl2.getReplyType(), nhl2.getErrorCode(), nhl2.getIPID());
						nh.setReplyType(nhl2.getReplyType());
						System.out.println(nhl2.getReplyType());
						nh.setErrorCode(nhl2.getErrorCode());
						System.out.println(nhl2.getErrorCode());
						results.add(nh);
						//System.err.println("adding host at " + nhl2.getTTL() + " (2)");
					} while (routeItr2.hasNext());
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
