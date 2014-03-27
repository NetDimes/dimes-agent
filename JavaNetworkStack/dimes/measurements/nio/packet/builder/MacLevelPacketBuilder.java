package dimes.measurements.nio.packet.builder;

import dimes.measurements.nio.MacLevelNetworkStack;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.header.EthernetHeader;
import dimes.measurements.nio.packet.header.Header;

/**
 * @author Ohad Serfaty
 * <br>
 * A Specialized packet builder for Mac level network stack
 * <br>
 */
public class MacLevelPacketBuilder extends PacketBuilder {

	private final EthernetHeader defaultHeader;

	public MacLevelPacketBuilder(MacLevelNetworkStack netStack , EthernetHeader defaultHeader) {
		super(netStack);
		this.defaultHeader = defaultHeader;
	}

	public Packet buildPacket(Header[] headers){
		Header[] newHeaders = headers;
		if (!(headers[0] instanceof EthernetHeader))
			newHeaders = this.constructNewHeaders(headers);
		return new Packet(newHeaders  , this.netStack , this.finalizer); 
	}

	private Header[] constructNewHeaders(Header[] headers) {
		Header[] result = new Header[headers.length + 1];
		result[0] = new EthernetHeader(this.defaultHeader);
		for (int i=0; i<headers.length ; i++)
			result[i+1] = headers[i];
		return result;
	}

}
	