/*
 * Created on 04/05/2006
 *
 *
 * This class defines a data structure for storing information about an Internet
 * Protocol header as defined in RFC 791.
 *
 * @author Udi Weinsberg
 * @author Neil A. Wilson
 */
package dimes.measurements.nio.packet.header;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dimes.measurements.nio.error.DeserializeException;

/**
 * @author Udi Weinsberg
 *
 *  0                   1                   2                   3   
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |Version|  IHL  |Type of Service|          Total Length         |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |         Identification        |Flags|      Fragment Offset    |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |  Time to Live |    Protocol   |         Header Checksum       |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                       Source Address                          |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                    Destination Address                        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                    Options                    |    Padding    |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   
 */
public class IPv4Header extends Header
{
    /**
     * The ethertype used in the Ethernet header to indicate that the packet
     * contains IPv4 data.
     */
    public static final short ETHERTYPE_IPV4 = 0x0800;
      
    public static byte	IPv4_PROTO_IP	= 42; // TODO - does IPv4 has protocol id?
    
    /**
	 * The IP version
	 */
	private byte	version = 4;	//  4-bit value

	/**
	 * The length of the IP header in 32-bit words
	 */
	private byte	hdrlen;	//  4-bit value

	/**
	 * the Type-Of-Service defined for the IP datagram
	 */
	public byte	tos;		//  8-bit type of service

	/**
	 * The total length of the IP datagram and it's payload
	 */
	private short	length;	// 16-bit total length of IP Packet

	/**
	 * The identity of the IP dagagram.
	 */
	public short	identity;	// 16-bit identification

	/**
	 * The fragmentation flags tha occupy the upper 3 bits
	 * of the fragment offset field
	 */
	public byte	flags;	//  3-bit flags

	/**
	 * The fragment offset of this packet
	 */
	public short	fragOffset;	// 13-bit fragment offset

	/**
	 * The Time-To-Live for this IP packet
	 */
	public byte	ttl;		//  8-bit time-to-live

	/**
	 * The protocol encapuslated by this packet
	 */
	//private byte	protocol;	//  8-bit protocol

	/**
	 * One's compliment 16-bit checksum of the header only.
	 * This does not include the value for the data
	 */
	private short	checksum;	// 16-bit one's compliment checksum

	/**
	 * Source address of the IP datagram
	 */
	public InetAddress	sourceIP;	// 32-bit source address

	/**
	 * Destination address of the IP datagram
	 */
	public InetAddress	destIP;	// 32-bit destination address
	
	/**
	 * any option data in the datagram
	 */
	public byte[]	options;	// maximum of 40-bytes
 
    	
	/**
	 * Constructs a basic IP header, but the header
	 * <EM>is not</EM> valid until a large part of
	 * the information is configured. 
	 */
	protected IPv4Header(short aHeaderId)
	{
	    super(  aHeaderId );
		hdrlen	= 5;
		version	= IPv4_VERSION;
		tos		= 0;
		length	= 20;
		identity	= 0;
		flags		= 0;
		fragOffset	= 0;
		ttl		= (byte)128;
		checksum	= 0;
		sourceIP	= null;
		destIP	= null;
		options	= new byte[0];
	}
	
	public IPv4Header(){
		this(ETHERTYPE_IPV4);
	}
	
