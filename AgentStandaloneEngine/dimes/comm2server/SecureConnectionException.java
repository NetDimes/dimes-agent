/*
 * Created on 01/12/2004
 */
package dimes.comm2server;

/**
 * thrown for errors unique to secure connection
 * @author anat
 */
public class SecureConnectionException extends ConnectionException
{

	/**
	 * @param theMsg
	 */
	public SecureConnectionException(String theMsg)
	{
		super("Security error. " + theMsg);
		// TODO Auto-generated constructor stub
	}

}