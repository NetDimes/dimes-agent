/**
 * 
 */
package dimes.measurements.nio.packet.header;

import java.nio.ByteBuffer;

import dimes.measurements.nio.error.DeserializeException;

/**
 * @author Ohad Serfaty
 *
 */
public class InnerIPv4Header extends IPv4Header {

	
	public static short INNER_IP_HEADER = 243; 
	
	/**
	 * 
	 */
	public InnerIPv4Header() {
		super(INNER_IP_HEADER);
	}
	
	public int getHeaderId( Header requestingHeader )
    {
        return INNER_IP_HEADER;
    }
	
	public int deserialize(ByteBuffer packetBuffer) throws DeserializeException{
		super.deserialize(packetBuffer);
		return InnerICMPHeader.INNER_ICMP_HEADER;
	}
	
	public String toString(){
		return "[InnerIPv4]:\n" + super.toString();
	}

}
