package dimes.measurements;

import java.util.Vector;

import dimes.measurements.results.RawResultHolder;

/*
 * Created on 2/02/2010
 *
 */

/**
 * @author galina
 *
 */
public class QBEHost implements RawResultHolder
{
	private int trainNum = -1;
	private int packetNum = -1;
	private int experimentID = -1;
	private int ttl = -1;
	private String destIP = "";
	private long delay;

	public QBEHost(int train, int packet, int expID, String destIP, long delay) {
		trainNum = train;
		packetNum = packet;
		experimentID = expID;
		this.destIP = destIP;
		this.delay = delay;
	}
	
	public void setTTL(int ttl) {
		this.ttl = ttl;
	}
	
	public int getPacketNumber() {
		return packetNum;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		String str = "address:" + this.destIP + "\tttl:" + this.ttl
				+ "\tdelay:" + this.delay + "\texperiment:" + this.experimentID 
				+ "\ttrain:"+this.trainNum + "\tpacket:" + this.packetNum;

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
	
//	/* (non-Javadoc)
//	 * @see dimes.measurements.RawResultHolder#toXML(java.lang.String)
//	 */
//	public String toXML(String theTabs)
//	{
//		return MeasurementsResultFormatter.toXML(this, theTabs);
//	}

	public String toXML( String tabs)
	{
		String result = "";
		result += tabs + "<Detail>\n";
		tabs += "\t";

		result += tabs + "<experimentID>" + experimentID + "</experimentID>" + "\n";
		result += tabs + "<trainNumber>" + trainNum + "</trainNumber>" + "\n";
		result += tabs + "<packetNumber>" + packetNum + "</packetNumber>" + "\n";
		result += tabs + "<ttl>" + ttl + "</ttl>" + "\n";
		result += tabs + "<destIP>" + destIP + "</destIP>" + "\n";
		result += tabs + "<delay>" + delay + "</delay>" + "\n";

		tabs = tabs.substring(1);
		result += tabs + "</Detail>" + "\n";
		return result;
	}


}