package dimes.AgentGuiComm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dimes.util.XMLUtil;

public class ExternalClient {

	/*
    *  Type of results the GUI wants to get (none/all/mine/server)
    * Type of scripts the GUI wants (none/name only/all content) where the Agent reports the content of its current scripts.
    * Type of messages the GUI wants to see (none/some/all)
    * Messages subtypes wanted (log/state/error/exceptions) 
	*/
	private int ID;
	private int resultMask=MessageTypes.MASK_NONE;
	private int scriptsMask=MessageTypes.MASK_NONE;
	private int messageMask=MessageTypes.MASK_NONE;
	private int messageSubtypeMask=1111;
//	private LinkedList<Integer> messageSubTypesMask=new LinkedList<Integer>();
	private Hashtable<Integer,Boolean> messageSubTypesMask = new Hashtable<Integer,Boolean>(4);
	private static Hashtable<String, Integer> messageSubtypes = new Hashtable<String, Integer>();
	private PrintStream outStream;
	private BufferedReader inStream;
	private Socket socket;
	static{
		messageSubtypes.put(MessageTypes.SEND_MESSAGE_LOG, MessageTypes.MASK_NONE);
		messageSubtypes.put(MessageTypes.SEND_MESSAGE_EXCEPTION, MessageTypes.MASK_NONE);
		messageSubtypes.put(MessageTypes.SEND_MESSAGE_ERROR, MessageTypes.MASK_NONE);
		messageSubtypes.put(MessageTypes.SEND_MESSAGE_UPDATE, MessageTypes.MASK_NONE);
	}
	
/*	public ExternalClient(int id, PrintStream ps, int port){
		ID=id;
		outStream=ps;
	}*/
	
	public ExternalClient(/*int id,*/ PrintStream ps, int port, int resultMask, int scriptMask, int messageMask){
//		this(id,ps, port);
		this.resultMask=resultMask;
		this.scriptsMask=scriptMask;
		this.messageMask=messageMask;
		this.messageSubTypesMask.put(MessageTypes.MESSAGE_SUBTYPE_LOG,false);
		this.messageSubTypesMask.put(MessageTypes.MESSAGE_SUBTYPE_EXCEPTION,false);
		this.messageSubTypesMask.put(MessageTypes.MESSAGE_SUBTYPE_ERROR, false);
		this.messageSubTypesMask.put(MessageTypes.MESSAGE_SUBTYPE_UPDATE,false);
	}
	
	public ExternalClient(PrintStream ps, int port){
		outStream=ps;
		ID=port;
	}
	
