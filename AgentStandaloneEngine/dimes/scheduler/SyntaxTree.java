/*
 * Created on 22/01/2004
 * updated 9/2009, BoazH version 0.6
 */
package dimes.scheduler;

import java.util.Iterator;
import java.util.LinkedList;
/*import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;*/
import dimes.measurements.MeasurementType;
import dimes.measurements.operation.Operation;
//import dimes.measurements.operation.PackettrainOp;
//import dimes.measurements.operation.PeerPacketTrainOp;
import dimes.measurements.operation.ParisTracerouteOp;
import dimes.measurements.operation.PingOp;
import dimes.measurements.operation.QbeOp;
import dimes.measurements.operation.TracerouteOp;
//import dimes.measurements.operation.TreerouteOp;
/*import dimes.measurements.newOps.PacketTrain;
import dimes.measurements.newOps.TrainMeasurementOp;*/
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.time.TimeSynchronizationManager;
//import dimes.util.update.UpdateOp;

/**
 * @author anat
 */
//todo - should not really extend LinkedList
public class SyntaxTree 
{
	Task containingTask = null;

	/* next 2 variables are used so that the Task progresses only if its 
	 * Operation was chosen to be executed by the TaskManager.
	 * If the Operation was executed, <valid> will be false.
	 */
	private Operation nextOp = null;
	private OpParams nextOpParamseters=null;
	private boolean valid = false;
	private int doneOperations=0;
	private LinkedList<OpParams> thisList = new LinkedList<OpParams>(); //<----- This class originally extended LinkedList. The list has now been moved into a variable. If this thing crashed, it means I forgot to change 'this' reference to 'thisList' somewhere. 
	int agentIndex=190;
	
	//Agent ID doesn't actually exist yet. This throws an exception 21.9.09-boazh
/*	public SyntaxTree(){
		try {
			agentIndex = Integer.parseInt(PropertiesBean.getProperty(PropertiesNames.AGENT_ID));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NoSuchPropertyException e) {
			e.printStackTrace();
		}
	}
	*/
	Operation getNextOp()
	{
		if (isValid(thisList))
		{
			return this.nextOp;
		}
		if (thisList.isEmpty())
			return null;
		// Find the next timed operation :
		for (Iterator<OpParams> i = thisList.iterator(); i.hasNext();){
			OpParams operationParameters = (OpParams) i.next();
			if (operationParameters.isTimedOperation())
			{
				// Remove timed out operations :
				long currentServerTime = TimeSynchronizationManager.getDimesServerTime();
				
				if (operationParameters.operationTimedOut(currentServerTime))
				{
					Loggers.getLogger().warning(" "+currentServerTime+": Operation timed out.Removing Operation from queue :" + operationParameters);
					this.doneOperations++;
					i.remove();
				}
				else
				{
					if (operationParameters.isExecutionTime(currentServerTime))
					{
						return this.createOperation(operationParameters);
					}
				}
			}
		}
		
		for (Iterator<OpParams> i = thisList.iterator(); i.hasNext();){
			OpParams operationParameters = (OpParams) i.next();
			if (!operationParameters.isTimedOperation()){
				return this.createOperation(operationParameters);
			}
		}
		// got nothing else to do yet ...
		return null;
	}

	public void reset(){
		thisList = new LinkedList<OpParams>();
		System.out.println("SystexTree Reset");
	}
	
	@SuppressWarnings("unchecked")
	public LinkedList<OpParams> getList(){
		return (LinkedList<OpParams>)thisList.clone();  //Clone, so that there is no cross-effect
	}
	
	private boolean isValid(LinkedList<OpParams> l) 
	{
		long currentServerTime = TimeSynchronizationManager.getDimesServerTime();
		return this.valid && nextOpParamseters!=null && !nextOpParamseters.operationTimedOut(currentServerTime);
	}

