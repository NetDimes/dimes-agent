/*
 * Created on 01/12/2004
 */
package dimes.comm2server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;
import dimes.util.logging.Loggers;

/**
 * @author anat
 */
public abstract class ConnectionHandler
{
	protected URL serverURL = null;
	protected HttpURLConnection currConnection = null;

	protected Logger logger;
	protected String myName;

	ConnectionHandler(String URLstr) throws MalformedURLException
	{
		serverURL = new URL(URLstr);

		//logging
		logger = Loggers.getLogger(this.getClass());
		this.myName = this.getClass().getName();
	}

	//should be implemented by extending classes
	public abstract void initConnection() throws ConnectionException;

	public void connect() throws IOException
	{
		this.currConnection.connect();
	}

	public InputStream getInputStream() throws IOException
	{
		return this.currConnection.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException
	{
		return this.currConnection.getOutputStream();
	}

	public void closeConnection()
	{
		if (this.currConnection != null)
			this.currConnection.disconnect();
		this.currConnection = null;
	}

	/**
	 * 
	 */
	public void setBinary()
	{
		this.currConnection.setRequestProperty("Content-Type", "image/jpg");

	}

}