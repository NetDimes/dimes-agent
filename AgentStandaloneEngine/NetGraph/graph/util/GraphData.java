/*
 * Created on 14/02/2005
 *
 */
package NetGraph.graph.util;

import java.awt.Color;
import java.awt.Dimension;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import NetGraph.graph.components.Edge;
import NetGraph.graph.components.Node;
import NetGraph.graph.components.discovery.ComponentDiscoveryDetails;
import NetGraph.utils.NodeInfo;
import NetGraph.utils.OverFlowException;
import dimes.util.logging.Loggers;

/**
 * @author Ohad
 *
 */
public class GraphData {

    private Logger logger = Loggers.getLogger(this.getClass());
    
	public Node[] nodes =new Node[MAX_NODES+50];
	public Edge[] edges =new Edge[MAX_EDGE+50];
  	
  	public  int nnodes =0;
  	public int nedges=0;
  	
  	/** Maximun de noeud et d'arc*/
  	public static final int MAX_NODES =250;
  	public static final int MAX_EDGE =250;
  	
  	public Vector virtualEdges = new Vector();

  	/**
  	 * find the edge that connects with the nodes contained in nodes[fromEdge].
  	 * ( actually the edge whose destination is fromEdge).
  	 * This is a very ungly code and The author knows it.
  	 * 
  	 * @param sourceNum index of the source node.
  	 * @return
  	 */
  	public Vector findEdgesWithSource(int sourceNum) {
  		Vector result = new Vector();
  		for (int i=0; i<this.nedges; i++)
  			if (edges[i].from == sourceNum)
  				result.add(edges[i]);
  		return result;
  	}
  	
  	/**
  	 * find the edge that connects with the nodes contained in nodes[fromEdge].
  	 * ( actually the edge whose destination is fromEdge).
  	 * This is a very ungly code and The author knows it.
  	 * 
  	 * @param destNum index of the dest node.
  	 * @return
  	 */
  	public Vector findEdgesWithDest(int destNum) {
  		Vector result = new Vector();
  		for (int i=0; i<this.nedges; i++)
  			if (edges[i].to == destNum)
  				result.add(edges[i]);
  		return result;
  	}
  	
 	/**
  	 * adjust the in/ot degree of the nodes so they will correspond to the 
  	 * real situation.
  	 * note that this function does not mess with the isPruned 
  	 * variable - a thing which unPrune does...
  	 * 
  	 */
  	public void adjustInOutDegrees() 
  	{
  		for (int i=0; i<this.nnodes; i++){
  			Node nodeToTest = nodes[i];
  			nodeToTest.inDegree=0;
  			nodeToTest.outDegree=0;
  		}
  		
  		for (int i=0; i<this.nedges; i++){
  			Edge edgeToTest = edges[i];
  			nodes[edgeToTest.to].inDegree++;
  			nodes[edgeToTest.from].outDegree++;
  		}
  		
  	}
  	
	/**
	 * a function used to avoid problems of an invalid xml files
	 * received from the server.
	 * all the nodes are declared "retrieved" and thus wouldn't be
	 * sent again to the server for resolving.
	 * 
	 * @param unretreivedIPs
	 * 
	 */
	public void markAllRetrieved() {
		// TODO Auto-generated method stub
		for (int i=0; i<nnodes; i++){
			nodes[i].isRetreived = true;
		}
	}
	
  	/**
  	 * Nettoye le graph .... enleve tout sauf la machine local
  	 */
  	public void clear() {
  		int oldnedges = nedges;
  		int oldnnodes = nnodes;
  		/* On remet a 0 ou presque*/
  		nedges = 0; nnodes = 1;
  		for(int i=1;i<oldnnodes;i++) nodes[i]=null;
  		for(int i=0;i<oldnedges;i++) edges[i]=null;
  		this.virtualEdges.clear();
  		
  	}
  	
  	/** Ajouter un arc entre deux IP, si une des IP n'existe pas, elle est cree
  	 * @param to IP destination
  	 * @param from IP origine de l'arc
  	 * @param len distance de l'arc entre les deux IP
  	 * @param info info retourne par le traceroute
  	 * 
  	 * @return the edge that was added - or null if the edge was not added.
  	 */
  	public Edge addEdge(
  			InetAddress from,InetAddress to,int len,
  			ComponentDiscoveryDetails discoveryDetails
			, Dimension frameSize)
  	
