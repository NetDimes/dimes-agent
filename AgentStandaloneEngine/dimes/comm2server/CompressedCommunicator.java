package dimes.comm2server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import dimes.util.CommUtils;

public class CompressedCommunicator extends Communicator {

	public CompressedCommunicator(String URLstr, int connectionType) throws MalformedURLException {
		super(URLstr, connectionType);
	}


	public void sendFile(BufferedReader outgoingReader, String aHeader, String aTrailer) throws IOException
	{
		if (outgoingReader == null)
			return;
		OutputStream outStream = null;
		BufferedWriter serverWriter = null;
		ZipOutputStream zipOut=null;
		try
		{
			// get the output stream to POST
			//			outStream = this.currConnection.getOutputStream();
			outStream = this.connectionHandler.getOutputStream();
			zipOut = new ZipOutputStream(outStream);
			zipOut.putNextEntry(new ZipEntry("Entry1"));
			
			// write to the server
			serverWriter = CommUtils.getWriter(zipOut);

			//write header
			BufferedReader strReader = new BufferedReader(new StringReader(aHeader));
			CommUtils.writeContinuous(strReader, serverWriter);
			strReader.close();

			//write file
			CommUtils.writeContinuous(outgoingReader, serverWriter);
			outgoingReader.close();

			//write trailer - use writeAll to close both reader and writer afterwards
			strReader = new BufferedReader(new StringReader(aTrailer));
			CommUtils.writeContinuous(strReader, serverWriter);
			strReader.close();
			zipOut.closeEntry();
			zipOut.flush();
		}
		finally
		{
			if (serverWriter != null)
			{
				serverWriter.close();
			}
			if (outStream != null)
				outStream.close();
			if (zipOut != null)
				zipOut.close();
		}

	}

	

	public void getResponse(BufferedWriter incomingWriter) throws IOException
	{
		BufferedReader serverReader = null;
		InputStream inStream = null;

		try
		{
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
