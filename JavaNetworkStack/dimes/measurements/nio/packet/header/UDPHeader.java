/**
 * 
 */
package dimes.measurements.nio.packet.header;

import java.nio.ByteBuffer;
import java.util.Random;

import dimes.measurements.nio.error.DeserializeException;

/**
 * @author Ohad Serfaty
 *
 *    0      7 8     15 16    23 24    31  
 *    +--------+--------+--------+--------+ 
 *    |     Source      |   Destination   | 
 *    |      Port       |      Port       | 
 *    +--------+--------+--------+--------+ 
 *    |                 |                 | 
 *    |     Length      |    Checksum     | 
 *    +--------+--------+--------+--------+ 
 *    |                                     
 *    |          data octets ...            
 *    +----------------------------------            
 *
 */
public class UDPHeader extends Header {

	public static final byte IP_PROTO_UDP	=	17;
	// byte short int long 
	public short sourcePort;
	public short destPort;
	public short udpLength;
	public short checksum;
	
	/**
	 * UDP header constructor
	 */
	public UDPHeader() {
		super(IP_PROTO_UDP);
	}

	/* (non-Javadoc)
	 * @see dimes.measurements.nio.packet.header.Header#getHeaderLength()
	 */
	public int getHeaderLength() {
		return (short) (8);
	}

	/* (non-Javadoc)
	 * @see dimes.measurements.nio.packet.header.Header#deserialize(java.nio.ByteBuffer)
	 */
	public int deserialize(ByteBuffer packetBuffer) throws DeserializeException {
		if( packetBuffer.remaining() < 8)
			throw new DeserializeException("Minimum UDP header size is 8 bytes");
		
//        System.out.println("UDP: deserialize Payload payloadLen"+packetBuffer.remaining());

//		 save the offset
//		int offset = packetBuffer.position();
		this.sourcePort = packetBuffer.getShort();
		this.destPort = packetBuffer.getShort();
		this.udpLength = packetBuffer.getShort();
		this.checksum = packetBuffer.getShort();
		
		// return the payload id :
		return 0;
	}

	/* (non-Javadoc)
	 * @see dimes.measurements.nio.packet.header.Header#serialize(java.nio.ByteBuffer)
	 */
	public void serialize(ByteBuffer packetBuffer) {
		udpLength = (short)(getHeadersTotalLength() );
		packetBuffer.putShort(sourcePort);
		packetBuffer.putShort(destPort);
		packetBuffer.putShort(udpLength );
		packetBuffer.putShort(checksum);
	}

	public static byte[] generateRandomData(int dataSize) {
		byte[] result = new byte[dataSize];
		Random rand = new Random();
		rand.nextBytes(result);
		return result;
	}

	public String toString(){
		return "[UDP Header] SourcePort:" +sourcePort + " , DestPort:" + destPort + " checksum:" + checksum;
	}
	
}
