package dimes.comm2server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.logging.Logger;

import dimes.util.FileHandlerBean;
import dimes.util.TimeSlot;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * @author Ohad
 * 
 * this class overrides the dimes.comm2server.Communicator class
 * it presents the 3 exchangeFiles methods that are exposed by
 * Comm2Server, and has it's own functionality within it's 
 * private methods.
 * Note : this class asserts that a communication failure will
 * thorw an IOException. however, those exceptions are caught 
 * and are thrown again.
 * Note: the maximum number of delays for backup is set as final.
 * 
 * private functionality :
 * 1. onBackupMode - whether the comm.op should be performed
 * 2. stopBackup - comm.op was performed correct and the backup
 *    mode can stop.
 * 3. resetBackup - the comm.op was delayed the appropriate number
 *    of times and it can now be performed again.
 * 4. resumeBackup - the comm.op is not ready to reset the backup,
 *    backup is resumed.
 * 
 */
public class ExponentialBackoffCommunicator extends CompressedCommunicator implements FileExchangeChannel
{

	int timesOfDelay = 1;
	int timesToWaitForEBTask;
	int timesAlreadyWaited = 0;
	final int MAX_NUMBER_OF_DELAYS = 256;
	final long DEFAULT_EXECUTION_TIME = 1500;
	TimeSlot executionTimeSlot;
	long trialSlot = 0;
	long delayPeriod = 30000;
	long tryTime = 0;

	//	logging
	private Logger logger;

	private FileHandlerBean fileHandler = null;

	/**
	 * 
	 * @param URLstr
	 * @param connectionType see ConnectionHandlerFactory (e.g. ConnectionHandlerFactory.SECURE_CONNECTION)
	 * @throws MalformedURLException
	 */
	public ExponentialBackoffCommunicator(String URLstr, int connectionType) throws MalformedURLException
	{
		super(URLstr, connectionType);
		executionTimeSlot = new TimeSlot();
		logger = Loggers.getLogger(this.getClass());

		this.fileHandler = new FileHandlerBean();
	}

	/*********************************
	 * this function overrides the class Communicator method
	 * exchangeFiles.
	 * @throws ConnectionException
	 */
	@Override
	public void exchangeFiles(BufferedReader outReader, OutputStream incomingStream, String header, String trailer) throws IOException, TaskBackedOffException
	{
		tryTime = new Date().getTime();
		if (onBackupMode())
			throw new TaskBackedOffException();

		boolean success = true;
		//  no backup : do the operation ...
		try
		{
			super.exchangeFiles(outReader, incomingStream, header, trailer);
		}
		catch (IOException e)
		{
			resumeBackup();
			success = false;
			throw e;
		}
		finally
		{
			if (success)
				stopBackup();
		}
	}

	/*********************************
	 * this function overrides the class Communicator method
	 * exchangeFiles.
	 * @throws ConnectionException
	 */
	@Override
	public File exchangeFiles(File outgoing, File incoming, String header, String trailer) throws IOException, TaskBackedOffException
	{
		tryTime = new Date().getTime();
		File resultFile = null;
		if (onBackupMode())
			throw new TaskBackedOffException();

		boolean success = true;
		//  no backup : do the operation ...
		try
		{
			resultFile = super.exchangeFiles(outgoing, incoming, header, trailer);
		}
		catch (IOException e)
		{
			// operation failed : resume backup.
			resumeBackup();
			success = false;
			throw e;
		}
		finally
		{
			if (success)
				// operation succeeded : undo backup
				stopBackup();
		}
		return resultFile;
	}

	/**
	 * send one file at a time
	 * @return null if unsuccessful
	 * @throws TaskBackedOffException if starting or in the middle of backoff
	 * 
	 */

