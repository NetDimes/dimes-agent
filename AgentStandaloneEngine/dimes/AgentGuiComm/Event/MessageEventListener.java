package dimes.AgentGuiComm.Event;

import java.util.EventListener;
import java.util.EventObject;

public interface MessageEventListener extends EventListener {

	public void handleEvent(EventObject o);
}
