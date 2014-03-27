/*
 * Created on 28/02/2005
 *
 */
package dimes.state.user;

import java.util.Vector;

import dimes.comm2server.StandardCommunicator;
import dimes.util.registration.ProgressMonitorComponent;
import dimes.util.registration.UpdateDetailsParentFrame;
import dimes.util.registration.ProgressMonitorPanel;
import dimes.util.registration.RegistrationFrame;

/**
 * @author Ohad Serfaty
 * 
 * a thread responsile for update details.
 *
 */
public class UpdateDetailsThread extends Thread implements CancelInputListener
{

	UpdateDetailsHandler updateHandler = null;
	UpdateDetailsParentFrame updateFrame;
	ProgressMonitorComponent regMonitor;
	private String password = null;
	private String userName = null;
	private String country = null;
	private String email = null;

	private String autoUpdate = null;

	private static final int IMPOSSIBLE_TYPE_REGISTRATION = 0;
	private static final int AGENT_NAME_UPDATE = 1;
	private static final int USER_DETAILS_UPDATE = 2;
	private static final int GROUP_DETAILS_UPDATE = 3;
	private static final int PASSWORD_UPDATE = 4;
	private static final int GENERAL_PROPERTIES_UPDATE = 5;
	private static final int DETAILS_VALIDATION = 6;

	private int updateType = IMPOSSIBLE_TYPE_REGISTRATION;
	private String agentID = null;
	private String agentName = null;
	private String group;
	private String groupAction;
	private String currentAgentName;
	private String newPdw;
	private String currentPsw;

	private PropertiesStatus generalRequest = null;
	private Vector generalRequestErrors = null;
	private Vector generalRequestSuccs = null;

	/**
	 * @param communicator
	 * @param frame
	 * @param regMonitorPanel
	 */
	public UpdateDetailsThread(StandardCommunicator communicator, UpdateDetailsParentFrame frame, ProgressMonitorComponent regMonitorPanel)
	{
		updateHandler = new UpdateDetailsHandler(communicator);
		updateFrame = frame;
		regMonitor = regMonitorPanel;
	}


	/**********
	 * run method : 
	 * 1.attempts to update
	 * 2. in case of an error - backs off exponential number of seconds.
	 * 3. in case of success - return the update status and terminate.
	 * 4. in case of cancelation - the thread is interrupted
	 * 
	 */
	public void run()
	{
		boolean updateCanceled = false;
		int secondsToWait = 4;
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			updateCanceled = true;
			e.printStackTrace();

		}

		PropertiesStatus updateStat = new UpdateDetailsStatus();
		do
		{
			if (updateCanceled) // in case the user canceled before update made...
				break;

			regMonitor.startProgress();
			regMonitor.resetMessage();

			//		    System.out.println("update type is : " +updateType );

			switch (updateType)
			{
				case UpdateDetailsThread.AGENT_NAME_UPDATE :
					updateStat = this.updateHandler.changeAgentName(currentAgentName,agentName);
					break;
				case UpdateDetailsThread.GROUP_DETAILS_UPDATE :
					updateStat = this.updateHandler.changeGroupDetails(group, groupAction);
					break;
				case UpdateDetailsThread.USER_DETAILS_UPDATE :
					updateStat = this.updateHandler.changeUserDetails(userName, country, email, agentName);
					break;
				case UpdateDetailsThread.PASSWORD_UPDATE :
					updateStat = this.updateHandler.changePassword(currentPsw, newPdw);
					break;
				case UpdateDetailsThread.GENERAL_PROPERTIES_UPDATE :
					if (this.generalRequest != null)
						updateStat = this.updateHandler.generalUpdate(generalRequest);
					else
					{
						// Sleep for one second while applying the changes.
						try
						{
							Thread.sleep(1000);
						}
						catch (InterruptedException e1)
						{
							// TODO Auto-generated catch block                    
						}
					}

					updateStat.errorMessages.addAll(this.generalRequestErrors);
					updateStat.successMessages.addAll(this.generalRequestSuccs);
					break;

				// user validation :
				case DETAILS_VALIDATION :
					updateStat = this.updateHandler.validateAgentDetails(agentID);
					break;

				default :
					updateStat.errorMessage = "Internal Agent error.";
					updateStat.updateCanceled = true;
					break;
			}

			if (updateStat.communicationError)
			{
				regMonitor.stopProgress();
				regMonitor.showErrorMessage("Communication failed.\n" + "DIMES Agent will automaticly try to update your details again every " + secondsToWait
						+ " Seconds.\n" + "Press the Cancel button if you wish to update later.\n");
				try
				{
					Thread.sleep(secondsToWait * 1000);
					if (secondsToWait < 128)
						secondsToWait = secondsToWait * 2;
				}
				catch (InterruptedException e)
				{
					updateCanceled = true;
					e.printStackTrace();
				}
			}

			if (updateStat.internalServerError)
			{
				regMonitor.stopProgress();
				regMonitor.showErrorMessage("Server internal error.\n" + "DIMES Agent will automaticly try to update your details again every " + secondsToWait
						+ " Seconds.\n" + "Press the Cancel button if you wish to update later.\n");
				try
				{
					Thread.sleep(secondsToWait * 1000);
					if (secondsToWait < 128)
						secondsToWait = secondsToWait * 2;
				}
				catch (InterruptedException e)
				{
					updateCanceled = true;
					e.printStackTrace();
				}
			}

		}
		while ((updateStat.communicationError || updateStat.internalServerError) && !updateCanceled);

