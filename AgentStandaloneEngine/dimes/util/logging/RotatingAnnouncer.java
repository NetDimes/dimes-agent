package dimes.util.logging;

import dimes.util.Listener;

public interface RotatingAnnouncer {

	public void rotate();
	public void rotate(Listener l);
	
}
