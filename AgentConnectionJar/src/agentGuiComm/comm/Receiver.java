package agentGuiComm.comm;

//import javafx.application.Platform;
//import javafx.beans.property.ObjectProperty;
//import javafx.beans.property.SimpleObjectProperty;
//import javafx.beans.value.ChangeListener;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import agentGuiComm.dataStructures.ItemReceiverListener;
import agentGuiComm.dataStructures.LogReceivedItem;
import agentGuiComm.dataStructures.MessageReceivedItem;
import agentGuiComm.dataStructures.ReceivedItemListenerHolder;
import agentGuiComm.dataStructures.ResultReceivedItem;
import agentGuiComm.dataStructures.StatusReceivedItem;
import agentGuiComm.util.XMLUtil;

/**The main class that handles incoming messages from the Agent.  This class cannot be instantiated. It is received once a client connects. 
 * Messages divide into three types:
 * Log - log message
 * Result - raw XML of a measurement result
 * Message - Any message from Agent that is not covered by other two types. 
 * 
 * To receive messages via a receiver a class must register as a Listener for a particular message type
 * Listeners can be either ItemReceivedListner type (which uses regular Java events) or ChangeLister type (which uses javafx)
 * Note that a message is propogated to ALL registered listeners of both types, and that no test is made for uniqness. 
 * 
 * @author user
 *
 */
/**
 * @author user
 *
 */
public class Receiver {

	private ReceivedItemListenerHolder<LogReceivedItem> logItems = new ReceivedItemListenerHolder<LogReceivedItem>();
	private ReceivedItemListenerHolder<ResultReceivedItem> resultItems = new ReceivedItemListenerHolder<ResultReceivedItem>();
	private ReceivedItemListenerHolder<MessageReceivedItem> messageItems = new ReceivedItemListenerHolder<MessageReceivedItem>();
	private ReceivedItemListenerHolder<StatusReceivedItem> statusItems = new ReceivedItemListenerHolder<StatusReceivedItem>();
//	private ObjectProperty<LogReceivedItem> logObjProp = new SimpleObjectProperty<LogReceivedItem>(); 
//	private ObjectProperty<MessageReceivedItem> messageObjProp = new SimpleObjectProperty<MessageReceivedItem>();
//	private ObjectProperty<ResultReceivedItem> resObjProp= new SimpleObjectProperty<ResultReceivedItem>(); 
//	private boolean useJavaFX=false;
	private boolean reportingStatus = false;
	
	/**default constructor available only within the package. The receiver should be instantiated from withing the client. 
	 * 
	 */
	Receiver(){
	}
	
	synchronized void dispatch(final String message) throws InterruptedException{
//		if(useJavaFX){
//			System.out.println("runlater if");
//		Platform.runLater(new Thread(new Runnable(){
//
//			@Override
//			public void run() {
//				System.out.println("runlater run");
//				try {
//				if (message.contains("</RESULTS>")) {
//					
//						publishResults(message.replaceAll("\t", "").replaceAll("><", ">\n<"));
//					
//				}
//				else if (message.contains("</LOG>")){
//					publishLog(message);
//				}
//				else if (message.matches("[</]\\w*[>]")){
//					publishMessage(message);
//				}} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//			}}));}
//		else{
		if (message.contains("</RESULTS>")) {
			publishResults(message.replaceAll("\t", "").replaceAll("><", ">\n<"));
		}
		else if (message.contains("</LOG>")){
			publishLog(message);
		}
		else if (message.contains("</STATUS>")&& reportingStatus){
			publishStatus(message);
		}
		else if (message.matches("[</]\\w*[>]")){
			publishMessage(message);
		}
//		}
	}

	private void publishMessage(String msg) throws InterruptedException {
		MessageReceivedItem msgItem = new MessageReceivedItem(msg);
		messageItems.put(msgItem);
//		messageObjProp.set(msgItem);
	}

	private void publishStatus(String msg) throws InterruptedException {
		StatusReceivedItem msgItem = new StatusReceivedItem(msg);
		statusItems.put(msgItem);
	}
	
	private void publishLog(String msg) throws InterruptedException {
		Element logElm = XMLUtil.getRootElement(msg);
		Node levelNode = logElm.getFirstChild();
		LogReceivedItem logItem= new LogReceivedItem(levelNode.getNodeName(), levelNode.getTextContent());
		logItem.setRawMessage(msg);
		logItems.put(logItem);
//		logObjProp.set(logItem);
	}

	private void publishResults(String msg) throws InterruptedException {
		ResultReceivedItem resItem  = new ResultReceivedItem(msg);
		resultItems.put(resItem);
//		resObjProp.set(resItem);
	}
	
	/**Uses Java.Util.EventListener to notify of an incoming logMessage 
	 * @param l
	 */
	public void addLogItemListener(ItemReceiverListener l){
		logItems.addListener(l);
	}

	public void addResultItemListener(ItemReceiverListener l){
		resultItems.addListener(l);
	}
	
	public void addMessageItemListener(ItemReceiverListener l){
		messageItems.addListener(l);
	}
	
	public void addstatusItemListener(ItemReceiverListener l){
		statusItems.addListener(l);
	}
	
//	public void addResultItemListener(ChangeListener<ReceivedItem> lcl){
//		resObjProp.addListener(lcl);
//		useJavaFX=true;
//	}
//	
//	public void addLogItemListener(ChangeListener<ReceivedItem> lcl){
//		logObjProp.addListener(lcl);
//		useJavaFX=true;
//	}
//	
//	public void addMessageItemListener(ChangeListener<ReceivedItem> lcl){
//		messageObjProp.addListener(lcl);
//		useJavaFX=true;
//	}
	
	public void removeLogItemListener(ItemReceiverListener l){
		logItems.removeListener(l);
		
	}
	
	public void removeResultsItemListener(ItemReceiverListener l){
		resultItems.removeListener(l);
	}
	
	public void removeMessageItemListener(ItemReceiverListener l){
		messageItems.removeListener(l);
	}

	public void setStatusReporting(boolean statusReporting) {
		this.reportingStatus = statusReporting;
	}

	public boolean isStatusReporting() {
		return reportingStatus;
	}
	
//	public void removeResultsItemListener(ChangeListener<ReceivedItem> lcl){
//		resObjProp.removeListener(lcl);
//	}
//	
//	public void removeLogItemListener(ChangeListener<ReceivedItem> lcl){
//		logObjProp.removeListener(lcl);
//	}
//	
//	public void removeMessageItemListener(ChangeListener<ReceivedItem> lcl){
//		messageObjProp.removeListener(lcl);
//	}
	
	
	
}
