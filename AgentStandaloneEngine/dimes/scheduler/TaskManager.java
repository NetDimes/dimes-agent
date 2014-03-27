/*
 * Created on 22/01/2004
 */
package dimes.scheduler;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import dimes.measurements.operation.Operation;
import dimes.state.TaskManagerInfo;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;

/**
 * @author anat
 */
public final class TaskManager
{
	private static TaskManager instance = null;
	//	contains Vectors of Tasks. Each Vector contains Tasks that have a specific priority.
	//	needs to be TreeMap in order for the iterator traversal to be in ascending order of priority (key)
	private TreeMap<Integer, Vector<Task>> prioritizedTasks;
	private HashSet<Task> finishedTasks;
	private HashSet<Task> finishedTasksInfo;
	private int taskNum = 0;
	//	keeps mapping between priority and last index of task in that priority which was chosen in 
	//	pickNextFromPriority. Used for traversing in round-robin over Tasks of the same priority.
	private static Hashtable<Integer, Integer> pickingIndexMap = new Hashtable<Integer, Integer>();

	private Logger logger;

	private TaskManager()
	{//nextVer - change the type of <tasks>
		prioritizedTasks = new TreeMap<Integer, Vector<Task>>();//create inner Hashtables when Tasks arrive
		instance = this;

		finishedTasks = new HashSet<Task>();
		finishedTasksInfo = new HashSet<Task>();
		this.logger = Loggers.getLogger(this.getClass());
	}

	static TaskManager getInstance()
	{
		if (TaskManager.instance == null)
			new TaskManager();
		return TaskManager.instance;
	}

	void mergeAll(TreeMap<Integer, Vector<Task>> newTasks)
	{
		Set<Integer> newPriorities = newTasks.keySet();
		Iterator<Integer> newPrioIter = newPriorities.iterator();
		while (newPrioIter.hasNext())
		{
			Integer prio = newPrioIter.next();
			Vector<Task> prioTasks = prioritizedTasks.get(prio);
			if (prioTasks == null)//create priority Vector if doesn't exist
			{
				prioTasks = new Vector<Task>();
				this.prioritizedTasks.put(prio, prioTasks);
				this.logger.finest("adding new prioTasks - prio: " + prio);//debug
			}
			Vector<Task> newPrioTasks = newTasks.get(prio);
			this.logger.finest("\tadding to prio " + prio + ": ");//debug

			for (int i = 0; i < newPrioTasks.size(); ++i)
			{
				Task aTask = newPrioTasks.get(i);
				this.logger.finest("\t\tid - " + aTask.getUniqueIdentifier() + ", priority - " + aTask.getPriority());//debug
			}
			prioTasks.addAll(newPrioTasks);
			this.taskNum += newPrioTasks.size();
		}
	}
	/**
	 * @return true if there were any finished Tasks to remove
	 */
	private boolean removeFinishedTasks()
	{
		boolean wereFinished = false;
		if (!this.areFinishedTasks())/*(this.finishedTasks.size() == 0)*/
			return wereFinished;
		Iterator<Task> finishedIter = this.finishedTasks.iterator();
		while (finishedIter.hasNext())
		{
			this.logger.finest("have finished");//debug
			Task aFinishedTask = finishedIter.next();
			Integer prio = new Integer(aFinishedTask.getPriority());
			Vector<Task> prioTasks = this.prioritizedTasks.get(prio);

			if (prioTasks == null)//debug
			{
				this.logger.finest("prioTasks is null");//debug
				continue;
			}
			else
				//debug
				if (!prioTasks.contains(aFinishedTask))//shouldn't get here
				{
					this.logger.finest("prioTasks doesn't contain task " + aFinishedTask.toString() + " and priority " + prio + ". this Vector contains:");//debug
					for (int i = 0; i < prioTasks.size(); ++i)
					{
						Task aTask = (Task) prioTasks.get(i);
						this.logger.finest("\ttask id: " + aTask.getUniqueIdentifier() + ", priority: " + aTask.getPriority());//debug
					}
				}
				else
				{
					//	                Integer finishedTaskPrio = new Integer(aFinishedTask.getPriority());
					int finishedTaskIndex = prioTasks.indexOf(aFinishedTask);
					Integer prioPickingIndex = pickingIndexMap.get(prio);
					/*
					 * if finished task that was pointed to by pickingIndex, need to point to 
					 * the same index, since other Tasks will be moved to start.
					 * just decrease the relevant pickingIndex, so that in the next call
					 * to pickNextFromPriority it will be increased back to its original location
					 */
					if (prioPickingIndex != null)
					{
						int prioPickingIndexVal = prioPickingIndex.intValue();
						if (finishedTaskIndex == prioPickingIndexVal)
							pickingIndexMap.put(prio, new Integer(prioPickingIndexVal - 1));
					}
					prioTasks.remove(aFinishedTask);
					/*System.out.println*/logger.fine("removing task: " + aFinishedTask);//debug
					--this.taskNum;
					if (prio.intValue() != Priority.USER)//ignore user traces for sendReceive
						wereFinished = true;

					if (prioTasks.size() == 0)
						this.resetPriority(prio);//check
						//	                    this.prioritizedTasks.remove(prio);//if priority container is empty - remove it
				}

		}
		this.resetFinished();
		this.logger.finest("exiting remove finished tasks");//debug
		return wereFinished;
	}

