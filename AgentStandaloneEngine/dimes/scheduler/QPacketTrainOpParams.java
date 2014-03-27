/**
 * 
 */
package dimes.scheduler;

import java.util.LinkedList;

import dimes.measurements.basicmeasurements.MeasurementCycle;

/**
 * @author Ohad Serfaty
 *
 */
public class QPacketTrainOpParams {
	
	public final int iterations;
	public LinkedList<QPacketInTrain> packetsVector = new LinkedList<QPacketInTrain>();
	public final long delayBetweenPT;
	public final int numberOfPacketsInTrain;
	
	private LinkedList<MeasurementCycle> results;
	
	public QPacketTrainOpParams(int iterations , long delayBetweenPT, int numberOfPacketsInTrain ){
		this.delayBetweenPT = delayBetweenPT;
		this.iterations = iterations;
		this.numberOfPacketsInTrain = numberOfPacketsInTrain;
	}
	
	public void addPacket(QPacketInTrain packet){
		this.packetsVector.addLast(packet);
	}
	
	public void setResults(LinkedList<MeasurementCycle> resList) {
		results = resList;
	}
	
	public LinkedList<MeasurementCycle> getResults() {
		return results;
	}
}
