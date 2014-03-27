/*
 * Created on 01/09/2005
 *
 */
package NetGraph.utils;

/**
 * @author Ohad Serfaty
 *
 */
public class DistancesMath {



    public static double Magnitude( Point Point1, Point Point2 )
    {
        Point vector= new Point();

        vector.X = Point2.X - Point1.X;
        vector.Y = Point2.Y - Point1.Y;
        vector.Z = Point2.Z - Point1.Z;

        double sqr = vector.X * vector.X + vector.Y * vector.Y + vector.Z * vector.Z ;
        return Math.sqrt( sqr);
    }
    

    public static double distancePointLine(Point Point, Point LineStart, Point LineEnd)
    {
        double LineMag;
        double U;
        Point Intersection = new Point();
        double distance = 0.0;
     
        LineMag = Magnitude( LineEnd, LineStart );
     
        U = ( ( ( Point.X - LineStart.X ) * ( LineEnd.X - LineStart.X ) ) +
            ( ( Point.Y - LineStart.Y ) * ( LineEnd.Y - LineStart.Y ) ) +
            ( ( Point.Z - LineStart.Z ) * ( LineEnd.Z - LineStart.Z ) ) ) /
            ( LineMag * LineMag );
     
        if( U < 0.0f || U > 1.0f )
            return distance;   // closest point does not fall within the line segment
     
        Intersection.X = LineStart.X + U * ( LineEnd.X - LineStart.X );
        Intersection.Y = LineStart.Y + U * ( LineEnd.Y - LineStart.Y );
        Intersection.Z = LineStart.Z + U * ( LineEnd.Z - LineStart.Z );
     
        
        distance = Magnitude( Point, Intersection );
        return Point.XOrientation(Intersection)*distance;
    }
    
    public static Point getItersection(Point Point, Point LineStart, Point LineEnd)
    {
        double LineMag;
        double U;
        Point Intersection = new Point();
        double distance = 0.0;
     
        LineMag = Magnitude( LineEnd, LineStart );
     
        U = ( ( ( Point.X - LineStart.X ) * ( LineEnd.X - LineStart.X ) ) +
            ( ( Point.Y - LineStart.Y ) * ( LineEnd.Y - LineStart.Y ) ) +
            ( ( Point.Z - LineStart.Z ) * ( LineEnd.Z - LineStart.Z ) ) ) /
            ( LineMag * LineMag );
        if (Double.isNaN(U))
        {
//            System.out.println("Nan U accepted for :" + Point + " , " + LineStart + " , " + LineEnd);
            return null;
        }
     
        if( U < 0.0f || U > 1.0f )
            return null;   // closest point does not fall within the line segment
     
        Intersection.X = LineStart.X + U * ( LineEnd.X - LineStart.X );
        Intersection.Y = LineStart.Y + U * ( LineEnd.Y - LineStart.Y );
        Intersection.Z = LineStart.Z + U * ( LineEnd.Z - LineStart.Z );
        if (Double.isNaN(Intersection.X) || Double.isNaN(Intersection.Y))
        {
//            System.out.println("Nan intersection accepted for :" + Point + " , " + LineStart + " , " + LineEnd);
            return null;
        }
        return Intersection;
    }


    /**
     * @param d
     * @param e
     * @param f
     * @param g
     * @return
     */
    public static double getDistance(double d, double e, double f, double g) {
       return Math.sqrt((d-f)*(d-f) + (e-g)*(e-g));
    }


    /**
     * @param d
     * @param e
     * @param f
     * @param g
     * @param h
     * @param i
     * @param j
     * @param k
     * @return
     */
    public static boolean linesIntersect(double x1, double y1, double x2, double y2,
            double u1, double v1, double u2, double v2) {
        if (x2==x1 || y2==y1)
            return false;
        double b1 = (y2-y1)/(x2-x1);
        double b2 = (v2-v1)/(u2-u1);
        double a1 = y1-b1*x1;
        double a2 = v1-b2*u1;

        if (b1==b2)
            return false;
        
        double xi = - (a1-a2)/(b1-b2);
        double yi = a1+b1*xi;
        if ( ((x1-xi)*(xi-x2)>=0) && 
                ((u1-xi)*(xi-u2)>=0) && 
                ((y1-yi)*(yi-y2)>=0) &&
                ((v1-yi)*(yi-v2)>=0))
        {
            System.out.println("Conversion :" + xi+" "+yi);
            return true;
        }
//        then print "lines cross at",xi,yi else print "lines do not cross" end if 
        return false;
    }
    
}
