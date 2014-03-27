/*
 * Created on 14/02/2005
 *
 */
package NetGraph.graph.util;

import java.awt.Dimension;
import java.util.Iterator;
import java.util.Vector;

import NetGraph.graph.components.Edge;
import NetGraph.graph.components.Node;

/**
 * @author Ohad
 *
 * a class responsible for the relaxation of a graph's Nodes. 
 * 
 * NOTE : this class's methods are not synchronized, any attemp 
 * to change the parameters given to it are not monitored , and
 * therefore it should strictly be synchronized from outside.
 * 
 */
public class GraphRelaxer {
	
	private int centerMassDX=0;
	private int centerMassDY=0;
	private volatile double DAMPING_CONSTANT=50000.0;
	
	public int relaxingConstrain = Edge.RELAX_GROUP_BY_NONE;
	public static final double DEFAULT_LENGTH = 55.0;
	public double currentDefaultLength = DEFAULT_LENGTH;
	
	private static final int GRAPH_BORDER_SIZE=10;
    private boolean doTravel=false;
    private volatile int mouseX;
    private volatile int mouseY;
    private static final double MAX_DAMPING = 15000;
    
    public static int RELAX_NORMAL = 3;
    public static int RELAX_ACTIVE_DRAG = 5;
    public static int RELAX_STRICT_DRAG = 7;
    
    public int relaxManner = RELAX_NORMAL;
	
