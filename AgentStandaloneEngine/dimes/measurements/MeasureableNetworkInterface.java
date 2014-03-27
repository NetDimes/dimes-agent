package dimes.measurements;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author Ohad
 *
 */
public class MeasureableNetworkInterface implements Serializable
{

	private final NetworkInterface intf;
	private String ipAddresses = "";
	private String hostName = null;
	private boolean isDefault;
	private String firstIP = null;
	private String deviceName = "default";
	static MeasureableNetworkInterface defaultNetworkIntf = new MeasureableNetworkInterface();

	/**
	 * @param intf
	 */
	public MeasureableNetworkInterface(NetworkInterface intf)
	{
		this.intf = intf;
		isDefault = false;
		Enumeration addresses = intf.getInetAddresses();
		deviceName = intf.getName();
		while (addresses.hasMoreElements())
		{
			InetAddress hostInet = (InetAddress) addresses.nextElement();
			ipAddresses += hostInet.getHostAddress();
			if (firstIP == null)// set the first IP:	
			{
				firstIP = ipAddresses;
				hostName = hostInet.getHostName();
			}
			if (addresses.hasMoreElements())
				ipAddresses += ",";
		}
	}

	/**
	 * 
	 * a constructor for the default network interface
	 * 
	 */
	private MeasureableNetworkInterface()
	{
		isDefault = true;
		this.intf = null;
		firstIP = null;
	}

	public String toString()
	{
		if (isDefault)
			return "Default Network Interface";
		else
			return "Name:" + intf.getName() + " Addresses:" + ipAddresses + " (" + hostName + ")";
	}

	public static MeasureableNetworkInterface getDefultNetworkInterface()
	{

		return defaultNetworkIntf;
	}

	/**
	 * @return
	 */
	public String getHostName()
	{
		return hostName;
	}

	/**
	 * @return
	 */
	public String getIP()
	{
		return firstIP;
	}

	public String getDeviceName()
	{
		return deviceName;
	}

}