/*
 * Created on 11/2008
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
import dimes.AgentGuiComm.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;

/**
 * @author BoazH (based on Ohad Serfaty) 
 *
 * a class responsible for registering a user/agent. It is deliberately NOT threaded, so that all other activity stops until this completes.  
 */
public class RegistrationWorker //extends Thread //implements CancelInputListener
{

	RegisterDetailsHandler registerHandler = null;
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
	private static final Logger logger = Loggers.getLogger(RegistrationWorker.class);


	/**
	 * @param communicator
	 * @param frame
	 * @param dialog
	 * @param regMonitorPanel
	 */
	public RegistrationWorker(StandardCommunicator communicator)
	{
		registerHandler = new RegisterDetailsHandler(communicator);
		
		//get the local machine's mac address and use it as a user name
		String macPropString="";
		InetAddress addr=null;
		try{
			ip = PropertiesBean.getProperty(PropertiesNames.IP);
			addr = InetAddress.getLocalHost();
			macPropString = PropertiesNames.MAC;
			mac = PropertiesBean.getProperty(macPropString);
//			ip = addr.getHostAddress();
//            NetworkInterface ni = NetworkInterface.getByInetAddress(addr);
//            mac =  getMac(ni.getHardwareAddress());

            agentSuffix = "."+addr.getHostName()+"."+ mac; //Agent name will be Anonymous.regName.Machinename.FF:FF:FF:FF:FF:FF
		}
		catch(UnknownHostException uhe){uhe.printStackTrace();}
		catch(PropertiesBean.NoSuchPropertyException  nspe){
			nspe.printStackTrace();
			System.out.println(macPropString+"\nTrying to get mac directly");
			if(macPropString.equalsIgnoreCase("mac")){
	            NetworkInterface ni;
				try {
					ni = NetworkInterface.getByInetAddress(addr);
					 mac =  getMac(ni.getHardwareAddress());
					 System.out.println(mac);
				} catch (SocketException e) {
					e.printStackTrace();
					System.out.println("Failed to get mac");
				}
	           
			}
		}

	}

	//This is the usual entry point to the class. File loc is the location of the regitration.xml
	public void startRegistration(File loc){
		registrationXMLLoc=loc;
		CustomRegistrationBean.init(loc);
		userName+="."+CustomRegistrationBean.getRegName();
		group=CustomRegistrationBean.getGroup();
		agentName=userName+agentSuffix;
		this.doRegistration();
	}
	
	//Registration.xml doesn't exit, use default values
	public void startRegistration(){
		
		userName+=".Anonymous";
		//group is null by default
		agentName=userName+agentSuffix;
		this.doRegistration();
	}
	
	/**********
	 * run method : 
	 * 1. attempts to register
	 * 2. in case of an error - backs off exponential number of seconds.
	 * 3. in case of success - return the registration status and terminate.
	 * 4. in case of cancelation - the thread is interrupted
	 * 
	 */
	public void doRegistration()
	{
		boolean registrationCanceled = false;
		int secondsToWait = 4;
		RegistrationStatus regStat = new RegistrationStatus();

		// repeat while the registration is not successfull 
		// and the user hadn't canceled.
		do
		{
			regStat = this.registerHandler.registerAgentToUsersGroup(userName, agentName, ip, mac, group);
			
			if (!regStat.errorMessage.equals("")){
				logger.severe(regStat.errorMessage+"Please Contact DIMES team \n Agent will exit");
				System.exit(1);
			}

			// see if there was an error and act :
			if (regStat.communicationError)
			{
//				regMonitor.stopProgress();
				logger.warning("Communication failed.\n" + "DIMES Agent will automaticly try to register again every " + secondsToWait
						+ " Seconds.\n");
				try{
					Thread.sleep(secondsToWait * 1000);
					if (secondsToWait < 128)
						secondsToWait = secondsToWait * 2;
				}
				catch (InterruptedException e){
					registrationCanceled = true;
					//                e.printStackTrace();
				}
			}

			if (regStat.internalServerError)
			{
//				regMonitor.stopProgress();
				logger.warning("Internal server error.\n" + "DIMES Agent will automaticly try to register again every " + secondsToWait
						+ " Seconds.\n" );
				try{
					Thread.sleep(secondsToWait * 1000);
					if (secondsToWait < 128)
						secondsToWait = secondsToWait * 2;
				}
				catch (InterruptedException e){
					registrationCanceled = true;
					//                e.printStackTrace();
				}
			}

		}while ((regStat.communicationError || regStat.internalServerError) && !registrationCanceled);

		// at the end : return the status.
		if (registrationCanceled)
			regStat.registrationCanceled = true;
		try{
			writeRegistrationDetails(regStat);  //Write the data stored in regStat to properties.xml
		}catch(IOException ioe){
			System.out.println("registration failed to save to properties.xml");
		}

	}

	public void writeRegistrationDetails(RegistrationStatus regStat) throws IOException
	{
		PropertiesBean.setProperty(PropertiesNames.REGISTERED_STATE/* "registered" */, "true");
//		String agentName = (String) regStat.agentNames.get(0);  Save the name we sent to the server, not the name we get from it
		PropertiesBean.setProperty(PropertiesNames.AGENT_NAME/* "agentName" */, agentName/* regStat.agentName */);
//		PropertiesBean.setProperty(PropertiesNames.EMAIL/* "email" */, regStat.email);
//		PropertiesBean.setProperty(PropertiesNames.COUNTRY/* "country" */, regStat.country);
		PropertiesBean.setProperty(PropertiesNames.USER_NAME/* "userName" */, regStat.userName);
		if (regStat.hasPassword != null)
			PropertiesBean.setProperty(PropertiesNames.HAS_PSWD_STATE/* "hasPswd" */, "true");
		if (regStat.groupName != null)
			PropertiesBean.setProperty(PropertiesNames.GROUP/* "group" */, regStat.groupName);
		if (regStat.groupOwner != null)
			PropertiesBean.setProperty(PropertiesNames.GROUP_OWNER/* "groupOwner" */, regStat.groupOwner);
	}
	
	
	
	//--- EVERYTHING BELOW THIS POINT IS STUFF THAT SHOULD BE NOT BE IN USE BY THIS VERSION (0.5.1) AND IS ONLY HERE FOR LEGACY PURPOSE --- //
	
	
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

		this.doRegistration();
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

		this.doRegistration();
	}


	public void startVeteranUserAgentRetrieval(String aUserName, String aPassword)
	{
		password = aPassword;
		userName = aUserName;
		country = null;
		email = null;

//		registrationType = VETERAN_USER_AGENT_RETRIEVAL;

		this.doRegistration();
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
		this.doRegistration();
	}

	
	//Turns a MAC address in byteArray form into a colon-delimited string (FF:FF:FF:FF:FF:FF)    
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
}