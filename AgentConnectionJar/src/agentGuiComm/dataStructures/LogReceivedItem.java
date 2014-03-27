package agentGuiComm.dataStructures;

import java.util.logging.Level;

public class LogReceivedItem extends ReceivedItem {

	String levelString;
	String message;
	Level level;
	
	public LogReceivedItem(String level, String msg){
		super("LOG");
		levelString = level;
		message =msg;
		this.level = Level.parse(levelString);		
	}

	public String getLevelString() {
		return levelString;
	}

	public String getMessage() {
		return message;
	}

	public Level getLevel() {
		return level;
	}
}
