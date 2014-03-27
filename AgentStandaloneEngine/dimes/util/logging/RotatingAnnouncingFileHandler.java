/*
 * Created on 07/06/2004
 */
package dimes.util.logging;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import dimes.util.Announcer;
import dimes.util.Listener;

/**
 * logs to a rotating set of files, whose size is limited by byteLimit. number of files is unlimited.
 * names of files consist of a unique string (currently, the time in millis), and a suffix given
 * in the c'tor.
 * Announces when rotates file to all registered listeners
 * @author anat
 */
public class RotatingAnnouncingFileHandler extends RotatingFileHandler implements Announcer
{
	private HashSet listeners = new HashSet();//holds Listeners
	/**
	 * @param aDir
	 * @param aSuffix
	 * @throws IOException
	 * @throws SecurityException
	 */
	public RotatingAnnouncingFileHandler(String aDir, String aSuffix) throws IOException, SecurityException
	{
		super(aDir, aSuffix);
	}

	protected void rotate() throws SecurityException, FileNotFoundException
	{
		super.rotate();
		this.announce();
	}
	/* (non-Javadoc)
	 * @see java.util.logging.Handler#close()
	 */

	public void rotate(Listener aListener) throws SecurityException, FileNotFoundException
	{
		super.rotate();
	}
	public void announce()
	{
		Iterator iter = this.listeners.iterator();
		while (iter.hasNext())
		{
			Listener aListener = (Listener) iter.next();
			aListener.listen(this);
		}
	}

	/* (non-Javadoc)
	 * @see dimes.util.Announcer#addListener(dimes.util.Listener)
	 */
	public void addListener(Listener aListener)
	{
		this.listeners.add(aListener);
	}

	/* (non-Javadoc)
	 * @see dimes.util.Announcer#removeListener(dimes.util.Listener)
	 */
	public void removeListener(Listener aListener)
	{
		this.listeners.remove(aListener);
	}
}