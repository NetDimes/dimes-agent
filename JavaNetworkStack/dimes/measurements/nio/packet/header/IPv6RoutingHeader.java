/*
 * Created on 05/05/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package dimes.measurements.nio.packet.header;

import java.net.Inet6Address;
import java.nio.ByteBuffer;

import dimes.measurements.nio.error.DeserializeException;


/**
 * @author Udi Weinsberg
 *
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |  Next Header  |  Hdr Ext Len  | Routing Type=0| Segments Left |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                            Reserved                           |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                                                               |
    +                                                               +
    |                                                               |
    +                           Address[1]                          +
    |                                                               |
    +                                                               +
    |                                                               |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                                                               |
    +                                                               +
    |                                                               |
    +                           Address[2]                          +
    |                                                               |
    +                                                               +
    |                                                               |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    .                               .                               .
    .                               .                               .
    .                               .                               .
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                                                               |
    +                                                               +
    |                                                               |
    +                           Address[n]                          +
    |                                                               |
    +                                                               +
    |                                                               |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

 */
public class IPv6RoutingHeader extends IPv6ExtensionHeader
{
    public static final short IPv6_ROUTING_HEADER	= 43;
    
    /**
     * Construct a IPv6RoutingHeader
     */
    public IPv6RoutingHeader()
    {
        super(IPv6_ROUTING_HEADER);
    }
    
    /**
     * Construct a IPv6RoutingHeader
     */
    public IPv6RoutingHeader( byte routingType, byte segmentsLeft, Inet6Address[] addresses )
    {
        super(IPv6_ROUTING_HEADER);
        this.routingType = routingType;
        this.segmentsLeft = segmentsLeft;
        this.addresses = addresses;
    }
    // protected byte nextHeader;
    protected byte hdrExtLen;
    protected byte routingType = 0;
    protected byte segmentsLeft;
    protected int reserved = 0;
    Inet6Address[] addresses;
    
    /* (non-Javadoc)
     * @see dimes.measurements.packet.header.Header#getHeaderLength()
     */
    public int getHeaderLength()
    {
        return 8+((addresses!=null)?addresses.length*16:0);
    }
    /* (non-Javadoc)
     * @see dimes.measurements.packet.header.Header#serialize(java.nio.ByteBuffer)
     */
    public void serialize(ByteBuffer packetBuffer)
    {
        // place the headers
        hdrExtLen = (byte)( addresses!=null?(addresses.length*2):0 );
        packetBuffer.put( (byte)getNextHeaderId() );
        packetBuffer.put( hdrExtLen );
        packetBuffer.put( routingType );
        packetBuffer.put( segmentsLeft );
        packetBuffer.putInt( reserved );
        // place addresses
        if ( addresses != null )
        {
	        for ( int i=0; i<addresses.length; ++i )
	        {
	            packetBuffer.put( addresses[i].getAddress() );
	        }
        }
    }
    
    /* (non-Javadoc)
     * @see dimes.measurements.packet.header.Header#deserialize(java.nio.ByteBuffer)
     */
    public int deserialize(ByteBuffer packetBuffer) throws DeserializeException
    {
        // read the headers
        int nextHeader = packetBuffer.get();
        hdrExtLen = packetBuffer.get();
        if ( hdrExtLen%2 != 0 )
            throw new DeserializeException("Parameter Problem, hdrExtLen is odd.");
        routingType = packetBuffer.get();
        segmentsLeft = packetBuffer.get();
        reserved = packetBuffer.getInt();
        
        int n = hdrExtLen/2;  // calc number of addresses
        if ( segmentsLeft > n )
            throw new DeserializeException("Parameter Problem, segments left is larger than n.");
        // place addresses
        if ( n > 0 )
        {
            addresses = new Inet6Address[ n ];
            for (int i =0; i<n; ++i )
            {
                try 
                {
                    byte[] addr = new byte[16];
                    addresses[i] = (Inet6Address)Inet6Address.getByAddress( packetBuffer.get( addr ).array() );
                }
                catch ( Exception e )
                {
                    throw new DeserializeException( "Cannot parse IPv6 address" );
                }
            }
        }
        return 0;
    }
    
    /* (non-Javadoc)
     * @see dimes.measurements.packet.matcher.Matchable#match(java.lang.Object)
     */
    public boolean match(Object obj)
    {
        
        return false;
    }
    /**
     * @return Returns the addresses.
     */
    public Inet6Address[] getAddresses()
    {
        return addresses;
    }
    /**
     * @param addresses The addresses to set.
     */
    public void setAddresses(Inet6Address[] addresses)
    {
        this.addresses = addresses;
    }
    /**
     * @return Returns the routingType.
     */
    public byte getRoutingType()
    {
        return routingType;
    }
    /**
     * @param routingType The routingType to set.
     */
    public void setRoutingType(byte routingType)
    {
        this.routingType = routingType;
    }
    /**
     * @return Returns the segmentsLeft.
     */
    public byte getSegmentsLeft()
    {
        return segmentsLeft;
    }
    /**
     * @param segmentsLeft The segmentsLeft to set.
     */
    public void setSegmentsLeft(byte segmentsLeft)
    {
        this.segmentsLeft = segmentsLeft;
    }
    /**
     * @return Returns the hdrExtLen.
     */
    public byte getHdrExtLen()
    {
        return hdrExtLen;
    }
}
