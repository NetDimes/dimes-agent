/*
 * Created on 15/02/2005
 */
package dimes.util;

import java.util.Hashtable;
import java.util.Set;

/**
 * skeleton of enum-like class. in order to get a class which functions similar to enum,
 * you should extend this class, and add desired enum values as fields. also add a static block
 * that fills this.names with the fields' names and values. 
 * @see dimes.scheduler.Priority
 * @author anat
 */
public abstract class MyEnum
{
	protected static Hashtable names = new Hashtable();

	public static String getName(int type)
	{
		String aName = (String) names.get(new Integer(type));
		if (aName == null)
			aName = "";//should check measurements - used to be "unknown"
		return aName;
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