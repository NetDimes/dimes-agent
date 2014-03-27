/*
 * Created on 01/05/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package dimes.measurements.nio.packet.header;

import java.nio.ByteBuffer;



/**
 * @author Udi Weinsberg
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ICMPv6Header extends ICMPv4Header
{

    public byte 	type	=	ICMP_ECHO_REQUEST;	// 8 bit
	public short 	code	=	0;					// 8 bit
	public short 	checksum=	0;					// 16 bits
	
	// Echo request
	public short identifier;
	public short sequence;
		
    /**
     * RFC 1885
     * 
     * ICMPv6 error messages:

                 1    Destination Unreachable      (see section 3.1)
                 2    Packet Too Big               (see section 3.2)
                 3    Time Exceeded                (see section 3.3)
                 4    Parameter Problem            (see section 3.4)

            ICMPv6 informational messages:

                 128  Echo Request                 (see section 4.1)
                 129  Echo Reply                   (see section 4.2)
                 130  Group Membership Query       (see section 4.3)
                 131  Group Membership Report      (see section 4.3)
                 132  Group Membership Reduction   (see section 4.3)

     */
	
    public static byte  IPv6_PROTO_ICMP = 0x3A;	// protocol number is 58
   
	public static byte ICMP_ECHO_REQUEST	=	(byte)128;
	public static byte ICMP_ECHO_REPLY		=	(byte)129;

	public static byte ICMP_DEST_UNREACHABLE	= 	1;	
	public static byte ICMP_PACKET_TOO_BIG 	= 	2;
	public static byte ICMP_TIME_EXCEEDED	=	3;
	public static byte ICMP_PARAM_PROBLEM	=	4;

    /**
     * Icmp vv6 header constructor
     */
    public ICMPv6Header()
    {
        super( (short)IPv6_PROTO_ICMP );
    }
    
   
   
    /**
	 * Checksum calculation:
	 * 
	 *     * 2.3 Message Checksum Calculation

   The checksum is the 16-bit one's complement of the one's complement
   sum of the entire ICMPv6 message starting with the ICMPv6 message
   type field, prepended with a "pseudo-header" of IPv6 header fields,
   as specified in [IPv6, section 8.1].  The Next Header value used in
   the pseudo-header is 58.  (NOTE: the inclusion of a pseudo-header in
   the ICMPv6 checksum is a change from IPv4; see [IPv6] for the
   rationale for this change.)

   For computing the checksum, the checksum field is set to zero.


	 * unsigned short in_cksum(unsigned short *data,int size){
        unsigned long sum = 0;

        while(size > 1){
                sum += *(data++);
                size -= 2;
        }

        if(size > 0) sum += (*data) & 0xff00;
        sum = (sum & 0xffff) + (sum >> 16);

        return ((~(unsigned short)((sum >> 16) + (sum & 0xffff))));
        }
	 */
    public short calculateChecksum( ByteBuffer packetBuffer )
    {
        try 
        {
	        IPv6Header ipHeader = (IPv6Header)packet.getHeaderById( IPv6Header.IPv6_PROTO_IP );
	         // create a pseudo header
	        int nextLevelLength = getHeadersTotalLength();
	        int psuedoHeaderLength = ipHeader.getPseudoHeaderLength( nextLevelLength );
	        // reset position
	        int offset = packetBuffer.position();
	        packetBuffer.position(  offset - psuedoHeaderLength );
	        ipHeader.createPseudoHeader( packetBuffer, (short)nextLevelLength, IPv6_PROTO_ICMP );
	             
	        // reset position
	        packetBuffer.position( offset - psuedoHeaderLength );
	        
	        // do checksum calcualtion
	        int checksum = 0;
	        int length = psuedoHeaderLength+nextLevelLength;
	        short wordValue = 0;
	        while ( length > 1 )
	        {
	            wordValue = packetBuffer.getShort();
	            
	            /*
	            wordValue = (short)(data[i++]&0xFF);
	            wordValue |= (short)((data[i++]<<8)&0xFF00);
	            */
	            
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
    
    /**
     * Parse the Header to a string representation.
     * 
     * @return the string representation of the header
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer().append( "[ICMPv6] Type:").append( Header.byteToShort(type));
        sb.append( ", Code:" ).append( Header.shortToInt(code) );
        sb.append( ", id:" ).append( Header.shortToInt(identifier) );
        sb.append( ", seq:" ).append( Header.shortToInt(sequence) );
        return sb.toString();
    }
}
