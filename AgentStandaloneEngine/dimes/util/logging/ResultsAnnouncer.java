package dimes.util.logging;

import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Handler;

import dimes.util.Announcer;
import dimes.util.Listener;

/**An announcer used by ResultsManager to let listeners know when results have reached 
 * size limit
 * 
 * 
 * @author BoazH
 * @since 0.5.5
 *
 */
public abstract class ResultsAnnouncer extends Handler implements Announcer {

	private HashSet<Listener> listeners = new HashSet<Listener>();//holds Listeners
	
	@Override
	public void addListener(Listener listener) {
		this.listeners.add(listener);

	}

	@Override
	public void announce() {
		System.out.println("DEBUGLOG ResultAnnouncer.announce called");
		Iterator<Listener> iter = this.listeners.iterator();
		while (iter.hasNext())
		{
			Listener aListener = iter.next();
			aListener.listen(this);
		}

	}

	@Override
	public void removeListener(Listener listener) {
		this.listeners.remove(listener);

	}

}
