/*
 * Created on 29/01/2004
 */
package dimes.scheduler;

/**
 * @author anat
 */
public class NoOperationsException extends DimesSchedulingException
{

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage()
	{
		// TODO Auto-generated method stub
		return "No more operations.\t";
	}

}