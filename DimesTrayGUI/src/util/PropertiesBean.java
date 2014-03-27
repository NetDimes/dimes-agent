package util;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Element;

public abstract class PropertiesBean {

	public static final String PORT = "localport";
	public static final String ICON_PATH = "iconpath";
	public static final String ICON_RED = "iconred";
	public static final String ICON_YELLOW = "iconyellow";
	public static final String ICON_GREEN = "icongreen";
	public static final String ABOUT_FILENAME = "aboutFile";
	private static Element rootElm;
	private static File propertiesFile;
	
	public static boolean init(String propertiesFileString){
		try {
			propertiesFile = new File(propertiesFileString);
			rootElm = XMLUtil.getRootElement(propertiesFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static String getResourcePath(){
		return propertiesFile.getParent();
	}
	
	public static String getProperty(String prop){
		return XMLUtil.getChildElementByName(rootElm, prop).getTextContent();
	}
}