	public ExternalClient(Socket s){
		try {
			outStream=new PrintStream(s.getOutputStream(),true,"UTF-8");
			inStream= new BufferedReader(new InputStreamReader(s.getInputStream()));
			ID=s.getPort();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ExternalClient(Socket s,  int resultMask, int scriptMask, int messageMask){
		this(s);	
		this.resultMask=resultMask;
		this.scriptsMask=scriptMask;
		this.messageMask=messageMask;
		this.messageSubTypesMask.put(MessageTypes.MESSAGE_SUBTYPE_LOG,false);
		this.messageSubTypesMask.put(MessageTypes.MESSAGE_SUBTYPE_EXCEPTION,false);
		this.messageSubTypesMask.put(MessageTypes.MESSAGE_SUBTYPE_ERROR, false);
		this.messageSubTypesMask.put(MessageTypes.MESSAGE_SUBTYPE_UPDATE,false);
	}
	
/*	public ExternalClient(int id, PrintStream ps, int port, int resultMask, int scriptMask, int messageMask, boolean[] messageSubMask){
		this(id,ps,port,resultMask,scriptMask,messageMask);
		for (int i=0;i<messageSubMask.length;i++)
				messageSubTypesMask.put(i, messageSubMask[i]);
//		this.messageSubtypeMask = messageSubMask;
	}*/
	public void setID(int id){
		ID=id;
	}

	public int getID(){
		return ID;
	}
	
	public void handleShowRequest(Element elm){
//		ShowRequestHandler.getInstance().processRequest(elm, this.ID);
		LinkedList<Node> requests = (LinkedList) XMLUtil.getNodeListAsList(XMLUtil.getChildNodesNoWS(elm));
		StringBuilder answer = new StringBuilder("<"+MessageTypes.ACK_TYPE_SHOW+">\n");
		for (Node n:requests){
			Element current = (Element)n;
			String type = current.getTagName();
			if(MessageTypes.GET_SHOW_MESSAGES.equals(type)){
				LinkedList<Node> subRequests = (LinkedList) XMLUtil.getNodeListAsList(XMLUtil.getChildNodesNoWS(current));
				for (Node q: subRequests){
					Element currentSub = (Element)q;
					messageSubtypes.put(currentSub.getTagName(), Integer.parseInt(currentSub.getAttribute("value")));
				}
			}
			else if (MessageTypes.GET_SHOW_RESULTS.equals(type))
				setResultMask(Integer.parseInt(current.getAttribute("value")));
			else if (MessageTypes.GET_SHOW_SCRIPTS.equals(type))
				setScriptsMask(Integer.parseInt(current.getAttribute("value")));
			else {};
		}
	}
	
	public boolean handleMessage(String msg, int messageType, int sourceOrSubtype) {

		int type = messageType;
		switch(type){
			case MessageTypes.TYPE_STATE:
			case MessageTypes.TYPE_PROPERTY:
			case MessageTypes.TYPE_DIME:
				return send(msg);
			case MessageTypes.TYPE_RESULTS:
				 if(resultMask!=MessageTypes.MASK_NONE) return handleResult(sourceOrSubtype, msg);
				break;
			case MessageTypes.TYPE_SCRIPT:
				if(scriptsMask!=MessageTypes.MASK_NONE) return handleScript(msg);
				break;
			case MessageTypes.TYPE_MESSAGE:
				if(messageMask!=MessageTypes.MASK_NONE) return handleMessage(sourceOrSubtype, msg);
		}
		return false; //Message didn't match any filter
	}
	
	private boolean handleMessage(int subType, String message) {
		if(MessageTypes.MASK_ALL==messageMask ||(messageSubTypesMask.get(subType) )) return send(message);
		else return false;
		
	}

	private boolean handleScript(String message) {
		if(MessageTypes.MASK_ALL==scriptsMask)return send(message);
//		else if(MessageTypes.MASK_NAMEONLY==scriptsMask){
//			Element msgElme =XMLUtil.getRootElement(message);
//			return send(XMLUtil.nodeToString(XMLUtil.getChildElementByName(msgElme, "NAME")));
//		}
		else return false;
		
	}

	private boolean handleResult(int subType, String message) {
		if(resultMask==MessageTypes.MASK_ALL || ((resultMask==MessageTypes.MASK_MINE) && subType==this.ID) || ((resultMask==MessageTypes.MASK_SERVER)&&subType==0)) return send(message);
		else return false;
		
	}
	
	public void setMessageSubmask(int type, boolean state){
		messageSubTypesMask.put(type, state);
	}

	/**Sends a properly formated XML Element
	 * 
	 * @param msg Message 
	 * @return true if message was send, false if send failed three times. 
	 */
	private boolean send(String msg){
//		if(!listenEnabled) return true; //If we are not listening for a GUI, we don't need to save messages for it
//		if (false==getConnected()){
////			msgBuffer.push(msg);
//			return true;
//		}
		for (int i=0;i<3;i++){
//			try {
				outStream.println("<XML>"+msg+"</XML>");
				if(!outStream.checkError());
				return true;
//				objectCount++;
//				System.out.println(objectCount+"\n");
//				if (32<objectCount){
//					Thread.sleep(500); //Give object time to be read by GUI
//					outStream.reset();
//					objectCount=0;
//				}
				
//			} catch (IOException e) {
//				logger.warning("send failed attempt "+i);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		return false;
	}

	public int getScriptsMask() {
		return scriptsMask;
	}

	public void setScriptsMask(int scriptsMask) {
		this.scriptsMask = scriptsMask;
		send("<DIMES name=\"SHOW\" type=\"SCRIPT\" value="+scriptsMask+ "/>");
	}

	public int getMessageMask() {
		return messageMask;
	}

	public void setMessageMask(int messageMask) {
		this.messageMask = messageMask;
	}

	public int getMessageSubtypeMask() {
		return messageSubtypeMask;
	}

	public void setMessageSubtypeMask(int messageSubtypeMask) {
		this.messageSubtypeMask = messageSubtypeMask;
	}

	public int getResultMask() {
		return resultMask;
	}
	
	public void setResultMask(int resultMask){
		this.resultMask=resultMask;
		send("<DIMES name=\"SHOW\" type=\"RESULTS\" value="+resultMask+ "/>");
	}
	
	public String showMasks(){
		return MessageTypes.GET_SHOW_MESSAGES+"=\""+messageMask+"\" " + 
		MessageTypes.GET_SHOW_RESULTS+"=\""+resultMask+"\" "+
		MessageTypes.GET_SHOW_SCRIPTS+"=\""+scriptsMask+"\" ";
	}
	
	public boolean isAlive(){
		return true;
	}
}
