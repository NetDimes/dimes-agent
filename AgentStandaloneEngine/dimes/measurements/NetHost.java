package dimes.measurements;

import java.util.Vector;

import dimes.measurements.results.RawResultHolder;

/*
 * Created on 24/02/2004
 *
 */

/**
 * @author galith
 *
 */
public class NetHost extends RawNetHost implements RawResultHolder
{

	
	protected int lostNum = -1;
	protected int bestTime = -1;
	protected int worstTime = -1;
	protected int avgTime = -1;

	private Vector<NetHost> alternatives = new Vector<NetHost>(0);//start empty
	private String exception = "";
	public boolean valid = false;//dimes
	private int    replyType;
	private int    errorCode;
	private int	   ipID;

	private boolean usingKernelTS = false;//whether using accurate kernel timestamping

	
	/**
	 * @return Returns the lostNum.
	 */
	public int getLostNum()
	{
		return this.lostNum;
	}

	
	/**
	 * @return Returns the worstTime.
	 */
	public int getWorstTime()
	{
		return this.worstTime;
	}

	/*	public boolean isValid()
	 {
	 return this.valid;
	 }	
	 */
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		String str = "address:" + this.hopAddress + "\taddress string: " + this.hopAddressStr + "\tname: " + this.hopNameStr + "\tlost:" + this.lostNum
				+ "\tbest:" + this.bestTime + "\tworst:" + this.worstTime + "\tIPID:"+this.ipID+"\tavg: " + this.avgTime;

		//		if (this.alternatives == null)
		//		    return "";
		//		this.alternatives.trimToSize();
		//		for (int i=0; i<this.alternatives.size(); ++i)
		//		{
		//		    NetHost aHost = (NetHost)alternatives.get(i);
		//		    str += "alternative --- " + aHost.toString();
		//		}
		return str;
	}

	/**
	 * @return Returns the avgTime.
	 */
	public int getAvgTime()
	{
		return this.avgTime;
	}
	/**
	 * @return Returns the bestTime.
	 */
	public int getBestTime()
	{
		return this.bestTime;
	}
	/**
	 * @return Returns the exception.
	 */
	public String getException()
	{
		return this.exception;
	}

	
//	/* (non-Javadoc)
//	 * @see dimes.measurements.RawResultHolder#toXML(java.lang.String)
//	 */
//	public String toXML(String theTabs)
//	{
//		return MeasurementsResultFormatter.toXML(this, theTabs);
//	}

	/**
	 * @param theException The exception to set.
	 */
	public void setException(String theException)
	{
		this.exception = theException;
	}


	//todo - private - supposed to be used only by JNI
	public void addAlternative(NetHost anAlternative)
	{
		this.alternatives.add(anAlternative);
	}
	
	public void removeAlternative(NetHost anAlternative)
	{
		this.alternatives.remove(anAlternative);
	}
	
	/**
	 * @return Returns the alternatives.
	 */
	public Vector<NetHost> getAlternatives()
	{
		return this.alternatives;
	}
	public boolean isUsingKernelTS()
	{
		return this.usingKernelTS;
	}
	
	public void setReplyType(int type) {
		replyType = type;
	}
	
	public int getReplyType() {
		return replyType;
	}
	
	public void setErrorCode(int code) {
		errorCode = code;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public void setIpID(int i){
		ipID=i;
	}
	
	public int getIPID(){
		return ipID;
	}
	
	public String toXML( String tabs)
	{
		String result = "";
		result += tabs + "<Detail>\n";
		tabs += "\t";

		result += tabs + "<sequence>" + getSequence() + "</sequence>" + "\n";
		result += tabs + "<hopAddress>" + getHopAddress() + "</hopAddress>" + "\n";
		result += tabs + "<hopAddressStr>" + getHopAddressStr() + "</hopAddressStr>" + "\n";
		result += tabs + "<hopNameStr>" + getHopNameStr() + "</hopNameStr>" + "\n";
		result += tabs + "<lostNum>" + getLostNum() + "</lostNum>" + "\n";
		result += tabs + "<bestTime>" + getBestTime() + "</bestTime>" + "\n";
		result += tabs + "<worstTime>" + getWorstTime() + "</worstTime>" + "\n";
		result += tabs + "<avgTime>" + getAvgTime() + "</avgTime>" + "\n";
		result += tabs + "<exception>" + getException() + "</exception>" + "\n";
		result += tabs + "<kernelTS>" + isUsingKernelTS() + "</kernelTS>" + "\n";
		result += tabs + "<replyType>" + getReplyType() + "</replyType>" +"\n";
		result += tabs + "<errorCode>" + getErrorCode() + "</errorCode>" +"\n";
		result += tabs + "<ipid>" + getIPID() + "</ipid>" + "\n";
		
		Vector<NetHost> alternatives = getAlternatives();
		if (alternatives == null)
			return result + tabs + "</Detail>" + "\n";
		alternatives.trimToSize();
		int validAlternative = -1;
		if (alternatives.size() != 0)
		{
			for (int i = 0; i < alternatives.size(); ++i)
				if (alternatives.get(i) != null)
					validAlternative = i;
		}
		if (validAlternative != -1)
		{
			result += tabs + "<Alternatives>\n";
			for (int i = 0; i <= validAlternative; ++i)
			{
				result += ((NetHost) alternatives.get(i)).toXML( tabs + "\t");
			}
			result += tabs + "</Alternatives>" + "\n";
		}

		tabs = tabs.substring(1);
		result += tabs + "</Detail>" + "\n";
		return result;
	}


}