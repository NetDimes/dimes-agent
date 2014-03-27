package dimes.measurements.nio;

import dimes.measurements.nio.error.MeasurementException;
import dimes.measurements.nio.error.MeasurementInitializationException;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.PacketBuffer;
import dimes.measurements.nio.packet.builder.PacketBuilder;
import dimes.measurements.nio.packet.builder.StandardPacketBuilder;
import dimes.measurements.nio.packet.header.IPv4Header;

/**
 * @author Ohad Serfaty
 *
 * a raw socket network stack for the usage of IP-V4
 * Please note that this class is a singleton and can be accessed only
 * threw the getInstance() function.
 *  
 */
public class RawNetworkStack extends NetworkStack implements NativeLogger{

	/**
	 *  The singleton instance of the Raw network stack class :
	 */
	private static RawNetworkStack instance;

	private RawNetworkStack(){
		super();
	}
	
	public static RawNetworkStack getInstance(){
		System.out.println("RawNetworkStack getInstance");
		if (instance == null)
			instance = new RawNetworkStack();
		return instance;
	}
	
	// IPv4 uses a standard Packet builder :
	private PacketBuilder packetBuilder = new StandardPacketBuilder(this);

	public void init() throws MeasurementInitializationException {
		//System.out.println("RawNetworkStack init");
		String ipAddress = PublicIPInquirer.queryMachineIP();
		init(new String[]{ipAddress});
	}

	public void init(String[] args) throws MeasurementInitializationException {
		//System.out.println("RawNetworkStack init-- String[] args");
		this.init((Object[])args);
		this.setFirstHeader(new IPv4Header());
	}
	
	private native void init(Object[] args) throws MeasurementInitializationException;

	/**
	 * Register a packet to send in the native code stack.
	 * 
	 * @param sendTime time to send
	 * @param buffer packet bytes.
	 */
	private native void registerPacket(long sendTime, byte[] buffer , int timestampPosition , int recalculateChecksum);
	
	
	/**
	 * 
	 * Initialize the sender with a packet buffer
	 * 
	 */
	public void initPackets(PacketBuffer buffer) throws MeasurementException {
		//System.out.println("RawNetworkStack initPackets");
		if (buffer==null)
			throw new MeasurementException("PacketBuffer null.");
		Packet[] orderedBufferPackets = buffer.getOrderedPackets();
		for (int i=0; i<orderedBufferPackets.length; i++)
		{
			this.registerPacket(orderedBufferPackets[i].microSecTimestamp , orderedBufferPackets[i].toByteArray() , 
					orderedBufferPackets[i].timeStampPosition , orderedBufferPackets[i].checkSumPosition);
		}
	}

	/**
	 * send the packets that are stacked in the native code.
	 * 
	 * @return actual send time
	 * @throws MeasurementException
	 */
	public native long[] send() throws MeasurementException ;
	
	
	/**
	 * send a packet on time
	 * 
	 * @param packet packet to send
	 * @param sendTime relative time
	 * @return actual send time
	 * 
	 * @throws MeasurementException
	 */
	public native long sendTimedPacket(byte[] packet , long sendTime) throws MeasurementException ;	
	public native long sendPacket(byte[] packet, int timestampPosition , int checksumPosition);

	public long send(Packet packet, long sendTime) throws MeasurementException {
		//System.out.println("RawNetworkStack send (Packet packet, long sendTime)"); 
		if (packet==null)
			throw new MeasurementException("PacketBuffer is null");
		long time = sendTimedPacket(packet.toByteArray() , sendTime);
		packet.setSendTime(time);
		return time; //sendTimedPacket(packet.toByteArray() , sendTime);
	}

	public long[] send(PacketBuffer buffer) throws MeasurementException {
		//System.out.println("RawNetworkStack sendbuffer");
		this.initPackets(buffer);
		return send();
	}
	
	public native void setCallbackBufferSize(int size);
	
	public native void receive(long listenTimeout, PacketBuffer buffer, CallbackContext context) throws MeasurementException;

	public PacketBuilder getPacketBuilder() {
		return packetBuilder;
	}
	
//	public void log(String logLevelString, String logMessage) {
//		if (logger==null)
//			System.out.println("["+logLevelString+"]:"+logMessage);
//		else
//			logger.log(logLevelString , logMessage);
//	}

	public native void close();

		
}
