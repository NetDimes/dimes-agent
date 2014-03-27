package dimes.state;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.logging.Logger;

import dimes.Agent;
import dimes.state.handlers.KeepAliveHandler;
import dimes.util.DelayedTimerTask;
import dimes.util.comState.ComStateChangeEvent;
import dimes.util.comState.ComStateDetector;
import dimes.util.comState.ComStateEventListener;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/*
 * Created on 05/07/2004
 */

/**
 * This class was widely changed in version 0.5.0. The main change is that most of the functionality was exported
 * to KeepAliveHandler which is responsible for handling the KeepAlive send receive process and for calling other
 * handlers according to the answer.
 * 
 * @since 0.5.0
 * @author anat, idob (0.5.0)
 */
public class KeepAliveTask extends DelayedTimerTask {
	private Timer myTimer = null;
	private Agent agent = null;
	private TimeZone israelTimeZone = TimeZone.getTimeZone("Jerusalem");
	private Calendar calendar; 
	private int minutes;
	private boolean check;

	//	logging
	private static final Logger logger = Loggers.getLogger(KeepAliveTask.class);

	//	Com State :
	private boolean internetConnectionExist = true;
	private ComStateDetector comStateWatch;
	
	// handler:
	KeepAliveHandler keepAliveHandler;

	public KeepAliveTask(Timer aTimer, Agent theAgent) throws MalformedURLException
	{
		this.delay = 30000;//start after 30 secs
		this.period = 1000 * 60 * 5; //call max every 5 minutes		
//		try {
			this.delayProperty = /*PropertiesBean.getProperty(*/PropertiesNames.KEEPALIVE_DELAY;//);
			this.periodProperty = /*(Integer.parseInt(PropertiesBean.getProperty(*/PropertiesNames.KEEPALIVE_PERIOD;/*))>50000)?PropertiesNames.KEEPALIVE_PERIOD:"50000";
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//Hard limit on keepalive time
*/
		// Com State :
		comStateWatch = ComStateDetector.getInstance();
		comStateWatch.addComStateEventListener(
				new ComStateEventListener() {
					/*
					 * this method is invoked whenever a communication status has changed.
					 * 
					 * @param evt - indicates whether connected or not.
					 */
					public void comStateChangeOccurred(ComStateChangeEvent evt)
					{
						KeepAliveTask.this.internetConnectionExist = evt.isConnected;
					}
				});
		internetConnectionExist = comStateWatch.connectionExists();

		this.myTimer = aTimer;
		this.agent = theAgent;
	
		// handler:
		keepAliveHandler = new KeepAliveHandler(this.agent);
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	//As of 0.5.2, The keepalive task also checks to see if the statistics need to be updated. - BoazH
	public void run()
	{
		logger.info("Running KeepAlive Task");
		if (this.agent.getKeepAliveTimerThread() == null)
			this.agent.setKeepAliveTimerThread(Thread.currentThread());

		if (this.internetConnectionExist){
			this.keepAliveHandler.sendReceiveKeepAlive();
			calendar = Calendar.getInstance(israelTimeZone);
			minutes = calendar.get(Calendar.MINUTE);
//			logger.info("System minutes:"+minutes+" Check:"+Boolean.toString(check));  //Debug
/*			if( minutes >= 40 && check) {
				logger.info("Time To update the Statistics");
				this.agent.getStats();
				check=false;
				}
			else{
				if (minutes<40 && !(check))
					check=true;
//					logger.info("Check is:"+check);  //Debug
				}*/
			
		}
		else
			logger.warning("KeepAlive Message was not sent to the Server - \n Check Internet connection and Proxy settings.");		
	}
}