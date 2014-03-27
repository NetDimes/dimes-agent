/**
 * 
 */
package dimes.state;

import java.io.File;

//import org.dom4j.Document;
//import org.dom4j.Element;
//import org.dom4j.io.SAXReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;

import dimes.util.XMLUtil;

/**
 * This class added in order to support in the 
 * default script. 
 * 
 * 
 * @author idob
 * 
 */
public final class DefaultScriptInfo {
	
	private static final String SCRIPT_ELEMENT = "Script";
	private static final String SCRIPT_ELEMENT_ID = "id";

	/**
	 * @return XML with the version of the current default script
	 */
	public final String toXML() {
		
//		SAXReader reader = new SAXReader();
		Document defaultScriptDoc = null;
		Element root = null;
		Element scriptElement = null;
		File defaultScriptFile = null;
		
		String id = null;
		String info = null;
		String version = null;
		
		block: {
			
			try {
				defaultScriptFile = new File(PropertiesBean
						.getProperty(PropertiesNames.DERFAULT_IN_FILE));
				
				if (!defaultScriptFile.exists()) {
					info = "";
					break block; //If we can't find the file, break out of the entire block
				}
				
//				defaultScriptDoc = reader.read(defaultScriptFile);
				root = XMLUtil.getRootElement(defaultScriptFile);//defaultScriptDoc.getRootElement();
				
			} catch (Exception e) {
				info = "";
				break block; 
			}
			
			scriptElement = XMLUtil.getChildElementByName(root, SCRIPT_ELEMENT);
			id = scriptElement.getAttribute(SCRIPT_ELEMENT_ID);
			
			try {
				
				version = id.substring(0, id.indexOf('_'));
				info = String.valueOf(Integer.parseInt(version));
				
			} catch (Exception e) {
				info = "";
			}
			
		}
		
		return "<defaultScriptVersion>" + info + "</defaultScriptVersion>";


	}
}
