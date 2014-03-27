/*
 * Created on 31/01/2005
 */
package dimes.measurements;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Logger;

import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * This class defines the protocols enabled.
 * Its fields' values must correspond with enum Protocol in MTR.dll.
 * @author anat
 */
//todo - change to extend MyEnum after fixed
public class Protocol //extends MyEnum
{
	private static Logger logger = Loggers.getLogger(Protocol.class);

	private static Hashtable names = new Hashtable();
	private static Hashtable headerIds = new Hashtable();
	public static final int ICMP = 1;
	public static final int UDP = 2;
	public static final int TCP = 3;
	public static final int RTP = 4;
	
	public static final short IP_PROTO_UDP = 17;
	public static final short IP_PROTO_TCP = 0x06;
	private static final short IP_PROTO_ICMP = 0x01;;

	public static int getDefault()
	{
		int proto = Protocol.ICMP;//backward compatibility with v0.3
		try
		{
			String protoStr = PropertiesBean.getProperty(PropertiesNames.DEFAULT_PROTOCOL);
			boolean hasDefault = false;
			if (PropertiesBean.isValidValue(protoStr))
			{
				proto = getValue(protoStr);
				if (proto != -1)
					hasDefault = true;
			}
			if (!hasDefault)
				proto = Protocol.ICMP;
		}
		catch (NoSuchPropertyException e)
		{
			logger.warning(e.toString());
		}
		return proto;
	}

	static
	{
		names.put(new Integer(ICMP), "ICMP");
		names.put(new Integer(UDP), "UDP");
		names.put(new Integer(TCP), "TCP");
		names.put(new Integer(RTP), "RTP");
		
		headerIds.put(new Integer(ICMP), new Short(IP_PROTO_ICMP));
		headerIds.put(new Integer(UDP), new Short(IP_PROTO_UDP));
		headerIds.put(new Integer(TCP), new Short(IP_PROTO_TCP));
		headerIds.put(new Integer(RTP), new Short(IP_PROTO_UDP));
	}
	//check
	//    public static String getName(int proto)
	//    {
	//        if ((proto<1) || (proto>names.length))
	//            return "unknown";
	//        return names[proto-1];
	//    }
	public static String getName(int type)
	{
		String aName = (String) names.get(new Integer(type));
		if (aName == null)
			aName = "";//should check measurements - used to be "unknown"
		return aName;
	}

	public static int getValue(String name)
	{
		Iterator keyIter = names.keySet().iterator();
		while (keyIter.hasNext())
		{
			Object key = keyIter.next();
			if (names.get(key).equals(name))
				return ((Integer) key).intValue();
		}
		return -1;
	}

	public static short getHeaderId(String protocolName) {
		return ((Short)headerIds.get(new Integer(getValue(protocolName)))).shortValue();
	}

}
