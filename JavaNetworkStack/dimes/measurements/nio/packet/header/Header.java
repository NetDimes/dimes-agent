/*
 * Created on 01/05/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package dimes.measurements.nio.packet.header;

import java.nio.ByteBuffer;

import dimes.measurements.nio.error.DeserializeException;
import dimes.measurements.nio.packet.Packet;

/**
 * @author Udi Weinsberg
 *
 */
public abstract class Header
{
    protected Header nextHeader;
    protected int headerId;
    protected Packet packet;
    
    public Header( short headerId )
    {
        nextHeader = null;
        this.headerId = headerId;
    }
    
    /**
     * Set the 'parent' packet.
     * @param packet the packet that holds the header
     */
    public void setPacket( Packet packet )
    {
        this.packet = packet;
    }
    
    /**
     * @return Returns the nextHeader.
     */
    public Header getNextHeader()
    {
        return nextHeader;
    }
    
    /**
     * Set nextHeader.
     * 
     * @param nextHeader the next header
     */
    public void setNextHeader( Header nextHeader )
    {
        this.nextHeader = nextHeader; 
        nextHeader.setPacket( this.packet );
    }
    
    /**
     * Get the header id. The id might depend on the requester, so <br>
     * the requester header is provided.
     *  
     * @param requestingHeader the header that requested the id, or null for default.
     * 
     * @return The header id
     */
    public int getHeaderId( Header requestingHeader )
    {
        return headerId;
    }
    
    /**
     * Get the header length.
     * @return header length
     */
    abstract public int getHeaderLength();
    
    /**
     * Get the total subsequent headers length.
     * 
     * @return total subsequent headers length
     */
    public int getHeadersTotalLength()
    {
        int totalLength = this.getHeaderLength();
        // sum up all next headers
        if ( nextHeader != null )
            totalLength += nextHeader.getHeadersTotalLength();
        
        // return total length
        return totalLength;
    }
       
    /**
     * Get the next header id.
     * 
     * @return next header id 
     */
    public int getNextHeaderId()
    {
        if ( nextHeader != null )
            return nextHeader.getHeaderId( this );
        else
            return 0;
    }
    
    /**
     * Returns the header using id.
     * 
     * @param headerId the id to look for
     * @return the Header reference or null if not found
     */
    public Header getHeaderById( int headerId )
    {
        if ( this.getHeaderId( this ) == headerId )
            return this;
        else if ( nextHeader != null )
            return nextHeader.getHeaderById( headerId );
       
        // cannot find? return null
        return null;
    }
   
    /**
     * Deserialize self from a byte buffer.
     * 
     * @see dimes.measurements.nio.packet.header.Header#deserialize(java.nio.ByteBuffer)
     */
    abstract public int deserialize( ByteBuffer packetBuffer ) throws DeserializeException;
    
    /**
     * Serialize self into byte buffer.
     * 
     * @param packetBuffer the packet
     */
    abstract public void serialize( ByteBuffer packetBuffer );
    
    /**
     * Serialize all headers into byte array.
     * 
     * @param packetBuffer a packet buffer to insert the values into
     * 
     */
    public void toByteArray( ByteBuffer packetBuffer )
    {
        // first serialize all headers in the proper location
        if ( nextHeader != null )
        {
            // advance position in buffer
            int offset = packetBuffer.position();
            packetBuffer.position( packetBuffer.position() + getHeaderLength() );
            nextHeader.toByteArray( packetBuffer );
            packetBuffer.position( offset );
        }
        
        // now, serialize self into the buffer
        this.serialize( packetBuffer );
        
    }
    
    /**
     * Finalize touchups on self, if needed.
     * 
     * @param packetBuffer header holder byte buffer.
     * 
     */
    public void touchup( ByteBuffer packetBuffer )
    {
        // do nothing
    }
    
    /**
     * Finalize touchups, if needed.
     *
     * @param packetBuffer header holder byte buffer.
     */
    public void finalTouchups( ByteBuffer packetBuffer )
    {
        // first serialize all headers in the proper location
        if ( nextHeader != null )
        {
            // advance position in buffer
            int offset = packetBuffer.position();
            packetBuffer.position( packetBuffer.position() + getHeaderLength() );
            nextHeader.finalTouchups( packetBuffer );
            packetBuffer.position( offset );
        }
        
        // now, serialize self into the buffer
        this.touchup( packetBuffer );
    }
    
    
    /**
	 * Converts a byte to a short, treating the byte as unsigned.
	 *
	 * @param b The byte to convert.
	 *
	 * @return The converted value.
	 *
	 */
	public static short byteToShort(byte b)
	{
		short r = (short)b;
		if(r < 0)
			r += 256;
		return r;
	}

	/**
	 * Converts a byte to an integer, treating the byte as unsigned.
	 *
	 * @param b The byte to convert.
	 *
	 * @return The converted value.
	 *
	 */
	public static int byteToInt(byte b)
	{
		int r = (int)b;
		if(r < 0)
			r += 256;
		return r;
	}

	/**
	 * Converts a short to an integer, treating the short as unsigned.
	 *
	 * @param s The short to convert.
	 *
	 * @return The converted value
	 *
	 */
	public static int shortToInt(short s)
	{
		int r = (int)s;
		if(r < 0)
			r += 0x10000;
		return r;
	}

    
}
