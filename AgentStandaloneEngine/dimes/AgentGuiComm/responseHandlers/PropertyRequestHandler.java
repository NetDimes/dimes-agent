package dimes.AgentGuiComm.responseHandlers;

import java.io.IOException;
import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dimes.AgentGuiComm.util.MessageTypes;
import dimes.util.XMLUtil;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

public class PropertyRequestHandler{

	private static PropertyRequestHandler me=null;
	
	public static PropertyRequestHandler getInstance(){
		if (null==me) me = new PropertyRequestHandler();
		return me;
	}
	
	private PropertyRequestHandler(){}
	
	private String getProperty(String propName){
		try {
			return PropertiesBean.getProperty(propName);
		} catch (NoSuchPropertyException e) {
			
			e.printStackTrace();
		}
		return null;
	}
	
	private void setProperty(String propName, String propValue){
		try {
			PropertiesBean.setProperty(propName, propValue);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String processRequest(Element req){
		LinkedList<Node> requests = (LinkedList) XMLUtil.getNodeListAsList(XMLUtil.getChildNodesNoWS(req));
		StringBuilder answer = new StringBuilder("<"+MessageTypes.SEND_TYPE_PROPERTY+">\n");
		for (Node n:requests){
			Element current = (Element)n;
			String type = current.getTagName();
			if(MessageTypes.GET_PROPERTY_GET.equals(type)){
				answer.append("<"+MessageTypes.GET_PROPERTY_GET+" name=\""+current.getAttribute("name")+"\" value=\""+getProperty(current.getAttribute("name"))+"\" />\n");
			}
			else if(MessageTypes.GET_PROPERTY_SET.equals(type)){}
			else{}
		}
		return answer.toString();
	}
}
