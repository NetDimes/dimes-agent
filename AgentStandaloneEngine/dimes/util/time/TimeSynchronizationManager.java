/*
 * Created on 16/02/2006
 *
 */
package dimes.util.time;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;

/**
 * @author Ohad Serfaty
 *
 * a class responsible for static access of NTP timestamp and Dimes Server timestamp
 * 
 * NTP timestamp means the timestamp queried from network time server close to the agent.
 * 
 * Dimes Server timestamp is the timestamp on the dimes server , serving the scheduler
 * to loosely synchronize between agent measurements. 
 *
 */
public class TimeSynchronizationManager
{

	// server time diff :
	private static long dimesServerTimeDiff;
	
	// NTP time diff :
	private static double clockOffsetMiliSecs = 0;
	private static double clockErrorMiliSecs = 0;
	private static boolean isSynchronized = false;
	private static Vector ntpServersList=null;
	static boolean stop = false;
	private static long nanoZeroTime;
	private static long miliZeroTime;
	private static Logger logger = Loggers.getMeasurementConsoleLogger();
	
	public static native long nanoTime();		// compatible with Java 1.4

	public static void setDimesServerTime(long dimesServerTime){
		long currentTime = System.currentTimeMillis();
		TimeSynchronizationManager.dimesServerTimeDiff = dimesServerTime-currentTime;
	}
	
	public static long getDimesServerTime(){
		long currentTime = System.currentTimeMillis();
		return currentTime+dimesServerTimeDiff;
	}
	

	 static
	 {
	 	resetZeroTime();
	 }
	 
	 public static void resetZeroTime(){
	 	nanoZeroTime = 0; //TimeSynchronizationManager.nanoTime();
		miliZeroTime = System.currentTimeMillis();
	 }
	 
	/**
	 * Note that a check of isSynchronized() must be 
	 * made before accessing this method.
	 * 
	 * @return the time clock offset.
	 */
	public static int  getClockOffsetMSec() {
		return Math.round((float)clockOffsetMiliSecs);
	}
	
	/**
	 * @return
	 */
	public static int getClockErrorMSec()
	{
		return Math.round((float)Math.ceil(clockErrorMiliSecs));
	}
	
	public static long getOffsettedNanoToMiliTime(long aNanoTime){
//		System.err.println("First time mili :" + miliZeroTime + " first time nano:"+nanoZeroTime);
//		long time = miliZeroTime +((aNanoTime*1000 - nanoZeroTime)/1000000) + getClockOffsetMSec();
		long time = miliZeroTime +((aNanoTime - nanoZeroTime)/1000) + getClockOffsetMSec();
//		System.err.println("nano tiem:" + aNanoTime+ " time:"+time);
		return time;
	}
	
	public static long getNanoToMiliTime(long aNanoTime){
		long time = miliZeroTime +((aNanoTime - nanoZeroTime)/1000);
		return time;
	}
	
	/********
	 * 
	 * @return whether or not a synchronization has been made
	 */
	public static boolean isSynchronized(){
		return isSynchronized;
	}

