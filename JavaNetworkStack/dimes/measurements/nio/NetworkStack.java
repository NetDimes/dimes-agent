package dimes.measurements.nio;

import dimes.measurements.nio.error.MeasurementException;
import dimes.measurements.nio.error.MeasurementInitializationException;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.PacketBuffer;
import dimes.measurements.nio.packet.builder.PacketBuilder;
import dimes.measurements.nio.packet.header.Header;

/***********
 * 
 * @author Ohad Serfaty
 * <br>
 * The main Abstract class for implementing a native Network Stack
 */
public abstract class NetworkStack {

	protected NativeLogger logger=null;
	private Header firstHeader;

	/**
	 * Initialize the Stack with the best possible interface
	 * 
	 * @throws MeasurementInitializationException
	 */
	public abstract void init() throws MeasurementInitializationException;
	
	/**
	 * initialize the stack with a specific device/IP parameter
	 * 
	 * @param arguments stack specific arguments (documented in each netwrok stack).
	 * 
	 * @throws MeasurementInitializationException in initialization failure
	 */
	public abstract void init(String[] arguments) throws MeasurementInitializationException;
	
	/**
	 * Initialize the internal buffer with a set of packets
	 * 
	 * @param buffer  a buffer of Packet objects
	 * @throws MeasurementException in initialization failure
	 */
	public abstract void initPackets(PacketBuffer buffer) throws MeasurementException;

	/**
	 * send a packet without delay
	 * 
	 * @param packet bytes to send
	 * @return sned time in nanosecs.
	 * 
	 * @throws MeasurementException in case of exception	
	 */
	public abstract long sendPacket(byte[] packet , int timestampPosition ,int checkSumPosition) throws MeasurementException;
	
	/**
	 * <p>
	 * send a packet without delay , without timestamping and without recalculating
	 * the Checksum.
	 * </p>
	 * 
	 * @param packet bytes to send
	 * @return sned time in nanosecs.
	 * 
	 * @throws MeasurementException in case of exception	
	 */
	public long sendPacket(byte[] packet) throws MeasurementException{
		return sendPacket(packet , -1 , -1);
	}
	
	
	/**
	 * send a set of packets in relative timestamps 
	 * 
	 * @param packet send a set of packets in relative timestamps 
	 * @param sendTime preferred time to send packet.
	 *  
	 * @return	actual send times in nano seconds.
	 * 
	 * @throws MeasurementException in case of exception	 */
	public abstract long send(Packet packet , long sendTime) throws MeasurementException;
	
	
	/**
	 * send a set of packets in relative timestamps 
	 * 
	 * @param buffer a buffer of Packet objects
	 * @return	actual send times in nano seconds.
	 * 
	 * @throws MeasurementException in case of exception
	 */
	public abstract long[] send(PacketBuffer buffer) throws MeasurementException;
	
	/**
	 * <p>
	 * Set the size of the PacketBuffer. when a receive() method is called with a non-null buffer ,
	 * it will perform a callback after accumulating this number of packets. 
	 * Default size is 10 packets.
	 * 
	 * @param size number of packets to buffer before calling 'callback'
	 */
	public abstract void setCallbackBufferSize(int size);
	
	/**
	 * <p>
	 * When one of the parameters is null , this function acts like the on of the subsequent
	 *  receive() functions.
	 *  <p>
	 * When both buffer and context are not null , the function will listen until the buffer
	 * is full and then perform a callback.
	 * <br>
	 * 
	 * @param listenTimeout time to listen
	 * @param buffer a buffer to fill packets with
	 * @param context callback context
	 * @throws MeasurementException
	 * 
	 */
	public abstract void receive(long listenTimeout, PacketBuffer buffer, CallbackContext context) throws MeasurementException;
	
	/**
	 * listen without callbacks
	 * 
	 * @param listenTimeout time to listen
	 * @param buffer a buffer to fill packets with
	 * @throws MeasurementException
	 */
	public void receive( long listenTimeout , PacketBuffer buffer) throws MeasurementException{
		receive(listenTimeout , buffer, null);
	}
	
	/**
	 * listen without buffering
	 * 
	 * @param listenTimeout timeout to listen
	 * @param callback callback context object . Please note that this context provides a mechanism to
	 * Stop the listening in case that a proper packet arrives...
	 * 
	 * @throws MeasurementException
	 */
	public void receive( long listenTimeout , CallbackContext callback) throws MeasurementException{
		receive(listenTimeout , null , callback);
	}
	
	/**
	 * @return the appropriate IP Header builder for this type of Stack.
	 */
	public abstract PacketBuilder getPacketBuilder();
	
	/**
	 *  close the stack and dispose of all resources.
	 */
	public abstract void close();
	

	/**
	 * <p>
	 * The default NativeLogger logging function.
	 *  to change the native logging mechanism , use setNativeLogger(...) and give
	 *  your own implementation of the logger.
	 * <p>
	 * TODO : find a way to ignore the logging inside the Native code
	 * for production measurements.
	 * 
	 * @param logLevelString
	 * @param logMessage
	 */
	public void log(String logLevelString, String logMessage) {
		if (logger==null)
			System.out.println("["+logLevelString+"]:"+logMessage);
		else
			logger.log(logLevelString , logMessage);
	}
	
	public void setNativeLogger(NativeLogger logger) {
		this.logger = logger;
	}
	
    /**
     * @return Returns the firstHeader.
     */
    public Header getFirstHeader()
    {
        return firstHeader;
    }
    /**
     * @param firstHeader The firstHeader to set.
     */
    public void setFirstHeader(Header firstHeader)
    {
        this.firstHeader = firstHeader;
    }
}
