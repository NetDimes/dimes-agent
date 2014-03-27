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
 * NOTE : this class's methods are not synchronized, any attemp 
 * to change the parameters given to it are not monitored , and
 * therefore it should strictly be synchronized from outside.
 *
 */
public class PruningManager {
	
	public int prunningLevel=0;
	
 	/**********************
  	 * resets the prunning device - getting everything back to the start :
  	 * 1.for each node : isPruned=false.
  	 * 2. adjusting the in/out Degree of each node.
  	 * 
  	 * NOTE : this class's methods are not synchronized, any attemp 
     * to change the parameters given to it are not monitored , and
     * therefore it should strictly be synchronized from outside.
  	 */
  	public void unPrune(GraphData data){
  		for (int i=0; i<data.nnodes; i++){
  			Node node = data.nodes[i];
  			node.isPruned=false;
  			node.inDegree=0;
  			node.outDegree=0;
  		}
  		
  		for (int i=0; i<data.nedges; i++){
  			Edge edge = data.edges[i];
  			edge.isPruned=false;
  			data.nodes[edge.to].inDegree++;
  			data.nodes[edge.from].outDegree++;
  		}
  	}

  	/*************************
  	 * a function that recalculates the pruning of the nodes :
  	 * the process is iterative, each iteration the function goes over 
  	 * the entire unpruned node set and prunes the leaves. 
  	 * 
  	 * NOTE : this class's methods are not synchronized, any attemp 
     * to change the parameters given to it are not monitored , and
     * therefore it should strictly be synchronized from outside.
  	 *
  	 */
  	public void adjustPruning(GraphData data){
  		// first reset the in/out degree so we start all over :
  		unPrune(data);

  		// iterate by the pruningLevel :
  		for (int j=0; j<prunningLevel; j++){  			
  			for (int i=0; i<data.nnodes; i++)
  			{

  				Node destNode = data.nodes[i];
  				// for each node : ignore the ones that are already pruned 
  				// and prune the ones with outDegree=0:
  				if ((!destNode.isPruned) && (destNode.outDegree == 0 )){
  					// find all edges that connect to this node :
  					Vector connectingEdges = data.findEdgesWithDest(i);
  					Iterator k = connectingEdges.iterator();
  					while(k.hasNext()){
  						Edge connectingEdge = (Edge)k.next();
  						connectingEdge.isPruned=true;	// make those edges pruned
  						Node sourceNode =  data.nodes[connectingEdge.from];
  						sourceNode.outDegree--;			// adjust the outDegree of the source.
  					}
  					// prune the node itself :
  					destNode.inDegree=0;
  					destNode.isPruned=true;
  				}
  			}
  		}
  	}
  	
  	
  	/***********
  	 * increase the prunning level by one.
  	 * 
  	 * NOTE : this class's methods are not synchronized, any attemp 
     * to change the parameters given to it are not monitored , and
     * therefore it should strictly be synchronized from outside.
  	 * 
  	 * @param data
  	 */
  	public void increasePrunningLevel(GraphData data){
  	    prunningLevel++;
//  		System.out.println("Prune counter = "+(prunningLevel++)); 			
  		adjustPruning(data);
  	}
  	
  	
  	/***********
  	 * decreace the prunning level by one.
  	 * 
  	 * NOTE : this class's methods are not synchronized, any attemp 
     * to change the parameters given to it are not monitored , and
     * therefore it should strictly be synchronized from outside.
  	 * 
  	 * @param data
  	 */ 	
  	public void decreasePrunningLevel(GraphData data){
  		if (prunningLevel>0)
  			prunningLevel--;
  		adjustPruning(data);
  		
  	}
  	
  	/**
  	 * prune to a specific pruning level.
  	 * 
  	 * @param pruningLevel the level.
  	 * 
  	 *  NOTE : this class's methods are not synchronized, any attemp 
     * to change the parameters given to it are not monitored , and
     * therefore it should strictly be synchronized from outside.
  	 */
  	public void prune(GraphData data,int aPruningLevel) {
  		this.prunningLevel = aPruningLevel;
  		this.adjustPruning(data);
  		// TODO : ADD THIS to GrpahDisplayPanel
//  		clearCollapseList();
//  		this.unCollaspeAll();
//  		this.collapseAll();
//  		System.out.println("pruned up to level :"+this.prunningLevel);
  		
  	}
  	
  	
	/**
  	 * this function performs a permanent one-level prune, meaning,
  	 * it clears the leaves of every trace - the nodes which 
  	 * have inDegree=1 and outDegree=0;
  	 * 
  	 *  NOTE : this class's methods are not synchronized, any attemp 
     * to change the parameters given to it are not monitored , and
     * therefore it should strictly be synchronized from outside.
  	 * 
  	 */
  	public int[] permanentPrune(GraphData data) {
//  		System.out.println("entring permanent prune... ");
  		Node[] nodesReplacement = new Node[GraphData.MAX_NODES+50];
  		Edge[] edgesReplacement = new Edge[GraphData.MAX_EDGE+50];
  		
  		int[]  mapNodesToReplacements = new int[GraphData.MAX_NODES+50];
  		for (int i=0; i<GraphData.MAX_NODES+50; i++) {
  			mapNodesToReplacements[i]=-1;
  		}
  		
  		this.unPrune(data);
  		int tempPruningCounter = this.prunningLevel;
  		prunningLevel=0;
  		this.increasePrunningLevel(data);
  		
  		int newNNodes = 0;
  		for (int i=0; i<data.nnodes; i++)
  		{
  			Node nodeToTest = data.nodes[i];
  			if (!nodeToTest.isPruned)
  			{
  				nodesReplacement[newNNodes]=nodeToTest;
  				mapNodesToReplacements[i] = newNNodes;
  				newNNodes++;
  			}
  		}
  		
  		int newNEdges = 0;
  		for (int i=0; i<data.nedges; i++){
  			Edge edgeToTest = data.edges[i];
  			if (!edgeToTest.isPruned){
  				edgesReplacement[newNEdges++]=edgeToTest;
//  				if (mapNodesToReplacements[edgeToTest.to] == -1 || 
//  						mapNodesToReplacements[edgeToTest.from] == -1)
//  					System.err.println("fault with this pruned...");
  				edgeToTest.to = mapNodesToReplacements[edgeToTest.to];
  				edgeToTest.from = mapNodesToReplacements[edgeToTest.from];
  			}
  		}
  		// garbage collect:
  		data.nodes=null;
  		data.edges=null;
  		
  		data.nnodes = newNNodes;
  		data.nedges = newNEdges;
  		
  		data.nodes = nodesReplacement;
  		data.edges = edgesReplacement;
  		
  		prunningLevel = tempPruningCounter;
  		data.adjustInOutDegrees();
//  		System.out.println("done...");
  		return mapNodesToReplacements;
  	}
  	

}
