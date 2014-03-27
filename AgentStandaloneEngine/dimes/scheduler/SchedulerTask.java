/*
 * Created on 21/03/2004
 */
package dimes.scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

//import dimes.gui.AgentFrame;
import dimes.measurements.Measurements;
import dimes.util.DelayedTimerTask;
import dimes.util.FileHandlerBean;
import dimes.util.Listener;
//import dimes.util.TimeSlot;
import dimes.util.comState.ComStateChangeEvent;
import dimes.util.comState.ComStateDetector;
import dimes.util.comState.ComStateEventListener;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.logging.ResultsAnnouncer;
import dimes.util.logging.ResultsManager;
import dimes.util.logging.ResultsMemoryHandler;
import dimes.util.logging.RotatingAnnouncingFileHandler;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

import java.util.Calendar;

/**
 * Task which is run by the timer every DELAY milliseconds. The run method of this task makes sure that conditions are right for running
 * a measurement (No measurements are already running, there is an available coonection) and then runs the next measurement. If there are
 * no measurements left to do, the run method attempts to get more work from the server, and failing that to run the default script. 
 * runs one Operation.
 *<p> 
 * Various changes were made in version 0.5.0 to allow running the default.in in case the server is down (see methods remarks)
 * @since 0.5.0
 * </p>
 * 
 * Extends DelayedTimerTask <p>
 * Implements ComStateEventListener, Listener
 * </P>
 * @author anat, idob (Version 0.5.0)
 * 
 *
 */
