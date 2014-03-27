package dimes.measurements.basicmeasurements;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.TreeMap;
import dimes.measurements.QBEHost;

//import dimes.measurements.nio.packet.header.ICMPv4Header;


public class MeasurementCycle {
	private short offset;
	private TreeMap<Short, NetHostLocal> hops;
	private int destReached;
	private InetAddress sourceIP;
	private InetAddress destIP;
	private PacketInfo[] sentPackets;
	private int 		 nSentPackets;
	
	public	MeasurementCycle(short offset, int maxRouteLength, InetAddress sourceIP, InetAddress destIP) {
		this.offset = offset;
		sentPackets = new PacketInfo[maxRouteLength];
		hops = new TreeMap<Short, NetHostLocal>();
		this.sourceIP = sourceIP;
		this.destIP = destIP;
		destReached = 0;
		nSentPackets = 0;
	}
	
	public void addPacket(short j, long sendTime) {
		sentPackets[j-1] = new PacketInfo(j, sendTime);
		++nSentPackets;
	}
	
	public void addResult(short idx, InetAddress destIP, long microsecReceiveTime, int replyType, int errorCode, int ipID) {
		PacketInfo myInfo = sentPackets[idx];
		if (myInfo != null) {
			short id = myInfo.getID();
			Short key = new Short(id);
			hops.put(key, new NetHostLocal(id, destIP, microsecReceiveTime - myInfo.getTime(), replyType, errorCode, ipID));
		}
	}
	
	public short getFirstID() {
		return offset;
	}
	
	public int getLastID() {
		return sentPackets.length-1;
	}
	
	public int printRoute(short length) {
		System.err.println("*** traceroute results *** :");
//		short maxTTLinCycle = 0;
		int nHops = hops.size();
		short printed = 0;
		short i;
		
		for (i=1; i<=length; i++) {
			Short key = new Short(i);
			NetHostLocal hop = (NetHostLocal) hops.get(key);
			if (hop != null) {
				System.err.println("(" + key + ") " + hop.getAddress().getHostAddress() + " - " + (double)(hop.getDelay())/1000.0 + " milisec ( " + hop.getReplyTypeString() + " )");
				System.err.println("TYPE: "+hop.getReplyType()+" CODE: "+hop.getErrorCode());
//				maxTTLinCycle = i;
				if (++printed == nHops) {
					break;
				}
				
			} else {
				//System.err.println("(" + keyttl + ") *** ");
			}
		}
		System.err.println("*** end ***");
		
		return i;
	}
	
	public LinkedList<NetHostLocal> getRoute() {
		LinkedList<NetHostLocal> route = new LinkedList<NetHostLocal>();
		int nHops = hops.size();
		int hopNumber = 1;
		short idx = 1;
		while (hopNumber <= nHops) {
			Short key = new Short(idx);
			NetHostLocal hop = (NetHostLocal) hops.get(key);
			if (hop != null) {
				route.add(hop);
				++hopNumber;
			} else {
				route.add(new NetHostLocal(idx, null, -1, 1, -1, -1));
			}
			++idx;
			if (hopNumber+1<0){
				System.out.println("wraparound!"+ idx);
				break;
			}
				
		}
		return route;
	}
	
	public LinkedList<QBEHost> getQBEResults(int expID, int trainNum) {
		int nHops = hops.size();
//		System.out.println("nHops: "+nHops);
		if (nHops == 0) {
			return null;
		}
		LinkedList<QBEHost> results = new LinkedList<QBEHost>();
		int hopNumber = 1;
		short idx = 1;
		while (hopNumber <= nHops) {
//			System.out.println("hop#: "+ hopNumber);
			Short key = new Short(idx);
			NetHostLocal hop = hops.get(key);
			if (hop != null) {
				results.add(new QBEHost(trainNum, idx, expID, hop.getHostAddress(), hop.getDelay()));
//				System.out.println("MeasurementCycle getQBEresults results: "+ results.size());
				++hopNumber;
			} /*else {
				results.add(new QBEHost(trainNum, idx, expID, "", -1));
			}*/
			if(hopNumber+1 <0){
				System.out.println("wraparound!"+ idx);
				break;
			}
//			if(idx>0 && idx%1000==0) System.out.println(idx);
			++idx;
		}
//		System.out.println("Final IDX:"+idx);
		return results;
	}
	
	public InetAddress getSourceAddress() {
		return sourceIP;
	}

	public InetAddress getDestAddress() {
		return destIP;
	}
	
	public void setDestReached() {
		++destReached;
	}
	
	public boolean isDestReached() {
		return destReached>1;
	}
	
	public int getNReceivedPackets() {
		return hops.size();
	}
	
	public boolean allReceived() {
		return hops.size() == nSentPackets;
	}
}
