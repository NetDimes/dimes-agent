/*
 * Created on 31/01/2005
 */
package dimes.measurements;

import java.util.Hashtable;

/**
 * This class defines the measurement types enabled.
 * Its fields' values must correspond with enum MeasurementType in MTR.dll.
 * @author anat
 */
//todo - change to extend MyEnum after fixed
public class MeasurementType //extends MyEnum
{
	private static Hashtable<Integer, String> names = new Hashtable<Integer, String>();
	// for traceroute and ping - the values mean initial TTL
	public static final int TRACEROUTE = 1;
	public static final int PING = 100;
//	public static final int PACKETTRAIN = 101; //steger
//	public static final int TREEROUTE = 200; 
	public static final int QBE = 300;
//	public static final int PEER_PACKET_TRAIN = 400;
	public static final int NEW_OP=999; //test
	public static final int PARIS_TRACEROUTE = 500;
	public static final int DIMES_QBE = 1000;
	public static final int UPDATE = 2000;

	static
	{
		names.put(new Integer(TRACEROUTE), "TRACEROUTE");
		names.put(new Integer(PING), "PING");
//		names.put(new Integer(PACKETTRAIN), "PACKETTRAIN"); //steger
//		names.put(new Integer(TREEROUTE), "TREEROUTE"); 
//		names.put(new Integer(TREEROUTE), "TREEROUTE");
		names.put(new Integer(QBE), "QBE");
//		names.put(new Integer(PEER_PACKET_TRAIN), "PEERTRAIN");
		names.put(new Integer(NEW_OP), "NEW_OP");
		names.put(new Integer(PARIS_TRACEROUTE), "PTRACEROUTE");
		names.put(new Integer(DIMES_QBE), "DIMES_QBE");
		names.put(new Integer(UPDATE), "UPDATE");
		
	}

	public static String getName(int type)
	{
		String aName = (String) names.get(new Integer(type));
		if (aName == null)
			aName = "";
		return aName;
	}
	
}