/*
 * Created on 01/09/2005
 *
 */
package NetGraph.utils;

/**
 * @author Ohad Serfaty
 *
 */
public  class Point
{
    public Point(){}
    
    /**
     * @param x2
     * @param y2
     * @param d
     */
    public Point(double x, double y, double z) {
        X=x;
        Y=y;
        Z=z;
    }
    
    public double X = 0;
    public  double Y = 0;
    public double Z = 0;
    
    public double XOrientation(Point p2){
        if ( X-p2.X < 0)
            return -1.0;
        return 1.0;
    }
    
    public String toString(){
        return "P:x="+X+" y="+Y;
    }
}
