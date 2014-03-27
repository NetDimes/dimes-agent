package agentGuiComm.dataStructures;

import org.w3c.dom.Element;

import agentGuiComm.util.XMLUtil;

public class StatusReceivedItem extends ReceivedItem {

	private boolean lastConnectionSuccess=false;
	private String currentMeasurement="";
	private String currentExId="";
	
	public StatusReceivedItem(String msg) {
		super("STATUS");
		setRawMessage(msg);
		parseStatus(msg);
	}

	private void parseStatus(String msg){
	Element rootelm = XMLUtil.getRootElement(msg);
	lastConnectionSuccess = Boolean.parseBoolean(XMLUtil.getChildElementByName(rootelm, "LAST_CONNECTION_SUCCEFUL").getTextContent());
	currentMeasurement = XMLUtil.getChildElementByName(rootelm, "CURRENT_MEASURMENT").getTextContent();
	currentExId =  XMLUtil.getChildElementByName(rootelm, "CURRENT_EXID").getTextContent();
	}

	public boolean isLastConnectionSuccess() {
		return lastConnectionSuccess;
	}

	public String getCurrentMeasurement() {
		return currentMeasurement;
	}

	public String getCurrentExId() {
		return currentExId;
	}
}
