/*
 * Created on 05/05/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package dimes.measurements.nio.packet.header;

import java.nio.ByteBuffer;

import dimes.measurements.nio.error.DeserializeException;

/**
 * @author Udi Weinsberg
 *  0                   1                   2                   3   
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |          Source Port          |       Destination Port        |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                        Sequence Number                        |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                    Acknowledgment Number                      |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |  Data |           |U|A|P|R|S|F|                               |
 | Offset| Reserved  |R|C|S|S|Y|I|            Window             |
 |       |           |G|K|H|T|N|N|                               |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |           Checksum            |         Urgent Pointer        |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                    Options                    |    Padding    |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 |                             data                              |
 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 
 *
 *
 *
 *
 */
public class TCPHeader extends Header
{
	/**
	 * The IP protocol value that indicates the data contains TCP information.
	 */
	public static final byte IP_PROTOCOL_TCP = 0x06;
	
	/**
	 * The bitmask that may be applied to the TCP flags to determine whether the
	 * urgent pointer field should be considered significant.
	 */
	public static final byte TCP_FLAG_MASK_URG = 0x20;
	
	/**
	 * The bitmask that may be applied to the TCP flags to determine whether the
	 * acknowledgement field should be considered significant.
	 */
	public static final byte TCP_FLAG_MASK_ACK = 0x10;
	
	
	
	/**
	 * The bitmask that may be applied to the TCP flags to determine whether the
	 * push flag is set.
	 */
	public static final byte TCP_FLAG_MASK_PSH = 0x08;
	
	
	
	/**
	 * The bitmask that may be applied to the TCP flags to determine whether the
	 * reset flag is set, indicating the forceful destruction of a connection.
	 */
	public static final byte TCP_FLAG_MASK_RST = 0x04;
	
	
	
	/**
	 * The bitmask that may be applied to the TCP flags to determine whether the
	 * SYN flag is set, indicating a new connection.
	 */
	public static final byte TCP_FLAG_MASK_SYN = 0x02;
	
	
	
	/**
	 * The bitmask that may be applied to the TCP flags to determine whether the
	 * FIN flag is set, indicating the connection is being closed.
	 */
	public static final byte TCP_FLAG_MASK_FIN = 0x01;
	
//	-----------------  Word 1 --------------------------------//
	// The source port for this TCP header.
	short sourcePort;
	// The destination port for this TCP header.
	short destinationPort;
	
//	-----------------  Word 2 --------------------------------//
	// The sequence number for this TCP header.
	int sequenceNumber;
	
//	-----------------  Word 3 --------------------------------//
	// The acknowledgement number for this TCP header.
	int ackNumber;
	
//	-----------------  Word 4 --------------------------------//
	// The position of the end of this TCP header and 
	//  the beginning of the data.
	byte dataOffset;
	// The set of flags for this TCP header.
	byte tcpFlags;
	// The window for this TCP header.
	short window;
	// The checksum for this TCP header.
	
//	-----------------  Word 5S -------------------------------//
	short checksum;
	// The urgent pointer for this TCP header.
	short urgentPointer;
//	-----------------  Word 5 optional  ----------------------//
	// Any data associated with options in this TCP header.
	byte[] optionBytes;
//	-----------------  end  ----------------------------------//
	
	
	
	
	
	
	/**
	 * Creates a new TCP header with the provided information.
	 *
	 * @param  sourcePort       The source port for this TCP header.
	 * @param  destinationPort  The destination port for this TCP header.
	 * @param  sequenceNumber   The sequence number for this TCP header.
	 * @param  ackNumber        The acknowledgement number for this TCP header.
	 * @param  dataOffset       The data offset for this TCP header, measured in
	 *                          32-bit words.
	 * @param  tcpFlags         The set of flags associated with this TCP header.
	 * @param  window           The window for this TCP header.
	 * @param  checksum         The checksum for this TCP header.
	 * @param  urgentPointer    The urgent pointer for this TCP header.
	 * @param  optionBytes      The raw data associated with any options in this
	 *                          TCP header.
	 */
	public TCPHeader(short sourcePort, short destinationPort, int sequenceNumber,
			int ackNumber, byte dataOffset, byte tcpFlags, short window,
			short checksum, short urgentPointer, byte[] optionBytes)
	{
		super(IP_PROTOCOL_TCP);
		this.sourcePort      = sourcePort;
		this.destinationPort = destinationPort;
		this.sequenceNumber  = sequenceNumber;
		this.ackNumber       = ackNumber;
		this.dataOffset      = dataOffset;
		this.tcpFlags        = tcpFlags;
		this.window          = window;
		this.checksum        = checksum;
		this.urgentPointer   = urgentPointer;
		this.optionBytes     = optionBytes;
		
		if(dataOffset>0xf || dataOffset<0x5 )
			System.err.println("TCPHeader: invalid dataOffset parameter"+dataOffset);
	}
	
	
	
