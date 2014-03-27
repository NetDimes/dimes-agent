package dimes.measurements.nio;

import dimes.measurements.nio.error.MeasurementException;
import dimes.measurements.nio.error.MeasurementInitializationException;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.PacketBuffer;
import dimes.measurements.nio.packet.builder.PacketBuilder;
import dimes.measurements.nio.packet.header.IPv4Header;

/**
 * @author Ohad Serfaty
 *
 * a raw socket network stack for the usage of IP-V4
 *
 */
public class EtomicNetworkStack extends NetworkStack implements NativeLogger{

	/**
	 *  The singleton instance of the Mac Level network stack class :
	 */
	private static EtomicNetworkStack instance;

	private EtomicNetworkStack (){
		super();
		System.out.println("EtomicNetworkStack constructore");
	}
	
	/**
	 * Get an instance of the mac level stack
	 * 
	 * @return A singleton instance of a Mac level network stack
	 */
	public static EtomicNetworkStack  getInstance(){
		System.out.println("EtomicNetworkStack getInstance");
		if (instance == null)
			instance = new EtomicNetworkStack ();
		return instance;
	}
	
	
	// The local ip header builder :
	public static final String DEFAULT_DAG_CARD = "/dag/dag0";

	public void init() throws MeasurementInitializationException {
		System.out.println("EtomicNetworkStack init");
		String ipAddress = PublicIPInquirer.queryMachineIP();
		System.out.println("EtomicNetworkStack::init:-------------------------------------"+DEFAULT_DAG_CARD);
		init(new String[]{ipAddress,DEFAULT_DAG_CARD});
		this.setFirstHeader(new IPv4Header());
	}

	public native void init(String[] args) throws MeasurementInitializationException;

	/**
	 * Register a packet to send in the native code stack.
	 * 
	 * @param sendTime time to send
	 * @param buffer packet bytes.
	 */
	private native void registerPacket(long sendTime, byte[] buffer);
	
	public void initPackets(PacketBuffer buffer) throws MeasurementException {
//		if (buffer==null)
//			throw new MeasurementException("PacketBuffer null.");
//		Packet[] orderedBufferPackets = buffer.getOrderedPackets();
//		for (int i=0; i<orderedBufferPackets.length; i++)
//		{
//			this.registerPacket(orderedBufferPackets[i].microSecTimestamp , orderedBufferPackets[i].buffer);
//		}
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
	public native long sendPacket(byte[] packet);

	public long send(Packet packet, long sendTime) throws MeasurementException {
		System.out.println("EtomicNetworkStack send ??? do nothing");
		return 0;
	}

	public long[] send(PacketBuffer buffer) throws MeasurementException {
		System.out.println("EtomicNetworkStack send the initPackets");
		this.initPackets(buffer);
		return send();
	}
	
	public native void setCallbackBufferSize(int size);
	
	public native void receive(long listenTimeout, PacketBuffer buffer, CallbackContext context) throws MeasurementException;

	public void log(String logLevelString, String logMessage) {
		System.out.println("["+logLevelString+"]:"+logMessage);
	}

	public native void close();

	public long sendPacket(byte[] packet, int timestampPosition, int checkSumPosition) throws MeasurementException {
		// TODO Auto-generated method stub
		return 0;
	}

	public PacketBuilder getPacketBuilder() {
		// TODO Auto-generated method stub
		return null;
	}
	

		
}
