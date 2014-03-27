/*
 * Created on 04/05/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package dimes.measurements.nio.packet.header;

import java.nio.ByteBuffer;

import dimes.measurements.nio.error.DeserializeException;
import dimes.measurements.nio.packet.util.ByteOutputFormatter;

/**
 * @author Udi Weinsberg
 * <br>
 * Most of the source here was adapted from com.sun.snoop <br> 
 * Construct the ethernet header. <br>
 *   <br>
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+<br>
 |       Ethernet destination address (first 32 bits)            |<br>
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+<br>
 | Ethernet dest (last 16 bits)  |Ethernet source (first 16 bits)|<br>
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+<br>
 |       Ethernet source address (last 32 bits)                  |<br>
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+<br>
 |        Type code              |                               |<br>
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+<br>
 |  IP header, then TCP header, then your data                   |<br>
 |                                                               |<br>
     ...<br>
 |                                                               |<br>
 |   end of your data                                            |<br>
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+<br>
 |                       Ethernet Checksum                       |<br>
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+<br>
 */
public class EthernetHeader extends Header
{

    //  The MAC address of the system for which this Ethernet packet is destined.
    byte[] destinationMAC;

    // The MAC address of the system from which this Ethernet packet came.
    byte[] sourceMAC;

	byte[] PPPoEHeader=null;


    public EthernetHeader()
    {
        super((short)0); // ethernet does not have an id
        destinationMAC = null;
        sourceMAC = null;
    }
    
    /**
     * Creates an Ethernet header data structure with the provided information.
     *
     * @param  destinationMAC  The MAC address of the system for which this
     *                         Ethernet packet is destined.
     * @param  sourceMAC       The MAC address of the system from which this
     *                         Ethernet packet was sent.
     */
    public EthernetHeader(byte[] sourceMAC, byte[] destinationMAC,byte[] PPPoEHeader)
    {
      super((short)0); // ethernet does not have an id
      this.destinationMAC = destinationMAC;
      this.sourceMAC      = sourceMAC;
      this.PPPoEHeader = PPPoEHeader;
    }

    public EthernetHeader(byte[] sourceMac, byte[] destMac) {
    	this(sourceMac , destMac,null);
	}

	public EthernetHeader(EthernetHeader header) {
		this(header.sourceMAC , header.destinationMAC , header.PPPoEHeader);
	}

	/**
     * Retrieves the MAC address of the system to which this Ethernet packet was
     * sent.
     *
     * @return  The MAC address of the system to which this Ethernet packet was
     *          sent.
     */
    public byte[] getDestinationMAC()
    {
      return destinationMAC;
    }



    /**
     * Retrieves the MAC address of the system that sent this Ethernet packet.
     *
     * @return  The MAC address of the system that sent this Ethernet packet.
     */
    public byte[] getSourceMAC()
    {
      return sourceMAC;
    }

    /**
     * Retrieves the number of bytes contained in this Ethernet header.
     *
     * @return  The number of bytes contained in this Ethernet header.
     */
    public int getHeaderLength()
    {
      // There will always be 14 bytes in an Ethernet header.
      return 14;
    }

    /* (non-Javadoc)
     * @see dimes.measurements.packet.header.Header#serialize(byte[], int)
     */
    public void serialize( ByteBuffer packetBuffer )
    {
        short nextHeaderId = (short)getNextHeaderId();
        
        packetBuffer.put( destinationMAC );
        packetBuffer.put( sourceMAC );
        
        if (this.PPPoEHeader == null || PPPoEHeader.length == 0 )
        {
        	packetBuffer.putShort( nextHeaderId );
        }
        else
        	packetBuffer.put(this.PPPoEHeader);
        
    }

    /* (non-Javadoc)
     * @see dimes.measurements.nio.packet.header.Header#deserialize(java.nio.ByteBuffer)
     */
    public int deserialize( ByteBuffer packet ) 
    	throws DeserializeException
    {
      if ( packet.remaining() < 14 )
      {
        throw new DeserializeException("Insufficient data available for an Ethernet packet header");
      }
      
      // get the header bytes
      destinationMAC = new byte[6];
      sourceMAC      = new byte[6];
      packet.get( destinationMAC );
      packet.get( sourceMAC );
      short ethertype = packet.getShort(); //(packetBytes[offset+12] << 8) | (packetBytes[offset+13]); 
  
      return (int)ethertype;
    }

    /* (non-Javadoc)
     * @see dimes.measurements.packet.matcher.Matchable#match(java.lang.Object)
     */
    public boolean match(Object obj)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public String toString(){
    	StringBuffer sb = new StringBuffer().append( "[EthernetHeader] , " );
    	sb.append("src:");
    	sb.append(ByteOutputFormatter.toHexString(this.sourceMAC));
    	sb.append(" , dest:");
    	sb.append(ByteOutputFormatter.toHexString(this.destinationMAC));
    	sb.append(" , Type:");
    	sb.append(ByteOutputFormatter.toHexString((short)getNextHeaderId()));
    	
    	
    	
    	return sb.toString();
    }

    
}
