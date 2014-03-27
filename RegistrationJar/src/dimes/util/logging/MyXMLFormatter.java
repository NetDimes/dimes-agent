/*
 * Created on 19/02/2004
 */
package dimes.util.logging;

import java.util.logging.Handler;
import java.util.logging.XMLFormatter;

/**
 * @author anat
 */
public class MyXMLFormatter extends XMLFormatter
{

	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#getTail(java.util.logging.Handler)
	 */
	public String getTail(Handler arg0)
	{
		// TODO Auto-generated method stub
		return "</log>\n";
	}
	/* (non-Javadoc)
	 * @see java.util.logging.Formatter#getHead(java.util.logging.Handler)
	 */
	public String getHead(Handler arg0)
	{
		// TODO Auto-generated method stub
		return "<log>\n";
	}

}