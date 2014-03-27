/*
 * Created on 22/08/2004
 */
package dimes.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author anat
 */
public class StreamGobbler extends Thread
{

	private final InputStream inputStream;

	private final OutputStream outputStream;

	public boolean shouldStop = false;

	public StreamGobbler(InputStream anInputStream, OutputStream anOutputStream)
	{
		this.setDaemon(true);
		this.inputStream = anInputStream;
		this.outputStream = anOutputStream;
	}

	public void run()
	{
		InputStreamReader isr = null;
		BufferedReader br = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		try
		{
			isr = new InputStreamReader(this.inputStream);
			br = new BufferedReader(isr);
			osw = new OutputStreamWriter(this.outputStream);
			bw = new BufferedWriter(osw);
			String line = null;
			while (!shouldStop && (line = br.readLine()) != null)
			{
				bw.write(line);
				bw.newLine();
				bw.flush();
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try
			{
				if (br != null)
					br.close();
				if (bw != null)
					bw.close();
				if (osw != null)
					osw.close();
				if (isr != null)
					isr.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}