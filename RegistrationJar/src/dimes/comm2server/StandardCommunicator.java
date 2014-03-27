/*
 * Created on 03/02/2004
 */
/**
 * This package contains all Agent classes that are related to communication
 * with the server.
 */
package dimes.comm2server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;

import dimes.util.CommUtils;

/**
 * @author anat
 * Communicates with the server by exchanging files with the server through
 * its URL.
 * This class is a singleton - its parameters are initialized only once
 * on creation.
 */
public class StandardCommunicator extends Communicator
{

	/**
	 * @param URLstr URL string of the server
	 * @param connectionType see ConnectionHandlerFactory (e.g. ConnectionHandlerFactory.SECURE_CONNECTION)
	 * @throws MalformedURLException
	 */
	public StandardCommunicator(String URLstr, int connectionType) throws MalformedURLException
	{
		super(URLstr , connectionType);
	}

	public void sendFile(BufferedReader outgoingReader, String aHeader, String aTrailer) throws IOException
	{
		if (outgoingReader == null)
			return;
		OutputStream outStream = null;
		BufferedWriter serverWriter = null;

		try
		{
			// get the output stream to POST
			//			outStream = this.currConnection.getOutputStream();
			outStream = this.connectionHandler.getOutputStream();

			// write to the server
			serverWriter = CommUtils.getWriter(outStream);

			//write header
			BufferedReader strReader = new BufferedReader(new StringReader(aHeader));
			CommUtils.writeContinuous(strReader, serverWriter);
			strReader.close();

			//write file
			CommUtils.writeContinuous(outgoingReader, serverWriter);
			outgoingReader.close();

			//write trailer - use writeAll to close both reader and writer afterwards
			strReader = new BufferedReader(new StringReader(aTrailer));
			CommUtils.writeAll(strReader, serverWriter);
			strReader.close();
		}
		finally
		{
			if (serverWriter != null)
			{
				serverWriter.close();
			}
			if (outStream != null)
				outStream.close();
		}

	}

	public void getResponse(BufferedWriter incomingWriter) throws IOException
	{
		BufferedReader serverReader = null;
		InputStream inStream = null;

		try
		{
			//			inStream = this.currConnection.getInputStream();
			inStream = this.connectionHandler.getInputStream();

			serverReader = CommUtils.getReader(inStream);

			CommUtils.writeAll(serverReader, incomingWriter);
		}
		finally
		{
			if (serverReader != null)
				serverReader.close();
			if (inStream != null)
				inStream.close();
		}
	}


	public boolean dummyAttempt() {
		return true;
	}


}