	//	nextVer - will not be necessary once sending of files is parallel	
	void resetFinished()
	{
		this.finishedTasksInfo.addAll(this.finishedTasks);
		this.finishedTasks.clear();
	}

	void resetFinishedInfo()
	{
		this.finishedTasksInfo.clear();
	}
	//	nextVer - will not be necessary once sending of files is parallel	
	public boolean areFinishedTasks()
	{
		return (this.finishedTasks.size() > 0);
	}

	private boolean areFinishedInfo()
	{
		return (this.finishedTasksInfo.size() > 0);
	}

	public boolean areTasks()
	{
		return (this.taskNum > 0);
	}

	public int getTaskNum()
	{
		return this.taskNum;
	}

	public String getTaskInfo()
	{
		String xml = "";
		Iterator<Vector<Task>> prioIter = this.prioritizedTasks.values().iterator();
		while (prioIter.hasNext())
		{
			Vector<Task> tasksPerPrio = prioIter.next();
			for (int i = 0; i < tasksPerPrio.size(); ++i)
				xml += ((Task) tasksPerPrio.get(i)).toXML();
		}

		if (this.areFinishedInfo())
		{
			Iterator<Task> finishedIter = this.finishedTasksInfo.iterator();
			while (finishedIter.hasNext())
			{
				xml += finishedIter.next().toXML();
			}
			this.resetFinishedInfo();//check
		}
		return xml;

	}

	Operation getNextOp()
	{
		Set<Integer> priorities = this.prioritizedTasks.keySet();
		Vector<Task> prioTasks;
		Iterator<Integer> prioIter = priorities.iterator();
		Operation result = null;
		TreeMap<Integer, LinkedList<Operation>> operations = new TreeMap<Integer, LinkedList<Operation>>();
		while (prioIter.hasNext())
		{
			Integer prio = prioIter.next();
			this.logger.finest("getting next op from tasks at prio " + prio);//debug
			prioTasks = this.prioritizedTasks.get(prio);
			if (prioTasks.size() == 0)//shouldn't happen
				this.resetPriority(prio);

			LinkedList<Operation> prioOps = new LinkedList<Operation>();
			for (int i = 0; i < prioTasks.size(); ++i)
			{
				Task aTask = prioTasks.get(i);
				this.logger.finest("getting next op from task id " + aTask.getUniqueIdentifier());//debug
				Operation op = aTask.getNextOp();
				if (op != null)
					prioOps.add(op);
				else
				{
					if (aTask.taskFinished())
					{
						this.finishedTasks.add(aTask);
						this.logger.finest("putting Task id " + aTask.getUniqueIdentifier() + " priority " + aTask.getPriority() + " in finishedTasks");//debug
					}
				}
			}
			operations.put(prio, prioOps);
		}
		try
		{
			//		    remove finished Tasks but reset only after sending their status in the keepalive 
			if (this.removeFinishedTasks()){ //there were finished Tasks
				result = null; //make sure we call sendReceive next time we try to execute, to get scripts instead of those removed
				Scheduler.setNeedWork(); //TODO:CHECK - Boaz 5.5
				}
			else
				result = this.pickNext(operations);
		}
		catch (NoOperationsException e)
		{
			result = null;
		}
		return result;
	}

