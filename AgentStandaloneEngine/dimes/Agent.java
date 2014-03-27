package dimes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import dimes.comm2server.StandardCommunicator;
import dimes.scheduler.usertask.UserTaskSource;
import dimes.state.Status;
//import dimes.state.user.RegistrationStatus;
//import dimes.state.user.UpdateDetailsStatus;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;
//import dimes.util.registration.ProgressMonitorComponent;
//import dimes.util.registration.UpdateDetailsParentFrame;
//import dimes.util.registration.UpdateMonitorDialog;

public interface Agent {

	/**initilizes the Agent by scheduling a task which checks for regisitration and
	 * then asks for work.
	 * 
	 * @throws NumberFormatException
	 * @throws NoSuchPropertyException
	 * @throws IOException
	 */
	public abstract void initAgent() throws NumberFormatException,
			NoSuchPropertyException, IOException;

	//changes how files are handled after usage (handleAfterUsage)
	public abstract void debugMode(/*boolean debug*/);

	/**
	 * causes the schedulerTask to be scheduled according to the latest values
	 * from the properties file.
	 */
	public abstract void rescheduleSchedTimer();

	/**
	 * "Pauses" the Agent's measurments by killing the measurements timer thread. When
	 * the measurements are "resumed" a new timer thread is generated and the measurements continue
	 * 
	 */
	public abstract void pauseSchedTimer();

	/**"Pauses" both the measurements and the keepAlive by killing their respective timer
	 * threads. When the two activities are "resumed" new timer threads are generated 
	 * 
	 */
	public abstract void pauseAllTimers();

	/**"resumes" both measurements and keepAlive by generating new timers
	 *  
	 * @throws MalformedURLException
	 */
	public abstract void resumeAllTimers() throws MalformedURLException;

	/**"resumes" measurements by generating a new measurements timer 
	 * 
	 */
	public abstract void resumeSchedTimer();

	/**Part of the Agent shutdown sequence. Terminates all timers and releases 
	 * the displayServer lock (port 33333) and regitration lock
	 * 
	 * @param theAgentFrame may be null. actually only used for distinguishing in a
	 * polymorphic way between both implementations of exit.
	 */
	public abstract void exit(/*AgentFrame*/Object theAgentFrame);

	/**
	 * 
	 * should be called by anything except by AgentFrame
	 */
	public abstract void exit();

	/** Used for update
	 * 
	 * @param code
	 */
	public abstract void exit(int code);

	/**
	 * Restart the agent.
	 * 
	 * Note that this restarts the agent from within AppSplash class and doesn't
	 * cause the agent to restart using the AutoUpdate mechanism.
	 *  
	 */
	public abstract void restart();

	/** Checks if this Agent is registered. 
	 * 
	 * @param registeredProp
	 * @return true if there are valid values for registered, Username, and AgentName and registered == true
	 */
	public abstract boolean isRegistered(String registeredProp);

	/**Relases the registration lock upon registartion success. 
	 *  
	 */
	public abstract void applyRegistrationSuccess();

	/**
	 * @return true if checkBlockedProtocols() was already called once.
	 */
	public abstract boolean blockedProtocolsWereChecked();

	/**Checks if protocols are blocked. If this is the first time this check is performed
	 * the method will change the default protocol if it is blocked while the other is not.
	 * (IE if the default is UDP, and UDP is blocked while ICMP is not, the default will cahnge to ICMP)
	 * 
	 */
	public abstract void checkBlockedProtocols();

	/**
	 * @param commandLineTask
	 * 
	 * perform a parsed user task. Note that parsing and preparing the user task
	 * is made before calling this function.
	 */
	public abstract void startUserTask(UserTaskSource commandLineTask);

	/**Checks for a valid AgentID in properties.Xml
	 * 
	 * @return true if properties.xml has a valid Agent ID property set
	 */
	public abstract boolean agentIDExist();

	/**Creates a XML Header for the appropriate requests (askForWork, askForAgentIndex, or both)
	 * 
	 * @param askForWork
	 * @param askForAgentIndex
	 * @return A String containing the XML header including the Agent name and IP
	 * @throws NoSuchPropertyException
	 */
	public abstract String getAgentHeader(boolean askForWork,
			boolean askForAgentIndex) throws NoSuchPropertyException;

	/** Creates a default XML header including the Agent name and IP
	 * @return A String containing the XML header including the Agent name and IP
	 * @throws NoSuchPropertyException
	 */
	public abstract String getAgentHeader() throws NoSuchPropertyException;

