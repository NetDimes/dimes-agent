package dimes.measurements.nio;

/**
 * @author Ohad Serfaty
 *
 * A callback context is called by the native code every time 
 * a packet is received. the function that is called depends on the
 * type of read() function you call in the NetworkStack level.
 *
 */
public interface CallbackContext {
	
	public boolean callback();
	public boolean callback(byte[] p , long milisecReceiveTime , long microsecReceiveTime) ;

}
