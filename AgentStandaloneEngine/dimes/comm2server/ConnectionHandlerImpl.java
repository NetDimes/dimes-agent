/*
 * Created on 01/12/2004
 */
package dimes.comm2server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Properties;

import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * @author anat
 */
public class ConnectionHandlerImpl extends ConnectionHandler
{

	/**
	 * should not be created directly, but through ConnectionHandlerFactory
	 * @param serverURLstr
	 * @throws MalformedURLException
	 */
	ConnectionHandlerImpl(String serverURLstr) throws MalformedURLException
	{
		super(serverURLstr);
	}

	/* (non-Javadoc)
	 * @see dimes.comm2server.ConnectionHandler#initConnection()
	 */
	public void initConnection() throws ConnectionException
	{
		try
		{
			boolean useProxy = false;
			String proxy = "";
			String port = "";
			try
			{
				useProxy = Boolean.valueOf(PropertiesBean.getProperty(PropertiesNames.USE_PROXY/*"comm.useProxy"*/)).booleanValue();
				proxy = PropertiesBean.getProperty(PropertiesNames.PROXY_HOST/*"comm.proxyHost"*/);
				port = PropertiesBean.getProperty(PropertiesNames.PROXY_PORT/*"comm.proxyPort"*/);
				useProxy = useProxy && (PropertiesBean.isValidValue(proxy) && PropertiesBean.isValidValue(port));
				PropertiesBean.setProperty(PropertiesNames.USE_PROXY/*"comm.useProxy"*/, String.valueOf(useProxy));
			}
			catch (NoSuchPropertyException e)
			{
				this.logger.warning(e.toString());
				useProxy = false;
			}
			Properties systemProperties = System.getProperties();
			if (useProxy)
			{
				systemProperties.put("httpProxySet", "true");
				systemProperties.put("http.proxyHost", proxy);
				systemProperties.put("http.proxyPort", port);
			}
			else
			{
				systemProperties.put("httpProxySet", "false");
				systemProperties.remove("http.proxyPort");
			}

			System.setProperties(systemProperties);

			//            HttpURLConnection conn = (HttpURLConnection) this.serverURL.openConnection();
			this.currConnection = (HttpURLConnection) this.serverURL.openConnection();
			String connTO = null;
			try
			{
				connTO = PropertiesBean.getProperty(PropertiesNames.CONNECT_TIMEOUT/*"connectTimeout"*/);
			}
			catch (NoSuchPropertyException e)
			{
				connTO = "30000";
			}
			String readTO;
			try
			{
				readTO = PropertiesBean.getProperty(PropertiesNames.READ_TIMEOUT/*"readTimeout"*/);
			}
			catch (NoSuchPropertyException e)
			{
				readTO = "30000";
			}
			System.setProperty("sun.net.client.defaultConnectTimeout", connTO);
			System.setProperty("sun.net.client.defaultReadTimeout", readTO);
			this.currConnection.setRequestMethod("POST");
			this.currConnection.setAllowUserInteraction(false); // system may not ask the user
			this.currConnection.setDoOutput(true); // we want to send things
			this.currConnection.setDoInput(true); // we want to receive things
			//		return conn;

		}
		catch (IOException e)
		{
			throw new ConnectionException("underlying exception: " + e.toString());
		}
	}

}