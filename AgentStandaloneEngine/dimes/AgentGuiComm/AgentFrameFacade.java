package dimes.AgentGuiComm;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dimes.Agent;
//import dimes.AgentGuiComm.util.Message;
import dimes.AgentGuiComm.util.MessageTypes;
import dimes.measurements.IPUtils;
import dimes.scheduler.usertask.CommandLineUserTask;
import dimes.scheduler.usertask.FileUserTaskSource;
import dimes.scheduler.usertask.LinkExtractor;
import dimes.scheduler.usertask.RawUserTaskSource;
import dimes.scheduler.usertask.URLCrawlUserTaskSource;
import dimes.scheduler.usertask.URLUserTaskSource;
import dimes.scheduler.usertask.UserTaskPerserException;
import dimes.scheduler.usertask.UserTaskSource;
import dimes.AgentGuiComm.logging.ResultSenderHandler;
//import dimes.state.user.RegistrationStatus;
//import dimes.state.user.UpdateDetailsStatus;
import dimes.util.XMLUtil;


public class AgentFrameFacade {

	private static AgentFrameFacade me;
	private static Agent agent;
	private static UserTaskSource task=null;
	
	static{
		me=new AgentFrameFacade();
	} 
	
	/**This is not like a normal singleton because this facade is initialized statically
	 * in order to receive messages from the GUI. 
	 * 
	 * @return
	 */	
	public static AgentFrameFacade getInstance(){
		return me;
	}
	
	public void setAgent(Agent anAgent){
		agent=anAgent;
	}

/**	 flags[0] - Operation type (traceroute/ping)
	 flags[1] - protocol (ICMP/UDP/TCP)
	 flags[2] - Crawl depth (for crawling URLs)
	 flags[3] - IP limit (Crawling URLs)*/
	/*public static void dispatchUserScript(Message msg){
		int flagsInt = msg.getID();
		String.valueOf(flagsInt).toCharArray();
		
		Thread runner;
		
		int[] flags = new int[] {0,0,0,0};
		for(int i=0;i<4;i++){
			flags[i]=((Integer)msg.getParam(i)).intValue();
		}

		System.out.println("dispatchUserScript: "+flags[0]);
		
		switch (msg.getID()){
			case MessageTypes.USER_TRACE:
				task = new CommandLineUserTask(msg.getFirstAttrib(), MessageTypes.names.get(flags[1]));//me.dispatchUserTrace(flags[2], msg.getFirstAttrib());
				break;
			case MessageTypes.USER_CRAWL_URL:
				task =new URLCrawlUserTaskSource(msg.getFirstAttrib(), flags[0], flags[1], flags[3], flags[4]);//me.dispatchUserCrawlURL(msg.getFirstAttrib(), flags[0], flags[1], flags[3], flags[4]);
				break;
			case MessageTypes.USER_LOAD_FILE:
				task = new FileUserTaskSource(new File(msg.getFirstAttrib()), flags[0], flags[1]);
				break;
			case MessageTypes.USER_LOAD_URL:
				task = new URLUserTaskSource(msg.getFirstAttrib(), flags[0], flags[1]);
				break;
			case MessageTypes.USER_CRAWL_STOP:
				LinkExtractor.stop();
				break;
		default: break;
				
		}
		runner = new Thread(){
			public void run(){
				try {
					System.out.println("preparse");
					task.parse();
				} catch (UserTaskPerserException e) {
					e.printStackTrace();
				}
				System.out.println("prestart");
	//			dimes.scheduler.SyntaxTree.reset(); //test only!
				agent.startUserTask(task);
			}
		};
		runner.start();
		
		
	}
*/	
	public String debugQueryReturn(String s){
		int i;
		try{
			i=Integer.parseInt(s);
			return String.valueOf(i^2);
		}catch(NumberFormatException nfe){
			return s+" "+s;
		}
	}
	
/*	public void dispatchCommand(Message msg){
		if ("SHUTOFF".equals(msg.getFirstAttrib())) agent.exit();
	}*/

	public void StopButton_actionPerformed(Object object, boolean b) {
		agent.exit(null);
		
	}

