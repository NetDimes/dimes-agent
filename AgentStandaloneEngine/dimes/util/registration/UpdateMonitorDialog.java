/*
 * Created on 06/03/2005
 *
 */
package dimes.util.registration;

import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;

import dimes.state.user.CancelInputListener;

/**
 * @author Ohad Serfaty
 *
 */
public class UpdateMonitorDialog extends JDialog implements ProgressMonitorComponent
{

	private ProgressMonitorPanel monitorPanel;

	public UpdateMonitorDialog(JFrame frame)
	{
		super(frame);
		this.jbInit();
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);//JDialog default
	}

	/**
	 * 
	 */
	public void jbInit()
	{
		monitorPanel = new ProgressMonitorPanel("Updating User details...");
		this.setSize(500, 350);
		this.getContentPane().add(monitorPanel);
	}

	public void dispose()
	{
		this.stopProgress();
		super.dispose();
	}
	/* (non-Javadoc)
	 * @see dimes.gui.ProgressMonitorComponent#setCancelListener(dimes.user.CancelInputListener)
	 */
	public void setCancelListener(CancelInputListener updateDetailsThread)
	{
		monitorPanel.setCancelListener(updateDetailsThread);

	}

	/* (non-Javadoc)
	 * @see dimes.gui.ProgressMonitorComponent#startProgress()
	 */
	public void startProgress()
	{
		monitorPanel.startProgress();
	}

	/* (non-Javadoc)
	 * @see dimes.gui.ProgressMonitorComponent#resetErrorMessages()
	 */
	public void resetMessage()
	{
		monitorPanel.resetMessage();

	}

	/* (non-Javadoc)
	 * @see dimes.gui.ProgressMonitorComponent#stopProgress()
	 */
	public void stopProgress()
	{
		// TODO Auto-generated method stub
		monitorPanel.stopProgress();
	}

	/* (non-Javadoc)
	 * @see dimes.gui.ProgressMonitorComponent#enableFinish()
	 */
	public void enableFinish()
	{
		// TODO Auto-generated method stub
		monitorPanel.enableFinish();
	}

	/**
	 * @param string
	 */
	public void showConfirmMessage(String string)
	{
		monitorPanel.showConfirmMessage(string);

	}

	/* (non-Javadoc)
	 * @see dimes.gui.ProgressMonitorComponent#showErrorMessage(java.lang.String)
	 */
	public void showErrorMessage(String string)
	{
		monitorPanel.showErrorMessage(string);

	}

	/* (non-Javadoc)
	 * @see dimes.gui.registration.ProgressMonitorComponent#showMessages(java.util.Vector, java.util.Vector)
	 */
	public void showMessages(Vector errorMessages, Vector successMessages)
	{
		monitorPanel.showMessages(errorMessages, successMessages);
	}

	/* (non-Javadoc)
	 * @see dimes.gui.registration.ProgressMonitorComponent#taskCanceled()
	 */
	public void taskCanceled()
	{
		this.hide();
	}

}