	/******************
	 * main relax system :
	 * 
	 * NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
	 * therefore it should strictly be synchronized from outside.
	 * 
	 * @param data
	 * @param panelSize
	 */
	public void relax(GraphData data, Dimension panelSize) {
		long currentTime = System.currentTimeMillis();
	    
	    // For each edge : direct it toward it's length:
	    for (int i = 0 ; i < data.nedges ; i++) {
			Edge e = data.edges[i];
			double vx = data.nodes[e.to].x - data.nodes[e.from].x;
			double vy = data.nodes[e.to].y - data.nodes[e.from].y;
			double len = Math.sqrt(vx * vx + vy * vy);
			
			
			len = (len == 0) ? .0001 : len;
			double f = (data.edges[i].len - len) / (len * 3.0);
			double dx = f * vx;
			double dy = f * vy;
			
			data.nodes[e.to].dx += dx;
			data.nodes[e.to].dy += dy;
			data.nodes[e.from].dx -= dx;
			data.nodes[e.from].dy -= dy;
			
		}
	    
	    
	    
	    
		/* Pour chaque noeud*/
		for (int i = 0 ; i < data.nnodes ; i++) {
			Node n1 = data.nodes[i];
			double dx = 0;
			double dy = 0;
//			long startTimeAdvanced=0;
			/* Pour chaque noeud*/
			for (int j = 0 ; j < data.nnodes ; j++) {
				if (i == j) {
					continue;
				}
				Node n2 = data.nodes[j];
				double vx = n1.x - n2.x;
				double vy = n1.y - n2.y;
				double len = vx * vx + vy * vy;
				if (len < 3) {
					dx += Math.random();
					dy += Math.random();
//				    startTimeAdvanced+=10;
				}
				else if (len < 100*100) {
					dx += vx / len;
					dy += vy / len;
//					startTimeAdvanced++;
				}
				
			}
			double dlen = dx * dx + dy * dy;
			if (dlen > 0) {
				dlen = Math.sqrt(dlen) / 2;
				n1.dx += dx / dlen;
				n1.dy += dy / dlen;
//				n1.startTime+=startTimeAdvanced;
			}
			//			System.out.println("2 " +n1.IPtexte + " n.dx="+n1.dx +" n.dy="+n1.dy);
		}
		if (this.relaxManner!= GraphRelaxer.RELAX_NORMAL){
		    for (int i = 0 ; i < data.nnodes ; i++) {
		        Node n = data.nodes[i];
		        if (n.isCollapsed)
		        {
		            Node lineN1 = data.nodes[n.virtualSource];
		            Node lineN2 = data.nodes[n.virtualDest];
		            
		            double maxDist = data.nodes[n.virtualDest].distFromSource;
		            double currentDist = n.distFromSource;
		            //		        System.out.println("current :" + currentDist + " max:" + maxDist) ;
		            double desiredX = lineN1.x + ((double)currentDist/(double)maxDist
		                    *(lineN2.x-lineN1.x));
		            double desiredY = lineN1.y + ((double)currentDist/(double)maxDist
		                    *(lineN2.y-lineN1.y));
		            n.x=desiredX;
		            n.y=desiredY;
		        }
		        else
		        {
		            if (!(n.virtualSource==-1 || n.virtualDest == -1))
		            {
		                Node lineN1 = data.nodes[n.virtualSource];
		                Node lineN2 = data.nodes[n.virtualDest];
		                double maxDist = data.nodes[n.virtualDest].distFromSource;
		                double currentDist = n.distFromSource;
		                //			        System.out.println("current :" + currentDist + " max:" + maxDist) ;
		                double desiredX = lineN1.x + ((double)currentDist/(double)maxDist
		                        *(lineN2.x-lineN1.x));
		                double desiredY = lineN1.y + ((double)currentDist/(double)maxDist
		                        *(lineN2.y-lineN1.y));
		                
		                if (this.relaxManner == GraphRelaxer.RELAX_ACTIVE_DRAG)
		                {
		                    if (Math.abs(desiredX-n.x)>15.0)
		                    {
		                        n.dx += (desiredX-n.x)/10.0;
		                    }
		                    else
		                        n.x=desiredX;
		                    if (Math.abs(desiredY-n.y)>15.0)
		                    {
		                        n.dy += (desiredY-n.y)/10.0;
		                    }
		                    else
		                        n.y=desiredY;
		                }
		                if (this.relaxManner == GraphRelaxer.RELAX_STRICT_DRAG){
		                    n.x=desiredX;
		                    n.y=desiredY;
		                }
		            }
		            
		        }
		    }
		}
		
		double centerOfMassX = 0.0;
		double centerOfMassY = 0.0;
		int centerOfMassCalcNodes = 0;
		
		double distFromCenterMass = 0.0;
		
		
		// fix the nodes position according to n.dx , n.dy :
		for (int i = 0 ; i < data.nnodes ; i++) {
			Node n = data.nodes[i];
			double dampFactor=1.0;
//			 System.out.println(n.dx+" "+n.dy);
			if (Math.abs(n.dx) > 10.0 || Math.abs(n.dy) > 10.0)
			    n.startTime = currentTime;
//			
			long timeFromStart = Math.min(currentTime - n.startTime,(long)DAMPING_CONSTANT);
			dampFactor = Math.abs((DAMPING_CONSTANT - (timeFromStart))/DAMPING_CONSTANT);
//			System.out.println("damp:" + dampFactor+" "+DAMPING_CONSTANT);
//			double dampFactor=1.0;
//			if (currentTime - n.startTime > 5000)
//			    dampFactor = (double)DAMPING_CONSTANT/(double)MAX_DAMPING;
			
//		    if (dampFactor < 0.2)
//		        dampFactor=0.2;
		    double mouseDistance = Math.sqrt( (n.x-mouseX)*(n.x-mouseX) + 
					 (n.y-mouseY)*(n.y-mouseY) );
		    if (mouseDistance < 100.0)
		    {
		        n.startTime=timeFromStart;
		        dampFactor=1.0;
		    }
//		    System.out.println("damp :" + dampFactor);
//		    System.out.println(n.IPtexte + " n.dx="+n.dx +" n.dy="+n.dy);
			if (!n.fixed) 
			{
				n.x += Math.max(-5, Math.min(5, n.dx*dampFactor));
				n.y += Math.max(-5, Math.min(5, n.dy*dampFactor));
				
				n.x+=this.centerMassDX;
				n.y+=this.centerMassDY;
				
				// calculate the center of mass so that expansion will 
				// be possible :
				centerOfMassX += n.x;
				centerOfMassY += n.y;
				centerOfMassCalcNodes++;
				
				distFromCenterMass += Math.sqrt(
						(panelSize.width/2-n.x)*(panelSize.width/2-n.x)
						+
						(panelSize.height/2-n.y)*(panelSize.height/2-n.y));
				
				
			}
			if (n.x < GRAPH_BORDER_SIZE) {
				n.x = GRAPH_BORDER_SIZE;
			} else if (n.x > (panelSize.width-GRAPH_BORDER_SIZE)) {
				n.x = (panelSize.width-GRAPH_BORDER_SIZE);
			}
			if (n.y < GRAPH_BORDER_SIZE) {
				n.y = GRAPH_BORDER_SIZE;
			} else if (n.y > (panelSize.height-GRAPH_BORDER_SIZE)) {
				n.y = (panelSize.height-GRAPH_BORDER_SIZE);
			}
			
		
			    n.dx =(n.dx<1 && n.dx>-1) ? 0 : n.dx/2;	
			    n.dy = (n.dy<1 && n.dy>-1) ? 0 : n.dy/2;	
//			System.out.println("3 " +n.IPtexte + " n.dx="+n.dx +" n.dy="+n.dy);
		}
		
		// adjust the center of mass :
		if (centerOfMassCalcNodes > 0)
		{
			centerOfMassX = centerOfMassX/centerOfMassCalcNodes;
			centerOfMassY = centerOfMassY/centerOfMassCalcNodes;
			
			
			int desiredCenterOfMAssX = panelSize.width/2;
			int desiredCenterOfMAssY = panelSize.height/2;
			
			if (( Math.abs(centerOfMassX-desiredCenterOfMAssX) > 5.0 ) 
			        ||
			        (Math.abs(centerOfMassY-desiredCenterOfMAssY) > 5.0)){				
				this.centerMassDX = (desiredCenterOfMAssX>centerOfMassX?1:-1);
				this.centerMassDY = (desiredCenterOfMAssY>centerOfMassY?1:-1);
			}
			else
			{
				centerMassDX = 0;
				centerMassDY = 0;
			}
			
//			distFromCenterMass = distFromCenterMass/centerOfMassCalcNodes;
//			double desiredDistanceFromCenterMAss =Math.sqrt(
//					(panelSize.width/2)*(panelSize.width/2)
//					+
//					(panelSize.height/2)*(panelSize.height/2))/2.0;
//			
//			
//			if ( Math.abs(distFromCenterMass-desiredDistanceFromCenterMAss) > 5.0){
//				this.expandEdgesLength(data,distFromCenterMass<desiredDistanceFromCenterMAss?1.0:-1.0);
//			}
			
			
//			System.out.println("Distance from center:" +distFromCenterMass );
//			System.out.println("Desired Distance from center:" +desiredDistanceFromCenterMAss );
			
		}
		
	}
	

