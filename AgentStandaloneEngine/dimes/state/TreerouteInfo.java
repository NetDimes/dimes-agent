package dimes.state;

import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 *
 *Returns the treeroute info from properties bean
 * A copy paste of the ProtocolInfo class
 * TODO : base a shared interface ? not important enough at the time...
 *
 * * @author Ohad Serfaty
 */
public class TreerouteInfo {
	private String info;

	/**Returns the treeroute info from properties bean. Gets updated info each time toString is called
	 * 
	 * @return String Protocol Info in XML form
	 */
	public String toXML()
	{
		try
		{
			this.info = PropertiesBean.getPropertyTree(PropertiesNames.TREEROUTE_INFO);
		}
		catch (NoSuchPropertyException e)
		{
			Loggers.getLogger(this.getClass()).finer(e.toString());
			this.info = "";
		}
		return this.info;
	}
}
