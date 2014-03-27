package dimes.measurements.nio.testing;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import junit.framework.TestCase;
import dimes.measurements.nio.CallbackContext;
import dimes.measurements.nio.RawNetworkStack;
import dimes.measurements.nio.error.MeasurementException;
import dimes.measurements.nio.error.MeasurementInitializationException;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.PacketBuffer;
import dimes.measurements.nio.packet.builder.PacketBuilder;
import dimes.measurements.nio.packet.header.Header;
import dimes.measurements.nio.packet.header.ICMPv4Header;
import dimes.measurements.nio.packet.header.IPv4Header;
import dimes.measurements.nio.platform.NetworkStackLibraryLoader;

/**
 * @author Ohad Serfaty
 *
 */
public class RawNetworkStackTest extends TestCase {
	
	RawNetworkStack rawStack;
	private PacketBuilder builder;
	
	public RawNetworkStackTest(){
		NetworkStackLibraryLoader.loadLibrary();
		System.out.println("RawNetworkStack getInstance RawNetworkStackTest");
	}

	protected void setUp() throws Exception {
		super.setUp();
		rawStack = RawNetworkStack.getInstance();
		builder = rawStack.getPacketBuilder();
	}
	
	public void testInit(){
		try 
		{
			rawStack.init();
			rawStack.close();	
			assertTrue(true);
		}
		catch (MeasurementInitializationException e) 
		{
			e.printStackTrace();
			fail(" Failed with Measurement exception :" + e.getMessage());
		}
		catch (Exception ex)
		{
			fail("Failed with a General Exception :" + ex.getMessage());
		}		
	}
	
	public void testInitFailure(){
		try 
		{
			rawStack.initPackets(null);
		}
		catch (MeasurementException e) 
		{
			System.err.println("uninitialized buffer success 1.");
			assertTrue(true);
		}
		
		PacketBuffer buffer = new PacketBuffer(rawStack);
		try 
		{
			rawStack.send(buffer);
		}
		catch (MeasurementException e) 
		{
			System.err.println("uninitialized send success 2.");
			assertTrue(true);
		}
		try 
		{
			rawStack.send(null);
		}
		catch (MeasurementException e) 
		{
			System.err.println("uninitialized buffer send success 1.");
			assertTrue(true);
		}
		try 
		{
			rawStack.send(null,0);
		}
		catch (MeasurementException e) 
		{
			System.err.println("uninitialized buffer send success 2.");
			assertTrue(true);
		}
		
		try 
		{
			rawStack.receive(1000,buffer,null);
		}
		catch (MeasurementException e) 
		{
			System.err.println("uninitialized receives uccess 1.");
			assertTrue(true);
		}
		
		try 
		{
			rawStack.receive(1000,null,new EmptyContext());
		}
		catch (MeasurementException e) 
		{
			System.err.println("uninitialized receives success 2.");
			assertTrue(true);
		}
		
		try 
		{
			rawStack.receive(1000,null,null);
		}
		catch (MeasurementException e) 
		{
			System.err.println("uninitialized receives success 3.");
			assertTrue(true);
		}
		
		try 
		{
			rawStack.init();
			rawStack.init();
			fail("Raw stack initialized without closing.");
		}
		catch (MeasurementInitializationException e) 
		{
			System.err.println("double init test success 3.");
			assertTrue(true);
		}
		finally
		{		
			rawStack.close();
		}
		System.err.println("Close test success.");		
	}

	public void testSendPacket() throws UnknownHostException{
		try 
		{
			IPv4Header ipHeader = new IPv4Header();
			ipHeader.sourceIP = InetAddress.getLocalHost(); 
			ipHeader.destIP = InetAddress.getByName("132.66.48.22");
			ICMPv4Header icmpHeader = new ICMPv4Header();
			icmpHeader.setEchoRequest(132,145);
			
			rawStack.init();			
			System.out.println("building packet...");
			Packet p = builder.buildPacket( new Header[]{ ipHeader , icmpHeader});
			System.out.println("sending packet...");
			rawStack.send(p,1000);
			PacketBuffer buffer = new PacketBuffer(rawStack);
			System.out.println("Listening for packets to return...");
			rawStack.receive(1000,buffer);
			assertFalse(buffer.isEmpty());
			for (Iterator i = buffer.iterator(); i.hasNext();){
				Packet receivedPacket = (Packet) i.next();
				System.out.println(receivedPacket);
				Header receivedIcmpHeader = receivedPacket.getHeaderById(ICMPv4Header.IPv4_PROTO_ICMP);
				assertEquals(receivedIcmpHeader.getClass() , ICMPv4Header.class);
				assertEquals( ((ICMPv4Header)receivedIcmpHeader).code , ICMPv4Header.ICMP_ECHO_REPLY);
			}
			
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
		finally
		{
			rawStack.close();
		}
	}
//	
//	public void testSendBatch(){
//		try 
//		{
//			rawStack.init();
//			PacketBuffer buffer = new PacketBuffer();
//			int numPackets =10;
//			for (int i=0; i<numPackets; i++)
//			{
//				Packet p = builder.buildPacket( ipDesc, icmpDesc);
//				p.microSecTimestamp = i*1000;
//				buffer.add(p);
//			}
//			assertEquals(buffer.size(),numPackets);
//			long[] sendTimes = rawStack.send(buffer);
//			for (int i=1; i<numPackets; i++)
//			{
//				// accept only a minute 
//				long timeDiff = sendTimes[i]-sendTimes[i-1];
//				System.out.println("time diff :" + timeDiff);
//				assertTrue(timeDiff < 1050000L && timeDiff > 9500L);
//			}
//			System.err.println("Send buffered packets in times success.");
//		}
//		catch (MeasurementInitializationException e) 
//		{
//			fail(e.getMessage());
//		}
//		catch (MeasurementException e) 
//		{
//			fail(e.getMessage());
//		}
//		finally
//		{
//			rawStack.close();
//		}
//	}
//
//	public void testSendBackToBack(){
//		try 
//		{
//			rawStack.init();
//			PacketBuffer buffer = new PacketBuffer();
//			int numPackets = 10;
//			for (int i=0; i<numPackets; i++)
//			{
//				Packet p = builder.buildPacket( ipDesc, icmpDesc);
//				p.setSendTime(1000);
//				buffer.add(p);
//			}
//			assertEquals(buffer.size(),10);
//			long[] sendTimes = rawStack.send(buffer);
//			for (int i=1; i<numPackets; i++)
//			{
//				// accept only a minute 
//				long timeDiff = sendTimes[i]-sendTimes[i-1];
//				assertTrue(timeDiff < 25000L && timeDiff >= 0L);
//			}
//			System.err.println("Send buffered packets in times success.");
//		}
//		catch (MeasurementInitializationException e) 
//		{
//			fail(e.getMessage());
//		}
//		catch (MeasurementException e) 
//		{
//			fail(e.getMessage());
//		}
//		finally
//		{
//			rawStack.close();
//		}
//		
//	}
	 
	
	private class EmptyContext implements CallbackContext{

		public boolean callback() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean callback(byte[] p, long milisecReceiveTime, long microsecReceiveTime) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
}
