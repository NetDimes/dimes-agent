/*
 * Created on 16/02/2005
 */
package dimes.util;

/**
 * @author anat
 */
public interface Announcer
{
	public void addListener(Listener aListener);
	public void removeListener(Listener aListener);
	public void announce();
}