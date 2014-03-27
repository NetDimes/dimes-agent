/*
 * Created on 28/02/2005
 *
 */
package dimes.state.user;

import dimes.comm2server.StandardCommunicator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;

import dimes.state.user.RegistrationStatus;

//import dimes.state.user.CancelInputListener;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.gui.registration.ProgressMonitorPanel;
//import dimes.gui.registration.RegisterDetailsParentFrame;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;

/**
 * @author Ohad Serfaty
 *
 * a thread responsible for registering a user/agent.
 */
@Deprecated
public class RegistrationThread extends Thread //implements CancelInputListener
{

	RegisterDetailsHandler registerHandler = null;
//	RegisterDetailsParentFrame regFrame;
//	ProgressMonitorPanel regMonitor;
	private String password = null;
	private String userName = "Anonymous";
	private String agentNameSufix =null;
	private String country = "anonymous";
	private String email = "anonymous";
	private String agentName;
	private String agentSuffix;
	private String group=null;
	private String ip = "127.0.0.1";
	private String mac="none";
	private File registrationXMLLoc=null;
	private boolean ignoreActiveAgentWarning = false;
	private static final Logger logger = Loggers.getLogger(RegistrationThread.class);

/*	private static final int IMPOSSIBLE_TYPE_REGISTRATION = 0;
	private static final int NEW_USER_REGISTRATION = 1;
	private static final int VETERAN_USER_REGISTRATION = 2;
	private static final int VETERAN_USER_AGENT_RETRIEVAL = 3;
	private static final int VETERAN_AGENT_REGISTRATION = 4;

	private int registrationType = IMPOSSIBLE_TYPE_REGISTRATION;*/
	//    private Window myParentWindow;

	/**
	 * @param communicator
	 * @param frame
	 * @param dialog
	 * @param regMonitorPanel
	 */
	public RegistrationThread(StandardCommunicator communicator)
	{
		registerHandler = new RegisterDetailsHandler(communicator);
		
		//get the local machine's mac address and use it as a user name
		try{
			InetAddress addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress();
            NetworkInterface ni = NetworkInterface.getByInetAddress(addr);
            mac =  getMac(ni.getHardwareAddress());
            agentSuffix = "."+addr.getHostName()+"."+ mac; //Agent name will be Anonymous.regName.Machinename.FF:FF:FF:FF:FF:FF
		}
		catch(UnknownHostException uhe){}
		catch(SocketException se){}

	}

	private String getMac(byte[] macArray){
		StringBuffer output = new StringBuffer("");
		Byte holder= macArray[0];
		if (holder.intValue()<0) output.append(Integer.toHexString((256+holder.intValue())));
		else output.append(Integer.toHexString(holder));
		for (int i=1;i<macArray.length;i++){
			holder= macArray[i];
			if (holder.intValue()<0) output.append(":"+Integer.toHexString((256+holder.intValue())));
			else output.append(":"+Integer.toHexString(holder));
		}
		return output.toString();
	}
	/**********
	 * run method : 
	 * 1. attempts to register
	 * 2. in case of an error - backs off exponential number of seconds.
	 * 3. in case of success - return the registration status and terminate.
	 * 4. in case of cancelation - the thread is interrupted
	 * 
	 */
	public void run()
	{
		boolean registrationCanceled = false;
		int secondsToWait = 4;
		RegistrationStatus regStat = new RegistrationStatus();

		// repeat while the registration is not successfull 
		// and the user hadn't canceled.
		do
		{
			regStat = this.registerHandler.registerAgentToUsersGroup(userName, agentName, ip, mac, group);
//			regStat = this.registerHandler.registerNewUser(userName+usernameSufix, country, email, password, ip, mac, group);
//			regMonitor.startProgress();
//			regMonitor.resetMessage();

			// register :
/*			switch (registrationType)
			{
				case NEW_USER_REGISTRATION :
					regStat = this.registerHandler.registerNewUser(userName, country, email, password, ip, mac);
					break;
				case VETERAN_USER_REGISTRATION :
					regStat = this.registerHandler.registerAgentToUsersGroup(userName, password, ip, mac);
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
*/
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
//				regMonitor.stopProgress();
				logger.warning("Communication failed.\n" + "DIMES Agent will automaticly try to register again every " + secondsToWait
						+ " Seconds.\n");
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
//				regMonitor.stopProgress();
				logger.warning("Internal server error.\n" + "DIMES Agent will automaticly try to register again every " + secondsToWait
						+ " Seconds.\n" );
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

		}//do loop end
		while ((regStat.communicationError || regStat.internalServerError) && !registrationCanceled);

		// at the end : return the status.
		if (registrationCanceled)
			regStat.registrationCanceled = true;
		try{
		writeRegistrationDetails(regStat);
		}catch(IOException ioe){
			System.out.println("registration failed to save to properties.xml");
		}

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

//		registrationType = NEW_USER_REGISTRATION;

		this.start();
	}

	public void startRegistration(File loc){
		registrationXMLLoc=loc;
		CustomRegistrationBean.init(loc);
		userName+="."+CustomRegistrationBean.getRegName();
		group=CustomRegistrationBean.getGroup();
		agentName=userName+agentSuffix;
		this.start();
	}
	
	public void startRegistration(){
		
		/**
		 *  Starts registration with the default values set at constractor
		 * 
		 */
//		registrationType = NEW_USER_REGISTRATION;

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

//		registrationType = VETERAN_USER_REGISTRATION;

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

//		registrationType = VETERAN_USER_AGENT_RETRIEVAL;

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

//		registrationType = VETERAN_AGENT_REGISTRATION;

		//        System.out.println("starting veteran agent registration with agent: "+theSelectedAgent);//debug
		this.start();
	}
	public void writeRegistrationDetails(RegistrationStatus regStat) throws IOException
	{
		PropertiesBean.setProperty(PropertiesNames.REGISTERED_STATE/* "registered" */, "true");
		String agentName = (String) regStat.agentNames.get(0);
		PropertiesBean.setProperty(PropertiesNames.AGENT_NAME/* "agentName" */, agentName/* regStat.agentName */);
		PropertiesBean.setProperty(PropertiesNames.EMAIL/* "email" */, regStat.email);
		PropertiesBean.setProperty(PropertiesNames.COUNTRY/* "country" */, regStat.country);
		PropertiesBean.setProperty(PropertiesNames.USER_NAME/* "userName" */, regStat.userName);
		if (regStat.hasPassword != null)
			PropertiesBean.setProperty(PropertiesNames.HAS_PSWD_STATE/* "hasPswd" */, "true");
		if (regStat.groupName != null)
			PropertiesBean.setProperty(PropertiesNames.GROUP/* "group" */, regStat.groupName);
		if (regStat.groupOwner != null)
			PropertiesBean.setProperty(PropertiesNames.GROUP_OWNER/* "groupOwner" */, regStat.groupOwner);
	}
}