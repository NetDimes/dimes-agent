package dimes.AgentGuiComm.logging;

import java.util.logging.LogRecord;
import dimes.util.logging.ResultsMemoryHandler;
 

/*A wrapper to add 
 * 
 */
public class ServerMemoryResultsHandler extends ServerResultsHandler {
	
	private ResultsMemoryHandler RMH;
	
	public ServerMemoryResultsHandler(){
		RMH = ResultsMemoryHandler.getInstance();
	}
	
	@Override
	public void publish(LogRecord log) {
		RMH.publish(log);
	}

}
