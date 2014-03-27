/*
 * Created on 12/12/2005
 */
package dimes.measurements.results;

/**
 * @author Ohad Serfaty
 *
 */
public interface StatisticsObject extends Comparable
{
	public Object getObjectID();
	public void addStatistics(StatisticsObject statsObj);
	public int getOccurences();
	public void incrementOccurences();
	

	
}
