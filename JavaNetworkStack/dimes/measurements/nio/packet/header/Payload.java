/*
 * Created on 04/05/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package dimes.measurements.nio.packet.header;

import java.nio.ByteBuffer;

import dimes.measurements.nio.error.DeserializeException;

/**
 * @author Udi Weinsberg
 *
 * Payload is a form of a header, that can be chained in the headers chain.
 */
public class Payload extends Header
{
	public static final byte id=0;
    protected byte[] payload;
    
    /**
     * 
     */
    public Payload( )
    {
        super(id);
        this.payload = null;
    }
    
    /**
     * Construct a Payload header from a byte array
     * 
     * @param payload payload byte array.
     */
    public Payload( byte[] payload )
    {
        super((byte)0);
        this.payload = payload;
    }

    /* (non-Javadoc)
     * @see dimes.measurements.packet.header.Header#getHeaderLength()
     */
    public int getHeaderLength()
    {
        return (payload!=null)?payload.length:0;
    }

    /* (non-Javadoc)
     * @see dimes.measurements.packet.header.Header#serialize()
     */
    public void serialize( ByteBuffer packetBuffer )
    {
        if ( payload != null )
            packetBuffer.put( payload );
    }

    /* (non-Javadoc)
     * @see dimes.measurements.packet.matcher.Matchable#match(java.lang.Object)
     */
    public boolean match(Object obj)
    {
        boolean packetsMatch = false;
        try
        {
            //Payload otherHeader = (Payload)obj;
            // don't waste time on checking bytes
            // TODO - do we need to compare bytes?
            packetsMatch = true;

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
        StringBuffer sb = new StringBuffer().append( "[Payload]" );
        sb.append( ", data:").append( String.valueOf(payload) );
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see dimes.measurements.packet.header.Header#deserialize(java.nio.ByteBuffer)
     */
    public int deserialize(ByteBuffer packetBuffer) throws DeserializeException
    {
        // treat all remaining data, until the limit, as payload...
        int payloadLen = packetBuffer.remaining();
//        System.out.println("PAYLOAD: deserialize Payload payloadLen"+payloadLen);
        payload = new byte[ payloadLen ];
        packetBuffer.get( payload );
        return 0;
    }

	/**
	 * Add a payload to the existing payload.
	 * This function is incremental - 
	 * useful for constructing a paayload in stages
	 * 
	 * @param load byte array to add to the end of payload.
	 */
	public void addLoad(byte[] load) {
		int currentSize = (this.payload == null ? 0 : this.payload.length);
		int expectedSize = currentSize +load.length;
		byte[] newBufferArray = new byte[expectedSize];
		//ByteBuffer newBuffer = ByteBuffer.allocate(expectedSize);
		ByteBuffer newBuffer = ByteBuffer.wrap(newBufferArray);
		if (this.payload != null)
			newBuffer.put(this.payload);
		newBuffer.put(load);
		//this.payload = newBuffer.array();
		this.payload = newBufferArray;
	}

	/**
	 * Get the payload byte array
	 * Useful for analyzin proprietary data stored in payload.
	 * 
	 * @return payload byte array
	 */
	public byte[] getBytes() {
		return this.payload;
	}
	
	public void setTrainId(short trainID) {
		ByteBuffer buffer = ByteBuffer.wrap(payload);
		buffer.putShort(12, trainID);
	}
	
	public short getTrainId() {
		ByteBuffer buffer = ByteBuffer.wrap(payload);
		return buffer.getShort(12);
	}
}