	/***********************
	 * 
	 * a function which updates the relaxation constrain 
	 * 
	 * NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
	 * therefore it should strictly be synchronized from outside.
	 * 
	 * @param relaxationConstrain - should be either Edge.RELAX_BY_COUNTRY,
	 * Edge.RELAX_BY_ISP etc.
	 */
	public void updateRelaxation(GraphData data, Edge edge , int relaxationConstrain){
		Node source=null,dest=null;
		switch(relaxationConstrain)
		{
		// grouping by no constrain :
		case Edge.RELAX_GROUP_BY_NONE:
			edge.len = currentDefaultLength;
		break;
		
		// grouping by the country name :
		case Edge.RELAX_GORUP_BY_COUNTRY:
			source = data.nodes[edge.from];
		dest = data.nodes[edge.to];
//		source.fixed = false;
//		dest.fixed = false;
		if (source.country.equals(dest.country))
			edge.len = 5.0;
		else
			edge.len = 130.0;  			
		break;
		
		// grouping by the ISP name :
		case Edge.RELAX_GROUP_BY_ISP:
			source = data.nodes[edge.from];
		dest = data.nodes[edge.to];		
//		source.fixed = false;
//		dest.fixed = false;
		if (source.ISPName.equals(dest.ISPName))
			edge.len = 5.0;
		else
			edge.len = currentDefaultLength ; //130.0
		break;
		default:
			edge.len = currentDefaultLength; // 130.0
		break;
		}
	}
	
	
	
	private void adjustEdgesSize(GraphData data)
	{
	    switch(relaxingConstrain){
	    case Edge.RELAX_GORUP_BY_COUNTRY:
	        startRelaxingByCountry(data);
	        break;
	    case Edge.RELAX_GROUP_BY_ISP:
	        startRelaxingByISP(data);
	        break;
	    case Edge.RELAX_GROUP_BY_NONE:
	        startRelaxingByDefault(data);
	        break;
	    default:
	        break;
	    }
	    
	}
	
	
	public void startRelaxingByCountry(GraphData data){
		relaxingConstrain = Edge.RELAX_GORUP_BY_COUNTRY;
		for (int i=0; i<data.nedges; i++){
			Edge edge = data.edges[i];
			this.updateRelaxation(data,edge,this.relaxingConstrain);		
		}
	}
	
