/*
 * Created on 16/02/2005
 */
package dimes.scheduler;

/**
 * @author anat
 */
public class FileSizeExceededException extends DimesSchedulingException
{
	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage()
	{
		// TODO Auto-generated method stub
		return "File size exceeded.\t";
	}

}