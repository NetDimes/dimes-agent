package dimes.util.debug;

import dimes.*;
import dimes.AgentGuiComm.*;
import dimes.scheduler.OpParams;
import dimes.scheduler.SyntaxTree;
import dimes.state.handlers.KeepAliveHandler;
import dimes.util.comState.ComStateDetector;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class AgentEngineTester extends Tester {

	private static AgentEngineTester me=null;
	private Agent agent=null;
	private static DebugFileHandler outFile=null;
	
	public static AgentEngineTester getInstance(Agent AG){
		if(null==me) me=new AgentEngineTester(AG);
		return me;
	}
	
	private AgentEngineTester(Agent ag){	
		agent=ag;		
	}
	
	public String testResultFormatter(String testName, boolean success, String msg){
		return testResultFormatter("Engine", testName, success, msg);
	}


	public debugTesterResults testConnection(){
		boolean success = ComStateDetector.getInstance().connectionExists();
		return new debugTesterResults(success,"testConnection","Internet Connection successul");
	}
	
	public debugTesterResults testPrintSystaxTree(){
		StringBuffer message = new StringBuffer();
		boolean success=false;
		try{
			for(OpParams op :SyntaxTree.getList()){
				message.append("\t<op>"+op.toString()+"</op>\n");
			}
			success=true;
		}catch (Exception e){}
		return new debugTesterResults(success, "testPrintSystaxTree", message.toString());
		
	}
	
	public debugTesterResults testServerConnection(){
		boolean success = KeepAliveHandler.getKeepAliveSuccess();
		String message = "Keepalive successful within 15 minutes from "+SimpleDateFormat.getDateTimeInstance().
			format(Calendar.getInstance().getTime());
		return new debugTesterResults(success,"testServerConnection", message);
	}
	

}