	/**Creates a XML Header which may ask for work (calls this.getAgentHeader(askForWork, false);)
	 * 
	 * @param askForWork
	 * @param askForAgentIndex
	 * @return A String containing the XML header including the Agent name and IP
	 * @throws NoSuchPropertyException
	 */
	public abstract String getAgentHeader(boolean askForWork)
			throws NoSuchPropertyException;

	/**
	 * @return "</agent>";
	 */
	public abstract String getAgentTrailer();

	/** Getter method for KeepAliveTimerThread
	 * @return this.keepAliveTimerThread
	 */
	public abstract Thread getKeepAliveTimerThread();

	/**Setter method for keepAliveTimerThread
	 * 
	 * @param keepAliveTimerThread
	 */
	public abstract void setKeepAliveTimerThread(Thread keepAliveTimerThread);

	/**
	 * @return this.status
	 */
	public abstract Status getStatus();

	/**
	 * @return Returns the value of restart (boolean)
	 */
	public abstract boolean getRestart();

	/**Sets the value of restart 
	 * 
	 * @param theRestart
	 *            
	 */
	public abstract void setRestart(boolean theRestart);

	/**Called from AgentFrame to save a change to the User properties
	 * can save a change to Username, AgnetName, Email, or Country
	 * 
	 * @param frame
	 * @param monitorComponent
	 * @param userName
	 * @param agentName
	 * @param country
	 * @param email
	 */
//	public abstract void applyUserPropertiesChange(
//			UpdateDetailsParentFrame frame,
//			ProgressMonitorComponent monitorComponent, String userName,
//			String agentName, String country, String email);

	/**Updates properties in propertiesBean that were set either in the
	 * Registration or properties frames
	 * 
	 * @param updateStat the updated status
	 * @return true if update was successful
	 */
//	public abstract boolean writeUpdatedDetails(UpdateDetailsStatus updateStat);

	/**Apply updates to the group settings 
	 * 
	 * 
	 * @param frame
	 * @param monitorComponent
	 * @param groupName
	 * @param groupAction
	 */
//	public abstract void applyGroupDetailsUpdate(
//			UpdateDetailsParentFrame frame,
//			UpdateMonitorDialog monitorComponent, String groupName,
//			String groupAction);

	/***************************************************************************
	 * 
	 * a general function for updating details.
	 * 
	 * note that if updateRequest is null , then this is in fact a local update
	 * meaning that there will be no exchange with the server.
	 * 
	 * also - success and error messages will be included in the resulting
	 * dialog.
	 * 
	 * @param frame
	 * @param monitorComponent
	 * @param groupAction
	 * @param groupName
	 * @param email
	 * @param country
	 * @param userName
	 * @param updateRequest
	 *  
	 */
//	public abstract void applyDetails(UpdateDetailsParentFrame frame,
//			UpdateMonitorDialog monitorComponent,
//			UpdateDetailsStatus updateDetails, Vector<String> successMessages,
//			Vector<String> errorMessages);

	/**Apply a passward change
	 * 
	 * @param frame
	 * @param updateDialog
	 * @param currentPassword
	 * @param newPassword
	 */
//	public abstract void applyPasswordChange(UpdateDetailsParentFrame frame,
//			UpdateMonitorDialog monitorComponent, String currentPassword,
//			String newPassword);

	/**Created a StandardCommunicator based on the URLS in properties.xml. By default, this method
	 * attempts to create a secure communicator first, and upon faliure then attempts to create a 
	 * non-secure one.
	 * 
	 * 
	 * @return StandardCommunicator 
	 */
	public abstract StandardCommunicator getPropertiesUpdateCommunicator();

	/**Writes registration details from a RegistartionStatus
	 * 
	 * @param RegistrationStatus regStat
	 * @throws IOException
	 */
//	public abstract void writeRegistrationDetails(RegistrationStatus regStat)
//			throws IOException;

	/**Returns the Operations Record of the current graph
	 * 
	 * @return XML of all current results 
	 */
	public abstract String getCurrectGraphStateRecord();

	//agentEngineTester and the debug mode were an idea that started out in 
	//version 0.6 and never got completed. The idea is to have a package which can
	//prob different asspects of the Agent remotly and give us an understanding as
	//to how the Agent behaves in different environments (User's computers) and
	//if a user complains that their agent "doesn't work" it may help us to find out
	//why. This was never implemented in any meaningful way, but the code for the
	//Agent Engine Tester can be found in dimes. util debug. - BoazH
	public abstract void toggleDebug(boolean state);

	public abstract boolean handleRegistration() throws IOException, NoSuchPropertyException;

	public abstract void getWork() throws Exception;

}