/*
 * Created on 05/02/2004
 */
package dimes.util.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * prints a string as is.
 * @author anat
 */
public class NakedFormatter extends Formatter
{

	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	public String format(LogRecord record)
	{
		return record.getMessage();
	}
	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#getHead(java.util.logging.Handler)
	 */
	public String getHead(Handler arg0)
	{
		// TODO Auto-generated method stub
		return "<Results>\n";
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#getTail(java.util.logging.Handler)
	 */
	public String getTail(Handler arg0)
	{
		// TODO Auto-generated method stub
		return "</Results>\n";
	}

}