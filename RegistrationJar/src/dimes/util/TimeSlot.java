package dimes.util;

import java.util.Date;

/**
 * @author Ohad
 * 
 * class TimeSlot represents a multiple time slot, where 
 */
public class TimeSlot
{

	private Date currentTime = new Date();
	private long startTime;
	private long stopTime;

	public TimeSlot(long period)
	{
		startTime = new Date().getTime();
		stopTime = startTime + period;
	}

	public TimeSlot(long aStartTime, long aStopTime)
	{
		startTime = aStartTime;
		stopTime = aStopTime;
	}

	public TimeSlot()
	{
		startTime = 0;
		stopTime = Long.MAX_VALUE;
	}

	public boolean passed()
	{
		return (new Date().getTime() > stopTime);
	}

	public boolean goodToGo(long timeToCheck)
	{
		//		System.out.println("start time is :" + startTime);
		//		System.out.println("stop time is  :" + stopTime);
		//		System.out.println("now time is   :" + timeToCheck);
		boolean success = ((timeToCheck > startTime) && (timeToCheck < stopTime));
		//		System.out.println("returning:" + success);
		return success;
	}

	public boolean goodToGo()
	{
		long now = new Date().getTime();
		return goodToGo(now);
	}

	public void merge(TimeSlot slot2)
	{
		// TODO : implement this when required.
	}

	public boolean isUnlimited()
	{
		return (stopTime == Long.MAX_VALUE);
	}
}