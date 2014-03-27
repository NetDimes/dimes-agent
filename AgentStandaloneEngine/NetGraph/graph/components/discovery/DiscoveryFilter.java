/*
 * Created on 01/03/2005
 *
 */
package NetGraph.graph.components.discovery;

/**
 * @author Ohad Serfaty
 *
 */
public class DiscoveryFilter {
    
    public String experimentName = null;
    public String scriptName = null;
    public String operation = null;
    public String protocol = null;
    
    public String priority = null;
    
    public String toString(){
        return experimentName + " - " + scriptName + " - " + operation 
        + " - " + protocol + " - " + priority;
    }
    
}