	/**
     * Get the header id.
     * 
     * @return header id 
     */
    public int getHeaderId( Header requestingHeader )
    {
        if (  requestingHeader instanceof EthernetHeader )
        {
            return ETHERTYPE_IPV4;
        }
        else
        {
            return IPv4_PROTO_IP;
        }
    }

	
	/* (non-Javadoc)
     * @see dimes.measurements.packet.header.Header#deserialize(java.nio.ByteBuffer)
     */
    public int deserialize( ByteBuffer packetBuffer )
    	throws DeserializeException
    {

        int nextHeader;
        
		if( packetBuffer.remaining() < 20)
			throw new DeserializeException("Minimum IP header size is 20 bytes");

		// save the offset
		int offset = packetBuffer.position();
 //       System.out.println("IPV4: deserialize Payload payloadLen"+packetBuffer.remaining());

		//
		// Get the header version and header length. The
		// header version lives in the first 4 bits of the
		// header. The header length lives in the next 4 bits.
		// NOTE: The header length is the number of 32-bit
		// words in the header, so the true length is hdrlen * 4.
		//
		byte b = packetBuffer.get();
		version = (byte)(b >>> 4);
		hdrlen  = (byte)(b & 0xf);
		
		//
		// check the version number
		//
		if(version != 4)
			throw new DeserializeException("Unknown IP Version, version = " + version);

		//
		// check to make sure there is enough data now that
		// we know the total length of the header
		//
		if( packetBuffer.remaining() < (hdrlen * 4))
			throw new DeserializeException("Insufficient data: buffer size = "
							   + (length - packetBuffer.position()) 
							   + " and header length = "
							   + (hdrlen * 4));

		//
		// Now get the Type Of Service flags (8-bits)
		//
		tos	= packetBuffer.get();
			
		//
		// Convert the 16-bit total length of the packet
		// in bytes.
		//
		length=packetBuffer.getShort(); 

		//
		// Next get the 16-bit identification field
		//
		identity= packetBuffer.getShort();

		//
		// Get the next 16-bits of information. The upper 3-bits
		// are the header flags. The lower 13-bits is the 
		// fragment offset!
		//
		fragOffset	= packetBuffer.getShort();
		flags		= (byte)(fragOffset >>> 13);		// get the upper 3-bits
		fragOffset	= (short)(fragOffset & 0x1fff);	// mask off the upper 3-bits
		
		//
		// The 8-bit Time To Live (TTL) is next
		//
		ttl	= packetBuffer.get();
		
		//
		// The 8-bit protocol is next. This is used by the 
		// OS to determine if the packet is TCP, UDP, etc al.
		//
		nextHeader = packetBuffer.get();
		
		//
		// Now get the 16-bit one's compliment checksum
		//
		checksum = packetBuffer.getShort();
		
		//
		// The 32-bit IPv4 source address is next. This is the 
		// address of the sender of the packet.
		//
		try 
		{
			byte[] addr = new byte[4];
			packetBuffer.get( addr );
			sourceIP = (Inet4Address)Inet4Address.getByAddress( addr );
			packetBuffer.get( addr );
			destIP = (Inet4Address)Inet4Address.getByAddress( addr );
		}
		catch ( Exception e )
		{
		    throw new DeserializeException( "Error parsing addresses" );
		}

		//
		// get the option data now
		//
		int offsetInHeader = packetBuffer.position() - offset;
		int hl = hdrlen << 2; // hdrlen * 4 ! :)
		if(hl > offsetInHeader)
		{
			options = new byte[hl - offsetInHeader];
			int x = 0;
			while(offsetInHeader < hl)
			{
				options[x++] = packetBuffer.get();
				++offsetInHeader;
			}
		}
		else
		{
			options = new byte[0];
		}

		return nextHeader;
	} 


	/**
	 * Used to retreive the current version of the 
	 * IP Header. Currently only version 4 is supported.
	 *
	 * @return The current IP version.
	 *
	 */
	public byte getVersion( )
	{
		return version;
	}

	/**
	 * Used to get the current length of the IP Header.
	 *
	 * @return The current IP header length.
	 *
	 */
	public int getHeaderLength( )
	{
	    return (int)(20 + options.length)+(options.length%4!=0?(4-options.length%4):0);
	}

	/**
	 * Retreives the current TOS field from the header.
	 *
	 * @return The current TOS.
	 *
	 */
	public byte getTypeOfService( )
	{
		return tos;
	}

	/**
	 * Sets the TOS flags for the IP header.
	 *
	 * @param tos The new TOS for the IP header
	 */
	public void setTypeOfService(byte tos)
	{
		this.tos = tos;
	}


	/**
	 * Use to test individual bits in the TOS fields. If the
	 * field is set then a value of true is returned. If the
	 * field is not set then a false value is returned.
	 *
	 * @param bit	The bit to validate. Valid values are 0 - 7.
	 *
	 * @return True if the bit is set, false otherwise.
	 *
	 */
	public boolean getTypeOfService(int bit)
	{
		if(bit >= 0 && bit < 8)
			return ((tos & (1 << bit)) != 0);
		
		return false;
	}

	/**
	 * Returns the length of the IP packet, including the
	 * header, in bytes.
	 *
	 * @return The total packet length
	 *
	 */
	public short getPacketLength( )
	{
		return (length);
	}

	/**
	 * Sets the length for IP packet, including the
	 * header. When setting this value the size of 
	 * the IP header must be accounted for. 
	 *
	 * @param length	The length of the IP 
	 * 	header plus the data contained within
	 */
	public void setPacketLength(short length)
	{
		this.length = length;
	}

	/**
	 * Used to retreive the 16-bit identity of the header.
	 *
	 * @return The header's identity.
	 *
	 */
	public short getIdentity( )
	{
		return identity;
	}

