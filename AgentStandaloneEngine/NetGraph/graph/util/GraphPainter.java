/*
 * Created on 14/02/2005
 *
 */
package NetGraph.graph.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Iterator;

import NetGraph.graph.components.Edge;
import NetGraph.graph.components.Node;
import NetGraph.utils.ArrowDrawer;
import NetGraph.utils.DistancesMath;

/**
 * @author Ohad
 * 
 * this class is used in order to paint the graph.
 * 
 *  NOTE : this class's methods are not synchronized, any attemp 
 * to change the parameters given to it are not monitored , and
 * therefore it should strictly be synchronized from outside.
 *
 */
public class GraphPainter {
	
	// Color constants :
	final Color fixedColor =Color.blue;
	final Color origineColor = new Color(97, 145, 194);	
	final Color selectColor = new Color(241, 201, 0);
	final Color edgeColor =Color.black;
	final Color activeNodeColor = new Color(187, 166, 255); 	
	final static Color nodeColor =new Color(250, 220, 100);
	public static final int ISP_COLOR_DISPLAY=1;
	public static final int COUNTRY_COLOR_DISPLAY=2;
	private static final Color PROGRESS_COLOR = new Color(170,30,210);
	private int colorDisplay = ISP_COLOR_DISPLAY;
	private Image localhostImage = null;
    private static final Color BLACK_TRANSPARENT = new Color(0,0,0,Node.TRANSPARENCY_ALPHA_CONSTANT);
    private int currentMouseY;
    private int currentMouseX;
	
    Font defaultFont = new Font("Arial", 0, 13);
    Font[] magnifyFonts;
    private boolean magnify=false;
    
	public GraphPainter(Image aLocalhostImage){
	    localhostImage = aLocalhostImage;
	    magnifyFonts = new Font[14];
	    for (int i=0; i<14; i++)
	        magnifyFonts[i] = new Font("Arial", 0, 13+i); 
	}
	
	/************
	 * 
	 * @param magnifyFactor a factor between 1.0 and 2.0 
	 * @return
	 */
	public Font getMagnifiedFont(double magnifyFactor){
	    int fontSize = (int)(13*magnifyFactor);
	    if (fontSize>26)
	        return magnifyFonts[13];
	    if (fontSize<13)
	        return magnifyFonts[0];
	    System.out.println("Returning " + (fontSize-13));
	    return magnifyFonts[fontSize-13];
	}
	
	/*******************
	 * paint all the edges of the Graph :
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
	 * therefore it should strictly be synchronized from outside.
	 * 
	 * @param offgraphics graphics env.
	 * @param data the data object.
	 */
	public void paintEdges(Graphics offgraphics,GraphData data){
		
	    offgraphics.setColor(edgeColor);
		/* Pour chaque Arc */
		for (int i = 0 ; i < data.nedges ; i++) {
			Edge e = data.edges[i];
			int x1 = (int)data.nodes[e.from].x;
			int y1 = (int)data.nodes[e.from].y;
			int x2 = (int)data.nodes[e.to].x;
			int y2 = (int)data.nodes[e.to].y;
			
			if (!e.isPruned && !e.isCollapsed && !e.isFiltered)
			{
			    offgraphics.setColor(e.isFiltered ? BLACK_TRANSPARENT:edgeColor);			    
				offgraphics.drawLine(x1, y1, x2, y2);
				ArrowDrawer.drawArrow(offgraphics,x1,y1,x2,y2,1.7f);
			}
			
		}
	}
	
	/*******************
	 * paint all the virtual edges of the Graph :
	 * 
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
	 * therefore it should strictly be synchronized from outside.
	 * 
	 * @param offgraphics graphics env.
	 * @param data the data object.
	 */
	public void paintVirtualEdges(Graphics offgraphics,GraphData data){
		// Draw the virtual edges :
		Iterator k  = data.virtualEdges.iterator();
		while (k.hasNext()){
			Edge virtualEdgeToDraw = (Edge) k.next();
			int x1 = (int)data.nodes[virtualEdgeToDraw.from].x;
			int y1 = (int)data.nodes[virtualEdgeToDraw.from].y;
			int x2 = (int)data.nodes[virtualEdgeToDraw.to].x;
			int y2 = (int)data.nodes[virtualEdgeToDraw.to].y;
			if (!virtualEdgeToDraw.isPruned && !data.nodes[virtualEdgeToDraw.to].isPruned
					&&
					!data.nodes[virtualEdgeToDraw.to].isFiltered
					&&
					!data.nodes[virtualEdgeToDraw.from].isFiltered
			)
			{
				offgraphics.setColor(Color.ORANGE);  			
				offgraphics.drawLine(x1, y1, x2, y2);
				ArrowDrawer.drawArrow(offgraphics,x1,y1,x2,y2,1.7f);
			}
		}
	}
	
