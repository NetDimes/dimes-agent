/*
 * Created on 22/01/2004
 */
package dimes.scheduler;

import dimes.measurements.operation.Operation;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesNames;

/**
 * @author anat
 */
public class Task
{
	private final String ID;//script id
	private final String exID;//experiment id
	private final int priority;
	private SyntaxTree syntaxTree = null;
	private Context context = null;

	private int totalOps = 0;
	private int doneOps = 0;

	Task(String anExID, String anID, int aPriority, SyntaxTree aTree)
	{
		Object[] params = {anExID, anID, new Integer(aPriority), aTree};//needed just for logging
		Loggers.getLogger(this.getClass()).entering("Task", "Task()", params);

		ID = anID;
		exID = anExID;
		priority = aPriority;
		syntaxTree = aTree;
		aTree.setContainingTask(this);
		context = new Context();

		this.totalOps = this.syntaxTree.size();
	}

	public static Task createArbitraryTask(){
		SyntaxTree syntaxTree = new SyntaxTree();
		Task task = new Task("StandAloneTreeroute","StandaloneScript", Priority.NORMAL, syntaxTree);
		return task;
	}
	
	Operation getNextOp()
	{
		return this.syntaxTree.getNextOp() ;
	}

	public boolean taskFinished(){
		return this.syntaxTree.isEmpty();
	}
	
	/* used to inform this Task that its next Operation was executed.
	 * results in the progressing of the Task
	 */
	void updateTakenOp(Operation op)
	{
		this.syntaxTree.updateTakenOp(op);
		this.doneOps = this.syntaxTree.getDoneOperations();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	//nextVer - better implementation
	public String toString()
	{
		return "Task ID " + this.ID + ", Experiment ID " + this.exID + " Priority "+priority+ "\n" + this.syntaxTree + "\n" + this.context;
	}

	public String toXML()
	{
		//	    String info = "";
		//	    //todo
		//	    info += "<"+PropertiesNames.TASK;
		//	    info += " "+PropertiesNames.EXPERIMENT_ID+"=\""+this.exID+"\"";
		//	    info += " "+PropertiesNames.SCRIPT_ID+"=\""+this.ID+"\">\n";
		//	    info += "<"+PropertiesNames.TOTAL_OPS+">"+this.totalOps+"</"+PropertiesNames.TOTAL_OPS+">\n";
		//	    info += "<"+PropertiesNames.DONE_OPS+">"+this.doneOps+"</"+PropertiesNames.DONE_OPS+">\n";
		//	    info += "</"+PropertiesNames.TASK+">\n";
		//	    
		//	    return info;
		String xml = PropertiesNames.getOpeningTag(PropertiesNames.SCRIPT);
		xml = PropertiesNames.addAttributeToTag(xml, PropertiesNames.SCRIPT_ID, this.ID);
		xml = PropertiesNames.addAttributeToTag(xml, PropertiesNames.EXPERIMENT_ID, this.exID) + "\n";
		xml += "\t" + PropertiesNames.getOpeningTag(PropertiesNames.DONE_OPS) + this.doneOps + PropertiesNames.getClosingTag(PropertiesNames.DONE_OPS) + "\n";
		xml += "\t" + PropertiesNames.getOpeningTag(PropertiesNames.TOTAL_OPS) + this.totalOps + PropertiesNames.getClosingTag(PropertiesNames.TOTAL_OPS)
				+ "\n";
		xml += "\t" + PropertiesNames.getOpeningTag(PropertiesNames.PRIORITY) + Priority.getName(this.priority)
				+ PropertiesNames.getClosingTag(PropertiesNames.PRIORITY) + "\n";
		xml += PropertiesNames.getClosingTag(PropertiesNames.SCRIPT) + "\n";

		return xml;
	}

	/**
	 * @return
	 */
	public String getID()
	{
		return ID;
	}

	public int getPriority()
	{
		return this.priority;
	}
	public String getExID()
	{
		return this.exID;
	}
	public String getCurrentMeasurmentIP(){
		OpParams param=null;
		param =  syntaxTree.getCurrentOpParameters();
		if(!(null==param)) return param.hostIP;
		return "";
	}

	/**
	 * @return String that is the unique identifier of this Task.
	 * 
	 */
	public String getUniqueIdentifier()
	{
		return this.exID + "_" + this.ID;
	}
}