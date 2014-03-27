package dimes.util.comState;

import java.util.LinkedList;
import java.util.Timer;
import java.util.logging.Logger;

import dimes.util.DelayedTimerTask;
//import dimes.util.logging.Loggers;
import dimes.AgentGuiComm.logging.Loggers;
import dimes.util.properties.PropertiesNames;

/**Class that interfaces with native methods to present the current state of the 
 * connection to the internet. Implemented as a singlton.
 * 
 * 
 * @author Ohad
 */
public class ComStateDetector
{

	private static ComStateDetector instance = null;
	private boolean isConnected = true;
	private Logger logger;	
	private static Timer timer = null;
	private static ComStateDetectorTimerTask task = null;
	private CommunicationDetector comDetector = null;//CommunicationDetector uses native methods to determine Internet connectivity.
//	private int siteToTry = 0;
	
	protected static LinkedList<ComStateEventListener> listenerList = new LinkedList<ComStateEventListener>();
	
	/***************
	 * this static method returns a static instance of the 
	 * ComStateDetector Class.
	 */
	public static ComStateDetector getInstance()
	{
		if (instance == null)
			new ComStateDetector();
		return instance;

	}

	/****************
	 * constructor for this singleton.
	 */
	private ComStateDetector()
	{

		logger = Loggers.getLogger(ComStateDetector.class);
		this.comDetector = new CommunicationDetector();
		instance = this;
	}

	/*****************
	 * this method is used in order to stop the communication Detector thread.
	 */
	public void stop()
	{
		logger.fine("Stopping ComStateDetector");//debug
		if (timer != null)
			timer.cancel();
		timer = null;
		instance = null;
		task = null;
	}

	/**
	 * builds the timer-delayed-task and schedules it.
	 */
	public void start()
	{
		logger.fine("Restarting ComStateDetector");//debug
		getInstance(); //init 1st instance, if doesn't exist.
		boolean identificationResult = getComState();
		isConnected = identificationResult;
		fireComStateChangeEvent(new ComStateChangeEvent(this, isConnected));
		timer = new Timer();
		task = new ComStateDetectorTimerTask();
		timer.schedule(task, task.getDelay(), task.getPeriod());

	}
	// This methods allows classes to register for MyEvents
	public void addComStateEventListener(ComStateEventListener listener)
	{
		logger.fine("Added " + listener + " to ComStateDetector listeners list"); // debug
		listenerList.add(listener);
	}

	// This methods allows classes to unregister for MyEvents
	public void removeMyEventListener(ComStateEventListener listener)
	{
		listenerList.remove(listener);
		logger.fine("Removed " + listener + " from ComStateDetector listeners list");
	}

	// This private class is used to fire MyEvents
	void fireComStateChangeEvent(ComStateChangeEvent evt)
	{
		logger.finest("Notifying Communication State to listeners : " + evt.isConnected); //debug
		Object[] listeners = listenerList.toArray();
		for (int i = 0; i < listeners.length; i++)
		{
			((ComStateEventListener) listeners[i]).comStateChangeOccurred(evt);
		}
	}

	private boolean getComState()
	{
		boolean comStatus = this.comDetector.isConnected();
		    	logger.finest("Communication Detected-->"+comStatus); //debug
		return comStatus;
	}

	/******************
	 * a method for examining the communication status from 
	 * the outside
	 * 
	 * @return true if a connection exists.
	 */
	public boolean connectionExists()
	{
		return getComState();
	}

	class ComStateDetectorTimerTask extends DelayedTimerTask
	{
		public ComStateDetectorTimerTask()
		{
			this.delay = 10000;
			this.period = 10000;
			this.delayProperty = PropertiesNames.COMM_STATE_DELAY/*"comState.delay"*/;
			this.periodProperty = PropertiesNames.COMM_STATE_PERIOD/*"comState.period"*/;
		}

		public void run()
		{
			boolean identificationResult = getComState();
			if (isConnected != identificationResult)
			{
				isConnected = identificationResult;
				if (isConnected)
					logger.info("Connection Detected.");//debug
				else
					logger.info("Dis-Connection Detected.");//debug
				fireComStateChangeEvent(new ComStateChangeEvent(this, isConnected));
			}
		}

	}

}