package dimes.util.time;


/**
 * @author Ohad Sefaty
 *
 * a representation of a simple time syncrhoniztion result.
 * 
 */
public class TimeSynchronizationResult
{
	public int milisecondOffset;
	public double roundTripTime;
	public final double maxErrorTime;
	
	/**
	 * @param roundTripDelay
	 * @param localClockOffset
	 * @param maxErrorTime
	 */
	public TimeSynchronizationResult(double roundTripDelay, int localClockOffset, double maxErrorTime)
	{
		milisecondOffset = localClockOffset;
		roundTripTime = roundTripDelay;
		this.maxErrorTime = maxErrorTime;
	}

	
}
