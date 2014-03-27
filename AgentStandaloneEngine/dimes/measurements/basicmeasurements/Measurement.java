package dimes.measurements.basicmeasurements;

import dimes.measurements.nio.packet.header.Payload;
import dimes.measurements.results.PingTracerouteResults;



public class Measurement {
	private static final short maxRouteLength = 50;
	
	private int ttlMin;
	private int ttlMax;
	private int tos;
	private String destIP;
	private long timeOut;
	private int protocol;
	private Payload payload;
	private short srcPort;
	private short dstPort;
	
	private PingTracerouteResults results;
	
	public	Measurement(String destIP, int protocol, short srcPort, short dstPort, int ttl, int tos, long timeOut) {
		this.destIP = destIP;
		this.protocol = protocol;
		if (ttl == -1) {
			// traceroute
			ttlMin = 1;
			ttlMax = maxRouteLength;
		} else {
			ttlMin = ttlMax = ttl;
		}
		this.tos = tos;
		this.srcPort = srcPort;
		this.dstPort = dstPort;
		payload = null;
		results = null;
	}
	
	public int getFirstTTL() {
		return ttlMin;
	}
	
	public int getLastTTL() {
		return ttlMax;
	}

	public int getTos() {
		return tos;
	}
	
	public String getDestAddress() {
		return destIP;
	}
	
	public long getTimeToWait() {
		return timeOut;
	}
	
	public int getProtocol() {
		return protocol;
	}
	
	public int getNPackets() {
		return ttlMax-ttlMin + 1;
	}
	
	public void setPayload(Payload p) {
		payload = p;
	}
	
	public Payload getPayload() {
		return payload;
	}
	
	public void setResults(PingTracerouteResults res) {
		results = res;
	}
	
	public PingTracerouteResults getResults() {
		return results;
	}
	
	public short getSourcePort() {
		return srcPort;
	}
	
	public short getDestPort() {
		return dstPort;
	}
}