  	throws OverFlowException
	{
  		Edge addedEdge = null;
  		if (from.equals(to))
  		{
  		  /*System.err.println*/logger.finest("avoiding an inner circle :" + from + " ->" + to);
  			return addedEdge;
  		}
  		
  		boolean overFlow = false;
  		boolean overflowAhead=false;
  		//changed by Ohad :
  		if(nedges>=MAX_EDGE-1 || nnodes>=MAX_NODES-1)
  		{
  		  /*System.err.println*/logger.finest("max of "+ ((nedges>=MAX_EDGE-1) ? "edges" : "nodes") );
  			overFlow = true;  			
  			//        throw new OverFlowException( (nedges>=MAX_EDGE-1) ? "edges" : "nodes");
  		}
  		if(nedges>=MAX_EDGE-50 || nnodes>=MAX_NODES-50)
  		{
//  		  /*System.err.println*/logger.finest("advancing toward overflowing the number of "+ ((nedges>=MAX_EDGE-50) ? "edges" : "nodes") );
  			overflowAhead = true;  			  			
  		}
  		Edge e = new Edge();
  		e.from = findNode(new NodeInfo(from,discoveryDetails),frameSize);
  		e.to = findNode(new NodeInfo(to,discoveryDetails),frameSize);
  		
  		nodes[e.to].addDiscoveryDetails(discoveryDetails);
  		nodes[e.from].addDiscoveryDetails(discoveryDetails);
  		
//  		nodes[e.from].setColorInProgress(false);
//  		nodes[e.to].setColorInProgress(true);  		  		
  		
  		e.len = len;
  		e.addDiscoveryDetails(discoveryDetails);
  		Edge existingEdge = findEdge(e);
  		if(  existingEdge == null ) {
  			addedEdge = e;
  			/* On calcul la position du noeud */
  			if( ! nodes[e.to].fixed) {
  				nodes[e.to].x = nodes[e.from].x + Math.pow(-1,e.from) * 150*Math.random();
  				nodes[e.to].y = nodes[e.from].y +  Math.pow(-1,nnodes) * 150*Math.random(); 				
  			}
  			// adjust the in/outDegree :
  			nodes[e.from].outDegree++;
  			nodes[e.to].inDegree++;		
  			// adjust prunng :
  			nodes[e.from].isPruned = false;
  			nodes[e.to].isPruned = false;
  			
  			// adjust the filtering :
//  			e.addDiscoveringScript("All Scripts");
  			edges[nedges++] = e;
  			this.adjustInOutDegrees();			
  			
  		}
  		else
  		{
  			existingEdge.addDiscoveryDetails(discoveryDetails);
  			addedEdge = existingEdge;
  			
  		}
		
  		if (overFlow)
  			throw new OverFlowException( (nedges>=MAX_EDGE-1) ? "edges" : "nodes");
  		if (overflowAhead)
  			throw new OverFlowException( (nedges>=MAX_EDGE-50) ? "edges" : "nodes",
  					(nedges>=MAX_EDGE-50) ? MAX_EDGE-nedges : MAX_NODES-nnodes);
  		return addedEdge;
  		
  	}
  	

    /**
	 * Savoir si un arc existe deja<br>
	 * Active l'arc pour qu'il soit mis en rouge
	 * @param e1 l'arc a chercher
	 * @return true si il existe
	 */
	public Edge findEdge(Edge e1) {
		for(int i=0;i<nedges;i++) {
			if(e1.equals(edges[i])) {
//				activeOneNode(edges[i].to); 
				return edges[i]; }
		}
//		activeOneNode(nnodes-1);
		return null;
	}
  	
//  	/**
//  	 * 
//  	 */
//  	public synchronized void killTheFishes() {
//  		Edge[] edgesReplacement = new Edge[MAX_EDGE+50];	
//  		
//  		for (int i=0; i<this.nnodes; i++){
//  			Node nodeToTest = nodes[i];
//  			if ( nodeToTest.inDegree>1){
//  				Vector connectingEdges = this.findEdgesWithDest(i);
//  				Iterator j = connectingEdges.iterator();
//  				Edge edgeToCut = null;
//  				while (j.hasNext()){
//  					Edge edge = (Edge) j.next();
//  					if (edgeToCut == null)
//  						edgeToCut=edge;		// the first edge is the default;
//  					
//  					Node sourceNode = nodes[edge.to];
//  					if (sourceNode.outDegree>1)
//  					{
//  						edgeToCut = edge;
//  						break;
//  					}
//  					
//  				}
//  				
//  				edgeToCut.isPruned=true;
//  			}
//  		}
//  		
//  		int newNEdges = 0;
//  		for (int i=0; i<this.nedges; i++){
//  			Edge edgeToTest = edges[i];
//  			if (!edgeToTest.isPruned){
//  				edgesReplacement[newNEdges++]=edgeToTest;
//  			}
//  		}
//  		
//  		this.nedges = newNEdges;	
//  		this.edges = edgesReplacement;
//  		this.adjustInOutDegrees();
//  	}
  	
