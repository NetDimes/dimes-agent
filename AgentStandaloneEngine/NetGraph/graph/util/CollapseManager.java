/*
 * Created on 14/02/2005
 *
 */
package NetGraph.graph.util;

import java.util.Iterator;
import java.util.Vector;

import NetGraph.graph.components.Edge;
import NetGraph.graph.components.Node;

/**
 * @author Ohad
 *
 * this class is responsible for collapsing a graph's database
 * 
 * NOTE : this class's methods are not synchronized, any attemp 
 * to change the parameters given to it are not monitored , and
 * therefore it should strictly be synchronized from outside.
 *
 */
public class CollapseManager {

	Vector collapsingList = new Vector();

	
	/******************
	 * reset the collapsing data.
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
 	 * therefore it should strictly be synchronized from outside.
	 */
  	public synchronized void unCollaspeAll(GraphData data){
  		for (int i=0; i<data.nnodes; i++)
  		{
  			data.nodes[i].isCollapsed = false;
  			data.nodes[i].collapseSource = -1;
  			data.nodes[i].collapseDest = -1;
  		}
  		for (int i=0; i<data.nedges; i++)
  		{
  			data.edges[i].isCollapsed = false;
  		}
  		data.virtualEdges.clear();
  	}
	
	/**
	 * un-collapse the entire graph.
	 * 
	 * NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
 	 * therefore it should strictly be synchronized from outside.
	 */
	public void makeAllExpand(GraphData data) {
		this.collapsingList.clear();
		this.unCollaspeAll(data);
	}
	
	/******************
	 * clear the collapse list from nodes that are not collapseable.
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
 	 * therefore it should strictly be synchronized from outside.
	 */
  	public void clearCollapseList(GraphData data){
  		Iterator i = this.collapsingList.iterator();
  		while (i.hasNext()){
  			Integer collapsedNodeIndex = (Integer) i.next();
  			if (!data.nodes[collapsedNodeIndex.intValue()].isCollapseable()){
  				i.remove();
//  				System.err.println("removing "+ collapsedNodeIndex.intValue() + " from collapse list");
  			}
  				
  		}
  	}
  	
  	public void adjustVirtualEdges(GraphData data)
  	{
//  	    System.out.println("adjusting...");
  	  Vector collapseableNodesList = this.getAllCollapseable(data);
  	  for (int i=0; i<data.nnodes; i++)
  	  {
  	      data.nodes[i].virtualSource = -1;
  	      data.nodes[i].virtualDest = -1;
  	  }
  	  Iterator j = collapseableNodesList.iterator();
  	  while(j.hasNext())
  	  {
  	      Integer nodeNumber = (Integer) j.next();
  	      this.startVirtualCollapse(data,nodeNumber.intValue());
  	  }
  	  
  	  
  	}
  	
	/**
  	 * recursively collapse all the nodes that 
  	 * are collapsabel from node number x :
  	 * 
  	 * @param nodeNumber the node to start the collapse from.
  	 * 
  	 * @return a vector of all the nodes that will connect directly
  	 * to this Node
  	 * 
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
 	 * therefore it should strictly be synchronized from outside.
	 */
  	private int virtualCollapse(GraphData data,int virtualSource, int nodeNumber , 
  	        double distFromSource) {
   		// the recursion stopping condition :
  		// when reached a node that is already collapsed or a junction
  		// or a leaf :
  		if ((data.nodes[nodeNumber].inDegree!=1)
  				|| (data.nodes[nodeNumber].outDegree==0)
				|| (data.nodes[nodeNumber].outDegree>1) )
  		{
  		    data.nodes[nodeNumber].distFromSource =distFromSource;
  			return nodeNumber;		
  		}
  		
  		// else : 
  		// and recourse to all the nodes that descend it :
  		data.nodes[nodeNumber].virtualSource = virtualSource;
  		data.nodes[nodeNumber].distFromSource =distFromSource;
  		Vector edgesFromSource = data.findEdgesWithSource(nodeNumber);
  		if (edgesFromSource.size()>1)
  		    System.out.println("More than one source ?");
  		int nextNodeNumber = ((Edge)edgesFromSource.get(0)).to;
  		int result = this.virtualCollapse(data,virtualSource,nextNodeNumber,distFromSource+((Edge)edgesFromSource.get(0)).len);
  		data.nodes[nodeNumber].virtualDest = result;
  		return result;
  		
  	}
  	
	/******************
	 * start the collapse of one node by the index.
	 * 
	 * @param nodeNumber - the nodes Index.
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
 	 * therefore it should strictly be synchronized from outside.
	 */
  	public void startVirtualCollapse(GraphData data,int nodeNumber){
  		Vector edgesFromSource = data.findEdgesWithSource(nodeNumber);
  		Vector nodesToConnect = new Vector();
  		
  		Iterator i = edgesFromSource.iterator();
  		while (i.hasNext())
  		{
  			Edge edge = (Edge) i.next();
  			int lastLeafOrJunction = virtualCollapse(data,nodeNumber,edge.to,edge.len);
  		}
  		
  	}
  	
  	
  	public Vector getAllCollapseable(GraphData data){
  	    Vector result = new Vector();
  	    result.add(new Integer(0));
  	    for (int i=1;i<data.nnodes; i++){
  	        if ((data.nodes[i].inDegree > 1) || (data.nodes[i].outDegree>1))
  	            result.add(new Integer(i));
  	    }
  	    return result;
  	}
  	
