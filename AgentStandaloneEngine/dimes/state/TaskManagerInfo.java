/*
 * Created on 15/05/2005
 */
package dimes.state;

import dimes.scheduler.TaskManager;

/**
 * functions as an interface to a single method in TaskManager: getTaskInfo.
 * this way, the xml info can be extracted by classes that shouldn't have direct access
 * to TaskManager, on the one hand, and still avoid the long chain it should travel to reach
 * the methods in TaskManager.
 * @author anat
 */
public class TaskManagerInfo
{
	private TaskManager taskManager;

	public TaskManagerInfo(TaskManager aTaskManager)
	{
		this.taskManager = aTaskManager;
	}

	public String getInfo()
	{
		return this.taskManager.getTaskInfo();
	}
}