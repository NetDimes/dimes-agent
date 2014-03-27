/*
 * Created on 25/08/2005
 *
 */
package dimes.scheduler.usertask;

import dimes.measurements.MeasurementType;
import dimes.measurements.Measurements;
import dimes.measurements.Protocol;

/**
 * @author Ohad Serfaty
 *
 */
public class CommandLineUserTask implements UserTaskSource
{

	private final String commandLineString;
	private final String protocolString;
	private int protocol;

	public CommandLineUserTask(String commandLineString, String protocolString)
	{
		this.commandLineString = commandLineString;
		this.protocolString = protocolString;
	}

	public void parse() throws UserTaskPerserException
	{

		if (Protocol.getName(Protocol.ICMP).equalsIgnoreCase(protocolString))
			protocol = Protocol.ICMP;
		else
			protocol = Protocol.UDP;
		if (!dimes.measurements.IPUtils.isValidAddress(commandLineString))
			throw new UserTaskPerserException();
	}

	public String getCommandsString()
	{
		return MeasurementType.getName(MeasurementType.TRACEROUTE) + " " + commandLineString + " " + Protocol.getName(protocol);
	}

	public String getScriptID()
	{
		return commandLineString;
	}

	public String toString()
	{
		return "CommandLine Operation : Traceroute " + commandLineString + " " + protocolString;
	}

}