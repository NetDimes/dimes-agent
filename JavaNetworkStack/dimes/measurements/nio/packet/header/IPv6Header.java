/*
 * Created on 01/05/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package dimes.measurements.nio.packet.header;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import dimes.measurements.nio.error.DeserializeException;

/**
 * @author Udi Weinsberg
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IPv6Header extends Header
{
    public static final short ETHERTYPE_IPV6 = (short) 0x86DD;
    
    protected byte version=6;	 			// 4 bit
	public byte trafficClass=0x00;		// 8 bit
	public int flowLabel = 0x00;		// 20 bit
	public short payloadLength = 0x00;	// 16 bit
	//public byte nextHeader = 99;		// 8 bit
	public byte hopLimit = (byte)128;	// 8 bit
	public InetAddress sourceIP;		// 128 bit
	public InetAddress destIP;			// 128 bit
    
	public static byte	IPv6_PROTO_IP	= 41;
	
    /**
     * IP v6 header constructor
     */
    public IPv6Header()
    {
        super(IPv6_PROTO_IP);
    }
    
    /**
     * Get the header id.
     * 
     * @return header ID
     */
    public int getHeaderId( Header requestingHeader )
    {
        if (  requestingHeader instanceof EthernetHeader )
            return ETHERTYPE_IPV6;
        else
            return IPv6_PROTO_IP;
    }


    public int getHeaderLength()
    {
        return 40;
    }
    
    
    /**
     * Returns the pseudo header length.
     * 
     * @param upperLayerLength
     * @return pseudo header length
     */
    public int getPseudoHeaderLength( int upperLayerLength )
    {
        return 36;
    }
    
    /**
     * Creates a pseudo header, into a target buffer
     *   
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                                                               |
   +                                                               +
   |                                                               |
   +                         Source Address                        +
   |                                                               |
   +                                                               +
   |                                                               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                                                               |
   +                                                               +
   |                                                               |
   +                      Destination Address                      +
   |                                                               |
   +                                                               +
   |                                                               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                   Upper-Layer Packet Length                   |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                      zero                     |  Next Header  |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */
    public void createPseudoHeader( ByteBuffer packetBuffer, short upperLayerLength, byte nextHeader )
    {
        byte[] srcIp = sourceIP.getAddress();
	    byte[] destIp = destIP.getAddress();
	  
	    // copy src and dest ip into target
	    packetBuffer.put( srcIp );
	    packetBuffer.put( destIp );
	    
	    // place the upper layer length (2 bytes, network order)
	    packetBuffer.putShort( upperLayerLength );
	    	    	    
	    // place zero and next header
	    packetBuffer.put( (byte)0x00 );
	    packetBuffer.put( nextHeader );
	    
    }
    
    /* (non-Javadoc)
     * @see dimes.measurements.packet.Header#serialize()
     */
    public void serialize( ByteBuffer packetBuffer )
    {
        // calculate payloadLength
        payloadLength = (nextHeader!=null)?(short)nextHeader.getHeadersTotalLength():0;
        // build the packet, network order
	    // start with the basic header
	    
        packetBuffer.put( (byte)((version<<4)&0xf0|((trafficClass>>4)&0xf)) );
        packetBuffer.put( (byte)((((trafficClass&0xf)<<4)&0xf0)|((flowLabel>>16)&0xf)) );
        packetBuffer.putShort( (short)(flowLabel&0xFFFF) );
        packetBuffer.putShort( (short)payloadLength );
        packetBuffer.put( (byte)(getNextHeaderId()&0xff) );
        packetBuffer.put( hopLimit );
	    	    
	    byte[] srcIp = sourceIP.getAddress();
	    byte[] destIp = destIP.getAddress();
	  
	    packetBuffer.put( srcIp );
	    packetBuffer.put( destIp ); 
	}

    /* (non-Javadoc)
     * @see dimes.measurements.packet.header.Header#deserialize(java.nio.ByteBuffer)
     */
    public int deserialize(ByteBuffer packetBuffer) throws DeserializeException
    {
        // read the packet
        byte b = packetBuffer.get();
        version = (byte)((b>>4)&0xf);
        trafficClass = (byte)(((b&0xf)<<4)&0xf0);
        b = packetBuffer.get();
        trafficClass |= (byte)(((b&0xf0)>>4)&0xf);
        flowLabel = (byte)(((b&0xf)<<16)&0xf0);
        short s = packetBuffer.getShort();
        flowLabel |= s;
        payloadLength = packetBuffer.getShort();
        int nextHeader = (int)packetBuffer.get();
        hopLimit = packetBuffer.get();

	    byte[] srcIp = new byte[16];
	    byte[] destIp = new byte[16];
	  
	    packetBuffer.get( srcIp );
	    packetBuffer.get( destIp );
	    
	    try
	    {
	        sourceIP = Inet6Address.getByAddress( srcIp );
	        destIP = Inet6Address.getByAddress( destIp );
	    }
	    catch ( Exception e )
	    {
	        throw new DeserializeException("Bad IPv6 addresses in packet.");
	    }
        return 0;
    }

    /* (non-Javadoc)
     * @see dimes.measurements.packet.matcher.Matchable#match(java.lang.Object)
     */
    public boolean match(Object obj)
    {
        boolean packetsMatch = false;
        try
        {
            IPv6Header otherHeader = (IPv6Header)obj;

            // Check that addresses match and types match
            packetsMatch = ( sourceIP.equals( otherHeader.destIP ) &&
                    		 destIP.equals( otherHeader.sourceIP ) );
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
        StringBuffer sb = new StringBuffer().append( "[IPv6]" );
        sb.append( ", src:").append( sourceIP );
        sb.append( ", dest:").append( destIP );
        sb.append( ", class:").append(trafficClass);
        sb.append( ", flowLabel:" ).append( flowLabel );
        sb.append( ", payloadLen:" ).append( payloadLength );
        sb.append( ", hopLimit:" ).append( hopLimit );
        return sb.toString();
    }
}
