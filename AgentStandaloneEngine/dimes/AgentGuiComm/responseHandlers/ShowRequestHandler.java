package dimes.AgentGuiComm.responseHandlers;

import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dimes.AgentGuiComm.util.MessageTypes;
import dimes.util.XMLUtil;

public class ShowRequestHandler {

	private static ShowRequestHandler me=null;
	
	private ShowRequestHandler(){}
	
	public static ShowRequestHandler getInstance(){
		if (null==me) me=new ShowRequestHandler();
		return me;
		
	}
	
	public String processRequest(Element req, int ID){
		LinkedList<Node> requests = (LinkedList) XMLUtil.getNodeListAsList(XMLUtil.getChildNodesNoWS(req));
		StringBuilder answer = new StringBuilder("<"+MessageTypes.ACK_TYPE_SHOW+">\n");
		for (Node n:requests){
			Element current = (Element)n;
			String type = current.getTagName();
			if(MessageTypes.GET_SHOW_LOG.equals(type)){}
			else if (MessageTypes.GET_SHOW_RESULTS.equals(type)){}
			else if (MessageTypes.GET_SHOW_SCRIPTS.equals(type)){}
			else return "";
		}
		return null;
	}
}
