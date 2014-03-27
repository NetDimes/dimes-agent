package dimes.measurements.operation;

import dimes.measurements.MeasurementType;
import dimes.measurements.Measurements;
import dimes.measurements.Protocol;
import dimes.scheduler.Task;

/*
 * Created on 22/01/2004
 */

/**
 * @author anat
 */
public class PingOp extends MeasurementOp
{
	String hostIP;
	int protocol;
	short sourcePort;//meaningful only for UDP
	short destPort;
	/**
	 * @param task
	 */

	//nextVer - con'tors should contain more info
	/**
	 * @deprecated
	 */
//	public PingOp(String anIP, Task task)
//	{
//		super(task);
//		// TODO Auto-generated constructor stub
//		hostIP = anIP;
//		protocol = Protocol.getDefault();//default
//		initialPort = 33435;//unix default
//	}

	/**
	 * @param anIP
	 * @param aProtocol @see Protocol
	 * @param aDestPort 
	 * @param task
	 */
	public PingOp(String anIP, int aProtocol, short aSourcePort, short aDestPort, Task task)
	{
		super(task);
		// TODO Auto-generated constructor stub
		hostIP = anIP;
		protocol = aProtocol;
		sourcePort = aSourcePort;
		destPort = aDestPort;
	}

	//	currently not used
	/*	*//**
	 * @param task
	 * @param date
	 */
	/*
	 public PingOp(String anIP, Task task, Date date)
	 {
	 super(task, date);
	 // TODO Auto-generated constructor stub
	 hostIP = anIP;
	 }
	 */

	//	public PingOp(String anIP)
	//	{
	//		hostIP = anIP;
	//	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */

	//nextVer - better implementation
	public String toString()
	{
		return MeasurementType.getName(MeasurementType.PING) + " " + this.hostIP + " " + Protocol.getName(this.protocol);
	}

	/* (non-Javadoc)
	 * @see common.MeasurementOp#execute()
	 */
	public boolean execute()
	{
		return Measurements.execute(this);
	}

	/**
	 * @return
	 */
	public String getHostIP()
	{
		return hostIP;
	}

	/**
	 * @param string
	 */
	public void setHostIP(String string)
	{
		hostIP = string;
	}

	/**
	 * @return Returns the protocol.
	 */
	public int getProtocol()
	{
		return this.protocol;
	}

	/**
	 * @return Returns the initialPort.
	 */
	public short getSourcePort()
	{
		return this.sourcePort;
	}
	
	public short getDestPort()
	{
		return this.destPort;
	}

	/* (non-Javadoc)
	 * @see dimes.measurements.Operation#isTimedOperation()
	 */
	public boolean isTimedOperation()
	{
		return false;
	}
}