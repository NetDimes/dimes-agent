/*
 * Created on 27/03/2005
 *
 */
package dimes.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import dimes.AgentGuiComm.logging.Loggers;
import dimes.Agent;
import dimes.measurements.Measurements;
//import dimes.util.logging.Loggers;

/**
 * @author Ohad Serfaty
 * 
 * a class acting as a server to graph state requests
 * 
 * note that:
 * 1. currently, this 'server' is meant to server only one
 * 		client - security reasons. also - this client must be
 * 		al localhost.
 * 
 * 2.  the server holds a listening thread  - so in order to 
 * 		start the server one must use startServer() and in order to exit
 * 		smoothly one must  use stopServer()
 * 
 * ****************************************************************
 * Ido:
 * Added in version 0.5.0: In case of IOException in the thread the run() method will be terminated - to avoid endless loop.
 */
public class DipslayServer extends ServerSocket
{

	// members :
	private DisplayServerThread listener;
	private Agent agent;
	Socket displayClientSocket = null;
	private ObjectOutputStream out = null;

	/************
	 * constructor 
	 * TODO : don't take an agent , but a class implementing
	 * some interfae with getCurrentGraphStateRecord().
	 * 
	 * @param port
	 * @param myAgent
	 * @throws IOException
	 */
	public DipslayServer(int port) throws IOException
	{
		super(port);

	}

	public void setAgent(Agent myAgent)
	{
		agent = myAgent;
	}

	/***************
	 * this function must be used in order to smoothly close 
	 * the server
	 *
	 */
	public void stopServer()
	{
		listener.stopServer();
	}

	public void startServer()
	{
		Loggers.getLogger().fine("Started Display server.");
		listener = new DisplayServerThread(this);
		listener.start();
	}

	/**
	 * @param requestSocket
	 * @throws IOException
	 */
	private void handleRequest(Socket clientSocket) throws IOException
	{

		displayClientSocket = clientSocket;
		out = new ObjectOutputStream(displayClientSocket.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(displayClientSocket.getInputStream()));
		String inputLine, outputLine;

		try
		{
			while ((inputLine = in.readLine()) != null)
			{
				//                    System.out
				//                            .println("<------------------ read : --------------->");
				//                    System.out.println(inputLine);
				if (inputLine.equals("DisplayIPs"))
				{
					outputLine = getCurrectGraphStateRecord();
					out.writeObject(outputLine);
					out.flush();
				}
			}
		}
		catch (Exception e)
		{

		}

		out.close();
		in.close();
		clientSocket.close();
		this.removeClient();
	}

	/**
	 * remove the one and only client.
	 * 
	 */
	private void removeClient()
	{
		Loggers.getLogger().fine("Removing ScreenSaver client.");
		displayClientSocket = null;
		out = null;
	}

	/**
	 * @return the current edges of the graph as xml
	 */
	private String getCurrectGraphStateRecord()
	{
		return agent.getCurrectGraphStateRecord();
	}

	/**
	 * publish a string message to the client.
	 * 
	 * @param msg
	 */
	public void publish(String msg)
	{
		if (displayClientSocket == null || out == null)
		{
			//            System.out.println("No live TCP client was found.");
			return;
		}
		//        System.out.println("Publishnig to a live client :\n" + msg);
		try
		{
			out.writeObject(msg);
			out.flush();
		}
		catch (Exception e)
		{
			removeClient();
		}

	}

	/**********************
	 *  a listener thread class for the server.
	 * 
	 * @author Ohad Serfaty
	 *
	 */
	class DisplayServerThread extends Thread
	{

		DipslayServer server;
		private boolean stopServer = false;

		DisplayServerThread(DipslayServer theServer)
		{
			server = theServer;
		}

		public void run()
		{

			while (!stopServer)
			{
				Socket requestSocket = null;
				try
				{
					requestSocket = server.accept();
					InetAddress remoteAddress = requestSocket.getInetAddress();
					//                    System.out.println("address: " + remoteAddress.getHostAddress());

					// Security check :
					//                    if (! (remoteAddress.getHostAddress().startsWith("127.0.0") ||
					//                            (remoteAddress.getHostAddress().startsWith("192.168") ||
					//                            InetAddress.getLocalHost().equals(remoteAddress)) ))
					if (!(dimes.measurements.IPUtils.isPrivateIP(remoteAddress.getHostAddress()) || InetAddress.getLocalHost().equals(remoteAddress)))
						throw new IOException("Attempt to connect out of localhost blocked.");
					Loggers.getLogger().info("Connected with screen saver from address:" + remoteAddress);
					if (requestSocket != null)
						server.handleRequest(requestSocket);
				}
				catch (IOException e)
				{
					Loggers.getLogger().severe("Display Server stopped.");
					Loggers.getLogger().severe("Problem with Display Server Socket. DIMES Screen Saver will stop run.");
					//e.printStackTrace();
					server.removeClient();
					stopServer = true;
					stopServer();
				}
			}
			/*System.out.println*/Loggers.getLogger(this.getClass()).finest("Display thread stopped");//debug
		}

		/****************
		 * stop the server : indicate the thread to stop 
		 * and the server to close.
		 *
		 */
		public void stopServer()
		{
			/*System.out.println*/Loggers.getLogger(this.getClass()).finest("stopping server");
			stopServer = true;
			try
			{					
				server.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			}
		}

	}

}