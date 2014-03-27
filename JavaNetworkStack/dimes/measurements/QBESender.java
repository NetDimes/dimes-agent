/**
 * 
 */
package dimes.measurements;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import dimes.measurements.nio.MacLevelNetworkStack;
import dimes.measurements.nio.error.MeasurementException;
import dimes.measurements.nio.error.MeasurementInitializationException;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.PacketBuffer;
import dimes.measurements.nio.packet.builder.PacketBuilder;
import dimes.measurements.nio.packet.header.Header;
import dimes.measurements.nio.packet.header.IPv4Header;
import dimes.measurements.nio.packet.header.Payload;
import dimes.measurements.nio.packet.header.TCPHeader;
import dimes.measurements.nio.packet.header.UDPHeader;
import dimes.measurements.nio.platform.NetworkStackLibraryLoader;

/**
 * @author Ohad Serfaty
 * An implementation of the QBE sender measurement module
 * 
 * Parameters are : 
 * a series of qpt commands to send packets
 * a series of wait commands to wait for a certain time
 * 
 * QPT arguments :
 * QPT <Iterations> <PacketTrainSize> <DelayBetweenPackets> <PerPacketDetails> <Train>
 * PerPacketDetails (comma seperated ): [Packet sizes] [protocols] [tos] [ttls] 
 * Train : [ IP1 , IP2 ,...]
 *
 *
 */
public class QBESender {

	
	private MacLevelNetworkStack networkStack;
	private PacketBuilder packetBuilder;

	public QBESender() throws MeasurementInitializationException{
		networkStack = MacLevelNetworkStack.getInstance();
		networkStack.init();
		packetBuilder = networkStack.getPacketBuilder();
	}

	public void sendPackets(QbeOp qbeOperation) throws Exception{
		if (qbeOperation.qptCommands.length-1 != qbeOperation.sleepPeriods.length)
			throw new MeasurementException("Sleep periods don't match with number of QPT commands.");
		
		int commandsNumber = qbeOperation.qptCommands.length;
		InetAddress sourceIP = InetAddress.getLocalHost();
		// do iterations of QPT : 
		for (int i=0; i<commandsNumber; i++){
			QPacketTrain qpt = qbeOperation.qptCommands[i];
			// build the packets :
			int iterations = qpt.iterations;
			for (int k = 0; k<iterations;k++)
			{
			PacketBuffer buffer = new PacketBuffer(networkStack);
			System.out.println("Vector size :" + qpt.packetsVector.size());
			for (int j=0; j<qpt.packetsVector.size(); j++)
			{
				IPv4Header ipHeader = new IPv4Header();
				UDPHeader udpHeader = new UDPHeader();
				
				QPacketInTrain packet = (QPacketInTrain) qpt.packetsVector.get(j);
				packet.setTrainId(k);
				
				
				
				Payload payload = new Payload();
				byte[] proprietary = packet.getProprietary();
				byte[] random = new byte[packet.packetSize - 28 - proprietary.length];
				Arrays.fill(random , (byte)0);
				payload.addLoad(proprietary);
				payload.addLoad(random);
				
				Packet packetToAdd = null;
				ipHeader.sourceIP = sourceIP;
				ipHeader.destIP = InetAddress.getByName(packet.IP);
				ipHeader.tos= (byte) packet.packetTos;
				ipHeader.ttl= (byte) packet.packetTTL;
				if (packet.protocol == UDPHeader.IP_PROTO_UDP)
				{
					udpHeader.sourcePort = (short) packet.sourcePort;
					udpHeader.destPort = (short) packet.destPort;
					packetToAdd = packetBuilder.buildPacket(new Header[]{ipHeader , udpHeader,payload});
					packetToAdd.timeStampPosition = 14+20+8+proprietary.length;
				}
				if (packet.protocol == TCPHeader.IP_PROTOCOL_TCP){
				}
				System.out.println("Adding packet :" + packetToAdd);
				buffer.addPacket(0,packetToAdd);
			}
			//System.out.println("Buffer : " + buffer);
			// send the packets :
			
				System.out.println("sending...");
				this.networkStack.send(buffer);
				System.out.println("Sleeping for " +qpt.delayBetweenIterations );
				if (k<iterations)
					Thread.sleep(qpt.delayBetweenIterations);
			}
			
			// sleep for the time that was scheduled :
			if (i<commandsNumber-1)
				Thread.sleep(qbeOperation.sleepPeriods[i]);
		}
		networkStack.close();
	}
	
