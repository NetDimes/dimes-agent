package dimes.AgentGuiComm;

import java.util.logging.Level;

import org.w3c.dom.Element;

import dimes.AgentGuiComm.responseHandlers.PropertyRequestHandler;
import dimes.AgentGuiComm.util.MessageTypes;
//import dimes.AgentGuiComm.util.Message;

public class GUICommunicator {

	private static GUICommunicator me=null;
	private static GUIConnectorBean bean=null;
	private AgentFrameFacade agentFacade;
	private int nextID=1001;
	
	public static GUICommunicator getInstance(){
		if (null==me) me=new GUICommunicator();
		return me;
	}
	
	private GUICommunicator(){
		if (null==bean) bean=GUIConnectorBean.getInstance();		
	}
	
/*	public void send(Message msg){
		
	}
	*/
	public static void sendLog(Level severity, String messageInfo, String message){
		String type=MessageTypes.SEND_MESSAGE_LOG;
		int subtype=MessageTypes.MESSAGE_SUBTYPE_LOG;
		
		if(severity ==Level.WARNING || severity ==Level.SEVERE){
			type=MessageTypes.SEND_MESSAGE_ERROR;
			subtype=MessageTypes.MESSAGE_SUBTYPE_ERROR;
		}
		
		if(messageInfo.equals("USER_LOG"))
			subtype=MessageTypes.MESSAGE_SUBTYPE_USERSCRIPT;
	//	bean.send(new Message(MessageTypes.LOG, severity.intValue(), messageInfo, message, null)); //Important: in a log message, the ID serves as severity indicatorqa@
		StringBuilder messageString = new StringBuilder("<"+type+">\n");
		messageString.append("<SEVERITY level=\""+severity+"\" />");
		messageString.append("<MESSAGE_INFO String=\""+messageInfo+"\" />");
		messageString.append("<MESSAGE String=\""+message+"\" />");
		messageString.append("</"+type+">");
		bean.send(makeMessage(messageString.toString()), MessageTypes.TYPE_MESSAGE, subtype);
	}
	

	public static void sendResult(String Result, int source){
		bean.send(wrapMessage(MessageTypes.SEND_TYPE_RESULTS, Result), MessageTypes.TYPE_RESULTS, source);
	}
	
	private static String wrapMessage(String wrapper, String message){
		return "<"+wrapper+">\n"+message+"\n</"+wrapper+">";
	}
	
	public static void sendException(String message){
	
		//TODO: GET THE RESULT SOURCE (USER/SERVER)
	
		bean.send(makeMessage("<EXCEPTION string=\""+message+"\" />\n"), MessageTypes.TYPE_MESSAGE, MessageTypes.MESSAGE_SUBTYPE_EXCEPTION);
	}
	
	public static void sendProperty(String prop){
		bean.send(wrapMessage(MessageTypes.SEND_TYPE_PROPERTY, prop), MessageTypes.TYPE_PROPERTY, MessageTypes.MASK_SERVER);
	}
//	
//	public static void sendUserScriptLog(Level severity, String message){
//		bean.send(new Message(MessageTypes.SCRIPT, severity.intValue(), "", message, null)); //Important: in a log message, the ID serves as severity indicatorqa@
//
//	}
//	
//	public static void sendQueryReply(int id, Object obj){
//		bean.send(new Message(MessageTypes.QUERY_REPLY, id, "", "",new Object[]{obj}));
//	} 
	
//	public void dispatch(Message msg){
//		
//	int type = msg.getType();
//		
//		switch (type){
//		
//			case MessageTypes.COMMAND: 
//					switch (msg.getID()){
//						case MessageTypes.COMMAND_DEST_AGENT:
//							agentFacade.dispatchCommand(msg);
//						case MessageTypes.COMMAND_DEST_PROPERTIES:
//						case MessageTypes.COMMAND_DEST_REGISTRATION:
//						default: break;
//					}
//				break;
////			case MessageTypes.QUERY: 
////				Runnable queryRunner = new QueryDispatch(msg);
////				queryRunner.run();
////				break;
//			case MessageTypes.SCRIPT: 	
//				AgentFrameFacade.dispatchUserScript(msg);
//				break;
//		}
//		
//	}

	public void setAgentFacade(AgentFrameFacade agent) {
		this.agentFacade=agent;
		
	}

	
	public void stopCommunicator() {
		// TODO Auto-generated method stub
		
	}

	public void dispatch(Element current) {
		int type = MessageTypes.lookup.get(current.getTagName().toUpperCase());
		switch(type){
			case MessageTypes.RECEIVE_COMMAND:
			case MessageTypes.RECEIVE_PROPERTY:				
				sendProperty(PropertyRequestHandler.getInstance().processRequest(current));
				break;
			case MessageTypes.RECEIVE_SCRIPT:
//				AgentFrameFacade.dispatchUserScript(current);
				break;
			case MessageTypes.RECEIVE_SHOW:
			default:
				break;
		}
		
	}
	
	public static String makeMessage(String msg){
		return "<"+MessageTypes.SEND_TYPE_LOG+">\n"+msg+"</"+MessageTypes.SEND_TYPE_LOG+">\n";
	}

}
