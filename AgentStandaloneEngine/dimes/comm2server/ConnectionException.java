/*
 * Created on 01/12/2004
 */
package dimes.comm2server;

import java.io.IOException;

/**
 * @author anat
 */
public class ConnectionException extends IOException
{

	/**
	 * @param msg
	 */
	public ConnectionException(String msg)
	{
		super("Could not connect. underlying exception: " + msg);
	}

}