	//	fixme - should be changed to enable calling both types of exchangeFiles - file/stream
	public File sendReceive(File aFile, String header, String trailer) throws NumberFormatException, NoSuchPropertyException, TaskBackedOffException
	{
		boolean success = false;
		File incoming = null;
		int trials = Integer.parseInt(PropertiesBean.getProperty(PropertiesNames.COMM_TRIALS_NUM/*"policies.comm.num_of_trials"*/));

		boolean backingOff = false;

		try
		{/*includes 1st try - before failure*/
			for (int trialNum = 0; ((incoming == null) || (incoming.length() == 0)) && (trialNum <= trials); ++trialNum)
			{
				this.fileHandler.handleAfterUsage(incoming, false);//handle previous try - first time is null

				/*
				 * exceptions: both fileHandler.getIncomingFileSlot and exchangeFiles throw IOException.
				 * All exceptions are sending trial failures, but failures resulting from fileHandler.getIncomingFileSlot
				 * should not cause the agent to start backoff.
				 * Therefore, a separate try-catch block surrounds exchangeFiles. in its catch clauses, it sets the backingOff flag,
				 * (either for TaskBackedOffException or for trialNum IOExceptions), and passes on the exception. 
				 * The containing try-catch clause handles all failures, and throws a TaskBackedOffException in case backingOff is true,
				 * to be caught by the calling class.
				 */
				try
				{
					incoming = this.fileHandler.getIncomingFileSlot();//cannot be null - if unsuccessful, throws exception
					try
					{
						incoming = this.exchangeFiles(aFile, incoming, header, trailer);
					}
					catch (TaskBackedOffException e)//exit method on the first time the exception is thrown 
					{
						backingOff = true;
						throw e;
					}
					catch (IOException e)
					{
						if (trialNum >= trials)
						//				    	   trialNum IOExceptions were thrown - should start to backoff
						{
							backingOff = true;
							//					    	     throw new TaskBackedOffException();
						}
						throw e;
					}
				}
				catch (IOException ex)
				{
					String msg = "send trial no." + (trialNum + 1) + " failed (" + ex.toString() + ")";
					msg += "\nClass name: " + ex.getClass().getName();
					ex.printStackTrace();
					//
					if (trialNum < trials)
						msg += "\n\t - trying again...";	
					this.logger.warning(msg); //debug
					if (backingOff)
						throw new TaskBackedOffException();
				}
			}
		}
		finally
		{
			if ((incoming == null) || (incoming.length() == 0))
				success = false;
			else
				success = true;

			if (!success)//comm failed
			{
				if ((incoming != null) && incoming.exists())
					incoming.delete();
				incoming = null;
			}
		}

		/*
		 * only if not backing off - should not handleAfterUsage any outgoing
		 * files while backing off
		 */
		if (!success)//comm failed
		{
			this.logger.warning("incoming file is null or empty");//debug
			this.fileHandler.handleAfterUsage(aFile, false);
		}
		else
		//comm successful
		{ 
			this.fileHandler.handleAfterUsage(aFile, true);
		}

		return incoming;
	}

	/**********************
	 * no need to backup any more. reset.
	 */
	private void stopBackup()
	{
		this.executionTimeSlot = new TimeSlot();
		timesOfDelay = 1;
		trialSlot = 0;
		logger.finer("Communication success");
	}

	/**********************
	 * multiply the backup-period.
	 * if the time-slot is unlimited, the communicating object is given
	 * a DEFAULT_EXECUTION_TIME period for trial communications.
	 * otherwise, backup is committed.
	 */
	private void resumeBackup()
	{
		if (tryTime < this.trialSlot)
			return;
		else
		{
			if (timesOfDelay < this.MAX_NUMBER_OF_DELAYS)
				timesOfDelay = timesOfDelay * 2;

			trialSlot = tryTime + DEFAULT_EXECUTION_TIME;
			this.executionTimeSlot = new TimeSlot(tryTime + (timesOfDelay * this.delayPeriod), tryTime + ((1 + timesOfDelay) * this.delayPeriod));
			logger.warning("Communication Failed. Backing up " + timesOfDelay + " Time slots");
		}
	}

	/*******************
	 * verify if the task was backed-up enough.
	 * 
	 * @return true if still needs backup. false if no need.
	 */
	private boolean onBackupMode()
	{
		if (tryTime < this.trialSlot)
			return false;
		if (executionTimeSlot.passed())
			executionTimeSlot = new TimeSlot();
		return (!this.executionTimeSlot.goodToGo());
	}

	/*****************
	 * this function will perform a dummy sendReceive attempt and return whether 
	 * the operation backed off or not.
	 * 
	 * shortly - it discovers whether the communicator is in back-off mode.
	 * 
	 * @return whether the dummyAttemt will succeed.
	 */
	public boolean dummyAttempt()
	{
		return onBackupMode();
	}

}

