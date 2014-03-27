/*
 * Created on 08/08/2004
 */
package dimes.util;


import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Logger;

import dimes.platform.PlatformDependencies;
//import dimes.util.logging.Loggers;
import dimes.AgentGuiComm.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * @author anat
 */
public class LibraryLoader
{
	private static String[] libraryNames = null;

	//  logging
	private static String myName = "dimes.util.LibraryLoader";
	private static Logger logger = Loggers.getLogger(LibraryLoader.class);
	private static String libraryExtensions;

	public static void load()
	{
		libraryNames = PlatformDependencies.currentLibraryNames;
		libraryExtensions = PlatformDependencies.currentLibraryExtension;
		String dir = "";
		try
		{
			dir = PropertiesBean.getProperty(PropertiesNames.RESOURCES_DIR/*"dirs.resources"*/);
		}
		catch (NoSuchPropertyException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.warning(e.toString());
			dir = ".\\resources";
		}

		for (int i = 0; i < libraryNames.length; ++i)
		{
			String latestLib = getLatestFileName(dir, libraryNames[i], libraryExtensions);
			try
			{
				System.load(latestLib);
				logger.fine("loaded " + latestLib);//debug
			}
			catch (Throwable e1)
			{
				logger.warning("Error While loading library " + latestLib + ": " + e1.toString());
			}
		}

		//        // initialize ResourceManager with resources from resources directory
		//        ResourceManager.addResources(dir);        

	}

	//todo - maybe move generic version to FileHandlerBean
	/*
	 * returns latest file in the specified dir with the specified extension. Latest means 
	 * greatest lexicographically.
	 */
	protected static File getLatestFile(String dirPath, String baseName, String extension)
	{
		logger.finest("looking for latest " + baseName + " File in : " + dirPath);//debug
		File dir = new File(dirPath);
		File[] files = dir.listFiles(//all files in classBase should have the specified extension
				new ExtensionFileFilter(baseName, extension));
//		File[] files = dir.listFiles();
		try
		{
			if (files == null)
				throw new Exception("not a directory: " + dirPath);
			if (files.length < 1)//this isn't a 1st run but there's only 1 JAR 
				throw new Exception("not enough files in dir " + dirPath + ": " + files.length);
		}
		catch (Exception e)
		{
			/*System.err.println*/logger.fine("Error While looking for Resources: Couldn't find " + baseName + "\nError: " + e.toString());//debug
			return null;
		}
		Arrays.sort(files);//sorts lexicographically

		File latestFile = files[files.length - 1];
		logger.fine("Found latest file: " + latestFile.getAbsolutePath());//debug
		return latestFile;
	}

	protected static String getLatestFileName(String dirPath, String baseName, String extension)
	{
		System.out.println("LibraryLoader.getLatestFileName");
		System.out.println("dirPath: "+dirPath);
		System.out.println("baseName: "+baseName);
		System.out.println("extension: "+extension);
		File latestFile = getLatestFile(dirPath, baseName, extension);
		return latestFile.getAbsolutePath();
	}

	private static class ExtensionFileFilter implements FileFilter
	{
		private final String extension;
		private final String baseName;

		public ExtensionFileFilter(String aBaseName, String anExtension)
		{
			this.extension = anExtension;
			this.baseName = aBaseName;
		}

		/* (non-Javadoc)
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File pathname)
		{
			String name = pathname.getName();
			if (name.startsWith(this.baseName) && name.endsWith(this.extension))
				return true;
			return false;
		}

	}
	



}