	public void paintNodes(Graphics offgraphics,GraphData data ,Node pickedNode ){
	    FontMetrics fm = offgraphics.getFontMetrics();
	    for (int i = 0 ; i < data.nnodes ; i++) {
	        drawNode(offgraphics, data.nodes[i], fm,pickedNode==data.nodes[i]);
	    }
	}
	
	/** On affiche un noeud
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
	 * therefore it should strictly be synchronized from outside.
	 * 
	 * @param g Graphics ou il faut peindre
	 * @param n node
	 * @param act si cette node est active
	 * @param fm Police, font
	 */
	public void drawNode(Graphics g,Node n,FontMetrics fm, boolean isPicked) {
//	    if (n.isCollapsed){
//	        g.setColor(Color.WHITE);
//	        g.drawOval((int)n.x-8,(int)n.y-5,16,11);
//	        return;
//	    }
	    if (n.isPruned || n.isCollapsed || n.isFiltered)
	        return;
	    else
	    {
	        String name =n.lbl;
	        int x = (int)n.x;
	        int y = (int)n.y;  	
	        double distanceFromMouse = DistancesMath.getDistance(x,y,currentMouseX,currentMouseY);
	        
	        int nodeXRadius=8,nodeYRadius=5,nodeXDiameter=16,nodeYDiameter=11;
	        int nodeAgentRadius=12,nodeAgentDiameter=25;
	        int fontFactor=1;
	        
	        double magnifyFactor=1.0;
	        
	        if (magnify && distanceFromMouse<100.0)
	        {
	            magnifyFactor=1.0+(100.0-distanceFromMouse)/50.00;
	            g.setFont(getMagnifiedFont(magnifyFactor));
	        }
	        else
	            g.setFont(defaultFont);

	        g.setColor(isPicked ? selectColor :		    
	            //		    (n.isInProgress? PROGRESS_COLOR :
	            (n.isCollapsed) ? Color.DARK_GRAY :
	                (n.isFiltered) ? (this.colorDisplay == COUNTRY_COLOR_DISPLAY ? n.countryColorFiltered:
	                    n.asColorFiltered) : 
	                        (this.colorDisplay == COUNTRY_COLOR_DISPLAY ? n.countryColor:
	                            n.asColor)/*)*/);	        
	        if (name == null)
	        {
	            if (n.isAgent)
	                g.drawImage(this.localhostImage,
	                        x-(int)(nodeAgentRadius*magnifyFactor),y-(int)(nodeAgentRadius*magnifyFactor),
	                        (int)(nodeAgentDiameter*magnifyFactor),(int)(nodeAgentDiameter*magnifyFactor)
	                               ,null);
	            else
	            {
	                g.fillOval(x-(int)(nodeXRadius*magnifyFactor),(int)(y-nodeYRadius*magnifyFactor),
	                        (int)(nodeXDiameter*magnifyFactor),(int)(magnifyFactor*nodeYDiameter));
	                if (!n.isFiltered)
	                    g.setColor( n.fixed ? fixedColor : Color.black);
	                g.drawOval(x-(int)(nodeXRadius*magnifyFactor),(int)(y-nodeYRadius*magnifyFactor)
	                        ,(int)(nodeXDiameter*magnifyFactor),(int)(magnifyFactor*nodeYDiameter));
	            }
	        }
	        else
	        {
	            /*(n.origine ? origineColor : (isActive) ? activeNodeColor : nodeColor) );*/
	            int w = g.getFontMetrics().stringWidth(name) + (int)(10);
	            int h = g.getFontMetrics().getHeight() + (int)(4);
	            //  			g.fillRect(x - w/2, y - h / 2, w, h);
	            g.fillRoundRect(x - w/2, y - h / 2, w, h, (int)(10),(int)(10));
	            g.setColor(!n.isFiltered ?( n.fixed ? fixedColor : Color.black) : BLACK_TRANSPARENT);
	            g.drawRoundRect(x - w/2, y - h / 2, w, h,(int)(10),(int)(10));
	            g.setColor (!n.isFiltered ? Color.black : BLACK_TRANSPARENT);
	            g.drawString(name, x - ((w-(int)(10))/2), y - ((h-(int)(4))/2) + (int)(g.getFontMetrics().getAscent()));
	        }	        
	    }
	}
	
	/**********************
	 * 
	 *  NOTE : this class's methods are not synchronized, any attemp 
	 * to change the parameters given to it are not monitored , and
	 * therefore it should strictly be synchronized from outside.
	 * 
	 * @param aColorDisplay - an indication which color is to be displayed
	 * meaning ISP_COLOR_DISPLAY etc.
	 */
	public synchronized void setColorDisplay(int aColorDisplay){
		this.colorDisplay = aColorDisplay;
//		System.out.println("setting color display = "+colorDisplay);
	}


    /**
     * @param x
     * @param y
     */
    public void updateMousePosition(int currentMouseX, int currentMouseY) {
        this.currentMouseX = currentMouseX;
        this.currentMouseY = currentMouseY;
    }
    
    public void setMagnify(boolean doMagnify){
        this.magnify = doMagnify;
    }
	
}
