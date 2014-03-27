package dimes.util.logging;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogRecord;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import dimes.util.XMLUtil;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

//import org.dom4j.Document;
//import org.dom4j.DocumentException;
//import org.dom4j.DocumentHelper;
//import org.dom4j.Element;

public class ResultsFileHandler extends RotatingAnnouncingFileHandler {

	private static DocumentBuilderFactory dbf= DocumentBuilderFactory.newInstance();
	private static DocumentBuilder docBuilder; 
	
	public ResultsFileHandler(String aDir, String aSuffix) throws IOException, SecurityException {
		super(aDir, aSuffix);
	}
	
	/* (non-Javadoc)
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)\\
	 * here the logger detect what to publish.
	 */
	public synchronized void publish(LogRecord log)
	{
		if (!this.isLoggable(log))
			return;
		// Do not log Treeroute client messages into files (logged in the server):
		InputStream msg = new ByteArrayInputStream(log.getMessage().getBytes());
		Document logDoc = null;
		try
		{
			docBuilder = dbf.newDocumentBuilder();
			logDoc = docBuilder.parse(msg);
		}
		catch (IOException ex)
		{//todo
			System.out.println("IOException error parsing " + msg);
			ex.printStackTrace();
		} catch (ParserConfigurationException e) {
			System.out.println("ParserConfiguration error parsing " + msg);
			e.printStackTrace();
		} catch (SAXException e) {
			System.out.println("SAXException error parsing " + msg);
			e.printStackTrace();
		}
		Element logRoot = logDoc.getDocumentElement();
		String command = XMLUtil.getChildElementByName(logRoot, "CommandType").getTextContent();// logDoc.getElementById("CommandType").getTextContent();
		System.out.println("Command type : "+command);
		
		int logLength = this.getFormatter().format(log).getBytes().length;
		if ((this.currFile.length() + logLength) > this.byteLimit)
		{
			try {
				this.rotate();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
					e.printStackTrace();
			}
		}		
		super.publish(log);
	} 

}
