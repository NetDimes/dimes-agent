/*
 * Created on 22/01/2004
 */
package dimes.scheduler;

import java.io.Reader;

//import org.dom4j.DocumentException;

import dimes.measurements.operation.Operation;
import dimes.scheduler.usertask.UserTaskSource;
import dimes.state.TaskManagerInfo;

/**
 * @author anat
 * Singleton class - connected to the single Scheduler.
 */
public final class OperationManager
{
	private static OperationManager instance = null;
	private Parser parser = null;
	private TaskManager taskManager = null;

	/**
	 * private constractor - singleton - can get an instance only through
	 * getInstance()
	 */
	private OperationManager()
	{
		instance = this;
		//todo - should Parser and TaskManager get references to <this>?
		parser = new Parser();
		taskManager = TaskManager.getInstance();
	}

	static OperationManager getInstance()
	{
		if (OperationManager.instance == null)
			new OperationManager();
		return OperationManager.instance;
	}

	/**
	 * can be used with any Reader, so reading from the command line
	 * is also possible, by wrapping the InputStream with InputStreamReader
	 *
	 * @param reader
	 * @throws DocumentException
	 */
	void handleResponse(Reader reader) //throws DocumentException
	{
		this.parser.parse(reader);
		this.taskManager.mergeAll(parser.getTasks());
	}

	Reader getMeasurementScript(String exId, String theScriptId, UserTaskSource commandLineTask)
	{
		return this.parser.getMeasurementScript(exId, theScriptId, commandLineTask);
	}
	Operation getNextOp()
	{
		return this.taskManager.getNextOp();
	}

	// todo - these next 2 shouldn't be here.
	//nextVer - will not be necessary once sending of files is parallel
/*	void resetFinished()
	{
		this.taskManager.resetFinished();
	}
*/
/*	public boolean areFinishedTasks()
	{
		return this.taskManager.areFinishedTasks();
	}*/

	public boolean areTasks()
	{
		return this.taskManager.areTasks();
	}

	public int getTaskNum()
	{
		return this.taskManager.getTaskNum();
	}

	/**
	 * @return
	 */
	public TaskManagerInfo getTaskManagerInfo()
	{
		return this.taskManager.getTaskManagerInfo();
	}

	/**
	 * 
	 */
	public void reset()
	{
		this.taskManager.resetAll();
	}

}