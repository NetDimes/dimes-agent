/*
 * Created on 05/05/2004
 */
package dimes.util;

import java.io.File;
import java.util.Comparator;

/**
 * compares 2 files by their name.
 * @author anat
 * @return 0 if equal, a number less than 0 if arg0 is lexicographically smaller than arg1, 
 * and greater than 0 if larger.
 * Meaning, if the file names are numbers representing time, the result will be positive
 * if arg0 is earlier than arg1. 
 */
public class FileNameComparator implements Comparator<Object>
{

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1)
	{
		return -(((File) arg0).getName().compareTo(((File) arg1).getName()));
	}

}