/*
 * Created on 28/02/2005
 *
 */
package dimes.state.user;

/**
 * @author Ohad Serfaty
 *
 * a class that implements this interface is called from a 
 * RegitrationThread or an UpdateThread.
 *
 */
public interface CancelInputListener
{

	/*********
	 * notify that the task was canceled
	 *
	 */
	public void taskCanceled();

	/**
	 * notify that the task had finished.
	 * 
	 */
	public void taskFinished();
}