	private Operation createOperation(OpParams nextOpParams) {
		int type = nextOpParams.type;
		String ip = nextOpParams.hostIP;
		int protocol = nextOpParams.protocol;
		short sourcePort = nextOpParams.sourcePort;
		short destPort = nextOpParams.destPort;
		Operation result = null;
		switch (type)
		{
			case MeasurementType.PING :
				result = new PingOp(ip, protocol, sourcePort, destPort, this.getContainingTask());
				break;
			case MeasurementType.TRACEROUTE :
				result = new TracerouteOp(ip, protocol, sourcePort, destPort, this.getContainingTask());
				break;
			
			//No longer used. Removed 0.6 BoazH	
/*			case MeasurementType.PACKETTRAIN : // steger -- packettrain measurement added
				result = new PackettrainOp(nextOpParams.numberOfRobins, nextOpParams.delay_usec, nextOpParams.packetsize, protocol, initialPort,
						nextOpParams.ipList, this.getContainingTask());
				break;
			case MeasurementType.TREEROUTE :
				result = new TreerouteOp(nextOpParams.peerAgentId , ip , protocol, initialPort,
						nextOpParams.measurementTime.getTimeInMillis() , this.getContainingTask(), 
						nextOpParams.peerAgentIP,nextOpParams.treerouteRole);
				((TreerouteOp)result).setOverride(nextOpParams.getOverride());
				break;*/
				
			case MeasurementType.QBE :
				
				result = new QbeOp(nextOpParams.qptCommands , nextOpParams.sleepPeriods ,  agentIndex , nextOpParams.getExperimentUniqueId() , this.getContainingTask());
				break;
				
//			case MeasurementType.UPDATE:
//				result = new UpdateOp(nextOpParams.updateCommands, nextOpParams.sourcePort, this.getContainingTask());
//				break;
				// Peer PAcket train :
				//No longer used. Removed 0.6 BoazH	
/*			case MeasurementType.PEER_PACKET_TRAIN:
				result = new PeerPacketTrainOp(
						nextOpParams.peerAgentId ,nextOpParams.measurementTime.getTimeInMillis() ,
						nextOpParams.peerAgentIP,nextOpParams.treerouteRole , 
						nextOpParams.numberOfRobins, nextOpParams.delay_usec, 
						nextOpParams.packetsize, protocol, initialPort,
						nextOpParams.ipList, this.getContainingTask());
				break;*/
			//test, runs like QBE
//			case MeasurementType.NEW_OP:
//				result = new TrainMeasurementOp(nextOpParams.qptCommands , nextOpParams.sleepPeriods ,  agentIndex , nextOpParams.getExperimentUniqueId() , this.getContainingTask());
//				break;
			case MeasurementType.PARIS_TRACEROUTE:
				result = new ParisTracerouteOp(ip, protocol, sourcePort, destPort, this.getContainingTask());
				break;
				
			default :
				Loggers.getLogger().warning(" *********  operation type not known: " + type + " *********");//debug
		}
		System.out.println("Syntax tree: "+ip+" "+protocol+" "+sourcePort+"\n");
	//	result = new ParisTracerouteOp(ip, protocol, initialPort, this.getContainingTask());
		// create a match between operation and prameters :
		if (result!=null)
		{
			result.setUniqueID(nextOpParams.uniqueID);
			this.nextOp = result;
			this.valid = true;
			this.nextOpParamseters = nextOpParams;
		}
		return result;
	}

	/* used to inform this SyntaxTree that its next Operation was executed.
	 * results in the progressing of the Task
	 */
	void updateTakenOp(Operation op)
	{
		this.valid = false;
		this.nextOp = null;
		this.nextOpParamseters = null;
		for (Iterator<OpParams> i =thisList.iterator(); i.hasNext();){
			if (((OpParams)i.next()).uniqueID == op.getUniqueID()){
				i.remove();
			}
		}
		doneOperations++;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	//nextVer - better implementation
	public String toString()
	{
	
		String res = "Syntax Tree:\n";
		int counter=0;
		for (Iterator<OpParams> i =thisList.iterator(); i.hasNext();){
			res += "\t" + counter + ": " + i.next().toString() + "\n";
			counter++;
		}
		return res;
	}

	/**
	 * @return
	 */
	public Task getContainingTask()
	{
		return containingTask;
	}

	/**
	 * @param task
	 */
	public void setContainingTask(Task task)
	{
		containingTask = task;
	}

	public int getDoneOperations() {
		return doneOperations;
	}

	public boolean isEmpty(){
		return thisList.isEmpty();
	}

	public int size(){
		return thisList.size();
	}

	public void addLast(OpParams op){
		thisList.addLast(op);
	}
	
	public OpParams getCurrentOpParameters(){
		if(!thisList.isEmpty())
		return thisList.get(0);
		return null;
	}
}