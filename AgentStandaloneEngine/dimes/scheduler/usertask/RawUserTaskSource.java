package dimes.scheduler.usertask;

import java.util.logging.Logger;

import dimes.AgentGuiComm.logging.Loggers;
import dimes.scheduler.Parser;

public class RawUserTaskSource implements UserTaskSource {

	private String rawScrpitString;
	private String commandString = "";
	private String ID;
	private Logger logger = Loggers.getUserScriptsLogger();
	
	public RawUserTaskSource(String scriptString, String scriptID)
	{
		this.rawScrpitString=scriptString;
		this.ID = scriptID;

	}
	
	@Override
	public String getCommandsString()
	{
		return commandString;
	}


	@Override
	public String getScriptID() {
		return ID;
	}

	@Override
	public void parse() throws UserTaskPerserException {
		commandString = Parser.parsePingTracerouteToXML(rawScrpitString);

	}
	
	public void setScriptID(String ID){
		this.ID =ID;
	}

}