		if (updateCanceled)
			updateStat.updateCanceled = true;

		if (updateStat.errorMessage != null)
			updateStat.errorMessages.add(updateStat.errorMessage);//check

		regMonitor.stopProgress();
		regMonitor.enableFinish();
		updateFrame.returnUpdateState(updateStat);

	}

	/**
	 * @param string
	 * @param string4
	 * @param string3
	 * @param string2
	 * @param userName
	 * 
	 */
	public void startUserDetailsUpdate(String aUserName, String aCoutnry, String anEmail, String anAgentName)
	{
		userName = aUserName;
		country = aCoutnry;
		email = anEmail;
		agentName = anAgentName;

		updateType = UpdateDetailsThread.USER_DETAILS_UPDATE;

		this.start();
	}

	/*********
	 * 
	 * @param aGroupName
	 * @param aGroupAction
	 */
	public void startGroupDetailsUpdate(String aGroupName, String aGroupAction)
	{
		group = aGroupName;
		groupAction = aGroupAction;
		updateType = UpdateDetailsThread.GROUP_DETAILS_UPDATE;

		this.start();
	}

	/**************
	 * 
	 * @param anAgentName
	 */
	public void startAgentNameUpdate(String aCurrentAgentName, String anAgentName)
	{
		currentAgentName = aCurrentAgentName;
		agentName = anAgentName;
		password = null;
		userName = null;
		country = null;
		email = null;

		updateType = UpdateDetailsThread.AGENT_NAME_UPDATE;

		this.start();
	}

	/**
	 * @param currentPassword
	 * @param newPassword
	 */
	public void startPasswordUpdate(String currentPassword, String newPassword)
	{
		currentPsw = currentPassword;
		newPdw = newPassword;

		updateType = UpdateDetailsThread.PASSWORD_UPDATE;

		this.start();
	}

	/**
	 * @param updateRequest
	 * @param successMessages
	 * @param errorMessages
	 */
	public void startGeneralUpdate(PropertiesStatus updateRequest, Vector successMessages, Vector errorMessages)
	{
		generalRequest = updateRequest;
		generalRequestErrors = errorMessages;
		generalRequestSuccs = successMessages;

		updateType = UpdateDetailsThread.GENERAL_PROPERTIES_UPDATE;

		this.start();
	}

	/**************
	 * if the task is canceled - this thread is interrupted from
	 * it's sleep - thus allowing cancel.
	 */
	public void taskCanceled()
	{
		this.interrupt();
		regMonitor.taskCanceled();
	}

	/* (non-Javadoc)
	 * @see dimes.user.CancelInputListener#taskFinished()
	 */
	public void taskFinished()
	{
		//        System.out.println("finishing task");
		updateFrame.allowExitUpdateFrame();

	}

	/**
	 * @param string
	 */
	public void validateAgentDetails(String anAgentID)
	{

		updateType = UpdateDetailsThread.DETAILS_VALIDATION;
		agentID = anAgentID;

		this.start();
	}

}