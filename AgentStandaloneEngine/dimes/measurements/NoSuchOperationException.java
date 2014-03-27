/*
 * Created on 22/01/2004
 */
package dimes.measurements;

/**
 * @author anat
 */
public class NoSuchOperationException extends Exception
{

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage()
	{
		// TODO Auto-generated method stub
		return "No such operation:\t" + super.getMessage();
	}

}