/*
 * Created on 10/02/2004
 */
package dimes.util.properties;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.swing.JOptionPane;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import dimes.util.logging.Loggers;

//fixme - change back to load file from dir, not from jar

/**
 * @author anat
 */
public abstract class PropertiesBeanOld implements Serializable
{
	private static String propertiesFileName = null;
	private static Document propertiesDoc = null;
	private static Element propertiesRoot = null;

	private static final String propertiesHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<!-- conf.xml    -->\n"
			+ "<!-- conf file for DOMtry.java -->\n\n" + "<!DOCTYPE agentConf [\n" + "<!ENTITY NOP \"0\">\n" + "<!ENTITY DELETE \"1\">\n"
			+ "<!ENTITY MOVE \"2\">\n" + "<!ENTITY RENAME \"3\">\n" + "<!ENTITY MOVE_RENAME \"4\">\n" + "]>\n";

	//should be activated by Agent()
	public static void init(String propFileName)
	{
		System.out.println("PropertiesBean.init");//debug
		if( propFileName.contains(".") || propFileName.contains("..") ) {
			File propFile = new File(propFileName);
			try {
				propFileName = propFile.getCanonicalPath();
				} catch (IOException e) {
				// TODO Auto-generated catch block
                            JOptionPane.showMessageDialog(null, " Agent cannot find properties.xml(Invalid Path.), program will exit",  
            			 "Error", 0);  
				e.printStackTrace();
                                System.exit(1); 
			}
			System.out.println("propFileName = " + propFileName);
		}
		PropertiesBean.propertiesFileName = propFileName;
		System.setProperty(PropertiesNames.PROP_FILENAME_PROPERTY, propFileName);
		try
		{
			PropertiesBean.loadProperties();
			String oldPropFileName = propFileName + ".old";//the installer renames the previous file to *.old
			File oldPropFile = new File(oldPropFileName);
			if (oldPropFile.exists())
			{
				PropertiesBean.mergeProperties(oldPropFile);
				oldPropFile.delete();
			}

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			Loggers.getLogger().warning(e.toString());
		}
		try{
			if (PropertiesBean.getProperty("base").equals("*agentClassBase*")){
				String basePath = PropertiesBean.propertiesFileName.substring(0, (PropertiesBean.propertiesFileName.length()-19));
				PropertiesBean.setProperty("base", basePath);
				PropertiesBean.setProperty("countryFile", basePath+"conf/countries-xml.txt");
				PropertiesBean.setProperty("jarDir", (basePath.substring(0, basePath.length()-5))+"JARs");
				PropertiesBean.saveProperties();
			}
		}catch (NoSuchPropertyException nspe){
			nspe.printStackTrace();
		}catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

	public static String getProperiesFileName()
	{
		return propertiesFileName;
	}

	public static synchronized void loadProperties() throws DocumentException
	{
		SAXReader reader = new SAXReader();
		//***********************************************************
		// The next line was changed to use a File object. Reason:
		//reader.read kept looking for a url called "C" (from c:\)
		// BoazH, 11/12/2008
		//***********************************************************
		try{
		PropertiesBean.propertiesDoc = reader.read(new File(PropertiesBean.propertiesFileName));
		PropertiesBean.propertiesRoot = PropertiesBean.propertiesDoc.getRootElement();
		}catch(IOException ioe){
			System.out.println("Problem loading properties.xml, agent will exit");
			ioe.printStackTrace();
			System.exit(-1);
		}
	}

	/*
	 * reads property values from aPropFile and merges them with those in the default
	 * property file. only some values will be kept from the old files - according to isConsistent
	 * 
	 */
	public static synchronized void mergeProperties(File oldPropFile) throws DocumentException, IOException
	{
		System.out.println("PropertiesBean - mergeProperties");//debug
		SAXReader reader = new SAXReader();
		Document oldDoc = reader.read(oldPropFile);
		Element oldRoot = oldDoc.getRootElement();

		//assuming new properties file has already been loaded
		PropertiesBean.mergeProperties(PropertiesBean.propertiesRoot, oldRoot);
		PropertiesBean.manualMerge(oldRoot);
	}

	/**
	 * used to merge version-specific property differences.
	 * for example, the 'name' property from v0.3 will be updated in the v0.4 properties:
	 * 'userName' and 'agentName'.
	 * @param newParent
	 * @param oldRoot
	 */
	private static synchronized void manualMerge(Element oldRoot)
	{
		System.out.println("PropertiesBean - manualMerge");//debug
		String name;
		String id;
		String agentClassBase;
		String jarBase;

		String newCountryFile;
		String newLogFile;
		try
		{
			//            name = PropertiesBean.getProperty("name", oldRoot);
			id = PropertiesBean.getProperty("id", oldRoot);
			jarBase = PropertiesBean.getProperty("init.jarDir", oldRoot);
			agentClassBase = PropertiesBean.getProperty("init.dirs.base", oldRoot);

			newCountryFile = PropertiesBean.getProperty(PropertiesNames.COUNTRY_FILE);
			newLogFile = PropertiesBean.getProperty(PropertiesNames.LOG_CONFIG_FILE);

		}
		catch (NoSuchPropertyException e1)
		{
			Loggers.getLogger(PropertiesBean.class).finer(e1.toString());
			return;//if 'id' property doesn't exist, this is a newer version than 0.3 - has the correct username/agentname properties
		}
		try
		{
			Loggers.getLogger().info("Added id Property with value:" + id);
			PropertiesBean.setProperty(PropertiesNames.AGENT_ID, id);

			//          // manually replace *agentClassBase* with the Agent/Base 
			//          // and *launcherClassBase* with Agent/JARs :
			PropertiesBean.setProperty(PropertiesNames.COUNTRY_FILE, agentClassBase + "\\conf\\countries-xml.txt");
			PropertiesBean.setProperty(PropertiesNames.LOG_CONFIG_FILE, agentClassBase + "\\conf\\logging.properties");
			PropertiesBean.setProperty(PropertiesNames.JAR_DIR, jarBase);

			//            PropertiesBean.setProperty(PropertiesNames.USER_NAME, name);
			//            PropertiesBean.setProperty(PropertiesNames.AGENT_NAME, name);
		}
		catch (IOException e)
		{
			Loggers.getLogger(PropertiesBean.class).warning(e.toString());
		}

	}

	public static synchronized void mergeProperties(Element newParent, Element oldRoot) throws IOException
	{
		/*
		 * todo - change so that property name will be complete. when recoursing, 
		 * send name of parent. 
		 */
		System.out.println("PropertiesBean - merge");//debug
		List elements = newParent.elements();
		for (int i = 0; i < elements.size(); ++i)
		{
			Element aNewChild = (Element) elements.get(i);
			if (!aNewChild.isTextOnly())
				PropertiesBean.mergeProperties(aNewChild, oldRoot);
			else
			{
				String property = aNewChild.getName();
				String newValue = aNewChild.getText();//PropertiesBean.getProperty(property, newParent);
				String oldValue;
				try
				{
					oldValue = PropertiesBean.getRawProperty(property, oldRoot);
				}
				catch (NoSuchPropertyException e)
				{
					oldValue = null; //in case this property didn't exist in previous property file
				}
				if (PropertiesBean.isConsistent(oldValue, newValue))
				{
					PropertiesBean.setProperty(property, oldValue);//adds to new properties file - using propertiesRoot
					System.out.println("set property " + property + " to " + oldValue);//debug
				}

			}
		}

	}

	/**
	 * sets all properties under newRoot in current property tree
	 * @param newRoot
	 * @throws IOException
	 */
	public static synchronized void setAllProperties(Element newRoot) throws IOException
	{
		/*
		 * todo - change so that property name will be complete. when recoursing, 
		 * send name of parent. 
		 */
		List elements = newRoot.elements();
		for (int i = 0; i < elements.size(); ++i)
		{
			Element aNewChild = (Element) elements.get(i);
			if (!aNewChild.isTextOnly())
				PropertiesBean.setAllProperties(aNewChild);
			else
			{
				String property = aNewChild.getName();
				String newValue = aNewChild.getText();
				PropertiesBean.setProperty(property, newValue);
			}
		}
	}

	/*
	 * returns true if oldValue should override newValue.
	 * currently, any property that wasn't initialized in the new properties file is delimited
	 * by *. Therefore, any property that was initialized in the old file and wasn't in the new,
	 * its value is taken from the old file.
	 */
	private static boolean isConsistent(String oldValue, String newValue)
	{
		if (oldValue == null)
			return false; //property didn't exist in old file - don't override in new file
		return !PropertiesBean.isValidValue(newValue);
	}

	public static synchronized void saveProperties() throws IOException
	{
		FileWriter writer = new FileWriter(PropertiesBean.propertiesFileName);
		writer.write(PropertiesBean.propertiesHeader + "\n\n");
		PropertiesBean.propertiesRoot.write(writer);
		writer.close();
	}

	public static synchronized void setProperty(String name, String value) throws IOException/*, NoSuchPropertyException*/
	{
		Element property;
		try
		{
			property = PropertiesBean.getElement(name);
		}
		catch (NoSuchPropertyException e)
		{
			property = PropertiesBean.addProperty(name);
		}//(Element)(PropertiesBean.propertiesRoot.selectSingleNode("//"+name));
		if (property == null)
			property = PropertiesBean.addProperty(name);
		property.setText(value);
		PropertiesBean.saveProperties();
	}

	public static synchronized String getProperty(String name) throws NoSuchPropertyException
	{

		return PropertiesBean.getProperty(name, PropertiesBean.propertiesRoot);
	}

	public static synchronized String getProperty(String name, Element aRoot) throws NoSuchPropertyException
	{
		String res = "";
		Element property = PropertiesBean.getElement(name, aRoot);
		if (property == null)
			throw new PropertiesBean.NoSuchPropertyException(name);

		// To read agent statistic server address (without "base" address)
		//if(name.equals(PropertiesNames.AGENT_STATISTIC))			
			//;
		//else 
		if (property.getName().compareTo("base") != 0)//not <base> property		
			if ((property.getParent().getName().compareTo("dirs") == 0) || (property.getParent().getName().compareTo("files") == 0))//is a descendant of dirs or files
				res = PropertiesBean.getElement("base", aRoot).getTextTrim() + File.separator;
		
		res += property.getTextTrim();
		
		return res;
	}

	public static synchronized String getPropertyTree(String name) throws NoSuchPropertyException
	{
		String res = "";
		Element property = PropertiesBean.getElement(name, PropertiesBean.propertiesRoot);
		if (property == null)
			throw new PropertiesBean.NoSuchPropertyException(name);
		res += property.asXML();
		return res;

	}

	// returns property text - ignores "special" properties
	private static synchronized String getRawProperty(String name, Element aRoot) throws NoSuchPropertyException
	{
		Element property = PropertiesBean.getElement(name, aRoot);
		if (property == null)
			throw new PropertiesBean.NoSuchPropertyException(name);
		String res = property.getTextTrim();
		return res;
	}

	/* add the property denoted by the doc path.
	 * pathname - a dot-separated string where each part signifies a subsection.
	 * for example: init.policies.after_usage.results denotes the property
	 * <results> in the subsection that has the following hierarchy:
	 * init -> policies -> after_usage
	 * This path can also be partial (doesn't have to start from the root
	 * element), but if it results in more than one Element (conflict),
	 * it automatically picks the first match.*/
	public static synchronized Element addProperty(String name)
	{//todo - should deal with adding a complex property name - policies.gui.showSplash for example
		Element property = (Element) (PropertiesBean.propertiesRoot.selectSingleNode(PropertiesBean.getXpath(name)));
		if (property != null)
			return null; //property already exists
		property = PropertiesBean.propertiesRoot.addElement(name.substring(name.lastIndexOf(".") + 1));
		return property;
	}

	private static synchronized String getXpath(String dottedPath)
	{
		String Xpath = dottedPath.replace('.', '/');
		return "//" + Xpath;
	}

	/*
	 * to be used only for setting properties in the default properties file
	 */
	private static Element getElement(String dottedPath) throws NoSuchPropertyException
	{
		//		String Xpath = PropertiesBean.getXpath(dottedPath);
		//		List elements = PropertiesBean.propertiesRoot.selectNodes(Xpath);
		//		if (elements.size() == 0)
		//			throw new NoSuchPropertyException(dottedPath);
		//		if (elements.size() > 1)
		//			Loggers.getLogger().warning("Property name conflict for property: "+dottedPath);
		//		return (Element)elements.get(0);
		return PropertiesBean.getElement(dottedPath, PropertiesBean.propertiesRoot);
	}

	private static Element getElement(String dottedPath, Element aRoot) throws NoSuchPropertyException
	{
		String Xpath = PropertiesBean.getXpath(dottedPath);
		List elements = aRoot.selectNodes(Xpath);
		if (elements.size() == 0)
			throw new NoSuchPropertyException(dottedPath);
		if (elements.size() > 1)
		{
			Loggers.getLogger().fine("Property name conflict for property: " + dottedPath);
			Throwable thrw = new Throwable();//debug
			StackTraceElement[] stackElements = thrw.getStackTrace();//debug
			String msg = "";
			for (int i = 0; i < stackElements.length; ++i)//debug
			{
				StackTraceElement traceElement = stackElements[i];
				msg += traceElement.toString() + " - line " + traceElement.getLineNumber() + "\n";
			}
			Loggers.getLogger(PropertiesBean.class).fine(msg);
		}
		return (Element) elements.get(0);

	}

	public static synchronized boolean hasValidValue(String property) throws NoSuchPropertyException
	{
		String value = PropertiesBean.getProperty(property);
		return PropertiesBean.isValidValue(value);
	}

	public static boolean isValidValue(String value)
	{
		if (value.trim().startsWith("*") && value.trim().endsWith("*"))
			return false;
		return true;
	}

	public static class NoSuchPropertyException extends Exception
	{
		private final String property;
		public NoSuchPropertyException(String aProperty)
		{
			property = aProperty;
		}

		/* (non-Javadoc)
		 * @see java.lang.Throwable#getMessage()
		 */
        @Override
		public String getMessage()
		{
			// TODO Auto-generated method stub
			return "No such property: " + this.property;
		}

	}

}