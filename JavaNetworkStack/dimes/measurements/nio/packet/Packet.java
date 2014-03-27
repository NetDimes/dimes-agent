package dimes.measurements.nio.packet;

import java.nio.ByteBuffer;

import dimes.measurements.nio.NetworkStack;
import dimes.measurements.nio.error.DeserializeException;
import dimes.measurements.nio.packet.header.Header;
import dimes.measurements.nio.packet.header.HeadersRepository;
import dimes.measurements.nio.packet.header.Payload;
	/**
	 * @author Udi Weinsberg
	 *
	 * TODO To change the template for this generated type comment go to
	 * Window - Preferences - Java - Code Style - Code Templates
	 */
	public class Packet implements Comparable
	{
	    protected Header header;
	    protected PacketFinalizer finalizer;
	    protected long sendTime;
	    protected long rcvTime;
	    protected NetworkStack netStack;
		public long microSecTimestamp;
		public long nanoSecTimestamp;
		
		public int timeStampPosition = -1;
		public int checkSumPosition = -1;
		
		private int id;
	    
	    /**
	     * Create a new packet, using the network stack.
	     * 
	     * @param netStack the network stack of the packet
	     */
	    public Packet( NetworkStack netStack )
	    {
	        header = null;
	        finalizer = null;
	        this.netStack = netStack; 
	    }
	    
	    /**
	     * Create a new packet, from a headers array
	     * 
	     * @param headers array
	     */
	    public Packet( Header[] headers ,  NetworkStack netStack )
	    {
	    	this(headers , netStack , null);
	    }
	    
	    /**
	     * Create a new packet, as copy constructor. Note that headers
	     * remains the same, and are not cloned.
	     * 
	     * @param packet the packet to copy data from
	     */
	    public Packet( Packet packet )
	    {
	        header = packet.header;
	        netStack = packet.netStack;
	        finalizer = packet.finalizer;
	        sendTime = packet.sendTime;
	        rcvTime = packet.rcvTime;
	    }

	    public Packet(Header[] headers, NetworkStack stack, PacketFinalizer aFinalizer) {
	        header = null;
	        this.finalizer = aFinalizer;
	        this.netStack = stack; 
	        this.setHeaders(headers);
		}

		/**
	     * Set the first header of the packet.
	     * 
	     * @param header
	     */
	    public void setHeader( Header header )
	    {
	        this.header = header; 
	        if ( header != null )
	            header.setPacket( this );
	    }
	    
	    /**
	     * Set all headers. Headers are chained by order.
	     * 
	     * @param headers the headers to set
	     */
	    public void setHeaders( Header[] headers )
	    {
	        if ( headers != null )
	        {
	        //	System.out.println(" headers != null ");
	            this.header = headers[0];
	            
	            Header currHeader = headers[0];
	            for ( int i = 1; i<headers.length; ++i )
	            {
	                currHeader.setNextHeader( headers[i] );
	                currHeader = headers[i];
	                
	            }
	        }
	        else
	        {
	        	//System.out.println(" headers == null ");
	        }
	        
	        
	    }

	    /**
	     * @param pf
	     */
	    public void setFinalizer( PacketFinalizer pf )
	    {
	        finalizer=pf;
	    }
	    
	    /**
	     * Returns the header using id.
	     * 
	     * @param headerId the id to look for
	     * @return the Header reference or null if not found
	     */
	    public Header getHeaderById( int headerId )
	    {
	        if ( header != null )
	            return header.getHeaderById( headerId );
	        
	        return null;
	    }
	    
	    /**
	     * Serialize all headers into byte array.
	     * 
	     * @return the byte array of self
	     */
	    public byte[] toByteArray()
	    {
	    	if (header == null) {
	    		return null;
	    	}
	    	
	        int packetLen = header.getHeadersTotalLength();
	        byte[] bufferArray = new byte[packetLen];
	        ByteBuffer packetBuffer = ByteBuffer.wrap(bufferArray);
		        
		    header.toByteArray( packetBuffer );
		    packetBuffer.rewind();
		    header.finalTouchups( packetBuffer );

		    // finalize the packet
		    if ( finalizer != null ) {
		        packetBuffer.rewind();
		        finalizer.finalizeBuffer( this, packetBuffer );
		    }
		    return bufferArray;
	    }

	    /**
	     * Serialize all headers into byte array.
	     * 
	     */
	    public void fromByteArray( byte[] packetBytes )
	    	throws DeserializeException
	    {
	        if ( netStack == null )
	            throw new DeserializeException( "Networking Stack not selected." );
	        
	        HeadersRepository repository = HeadersRepository.getInstace();
	        
	        try
	        {
	            // wrap the packet
//	        	System.out.println("\n fromByteArray:packetBytes"+packetBytes.length);
	            ByteBuffer packetBuffer  = ByteBuffer.wrap( packetBytes );
//	            System.out.println("fromByteArray: deserialize Payload payloadLen"+packetBuffer.remaining());

	            // create the first header, using the network stack
	            this.header = (Header)netStack.getFirstHeader().getClass().newInstance();
	            if(this.header == null)
	            {
	            	throw new DeserializeException( "fromByteArray failed to get new instance " );	
	            }
	            Header currHeader = this.header;  
	            int nextHeaderId = currHeader.deserialize( packetBuffer );
	            while ( nextHeaderId != 0 )
	            {
	                // create the header using the id
	                Header nextHeader = repository.createHeader( nextHeaderId );
	                
	                if(nextHeader == null)
	                {
	                	System.err.println("nextHeaderId:"+nextHeaderId);
	                	throw new DeserializeException( "fromByteArray failed:nextHeader == null " );	
	                }
	                // deserialize the header
	                nextHeaderId = nextHeader.deserialize( packetBuffer );
	                
	                // chain the headers
	                currHeader.setNextHeader( nextHeader );
	                
	                // move to next header
	                currHeader = nextHeader;
	            }
	            
	            // check if there is still more bytes
	            if ( packetBuffer.remaining() > 0 )
	            {
	                // treat the rest as payload
	                Payload payload = new Payload();
	                payload.deserialize( packetBuffer );
	                currHeader.setNextHeader( payload );
	            }
	        }
	        catch (InstantiationException e)
	        {
	            throw new DeserializeException( "Cannot instantiate header, InstantiationException." );
	        }
	        catch (IllegalAccessException e)
	        {
	            throw new DeserializeException( "Cannot instantiate header, IllegalAccessException." );
	        }
	    }
	    /**
	     * @return Returns the rcvTime.
	     */
	    public long getRcvTime()
	    {
	        return rcvTime;
	    }
	    /**
	     * @param rcvTime The rcvTime to set.
	     */
	    public void setRcvTime(long rcvTime)
	    {
	        this.rcvTime = rcvTime;
	    }
	    /**
	     * @return Returns the sendTime.
	     */
	    public long getSendTime()
	    {
	        return sendTime;
	    }
	    /**
	     * @param sendTime The sendTime to set.
	     */
	    public void setSendTime(long sendTime)
	    {
	        this.sendTime = sendTime;
	    }
	    /**
	     * @return Returns the header.
	     */
	    public Header getHeader()
	    {
	        return header;
	    }
	    /**
	     * @return Returns the netStack.
	     */
	    public NetworkStack getNetStack()
	    {
	        return netStack;
	    }
	    
	    /**
	     * Parse the Header to a string representation.
	     * 
	     * @return the string representation of the header
	     */
	    public String toString()
	    {
	        StringBuffer sb = new StringBuffer().append( "[Packet]");
	        sb.append( ", sendTime:" ).append( sendTime );
	        sb.append( ", recvTime:" ).append( rcvTime );
	        sb.append( "\r\n" );
	        Header h = header;
	        while ( h != null )
	        {
	            sb.append( h ).append( "\r\n" );
	            h = h.getNextHeader();
	        }
	        return sb.toString();
	    }

	public static String toHexString ( byte[] b )
	   {
	   StringBuffer sb = new StringBuffer( b.length * 2 );
	   for ( int i=0; i<b.length; i++ )
	      {
	      // look up high nibble char
	      sb.append( hexChar [( b[i] & 0xf0 ) >>> 4] );

	      // look up low nibble char
	      sb.append( hexChar [b[i] & 0x0f] );
	      
	      if (i<b.length-1)
	    	  sb.append('-');
	      }
	   return sb.toString();
	   }

	
//	 table to convert a nibble to a hex char.
	static char[] hexChar = {
	   '0' , '1' , '2' , '3' ,
	   '4' , '5' , '6' , '7' ,
	   '8' , '9' , 'a' , 'b' ,
	   'c' , 'd' , 'e' , 'f'};

	public int compareTo(Object arg0) {
		Packet otherPacket = (Packet) arg0;
		return (int) (this.microSecTimestamp - otherPacket.microSecTimestamp);
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public int getHeadersSize() {
		return header == null ? 0 : header.getHeadersTotalLength();
	}
}