	public void applyRegistrationSuccess() {
		agent.applyRegistrationSuccess();
		
	}
//TODO: Put an import here for the registration Jar
	
//	public void writeRegistrationDetails(RegistrationStatus regStat) throws IOException {
//		agent.writeRegistrationDetails(regStat);
//		
//	}
//
//	public boolean writeUpdatedDetails(UpdateDetailsStatus updateStat) {
//		return agent.writeUpdatedDetails(updateStat);
//	}

	public String getCurrectGraphStateRecord() {
	
		return null;
	}

	public void dispatchUserScript(Element current, String expID, int port) {
		//TODO: Determine what kind of script did we get and creat proper task for it
//		if("file".equals(current.getAttribute("type")))
//		{
//			task = new FileUserTaskSource(new File(current.getAttribute("filename")), dimes.measurements.MeasurementType.TRACEROUTE, dimes.measurements.Protocol.ICMP);
//		}
		String experimentID= expID;
		int defaultOperation;
		int defaultProtocol;
		UserTaskSource task = null;
		String resource = null;
		try{
		Element resourceElm = XMLUtil.getChildElementByName(current, "RESOURCE");//.getTextContent();
		Node resourceFirstChildElm =	resourceElm.getFirstChild();
		if(null==resourceFirstChildElm) resource = resourceElm.getTextContent();
		else resource = XMLUtil.nodeToString(resourceFirstChildElm);
		switch (MessageTypes.lookup.get(XMLUtil.getChildElementByName(current, "TYPE").getTextContent())){
		case MessageTypes.SCRIPT_ACTION_LOAD_FILE_ACTION :
			File fileToOpen = new File(resource);
			 defaultOperation = MessageTypes.lookup.get(XMLUtil.getChildElementByName(current, "OPERATION").getTextContent());
			 defaultProtocol = MessageTypes.lookup.get(XMLUtil.getChildElementByName(current, "PROTOCOL").getTextContent());
			task = new FileUserTaskSource(fileToOpen, defaultOperation, defaultProtocol);
			break;

		case MessageTypes.SCRIPT_ACTION_LOAD_URL_ACTION :
			 defaultOperation = MessageTypes.lookup.get(XMLUtil.getChildElementByName(current, "OPERATION").getTextContent());
			 defaultProtocol = MessageTypes.lookup.get(XMLUtil.getChildElementByName(current, "PROTOCOL").getTextContent());
			task = new URLUserTaskSource(resource, defaultOperation, defaultProtocol);
			break;

		case MessageTypes.SCRIPT_ACTION_CRAWL_URL_ACTION :
			defaultOperation = MessageTypes.lookup.get(XMLUtil.getChildElementByName(current, "OPERATION").getTextContent());
			 defaultProtocol = MessageTypes.lookup.get(XMLUtil.getChildElementByName(current, "PROTOCOL").getTextContent());
			int crawlLevel = MessageTypes.lookup.get(XMLUtil.getChildElementByName(current, "CRWALERLEVEL").getTextContent());
			int ipsLimit = MessageTypes.lookup.get(XMLUtil.getChildElementByName(current, "IPLIMITLEVEL").getTextContent());
			task = new URLCrawlUserTaskSource(resource, defaultOperation, defaultProtocol, crawlLevel, ipsLimit);
			break;
		case MessageTypes.SCRIPT_ACTION_RAW_SCRIPT_ACTION:
			task = new RawUserTaskSource(resource, expID+port);
			break;
			}
		}catch(Exception e){
		 return;	
		}
				
		try {
			task.parse();
			if(task instanceof FileUserTaskSource){
				String commandString = task.getCommandsString();
				Element pennyElm = XMLUtil.getRootElement(commandString);
				Element scriptElm = XMLUtil.getChildElementByName(pennyElm, "Script");
				experimentID = scriptElm.getAttribute("ExID");
				
			}
			if(task instanceof RawUserTaskSource){
				String commandString = task.getCommandsString();
				Element pennyElm = XMLUtil.getRootElement(commandString);
				Element scriptElm = XMLUtil.getChildElementByName(pennyElm, "SCRIPT");
				experimentID = scriptElm.getAttribute("EXID");
				((RawUserTaskSource)task).setScriptID(experimentID);
			}
			ResultSenderHandler.getInstance();
			ResultSenderHandler.addUserExperiment(experimentID, Integer.toString(port));

			agent.startUserTask(task);
		} catch (UserTaskPerserException e) {
			e.printStackTrace();
		}
	}


	

}
