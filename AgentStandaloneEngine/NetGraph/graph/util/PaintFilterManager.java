/*
 * Created on 14/02/2005
 *
 */
package NetGraph.graph.util;

import NetGraph.graph.components.FilteredGraphComponent;
import NetGraph.graph.components.discovery.DiscoveryFilter;

/**
 * @author Ohad
 *
 * a class responsible for script filters on the display.
 * 
 * NOTE : this class's methods are not synchronized, any attemp 
 * to change the parameters given to it are not monitored , and
 * therefore it should strictly be synchronized from outside.
 *
 */
public class PaintFilterManager {
	
	public static final String ALL_EXPERIMENTS_STRING = "All Experiments";
    public static final String ALL_OPERATIONS_STRING = "All Operations";
    public static final String ALL_PROTOCOLS_STRING = "All Protocols";    	
	public static final String ALL_SCRIPTS_STRING = "All Scripts";
	public static final String ALL_PRIORITY_STRING = "All Priorities";
	
	private DiscoveryFilter activeFilter = new DiscoveryFilter();
    public static final String USER_PRIORITY_FILTER = "User_Priority";
    public static final String DIMES_PRIORITY_FILTER = "DIMES_Priority";
    public static final String ICMP_PROTOCOL_FILTER = "ICMP";
    public static final String UDP_PROTOCOL_FILTER = "UDP";
    public static final String TRACEROUTE_OPERATION_FILTER = "Traceroute";
    public static final String PING_OPERATION_FILTER = "Ping";
	
//	private Vector scriptDisplayFilter=new Vector();
//	private Vector experimentDisplayFilter=new Vector();
//	private Vector operationDisplayFilter=new Vector();
//	private Vector protocolDisplayFilter=new Vector();
	
	public PaintFilterManager(){
	    initFilter();
	}
	
	public synchronized void applyFilter(FilteredGraphComponent component){
	    component.isFiltered = false;
	    
	    
	if (!component.wasDiscoveredByExperiment(activeFilter.experimentName))
	    {
	    component.isFiltered = true;
//	    System.out.println(component + " filtered by experiment");
	    }

	if (!component.wasDiscoveredByScript(activeFilter.scriptName))
    {
    component.isFiltered = true;
//    System.out.println(component + " filtered by script");
    }
	
	if (!component.wasDiscoveredByOperation(activeFilter.operation))
    {
    component.isFiltered = true;
//    System.out.println(component + " filtered by operation");
    }
	
	if (!component.wasDiscoveredByProtocol(activeFilter.protocol))
    {
    component.isFiltered = true;
//    System.out.println(component + " filtered by protocol");
    }
	
	if (!component.wasDiscoveredByPriority(activeFilter.priority))
    {
    component.isFiltered = true;
//    System.out.println(component + " filtered by priority");
    }
	    
//	    component.isFiltered = 
//	        		(
//	        		!component.wasDiscoveredByExperiment(activeFilter.experimentName)
//	                ||
//	                !component.wasDiscoveredByScript(activeFilter.scriptName)  
//	                ||
//	                !component.wasDiscoveredByOperation(activeFilter.operation)
//	                ||
//	                !component.wasDiscoveredByProtocol(activeFilter.protocol)
//	                ||
//	                !component.wasDiscoveredByPriority(activeFilter.priority)
//	                );
//	    System.out.println(component + " -> isFiltered = " + component.isFiltered);
	}
	
	private synchronized void adjustFiltering(GraphData data){
//	    System.out.println("adjusting filter : ..." + activeFilter);
	    for (int k=0; k<data.nnodes; k++)
	        applyFilter(data.nodes[k]);
		for (int k=0; k<data.nedges; k++)
		    applyFilter(data.edges[k]);				
	}
	
	/**
	 * @param selectedScripts array of strings with script names.
	 * 
	 * set a new script filter
	 */
	public synchronized void setScriptDisplayFilter(GraphData data, String selectedScript) {
	    activeFilter.scriptName = selectedScript;
	    this.adjustFiltering(data);
	}
	
	/**
	 * @param selectedScripts array of strings with script names.
	 * 
	 * set a new script filter
	 */
	public synchronized void setExperimentDisplayFilter(GraphData data, String selectedExperiment) {
	    activeFilter.experimentName = selectedExperiment;
	    this.adjustFiltering(data);
	}
	
	/**
	 * @param selectedScripts array of strings with script names.
	 * 
	 * set a new script filter
	 */
	public synchronized void setOperationDisplayFilter(GraphData data, String selectedOperation) {
	    activeFilter.operation = selectedOperation;
	    this.adjustFiltering(data);
	}
	
	/**
	 * @param selectedScripts array of strings with script names.
	 * 
	 * set a new script filter
	 */
	public synchronized void setProtocolDisplayFilter(GraphData data, String selectedProtocol) {
	    activeFilter.protocol = selectedProtocol;
	    this.adjustFiltering(data);
	}
	
	
	
	/** Change tous les labels de toutes les nodes
	 * en fonction de nb
	 * @param nb un chiffre pour indiquer si on affiche plutot
	 * le nom de la machine, le dns ou l'ip, voir node.java
	 * 
	 * NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
	 * therefore it should strictly be synchronized from outside.
	 * 
	 */
	public void ChangeLabel(GraphData data, int nb) {
		for(int i=0; i<data.nnodes;i++) {
			data.nodes[i].ChangeLabel(nb);
		}
	}

    /**
     * @param data
     * @param priority
     */
    public synchronized void setPriorityDisplayFilter(GraphData data, String priority) {
        activeFilter.priority = priority;
        this.adjustFiltering(data);        
    }

    /**
     * 
     */
    public void resetFilter(GraphData data) {
        initFilter();    
	    this.adjustFiltering(data);
    }
    
    public void initFilter() {
        activeFilter.scriptName = ALL_SCRIPTS_STRING;
	    activeFilter.experimentName = ALL_EXPERIMENTS_STRING;
	    activeFilter.operation = ALL_OPERATIONS_STRING;
	    activeFilter.protocol = ALL_PROTOCOLS_STRING;
	    activeFilter.priority = ALL_PRIORITY_STRING;	
    }
    
	

	
	
}
