package dimes.comm2server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import org.apache.log4j.Logger;

import dimes.util.CommUtils;
import dimes.util.logging.Loggers;

public abstract class Communicator {
	
	public Communicator(String  URLstr, int connectionType) throws MalformedURLException {
		connectionHandler = ConnectionHandlerFactory.getConnectionHandler( URLstr, connectionType);
		logger = Loggers.getLogger(this.getClass());
		this.logger.debug("initialized communicator with: " + URLstr);//debug	}
	}
	
	protected ConnectionHandler connectionHandler = null;
	protected Logger logger;

	public abstract void getResponse(BufferedWriter incomingWriter) throws IOException;
	public abstract void sendFile(BufferedReader outgoingReader, String aHeader, String aTrailer) throws IOException;
	
	// send one file per connection
	public File exchangeFiles(File outgoing, File incoming, String header, String trailer) throws ConnectionException, IOException
	{
		BufferedReader reader = CommUtils.getReader(outgoing);
		BufferedWriter writer = CommUtils.getWriter(incoming);
		this.exchangeFiles(reader , writer , header , trailer);
		return incoming;
	}

	public void exchangeFiles(BufferedReader outReader, OutputStream incomingStream, String header, String trailer) throws ConnectionException, IOException {
		BufferedWriter writer = CommUtils.getWriter(incomingStream);
		this.exchangeFiles(outReader , writer , header , trailer);
	}
	
	public void exchangeFiles(BufferedReader outReader, OutputStream outStream) throws IOException, ConnectionException
	{
		BufferedWriter writer = CommUtils.getWriter(outStream);
		this.exchangeFiles(outReader , writer,"","");
	}

	public void exchangeFiles(BufferedReader outReader, BufferedWriter writer, String header, String trailer) throws IOException, ConnectionException
	{
		try
		{
			this.connectionHandler.initConnection();
			this.sendFile(outReader, header, trailer);

			// explicitly connect to the server.
			this.connectionHandler.connect();
			this.getResponse(writer);
		}
		finally
		{
			this.connectionHandler.closeConnection();
		}

		return;

	}

	public File receiveFile(File incoming) throws ConnectionException, IOException{
		try
		{
			this.connectionHandler.initConnection();

			// explicitly connect to the server.
			this.connectionHandler.connect();
			BufferedWriter writer = CommUtils.getWriter(incoming);
			this.getResponse(writer);
		}
		finally
		{
			this.connectionHandler.closeConnection();
		}

		return incoming;
	}
	
	
	/**
	 * <p>
	 * This method was added in order to allow small amount of data to be saved
	 * in String, without the need to use an in file.  
	 * </p>
	 * 
	 * @return The String which contains the asked data 
	 * @throws ConnectionException
	 * @throws IOException
	 * @author idob
	 * @since 0.5.0
	 */
	public String receiveDataToString() throws ConnectionException, IOException {
		StringWriter strWriter = null;
		BufferedWriter bfrWriter = null;
		try
		{
			this.connectionHandler.initConnection();

			// explicitly connect to the server.
			this.connectionHandler.connect();
			strWriter = new StringWriter();
			bfrWriter = new BufferedWriter(strWriter);
			this.getResponse(bfrWriter);
		}
		finally
		{
			this.connectionHandler.closeConnection();
			bfrWriter.close();
		}

		return strWriter.toString();
	}
}
