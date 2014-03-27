/*
 * Created on 19/10/2005
 */
package dimes.measurements.results;

import java.util.Date;

import dimes.measurements.RawNetHost;
import dimes.util.time.TimeSynchronizationManager;

/**
 * @author Ohad Serfaty
 *
 * a represenatation of a Packet sent or received over the network ,
 * consisting of the basic implementation and packet send/receive time , plus return ttl.
 *
 */
public class SimpleNetHost extends RawNetHost implements RawResultHolder {

	public long packetSendTime=-1;
	public long packetSendTimeNanos=-1;
	public int packetReturnTTL = -1;
	public boolean isValid = false;
	
	/* (non-Javadoc)
	 * @see dimes.treeroute.util.RawResultHolder#toXML(java.lang.String)
	 */
	public String toXML(String tabs) 
	{
		// calculated at another place ? 
		return null;
	}
	
	public String toString()
	{
		Date calendar = new Date(packetSendTime);
		return "SimpleNetHost "+(isValid?"(VALID)":"INVAILD")+ 
		": TTL:" + sequence + " HostIP:"+hopAddressStr+ " return ttl:"+packetReturnTTL+" SendTime:" + packetSendTime + " NanoSecs :" + packetSendTimeNanos
		+ " --> "+TimeSynchronizationManager.getOffsettedNanoToMiliTime(packetSendTimeNanos)+"("+calendar+")";
	}

}
