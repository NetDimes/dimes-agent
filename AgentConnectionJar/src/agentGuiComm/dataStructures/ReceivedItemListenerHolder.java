package agentGuiComm.dataStructures;

import java.util.HashSet;

/**A holder class allowing for listeners to be registered on events from a receiver
 * @author user
 *
 */
public class ReceivedItemListenerHolder<T extends ReceivedItem> {


	HashSet<ItemReceiverListener> listeners = new HashSet<ItemReceiverListener>();

	public ReceivedItemListenerHolder(){
		
	}
	
	public void addListener(ItemReceiverListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(ItemReceiverListener listener){
		listeners.remove(listener);
	}
	
	public void put(T item) throws InterruptedException{
		for(ItemReceiverListener a:listeners){			
			a.ItemReceived(item);
		}
		
	}


}