	/**
	 * Sets the identity of the IP header
	 *
	 * @param ident	The new identity of the IP header
	 */
	public void setIdentity(short ident)
	{
		identity = ident;
	}

	/**
	 * Used to get the 3-bit flags from the header. The
	 * flags are located in the 3 least significant bits
	 * of the returned byte.
	 *
	 * @return The byte containing the three flags.
	 *
	 */
	public byte getFlags( )
	{
		return flags;
	}

	/**
	 * Sets the flags contained in the upper 3 bits 
	 * of the short value for the fragmentation offset.
	 * The passed bits should occupy the lower 3 bits
	 * of the passed byte.
	 *
	 * @param flags	The flag bits, set in the lower 3 bits of the value.
	 */
	public void setFlags(byte flags)
	{
		this.flags = flags;
	}

	/**
	 * Used to get an individual flag from the flags field.
	 * The bit must be in the range of [0..3). 
	 *
	 * @param bit The flag to retreive.
	 *
	 * @return True if the bit is set, false otherwise.
	 *
	 */
	public boolean getFlag(int bit)
	{
		if(bit >= 0 && bit < 3)
			return ((flags & (1 << bit)) != 0);

		return false;
	}

	/**
	 * Returns the 13-bit fragment offset field from
	 * the IP header.
	 *
	 * @return The 13-bit fragment offset.
	 *
	 */
	public short getFragmentOffset( )
	{
		return fragOffset;
	}

	/**
	 * Sets the fragmentation index for this packet
	 */
	public void setFragmentOffset(short offset)
	{
		this.fragOffset = offset;
	}

	/**
	 * Gets the 8-bit Time To Live (TTL) of the packet.
	 *
	 * @return The packet's ttl.
	 *
	 */
	public byte getTTL( )
	{
		return ttl;
	}

	/**
	 * Sets the time to live for the IP header
	 */
	public void setTTL(byte ttl)
	{
		this.ttl = ttl;
	}