	/**
	 * Decodes information in the provided byte array as a TCP header.
	 * 
	 * @param packetBuffer the buffer which holds the packet
	 * 
	 * @return next header id ( usually payload = 0 )
	 */
	public int deserialize( ByteBuffer packetBuffer )
	throws DeserializeException
	{
 //       System.out.println("TCP: deserialize Payload payloadLen"+packetBuffer.remaining());

		if( packetBuffer == null)
			throw new DeserializeException("TCP header object is  null");
		
		if( packetBuffer.remaining() < 20)
			throw new DeserializeException("Minimum TCP header size is 20 bytes");
		
//		save the offset
//		-----------------  Word 1 -------------------------------//   		
		this.sourcePort = packetBuffer.getShort();
		this.destinationPort = packetBuffer.getShort();
//		-----------------  Word 2 -------------------------------// 
		this.sequenceNumber = packetBuffer.getInt();
//		-----------------  Word 3 -------------------------------// 
		this.ackNumber = packetBuffer.getInt();
		
		
//		-----------------  Word 4 -------------------------------//		
		
		short dataOffset_Flags= packetBuffer.getShort();
		this.dataOffset = (byte)((dataOffset_Flags >>4) & 0xf);
		// The set of flags for this TCP header.
		this.tcpFlags  =  (byte)(dataOffset_Flags & 0xf);;
		// The window for this TCP header.
		this.window=packetBuffer.getShort();
		// The checksum for this TCP header.
		
//		-----------------  Word 5 -------------------------------//
		this.checksum = packetBuffer.getShort();
		this.urgentPointer = packetBuffer.getShort();
		
		
		
		
//		-----------------  Word 6 .... -------------------------------//   
		byte[] optionBytes;
		if (dataOffset > 5)
		{
			optionBytes = new byte[(dataOffset - 5) * 4];
			System.err.println("the TCP header option bytes were ignored"+ optionBytes + "words are ignored");
//			System.arraycopy(packetBuffer.get(20) , offset+20, optionBytes, 0,optionBytes.length);
		}
		else
		{
			this.optionBytes =null;
		}
		return 0;
		
	}
	
	
	
	/**
	 * Retrieves the source port for this TCP packet.  The port will be encoded in
	 * the lower 16 bits of the returned int value.
	 *
	 * @return  The source port for this TCP packet.
	 */
	public int getSourcePort()
	{
		return sourcePort;
	}
	
	
	
	/**
	 * Retrieves the destination port for this TCP packet.  The port will be
	 * encoded in the lower 16 bits of the returned int value.
	 *
	 * @return  The destination port for this TCP packet.
	 */
	public short getDestinationPort()
	{
		return destinationPort;
	}
	
	
	
	/**
	 * Retrieves the sequence number for this TCP packet.  The value will use all
	 * 32 bits of the returned int value.
	 *
	 * @return  The sequence number for this TCP packet.
	 */
	public int getSequenceNumber()
	{
		return sequenceNumber;
	}
	
	
	
	/**
	 * Retrieves the acknowledgement number for this TCP packet.  The value will
	 * use all 32 bits of the returned int value.
	 *
	 * @return  The acknowledgement number for this TCP packet.
	 */
	public int getAcknowledgementNumber()
	{
		return ackNumber;
	}
	
	
	
	/**
	 * Retrieves the data offset for this TCP header.  The value will be encoded
	 * in the lower 4 bits of the provided value and will indicate the number of
	 * 32-bit words contained in the header.
	 *
	 * @return  The data offset for this TCP header.
	 */
	public byte getDataOffset()
	{
		return dataOffset;
	}
	
	
	
	/**
	 * Retrieves the length in bytes of this TCP header.
	 *
	 * @return  The length in bytes of this TCP header.
	 */
	public int getHeaderLength()
	{
		return (dataOffset * 4);
	}
	
	
	