	/**
	 * 
	 */
	public static int synchronize() throws TimeSyncException{
//		isSynchronized = true;
		logger.info("Synchronizing NTP Time Servers...");
		Iterator i = TimeSynchronizationManager.ntpServersList.iterator();		
		double currentRTT = Double.MAX_VALUE;
		double currentError = 0.0;
		double tempClockOffsetMiliSecs=0.0;
		boolean synchrizationSucceeded = false;
		if (ntpServersList.isEmpty())
			throw new TimeSyncException("No NTP Servers to synchronize with.");
			
		while (i.hasNext())
		{
			String sntpServer = (String) i.next();
			
			try
			{
				TimeSynchronizationResult result = SntpClient.connect(sntpServer);
				logger.fine("Synchronizing with "+sntpServer+"...\tOffset:" + result.milisecondOffset + " (Rtt:" +1000.0*result.roundTripTime+")" );	
				if (result.roundTripTime < currentRTT)
				{
					tempClockOffsetMiliSecs = (double)result.milisecondOffset;
					currentRTT = result.roundTripTime ;
					currentError = result.maxErrorTime;
				}
				synchrizationSucceeded=true;
			}
			catch (Exception e)
			{
				logger.finest("Synchronizing with "+sntpServer+" failed :"+e.getMessage());
			}
		}
		if (!synchrizationSucceeded)
			throw new TimeSyncException("NTP synchronization failed. Check UDP port 123 firewall blocking.");
		
		if (!isSynchronized)
		{
			isSynchronized = true;
			clockOffsetMiliSecs = tempClockOffsetMiliSecs;
			clockErrorMiliSecs = currentError;
			logger.info("Clock Synchronized. Setting offset=" +clockOffsetMiliSecs +" Best rtt:"+(1000.0*currentRTT)+" Clock Error :" +clockErrorMiliSecs );
		}
		else
		{
			System.out.println("Clock offset :" +tempClockOffsetMiliSecs +" Best rtt:"+(1000.0*currentRTT)
					+ " Error:"+currentError);
			double newClockOffsetMiliSecs = 0.875*clockOffsetMiliSecs + 0.125*tempClockOffsetMiliSecs;
			double newClockErrorMiliSecs =  0.875*clockErrorMiliSecs + 0.125*currentError;
			System.out.println("Offset turned from "+clockOffsetMiliSecs + " to "+newClockOffsetMiliSecs);
			System.out.println("Error turned from "+clockErrorMiliSecs + " to "+newClockErrorMiliSecs);
			clockErrorMiliSecs = newClockErrorMiliSecs;
			clockOffsetMiliSecs = tempClockOffsetMiliSecs;
			logger.info("Clock Synchronized. (Moving Average) offset=" +clockOffsetMiliSecs +" Best rtt:"+(1000.0*currentRTT)+" Clock Error :" +clockErrorMiliSecs );
		}
		// TODO : perhaps remove the reset into the measurement server/client code
		resetZeroTime();
		return Math.round((float)clockOffsetMiliSecs);
	}


	/**
	 * @param ntpServersList
	 */
	public static void setServersList(Vector ntpServersList)
	{
		TimeSynchronizationManager.ntpServersList = ntpServersList;
	}
	
	
	/**
	 * 
	 */
	protected static void stop()
	{
		stop = true;
		System.err.println("---"+ + getClockOffsetMSec());
		
	}

	/**
	 * @param i
	 * @throws TimeSyncException
	 */
	private static void synchronize(int trials) throws TimeSyncException
	{
		for (int i=0; i<trials;i++)
			synchronize();
	}


	
	public static void main(String[] args) throws TimeSyncException, InterruptedException
	{
		
//		System.out.println(nanoTime());
//		System.out.println(System.nanoTime());
		
		Vector vec = new Vector();
		vec.add("uk.pool.ntp.org");
		vec.add("il.pool.ntp.org");
		vec.add("ntp.ac.il");
		vec.add("us.pool.ntp.org");
		vec.add("de.pool.ntp.org");
		vec.add("ru.pool.ntp.org");
		vec.add("jp.pool.ntp.org");
		vec.add("timeserver.iix.net.il");
				vec.add("sg.pool.ntp.org");
				vec.add("br.pool.ntp.org");
				vec.add("au.pool.ntp.org");
				vec.add("nz.pool.ntp.org" );
				vec.add("uk.pool.ntp.org" );
				vec.add("za.pool.ntp.org");
				vec.add("be.pool.ntp.org");
				vec.add("se.pool.ntp.org" );
				vec.add("fr.pool.ntp.org" );
				vec.add("ch.pool.ntp.org" );
				vec.add("ca.pool.ntp.org" );
				vec.add("mx.pool.ntp.org");
		
		
		setServersList(vec);
		synchronize();
		

		System.err.println("First time mili :" + miliZeroTime + " first time nano:"+nanoZeroTime);
		new Thread(){
			public void run(){
			BufferedInputStream reader = new BufferedInputStream(System.in);
			try
			{
				reader.read();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			TimeSynchronizationManager.stop();
//			reader.readLine();
			}
		}.start();
		
//		while(!stop)
//		{
////			System.out.print(System.nanoTime() + getClockOffsetMSec()*1000000);
//		}
//		long time = miliZeroTime +((System.nanoTime() - nanoZeroTime)/1000000) + getClockOffsetMSec();
//		System.out.println("Time stopped :" + time);
	}
	
}
