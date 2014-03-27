/*
 * Created on 15/02/2005
 */
package dimes.scheduler;

import java.util.Hashtable;
import java.util.Set;

/**
 * @author anat
 */
//todo - change to extend MyEnum after fixed
public class Priority //extends MyEnum
{
	protected static Hashtable names = new Hashtable();
	public static final int USER = 5;
	// steger new priority level added
	public static final int URGENT = 6;
	public static final int NORMAL = 10;
	public static final int LOW = 15;

	static
	{
		names.put(new Integer(USER), "USER");
		//steger
		names.put(new Integer(URGENT), "URGENT");
		names.put(new Integer(NORMAL), "NORMAL");
		names.put(new Integer(LOW), "LOW");
	}

	public static String getName(int type)
	{
		String aName = (String) names.get(new Integer(type));
		if (aName == null)
			aName = "";//should check measurements - used to be "unknown"
		return aName;
	}

	public static int getDefault()
	{
		return Priority.NORMAL;
	}
	public static Hashtable getNames()
	{
		return names;
	}

	public static Set getValues()
	{
		return names.keySet();
	}
	public static int size()
	{
		return names.size();
	}
}