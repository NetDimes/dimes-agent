package agentGuiComm.dataStructures;

public class ResultReceivedItem extends ReceivedItem {

	
	public ResultReceivedItem(String msg){
		super("RESULT");
		setRawMessage(msg);		
	}
	
	public String getMessage(){
		return getRawMessage();
	}
}