	private static final long SEED = 789234;
	
	private static String getRandomIP(Random rand){
		int[] array = new int[4];
		for (int i=0; i<4 ; i++)
			array[i] = rand.nextInt(255);
		return array[0]+"."+array[1]+"."+array[2]+"."+array[3];
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		NetworkStackLibraryLoader.loadLibrary();
		QbeOp operation = new QbeOp((short) 190);
		int trainsNumber = 100;
		QPacketTrain[] qptCommands = new QPacketTrain[trainsNumber];
		int[] sleepPeriods = new int[trainsNumber-1]; 
		Random rand = new Random(SEED);
		for (int i=0; i<trainsNumber; i++){
			int iterations = rand.nextInt(100);
			long delayBetweenIterations = 100+rand.nextInt(100);
			LinkedList packetVector = new LinkedList();
			int packetVectorSize = 1+rand.nextInt(10);
			QPacketTrain packetTrain = new QPacketTrain(operation , iterations , delayBetweenIterations);

			for (int j=0; j<packetVectorSize; j++)
			{
				short packetSize = (short) 100;//(20+8+rand.nextInt(100));
				short ttl = 100;
				short tos = 3;
				short sourcePort = 10000;
				short destPort = 7777;
				QPacketInTrain aPacket = new QPacketInTrain(/*getRandomIP(rand)*/"132.66.53.157" ,packetSize , tos 
						, UDPHeader.IP_PROTO_UDP ,  
				 		sourcePort , destPort , ttl , packetTrain , j);
				packetVector.add(aPacket);
			}
			//System.out.println("Packet vector :" + packetVector);
			packetTrain.setPacketsVector(packetVector);
			qptCommands[i] = packetTrain;
			if (i<trainsNumber-1)
				sleepPeriods[i] = 1000+rand.nextInt(3000); 
		}
		operation.setQPTCommands(qptCommands);
		operation.setSleepPeriods(sleepPeriods);
		
		QBESender sender = new QBESender();
		sender.sendPackets(operation);
	}

	private static class QbeOp {
		
		
		public QPacketTrain[] qptCommands;
		short agentIndex;
		public int[] sleepPeriods;
		
		public QbeOp(short agentIndex) {
			this.agentIndex = agentIndex;
		}
		
		public void setQPTCommands( QPacketTrain[] qptCommands){
			this.qptCommands = qptCommands;
		}
		
		public void setSleepPeriods( int[] sleepPeriods){
			this.sleepPeriods = sleepPeriods;
		}
		
	}
	
	private static class QPacketTrain {

		public final QbeOp operation;
		public final int iterations;
		public final long delayBetweenIterations;
		public LinkedList packetsVector;

		public QPacketTrain(QbeOp operation, int iterations, long delayBetweenIterations) {
			this.operation = operation;
			this.iterations = iterations;
			this.delayBetweenIterations = delayBetweenIterations;
		}
		
		public void setPacketsVector(LinkedList packetsVector){
			this.packetsVector = packetsVector;
		}
		
		
	}

	private static class QPacketInTrain {
		
		public final String IP;
		public final short packetSize;
		public final short packetTos;
		public final short protocol;
		public final short sourcePort;
		public final short destPort;
		public final short packetTTL;
		public final QPacketTrain train;
		public int trainId;
		public final int sequenceNumber;

		public QPacketInTrain (String IP , short packetSize , short packetTos ,short protocol ,short sourcePort ,
				short destPort ,short packetTTL , QPacketTrain train  , int sequenceNumber){
			this.IP = IP;
			this.packetSize = packetSize;
			this.packetTos = packetTos;
			this.protocol = protocol;
			this.sourcePort = sourcePort;
			this.destPort = destPort;
			this.packetTTL = packetTTL;
			this.train = train;
			this.sequenceNumber = sequenceNumber;
		}
		
		public void setTrainId(int trainId){
			this.trainId = trainId;
		}
		
		public byte[] getProprietary() {
			ByteBuffer buffer = ByteBuffer.allocate(16);
			buffer.putShort(this.train.operation.agentIndex);
//			buffer.putShort(this.train.operation.expId);
//			buffer.putShort(this.train.operation.runId);
			buffer.putShort(this.packetTos);
			buffer.putShort(this.protocol);
			buffer.putShort(this.packetTTL);
			buffer.putInt(this.trainId);
			buffer.putInt(this.sequenceNumber);
			return buffer.array();
		}
		
		public String toString(){
			return "Packet Dest:"+this.IP;
		}
	}
	
}
