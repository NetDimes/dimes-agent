package dimes.measurements.nio.packet.builder;

import dimes.measurements.nio.NetworkStack;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.PacketFinalizer;
import dimes.measurements.nio.packet.header.Header;

/**
 * @author Ohad Serfaty
 * <br>
 * A general packet builder
 * <br>
 */
public abstract class PacketBuilder {

	protected final NetworkStack netStack;
	protected PacketFinalizer finalizer;

	public PacketBuilder(NetworkStack netStack){
		this.netStack = netStack;
	}
	
	/**
	 * build a packet as a concatenation of headers.
	 * 
	 * @param headers an array of ordered headers
	 * 
	 * @return a serialized Packet Object.
	 */
	public abstract Packet buildPacket(Header[] headers);

	public void setPacketFinalizer(PacketFinalizer finalizer) {
		this.finalizer = finalizer;
	}
	
}
