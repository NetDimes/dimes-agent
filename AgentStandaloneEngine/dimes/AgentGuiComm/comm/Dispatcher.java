package dimes.AgentGuiComm.comm;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dimes.AgentGuiComm.AgentFrameFacade;
import dimes.AgentGuiComm.PropertiesFrameFacade;
import dimes.AgentGuiComm.util.MessageTypes;
import dimes.util.XMLUtil;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**A central point for dealing with incoming requests
 * from clients. Maintains references to appropriate
 * facades (AgentFrameFace, UserScriptFacade, etc) and
 * invokes the appropriate commands.
 * 
 * @author Boazh
 * 
 *
 */
public class Dispatcher {

	private static Dispatcher me=null;
	private AgentFrameFacade agent=null;
//	private PropertiesFrameFacade propertiesFrame=null;
	
	public static Dispatcher getInstance(AgentFrameFacade agentFrameFacade, PropertiesFrameFacade propertiesFrameFacade){
		if(null==me)
			me=new Dispatcher(agentFrameFacade,propertiesFrameFacade);
		else
			{
			me.agent=agentFrameFacade;
//			me.propertiesFrame=propertiesFrameFacade;
			}
		return me;
	}
	
	private Dispatcher(AgentFrameFacade agentFrameFacade, PropertiesFrameFacade propertiesFrameFacade){
		agent=agentFrameFacade;
//		propertiesFrame=propertiesFrameFacade;
	}
	
	/**resolves a message from a client and dispatches the command to the appropriate handler
	 * @param type the main tag from the xml (eg "SCRIPT")
	 * @param messagXML the main tag and everything in it, in XML
	 * @param source
	 */
	public void dispatch(String type, String messagXML, int source){
		ClientCommunicationsObject client = ClientsBean.findClient(source);
		if (type.equals(MessageTypes.SEND_TYPE_SCRIPT)) dispatchScript(messagXML, client, source);
		if (type.equals(MessageTypes.SEND_TYPE_SHOW)) dispatchShow(messagXML, client);
		if (type.equals(MessageTypes.SEND_TYPE_DIMES)) dispatchCommand(messagXML, client);
		if (type.equals(MessageTypes.SEND_TYPE_PROPERTY)) dispatchProperty(messagXML, client);
	}
	
	/**Dispatch a show command (sets the show mask)
	 * @param message content in XML. Expecting: <SHOW><RESULTS>ALL</RESULTS><LOG>info</LOG></SHOW>
	 * @param source port of client making request
	 */
	private void dispatchShow(String message, ClientCommunicationsObject client){
		int result = -1;
		int log=-1;
		Hashtable<String, Integer> values = getSubsAsTable(message);
		if(values.containsKey(MessageTypes.SEND_TYPE_RESULTS)) result=values.get(MessageTypes.SEND_TYPE_RESULTS);
		if(values.containsKey(MessageTypes.SEND_MESSAGE_LOG)) log=values.get(MessageTypes.SEND_TYPE_LOG);
		client.setMask(log, result);
	}
	
	private void dispatchScript(String message, ClientCommunicationsObject client, int source){
		Element scriptELm = XMLUtil.getRootElement(message);
		String expID="Default"+source;
		try{
			String id = scriptELm.getAttribute("EXID");
			expID=source+id; //adding the source port ensures that you can't have a script with the same name as the default scripts
			}catch(Exception e){
				System.err.println("No ExID specified. Using:"+expID);
			}
	//	client.listenForScript(expID);
		agent.dispatchUserScript(scriptELm, expID, source);
	}
	
	private void dispatchCommand(String message, ClientCommunicationsObject client){
		Element DimesElm = XMLUtil.getRootElement(message);
		if(!(null==XMLUtil.getChildElementByName(DimesElm, MessageTypes.SUB_GOODBYE)))
			try {
				client.hangup();
			} catch (IOException e) {
				e.printStackTrace();
			}
			else {
				Element pauseElm;
				if(!(null==(pauseElm=XMLUtil.getChildElementByName(DimesElm, MessageTypes.SUB_PAUSE)))){
					boolean setPause = Boolean.parseBoolean(pauseElm.getTextContent());
					client.setActive(setPause);
				}
			}
	}
	
	private void dispatchProperty(String message, ClientCommunicationsObject client){
		List<Node> subs = XMLUtil.getNodeListAsList(XMLUtil.getChildNodesNoWS(XMLUtil.getRootElement(message)));
		for(Node n:subs){
			if(n.getNodeName().equals("GET")){
				String propName = n.getTextContent();
				String propValue;
				String returnmessage=null;
				try {
					propValue=PropertiesBean.getProperty(propName);
					returnmessage="<"+MessageTypes.SEND_TYPE_PROPERTY+" name=\""+propName+"\">"+"<"+MessageTypes.SUB_VALUE+">"+propValue+"</"+MessageTypes.SUB_VALUE+"></"+MessageTypes.SEND_TYPE_PROPERTY+">";
					client.send(MessageTypes.SEND_TYPE_PROPERTY, null, returnmessage);
				} catch (NoSuchPropertyException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(n.getNodeName().equals("SET")){
				String propName = ((Element)n).getAttribute("name");
				String propValue= n.getTextContent();
				String returnmessage=null;
				try {
					PropertiesBean.setProperty(propName, propValue);
					returnmessage="<"+MessageTypes.SEND_TYPE_PROPERTY+" name=\""+propName+"\">"+"<"+MessageTypes.SUB_VALUE+">"+propValue+"</"+MessageTypes.SUB_VALUE+"></"+MessageTypes.SEND_TYPE_PROPERTY+">";
					client.send(MessageTypes.SEND_TYPE_PROPERTY, null, returnmessage);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private Hashtable<String, Integer> getSubsAsTable(String XMLMessage){
		Hashtable<String, Integer> subsTable= new Hashtable<String, Integer>();
		List<Node> subs = XMLUtil.getNodeListAsList(XMLUtil.getChildNodesNoWS(XMLUtil.getRootElement(XMLMessage)));
		for(Node n:subs){
			String s = n.getNodeName();
			if(s.equals("LOG")) subsTable.put(s, Level.parse(n.getTextContent()).intValue());
			else subsTable.put(n.getNodeName(), MessageTypes.lookup.get(n.getTextContent()));
		}
		return subsTable;
	}
}
