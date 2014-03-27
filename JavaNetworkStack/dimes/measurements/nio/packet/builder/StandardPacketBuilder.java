/**
 * 
 */
package dimes.measurements.nio.packet.builder;

import dimes.measurements.nio.NetworkStack;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.header.Header;

/**
 * @author Ohad Serfaty
 * <br>
 * A Standard packet builder 
 */
public class StandardPacketBuilder extends PacketBuilder {

	/**
	 * @param netStack
	 */
	public StandardPacketBuilder(NetworkStack netStack) {
		super(netStack);
	}

	public Packet buildPacket(Header[] headers){
		return new Packet(headers , this.netStack , this.finalizer); 
	}

}
