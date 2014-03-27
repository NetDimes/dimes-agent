/*
 * Created on 28/08/2005
 *
 */
package dimes.scheduler.usertask;

/**
 * @author Ohad Serfaty
 *
 */
public class StringFilter
{

	private final String[] filterStrings;

	StringFilter(String[] filterStrings)
	{
		this.filterStrings = filterStrings;
	}

	public boolean startsWith(String testString)
	{
		for (int i = 0; i < filterStrings.length; i++)
			if (testString.startsWith(filterStrings[i]))
				return true;
		return false;
	}

	public boolean endsWith(String testString)
	{
		for (int i = 0; i < filterStrings.length; i++)
			if (testString.endsWith(filterStrings[i]))
				return true;
		return false;
	}

}