/*
 * Created on 28/02/2005
 *
 */
package dimes.state.user;

import dimes.comm2server.StandardCommunicator;
import dimes.util.registration.ProgressMonitorPanel ;
import dimes.gui.registration.RegisterDetailsParentFrame;

/**
 * @author Ohad Serfaty
 *
 * a thread responsible for registering a user/agent.
 */
public class RegistrationThread extends Thread implements CancelInputListener
{

	RegisterDetailsHandler registerHandler = null;
	RegisterDetailsParentFrame regFrame;
	ProgressMonitorPanel regMonitor;
	private String password = null;
	private String userName = null;
	private String country = null;
	private String email = null;
	private String agentName = null;
	private boolean ignoreActiveAgentWarning = false;

	private static final int IMPOSSIBLE_TYPE_REGISTRATION = 0;
	private static final int NEW_USER_REGISTRATION = 1;
	private static final int VETERAN_USER_REGISTRATION = 2;
	private static final int VETERAN_USER_AGENT_RETRIEVAL = 3;
	private static final int VETERAN_AGENT_REGISTRATION = 4;

	private int registrationType = IMPOSSIBLE_TYPE_REGISTRATION;
	//    private Window myParentWindow;

	/**
	 * @param communicator
	 * @param frame
	 * @param dialog
	 * @param regMonitorPanel
	 */
	public RegistrationThread(StandardCommunicator communicator, RegisterDetailsParentFrame frame,
	//            Window parentWindow, 
			ProgressMonitorPanel regMonitorPanel)
	{
		registerHandler = new RegisterDetailsHandler(communicator);
		regFrame = frame;
		//        	myParentWindow = parentWindow;
		regMonitor = regMonitorPanel;
	}

	/**********
	 * run method : 
	 * 1.attempts to register
	 * 2. in case of an error - backs off exponential number of seconds.
	 * 3. in case of success - return the registration status and terminate.
	 * 4. in case of cancelation - the thread is interrupted
	 * 
	 */
	public void run()
	{
		boolean registrationCanceled = false;
		int secondsToWait = 4;
		PropertiesStatus regStat = new RegistrationStatus();

		// repeat while the registration is not successfull 
		// and the user hadn't canceled.
		do
		{

			regMonitor.startProgress();
			regMonitor.resetMessage();

			// register :
			switch (registrationType)
			{
				case NEW_USER_REGISTRATION :
					regStat = this.registerHandler.registerNewUser(userName, country, email, password);
					break;
				case VETERAN_USER_REGISTRATION :
					regStat = this.registerHandler.registerAgentToUsersGroup(userName, password);
					break;
				case VETERAN_USER_AGENT_RETRIEVAL :
					regStat = this.registerHandler.retrieveAgentsForExistingUser(userName, password);
					break;
				case VETERAN_AGENT_REGISTRATION :
					regStat = this.registerHandler.registerExistingAgent(agentName, this.ignoreActiveAgentWarning);
					this.ignoreActiveAgentWarning = false;
					break;
				default :
					regStat.errorMessage = "Internal Agent error.";
					regStat.registrationCanceled = true;
					break;
			}

			//		    if (registrationType == NEW_USER_REGISTRATION)		    
			//		        regStat = this.registerHandler.registerNewUser(userName,country,email,password);
			//		    else		        
			//		        if (registrationType == VETERAN_USER_REGISTRATION)		    
			//		            regStat = this.registerHandler.registerAgentToUsersGroup(userName,password);
			//		        else
			//		            if (registrationType == VETERAN_USER_AGENT_RETRIEVAL)
			//		                regStat = this.registerHandler.retrieveAgentsForExistingUser(userName, password);
			//		            else
			//		                if (registrationType == VETERAN_AGENT_REGISTRATION)
			//		                    regStat = this.registerHandler.registerExistingAgent(userName, agentName);
			//		                else
			//				        {
			//				            regStat.errorMessage = "Internal Agent error.";
			//				            regStat.registrationCanceled = true;
			//				        }

			// see if there was an error and act :
			if (regStat.communicationError)
			{
				regMonitor.stopProgress();
				regMonitor.showErrorMessage("Communication failed.\n" + "DIMES Agent will automaticly try to register again every " + secondsToWait
						+ " Seconds.\n" + "Press the Cancel button if you wish to register later.");
				try
				{
					Thread.sleep(secondsToWait * 1000);
					if (secondsToWait < 128)
						secondsToWait = secondsToWait * 2;
				}
				catch (InterruptedException e)
				{
					registrationCanceled = true;
					//                e.printStackTrace();
				}
			}

			if (regStat.internalServerError)
			{
				regMonitor.stopProgress();
				regMonitor.showErrorMessage("Internal server error.\n" + "DIMES Agent will automaticly try to register again every " + secondsToWait
						+ " Seconds.\n" + "Press the Cancel button if you wish to register later.");
				try
				{
					Thread.sleep(secondsToWait * 1000);
					if (secondsToWait < 128)
						secondsToWait = secondsToWait * 2;
				}
				catch (InterruptedException e)
				{
					registrationCanceled = true;
					//                e.printStackTrace();
				}
			}

		}
		while ((regStat.communicationError || regStat.internalServerError) && !registrationCanceled);

		// at the end : return the status.
		if (registrationCanceled)
			regStat.registrationCanceled = true;
		regFrame.returnRegistrationState(regStat);

	}

	/**
	 * starts a new user registration with a thread.
	 * 
	 * @param string4
	 * @param string3
	 * @param string2
	 * @param userName
	 * 
	 */
	public void startNewUserRegistration(String aUserName, String aCoutnry, String anEmail, String aPassword)
	{
		password = aPassword;
		userName = aUserName;
		country = aCoutnry;
		email = anEmail;

		registrationType = NEW_USER_REGISTRATION;

		this.start();
	}

	/**
	 * start a agent-to-user registration with a seperate thread.
	 * 
	 * @param string4
	 * @param string3
	 * @param string2
	 * @param userName
	 * 
	 */
	public void startVeteranUserRegistration(String aUserName, String aPassword)
	{
		password = aPassword;
		userName = aUserName;
		country = null;
		email = null;

		registrationType = VETERAN_USER_REGISTRATION;

		this.start();
	}

	/**************
	 * if the task is canceled - this thread is interrupted from
	 * it's sleep - thus allowing cancel.
	 */
	public void taskCanceled()
	{
		this.interrupt();
	}

	/* (non-Javadoc)
	 * @see dimes.user.CancelInputListener#taskFinished()
	 */
	public void taskFinished()
	{
		// do nothing - the parent frame will do the rest...

	}

	public void startVeteranUserAgentRetrieval(String aUserName, String aPassword)
	{
		password = aPassword;
		userName = aUserName;
		country = null;
		email = null;

		registrationType = VETERAN_USER_AGENT_RETRIEVAL;

		this.start();
	}

	/**
	 * @param theSelectedAgent
	 * @param ignoreWarning
	 * @param theUserName
	 */
	public void startVeteranAgentRegistration(String theSelectedAgent, boolean ignoreWarning)
	{
		this.agentName = theSelectedAgent;
		this.password = null;
		this.country = null;
		this.email = null;
		this.userName = null;
		this.ignoreActiveAgentWarning = ignoreWarning;

		registrationType = VETERAN_AGENT_REGISTRATION;

		//        System.out.println("starting veteran agent registration with agent: "+theSelectedAgent);//debug
		this.start();
	}

}