/*
 * Created on 27/03/2005
 *
 */
package dimes.util.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import dimes.util.DipslayServer;

/**
 * @author Ohad Serfaty
 *
 */
public class DisplayUpdateHandler extends ConsoleHandler
{

	private DipslayServer server;

	/**
	 * @param server
	 */
	public DisplayUpdateHandler(DipslayServer aServer)
	{
		server = aServer;
		this.setLevel(Level.ALL);
	}

	public void publish(LogRecord log)
	{
		if (!this.isLoggable(log))
			return;
		String msg = log.getMessage();
		//        System.out.println("Sending to client :\n" + msg);
		server.publish(msg);
	}

}