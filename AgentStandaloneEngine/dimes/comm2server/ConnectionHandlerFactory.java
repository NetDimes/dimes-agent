/*
 * Created on 01/12/2004
 */
package dimes.comm2server;

import java.net.MalformedURLException;

/**
 * @author anat
 */
public class ConnectionHandlerFactory
{
	public static final int NONSECURE_CONNECTION = 0;
	public static final int SECURE_CONNECTION = 1;

	public static ConnectionHandler getConnectionHandler(String urlStr, int type) throws MalformedURLException
	{
		switch (type)
		{
			case NONSECURE_CONNECTION :
				return new ConnectionHandlerImpl(urlStr);
			case SECURE_CONNECTION :
				return new SecureConnectionHandlerImpl(urlStr);
			default :
				return null;
		}
	}

}