/*
 * Created on 14/02/2005
 *
 */
package NetGraph.utils;

import java.awt.Graphics;
import java.awt.Polygon;

/**
 * @author Ohad
 *
 */
public class ArrowDrawer {

    public static Polygon tmpPoly = new Polygon();
    
	/*************************
  	 * draw an arrow on the screen :
  	 * 
  	 * @param g2d
  	 * @param xCenter
  	 * @param yCenter
  	 * @param x
  	 * @param y
  	 * @param stroke
  	 */
  	public static void drawArrow(Graphics g2d, int xCenter, int yCenter, int x, int y, float stroke) {
  		double aDir=Math.atan2(xCenter-x,yCenter-y);
  		g2d.drawLine(x,y,xCenter,yCenter);
  		//        g2d.se(new BasicStroke(1f));					// make the arrow head solid even if dash pattern has been specified
  		
  		tmpPoly.reset();
  		int i1=12+(int)(stroke*2);
  		int i2=6+(int)stroke;							// make the arrow head the same size regardless of the length length
  		tmpPoly.addPoint(x,y);							// arrow tip
  		tmpPoly.addPoint(x+xCor(i1,aDir+.5),y+yCor(i1,aDir+.5));
  		tmpPoly.addPoint(x+xCor(i2,aDir),y+yCor(i2,aDir));
  		tmpPoly.addPoint(x+xCor(i1,aDir-.5),y+yCor(i1,aDir-.5));
  		tmpPoly.addPoint(x,y);							// arrow tip
  		g2d.drawPolygon(tmpPoly);
  		g2d.fillPolygon(tmpPoly);						// remove this line to leave arrow head unpainted
  	}
  	
  	
  	private static int yCor(int len, double dir) {return (int)(len * Math.cos(dir));}
  	private static int xCor(int len, double dir) {return (int)(len * Math.sin(dir));}
  	

}