	/**
	 * Retrieves the set of flags for this TCP header.  The flags will be encoded
	 * in the lower 6 bits of the returned int value, and individual flags may
	 * be checked by ANDing them with the values of the
	 * <CODE>TCP_FLAG_MASK_*</CODE> constants.
	 *
	 * @return  The set of flags for this TCP header.
	 */
	public byte getTCPFlags()
	{
		return tcpFlags;
	}
	
	
	
	/**
	 * Retrieves the window for this TCP header.  The value will be encoded in
	 * the lower 16 bits of the returned int value.
	 *
	 * @return  The window for this TCP header.
	 */
	public short getWindow()
	{
		return window;
	}
	
	
	
	/**
	 * Retrieves the checksum for this TCP header.  The value will be encoded in
	 * the lower 16 bits of the returned int value.
	 *
	 * @return  The checksum for this TCP header.
	 */
	public short getChecksum()
	{
		return checksum;
	}
	
	
	
	/**
	 * Retrieves the urgent pointer for this TCP header.  The window will be
	 * encoded in the lower 16 bits of the returned int value.
	 *
	 * @return  The urgent pointer for this TCP header.
	 */
	public short getUrgentPointer()
	{
		return urgentPointer;
	}
	
	
	
	/**
	 * Retrieves the data for any options associated with this TCP header.
	 *
	 * @return  The data for any options associated with this TCP header.
	 */
	public byte[] getOptionBytes()
	{
		return optionBytes;
	}
	
	/**
	 * Tcp header constructor
	 * 
	 */
	public TCPHeader()
	{
		super(IP_PROTOCOL_TCP);
		// TODO Auto-generated constructor stub
	}
	
	
	/* (non-Javadoc)
	 * @see dimes.measurements.packet.header.Header#serialize(java.nio.ByteBuffer)
	 */
	public void serialize(ByteBuffer packetBuffer)
	{
		// TODO Auto-generated method stub
		
		
		packetBuffer.putShort(sourcePort);
		packetBuffer.putShort(destinationPort);
		packetBuffer.putInt(sequenceNumber );
		packetBuffer.putInt(ackNumber );
		packetBuffer.putShort((short)((dataOffset<<(16-4)) + (tcpFlags)));
		packetBuffer.putShort(window);
		packetBuffer.putShort(checksum);
		packetBuffer.putShort(urgentPointer);
		
		if(dataOffset>5 && dataOffset<0xf)
		{
			if(optionBytes.length != dataOffset*4)
				System.err.println("TCPHeader::serialize:invalid optionBytes.length"+optionBytes.length +" != dataOffset"+ dataOffset);
			else
			{
				System.err.println("TCPHeader::serialize: TODO add optionBytes to the packet");
				//	packetBuffer.put(optionBytes,dataOffset*4,);
			}
		}
		
		
	}
	
	/* (non-Javadoc)
	 * @see dimes.measurements.packet.matcher.Matchable#match(java.lang.Object)
	 */
	public boolean match(Object obj)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public String toString()
	{
		String str="srcPort:"+sourcePort+" dstPort:"+destinationPort
					+" seq:"+sequenceNumber+" Ack#:"+ackNumber+"\n"  
					+"dataOffset:"+dataOffset+"(header size ="+(dataOffset*4)+
					" tcpFlags:"+ tcpFlags
					+" window"+window+" checksum:"+checksum+
					"urgentPointer"+urgentPointer+
					"optionBytes"+optionBytes;

		
		if ((tcpFlags & TCP_FLAG_MASK_URG)!=0)
			str+= " TCP_FLAG_MASK_URG is set\n";

		if ((tcpFlags & TCP_FLAG_MASK_ACK)!=0)
			str+= " TCP_FLAG_MASK_ACK is set\n";
		if ((tcpFlags & TCP_FLAG_MASK_PSH)!=0)
			str+= " TCP_FLAG_MASK_PSH is set\n";
		if ((tcpFlags & TCP_FLAG_MASK_RST)!=0)
			str+= " TCP_FLAG_MASK_RST is set\n";
		if ((tcpFlags & TCP_FLAG_MASK_SYN)!=0)
			str+= " TCP_FLAG_MASK_SYN is set\n";
		if ((tcpFlags & TCP_FLAG_MASK_FIN)!=0)
			str+= " TCP_FLAG_MASK_FIN is set\n";

		return str; 
	}
	
}
