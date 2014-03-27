/**
 * 
 */
package dimes.util;

import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * @author Ohad Serfaty
 *
 */
public interface HeaderProducer {
	
	public String getAgentHeader(boolean askForWork) throws NoSuchPropertyException;
	public String getAgentTrailer();
	

}
