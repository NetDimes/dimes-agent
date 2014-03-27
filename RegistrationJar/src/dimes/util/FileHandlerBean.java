/*
 * Created on 08/02/2004
 */
package dimes.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.io.IOUtils;

import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * Handles fetching of files (ingoing and outgoing), and has policies for
 * handling files which were used (delete, move, etc.). Gets the configuration-
 * specific data from the conf bean.
 *
 * @author anat
 * 
 * This class was extensively rewritten in ver. 0.5.0.<br>
 * Most of changes were made to adjust the class to work with a new folders tree in order to decrease the use of files in the agent.<br>
 * This way, the Agent has only 3 folders:<br>
 * <ol>
 * <li>Outgoing - The upper folder</li>
 * <li>Outgoing/log - The log folder</li>
 * <li>Outgoing/results - for the *.out files</li>
 * </ol>
 * 
 * Other changes (methods):
 * 
 * <ol>
 * <li>Creating cleanDirectories() method which is read from AppSplash in order to clean old log files and prepare .out files before sending</li>
 * <li>Canceling the use of in files - being saved as a String</li>
 * <li>closeOutFile() - checks if the *.out file is closed properly, otherwise fixing it. added to allow problems with the 
 * 		structure of out files which wasn't closed properly due to unexpected termination of the Java process</li>
 * <li>verifyWorkingDirectories() - Run in the beginning of the agent. Check for sure that the working directories do exists.</li>
 * <li>Creating one filter to be used in the class: WorkspaceFileFilter</li>
 * </ol>
 */
public class FileHandlerBean {

	private final String FAIL_EXT = ".fail";
	private final String SUCCESS_EXT = ".succ";
	private final String IN_EXT = ".in";
	private static final String OUT_EXT = ".out";
	private final String TMP_EXT = ".tmp";
	private final String LOGGER_LCK_EXT = ".lck";
	private final String GENERAL_EXT = "*";
	private static String Properties_File_Location="";

	/** Calls getOutgoing with the results directory name that was listed in properties.xml  
	 * @return
	 * @throws NoSuchPropertyException
	 */
	public File[] getNextOutgoing() throws NoSuchPropertyException {
		return this.getOutgoing(new WorkspaceFileFilter(PropertiesNames.RESULTS_DIR, OUT_EXT));
	}

	/**
	 * @param filter
	 * @return
	 * @throws NoSuchPropertyException
	 */
	public File[] getOutgoing(FilenameFilter filter) throws NoSuchPropertyException {
		
		File outgoingDir = new File(PropertiesBean.getProperty(PropertiesNames.OUTGOING_DIR/* "outgoing_dir" */));
		File[] dirs = outgoingDir.listFiles();
		Vector<File> files = new Vector<File>();
		
		for (int index = 0; index < dirs.length; ++index) {
			if (dirs[index].isDirectory())// subdirectory
				files.addAll(Arrays.asList(dirs[index].listFiles(filter)));
			else
				files.add(dirs[index]);// file
		}
		
		return files.toArray(new File[files.size()]); //We can do this because the vector is a vector of File
	}

