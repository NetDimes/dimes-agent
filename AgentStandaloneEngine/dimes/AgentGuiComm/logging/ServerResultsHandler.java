package dimes.AgentGuiComm.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;

public abstract class ServerResultsHandler extends ConsoleHandler {

	public abstract void publish(LogRecord log);
//	public abstract static  serverResultsHandler getInstance();
}
