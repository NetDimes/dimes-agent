package dimes.util.comState;

import java.util.EventObject;

/**
 * @author Ohad
 */
public class ComStateChangeEvent extends EventObject
{
	public boolean isConnected;

	public ComStateChangeEvent(Object source, boolean isConnected_)
	{
		super(source);
		isConnected = isConnected_;
	}
}