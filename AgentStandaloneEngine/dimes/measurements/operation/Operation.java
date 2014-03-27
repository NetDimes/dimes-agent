package dimes.measurements.operation;
import java.util.Date;

import dimes.measurements.NoSuchOperationException;
import dimes.scheduler.Scheduler;
import dimes.scheduler.Task;

/**
 * @author anat
 */
public abstract class Operation
{
	
	private int uniqueID;
	
	private Task containingTask = null;
	private Date initialExecTime = null;

	public abstract boolean execute(Scheduler sched) throws NoSuchOperationException;
	public abstract boolean isTimedOperation();

	public Operation(Task task)
	{
		this(task , null);
	}
	public Operation(Task task, Date date)
	{
		containingTask = task;
		initialExecTime = date;
	}

	/**
	 * @return
	 */
	public Task getContainingTask()
	{
		return containingTask;
	}

	/**
	 * @return
	 */
	public Date getInitialExecTime()
	{
		return initialExecTime;
	}

	/**
	 * @param date
	 */
	public void setInitialExecTime(Date date)
	{
		initialExecTime = date;
	}
	
	public int hashCode(){
		return uniqueID;
	}
	
	public void setUniqueID(int uniqueId){
		this.uniqueID = uniqueId;
	}
	
	public int getUniqueID(){
		return this.uniqueID;
	}
	
}