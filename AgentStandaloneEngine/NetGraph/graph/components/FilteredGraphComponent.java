/*
 * Created on 01/03/2005
 *
 */
package NetGraph.graph.components;

import java.util.Vector;

import NetGraph.graph.components.discovery.ComponentDiscoveryDetails;
import NetGraph.graph.util.PaintFilterManager;

/**
 * @author Ohad Serfaty
 *
 */
public class FilteredGraphComponent extends GraphComponent{
    
    public boolean isFiltered = false;
    private Vector discoveringScripts = new Vector();
    private Vector discoveringExperiments = new Vector();
    private Vector discoveringOpertaions = new Vector();
    private Vector discoveringProtocols= new Vector();
    private Vector discoveringPriorities= new Vector();
    
    
    
    public FilteredGraphComponent(){
        super();
//        discoveringScripts.add(PaintFilterManager.ALL_SCRIPTS_STRING);
//        discoveringExperiments.add(PaintFilterManager.ALL_EXPERIMENTS_STRING);
//        discoveringOpertaions.add(PaintFilterManager.ALL_OPERATIONS_STRING);
//        discoveringProtocols.add(PaintFilterManager.ALL_PROTOCOLS_STRING);
//        discoveringPriorities.add(PaintFilterManager.ALL_PRIORITY_STRING);
    }

    
  
    
    
    /*******************
     * add a discovery for the specific script
     * 
     * @param scriptName
     * @param expName
     */
    private void addDiscoveringScriptAndExperiment(String scriptName,String expName){
    	if (!discoveringScripts.contains(scriptName))
    		discoveringScripts.add(scriptName);
    	if (!discoveringExperiments.contains(expName))
    	    discoveringExperiments.add(scriptName);
    }
    
    
    /************
     * indicates that the Node was discovered by a particular script
     * 
     * @param scriptName - the script name to verify discovery.
     */
    public boolean wasDiscoveredByScript(String scriptName){
    	return ( discoveringScripts.contains(scriptName)  || discoveringScripts.contains(PaintFilterManager.ALL_SCRIPTS_STRING)
                || scriptName.equals(PaintFilterManager.ALL_SCRIPTS_STRING));
    }
    
    /************
     * indicates that the Node was discovered by a particular Experiment
     * 
     * @param expName - the Experiment name to verify discovery.
     */
    public boolean wasDiscoveredByExperiment(String expName){
    	return ( discoveringExperiments.contains(expName)  || discoveringExperiments.contains(PaintFilterManager.ALL_EXPERIMENTS_STRING)
                || expName.equals(PaintFilterManager.ALL_EXPERIMENTS_STRING) ) ;
    }
    
    /*******************
     * add a discovery for the specific operation
     * 
     * @param operationName
     */
    private void addDiscoveringOperation(String operationName){
        if (!discoveringOpertaions.contains(operationName))
            discoveringOpertaions.add(operationName);
    }
    
    /************
     * indicates that the Node was discovered by a particular operation 
     * (i.e traceroute/ping)
     * 
     * @param operationName - the script name to verify discovery.
     */
    public boolean wasDiscoveredByOperation(String operationName){
    	return ( discoveringOpertaions.contains(operationName)  || discoveringOpertaions.contains(PaintFilterManager.ALL_OPERATIONS_STRING)
                || operationName.equals(PaintFilterManager.ALL_OPERATIONS_STRING));
    }
    
    /*******************
     * add a discovery for the specific protocol (i.e ICMP,TCP,UDP)
     * 
     * @param protocolName
     */
    private void addDiscoveringProtocol(String protocolName){
        if (!discoveringOpertaions.contains(protocolName))
            discoveringProtocols.add(protocolName);
    }
    
    /************
     * indicates that the Node was discovered by a particular protocol
     *  (i.e ICMP,TCP,UDP) 
     * 
     * @param protocolName - the script name to verify discovery.
     */
    public boolean wasDiscoveredByProtocol(String protocolName){
    	return ( discoveringProtocols.contains(protocolName) || discoveringProtocols.contains(PaintFilterManager.ALL_PROTOCOLS_STRING)
                || protocolName.equals(PaintFilterManager.ALL_PROTOCOLS_STRING));
    }
    
    /**
     * @param discoveryDetails
     */
    public void addDiscoveryDetails(ComponentDiscoveryDetails discovery) {
       this.addDiscoveringOperation(discovery.operation);
       this.addDiscoveringProtocol(discovery.protocol);
       this.addDiscoveringPriority(discovery.priority);
       this.addDiscoveringScriptAndExperiment(discovery.scriptName,discovery.experimentName);
    }



    /*******************
     * add a discovery for the specific protocol (i.e ICMP,TCP,UDP)
     * 
     * @param priority
     */
    private void addDiscoveringPriority(String priority){        
        if (!discoveringPriorities.contains(priority))
            discoveringPriorities.add(priority);
    }
    


    /**
     * @param priority
     * @return
     */
    public boolean wasDiscoveredByPriority(String priority) {
        return (discoveringPriorities.contains(priority) || discoveringPriorities.contains(PaintFilterManager.ALL_PRIORITY_STRING)
                || priority.equals(PaintFilterManager.ALL_PRIORITY_STRING));
    }
    
}
