package dimes.AgentGuiComm.Event;

import java.util.EventObject;
//import dimes.connector.util.Message;

public class MessageEvent extends EventObject{
	
	private static final long serialVersionUID = 1L;
	private String msg;
	private int messageType;
	private int messageSubType;
	
	private MessageEvent(Object source) {
		super(source);
	}

	public MessageEvent(Object source, String message){
		this(source);
		msg=message;
	}
	
	public MessageEvent(Object source, String message, int type, int subType){
		this(source);
		msg=message;
		messageType=type;
		messageSubType=subType;
	}
	
	public String getMessage(){
		return msg;
	}
	
	public int getType(){
		return messageType;
	}
	
	public int getSubType(){
		return messageSubType;
	}
}
