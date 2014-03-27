package dimes.measurements.paristraceroute;

import java.net.InetAddress;

public class NetHost {
	private InetAddress IP;
	private long delay;
	
	public	NetHost(InetAddress ip, long roundDelay) {
		IP = ip;
		delay = roundDelay;
	}
	
	public InetAddress getAddress() {
		return IP;
	}

	public long getDelay() {
		return delay;
	}
}
