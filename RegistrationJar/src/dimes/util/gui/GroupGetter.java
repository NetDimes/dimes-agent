/*
 * Created on 23/03/2005
 *
 */
package dimes.util.gui;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import dimes.util.XMLUtil;
//import org.dom4j.Document;
//import org.dom4j.Element;
//import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import dimes.comm2server.ConnectionHandlerFactory;
import dimes.comm2server.StandardCommunicator;
import dimes.util.FileHandlerBean;
import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * @author anat
 */
public class GroupGetter
{
	private StandardCommunicator commHandler = null;
	private FileHandlerBean fileHandler = null;
	private Logger logger;

	public GroupGetter()
	{
		this.logger = Loggers.getLogger(this.getClass());

		String urlStr = "";
		try
		{
			urlStr = PropertiesBean.getProperty(PropertiesNames.GROUP_NAMES_URL/*"groupNamesURL"*/);
		}
		catch (NoSuchPropertyException e)
		{
			this.logger.debug(e.toString());
			urlStr = "http://www.netdimes.org/DIMES/statistics/group-names";
		}

		try
		{
			this.commHandler = new StandardCommunicator(urlStr, ConnectionHandlerFactory.NONSECURE_CONNECTION);
		}
		catch (MalformedURLException e1)
		{
			this.logger.debug(e1.toString());
		}
		this.fileHandler = new FileHandlerBean();
	}

	public GroupGetter(StandardCommunicator aCommunicator)
	{
		this.commHandler = aCommunicator;
		this.fileHandler = new FileHandlerBean();
	}

	public Vector getGroups()
	{
		if (this.commHandler == null)
		{
			this.logger.debug("communicator is null.");
			return new Vector();
		}

		try
		{
			String incoming = null;
//			File incoming = this.fileHandler.getIncomingFileSlot();
			incoming = this.commHandler.receiveDataToString();
//			return this.parseGroups(incoming);
			return this.parseGroups(incoming);
		}
		catch (Exception e)
		{
			this.logger.debug(e.toString());
		}

		return new Vector();
	}
	/*
	 * Change made: Using String for the input data instead of in file -
	 * to avoid the need of local I/O and creating unnecessary files.  
	 * 
	 * updater: idob
	 * since: ver. 0.5.0
	 **/
	private Vector parseGroups(/*File groupFile*/String strGroup)
	{
		Vector<String> groups = new Vector<String>();
//		StringReader reader = new StringReader(strGroup);
//		SAXReader saxReader = new SAXReader();
		
//		saxReader.setValidation(false);
		try
		{
//			Document doc = saxReader.read(reader);
			Element root = XMLUtil.getRootElement(strGroup);
			List groupElements = XMLUtil.getNodeListAsList(XMLUtil.getChildNodesNoWS(root));// doc.getRootElement().selectNodes("//group");
			
			
			Iterator iter = groupElements.iterator();
			String name;
			while (iter.hasNext())
			{
				Element groupElem = (Element) iter.next();
				name=groupElem.getAttribute("name");
//				String name = groupElem.attributeValue("name");
				groups.add(name);
			}
			Collections.sort(groups);
		}
		catch (Exception e)
		{
			this.logger.debug(e.toString());
		}
//		// handle after usage :
//		try
//		{
//			fileHandler.handleAfterUsage(groupFile, true);
//		}
//		catch (NoSuchPropertyException e1)
//		{
//			// abort...
//		}
		return groups;
	}
}