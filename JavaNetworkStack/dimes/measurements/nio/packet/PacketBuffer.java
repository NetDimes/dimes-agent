package dimes.measurements.nio.packet;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import dimes.measurements.nio.NetworkStack;
import dimes.measurements.nio.packet.header.Header;
import dimes.measurements.nio.packet.header.ICMPv4Header;
import dimes.measurements.nio.packet.header.IPv4Header;
import dimes.measurements.nio.packet.header.Payload;

/**
 * @author Ohad Serfaty
 *
 */
public class PacketBuffer extends Vector {
	
	private final NetworkStack netStack;

	public PacketBuffer(NetworkStack netStack){
		this.netStack = netStack;
	}
	
	private static final long serialVersionUID = 1L;

	public Packet[] getOrderedPackets(){	
		Packet[] result = new Packet[this.size()];
		int counter=0;
		for (Iterator i=this.iterator(); i.hasNext();)
		{
			Packet packet = (Packet) i.next();
			result[counter++] = packet;
		}
		return result;
	}

	public void addPacket(long sendTime, Packet packet) {
		packet.microSecTimestamp = sendTime;
		this.add(packet);
	}

	public void setTrainID(short trainID) {
		for (Iterator i=this.iterator(); i.hasNext();)
		{
			Packet packet = (Packet) i.next();
			Payload payload = (Payload) packet.getHeaderById(Payload.id);
			payload.setTrainId(trainID);
		}
	}
	
	public boolean addIdentityOffset(short offset) {
		boolean status = true;
		for (Iterator i=this.iterator(); i.hasNext();)
		{
			Packet packet = (Packet) i.next();
			IPv4Header ipHeader = (IPv4Header) packet.getHeaderById(IPv4Header.IPv4_PROTO_IP);
			ipHeader.identity += offset;
			if (ipHeader.identity < 0) {
				status = false;
			}
			ICMPv4Header icmpHeader = (ICMPv4Header) packet.getHeaderById(ICMPv4Header.IPv4_PROTO_ICMP);
			if (icmpHeader != null) {
				icmpHeader.sequence += offset;
				if (icmpHeader.sequence < 0) {
					status = false;
				}
			}
		}
		return status;
	}
	
	public short getTrainID() {
		for (Iterator i=this.iterator(); i.hasNext();)
		{
			Packet packet = (Packet) i.next();
			Payload payload = (Payload) packet.getHeaderById(Payload.id);
			return payload.getTrainId();
		}
		return 0;
	}
	
	/**
	 * Add a packet to the buffer
	 * 
	 * @param packetBytes
	 * @param packetSize
	 * @param sendTimeMilisec
	 * @param sendTimeMicrosec
	 */
	public void addPacket(byte[] packetBytes , int packetSize , long sendTimeMilisec , long sendTimeMicrosec){
		try 
		{
			Packet packet = new Packet(netStack);
			packet.fromByteArray(packetBytes);
			packet.microSecTimestamp = sendTimeMicrosec;
			packet.nanoSecTimestamp = sendTimeMicrosec;
			this.add(packet);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
