/*
 * Created on 22/04/2004
 */
package dimes.AgentGuiComm.logging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;

//import org.dom4j.Document;
//import org.dom4j.DocumentException;
//import org.dom4j.DocumentHelper;
//import org.dom4j.Element;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import dimes.AgentGuiComm.AgentFrameFacade;
import dimes.AgentGuiComm.GUICommunicator;
import dimes.util.logging.Loggers;

/**
 * @author anat
 */
public class GraphHandler extends ConsoleHandler
{
	AgentFrameFacade frame = null;

	public GraphHandler(AgentFrameFacade aFrame)
	{
		frame = aFrame;
	}
	
	public void publish(LogRecord log){
		System.out.println("GraphHandler publish "+ log.getMessage());
//		GUICommunicator.sendGraph(log);
	}
//
//	public void publish(LogRecord log)
//	{
//		
//		if (!this.isLoggable(log))
//			return;
//		String msg = log.getMessage();
//		System.out.println("\n RUNNING GRAPH HANDLER \n");
//		Document logDoc = null;
////		try
////		{
//			try {
//				logDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(msg.getBytes()));
//			} catch (SAXException e1) {
//				// TODO Auto-generated catch block
//				Loggers.getLogger(this.getClass()).fine("error parsing " + msg);
//				e1.printStackTrace();
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				Loggers.getLogger(this.getClass()).fine("error parsing " + msg);
//				e1.printStackTrace();
//			} catch (ParserConfigurationException e1) {
//				// TODO Auto-generated catch block
//				Loggers.getLogger(this.getClass()).fine("error parsing " + msg);
//				e1.printStackTrace();
//			}// DocumentHelper.parseText(msg);
////		}
////		catch (DocumentException ex)
////		{//todo
////			/*System.err.println*/Loggers.getLogger(this.getClass()).fine("error parsing " + msg);
////			ex.printStackTrace();
////		}
//		Element logRoot = logDoc.getDocumentElement();//.getRootElement();
//		String command = logRoot.getElementsByTagName("CommandType").item(0).getTextContent(); //.elementTextTrim("CommandType");
//		System.out.println(" ********************* Logging command : " + command);
//		try
//		{
//		if (command.equalsIgnoreCase("TRACEROUTE"))
//			this.logTraceroute(logRoot);
////		if (command.equalsIgnoreCase("TREEROUTE"))
////			this.logTreeroute(logRoot);
//		if (command.equalsIgnoreCase("PING"))
//			this.logPing(logRoot);
//		
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//
//	}
//
//	public byte[] getIntToByteArray(int v)
//	{
//		byte b[] = new byte[4];
//		int i, shift;
//
//		for (i = 0, shift = 24; i < 4; i++, shift -= 8)
//			b[i] = (byte) (0xFF & (v >> shift));
//
//		return b;
//	}
//	
//	private Vector getRawIPsVector(Element logRoot) throws Exception{
//		Vector IPs = new Vector();// will contain IP addresses
//		Element detailsRoot = logRoot.element("RawDetails");
//		List details = detailsRoot.elements();
//		for (int index = 0; index < details.size(); ++index) {
//			String addr = ((Element) details.get(index)).elementText("hopAddress");
//			String anIP = ((Element) details.get(index)).elementText("hopAddressStr");
//			// doesn't draw unknown nodes
//			if ((anIP.equals("null")) || (anIP.equals("127.0.0.1")) || (addr.trim().equalsIgnoreCase("0")))
//				continue;
//			String name = ((Element) details.get(index)).elementText("hopNameStr");
//			InetAddress address = InetAddress.getByName(name);
//			IPs.add(address);
//		}
//		IPs.trimToSize();
//		return IPs;
//	}
//	
//	private void logTraceroute(Element logRoot) throws Exception 
//	{
//		System.out.println(" ********************* Logging traceroute : ");
//		Vector IPs = getRawIPsVector(logRoot);
//		String sourceIP = logRoot.elementText("SourceIP");
//		// add the trace with the specific script :
//		this.frame.addTrace(new Vector[]{IPs}, new String[]{sourceIP} , 
//				logRoot.elementText("ExID"), 
//				logRoot.elementText("ScriptID"), logRoot.elementText("Protocol"), 
//				logRoot.elementText("Priority"));
//	}
//	
///*	private void logTreeroute(Element logRoot){
//		System.out.println(" ********************* Logging treeroute");
//		String treerouteRole = logRoot.elementText("Role");
//
//		try 
//		{
//			Element clientServerTrace = logRoot.element("clientServerTrace").element("OperationResult");
//			Element clientDestTrace = logRoot.element("clientDestTrace").element("OperationResult");
//			String clientSourceIP = clientServerTrace.elementText("SourceIP");
//			Vector clientDestIPs = this.getRawIPsVector(clientDestTrace);
//			Vector clientServerIPs = this.getRawIPsVector(clientServerTrace);
//			Vector treerouteVector = clientServerIPs;
//			Vector[] traceroutes = new Vector[]{clientDestIPs, clientServerIPs};
//			String[] sourceIPs = new String[]{clientSourceIP , clientSourceIP };
//			
//		
//			if (treerouteRole.equalsIgnoreCase("SERVER"))	
//			{
//				Element serverClientTrace = logRoot.element("serverClientTrace").element("OperationResult");
//				Element serverDestTrace = logRoot.element("serverDestTrace").element("OperationResult");
//				Vector serverDestIPs = this.getRawIPsVector(serverDestTrace);
//				Vector serverClientIPs = this.getRawIPsVector(serverClientTrace);
//				treerouteVector = this.getRawIPsVector(logRoot);
//				String serverSourceIP = serverClientTrace.elementText("SourceIP");
//				traceroutes = new Vector[]{clientDestIPs, clientServerIPs,serverDestIPs , serverClientIPs };
//				sourceIPs = new String[]{clientSourceIP , clientSourceIP ,serverSourceIP,serverSourceIP};
//			}
//			this.frame.addTrace(traceroutes, sourceIPs , logRoot.elementText("ExID"), logRoot.elementText("ScriptID"), logRoot.elementText("Protocol"), logRoot
//					.elementText("Priority"));
//			System.out.println("Treeroute vector :" + treerouteVector);
//			this.frame.addTree(new Vector[]{treerouteVector} , new String[]{clientSourceIP} , 
//					logRoot.elementText("ExID"), logRoot.elementText("ScriptID"), 
//					logRoot.elementText("Protocol"), logRoot.elementText("Priority"));
//
//			
//		
//		}
//		catch (Exception e) 
//		{
//			System.err.println("Error while Parsing client results : " + e.getMessage());
//			e.printStackTrace();
//		}
//	}
//	*/
	private void logPing(Element logRoot){
		
	}

}