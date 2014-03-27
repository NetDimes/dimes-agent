package dimes.measurements.operation;

import java.util.LinkedList;

import dimes.measurements.Measurements;
import dimes.scheduler.QPacketTrainOpParams;
import dimes.scheduler.Task;

/*
 * Created on 16/06/2005
 */

/**
 * @author steger
 */
public class QbeOp extends MeasurementOp
{

	public final QPacketTrainOpParams[] qptCommands;
	public final Integer[] sleepPeriods;
	public final int agentIndex;
	public final int experimentUniqueId;

	public QbeOp(LinkedList aQptCommandsList, LinkedList aSleepPeriodsList, int agentIndex , int experimentUniqueId, Task task)
	{
		super(task);
		this.experimentUniqueId = experimentUniqueId;
		this.agentIndex = Measurements.getAgentIndex();
		this.qptCommands = (QPacketTrainOpParams[]) aQptCommandsList.toArray(new QPacketTrainOpParams[]{});
		this.sleepPeriods = (Integer[]) aSleepPeriodsList.toArray(new Integer[]{});
	}

	public String toString()
	{
		String commandsString = "";
//		for (Iterator i = qptCommandsList.iterator(); i.hasNext();){
//			QPacketTrainOpParams params = (QPacketTrainOpParams) i.next();
//			commandsString+="QBE-Send ...";
//		}
		return "QBE-Start\n" + 
		commandsString
		+"QBE-Stop";
//		String iplist = new String();
//		for (int i = 0; i < this.ipList.length; i++)
//		{
//			iplist = iplist.concat(this.ipList[i] + " ");
//		}
//		return MeasurementType.getName(MeasurementType.PACKETTRAIN) + " " + this.numberOfRobins + " " + this.delay_usec + " " + this.packetsize + " "
//				+ Protocol.getName(this.protocol) + " " + this.port + " " + iplist;
	}

	public boolean execute()
	{
		return Measurements.execute(this);
	}

	/* (non-Javadoc)
	 * @see dimes.measurements.Operation#isTimedOperation()
	 */
	public boolean isTimedOperation()
	{
		return false;
	}
	
	public int getMaxTrainSize() {
		int max = 0;
		for (int i=0; i<qptCommands.length; i++) {
			QPacketTrainOpParams op = qptCommands[i];
			if (op.numberOfPacketsInTrain > max) {
				max = op.numberOfPacketsInTrain;
			}
		}
		return max;
	}
}