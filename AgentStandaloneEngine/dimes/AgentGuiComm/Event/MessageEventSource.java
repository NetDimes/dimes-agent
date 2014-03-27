package dimes.AgentGuiComm.Event;

import java.util.ArrayList;
import java.util.EventObject;

import dimes.AgentGuiComm.Event.MessageEventListener;

public abstract class MessageEventSource {
	  ArrayList<MessageEventListener> listeners = new ArrayList<MessageEventListener>();
	  
	  public void addListener(MessageEventListener l) {
	    listeners.add(l);
	  }
	  
	  public void removeListener(MessageEventListener l) {
	    listeners.remove(l);
	  }
	  
	  public void fireEvent(EventObject o) {
	    for (int i = 0; i < listeners.size(); i++) {
	    	MessageEventListener l = (MessageEventListener) listeners.get(i);
	      l.handleEvent(o);
	    }
	  }
}
