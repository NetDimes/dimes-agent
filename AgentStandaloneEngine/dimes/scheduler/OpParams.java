/*
 * Created on 26/02/2004
 */
package dimes.scheduler;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

import dimes.measurements.MeasurementType;
import dimes.measurements.Protocol;
import dimes.measurements.operation.TracerouteOp;
//import dimes.util.update.UpdateOpParams;

/**
 * @author anat
 */
public class OpParams
{
	private static final Random rand = new Random();
	public int uniqueID = rand.nextInt();
	private static final long DEFAULT_MEASUREMENT_TIMEOUT = 30000;
	
	//	public final String type;
	public final int type; //check
	public final String hostIP;
	//	public final String protocol;
	public final int protocol; //check
	public short sourcePort;
	public short destPort;
	// steger -- packettrain needs more parameters
	public int numberOfRobins;
	public int delay_usec;
	public int packetsize;
	public String[] ipList;
	// treeroute ? Should we transfer this to a seperate class ?
	public int treerouteRole=-1;
	public String peerAgentId;
	public String peerAgentIP=null;
	public Calendar measurementTime;
	private boolean timedOperation=false;
	private long measurementTimeOut=DEFAULT_MEASUREMENT_TIMEOUT;
//	 ignore incapability of treeroute...
	public boolean overrideTreerouteCapability=true;
	
	// QBE Parameters :
	public LinkedList qptCommands;
	public LinkedList sleepPeriods;
	//UPDATE Parameters
//	public LinkedList<UpdateOpParams> updateCommands = null;
	private int experimentUniqueId;	

	public OpParams(String aType, String anIP, String aProtocol, short aSourcePort, short aDestPort)
	{
		// steger -- some parameters are needed only by packettrain
		numberOfRobins = 0;
		delay_usec = 0;
		packetsize = 0;
		ipList = null;

		if (aType.equalsIgnoreCase("TRACEROUTE"))
			type = MeasurementType.TRACEROUTE;
		else
			if (aType.equalsIgnoreCase(("PARISTRACEROUTE")))
				type = MeasurementType.PARIS_TRACEROUTE;
/*			else
				if (aType.equalsIgnoreCase(("TREEROUTE")))
				{
					type = MeasurementType.TREEROUTE;
					this.timedOperation = true;
				}
				else
			if (aType.equalsIgnoreCase(("PING")))
				type = MeasurementType.PING;*/
			else
				if (aType.equalsIgnoreCase(("QBE")))
					type = MeasurementType.QBE;
				else if(aType.equalsIgnoreCase("UPDATE"))
				{
					type = MeasurementType.UPDATE;
				} else
					{
					type = MeasurementType.PING; //default
					}
			

		hostIP = anIP;

		if (aProtocol.equalsIgnoreCase("UDP"))
			protocol = Protocol.UDP;
		else
			if (aProtocol.equalsIgnoreCase("ICMP"))
				protocol = Protocol.ICMP;
			else
			{
				protocol = Protocol.getDefault();
			}
//This test now performed in Parser
//		if (aSourcePort == -1)
//			sourcePort = TracerouteOp.sourcePort;
//		else
//			sourcePort = aSourcePort;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return MeasurementType.getName(this.type) + " " + this.hostIP + " " + Protocol.getName(this.protocol)
		+(timedOperation?" TimeOut : "+(this.measurementTime.getTimeInMillis() + this.measurementTimeOut):"");
	}

	/**
	 * @return
	 */
	public boolean isTimedOperation()
	{
		return timedOperation;
	}
		
	/**
	 * @param currentServerTime
	 * @return
	 */
	public boolean operationTimedOut(long currentServerTime)
	{
		if (!this.isTimedOperation())   // it's always the time for those...
			return false;
		long measurementTimeOut = this.measurementTime.getTimeInMillis() + this.measurementTimeOut;
		return currentServerTime > measurementTimeOut ;
	}

	/**
	 * check whether it is time to execute the current operation (parameters)
	 * 
	 * @param currentServerTime
	 * @return
	 */
	public boolean isExecutionTime(long currentServerTime)
	{
		if (!this.isTimedOperation())	// it's always the time for those...
			return true;
		return (currentServerTime >=  this.measurementTime.getTimeInMillis() 
		&& (currentServerTime <= this.measurementTime.getTimeInMillis()+ this.measurementTimeOut));
	}

	public boolean getOverride() {
		return overrideTreerouteCapability;
	}

	public void setQPTCommands(LinkedList qptCommands) {
		this.qptCommands = qptCommands;
	}
	
//	public void setUpdateCommands(LinkedList<UpdateOpParams> up){
//		updateCommands=up;
//	}

	public void setSleepPeriods(LinkedList sleepPeriods) {
		this.sleepPeriods = sleepPeriods;
	}

	public void setExperimentUniqueId(int experimentUniqueId) {
		this.experimentUniqueId = experimentUniqueId;
	}
	
	public int getExperimentUniqueId(){
		return this.experimentUniqueId;
	}


}