	/******************
	 * causes all the collapseable nodes to collapse.
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
 	 * therefore it should strictly be synchronized from outside.
	 */
 	public void makeAllCollapse(GraphData data){
//  		System.out.println("Collapsing all..");
  		collapsingList.clear();
  		this.collapsingList.add(new Integer(0));
  		for (int i=1;i<data.nnodes; i++){
  			if ((data.nodes[i].inDegree > 1) || (data.nodes[i].outDegree>1))
  					this.collapsingList.add(new Integer(i));
  			}
  		this.clearCollapseList(data);
		this.unCollaspeAll(data);
		this.collapseAll(data);
  	}
 	
	/******************
	 * this function re-collapses all the nodes that are in the collapsing list.
	 * it assumes a "clear" situation, meaning someone has already called
	 * unCollaspeAll berfore.
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
 	 * therefore it should strictly be synchronized from outside.
	 */
  	public void collapseAll(GraphData data) {
  		Iterator i = collapsingList.iterator();
  		while (i.hasNext())
  		{
  			int nodeToCollapse = ((Integer)i.next()).intValue();  			
  			this.startCollapse(data,nodeToCollapse);
  		}
  		
  	}
  	
	/******************
	 * add a node manually to the collapsing list
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
 	 * therefore it should strictly be synchronized from outside.
	 */
  	public void addNodeToCollapseList(int i){
  		if (!collapsingList.contains(new Integer(i))){
				collapsingList.add(new Integer(i));
			}
			else
				collapsingList.remove(new Integer(i));
  	}
  	
	/******************
	 * clear the collapsing list
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
 	 * therefore it should strictly be synchronized from outside.
	 */
  	public void clear(){
  		collapsingList.clear();
  	}
  	
	/**
  	 * recursively collapse all the nodes that 
  	 * are collapsabel from node number x :
  	 * 
  	 * @param nodeNumber the node to start the collapse from.
  	 * 
  	 * @return a vector of all the nodes that will connect directly
  	 * to this Node
  	 * 
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
 	 * therefore it should strictly be synchronized from outside.
	 */
  	private Vector collapse(GraphData data,int nodeNumber, Vector collapsedNodes) {
   		// the recursion stopping condition :
  		// when reached a node that is already collapsed or a junction
  		// or a leaf :
  		if ((data.nodes[nodeNumber].isCollapsed) || (data.nodes[nodeNumber].inDegree!=1)
  				|| (data.nodes[nodeNumber].outDegree==0)
				|| (data.nodes[nodeNumber].outDegree>1) )
  		{
  			// if so : return the node itself inside the vector :
  			Vector returnVector = new Vector();
  			returnVector.add(new Integer(nodeNumber));
  			return returnVector;		
  		}
  		
  		// else : 
//  		data.nodes[nodeNumber].isCollapsed = true; // collapse the node
  		collapsedNodes.add(data.nodes[nodeNumber]);
  		Vector result = new Vector();
  		// and recourse to all the nodes that descend it :
  		Vector edgesFromSource = data.findEdgesWithSource(nodeNumber);
  		Iterator i = edgesFromSource.iterator();
  		while (i.hasNext()){
  			Edge edge = (Edge) i.next();
  			Vector nodesToConnect = this.collapse(data,edge.to,collapsedNodes);
  			result.addAll(nodesToConnect);
  			edge.isCollapsed = true;
  		}
  		
  		return result;
  		
  	}
  	
	/******************
	 * start the collapse of one node by the index.
	 * 
	 * @param nodeNumber - the nodes Index.
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
 	 * therefore it should strictly be synchronized from outside.
	 */
  	public void startCollapse(GraphData data,int nodeNumber){
  		Vector edgesFromSource = data.findEdgesWithSource(nodeNumber);
  		Vector nodesToConnect = new Vector();
  		
//  		data.nodes[nodeNumber].fixed=true;
  		
  		Iterator i = edgesFromSource.iterator();
  		while (i.hasNext()){
  			Edge edge = (Edge) i.next();
  			//			System.out.println("Collapsing edge of nodes :" + edge.from + "->" + edge.to);
  			Vector collapsedNodes = new Vector();
  			Vector nodesToAdd = collapse(data,edge.to,collapsedNodes);
  			nodesToConnect.addAll(nodesToAdd);		
  			edge.isCollapsed = true;
  			
  			Iterator k = collapsedNodes.iterator();
  			while(k.hasNext())
  			{
  			    Node node = (Node) k.next();
  			    node.isCollapsed = true;
  			    node.collapseSource = nodeNumber;
  			    node.collapseDest = ((Integer)nodesToAdd.get(0)).intValue();
  			}
  		}
  		
  		Iterator j = nodesToConnect.iterator();
  		while (j.hasNext()){
  			Integer nodeConnection = (Integer) j.next();
  			Edge edgeToAdd = new Edge();
  			edgeToAdd.from = nodeNumber;
  			edgeToAdd.to = nodeConnection.intValue();
  			data.virtualEdges.add(edgeToAdd);
  			
  		}
  	}

 
  	
	/**
	 * @param mapNodesToReplacements
	 * 
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
 	 * therefore it should strictly be synchronized from outside.
	 */
	public void adjustClearList(int[] mapNodesToReplacements) {
		Vector newCollapsingList = new Vector();
  		
  		Iterator k = this.collapsingList.iterator();
  		while (k.hasNext()){
  			Integer collapsedNodeIndex = (Integer) k.next();
  			if (mapNodesToReplacements[collapsedNodeIndex.intValue()] != -1)
  				newCollapsingList.add(
  						new Integer(mapNodesToReplacements[collapsedNodeIndex.intValue()]));
  		}
  		
  		collapsingList.clear();
  		collapsingList.addAll(newCollapsingList);
		
	}

}
