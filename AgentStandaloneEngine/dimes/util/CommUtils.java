package dimes.util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import lib_odi.com.ch.odi.io.BandwidthLimitInputStream;
import lib_odi.com.ch.odi.io.BandwidthLimitOutputStream;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.AgentGuiComm.logging.Loggers;
////import dimes.util.logging.Loggers;

/*
 * Created on 03/02/2004
 */

/**
 * @author anat
 */

/* 
 * todo - should behave like an abstract factory: should return matching reader 
 * and writer, as well as using the appropriate method to write/read
 * for now - all methods return Buffered?, and use a specific stream
 */
public class CommUtils
{
	public static BufferedWriter getWriter(OutputStream outStream)
	{
		int rate = 0;
		try
		{
			rate = Integer.parseInt(PropertiesBean.getProperty(PropertiesNames.FILE_TRANSFER_RATE/*"fileTransfer.rate"*/));
		}
		catch (Exception e)
		{
			Loggers.getLogger().warning(e.toString());
		}
		return new BufferedWriter(new PrintWriter(new BandwidthLimitOutputStream(outStream, rate)));
	}

	public static BufferedReader getReader(InputStream inStream)
	{
		int rate = 0;
		try
		{
			rate = Integer.parseInt(PropertiesBean.getProperty(PropertiesNames.FILE_TRANSFER_RATE/*"fileTransfer.rate"*/));
		}
		catch (Exception e)
		{
			Loggers.getLogger().warning(e.toString());
		}
		return new BufferedReader(new InputStreamReader(new BandwidthLimitInputStream(inStream, rate)));
	}

	public static BufferedWriter getWriter(File aFile) throws IOException
	{
		return new BufferedWriter(new FileWriter(aFile));
	}

	public static BufferedReader getReader(File aFile) throws FileNotFoundException
	{
		return new BufferedReader(new FileReader(aFile));
	}

	public static void writeAll(BufferedReader reader, BufferedWriter writer) throws IOException
	{
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			writer.write(line);
			writer.newLine();
		}
		writer.flush(); //can this be used in case of writing to a file?
		reader.close();
		writer.close();
	}
	//writes without closing the writer and reader
	public static void writeContinuous(BufferedReader reader, BufferedWriter writer) throws IOException
	{
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			writer.write(line);
			writer.newLine();
		}
		writer.flush();

	}

	public static void writeAll(InputStream inStream, OutputStream outStream) throws IOException
	{
		BufferedInputStream buffInStream = new BufferedInputStream(inStream);
		BufferedOutputStream buffOutStream = new BufferedOutputStream(outStream);

		//		does not work with read(byte[])
		for (int byteData = buffInStream.read(); byteData != -1; byteData = buffInStream.read())
		{
			buffOutStream.write(byteData);
		}
		outStream.flush(); //can this be used in case of writing to a file?
		buffInStream.close();
		inStream.close();
		buffOutStream.close();
		outStream.close();
	}

	public static void writeBinary(FileInputStream reader, OutputStream writer) throws IOException
	{
		byte[] array = new byte[1];

		int readChar = 0;
		while ((readChar = reader.read(array)) != -1)
		{
			writer.write(array[0]);
		}
		writer.flush();
		reader.close();
		writer.close();
	}

	public static long ipToLong(String ip)
	{
		StringTokenizer prefixDotTokenizer = new StringTokenizer(ip, ".");
		long ipPart1 = Integer.parseInt(prefixDotTokenizer.nextToken());
		long ipPart2 = Integer.parseInt(prefixDotTokenizer.nextToken());
		long ipPart3 = Integer.parseInt(prefixDotTokenizer.nextToken());
		long ipPart4 = Integer.parseInt(prefixDotTokenizer.nextToken());

		long result = (ipPart1 << 24) + (ipPart2 << 16) + (ipPart3 << 8) + ipPart4;
		return result;
	}

	public static long ipToLongSafe(String ip)
	{
		try
		{
			if (!ip.matches("([0-9]*).([0-9]*).([0-9]*).([0-9]*)"))
				return 0;
			return ipToLong(ip);
		}
		catch (Exception e)
		{
			return 0;
		}
	}
	
	//TODO: DEBUG MAIN
	//Start-------
	
	public static void main(String[] args){
		System.out.println(ipToLongSafe(args[0]));
	}
		
	//End---------
}