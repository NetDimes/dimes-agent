/*
 * Created on 10/08/2004
 */
package dimes.util;

/**
 * @author anat
 */
public class Lock
{
	public synchronized void waitFor()
	{
		try
		{
			wait();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public synchronized void release()
	{
		notifyAll();
	}

}