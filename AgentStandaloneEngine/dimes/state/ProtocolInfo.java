/*
 * Created on 02/05/2005
 */
package dimes.state;

import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * @author anat
 */
public class ProtocolInfo
{
	private String info;

	
	/**Returns the protocol info from properties bean. Gets updated info each time toString is called
	 * 
	 * @return String Protocol Info in XML form
	 */
	public String toXML()
	{
		try
		{
			this.info = PropertiesBean.getPropertyTree(PropertiesNames.PROTOCOL_INFO);
		}
		catch (NoSuchPropertyException e)
		{
			Loggers.getLogger(this.getClass()).finer(e.toString());
			this.info = "";
		}
		return this.info;
	}
}