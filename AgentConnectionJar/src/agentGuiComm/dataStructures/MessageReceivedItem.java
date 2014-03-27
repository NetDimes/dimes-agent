package agentGuiComm.dataStructures;

public class MessageReceivedItem extends ReceivedItem {

	public MessageReceivedItem(String msg) {
		super("MESSAGE");
		setRawMessage(msg);
	}
	
	public String getMessage(){
		return getRawMessage();
	}

}
