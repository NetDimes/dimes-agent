package agentGuiComm.comm;

import java.util.Hashtable;
import agentGuiComm.util.MessageTypes;

public class Sender {

	Client comm;
	int portNumber;
	final String defaultOperation = MessageTypes.names.get(MessageTypes.USER_OPERATION_TRACEROUTE);
	final String defaultProtocol = MessageTypes.names.get(MessageTypes.USER_PROTOCOL_ICMP);
	Hashtable<String, String> params = new Hashtable<String, String>();

	
	public void  setClient(Client c){
		comm=c;
		portNumber = c.getPort();
		params.put("PROTOCOL", defaultProtocol);
		params.put("OPERATION", defaultOperation);
	}
	
	public void sendMask(String resultsMask, String logMask){
		StringBuilder message = new StringBuilder("<SHOW>");
		message.append("<RESULTS>"+resultsMask+"</RESULTS>");
		message.append("<LOG>"+logMask+"</LOG>");
		message.append("</SHOW>");
		comm.setResMask(resultsMask);
		comm.setLogMask(logMask);
		System.out.println("Sending Mask Message:\n"+message.toString()+"\n");
		comm.sendRaw(message.toString());
	}

	private void sendScript(String ExID, String resource, String type, Hashtable<String, String> details){
		StringBuilder message = new StringBuilder("<SCRIPT ExId=\""+ExID+"\">");
		message.append("<RESOURCE>"+resource+"</RESOURCE>");
		message.append("<TYPE>"+type+"</TYPE>");
		for(String key:details.keySet()){
			message.append("<"+key+">"+details.get(key)+"</"+key+">");
		}
		message.append("</SCRIPT>");
		comm.sendRaw(message.toString());
	}
	
	public void sendFileLoadScript(String filename){
		sendScript("LOADFILE_"+portNumber, filename, "LOADFILE", params);
	}
	
	public void sendURLCrawelScript(String URL, int crawelLevel, int IPLimitLevel){
		params.put("CRWALERLEVEL", Integer.toString(crawelLevel));
		params.put("IPLIMITLEVEL", Integer.toString(IPLimitLevel));
		sendScript("CRAWLURL_"+portNumber, URL, "CRAWLURL", params);
	}
	
	public void sendLoadURLScript(String URL){
		sendScript("LOADURL_"+portNumber, URL, "LOADURL", params);
	}
	
	public void sendRawStringScript(String script){
		sendScript("RAWSCRIPT_"+portNumber, script, "RAWSCRIPT", params);
	}

	public void sendRaw(String text){
		comm.sendRaw(text);
	}
}
