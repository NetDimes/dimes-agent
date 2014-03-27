/*
 * Created on 01/05/2006
 *
 */
package dimes.measurements.nio.packet.header;

import java.nio.ByteBuffer;

import dimes.measurements.nio.error.DeserializeException;



/**
 * @author Udi Weinsberg
 * 
 * The ICMPv4 Header looks as follows: <br> 
 *
 *   0                   1                   2                   3 <br>
 *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 <br>
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ <br> 
 *  |     Type      |     Code      |          Checksum             | <br>
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+<br>
 *  |           Identifier          |        Sequence Number        |<br>
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+<br>
 *  |     Data ...<br>
 *  +-+-+-+-+-<br>
 *<br>
 *
 */
public class ICMPv4Header extends Header
{

    public byte 	type	=	ICMP_ECHO_REQUEST;	// 8 bit
	public short 	code	=	0;				// 8 bit
	public short 	checksum=	0;				// 16 bits
	
	// Echo request
	public short identifier;
	public short sequence;
		
    public static final byte  IPv4_PROTO_ICMP = 0x01;	// protocol number is 1
   
	public static final byte ICMP_ECHO_REQUEST		=	(byte)8;
	public static final byte ICMP_ECHO_REPLY		=	(byte)0;

	public static final byte ICMP_DEST_UNREACHABLE	= 	3;	
	public static final byte ICMP_PACKET_TOO_BIG 		= 	3; //??
	public static final byte ICMP_TIME_EXCEEDED		=	11;
	public static final byte ICMP_PARAM_PROBLEM		=	12;

    /**
     * Icmp v4 header constructor 
     */
    public ICMPv4Header()
    {
        super( (short)IPv4_PROTO_ICMP );
    }
    
    /**
     * @param headerId
     */
    public ICMPv4Header( short headerId )
    {
        super( headerId );
    }
    
    /**
     * Set an echo request
     */
    public void setEchoRequest( int identifier, int sequence )
    {
        this.type = ICMP_ECHO_REQUEST;
        this.code = 0;
        this.checksum = 0;
        this.identifier = (short)identifier;
        this.sequence = (short)sequence;
    }
    
