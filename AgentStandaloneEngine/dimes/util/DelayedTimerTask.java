/*
 * Created on 26/07/2004
 */
package dimes.util;

import java.util.TimerTask;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;

/**
 * @author anat
 */
public abstract class DelayedTimerTask extends TimerTask
{
	//default values - should be initialized by extending class
	protected long delay = 30000;
	protected long period = 10000;  //Allows upto six measurements a minute

	protected String delayProperty = PropertiesNames.SCHEDULER_DELAY;
	protected String periodProperty = PropertiesNames.SCHEDULER_PERIOD;

	/**
	 * @return Returns the delay. Imposes a hard coded lower boundary on the delay.
	 */
	public long getDelay()
	{
		long foundDelay = this.delay;
		try
		{
			foundDelay = Long.parseLong(PropertiesBean.getProperty(this.delayProperty));
		}
		catch (Exception e)
		{
			Loggers.getLogger().warning(e.toString());//debug
		}
		if (foundDelay > this.delay)
			return foundDelay;
		return this.delay;
	}
	/**
	 * @return Returns the period. Imposes a hard coded lower boundary on the period.
	 */
	public long getPeriod()
	{
		long foundPeriod = this.period;
		try
		{
			foundPeriod = Long.parseLong(PropertiesBean.getProperty(this.periodProperty));
		}
		catch (Exception e)
		{
			Loggers.getLogger().warning(e.toString());//debug
		}
		if (foundPeriod > this.period)
			return foundPeriod;
		return this.period;
		
	}
}