package dimes.scheduler;

import java.nio.ByteBuffer;

public class QPacketInTrain {
	private static short UNIQUE_LONG = 0x1ead ; //0x0123456789ABCDEFL;
	
	public final String IP;
	public final short packetSize;
	public final short packetTos;
	public final short protocol;
	public final short sourcePort;
	public final short destPort;
	public final short packetTTL;
	public final short trainsize;
	public short trainId;
	public final short sequenceNumber;
	private int agentIndex;
	
	public static int PAYLOAD_SIZE_NO_TS = /*(short)*/2*8+ /*(int)*/4*2 ;	
	public static int PAYLOAD_SIZE = PAYLOAD_SIZE_NO_TS+/*(long)*/8*1;

	
	public QPacketInTrain (String IP , short packetSize , short packetTos ,short protocol ,short sourcePort ,
			short destPort ,short packetTTL , short sequenceNumber,short trainsize){
		this.IP = IP;
		this.packetSize = 60; //packetSize;
		this.packetTos = packetTos;
		this.protocol = protocol;
		this.sourcePort = sourcePort;
		this.destPort = destPort;
		this.packetTTL = packetTTL;
		this.sequenceNumber = sequenceNumber;
		this.trainsize =trainsize;
	}
	
	public void setTrainId(short trainId){
		this.trainId = trainId;
	}
	
	public void setAgentIndex(int agentIndex){
		this.agentIndex = agentIndex;
	}
	
	public byte[] getProprietary(int experimentUniqueId) {
		/*System.out.println("buffer.putInt((this.agentIndex)"+(this.agentIndex)+"\n"+
		"packetTos"+this.packetTos+"\n"+
		"buffer.putChar((char)protocol"+this.protocol+"\n"+
		"buffer.putChar((char)TTL"+this.packetTTL+"\n"+
		"buffer.putShort packetsize"+(this.packetSize)+"\n"+
		"buffer.putShort(trainid" + this.trainId +"\n"+
		"buffer.putShort sequenceNumber"+(this.sequenceNumber)+"\n"+
		"buffer.putInt experimentUniqueId"+(experimentUniqueId)+"\n"+
		"buffer.putShorttrainsize"+(trainsize)+"\n"+
		"buffer.putLong("+UNIQUE_LONG+"\n");
		*/
		ByteBuffer buffer = ByteBuffer.allocate(PAYLOAD_SIZE_NO_TS);
		buffer.putInt(this.agentIndex);
		buffer.putShort(this.packetTos);
		buffer.putShort(this.protocol);
		buffer.putShort(this.packetTTL);
		buffer.putShort(this.packetSize);
		buffer.putShort(this.trainId);
		buffer.putShort(this.sequenceNumber);
		buffer.putInt(experimentUniqueId);
		buffer.putShort(trainsize);
		buffer.putShort(UNIQUE_LONG);
		return buffer.array();
	}
	
	public String toString(){
		return "Packet Dest:"+this.IP;
	}
}