 	/**
  	 * a function which updates the AS-Country-etc infromation
  	 * for a specific IP in the graph.
  	 * 
  	 * @param ip - the IP to be updated.
  	 * @param countryName - the country where the IP is located.
  	 * @param anISPName - the name of the ISP the IP belongs to.
  	 * @param asNumber - the AS number - not neccesary?
  	 */
  	public int updateIPInfo(String ip, String countryName, String anISPName, int asNumber) {
  		
  		for (int i=0; i<nnodes; i++){
  			if (nodes[i].IP.getHostAddress().equals(ip)){
  				nodes[i].isRetreived=true;
  				nodes[i].country = (countryName.trim().equals("")?"Unknown":countryName);
  				nodes[i].ISPName = (anISPName.trim().equals("")?"Unknown":anISPName);
  				nodes[i].ASNumber = asNumber;
  				
  				nodes[i].ChangeLabel();
  				
  				int redIsp = 55+Math.abs( nodes[i].ISPName.hashCode()%200);
  				int blueIsp = 55+Math.abs((nodes[i].ISPName.hashCode()/(200))%200);
  				int greenIsp =55+Math.abs((nodes[i].ISPName.hashCode()/(200^2))%200);
  				
  				int redCountry =55+Math.abs( nodes[i].country.hashCode()%200);
  				int blueCountry = 55+Math.abs((nodes[i].country.hashCode()/(200))%200);
  				int greenCountry = 55+Math.abs((nodes[i].country.hashCode()/(200^2))%200);
  				
  				
//  				System.out.println(nodes[i].ISPName + " -> "+redIsp + " , " + blueIsp + " , " + greenIsp);
//  				System.out.println(nodes[i].country + " -> "+redCountry + " , " + blueCountry + " , " + greenCountry);
  				
  				if (!anISPName.equalsIgnoreCase("unknown"))
  				{
  					nodes[i].asColor = new Color(redIsp,greenIsp,blueIsp);
  					nodes[i].asColorFiltered = new Color(redIsp,greenIsp,blueIsp, Node.TRANSPARENCY_ALPHA_CONSTANT );
  				}
  				if (!countryName.equalsIgnoreCase("unknown"))
				{  				  				      				
  					nodes[i].countryColor = new Color(redCountry,greenCountry,blueCountry);
  					nodes[i].countryColorFiltered = new Color(redCountry,greenCountry,blueCountry , Node.TRANSPARENCY_ALPHA_CONSTANT);
  				}
  				
  				
  				return i;
  			}
  		}
  		return -1;
  	}
  	
  	/**
  	 * a function which returns all the nodes that haven't been fetched
  	 * and retreived by the server ( Meaning - the information about ASNumber,
  	 * ISPName, country etc isnt available yet).
  	 * 
  	 * @return a vector containing all unretrieved IPs.
  	 */
  	public Vector getUnretreivedIPs() {

  		Vector result = new Vector();
  		for (int i=0; i<nnodes; i++){
  			Node nodeToCheck = nodes[i];
  			if (!nodeToCheck.isRetreived){
  				result.add(nodeToCheck.IP.getHostAddress());
  			}
  		}		
  		return result;
  	}
  	
	/**
  	 * 
  	 */
  	public void killTheFishes() {
  		Edge[] edgesReplacement = new Edge[MAX_EDGE+50];
  		
  		
  		for (int i=0; i<nnodes; i++){
  			Node nodeToTest = nodes[i];
  			if ( nodeToTest.inDegree>1){
  				Vector connectingEdges = findEdgesWithDest(i);
  				Iterator j = connectingEdges.iterator();
  				Edge edgeToCut = null;
  				while (j.hasNext()){
  					Edge edge = (Edge) j.next();
  					if (edgeToCut == null)
  						edgeToCut=edge;		// the first edge is the default;
  					
  					Node sourceNode = nodes[edge.to];
  					if (sourceNode.outDegree>1)
  					{
  						edgeToCut = edge;
  						break;
  					}
  					
  				}
  				
  				edgeToCut.isPruned=true;
  			}
  		}
  		
  		int newNEdges = 0;
  		for (int i=0; i<nedges; i++){
  			Edge edgeToTest = edges[i];
  			if (!edgeToTest.isPruned){
  				edgesReplacement[newNEdges++]=edgeToTest;
  			}
  		}
  		
  		nedges = newNEdges;	
  		edges = edgesReplacement;

  		adjustInOutDegrees();
//  		pruner.adjustPruning(data);
//  		collapser.clearCollapseList(data);
//  		collapser.unCollaspeAll(data);
//  		collapser.collapseAll(data);
//
//  		this.adjustInOutDegrees();
//  		this.adjustPruning();
//  		clearCollapseList();
//  		this.unCollaspeAll();
//  		this.collapseAll();
  	}

  	
  	/** Trouver un noeud, ajoute le noeud si pas trouve
  	 * @param IP l'addresse IP du noeud
  	 * @return un entier correspondant a la position dans le tableau
  	 */
  	public int findNode(InetAddress IP,Dimension frameSize) {
  		return findNode(new NodeInfo(IP,null),frameSize);
  	}
  	
