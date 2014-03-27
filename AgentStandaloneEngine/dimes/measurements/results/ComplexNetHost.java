/*
 * Created on 12/12/2005
 */
package dimes.measurements.results;

import java.util.Vector;

import dimes.measurements.NetHost;
import dimes.util.CommUtils;

/**
 * @author Ohad Serfaty
 *
 */
public class ComplexNetHost extends NetHost implements StatisticsObject
{

	int occurences=1;
	int packetReturningTTL=-1;
	
	/* (non-Javadoc)
	 * @see dimes.measurements.StatisticsObject#getObjectID()
	 */
	public Object getObjectID()
	{
		return this.getHopAddressStr();
	}
	
	public ComplexNetHost(int ttl, int delay, String hostAddr, String hostName, int replyType, int errorCode, int ipID) {
		setTimes(delay);
		setHopAddressStr(hostAddr);
		setHopNameStr(hostName);
		occurences = 1;
		setReturningTTL(ttl);
		setSequence(ttl);
		setReplyType(replyType);
		setErrorCode(errorCode);
		setIpID(ipID);
	}
	
	/* (non-Javadoc)
	 * @see dimes.measurements.StatisticsObject#addStatistics(dimes.measurements.StatisticsObject)
	 */
	public void addStatistics(StatisticsObject statsObj)
	{
		if (!(statsObj instanceof NetHost))
		{
			System.err.println("Could not add statistics with Object:" + statsObj + " (Not NetHost Object)");
			return ;
		}
		NetHost statsToAdd = (NetHost) statsObj;
		// handle worse , best and lost :
		if (statsToAdd.getLostNum()>0)
			this.lostNum+=statsToAdd.getLostNum();
		else
		{
			if (statsToAdd.getBestTime() < this.getBestTime())
				this.bestTime =statsToAdd.getBestTime();
			if (statsToAdd.getWorstTime() > this.getWorstTime())
				this.worstTime =statsToAdd.getWorstTime();
			this.avgTime = (statsToAdd.getBestTime()+  this.getAvgTime()*occurences) / (occurences+1); 
		}
		
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0)
	{
		if (!(arg0 instanceof StatisticsObject))
			return 0;
		return ((StatisticsObject)arg0).getOccurences() - this.getOccurences();
	}
	
	/* (non-Javadoc)
	 * @see dimes.measurements.StatisticsObject#getOccurences()
	 */
	public int getOccurences()
	{
		// TODO Auto-generated method stub
		return occurences;
	}
	
	/* (non-Javadoc)
	 * @see dimes.measurements.StatisticsObject#incrementOccurences()
	 */
	public void incrementOccurences()
	{
		occurences++;
	}
	
	public String toString(){
		String superStr = super.toString();
		return (superStr + " occurences:"+this.getOccurences() + " returning ttl:" + packetReturningTTL);
	}

	/**
	 * @param delay
	 */
	public void setTimes(int delay)
	{
		this.avgTime=delay;
		this.bestTime=delay;
		this.worstTime=delay;
	}

	public void swap(ComplexNetHost anAlternative) {
		int tmp = this.occurences;
		this.occurences = anAlternative.occurences;
		anAlternative.occurences = tmp;
		
		tmp = this.bestTime;
		this.bestTime = anAlternative.bestTime;
		anAlternative.bestTime = tmp;
		
		tmp = this.worstTime;
		this.worstTime = anAlternative.worstTime;
		anAlternative.worstTime = tmp;
		
		tmp = this.avgTime;
		this.avgTime = anAlternative.avgTime;
		anAlternative.avgTime = tmp;
	}
	
	/**
	 * 
	 */
	public void setLost()
	{
		this.lostNum=1;
	}
	
	public int getLost(){
		return this.lostNum;
	}

	
	/**
	 * 
	 */
	public void setLost(int value)
	{
		this.lostNum=value;
	}
	
//	/**
//	 * @return
//	 */
//	public NetHost getNetHost(){
//		NetHost result = new NetHost();
//		result.avgTime=this.avgTime;
//		result.bestTime=this.bestTime;
//		result.worstTime=this.worstTime;
//		return result;
//	}

	/**
	 * @param packetReturnTTL
	 */
	public void setReturningTTL(int packetReturnTTL)
	{
		this.packetReturningTTL = packetReturnTTL;
	}

	/**
	 * @return
	 */
	public int getReturnTTL()
	{
		return this.packetReturningTTL;
	}

	public String toXML( String tabs)
	{
		String result = "";
		result += tabs + "<Detail>\n";
		tabs += "\t";
		result += tabs + "<sequence>" + getSequence() + "</sequence>" + "\n";
		result += tabs + "<hopAddress>" + CommUtils.ipToLongSafe(getHopAddressStr()) + "</hopAddress>" + "\n";
		result += tabs + "<hopAddressStr>" + getHopAddressStr() + "</hopAddressStr>" + "\n";
		result += tabs + "<hopNameStr>" + this.getHopNameStr() + "</hopNameStr>" + "\n";
		result += tabs + "<lostNum>" + (getLostNum()<=0 ? 0 : getLostNum())  + "</lostNum>" + "\n";
		result += tabs + "<bestTime>" + getBestTime() + "</bestTime>" + "\n";
		result += tabs + "<worstTime>" + getWorstTime() + "</worstTime>" + "\n";
		result += tabs + "<avgTime>" + getAvgTime() + "</avgTime>" + "\n";
		result += tabs + "<returnTTL>" +packetReturningTTL + "</returnTTL>" +"\n";
		result += tabs + "<replyType>" + getReplyType() + "</replyType>" +"\n";
		result += tabs + "<errorCode>" + getErrorCode() + "</errorCode>" +"\n";
		result += tabs + "<ipid>" + getIPID() + "</ipid>" +"\n";
//		result += tabs + "<exception>" + getException() + "</exception>" + "\n";

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
				result += ((ComplexNetHost) alternatives.get(i)).toXML( tabs + "\t");
			}
			result += tabs + "</Alternatives>" + "\n";
		}

		tabs = tabs.substring(1);
		result += tabs + "</Detail>" + "\n";
		return result;
	}

	public void addMeasurement(int delay) {
		if (delay < bestTime) {
			bestTime = delay;
		} else if (delay > worstTime) {
			worstTime = delay;
		}
		int totalTime = avgTime*occurences + delay;
		++occurences;
		avgTime = totalTime/occurences;
	}	
	
	public void setLostNum() {
		lostNum = 4-occurences;
	}
}
