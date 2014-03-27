package dimes.measurements.paristraceroute;

import java.net.InetAddress;

import dimes.measurements.nio.packet.header.ICMPv4Header;

public class NetHostLocal {
	private InetAddress IP;
	private long delay;
	private int  ttl;
	private int  replyType;
	private int  errorCode;
	
	public	NetHostLocal(int ttl, InetAddress ip, long roundDelay, int replyType, int errorCode) {
		IP = ip;
		delay = roundDelay;
		this.ttl = ttl;
		this.replyType = replyType;
		this.errorCode = errorCode;
	}
	
	public InetAddress getAddress() {
		return IP;
	}

	public long getDelay() {
		return delay;
	}
	
	public int getDelayInt() {
		return (int) delay/1000;
	}
	
	public int getTTL() {
		return ttl;
	}
	
	public String getHostAddress() {
		return IP == null ? "unknown" : IP.getHostAddress();
	}
	
	public String getHostName() {
		return IP == null ? "unknown" : IP.getHostName();
	}
	
	public int getReplyType() {
		return replyType;
	}
	
	public String getReplyTypeString() {
		return replyType == ICMPv4Header.ICMP_TIME_EXCEEDED ? 
					"TIME EXCEEDED" : replyType == ICMPv4Header.ICMP_ECHO_REPLY ? "ECHO REPLY" : "DEST UNREACHABLE"; 
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}