  	/** Trouver un noeud, ajoute le noeud si pas trouve
  	 * @param ni des informations sur le noeud
  	 * @return un entier correspondant a la position dans le tableau
  	 */
  	public int findNode(NodeInfo ni,Dimension frameSize) {
  		for (int i = 0 ; i < nnodes ; i++) {
  			if (nodes[i].IP.equals(ni.IP)) return i;
  		}
  		return addNode(ni,frameSize);
  	}
  	
  	/** Obtenir les informations sur une node
  	 * @param IP l'address IP de la node
  	 */
  	public String findNodeInformation(InetAddress IP) {
  		for ( int i = 0 ; i < nnodes ; i++) {
  			if(nodes[i].IP.equals(IP)) return nodes[i].information;
  		}
  		return null;
  	}
  	
  	/** Ajouter un noeud
  	 * @param ni Plusieurs info a ajouter
  	 */
  	int addNode(NodeInfo ni, Dimension frameSize) {
//  		System.out.println(" adding " + ni.IP.getHostAddress());
  		Node n = new Node();
  		n.IP = ni.IP;
  		
  		n.addDiscoveryDetails(ni.discoveryDetails);
//  		n.addDiscoveringScriptAndExperiment(ni.discoveringScript);
//  		n.addDiscoveringScript("All Scripts");

				
  		//Apres IP important
  		n.setLabel("Resolving"/*Conf.getConf().getLang().getString("RESOLUTION")*/);
  		
  		if(nnodes==0) 
  		{
  			n.x = frameSize.width/2;
  			n.y = frameSize.height/2;
  			n.setAsOrigin();
  		}

  		nodes[nnodes] = n;
  		
  		return nnodes++;
  	}

	/**
	 * 
	 */
	public void centerLocalhost(Dimension frameSize) {
		Node n = this.nodes[0];
  		if (n==null)//before agent fully loads
  			return;
  		n.x = frameSize.width/2;
  		n.y = frameSize.height/2;
		
	}

    /**
     * @return
     */
    public String export() {
       String result = "<Results>\n";
       
       
       
       for (int i=0; i<nedges; i++)
       {
        result+=
       	"<OperationResult>\n"+
		"<ExID>All Experiments</ExID>\n"+
		"<ScriptID>All Scripts</ScriptID>\n"+
		"<Priority>NORMAL</Priority>\n"+
		"<CommandType>TRACEROUTE</CommandType>\n"+
		"<Protocol>UDP</Protocol>\n";

    	
        Node source = nodes[edges[i].from];
        Node dest = nodes[edges[i].to];
        
        result+=
        "<RawDetails>\n"+
		"<Detail>\n"+
		"	<sequence>1</sequence>\n"+
		"	<hopAddressStr>" + source.IP.getHostAddress() + "</hopAddressStr>\n"+
		"	<country>" + source.country + "</country>\n"+
		"	<ISPName>" + source.ISPName + "</ISPName>\n"+
        "	<ASNumber>" + source.ASNumber + "</ASNumber>\n"+
		"	<hopAddress>"+source.IP.getHostName()+"</hopAddress>\n"+
		"	<hopNameStr>"+source.IP.getHostName()+"</hopNameStr>\n"+
		"</Detail>\n"+
		"<Detail>\n"+
		"	<sequence>2</sequence>\n"+
		"	<hopAddressStr>" + dest.IP.getHostAddress() + "</hopAddressStr>\n"+
		"	<country>" + dest.country + "</country>\n"+
		"	<ISPName>" + dest.ISPName + "</ISPName>\n"+
        "	<ASNumber>" + dest.ASNumber + "</ASNumber>\n"+
		"	<hopAddress>"+dest.IP.getHostName()+"</hopAddress>\n"+
		"	<hopNameStr>"+dest.IP.getHostName()+"</hopNameStr>\n"+
		"</Detail>\n";
        result+="</RawDetails>\n";
        result+="</OperationResult>\n";
       }
       
       result+="</Results>\n";
       return result;
    }
  	
}
