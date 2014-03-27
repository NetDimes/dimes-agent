///*
// * Created on 15/03/2005
// *
// */
//package dimes.comm2server;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.net.MalformedURLException;
//
//import dimes.util.CommUtils;
//
///**
// * @author Ohad Serfaty
// *
// * an extension to communicator, allowing binary exchange files.
// * Note that :
// * 1. this class does NOT use a bandwidth limited output stream - 
// *    so the load on the users bandwith is full
// * 		reason is : this class is ment to serve the gallery post.
// * 
// * 
// * 2. the servlet reading this function on the other side has
// * 		to use a proper function in order to read the stream
// * 		of this class (CommUtils.writeBinary in that case)  
// *
// *
// */
//public class BinaryCommunicator extends Communicator
//{
//
//	/**
//	 * @param URLstr
//	 * @param connectionType
//	 * @throws MalformedURLException
//	 */
//	public BinaryCommunicator(String URLstr, int connectionType) throws MalformedURLException
//	{
//		super(URLstr, connectionType);
//	}
//
//	public void getResponse(BufferedWriter incomingWriter) throws IOException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public void sendFile(BufferedReader outgoingReader, String aHeader, String aTrailer) throws IOException {
//		try
//		{
//			if (outgoingReader == null)
//				return;
//			OutputStream outStream = null;
//			BufferedWriter serverWriter = null;
//			// get the output stream to POST
//			outStream = connectionHandler.getOutputStream();
//
//			//write file
//			CommUtils.writeBinary(outgoingReader, outStream);
//			outgoingReader.close();
//		}
//		finally
//		{
//			if (serverWriter != null)
//			{
//				serverWriter.close();
//			}
//			if (outStream != null)
//				outStream.close();
//		}
//		
//	}
//
////	
////	private void sendFile(File anOutgoing) throws IOException
////	{
////		if (anOutgoing == null)
////			return;
////		FileInputStream outgoingReader = null;
////
////		try
////		{
////			//write header
////			outgoingReader = new FileInputStream(anOutgoing);
////			this.sendFile(outgoingReader);
////		}
////		finally
////		{
////			if (outgoingReader != null)
////				outgoingReader.close();
////		}
////	}
////
////	private void sendFile(FileInputStream outgoingReader) throws IOException
////	{
////		if (outgoingReader == null)
////			return;
////		OutputStream outStream = null;
////		BufferedWriter serverWriter = null;
////
////		try
////		{
////			// get the output stream to POST
////			outStream = connectionHandler.getOutputStream();
////
////			//write file
////			CommUtils.writeBinary(outgoingReader, outStream);
////			outgoingReader.close();
////		}
////		finally
////		{
////			if (serverWriter != null)
////			{
////				serverWriter.close();
////			}
////			if (outStream != null)
////				outStream.close();
////		}
////
////	}
////
////	public File exchangeFiles(File outgoing, File incoming) throws ConnectionException, IOException
////	{
////
////		try
////		{
////			this.connectionHandler.initConnection();
////			connectionHandler.setBinary();
////
////			this.sendFile(outgoing);
////
////			// explicitly connect to the server.
////			this.connectionHandler.connect();
////
////			this.getResponse(incoming);
////		}
////		finally
////		{
////			this.connectionHandler.closeConnection();
////		}
////
////		return incoming;
////	}
//
//}