	public void startRelaxingByISP(GraphData data){
		relaxingConstrain = Edge.RELAX_GROUP_BY_ISP;
		for (int i=0; i<data.nedges; i++){
			Edge edge = data.edges[i];
			this.updateRelaxation(data,edge,this.relaxingConstrain);		
		}
	}
	
	public void startRelaxingByDefault(GraphData data){
		relaxingConstrain = Edge.RELAX_GROUP_BY_NONE;
		for (int i=0; i<data.nedges; i++){
			Edge edge = data.edges[i];
			this.updateRelaxation(data,edge,this.relaxingConstrain);		
		}
	}
	
	/**
	 * @param data
	 * @param nodeIndex
	 * 
	 * NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
	 * therefore it should strictly be synchronized from outside.
	 * 
	 */
	public void updateRelaxationByInfo(GraphData data, int nodeIndex) {
		Vector edgesToUpdateByDest = data.findEdgesWithDest(nodeIndex);
		Vector edgesToUpdateBySource= data.findEdgesWithSource(nodeIndex);
		edgesToUpdateByDest.addAll(edgesToUpdateBySource);
		for (Iterator k = edgesToUpdateByDest.iterator();
		k.hasNext(); 
		updateRelaxation(data,(Edge)k.next(),relaxingConstrain));
		
	}
	
	/**
	 * @param data
	 * @param addedEdge
	 * 
	 * NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
	 * therefore it should strictly be synchronized from outside.
	 * 
	 */
	public void updateRelaxation(GraphData data, Edge addedEdge) {
		this.updateRelaxation(data,addedEdge,this.relaxingConstrain);
		
	}
	
	public void travel(boolean travelVal){
	    this.doTravel= travelVal;
	}

    /**
     * @param x
     * @param y
     */
    public void updateMousePosition(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
//        System.out.println("Updating : " +mouseX + " , " + mouseY );
    }
	
	
    public void setRelaxManner(int aRelaxManner){
        this.relaxManner=aRelaxManner;
    }
    
    public void setDampingConstane(double constant){
	    DAMPING_CONSTANT = 1+constant;
	}
    
//	
//	/***********************
//	 * 
//	 * a function which updates the relaxation constrain 
//	 * 
//	 * NOTE : this class's methods are not synchronized, any attemp 
//	 * to change the parameters given to it are not monitored , and
//	 * therefore it should strictly be synchronized from outside.
//	 * 
//	 * @param relaxationConstrain - should be either Edge.RELAX_BY_COUNTRY,
//	 * Edge.RELAX_BY_ISP etc.
//	 */
//	public void expandEdgeRelaxation(GraphData data, Edge edge ,
//			int relaxationConstrain, double lengthExpansion){
//		Node source=null,dest=null;
//		switch(relaxationConstrain)
//		{
//		// grouping by no constrain :
//		case Edge.RELAX_GROUP_BY_NONE:
//			edge.len+= lengthExpansion;
//		break;
//		
//		// grouping by the country name :
//		case Edge.RELAX_GORUP_BY_COUNTRY:
//			source = data.nodes[edge.from];
//		dest = data.nodes[edge.to];
////		source.fixed = false;
////		dest.fixed = false;
//		if (source.country.equals(dest.country))
//			edge.len = 5.0;
//		else
//			edge.len += lengthExpansion;  			
//		break;
//		
//		// grouping by the ISP name :
//		case Edge.RELAX_GROUP_BY_ISP:
//			source = data.nodes[edge.from];
//		dest = data.nodes[edge.to];		
////		source.fixed = false;
////		dest.fixed = false;
//		if (source.ISPName.equals(dest.ISPName))
//			edge.len = 5.0;
//		else
//			edge.len +=lengthExpansion; 
//		break;
//		default:
//			edge.len = Edge.DEFAULT_LENGTH;
//		break;
//		}
//	}
	
	
}