	//check - trying to pick from priority groups now

	/**
	 * @param prioritizedOps Hashtable where the key is priority and the value is a Vector of
	 * ops that are all of the same priority.
	 * @return the next Operation to be executed.
	 * @throws NoOperationsException
	 */
	private Operation pickNext(TreeMap<Integer, LinkedList<Operation>> prioritizedOps) throws NoOperationsException
	{
		//nextVer - should implement checking of time etc. could use sorted
		//set. chooses now a random task operation from the set.
		Operation chosenOp = null;
		if (prioritizedOps.size() == 0)
		{
			this.logger.finest("no operations to pick from");//debug
			throw new NoOperationsException();
		}
		Iterator<Integer> prioIter = prioritizedOps.keySet().iterator();
		while (prioIter.hasNext())
		{
			Integer prio = prioIter.next();
			LinkedList<Operation> prioTasks = prioritizedOps.get(prio);
			chosenOp = this.pickNextFromPriority(prio, prioTasks);
			if (chosenOp != null)
				break;//choose according to priority first
		}
		if (chosenOp == null)
			throw new NoOperationsException();
		return chosenOp;
	}

	private Operation pickNextFromPriority(Integer prio, LinkedList<Operation> prioOps)
	{
		if (prioOps.size() == 0)
			return null;
//		// remove all time operations :
		LinkedList<Operation> timedOperations = new LinkedList<Operation>();
		for (Iterator<Operation> i = prioOps.iterator(); i.hasNext();){
			Operation op = i.next();
			if (op.isTimedOperation())
			{
				timedOperations.addLast(op);
				i.remove();
			}
		}
		// insert all timed operations at the begining , in the same order as before...
		while (!timedOperations.isEmpty()){
			prioOps.addFirst(timedOperations.removeLast());
		}
		int lastIndexVal=-1;
			Integer lastIndex = pickingIndexMap.get(prio);
			if (lastIndex != null)
			{
				lastIndexVal = lastIndex.intValue();
				++lastIndexVal;//round robin
				if ((lastIndexVal >= prioOps.size()) || (lastIndexVal < 0))//circular
					lastIndexVal = 0;
			}
			else
				lastIndexVal = 0;
		pickingIndexMap.put(prio, new Integer(lastIndexVal));//keep new value of lastIndex
		return prioOps.get(lastIndexVal);
	}

	private void resetPriority(Integer prio)
	{
		//        this.prioritizedTasks.remove(prio);//if priority container is empty - remove it
		pickingIndexMap.put(prio, new Integer(-1));//reset pickingIndex
	}

	/**
	 * @return
	 */
	public TaskManagerInfo getTaskManagerInfo()
	{
		return new TaskManagerInfo(this);
	}

	/**
	 * 
	 */
	private void resetTasksNumber()
	{
		this.taskNum = 0;
	}

	/**
	 * 
	 *  remove all tasks from the current queue and reset the task number
	 *  so that the agent can get new tasks from the server.
	 * 
	 */
	public void resetAll()
	{
		this.prioritizedTasks.clear();
		this.removeFinishedTasks();
		this.resetFinishedInfo();
		this.resetTasksNumber();
	}

}