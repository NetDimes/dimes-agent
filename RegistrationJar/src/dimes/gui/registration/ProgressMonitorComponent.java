/*
 * Created on 06/03/2005
 *
 */
package dimes.gui.registration;

import java.util.Vector;

import dimes.state.user.CancelInputListener;

/**
 * @author Ohad Serfaty
 *
 */
public interface ProgressMonitorComponent
{

	/**
	 * @param updateDetailsThread
	 */
	void setCancelListener(CancelInputListener updateDetailsThread);

	/**
	 * 
	 */
	void startProgress();

	/**
	 * 
	 */
	void resetMessage();

	/**
	 * 
	 */
	void stopProgress();

	/**
	 * @param string
	 * 
	 */
	void showErrorMessage(String string);

	/**
	 * 
	 */
	void enableFinish();

	/********
	 * @param string
	 */
	public void showConfirmMessage(String string);

	public void showMessages(Vector errorMessages, Vector successMessages);

	/**
	 * 
	 */
	void taskCanceled();
}