	/**
	 * Prepare tmp files to be sent - closes xml tags and removes tmp
	 * extension. also deletes log files.
	 * 
	 * @author idob
	 * @since 0.5.0
	 * @throws SecurityException
	 * @throws IOException
	 * @throws NoSuchPropertyException
	 */
	public void cleanDirectories() throws SecurityException, IOException, NoSuchPropertyException {
		FileNameComparator nameCompare = new FileNameComparator();

		TreeSet<Object> fileSet = new TreeSet<Object>(nameCompare);
		Iterator<Object> fileIter = null;
		File aFile = null;

		//***************** Clean results directory ****************
		Object[] files = this.getOutgoing(new WorkspaceFileFilter(
				PropertiesNames.RESULTS_DIR, GENERAL_EXT));
		fileSet.addAll(Arrays.asList(files));
		fileIter = fileSet.iterator();
		while (fileIter.hasNext()) {
			aFile = (File) fileIter.next();
			// case of empty file:
			if (aFile.length() == 0) {
				System.out.print("Deleting File: " + aFile.getAbsolutePath());
				System.out.println(". File size: " + aFile.length());
				aFile.delete();
			} else {
				this.closeOutFile(aFile);
				this.turnToActive(aFile);
			}	
		}
		
		//***************** Clean log directory ****************
		fileSet = new TreeSet<Object>(nameCompare);

		files = this.getOutgoing(new WorkspaceFileFilter(PropertiesNames.LOG_DIR, GENERAL_EXT));
		fileSet.addAll(Arrays.asList(files));
		fileIter = fileSet.iterator();
//		if(fileIter.hasNext())
//			fileIter.next();
		while (fileIter.hasNext()) {
			aFile = (File) fileIter.next();
			aFile.delete();
		}		
	}

	
	/**
	 * This method was added in order to close out files which didn't close
	 * correctly due to process termination. Basically, it reads the .out file
	 * into a String, checks if it has been closed properly and closes it in
	 * case the answer is not. First, I tried to work with RandomAccessFile 
	 * but it added garbage - unwanted characters - to the file. Due to that
	 * I tried to use the regular Reader/Writer streams code - but the reading 
	 * was too slow. So I'm using org.apache.commons.io.IOUtils.
	 * 
	 *  @since 0.5.0
	 *  @author idob
	 */  
	private void closeOutFile(File aFile) throws IOException {
		FileReader outFileReader = null;
		BufferedWriter outFileWriter = null;
		String fileContent = "";
		String endOfFile = "</Results>";
		try {
			outFileReader = new FileReader(aFile);
			fileContent = IOUtils.toString(outFileReader);
			outFileReader.close();
			if( !fileContent.contains(endOfFile)) {
				System.out.print("closeOutFile. File Name: " + aFile.getAbsolutePath());
				fileContent += endOfFile;
				fileContent += "\n";
				outFileWriter = new BufferedWriter(new FileWriter(aFile));
				outFileWriter.write(fileContent);
				System.out.println("... Done.");
			}	
		} 
		finally {
			if ( outFileReader != null )
				IOUtils.closeQuietly(outFileReader);
			if ( outFileWriter != null )
				IOUtils.closeQuietly(outFileWriter);
		}
/*		These code took ages to run...                   */
//		BufferedReader outFileReader = null;
//		BufferedWriter outFileWriter = null;
//		String tmpLine = "";
//		String fileContent = "";
//		String endOfFile = "</Results>";
//		try {
//			outFileReader = new BufferedReader(new FileReader(aFile));
//			while( (tmpLine = outFileReader.readLine()) != null ) {
//				fileContent += tmpLine;
//				fileContent += "\n";
//			}
//			outFileReader.close();
//			if( !fileContent.contains(endOfFile)) {
//				System.out.print("closeOutFile. File Name: " + aFile.getAbsolutePath());
//				fileContent += endOfFile;
//				fileContent += "\n";
//				outFileWriter = new BufferedWriter(new FileWriter(aFile));
//				outFileWriter.write(fileContent);
//				System.out.println("... Done.");
//			}			
//		} catch (IOException e) {
//			Loggers.getLogger(this.getClass()).warning("Can not check tho out file: "+ e.getMessage());
//		} finally {
//			try {
//				if ( outFileReader != null )
//					outFileReader.close();
//				if ( outFileWriter != null )
//					outFileWriter.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}

	/*
	 * Clean all files from a specific directory
	 * 
	 * @param directory
	 *            the directory File.
	 * @throws Exception
	 */
	private void cleanFilesFromDir(File directory) throws Exception {
		if (!directory.isDirectory())
			throw new Exception("Invalid parameter : "
					+ directory.getAbsolutePath() + " Is not a directory");
		File[] filesInDir = directory.listFiles();
		for (int index = 0; index < filesInDir.length; ++index) {
			if (filesInDir[index].isDirectory())
				cleanFilesFromDir(filesInDir[index]);
			else
				filesInDir[index].delete();
		}
	}

	/**
	 * 
	 * Clean all files from the Working directories.
	 * 
	 * TODO - To check the call from dimes.measurements.treeroute.TreerouteStandaloneAgent
	 * Do we need this method? 
	 */
	public void cleanAll() {
		try {
			// Get directories:
			File historyDir = new File(PropertiesBean
					.getProperty(PropertiesNames.HISTORY_DIR));
			File logDir = new File(PropertiesBean
					.getProperty(PropertiesNames.LOG_DIR));
			// clean Files :
			cleanFilesFromDir(historyDir);
			cleanFilesFromDir(logDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * get a File handle to a file into which incoming data will be written. if
	 * already exists, it will be deleted first.
	 * 
	 * @param incomingDir
	 *            the dir that will contain the reponse file. if null, a default
	 *            path from the properties file will be used. otherwise, all
	 *            dirs in the path which do not exist will be created, and also
	 *            the file itself.
	 * @return File handle to the incoming file
	 * @throws IOException
	 */
	public File getIncomingFileSlot(File incomingDir) throws IOException,
			NoSuchPropertyException {
		if (incomingDir == null)
			incomingDir = new File(
					PropertiesBean
							.getProperty(PropertiesNames.OUTGOING_DIR/* "incoming_dir" */));
		if (!incomingDir.exists())
			incomingDir.mkdirs();
		return this.getDefaultIncomingSlot(incomingDir);
	}

	/**
	 * like getIncomingFileSlot(File incoming), only uses the default
	 * 
	 * @return
	 * @throws IOException
	 */
	public File getIncomingFileSlot() throws IOException,
			NoSuchPropertyException {
		return this.getIncomingFileSlot(null);
	}

	/**
	 * @param outgoing dir which will contain the file. this is required because
	 *            there are 2 outgoing dirs - log and results.
	 * @return
	 * @throws IOException
	 * @throws NoSuchPropertyException
	 */
	public File getOutgoingFileSlot(File outgoingDir) throws IOException,
			NoSuchPropertyException {
		if (outgoingDir == null)
			throw new IOException();// todo - handle error
		if (!outgoingDir.exists())
			outgoingDir.mkdirs();
		return this.getDefaultOutgoingSlot(outgoingDir);
	}

	public File getDefaultIncomingSlot(File incomingDir) throws IOException {
		File inFile = new File(incomingDir, String.valueOf(System
				.currentTimeMillis())
				+ this.IN_EXT + this.TMP_EXT);// check
		if (inFile.exists())
			inFile.delete();
		if (!inFile.createNewFile())
			throw new IOException("could not create new file: "
					+ inFile.getAbsolutePath());
		return inFile;
	}

	private File getDefaultOutgoingSlot(File outgoingDir) throws IOException {
		long timeStr = System.currentTimeMillis();
		File outFile = new File(outgoingDir, timeStr + "."
				+ outgoingDir.getName() + FileHandlerBean.OUT_EXT + this.TMP_EXT);// check
		Random randGen = new Random();
		while (outFile.exists()) {
			float randTime = timeStr + randGen.nextFloat();
			outFile = new File(outgoingDir, randTime + "."
					+ outgoingDir.getName() + FileHandlerBean.OUT_EXT + this.TMP_EXT);// check
		}
		// outFile.createNewFile();
		return outFile;
	}

	public String getOutgoingSuffix(String outgoingDirPath) {
		String outgoingDirName = (new File(outgoingDirPath)).getName();
		return outgoingDirName + FileHandlerBean.OUT_EXT + this.TMP_EXT;
	}

	public File addExtension(File aFile, String newExt) {
		File destFile = new File(aFile.getAbsolutePath() + newExt);
		if (!aFile.renameTo(destFile)) {
			Loggers.getLogger().debug(
					"can't rename " + aFile.getAbsolutePath() + " to "
							+ destFile.getAbsolutePath());// todo- erase
			aFile.delete();
		}
		return destFile;
	}

	public File removeExtension(File aFile) {
		String name = aFile.getAbsolutePath();
		int index = name.lastIndexOf('.');
		String newName;
		if (index < 0)
			newName = name;
		else
			newName = name.substring(0, index);
		File destFile = new File(newName);
		if (!aFile.renameTo(destFile))// check if should do anything
		{
			Loggers.getLogger().debug(
					"can't rename " + aFile.getAbsolutePath() + " to "
							+ destFile.getAbsolutePath());// todo- erase
		}

		return destFile;

	}

	private String getExtension(File aFile) {
		String name = aFile.getName();
		int index = name.lastIndexOf('.');
		String extension;
		if (index < 0)
			extension = "";
		else
			extension = name.substring(index);
		return extension;
	}

	public void handleAfterUsage(File aFile, boolean success)
			throws NoSuchPropertyException {
		if (aFile == null)
			return;

		// delete tmp files of the logger (.lck or .tmp.lck)
		File loggerTmpFile = new File(aFile.getAbsolutePath() + LOGGER_LCK_EXT);
		if (loggerTmpFile.exists())
			loggerTmpFile.delete();
		loggerTmpFile = new File(aFile.getAbsolutePath() + TMP_EXT
				+ LOGGER_LCK_EXT);
		if (loggerTmpFile.exists())
			loggerTmpFile.delete();

		String extension = this.getExtension(aFile);
		if (extension.compareTo(this.TMP_EXT) == 0) {
			aFile = this.turnToActive(aFile);
			extension = this.getExtension(aFile);
		}
		int policy = Integer.valueOf(
				PropertiesBean.getProperty(PropertiesNames.AFTER_USAGE/* "policies.after_usage" */
						+ extension)).intValue()/* this.config.getAfterUsagePolicy() */;
		switch (policy) {
		case (AfterUsage.DELETE):
			aFile.delete();
			break;
		case (AfterUsage.MOVE):
			File newDir = new File(
					PropertiesBean
							.getProperty(PropertiesNames.HISTORY_DIR/* "dirs.history" */));
			if (!newDir.exists())
				newDir.mkdirs();
			aFile.renameTo(new File(newDir, aFile.getName()));
			break;
		case (AfterUsage.RENAME):
			String newExt = success ? this.SUCCESS_EXT : this.FAIL_EXT;
			this.addExtension(aFile, newExt);
			break;
		case (AfterUsage.MOVE_RENAME):
			newDir = new File(
					PropertiesBean
							.getProperty(PropertiesNames.HISTORY_DIR/* "dirs.history" */));
			if (!newDir.exists())
				newDir.mkdirs();
			File newFile = new File(newDir.getAbsoluteFile() + File.separator
					+ aFile.getName());
			if (aFile.renameTo(newFile) == false)
				Loggers.getLogger().warn(
						"can't move\n\t" + aFile.getAbsolutePath() + " to\n\t"
								+ newFile.getAbsolutePath());
			newExt = success ? this.SUCCESS_EXT : this.FAIL_EXT;
			this.addExtension(newFile, newExt);
			break;
		}
	}

	public void handleAfterUsage(Object[] files, boolean success)
			throws /* NumberFormatException, */NoSuchPropertyException {
		for (int index = 0; index < files.length; ++index) {
			this.handleAfterUsage((File) files[index], success);
		}
	}

	public File turnToActive(File aFile) {
		if (!aFile.getName().endsWith(this.TMP_EXT))
			return aFile;
		return this.removeExtension(aFile);

	}

	/**
	 * This method was added in order to move the warranty of
	 * creating the necessary directories from the installer to the agent itself
	 * The method returns boolean value which is true if and only if all the
	 * necessary working directories where created for the first time. in this
	 * case, cleanWorkingDirectories won't be called - there is nothing to clean
	 * in new directories.
	 * 
	 * @since 0.5.0
	 * @author idob
	 */
	/**
	 * @throws NoSuchPropertyException
	 */
	public void verifyWorkingDirectories() {
		try {
			System.out.print("looking for necessary directories... ");
			File outgoingDir = new File(PropertiesBean
					.getProperty(PropertiesNames.OUTGOING_DIR));
			if (!outgoingDir.exists()) {
				outgoingDir.mkdirs();
				System.out.print("Outgoing was created... ");
			}
			File logDir = new File(PropertiesBean
					.getProperty(PropertiesNames.LOG_DIR));
			if (!logDir.exists()) {
				logDir.mkdirs();
				System.out.print("log was created... ");
			}
			File resultsDir = new File(PropertiesBean
					.getProperty(PropertiesNames.RESULTS_DIR));
			if (!resultsDir.exists()) {
				resultsDir.mkdirs();
				System.out.print("results was created... ");
			}
			File historyDir = new File(PropertiesBean
					.getProperty(PropertiesNames.HISTORY_DIR));
			if (!historyDir.exists()) {
				historyDir.mkdirs();
				System.out.print("History was created... ");
			}
			System.out.println("Done.");
		} catch (Exception e) {
			Loggers.getLogger()
					.warn(
							"could not create necessary directories :"
									+ e.getMessage());
			throw new RuntimeException(
					"could not create necessary working directories", e);
		}
	}
	
	/**
	 * A service method which gets an array of files objects and returns a reference to
	 * the newest file in the array.
	 * 
	 * @param files The array to be sorted.
	 * @return A reference to the newest file in the array.
	 * 
	 * @since 0.5.0
	 * @author idob
	 */
	public File getNewestFile(File[] files) {
		if( files.length == 0 || files == null )
			return null;
		else{
			FileNameComparator comparator = new FileNameComparator();
			Arrays.sort(files, comparator);
			return files[0];
		}
	}
	
	/**
	 * <p>
	 * A service method which was added in order to allow the SchedulerTask to check<br>
	 * if the out directory is not too big - in case of running the default script.<br>
	 * Can be used to any check of folder's size.
	 * </p> 
	 * 
	 * @param dir The directory to be checked.
	 * @return The total size of the files in the directory or -1 in case the parameter is null or is not a directory.
	 * @author idob
	 * @since 0.5.0
	 */
	public static long getFolderSize(File dir) {
		if( dir == null )
			return -1;
		File[] listFiles = dir.listFiles();
		if( listFiles == null )
			return -1;
		long amount = 0;
		for(int i = 0; i < listFiles.length; i++) {
			amount += listFiles[i].length();
		}
		return amount;
	}

	// *******************************************************************
	// ********************** Inner classes ******************************
	// *******************************************************************

//	

	/**
	 * This inner class is a replacement to TmpLogFileFilter, TmpResultFileFilter and
	 * ActiveFileFilter. The argument propName sign which directory is being
	 * checked. The aim is checking - and getting - all the .tmp files from 
	 * a directory, either the log one or the Outgoing one. 
	 * 
	 * @author: idob
	 * @since: 0.5.0
	 */
	private class WorkspaceFileFilter implements FilenameFilter {
		private String dir = null;
		private String extension = null;

		private WorkspaceFileFilter(String propName, String ext) {
			String path = null;
			try {
				path = PropertiesBean.getProperty(propName);
			} catch (NoSuchPropertyException e) {
				throw new RuntimeException("Can't get " + propName, e);
			}
			dir = cleanSlashesFromPath(path);
			extension = ext;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		public boolean accept(File d, String filename) {
			String dirPath = d.getAbsolutePath();
			dirPath = cleanSlashesFromPath(dirPath);
			if ( extension.equals("*") ) {
				if ((dirPath.equals(dir)))
					return true;
			}
			else {
				if ((dirPath.equals(dir)) && (filename.endsWith(extension)))
					return true;
			}			
			return false;
		}

		/*
		 * To avoid the problem that identical paths are marked as unequal due
		 * to the difference between slash and back slash, this methos is
		 * activated on the paths.
		 */
		private String cleanSlashesFromPath(String path) {
			if (path.contains("/"))
				path = path.replace("/", "");
			if (path.contains("\\"))
				path = path.replace("\\", "");
			return path;
		}

	}
	
	/**
	 * SetPropertiesLocation allows the program to set the Properties_File_Location just once. If it's already 
	 * set, it throws an exception. The variable itself is declared as static to prevent multiple locations.
	 * This mechanism is implemented in order to have just one place where this information is stored, with 
	 * different classes referring to here rather than args[0]
	 * @author Boazh
	 * @param Location The location of the properties.xml file
	 * @Since 0.5.1  
	*/ 
	public static void setPropertiesLocation(String Location) throws PropertyAlreadySetException{
		if (FileHandlerBean.Properties_File_Location.isEmpty()) FileHandlerBean.Properties_File_Location=Location;
		else throw new FileHandlerBean.PropertyAlreadySetException();
	}

	/** 
	 * 
	 * @return String Properties File Location or null if not set
	 */
	public static String getPropertiesLocation(){
		return Properties_File_Location;
	}
	
	/**Gives a File which can be used for outgoing data. We use this if we need to dump 
	 * measurements to a file while the Internet is down. 
	 * 
	 * @return File to write results to
	 * @throws SecurityException
	 * @throws FileNotFoundException
	 */
	public static File getAnOutgoingFile() 
	{
		String currFileName = String.valueOf(System.currentTimeMillis()) + FileHandlerBean.OUT_EXT;
		String outgoingDir="";
		try{
			outgoingDir= PropertiesBean.getProperty(PropertiesNames.RESULTS_DIR);
		}catch(NoSuchPropertyException nspe){}
		File newFile = new File(outgoingDir, currFileName);
		
		while (newFile.exists())
		{
			Random randGen = new Random();
			float randTime = System.currentTimeMillis() + randGen.nextFloat();
			currFileName = String.valueOf(randTime) + FileHandlerBean.OUT_EXT;
			newFile = new File(outgoingDir, currFileName);
		}	
		return newFile;	
	}

	
	@SuppressWarnings("serial")
	public static class PropertyAlreadySetException extends Exception{
		public PropertyAlreadySetException(){
			super("Property has alread been set");
		}
	}
	
	public class AfterUsage {
		static final int NOP = 0;

		static final int DELETE = 1;

		static final int MOVE = 2;

		static final int RENAME = 3;

		static final int MOVE_RENAME = 4;
	}
	
}