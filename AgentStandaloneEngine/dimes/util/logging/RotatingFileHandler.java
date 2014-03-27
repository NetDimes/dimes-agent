/*
 * Created on 07/06/2004
 */
package dimes.util.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;

import dimes.util.FileHandlerBean;

/**
 * logs to a rotating set of files, whose size is limited by byteLimit. number of files is unlimited.
 * names of files consist of a unique string (currently, the time in millis), and a suffix given
 * in the c'tor.
 * @author anat
 */
public class RotatingFileHandler extends FileHandler
{
	protected boolean isLogFile = false;
	protected final int byteLimit;
	protected final String directory;
	protected final String suffix;
	protected File currFile;

	protected FileHandlerBean fileBean; //todo - maybe split responsibilities better
	//separate this from other classes that aren't strictly logging related

	/**
	 * @param aDir
	 * @param aSuffix
	 * @throws IOException
	 * @throws SecurityException
	 */
	public RotatingFileHandler(String aDir, String aSuffix) throws IOException, SecurityException
	{
		super();
		this.fileBean = new FileHandlerBean();

		this.directory = aDir;
		this.suffix = "." + aSuffix;
		byteLimit = 250000;//250K
		this.updateNewFile();
	}
	

	/**
	 * Optional: A constructor for using an old log file.
	 * TODO - be aware that the new data is overriding the old one.
	 * 
	 * @param aDir
	 * @param aSuffix
	 * @param useOldFile
	 * @throws IOException
	 * @throws SecurityException
	 * 
	 * @since 0.5.0
	 * @author idob
	 */
	public RotatingFileHandler(String aDir, String aSuffix, boolean isLogFile) throws IOException, SecurityException{
		this.fileBean = new FileHandlerBean();
		this.directory = aDir;
		this.suffix = "." + aSuffix;
		if( !isLogFile ){
			this.byteLimit = 250000;//250K
			this.updateNewFile();
		} else {
			this.isLogFile = isLogFile;//true
			this.byteLimit = 1000000;//1MB
			this.useExistingLogFile();
		}
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	public synchronized void publish(LogRecord log)
	{
		int logLength = this.getFormatter().format(log).getBytes().length;
		if ((this.currFile.length() + logLength) > this.byteLimit)
		{
			try {
				this.rotate();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.publish(log);
	}
	
	public long getFileLength(){
		return this.currFile.length();
	}
	
	protected String getUniqueName()
	{
		return String.valueOf(System.currentTimeMillis());
	}

	/**
	 * Method which is used in order to write the log data to an
	 * existing file.
	 * 
	 * @throws IOException
	 * @throws SecurityException
	 * 
	 * @since 0.5.0
	 * @author idob
	 */
	protected void useExistingLogFile() throws IOException, SecurityException {
		File dir = new File(directory);
		if( !dir.isDirectory() )
			throw new IOException(directory + " is not a directory. Can not be used for storing a new rotating file.");
		File[] files = dir.listFiles();
		if( files.length == 0 )
			this.updateNewFile();
		else if( files.length == 1 ){
				currFile = files[0];
				setOutputStream(new FileOutputStream(currFile, true));
			} else {
				currFile = fileBean.getNewestFile(files);
				setOutputStream(new FileOutputStream(currFile, true));
				}
			
	}
	
	protected void updateNewFile() throws SecurityException, FileNotFoundException
	{
		String currFileName = this.getUniqueName() + this.suffix;
		this.currFile = new File(this.directory, currFileName);
		Random randGen = new Random();
		while (this.currFile.exists())
		{
			float randTime = Long.parseLong(this.getUniqueName()) + randGen.nextFloat();
			currFileName = String.valueOf(randTime) + this.suffix;
			this.currFile = new File(this.directory, currFileName);
		}	
		this.setOutputStream(new FileOutputStream(this.currFile));		
	}

	protected void rotate() throws SecurityException, FileNotFoundException
	{
		if(  this.currFile.getName().contains("log") )
			this.closeAndDelete();
		else
			this.close();
		this.updateNewFile();
	}
	/* (non-Javadoc)
	 * @see java.util.logging.Handler#close()
	 */
	public synchronized void close() throws SecurityException
	{
		super.close();
		this.fileBean.turnToActive(this.currFile);//check - should be activated here? causes problems when files are closed automatically on System.exit
	}
	
	
	/* (non-Javadoc)
	 * 	This method was added to allow rotate() method to delete the old log file during rotation.
	 * 	In case of handler closing through to Agent's stop the regular close() method - which doesn't
	 * 	delete the file - will be called, to assure the last log file still exists.
	 * 
	 * 	@since 0.5.0
	 *  @author idob 
	 */
	private void closeAndDelete() {
		super.close();
		this.currFile.delete();
	}

}