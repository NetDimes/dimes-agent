package agentGuiComm.dataStructures;

import java.util.EventListener;

/** An interface to designate Java listeners that receive Message Items from the Agent
 * All listeners that register with the receiver must implement this interface or be
 * javafx ChangeListeners
 * 
 * @see ReceivedItem
 * @author user
 *
 */
public interface ItemReceiverListener extends EventListener {

	public void ItemReceived(ReceivedItem riq);
}
