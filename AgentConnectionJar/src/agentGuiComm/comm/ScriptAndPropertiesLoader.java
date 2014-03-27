package agentGuiComm.comm;

import org.w3c.dom.Element;

import agentGuiComm.util.XMLUtil;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

public abstract class ScriptAndPropertiesLoader {

	private static int port=33333;//default
	private static int timeToDie=0;//default, live forever
	private static int timeToScript=0;//default, no script
	private static boolean showRawXML=true;
	private static String logMask = Level.INFO.toString();//MessageTypes.LOGGER_LEVEL_INFO;
	private static String resultsMask = "ALL";
	
	public static boolean loadProperties(File propFile){
		try {
			Element rootElm = XMLUtil.getRootElement(propFile);
			port = Integer.parseInt(XMLUtil.getChildElementByName(rootElm, "Port").getTextContent());
			timeToDie=Integer.parseInt(XMLUtil.getChildElementByName(rootElm, "TimeToDie").getTextContent());
			timeToScript = Integer.parseInt(XMLUtil.getChildElementByName(rootElm, "TimeToScript").getTextContent());
			logMask = (XMLUtil.getChildElementByName(rootElm, "LogMask").getTextContent());
			resultsMask = XMLUtil.getChildElementByName(rootElm, "ResultsMask").getTextContent();
			showRawXML = Boolean.parseBoolean(XMLUtil.getChildElementByName(rootElm, "PrintRawXML").getTextContent());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	

	public static String loadRawScriptFile(File inputFile){
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;
		try{
            reader = new BufferedReader(new FileReader(inputFile));
            String text = null;

            // repeat until all lines is read
            while ((text = reader.readLine()) != null) {
                contents.append(text);
            }
	
		}catch(IOException ieo){
			ieo.printStackTrace();
		}finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		return contents.toString();
	}

	public static int getPort() {
		return port;
	}

	public static int getTimeToDie() {
		return timeToDie;
	}

	public static int getTimeToScript() {
		return timeToScript;
	}

	public static boolean isShowRawXML() {
		return showRawXML;
	}

	public static String getLogMask() {
		return logMask;
	}

	public static String getResultsMask() {
		return resultsMask;
	}
}
