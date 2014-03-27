/*
 * Created on 06/05/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package dimes.measurements.nio.packet.header;

import java.util.Hashtable;

/**
 * @author Udi Weinsberg
 *
 */
public class HeadersRepository
{
    protected Hashtable protocols;
    protected static HeadersRepository theInstance = new HeadersRepository();
    
    public static HeadersRepository getInstace()
    {
        return theInstance;
    }
    /**
     * 
     */
    private HeadersRepository()
    {
        protocols = new Hashtable();
        buildIPStack();
    }
    
    /**
     * Register a protocol.
     * 
     * @param protocolId
     * @param header
     */
    public void registerProtocol( int protocolId, Header header )
    {
        protocols.put( new Integer(protocolId), header );
    }

    /**
     * Returns a new object of the header of a registered protocol.
     * 
     * @param protocolId
     */
    public Header createHeader( int protocolId )
    {
        Header header = null;
        try 
        {
	        header = (Header)(protocols.get( new Integer(protocolId) ));
	        if ( header != null )
	        {
	            header = (Header)header.getClass().newInstance();
	        }
        } 
        catch ( Exception e )
        {
        }
        return header;
    }

    /**
     * Builds the IPv4 Stack.
     */
    private void buildIPStack()
    {
        registerProtocol( 0, new EthernetHeader() );
        registerProtocol( IPv4Header.ETHERTYPE_IPV4, new IPv4Header() );
        registerProtocol( ICMPv4Header.IPv4_PROTO_ICMP, new ICMPv4Header() );
        registerProtocol( IPv6Header.ETHERTYPE_IPV6, new IPv6Header() );
        registerProtocol( ICMPv6Header.IPv6_PROTO_ICMP, new ICMPv6Header() );
        registerProtocol( UDPHeader.IP_PROTO_UDP, new UDPHeader() );
        registerProtocol( TCPHeader.IP_PROTOCOL_TCP, new TCPHeader() );
        registerProtocol( InnerICMPHeader.INNER_ICMP_HEADER, new InnerICMPHeader() );
        registerProtocol( InnerIPv4Header.INNER_IP_HEADER, new InnerIPv4Header() );
    }
}