    public int getHeaderLength()
    {
        if ( type == ICMP_ECHO_REQUEST || type == ICMP_ECHO_REPLY)
            return 8;
        
        return 0;
    }
    
   
    /**
	 * Checksum calculation:
	 * 
	  The checksum is the 16-bit ones's complement of the one's
      complement sum of the ICMP message starting with the ICMP Type.
      For computing the checksum , the checksum field should be zero.
      If the total length is odd, the received data is padded with one
      octet of zeros for computing the checksum.  This checksum may be
      replaced in the future.

	 * @param packetBuffer the buffer to work on
	 */
    public short calculateChecksum( ByteBuffer packetBuffer )
    {
        try 
        {
	       int nextLevelLength = getHeadersTotalLength();
	        
	        // do checksum calcualtion
	        int checksum = 0;
	        int length = nextLevelLength;
	        short wordValue = 0;
	        while ( length > 1 )
	        {
	            wordValue = packetBuffer.getShort();
	                  
	            checksum += (wordValue&0xFFFF);
	            length -= 2;
	        }
	        
	        // last byte
	        if ( length > 0 )
	        {
	            // take the single byte as big-endien
	            checksum += (short)((packetBuffer.get()<<8)&0xff00);
	        }
	        
	        // do the one's complement
	        checksum = (int)((checksum & 0xffff) + ((checksum >> 16) & 0xffff));
	        checksum += (checksum>>16);
	                
	        return (short)((~checksum)&0xFFFF);
	        
        } 
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    /* (non-Javadoc)
     * @see dimes.measurements.packet.Header#serialize()
     */
    public void serialize( ByteBuffer packetBuffer )
    {
        int offset = packetBuffer.position();
        // build the packet, network order
	    // start with the basic header
	    checksum = 0;
	    packetBuffer.put( (byte)type );
	    packetBuffer.put( (byte)(code) );
	    packetBuffer.putShort( checksum );
	    
	    // if echo request
	    if ( type == ICMP_ECHO_REQUEST || type == ICMP_ECHO_REPLY )
	    {
	        packetBuffer.putShort( identifier );
	        packetBuffer.putShort( sequence );
	    }
	    
	    // calc the checksum, and place the calculated bytes
	    packetBuffer.position( offset );
	    checksum = calculateChecksum( packetBuffer );
	    packetBuffer.position( offset + 2 );
	    packetBuffer.putShort( checksum );
	}
   

    /* (non-Javadoc)
     * @see dimes.measurements.packet.header.Header#deserialize(java.nio.ByteBuffer)
     */
    public int deserialize(ByteBuffer packetBuffer) throws DeserializeException
    {

        // build the packet, network order
	    // start with the basic header
	    type = packetBuffer.get();
	    code = packetBuffer.get();
	    checksum = packetBuffer.getShort();
	    
	    // if echo request
	    if ( type == ICMP_ECHO_REQUEST || type == ICMP_ECHO_REPLY )
	    {
	        identifier = packetBuffer.getShort();
	        sequence = packetBuffer.getShort();
	    }
	    
	    // Galina 17.07.09 - port unreachable also has INNER_IP_HEADER
	    if (type == ICMP_TIME_EXCEEDED || type == ICMP_DEST_UNREACHABLE) {
	    	identifier = packetBuffer.getShort();
	        sequence = packetBuffer.getShort();
	    	return InnerIPv4Header.INNER_IP_HEADER;
	    }
	    
        return 0; // TODO - no next protocol? how to mark this?
    }
   
    /* (non-Javadoc)
     * @see dimes.measurements.packet.Header#touchup(byte[])
    */
   public byte[] touchup( byte[] packet )
   {
       // calc the checksum of the packet
       return packet;
   }
   
    /* (non-Javadoc)
     * @see dimes.measurements.packet.matcher.Matchable#match(java.lang.Object)
     */
    public boolean match(Object obj)
    {
        boolean packetsMatch = false;
        try
        {
            ICMPv4Header otherHeader = (ICMPv4Header)obj;

            packetsMatch = ((type == ICMP_ECHO_REQUEST && otherHeader.type == ICMP_ECHO_REPLY ) ||
            			    (type == ICMP_ECHO_REPLY && otherHeader.type == ICMP_ECHO_REQUEST )) ||
            			   ((type == ICMP_ECHO_REQUEST && otherHeader.type == ICMP_PACKET_TOO_BIG ) ||
            			    (type == ICMP_PACKET_TOO_BIG && otherHeader.type == ICMP_ECHO_REQUEST )) ||
            			   ((type == ICMP_ECHO_REQUEST && otherHeader.type == ICMP_TIME_EXCEEDED ) ||
            			    (type == ICMP_TIME_EXCEEDED && otherHeader.type == ICMP_ECHO_REQUEST ));
            
            // check that id and seq match
            if ( packetsMatch && otherHeader.type == ICMP_ECHO_REPLY )
            {
                packetsMatch = ( sequence == otherHeader.sequence )&& 
                			   ( identifier == otherHeader.identifier );	
            }
        }
        catch ( Exception e )
        {    
        }
        
        return packetsMatch;
    }
    
    
    /**
     * Parse the Header to a string representation.
     * 
     * @return the string representation of the header
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer().append( "[ICMPv4]");
        sb.append( ", type:" ).append( Header.byteToShort(type) );
        sb.append( ", Code:" ).append( Header.shortToInt(code) );
        sb.append( ", id:" ).append( Header.shortToInt(identifier) );
        sb.append( ", seq:" ).append( Header.shortToInt(sequence) );
        return sb.toString();
    }

}
