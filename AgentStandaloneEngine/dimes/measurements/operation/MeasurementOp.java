package dimes.measurements.operation;

import java.util.Date;

import dimes.measurements.NoSuchOperationException;
import dimes.scheduler.Scheduler;
import dimes.scheduler.Task;

import java.util.Calendar;
/*
 * Created on 22/01/2004
 */

/**
 * @author anat
 */
public abstract class MeasurementOp extends Operation
{

	
	/**
	 * @param task
	 */
	public MeasurementOp(Task task)
	{
		super(task);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param task
	 * @param date
	 */
	public MeasurementOp(Task task, Date date)
	{
		super(task, date);
		// TODO Auto-generated constructor stub
	}

	public abstract boolean execute();

	/* (non-Javadoc)
	 * @see common.Operation#execute(scheduler.Scheduler)
	 */
	public boolean execute(Scheduler sched) throws NoSuchOperationException
	{

		return sched.execute(this);
	}

}