	/**
	 * Checksum calculation:
	 * 
	  A checksum on the header only.  Since some header fields change
	  (e.g., time to live), this is recomputed and verified at each point
	  that the internet header is processed.

    	The checksum algorithm is:

			The checksum field is the 16 bit one's complement of the one's
			complement sum of all 16 bit words in the header.  For purposes of
			computing the checksum, the value of the checksum field is zero.

    	This is a simple to compute checksum and experimental evidence
    	indicates it is adequate, but it is provisional and may be replaced
    	by a CRC procedure, depending on further experience.


	 * @param packetBuffer the buffer to work on
	 */
    public short calculateChecksum( ByteBuffer packetBuffer )
    {
        try 
        {
	       	        
	        // do checksum calcualtion
	        int checksum = 0;
	        int length = getHeaderLength();
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

	/**
	 * Returns the dotted decimal string address of the
	 * source IP address.
	 *
	 * @return The 32-bit IPv4 address
	 */
	public InetAddress getSourceAddress( )
	{
		return sourceIP;
	}

	/**
	 * Sets the IP headers source address.
	 *
	 * @param addr The soruce address for the header.
	 */
	public void setSourceAddr(Inet4Address addr)
	{
		sourceIP = addr;
	}

	/**
	 * Returns the dotted decimal string address of the
	 * destination IP address.
	 *
	 * @return The 32-bit IPv4 address.
	 */
	public InetAddress getDestinationAddress()
	{
		return destIP;
	}
	
	/**
	 * Sets the IP headers destination address.
	 *
	 * @param addr	The destination address
	 *
	 */
	public void setDestinationAddress(Inet4Address addr)
	{
		destIP = addr;
	}
	
	/**
	 * Retrieves the IP header options from the header.
	 * The data is treated as a varaiable length of 
	 * option data. The IPHeader object does not attempt
	 * to interpert the data.
	 *
	 * @return The IP header option data, null if there is none.
	 */
	public byte[] getOptionData( )
	{
		return options;
	}

	/**
	 * Sets the current option data for the header.
	 *
	 * @param options	The new options data.
	 *
	 */
	public void setOptionData(byte[] options)
	{
		this.options = options;
		hdrlen = (byte)((20 + options.length + (options.length%4!=0?(4-options.length%4):0)) / 4);
	}

	/**
	 * Returns a list of options that are associated with the
	 * IP header.
	 *
	 * @return The list of current options.
	 */
	public List getOptions()
		throws InstantiationException
	{
		//
		// check for null data first
		//
		if(this.options == null)
			return new ArrayList();

		//
		// Process the options
		//
		List options = new ArrayList();
		int offset = 0;
		while(offset < this.options.length)
		{
			switch( (int)this.options[offset++] & 0xff )
			{
			case Option.CODE_END_OF_OPTION_LIST:
				options.add(new EndOfOptions());
				break;

			case Option.CODE_LOOSE_SOURCE_ROUTE:
			    {
				LooseSourceRouteOption opt = new LooseSourceRouteOption();
				int addrs = ((int)this.options[offset] & 0xff) - 3;
				offset += 2;

				for(int i = 0; i < addrs/4; i++)
				{
				    byte[] addr = new byte[4];
					for(int j = 0; j < 4; j++)
						addr[j]=this.options[offset++];

					try
					{
					    opt.add( (Inet4Address)Inet4Address.getByAddress(addr) );
					}
					catch (Exception e)
					{
					    throw new InstantiationException( "Cannot parse address.");
					}
				}
				options.add(opt);
			    }
			    break;

			case Option.CODE_STRICT_SOURCE_ROUTE:
			    {
				StrictSourceRouteOption opt = new StrictSourceRouteOption();
				int addrs = ((int)this.options[offset] & 0xff) - 3;
				offset += 2;

				for(int i = 0; i < addrs/4; i++)
				{
				    byte[] addr = new byte[4];
					for(int j = 0; j < 4; j++)
						addr[j]=this.options[offset++];

					try
					{
					    opt.add( (Inet4Address)Inet4Address.getByAddress(addr) );
					}
					catch (Exception e)
					{
					    throw new InstantiationException( "Cannot parse address.");
					}
				}
				options.add(opt);
			    }
			    break;

			case Option.CODE_ROUTE_RECORD:
			    {
				LooseSourceRouteOption opt = new LooseSourceRouteOption();
				int addrs = ((int)this.options[offset] & 0xff) - 3;
				offset += 2;

				for(int i = 0; i < addrs/4; i++)
				{
				    byte[] addr = new byte[4];
					for(int j = 0; j < 4; j++)
						addr[j]=this.options[offset++];

					try
					{
					    opt.add( (Inet4Address)Inet4Address.getByAddress(addr) );
					}
					catch (Exception e)
					{
					    throw new InstantiationException( "Cannot parse address.");
					}
				}
				options.add(opt);
			    }
			    break;

			default:
				throw new InstantiationException("Unsupported Option Type");

			} // end switch
		}

		return options;
	} // end method

	/**
	 * Adds an option to the IP header.
	 *
	 * @param opt	The option to add to the header.
	 *
	 */
	public void addOption(Option opt)
	{
		int origLen = 0;
		if(options == null)
		{
			int len = opt.bytesRequired();
			if((len % 4) != 0)
				len = 4 - (len % 4);

			options = new byte[opt.bytesRequired()];
			int off = opt.writeBytes(options, 0);
			while(off < len)
				options[off++] = (byte)0;
		}
		else
		{
			origLen = options.length;
			if(origLen + opt.bytesRequired() > 40)
				throw new IndexOutOfBoundsException("Option List is too long, must be less than 40 bytes");

			int len = origLen + opt.bytesRequired();
			if((len % 4) != 0)
				len += 4 - (len % 4);

			byte[] ndata = new byte[len];
			System.arraycopy(options,
					 0,
					 ndata,
					 0,
					 origLen);

			int off = opt.writeBytes(ndata, origLen);
			while(off < len)
				ndata[off++] = (byte)0;
		}

		hdrlen = (byte)((20 + options.length) / 4);
	}

	/**
	 * Stores the IP header as an array of bytes into the 
	 * passed data buffer. The IP header is written starting
	 * at the specified offset, and the new offset is returned
	 * to the caller.
	 * 
	 * @param packetBuffer byte buffer to store the header in.
	 *
	 */
	public void serialize( ByteBuffer packetBuffer )
	{
	    int offset = packetBuffer.position();
	    // update hdrlen
	    hdrlen = (byte)((20 + options.length+(options.length%4!=0?(4-options.length%4):0))/4);
	    packetBuffer.put( (byte)((version << 4) | (hdrlen & 0xf)) );
		packetBuffer.put((byte)tos);
		packetBuffer.putShort( (short)getHeadersTotalLength() );
		packetBuffer.putShort( identity );
		packetBuffer.put((byte)((flags << 5) | ((fragOffset >> 8) & 0xff)));
		packetBuffer.put((byte)(fragOffset & 0xff));
		packetBuffer.put((byte)ttl);
		packetBuffer.put((byte)getNextHeaderId());
		checksum = 0x00;
		packetBuffer.putShort(checksum);
		
		packetBuffer.put(sourceIP.getAddress());
		packetBuffer.put(destIP.getAddress());
		if ( options != null )
		{
		    packetBuffer.put(options);
		    int padding = (options.length%4!=0?(4-options.length%4):0);
		    for ( int i=0; i<padding; ++i )
		        packetBuffer.put( (byte)0x00 );
		    
		}
		
		// calc the checksum, and place the calculated bytes
	    packetBuffer.position( offset );
	    checksum = calculateChecksum( packetBuffer );
	    packetBuffer.position( offset + 10 );
	    packetBuffer.putShort( checksum );
	}
	
    /* (non-Javadoc)
     * @see dimes.measurements.packet.matcher.Matchable#match(java.lang.Object)
     */
    public boolean match(Object obj)
    {
        boolean packetsMatch = false;
        try
        {
            IPv4Header otherHeader = (IPv4Header)obj;

            // Check that addresses match and types match
            packetsMatch = ( sourceIP.equals( otherHeader.destIP ) ||
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
        StringBuffer sb = new StringBuffer().append( "[IPv4]" );
        sb.append( ", src:").append( sourceIP );
        sb.append( ", dest:").append( destIP );
        sb.append( ", hdrLen:").append( Header.byteToShort(hdrlen));
        sb.append( ", tos:" ).append( Header.byteToShort(tos) );
        sb.append( ", totalLen:" ).append( Header.shortToInt(length) );
        sb.append( ", id:" ).append( Header.shortToInt(identity) );
        sb.append( ", flags:" ).append( flags );
        sb.append( ", fragOffset:" ).append( Header.shortToInt(fragOffset) );
        sb.append( ", ttl:" ).append( Header.byteToShort(ttl) );
        sb.append( ", options:" ).append(  options );
        
        return sb.toString();
    }

    /**
	 * The supported version of the IP header
	 */
	public static final int IPv4_VERSION	= 4;

	/**
	 * The Type-Of-Service  mask. This constant is used to mask
	 * bits that define the type of service field. See RFC 791.
	 */
	public static final int	TOS_PRECEDENCE_MASK 		= 0xe0;

	/**
	 * Network Critical TOS. See RFC 791.
	 */
	public static final int TOS_PRECEDENCE_NETWORK_CRITICAL	= 0xe0;

	/**
	 * Internetworking Control TOS. See RFC 791.
	 */
	public static final int TOS_PRECEDENCE_INTERNETWORK_CONTROL = 0xc0;

	/**
	 * Critical/ECP TOS. See RFC 791.
	 */
	public static final int TOS_PRECEDENCE_CRITICAL_ECP	= 0x90;

	/**
	 * Flash Override TOS. See RFC 791.
	 */
	public static final int TOS_PRECEDENCE_FLASH_OVERRIDE	= 0x80;

	/**
	 * Flash TOS. See RFC 791.
	 */
	public static final int TOS_PRECEDENCE_FLASH		= 0x60;

	/**
	 * Immediate TOS. See RFC 791.
	 */
	public static final int TOS_PRECEDENCE_IMMEDIATE	= 0x40;

	/**
	 * Priority TOS. See RFC 791.
	 */
	public static final int TOS_PRECEDENCE_PRIORITY		= 0x20;

	/**
	 * Routine TOS. See RFC 791.
	 */
	public static final int TOS_PRECEDENCE_ROUTINE		= 0x00;

	/**
	 * TOS delay mask as defined by RFC 791.
	 */
	public static final int TOS_DELAY_MASK			= 0x10;
	
	/**
	 * Minimize the delay when handling packets.
	 */
	public static final int TOS_DELAY_LOW			= 0x10;

	/**
	 * Normal packet handling
	 */
	public static final int TOS_DELAY_NORMAL		= 0x00;

	/**
	 * TOS Throughput mask
	 */
	public static final int TOS_THROUGHPUT_MASK		= 0x08;

	/**
	 * High throughput requested
	 */
	public static final int TOS_THROUGHPUT_HIGH		= 0x08;

	/**
	 * Normal throughput requested
	 */
	public static final int TOS_THROUGHPUT_NORMAL		= 0x00;

	/**
	 * Packet reliablity mask.
	 */
	public static final int TOS_RELIBILITY_MASK		= 0x04;

	/**
	 * High Reliability requested.
	 */
	public static final int TOS_RELIBILITY_HIGH		= 0x04;

	/**
	 * Normal reliability requrested
	 */
	public static final int TOS_RELIBILITY_NORMAL		= 0x00;

	/**
	 * Mask of the reseered bits.
	 */
	public static final int TOS_RESERVED_MASK		= 0x03;

	/**
	 * The mask of the flags in the fragment field of the 
	 * IP header
	 */
	public static final int FLAGS_MASK			= 0xe000;

	/**
	 * Don't fragment datagrams field
	 */
	public static final int FLAGS_DONT_FRAGMENT		= 0x4000;
	
	/**
	 * More fragments are necessary to reassemble this packet
	 */
	public static final int FLAGS_MORE_FRAGMENTS		= 0x2000;


	/**
	 * The bit(s) that define if the optiosn are copied to
	 * each datagram when (or if) it is fragmented.
	 */
	public static final int OPTION_COPY_MASK		= 0x80;

	/**
	 * The option class mask
	 */
	public static final int OPTION_CLASS_MASK		= 0x60;

	/**
	 * The option number mask
	 */
	public static final int OPTION_NUMBER_MASK		= 0x1f;

	/**
	 * Option identifier for the End Of Options List option.
	 */
	public static final int OPTION_ID_EOO			= 0x00;

	/**
	 * Option identifier for the loose source routing option
	 */
	public static final int OPTION_ID_LOOSE_SOURCE_ROUTING	= 0x83;

	/**
	 * Option identifer for the the strict source routing option
	 */
	public static final int OPTION_ID_STRICT_SOURCE_ROUTING = 0x89;

	/**
	 * Option identifier for the route record option
	 */
	public static final int OPTION_ID_ROUTE_RECORD		= 0x07;

	
	/**
	 * The Option class is used as the base class for any
	 * options that are at the end of the IP header.
	 *
	 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver</A>
	 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
	 *
	 */
	public abstract static class Option
	{
		/**
		 * The single byte that defiend the copied bit,
		 * class, and code for the option
		 */
		protected int code;

		/**
		 * Defines the code for the End-Of-Options list
		 */
		public static final int CODE_END_OF_OPTION_LIST = 0;

		/**
		 * Defines the code for the loose source routing
		 * option
		 */
		public static final int CODE_LOOSE_SOURCE_ROUTE = 0x83;

		/**
		 * Defines the code for the strict soruce routing option
		 */
		public static final int CODE_STRICT_SOURCE_ROUTE= 0x89;

		/**
		 * Defines the code for the packet route record option.
		 */
		public static final int CODE_ROUTE_RECORD	= 0x07;

		/**
		 * Class constructor that is only available to the 
		 * derived classes of the Option class.
		 *
		 * @param code	The code for the option.
		 */
		protected Option(byte code)
		{
			this.code = (int)code & 0x000000ff;
		}

		/**
		 * The nubmer of bytes required to represent this
		 * option in the IP header
		 *
		 * @return The bytes used by this option
		 *
		 */
		abstract int bytesRequired();

		/**
		 * Writes the option to the passed array, starting at
		 * the defined offset. The array must have enough space
		 * or an exception is generated.
		 *
		 * @param dest	The destination to write the data
		 * @param offset The offset of the first written byte
		 *
		 * @return The passed offset plus the number of required
		 *	bytes.
		 *
		 */
		abstract int writeBytes(byte[] dest, int offset);


		/**
		 * Returns the class for the option.
		 *
		 */
		public int getOptionClass()
		{
			return (int)code & OPTION_CLASS_MASK;
		}

		/**
		 * Returns the option number for the instance
		 */
		public int getOptionNumber()
		{
			return (int)code & OPTION_NUMBER_MASK;
		}

		/**
		 * Returns true if the copy flag is set in the 
		 * options header
		 */
		public boolean isOptionCopied()
		{
			return ((code & OPTION_COPY_MASK) != 0);
		}
	}

	/**
	 * This class is used to represent the <EM>End-Of-Option</EM>
	 * list in the IP header. After this option, the option list
	 * is not processed any further.
	 *
	 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver</A>
	 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
	 *
	 */
	public static final class EndOfOptions extends Option
	{
		/**
		 * Returns the number of bytes requried to represent this
		 * option
		 */
		int bytesRequired()
		{
			return 1;
		}

		/**
		 * Converts the option to an array of bytes and writes
		 * those bytes in to the destiantion buffer. The bytes
		 * are written startint at the offset passed to the method.
		 *
		 * @param dest	The destiantion buffer to write the bytes
		 * @param offset The offset to start writing in the buffer
		 *
		 * @return The offset plus the number of bytes written
		 *	to the buffer.
		 *
		 * @exception java.lang.ArrayIndexOutOfBounds Throws in there is
		 *	insufficient space in the buffer.
		 *
		 */
		int writeBytes(byte[] dest, int offset)
		{
			dest[offset++] = 0;
			return offset;
		}


		/**
		 * Constructs a new End-Of-Options list instance
		 * that can be added or found in the IP header.
		 */
		public EndOfOptions()
		{
			super((byte)0);
		}
	}

	/**
	 * This class represents  routing options that
	 * may be part of an IP header. The  route defines
	 * a set of IP addresses that a packet may have or
	 * should pass though. 
	 *
	 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver</A>
	 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
	 *
	 */
	public static class RouteOption extends Option
	{
		/**
		 * The list of addresses for the packet to hit
		 * on it's way to it's destination
		 */
		protected ArrayList addrs;

		/**
		 * Adds an address to the end of the 
		 * set of addresses to hit on its lan trip
		 *
		 * @param addr	The address to add to the loose source route
		 *
		 * @exception java.lang.IndexOutOfBoundsException Thrown if the
		 * 	address list is full
		 */
		void add(Inet4Address addr)
		{
			if(addrs.size() == 9)
				throw new IndexOutOfBoundsException("The address could not be added, the record is full");
			addrs.add(addr);
		}

		/**
		 * The number of bytes required to represent this 
		 * option in an IP header
		 */
		int bytesRequired()
		{
			return 3 + (4 * addrs.size());
		}

		/**
		 * This method is used to serialized the data contained
		 * in the option to the passed array, starting at the offset
		 * passed. If an insufficient amount of space exists then
		 * an exception is thrown.
		 *
		 * @param dest	The destination buffer
		 * @param offset The offset to start writing data
		 *
		 * @return The new offset after writing data
		 *
		 * @exception java.lang.ArrayIndexOutOfBounds Thrown if there
		 * 	is not sufficent space in the passed buffer.
		 */
		int writeBytes(byte[] dest, int offset)
		{
			dest[offset++] = (byte)code;
			dest[offset++] = (byte)bytesRequired();
			dest[offset++] = (byte)4;
			
			Iterator iter = addrs.iterator();
			while(iter.hasNext())
			{
				byte[] addr = ((Inet4Address)iter.next()).getAddress();
				for(int i = 0; i < 4; i++)
					dest[offset++] = addr[i];
			}

			return offset;
		}


		/**
		 * Constructs a new, empty instance of the class.
		 */
		RouteOption(byte code)
		{
			super(code);
			addrs = new ArrayList(9);
		}

		/**
		 * Constructs a new instance of the class with the passed
		 * addresses used for the routing. If the set of addresses
		 * is larger than the option can hold an exception is thrown.
		 *
		 * @param addrs	The list of addresses for the loose source route.
		 *
		 * @exception java.lang.IndexOutOfBoundsException Thrown if the number
		 *	of addresses is to large for the option
		 */
		RouteOption(byte code, Inet4Address[] addrs)
		{
			super(code);
			if(addrs.length > 9)
				throw new IndexOutOfBoundsException("Route Option List Cannot Exceed 9 Addresses");

			this.addrs = new ArrayList(9);
			for(int i = 0; i < addrs.length; i++)
				this.addrs.add(addrs[i]);
		}

		/**
		 * Constructs a new instance of the class with the passed
		 * addresses used for the routing. If the set of addresses
		 * is larger than the option can hold an exception is thrown.
		 *
		 * @param addrs	The list of addresses for the loose source route.
		 *
		 * @exception java.lang.IndexOutOfBoundsException Thrown if the number
		 *	of addresses is to large for the option
		 */
		RouteOption(byte code, List addrs)
		{
			super(code);
			if(this.addrs.size() > 9)
				throw new IndexOutOfBoundsException("Route Option List Cannot Exceed 9 Addresses");

			Iterator iter = addrs.iterator();
			this.addrs = new ArrayList(9);
			while(iter.hasNext())
				this.addrs.add(iter.next());
		}

		/**
		 * Returns the iterator that may be used to look at the
		 * encapsulated addresses. The class Inet4Address is used
		 * to represent the addresses in the list.
		 *
		 * @return An iterator that can be used to operate on 
		 * 	the list.
		 */
		public Iterator iterator()
		{
			return addrs.iterator();
		}

		/**
		 * Returns the number of addresses contained in the
		 * option list.
		 */
		public int size()
		{
			return addrs.size();
		}
	}

	/**
	 * This class represents the loose source routing options that
	 * may be part of an IP header. The loose source route defines
	 * a set of IP addresses that a packet should pass though. As
	 * the packet reaches each address the packet is forwarded to
	 * the next element in the route.
	 *
	 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver</A>
	 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
	 *
	 */
	public static final class LooseSourceRouteOption extends RouteOption
	{
		/**
		 * Constructs a new, empty instance of the class.
		 */
		LooseSourceRouteOption()
		{
			super((byte)0x83);
		}

		/**
		 * Constructs a new instance of the class with the passed
		 * addresses used for the routing. If the set of addresses
		 * is larger than the option can hold an exception is thrown.
		 *
		 * @param addrs	The list of addresses for the loose source route.
		 *
		 * @exception java.lang.IndexOutOfBoundsException Thrown if the number
		 *	of addresses is to large for the option
		 */
		public LooseSourceRouteOption(Inet4Address[] addrs)
		{
			super((byte)0x83, addrs);
		}

		/**
		 * Constructs a new instance of the class with the passed
		 * addresses used for the routing. If the set of addresses
		 * is larger than the option can hold an exception is thrown.
		 *
		 * @param addrs	The list of addresses for the loose source route.
		 *
		 * @exception java.lang.IndexOutOfBoundsException Thrown if the number
		 *	of addresses is to large for the option
		 */
		public LooseSourceRouteOption(List addrs)
		{
			super((byte)0x83, addrs);
		}
	}

	/**
	 * This class represents the strict source routing options that
	 * may be part of an IP header. The strict source route defines
	 * a set of IP addresses that a packet must pass though. As
	 * the packet reaches each address the packet is forwarded to
	 * the next element in the route.
	 *
	 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver</A>
	 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
	 *
	 */
	public static final class StrictSourceRouteOption extends RouteOption
	{
		/**
		 * Constructs an empty instance of this class
		 */
		StrictSourceRouteOption()
		{
			super((byte)0x89);
		}

		/**
		 * Constructs a new instance of the class with the passed
		 * addresses used for the routing. If the set of addresses
		 * is larger than the option can hold an exception is thrown.
		 *
		 * @param addrs	The list of addresses for the loose source route.
		 *
		 * @exception java.lang.IndexOutOfBoundsException Thrown if the number
		 *	of addresses is to large for the option
		 */
		public StrictSourceRouteOption(Inet4Address[] addrs)
		{
			super((byte)0x89, addrs);
		}

		/**
		 * Constructs a new instance of the class with the passed
		 * addresses used for the routing. If the set of addresses
		 * is larger than the option can hold an exception is thrown.
		 *
		 * @param addrs	The list of addresses for the loose source route.
		 *
		 * @exception java.lang.IndexOutOfBoundsException Thrown if the number
		 *	of addresses is to large for the option
		 */
		public StrictSourceRouteOption(List addrs)
		{
			super((byte)0x89, addrs);
		}
	}

	/**
	 * This class represents the route record option that
	 * may be part of an IP header. The strict route record records
	 * a set of IP addresses that a packet has passed though. As
	 * the packet reaches each address the address is added to the
	 * the next element in the route.
	 *
	 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver</A>
	 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
	 *
	 */
	public static final class RouteRecordOption extends RouteOption
	{
		/**
		 * Constructs an empty route record option
		 */
		RouteRecordOption()
		{
			super((byte)0x7);
		}

		/**
		 * Constructs an empty route record with
		 * space for <EM>capacity</EM> addresses
		 * to be recoreded. The capacity CANNOT
		 * exceed 9.
		 *
		 * @param capacity The number of addresses to record, max = 9.
		 *
		 */
		public RouteRecordOption(int capacity)
		{
			super((byte)0x7);

		}

		/**
		 * Constructs a new instance with the give addresses
		 * set in the option header
		 *
		 * @param addrs	The list of addresses for the loose source route.
		 *
		 * @exception java.lang.IndexOutOfBoundsException Thrown if the number
		 *	of addresses is to large for the option
		 */
		public RouteRecordOption(Inet4Address[] addrs)
		{
			super((byte)0x7,addrs);
		}

		/**
		 * Constructs a new instance with the given addresses
		 * stored in the option.
		 *
		 * @param addrs	The list of addresses for the loose source route.
		 *
		 * @exception java.lang.IndexOutOfBoundsException Thrown if the number
		 *	of addresses is to large for the option
		 */
		public RouteRecordOption(List addrs)
		{
			super((byte)0x7,addrs);
		}
	}


}
