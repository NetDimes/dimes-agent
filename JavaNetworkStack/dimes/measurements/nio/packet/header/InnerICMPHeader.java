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
public class InnerICMPHeader extends ICMPv4Header {

	public static final short INNER_ICMP_HEADER = 242;
	
	/**
	 * 
	 */
	public InnerICMPHeader() {
		super(INNER_ICMP_HEADER);
	}
	
	public int getHeaderId( Header requestingHeader )
    {
        return INNER_ICMP_HEADER;
    }
	
	public int deserialize(ByteBuffer packetBuffer) throws DeserializeException{
		super.deserialize(packetBuffer);
		return 0;
	}
	
	public String toString(){
		return "[InnerIcmpV4]:\n" + super.toString();
	}
	

}
