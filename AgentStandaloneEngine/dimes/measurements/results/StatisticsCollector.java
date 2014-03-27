/*
 * Created on 12/12/2005
 */
package dimes.measurements.results;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;


/**
 * @author Ohad Serfaty
 *
 */
public class StatisticsCollector
{

	private HashMap statsMap = new HashMap();
	
	public void collect(StatisticsObject newStats){
		if (statsMap.containsKey(newStats.getObjectID()))
		{
			StatisticsObject knownStatistics =  (StatisticsObject) statsMap.get(newStats.getObjectID());
			knownStatistics.addStatistics(newStats);
			knownStatistics.incrementOccurences();
		}
		else
		{
			statsMap.put(newStats.getObjectID() , newStats);
		}
	}
	
	public void clear(){
		statsMap.clear();
	}
	
	/**
	 * @return
	 */
	public StatisticsObject[] getStatistics(){
		Collection values = statsMap.values();
		StatisticsObject[] statsArray = new StatisticsObject[values.size()];
		Iterator i = values.iterator();
		int j=0;
		while(i.hasNext())
		{
			statsArray[j] = (StatisticsObject) i.next();
			j++;
		}
		Arrays.sort(statsArray);
		return statsArray;
	}
	
//	public static void main(String[] args)
//	{
//		ComplexNetHost host1 = new ComplexNetHost();
//		host1.setHopAddressStr("132.66.48.22");
//		host1.bestTime=100;
//		host1.avgTime = 100;
//		host1.worstTime = 100;
//		host1.lostNum=0;
//		
//		ComplexNetHost host2 = new ComplexNetHost();
//		host2.setHopAddressStr("132.66.48.21");
//		host2.bestTime=100;
//		host2.avgTime = 100;
//		host2.worstTime = 100;
//		host2.lostNum=0;
//		
//		ComplexNetHost host3 = new ComplexNetHost();
//		host3.setHopAddressStr("132.66.48.22");
//		host3.bestTime=10;
//		host3.avgTime = 10;
//		host3.worstTime = 10;
//		host3.lostNum=0;
//		
//		ComplexNetHost host4 = new ComplexNetHost();
//		host4.setHopAddressStr("132.66.48.22");
//		host4.bestTime=50;
//		host4.avgTime = 50;
//		host4.worstTime = 50;
//		host4.lostNum=0;
//		
//		
//		
//		ComplexNetHost host5 = new ComplexNetHost();
//		host5.setHopAddressStr("unknown");
//		host5.bestTime=10;
//		host5.avgTime = 10;
//		host5.worstTime = 10;
//		host5.lostNum=1;
//		
//		ComplexNetHost host6 = new ComplexNetHost();
//		host6.setHopAddressStr("132.66.48.21");
//		host6.bestTime=50;
//		host6.avgTime = 50;
//		host6.worstTime = 50;
//		host6.lostNum=0;
//		
//		StatisticsCollector collector = new StatisticsCollector();
//		collector.collect(host1);
//		collector.collect(host2);
//		collector.collect(host3);
//		collector.collect(host5);
//		collector.collect(host4);
//		collector.collect(host6);
//		
//		StatisticsObject[] statsObj = collector.getStatistics();
//		for (int i=0; i<statsObj.length; i++)
//			System.out.println(statsObj[i]);
//		
//	}
	
}
