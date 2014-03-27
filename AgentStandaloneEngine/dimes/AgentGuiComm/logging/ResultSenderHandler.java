package dimes.AgentGuiComm.logging;

import java.util.Hashtable;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;

import dimes.AgentGuiComm.GUICommunicator;
import dimes.AgentGuiComm.util.MessageTypes;
import dimes.AgentGuiComm.comm.ClientsBean;
import dimes.util.XMLUtil;

/**A logger that takes all results and duplicates them to the GUI
 * is there is one.
 * 
 * @author user
 *
 */
public class ResultSenderHandler extends ConsoleHandler {
	
	private static boolean enabled=true;
	private static ResultSenderHandler me=null;
	private static Hashtable<String, String> experimentsTable = new Hashtable<String, String>(); 
	
	public void publish(LogRecord log){
		if(ResultSenderHandler.enabled){
			String port = "0";
			String exid = XMLUtil.getChildElementByName(XMLUtil.getRootElement(log.getMessage()), "ExID").getTextContent();
			if(experimentsTable.containsKey(exid))
				port = experimentsTable.remove(exid);
			ClientsBean.send(MessageTypes.SEND_TYPE_RESULTS, port, log.getMessage());			
		}
//		GUICommunicator.sendResult(log.getMessage(), MessageTypes.SEND_RESULT_SOURCE_SERVER);
	}

	public static ResultSenderHandler getInstance(){
		if(null==me)
			me = new ResultSenderHandler();
		return me;
	}
	
	private ResultSenderHandler(){}
	
	/** Enable or disable result duplication. Effects all ResultSenderHandlers
	 * @param state
	 */
	public static void setEnabled(boolean state){
		enabled=state;
	}
	
	public static boolean getEnabled(){
		return enabled;
	}
	
	public static void addUserExperiment(String ID, String port ){
		experimentsTable.put(ID, port);
	}
}
