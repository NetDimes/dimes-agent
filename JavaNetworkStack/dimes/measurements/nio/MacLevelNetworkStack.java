package dimes.measurements.nio;

//import java.nio.ByteBuffer;

import dimes.measurements.nio.error.MeasurementException;
import dimes.measurements.nio.error.MeasurementInitializationException;
import dimes.measurements.nio.packet.CodedPacketFilter;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.PacketBuffer;
//import dimes.measurements.nio.packet.PacketFinalizer;
import dimes.measurements.nio.packet.builder.MacLevelPacketBuilder;
import dimes.measurements.nio.packet.builder.PacketBuilder;
//import dimes.measurements.nio.packet.builder.StandardPacketBuilder;
import dimes.measurements.nio.packet.header.EthernetHeader;

/**
 * @author Ohad Serfaty
 *<p>
 * a network stack for the usage of mac level measurements
 *<p>
 * Please note that this class is a singleton and can be accessed only
 * threw the getInstance() function.
 */
public class MacLevelNetworkStack extends NetworkStack implements NativeLogger{

	/**
	 *  The singleton instance of the Mac Level network stack class :
	 */
	private static MacLevelNetworkStack instance;

	private MacLevelNetworkStack (){
		super();
	}
	
	/**
	 * Get an instance of the mac level stack
	 * 
	 * @return A singleton instance of a Mac level network stack
	 */
	public static MacLevelNetworkStack  getInstance(){
		if (instance == null)
			instance = new MacLevelNetworkStack ();
		return instance;
	}

	// The local packet builder :
	private PacketBuilder packetBuilder;

	public void init() throws MeasurementInitializationException {
		String ipAddress = PublicIPInquirer.queryMachineIP();
		System.err.println("init ipAddress " +ipAddress);
		
		init(new String[]{ipAddress});
		
		System.out.println("pppoe :" + this.isPPPoE());
	}

	public void init(String[] args) throws MeasurementInitializationException{
		init((Object[])args);
		System.err.println("this.getPPPoEHeader()  "+this.getPPPoEHeader());
		EthernetHeader ethernetHeader = new EthernetHeader(this.getSourceMac() , this.getDestMac() , this.getPPPoEHeader());
		this.setFirstHeader(ethernetHeader);
		packetBuilder = new MacLevelPacketBuilder(this,ethernetHeader);
	}
	
	private native void init(Object[] args) throws MeasurementInitializationException;

	// Mac addresses :
	/**
	 * Get the source MAC address
	 * 
	 * @return source MAC address (6 bytes)
	 */
	public native byte[] getSourceMac();
	
	/**
	 * Get the dest MAC address
	 * 
	 * @return a default dest MAC address (6 bytes)
	 */
	public native byte[] getDestMac();
	
	/**
	 * Get the pppoe Header 
	 * 
	 * @return pppoe hedaer
	 */
	public native byte[] getPPPoEHeader();
	
	/**
	 * 
	 * @return true if the session is a pppoe session
	 */
	public native boolean isPPPoE();
	
	/**
	 * Register a packet to send in the native code stack.
	 * 
	 * @param sendTime time to send
	 * @param buffer packet bytes.
	 */
	private native void registerPacket(long sendTime, byte[] buffer , int timeStampPosition , int checkSumPosition);
	
	/**
	 * 
	 * Initialize packets.
	 * 
	 * @param buffer A Packet buffer with packets to initialize
	 * 
	 */
	public void initPackets(PacketBuffer buffer) throws MeasurementException {
		if (buffer==null)
			throw new MeasurementException("PacketBuffer null.");
		Packet[] orderedBufferPackets = buffer.getOrderedPackets();
		for (int i=0; i<orderedBufferPackets.length; i++)
		{
			System.out.println("Registering packet length :" +orderedBufferPackets[i].toByteArray().length + " stamp pos :" +orderedBufferPackets[i].timeStampPosition);
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

	/* (non-Javadoc)
	 * @see dimes.measurements.nio.NetworkStack#sendPacket(byte[], int, int)
	 */
	public native long sendPacket(byte[] packet, int timestampPosition , int checksumPosition);

	public long send(Packet packet, long sendTime) throws MeasurementException {
		if (packet==null)
			throw new MeasurementException("PacketBuffer is null");
		return sendTimedPacket(packet.toByteArray() , sendTime);
	}

	public long[] send(PacketBuffer buffer) throws MeasurementException {
		this.initPackets(buffer);
		return send();
	}
	
	public native void setCallbackBufferSize(int size);
	
	public native void receive(long listenTimeout, PacketBuffer buffer, CallbackContext context) throws MeasurementException;

	/**
	 * <p>
	 * Set a coded Packet filter. Packet filters are bpf programs (Berkeley
	 * Packet Filter ) , which are generated by a tcpdump or windump application
	 * 
	 * 
	 * @param filterPattern a filter pattern int[][] array
	 */
	public native void setPacketFilter(int[][] filterPattern);
	
	/**
	 * <p>
	 * Open the listener for incoming packets. Use this function
	 *  before you use the receive() function to mark the point
	 *  where the stack is open for packets.
	 */
	public native void openListener();
	
	/**
	 * Set a coded Packet filter. Packet filters are bpf programs (Berkeley
	 * Packet Filter ) , which are generated by a tcpdump or windump application
	 * 
	 * @param filter a pre made filter
	 * 
	 * @see CodedPacketFilter
	 * 
	 */
	public void setPacketFilter(CodedPacketFilter filter){
		setPacketFilter(filter.filterDescription);
	}
	
	public native void close();

	public PacketBuilder getPacketBuilder() {
		return packetBuilder;
	}

		
}
