/*
 * Created on 27/10/2005
 */
package dimes.state.user;

import dimes.util.properties.PropertiesBean;

/**
 * @author Ela
 *
 * This class handles with properties which 
 * can be changed by the control center  
 */
public class PropertiesDetails
{
	/**
	 * Ctor, checks which operation was given and updates it's parm
	 */
	public PropertiesDetails(String op, String parm)
	{
		super();
		if (op.equalsIgnoreCase("fileTransferRate"))
		{
			setFileTransferRate(op);
			setFileTransferRateParm(parm);
		}
		else
			if (op.equalsIgnoreCase("mesurmentRate"))
			{
				setMesurmentRate(op);
				setMesurmentRateParm(parm);
			}
			else
				if (op.equalsIgnoreCase("defaultProtocol"))
				{
					setDefaultProtocol(op);
					setDefaultProtocolParm(parm);
				}
				else
					if (op.equalsIgnoreCase("useProxy"))
					{
						setUseProxy(op);
						setUseProxyParm(parm);
					}
					else
						if (op.equalsIgnoreCase("proxyHost"))
						{
							setProxyHost(op);
							setProxyHostParm(parm);
						}
						else
							if (op.equalsIgnoreCase("proxyPort"))
							{
								setProxyPort(op);
								setProxyPortParm(parm);
							}
							else
								if (op.equalsIgnoreCase("automaticUpdate"))
								{
									setAutomaticUpdate(op);
									setAutomaticUpdateParm(parm);
								}
	}

	public String fileTransferRate;
	public String fileTransferRateParm;
	public String mesurmentRate;
	public String mesurmentRateParm;
	public String defaultProtocol;
	public String defaultProtocolParm;
	public String useProxy;
	public String useProxyParm;
	public String proxyHost;
	public String proxyHostParm;
	public String proxyPort;
	public String proxyPortParm;
	public String automaticUpdate;
	public String automaticUpdateParm;

	/**
	 * @param fileTransferRate The fileTransferRate to set.
	 */
	public void setFileTransferRate(String fileTransferRate)
	{
		this.fileTransferRate = fileTransferRate;
	}
	/**
	 * @param mesurmentRate The mesurmentRate to set.
	 */
	public void setMesurmentRate(String mesurmentRate)
	{
		this.mesurmentRate = mesurmentRate;
	}
	/**
	 * @param proxyHost The proxyHost to set.
	 */
	public void setProxyHost(String proxyHost)
	{
		this.proxyHost = proxyHost;
	}
	/**
	 * @param proxyPort The proxyPort to set.
	 */
	public void setProxyPort(String proxyPort)
	{
		this.proxyPort = proxyPort;
	}
	/**
	 * @param useProxy The useProxy to set.
	 */
	public void setUseProxy(String useProxy)
	{
		this.useProxy = useProxy;
	}
	/**
	 * @param defaultProtocol The defaultProtocol to set.
	 */
	public void setDefaultProtocol(String defaultProtocol)
	{
		this.defaultProtocol = defaultProtocol;
	}
	/**
	 * @param automaticUpdate The automaticUpdate to set.
	 */
	public void setAutomaticUpdate(String automaticUpdate)
	{
		this.automaticUpdate = automaticUpdate;
	}
	/**
	 * @return Returns the automaticUpdate.
	 */
	public String getAutomaticUpdate()
	{
		return automaticUpdate;
	}
	/**
	 * @return Returns the defaultProtocol.
	 */
	public String getDefaultProtocol()
	{
		return defaultProtocol;
	}
	/**
	 * @return Returns the fileTransferRate.
	 */
	public String getFileTransferRate()
	{
		return fileTransferRate;
	}
	/**
	 * @return Returns the mesurmentRate.
	 */
	public String getMesurmentRate()
	{
		return mesurmentRate;
	}
	/**
	 * @return Returns the proxyHost.
	 */
	public String getProxyHost()
	{
		return proxyHost;
	}
	/**
	 * @return Returns the proxyPort.
	 */
	public String getProxyPort()
	{
		return proxyPort;
	}
	/**
	 * @return Returns the useProxy.
	 */
	public String getUseProxy()
	{
		return useProxy;
	}
	/**
	 * @param fileTransferRateParm The fileTransferRateParm to set.
	 */
	public void setFileTransferRateParm(String fileTransferRateParm)
	{
		this.fileTransferRateParm = fileTransferRateParm;
	}
	/**
	 * @param automaticUpdateParm The automaticUpdateParm to set.
	 */
	public void setAutomaticUpdateParm(String automaticUpdateParm)
	{
		this.automaticUpdateParm = automaticUpdateParm;
	}
	/**
	 * @param defaultProtocolParm The defaultProtocolParm to set.
	 */
	public void setDefaultProtocolParm(String defaultProtocolParm)
	{
		this.defaultProtocolParm = defaultProtocolParm;
	}
	/**
	 * @param mesurmentRateParm The mesurmentRateParm to set.
	 */
	public void setMesurmentRateParm(String mesurmentRateParm)
	{
		this.mesurmentRateParm = mesurmentRateParm;
	}
	/**
	 * @param proxyHostParm The proxyHostParm to set.
	 */
	public void setProxyHostParm(String proxyHostParm)
	{
		this.proxyHostParm = proxyHostParm;
	}
	/**
	 * @param proxyPortParm The proxyPortParm to set.
	 */
	public void setProxyPortParm(String proxyPortParm)
	{
		this.proxyPortParm = proxyPortParm;
	}
	/**
	 * @param useProxyParm The useProxyParm to set.
	 */
	public void setUseProxyParm(String useProxyParm)
	{
		this.useProxyParm = useProxyParm;
	}

	//	public String toString(){
	//		return (this.fileTransferRate==null?"":"tranfer rate " +this.fileTransferRate);
	//	}

}