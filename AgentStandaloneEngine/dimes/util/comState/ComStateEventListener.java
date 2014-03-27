package dimes.util.comState;

import java.util.EventListener;

/**
 * @author Ohad
 */
public interface ComStateEventListener extends EventListener
{
	public void comStateChangeOccurred(ComStateChangeEvent evt);
}