package agentGuiComm.dataStructures;

import javafx.beans.value.ObservableValueBase;

/**
 * Super class for all items received from agent. 
 * 
 * 
 * @author Boaz
 *
 */
public abstract class ReceivedItem extends ObservableValueBase<ReceivedItem>{//implements ObservableObjectValue<ReceivedItem>{

	private String rawMessage=null;
	private String type="";
	
	public ReceivedItem(String type){
		this.type=type;
	}
	
	public String getRawMessage(){
		return rawMessage!=null? rawMessage:"";
	}
	public void setRawMessage(String msg){
		rawMessage=msg;
	}
	
	public String getType(){
		return type;
	}
	
	public ReceivedItem getValue(){
		return this;
	}
	
}
