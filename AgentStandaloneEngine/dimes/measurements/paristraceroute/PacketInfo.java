package dimes.measurements.paristraceroute;

public class PacketInfo {
	public	PacketInfo(short packetID, long time) {
		sendTime = time;
		id = packetID;
	}
	
	public short getID() {
		return id;
	}
	
	public long getTime() {
		return sendTime;
	}
	
	private	long sendTime;
	private	short  id;
}
