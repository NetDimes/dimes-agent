/*
 * Created on 01/03/2005
 *
 */
package NetGraph.graph.components.discovery;

import NetGraph.graph.util.PaintFilterManager;

/**
 * @author Ohad Serfaty
 *
 * a class indicating the discovery details 
 * 
 * This class wassists the filtering of a Graph component (i.e Node/Edge).
 */
public class ComponentDiscoveryDetails {
    
    /**
     * @param scriptName2
     * @param expName
     * @param operation2
     * @param protocol2
     */
    public ComponentDiscoveryDetails(String scriptName2, String expName, 
            String operation2, String protocol2, String aPriority) {
        experimentName = expName;
        scriptName = scriptName2;
        operation = operation2;
        protocol = protocol2;
        priority = aPriority;
        // TODO Auto-generated constructor stub
    }
    
    public String priority;
    public String experimentName;
    public String scriptName;
    public String operation;
    public String protocol;
    
    /**
     * @return
     */
    public static ComponentDiscoveryDetails masterFilter() {
        return new ComponentDiscoveryDetails(
        PaintFilterManager.ALL_SCRIPTS_STRING,
        PaintFilterManager.ALL_EXPERIMENTS_STRING,
        PaintFilterManager.ALL_OPERATIONS_STRING,
        PaintFilterManager.ALL_PROTOCOLS_STRING,
        PaintFilterManager.ALL_PRIORITY_STRING
        );
    }
    
}

