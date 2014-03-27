package dimes.measurements.results;
/*
 * Created on 25/01/2004
 *
 */

/**
 * @author galith
 *
 * Class to hold variables to return to the Measurements Module
 * 
 */

import java.sql.Timestamp;

/**
 * @author Ohad Serfaty
 *
 * The Abstract representation of a measurement results , consisting 
 * of all the (raw_res_)main details of a measurement ,  
 *
 */
public abstract class Results 
{
	protected int scriptLine = -1;
	protected Timestamp timeStamp;
	protected String localTime;
	protected String exception = "";
	protected int requestedNumOfTrials;
	protected int actualNumOfTrials;
	protected boolean SF;
	protected boolean reachedDest = false;
	protected String command;
	protected String protocol;
	protected String source;// by name
	protected String sourceIp; // by ip address
	protected String host;
	protected String destIp;
	protected long destAddress;//long representation of dest IP address
	protected String s;
	

	public Results(String source, String sourceIp, String host, String destIp, long destAddress, int s, String comm, String proto, Timestamp ts,
			String localTime, int n)
	{
		this.sourceIp = sourceIp;
		this.source = source;
		this.host = host;
		this.destIp = destIp;
		this.destAddress = destAddress;
		scriptLine = s;
		command = comm;
		protocol = proto;
		timeStamp = ts;
		this.localTime = localTime;
		requestedNumOfTrials = n;
	}
	
	public void setRequestedNumOfTrials(int r)
	{
		requestedNumOfTrials = r;
	}
	public void setActualNumOfTrials(int a)
	{
		actualNumOfTrials = a;
	}
	public void setSF(boolean sf)
	{
		SF = sf;
	}


	/**
	 * @return
	 */
	public int getActualNumOfTrials()
	{
		return actualNumOfTrials;
	}

	/**
	 * @return
	 */
	public String getCommand()
	{
		return command;
	}

	/**
	 * @return
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * @return
	 */
	public int getRequestedNumOfTrials()
	{
		return requestedNumOfTrials;
	}

	/**
	 * @return
	 */
	public String getS()
	{
		return s;
	}

	/**
	 * @return
	 */
	public int getScriptLine()
	{
		return scriptLine;
	}

	/**
	 * @return
	 */
	public boolean isSF()
	{
		return SF;
	}

	/**
	 * @param string
	 */
	public void setCommand(String string)
	{
		command = string;
	}

	/**
	 * @param string
	 */
	public void setHost(String string)
	{
		host = string;
	}

	/**
	 * @param string
	 */
	public void setS(String string)
	{
		s = string;
	}

	/**
	 * @param i
	 */
	public void setScriptLine(int i)
	{
		scriptLine = i;
	}

	/**
	 * @return
	 */
	public Timestamp getTimeStamp()
	{
		return timeStamp;
	}

	/**
	 * @return
	 */
	public String getSource()
	{
		return source;
	}

	/**
	 * @param string
	 */
	public void setSource(String string)
	{
		source = string;
	}

	/**
	 * @return
	 */
	public String getSourceIp()
	{
		return sourceIp;
	}

	/**
	 * @param string
	 */
	public void setSourceIp(String string)
	{
		sourceIp = string;
	}

	/**
	 * @return
	 */
	public String getLocalTime()
	{
		return localTime;
	}

	/**
	 * @param string
	 */
	public void setLocalTime(String string)
	{
		localTime = string;
	}

	/**
	 * @return
	 */
	public String getDestIp()
	{
		return destIp;
	}

	/**
	 * @param string
	 */
	public void setDestIp(String string)
	{
		destIp = string;
	}

	/**
	 * @return
	 */
	public String getException()
	{
		return exception;
	}

	/**
	 * @param string
	 */
	public void setException(String string)
	{
		exception = string;
	}

	/**
	 * @return Returns the destAddress.
	 */
	public long getDestAddress()
	{
		return this.destAddress;
	}
	/**
	 * @param theDestAddress The destAddress to set.
	 */
	public void setDestAddress(long theDestAddress)
	{
		this.destAddress = theDestAddress;
	}
	/**
	 * @return Returns the reachedDest.
	 */
	public boolean isReachedDest()
	{
		return this.reachedDest;
	}
	/**
	 * @param theReachedDest The reachedDest to set.
	 */
	public void setReachedDest(boolean theReachedDest)
	{
		this.reachedDest = theReachedDest;
	}
	/**
	 * @return Returns the protocol.
	 */
	public String getProtocol()
	{
		return this.protocol;
	}

	/**
	 * format the <RawResults> content tags
	 * 
	 * @param tabs
	 * @return
	 */
	public abstract String formatRawDetails(String tabs);
	
	
	/**
	 * @return true if there are Raw details available.
	 */
	public abstract boolean hasRawDetails();
	
	
	/**
	 * Format the residual operation details , supplementary to this
	 * classe's details.
	 * 
	 * @param tabs
	 * @return the format.
	 */
	public abstract String formatOperationDetails(String tabs);

	/**
	 * formats the basic XML details
	 * 
	 * @param tabs
	 * @return the format
	 */
	public String formatResultDetails(String tabs)
	{
		String result="";
		result += tabs + "<ScriptLineNum>" + getScriptLine() + "</ScriptLineNum>" + "\n";
		result += tabs + "<StartTime>" + getTimeStamp() + "</StartTime>" + "\n";
		result += tabs + "<LocalStartTime>" + getLocalTime() + "</LocalStartTime>" + "\n"; //7.3.04
		result += tabs + "<CommandType>" + getCommand() + "</CommandType>" + "\n";
		result += tabs + "<Protocol>" + getProtocol() + "</Protocol>" + "\n";//2.2.05
		result += tabs + "<SourceName>" + getSource() + "</SourceName>" + "\n";
		result += tabs + "<SourceIP>" + getSourceIp() + "</SourceIP>" + "\n";
		result += tabs + "<DestName>" + getHost() + "</DestName>" + "\n"; //7.3.04
		result += tabs + "<DestIP>" + getDestIp() + "</DestIP>" + "\n";//7.3.04
		result += tabs + "<DestAddress>" + getDestAddress() + "</DestAddress>" + "\n";//16.12.04
		result += tabs + "<NumOfTrials>" + getRequestedNumOfTrials() + "</NumOfTrials>" + "\n";
		result += tabs + "<Success>" + isSF() + "</Success>" + "\n";
		result += tabs + "<reachedDest>" + isReachedDest() + "</reachedDest>" + "\n";
		result += tabs + "<Exceptions>" + getException() + exception + "</Exceptions>" + "\n";
		return result;
	}
	
	
	
}