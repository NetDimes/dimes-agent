/*
 * Created on 01/03/2005
 *
 */
package NetGraph.graph.components;

/**
 * @author Ohad Serfaty
 *
 */
public class GraphComponent {

    public boolean isVirtual=false;
	public boolean isPruned=false;
	public boolean isCollapsed = false;
	
	public long startTime = System.currentTimeMillis();
}