public class SchedulerTask extends DelayedTimerTask implements
		ComStateEventListener, Listener {

/*	Inherited from DelayedTimerTask:
    --------------------------------
    protected long delay = 30000;
	protected long period = 10000; //allows up to six measurements a minute
	protected String delayProperty = "delay";
	protected String periodProperty = "period";
*/
	private static final long MAX_SIZE_OF_OUT_DIRECTORY = 4000000;
	private static final int MINIMUM_ATTEMPTS_BEFORE_DEFAULT_RUN = 5;
	
	private static final Logger logger = Loggers.getLogger(SchedulerTask.class);
	
	private boolean internetConnectionExist = true; // Com State 
	private boolean fileSizeExceeded = false;
	private boolean notifiedCommunicationError = false;
	private int failedAttemptsCounter = 0; // A counter which is incremented in case of server failure. After 5 failures - The default script starts to run.
//	private String myName;	

	static Calendar cal = Calendar.getInstance();
	static long beginDate= cal.getTimeInMillis();
	
	
	private ComStateDetector comStateWatch;
//	private TimeSlot executionTimeSlot = new TimeSlot(); // unlimited timeslot.	
	private Scheduler sched;
//	private AgentFrame agentFrame = null;

//	private static boolean turbo = false; //TODO: debug, remove
//	private static int numOfMeasurements=0;
//	private static File statsFile;
//	private static int lastMinute = Calendar.getInstance().get(Calendar.MINUTE);//TODO: debug, remove
//	private static int lastHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

//	private static float[] statsArray = new float[24];
//	private static int[] mesurementsArray = new int[60];
	
	/**
	 * Constructor for SchedulerTask user in interactive mode (includes an AgentFrame)
	 * 
	 * Sets the delay and period for the task (Hard coded 30 sec and 15sec, respectivaly) 
	 * Checks the Internet connection and Handlers 
	 * 
	 * @param aSched The Scheduler 
	 * @param agentFrame The Agent Frame
	 */
//	public SchedulerTask(Scheduler aSched, AgentFrame agentFrame) {
//		this.delay = 30000; // start after 30 sec.
//		this.period = 15000;// call every 15 sec - minimum
//		this.delayProperty = PropertiesNames.SCHEDULER_DELAY/* "scheduler.delay" */;
//		this.periodProperty = PropertiesNames.SCHEDULER_PERIOD/* "scheduler.period" */;
//
//		this.sched = aSched;
//		comStateWatch = ComStateDetector.getInstance();
//		comStateWatch.addComStateEventListener(this);
//		internetConnectionExist = comStateWatch.connectionExists();
//
//		this.registerOnHandlers();// check
//
//		this.myName = this.getClass().getName();
//		this.agentFrame = agentFrame;
//	}

	/**
	 * Constructor for SchedulerTask user in non-interactive mode (no AgentFrame)
	 * 
	 * Sets the delay and period for the task (Hard coded 30 sec and 15sec, respectivaly) 
	 * Checks the Internet connection and Handlers
	 * 
	 * @param aSched The Scheduler 
	 */
	public SchedulerTask(Scheduler scheduler) {
		boolean turbo=false;
		try {
			turbo=Boolean.parseBoolean(PropertiesBean.getProperty(PropertiesNames.SCHEDULER_TURBO));
			
		} catch (NoSuchPropertyException e) {
			dimes.AgentGuiComm.GUICommunicator.sendLog(Level.SEVERE, "Error: Can not determine turbo status", "Turbo set to default: "+Boolean.toString(turbo));
		} 
		if (turbo){
			this.delay=3000; // still give it a bit of a head start
			this.period=2100;//can't have nonpositive value, so no zero
			
		}/*else{
			this.delay = 30000;// start after 30 sec.
			this.period = 12000;// call every 15 sec - minimum
		}*/
//		this.delayProperty = PropertiesNames.SCHEDULER_DELAY/* "scheduler.delay" */;
//		this.periodProperty = PropertiesNames.SCHEDULER_PERIOD/* "scheduler.period" */;

		this.sched = scheduler;
		comStateWatch = ComStateDetector.getInstance();
		comStateWatch.addComStateEventListener(this);
		internetConnectionExist = comStateWatch.connectionExists();
		this.registerOnHandlers();// check
//		this.myName = this.getClass().getName();

	}

/*	private void evalStats(){  //TODO:debug, remove
		numOfMeasurements++;
		Calendar now = Calendar.getInstance();
		int thisHour = now.get(Calendar.HOUR_OF_DAY);
		if(lastHour!=thisHour){
			FileWriter fw=null;
			try{
				fw = new FileWriter(Scheduler.statsFile,true);
				String entry = now.get(Calendar.DATE)+" "+now.get(Calendar.HOUR_OF_DAY)+","+numOfMeasurements+";\n";
				fw.append(entry);
				fw.flush();
				
			}catch(Exception e){}
			finally{
				try {
					fw.close();
					fw=null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			numOfMeasurements=1;
			lastHour=thisHour;
		}
		int thisMin = Calendar.getInstance().get(Calendar.MINUTE);  //TODO:debug, remove
		mesurementsArray[thisMin]++;
		if(!(lastMinute==thisMin)){
			System.out.println("Measurements min "+lastMinute+": "+mesurementsArray[lastMinute]+"\n");
			int mesurements=0;
			float minutes=0;
			int i=lastMinute;
			do{
			minutes++;
			mesurements+=mesurementsArray[i];
			i--;
			if(i<0)i=59;
				
			}while(mesurementsArray[i]!=0);
			float stat = mesurements/minutes;
			System.out.println("Avarage: "+stat+"\n Day Stats:");
			for (int q=0;q<24;q++){
				System.out.print("\t"+q);
				System.out.print("\n");
				System.out.print("\t"+statsArray[q]);
				System.out.print("\n");
			}
				
			lastMinute=thisMin;
			try{
				i=thisMin+1;
				if(i==60)i=0;
			if(mesurementsArray[i]!=0){
				statsArray[Calendar.getInstance().get(Calendar.HOUR_OF_DAY)]=stat;
				int j = mesurementsArray[thisMin];
				mesurementsArray=new int[60];
				mesurementsArray[thisMin]=j;
				}
			
			}catch(Exception e){
				mesurementsArray = new int[60];
			}
		}
	}*/
	
	/**
	 * Runs the measurement. 
	 * Checks first to see if there isn't a measurement already running and if there is there a connection, before running
	 * the next task. 
	 * 
	 * If there are no more tasks, this method initiates a call to the server in order to get the next script. If it can't
	 * get to the server, but determines that there is an Internet connection, it launches the default script. 
	 * 
	 */
	public void run() {
		
		long now = Calendar.getInstance().getTimeInMillis();
		System.out.println("Time from last call to SchedulerTask.run:"+ (now-beginDate)/1000 +" seconds");
		beginDate = now;
//		if (turbo)//TODO:debug,remove
//		{
//		evalStats();
		//}
		// 1st check: If Measurements are running there is nothing to do.
		if (Measurements.isExecuting()) // nextVer - lock better
			return;
		
		// 2nd check: If Measurements aren't running check if there is an Internet
		// connection.
		if (!this.internetConnectionExist) {
			if (!notifiedCommunicationError) {
				notifiedCommunicationError = true;
				logger.warning("Scheduler Task failed - No Communication. Please check Internet connection or proxy settings.");// debug
			}
			return;
		}

		try {
			try {
				// Run next measurement:
				this.sched.executeOperation();

				// After the measurement run - check if there is a necessity for send the results file:
				if (this.fileSizeExceeded) {
					this.handleFileSizeExceeded();
				}
			}catch (NoOperationsException e) { // sched.executeOperation() says there are no operations to run
				
				// If there is an Internet connection let's try to get a new
				// measurements script from server:
				if (internetConnectionExist) {
					boolean success = this.sched.sendReceive(/*true*/);
					if (!success) {
						failedAttemptsCounter++;
						logger.warning("Scheduler Task failed - No answer from server");
						System.out.println("No. of failures: " + failedAttemptsCounter); // Debug
						if (failedAttemptsCounter >= MINIMUM_ATTEMPTS_BEFORE_DEFAULT_RUN
								&& !Measurements.isExecuting() && internetConnectionExist) {
							this.runDefaultMeasurements();
						}
					} else {
						failedAttemptsCounter = 0;
/*						if( agentFrame != null )
							AgentFrame.statisticsPanel.updateStatistics();*/
						logger.info("Files exchange succeeded.");
						return;
					}
					// Case of no Internet Connection (internetConnectionExist == false):	
				} else {
					logger.warning("Scheduler Task failed - No Communication. Please check Internet connection or proxy settings.");// debug
					notifiedCommunicationError = true;
					return;
				}
			}
						
		} catch (Throwable e)// IOException / NoSuchPropertyException //todo
		// - should be Throwable (UnsatisfiedLinkError)
		{
			String errMsg = e.toString() + "\n\tAgent exiting...";
			StackTraceElement[] traceElems = e.getStackTrace();
			for (int i = 0; i < traceElems.length; ++i)
				errMsg += "\n" + traceElems[i].toString();
			SchedulerTask.logger.severe(errMsg);// debug
			e.printStackTrace();// debug
			this.cancel();
			System.exit(-1);
		}
	}

	/**
	 * A method which orders the scheduler to run the default script
	 * measurements. Called if there are no measurements to run and the connection to server can't be established.
	 * 
	 * @author idob
	 * 
	 * @since 0.5.0
	 */
	private void runDefaultMeasurements() {
		
		long outDirSize = 0;
		String resultsDirPath = null;
		failedAttemptsCounter = 0;
		
		try {
			resultsDirPath = PropertiesBean.getProperty(PropertiesNames.RESULTS_DIR);
		} catch (NoSuchPropertyException e) {
			logger.severe("Agent can not find the results dir. Please check possibility of properties file corruption.");
			throw new RuntimeException(e);
		}
		
		if (resultsDirPath == null) {
			logger.severe("Agent can not find the results dir. Please check possibility of properties file corruption.");
			return;
		}
		
		File outDir = new File(resultsDirPath);
		outDirSize = FileHandlerBean.getFolderSize(outDir);
		
		if (outDirSize > -1 && outDirSize < MAX_SIZE_OF_OUT_DIRECTORY) {
			logger.info("Running default measurements.");
			boolean success = this.sched.runDefaultScript(); //Note that this returns true if the default script has been successfully parsed and merged. It isn't neccessarily running yet.
			
			if (success) {
				logger.info("Default measurements are running."); //This is a bit of a lie. Default measurements are SCHEDULED to run now.
				fileSizeExceeded = false;	// Let them run for a little bit:
			} else if (!internetConnectionExist) {//Measurements didn't run and there is no connection
				logger.warning("No measurements will run until the connection to the Internet is established again.");
			} else {//Measurements didn't run, but there IS a net conenction. So the server is probably be down
				logger.severe("Agent can not run default measurements. The measurements will run again when connection to server will be renewed.");
			}
			
		}else {// Too many results - no more measurements before connection to server will reestablished.
			logger.warning("Agent will not run default measurements to avoid results files overflow. The measurements will run again when connection to server will be renewed.");
			fileSizeExceeded = true;
		}
		
	}

	/**
	 * This method is being called from run method when the results (*.out) file crossed the 250KB limit.
	 * In case that the connection can't be established there are 2 possible scenarios:
	 * 1. No Internet connection - In that case an attempts to send the file will be made every SchedulerTask iteration
	 *    until Internet connection will be re established (anyway no measurements can be done).
	 * 2. Server failure - In that case another try will be made when the next results file will be full - scheduler
	 * 	  will try to send both file. Meanwhile, since there is a connection to Internet measurements will keep on running.
	 * 
	 *    @author idob (version 0.5.0)
	 */
	private void handleFileSizeExceeded() {
		if (internetConnectionExist) {
			
			boolean exchangeSuccess = false;
			
			try {
				exchangeSuccess = this.sched.sendReceive();
			} catch (NoSuchPropertyException e) {
				logger.severe("A properties file corruption was detected. Please check it.");
				throw new RuntimeException(e);
			}
			
			// Even if there was no success in exchanging files - another try will be made with next file.
			// No need to try it every 15 sec.
			this.fileSizeExceeded = false;
			
			if (exchangeSuccess) {// success in exchanging files
				logger.info("Files exchange succeeded.");
/*				if( agentFrame != null )
					AgentFrame.statisticsPanel.updateStatistics();*/
			} else {// No success in exchanging files
				logger.info("Files exchange failed. Another attempt will be made later.");
			}
			
		} else {// No Internet connection (internetConnectionExist == false)
			// Get results from memory and save to file			
			if (ResultsManager.hasResultsPending())
				ResultsMemoryHandler.writeResultsToFile(ResultsManager.poolResults());
/*				try {
					File outputFile = FileHandlerBean.getAnOutgoingFile();
					BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile));
					String results = ResultsManager.poolResults();
					fileWriter.write(results);
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			this.fileSizeExceeded = true;
			notifiedCommunicationError = true;
			logger.warning("Scheduler Task denied - No Communication.  Please check Internet connection or proxy settings.");// debug
		}
		
	}

	/* (non-Javadoc)
	 * @see dimes.util.comState.ComStateEventListener#comStateChangeOccurred(dimes.util.comState.ComStateChangeEvent)
	 */
	@Override
	public void comStateChangeOccurred(ComStateChangeEvent evt) {
		
		logger.finest("Communication State Changed to :" + evt.isConnected);
		Measurements.ComStatceChangeOccurred(evt);
		this.internetConnectionExist = evt.isConnected;
		notifiedCommunicationError = false;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dimes.util.Listener#announce()
	 */
	public void listen(Object announcer) {
		System.out.println("DEBUGLOG scheduletask.listen() called");
		if (announcer instanceof ResultsAnnouncer) {
			
			this.fileSizeExceeded=true;
			this.handleFileSizeExceeded();
/*			SchedulerTask.logger.finest("rotating...");// debug
			this.fileSizeExceeded = true;
			Handler[] handlers = Loggers.getResultWriter().getHandlers();
			System.out.println("boo");*/
/*			for (int i = 0; i < handlers.length; ++i) {  //This for loop should have no effect. Boaz 0.5.5
				if ((handlers[i] instanceof RotatingAnnouncingFileHandler)
						&& (!handlers[i].equals(announcer))) {
					try {
						((RotatingAnnouncingFileHandler) handlers[i])
								.rotate(this);
					} catch (SecurityException e) {				
						e.printStackTrace();
					} catch (FileNotFoundException e) {						
						e.printStackTrace();
					}
				}
			}*/
			
			Handler[] handlers = Loggers.getLogger(this.getClass()).getHandlers();
			
			for (int i = 0; i < handlers.length; ++i) {
				if ((handlers[i] instanceof RotatingAnnouncingFileHandler)
						&& (!handlers[i].equals(announcer))) {
					try {
						((RotatingAnnouncingFileHandler) handlers[i])
								.rotate(this);
					} catch (SecurityException e) {				
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			

		}
	}

	private void registerOnHandlers() {
		boolean foundHandler = false;
		Handler[] handlers = Loggers.getResultWriter().getHandlers();
 
		for (int i = 0; i < handlers.length; ++i) {
		//	if (handlers[i] instanceof RotatingAnnouncingFileHandler)
			if (handlers[i] instanceof ResultsAnnouncer){
				//((RotatingAnnouncingFileHandler) handlers[i]).addListener(this);// check
				((ResultsAnnouncer) handlers[i]).addListener(this);// check
				foundHandler=true;
			}
		}
//		if(!foundHandler) Loggers.getResultsMemoryHandler().addListener(this); //If loggers didn't return the handler, get the Resultshandler manually
		
		handlers = Loggers.getLogger(this.getClass()).getHandlers();
		
		for (int i = 0; i < handlers.length; ++i) {
			if (handlers[i] instanceof RotatingAnnouncingFileHandler)
				((RotatingAnnouncingFileHandler) handlers[i]).addListener(this);//check
		}
		
//		ResultsDirectWriter.getInstance().addListener(this);

	}
}