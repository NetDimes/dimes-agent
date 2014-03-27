/*
 * Created on 08/06/2004
 */
package dimes;

import java.util.TimerTask;

//import javax.swing.JOptionPane;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**StartAgentTask is a task that runs once at the start of the Agent. It is 
 * responsible for checking if the Agent is registered and for getting work 
 * the first time. 
 * 
 * @author anat
 */
public class StartAgentTask extends TimerTask
{
	private Agent agent;

	StartAgentTask(Agent anAgent)
	{
		this.agent = anAgent;
	}

	/**
	 * Checks registration twice (first by calling Agent.handleRegistration, and then
	 * by checking the properties.xml file for the REGISTERED_STATE) then asks the
	 * server for work.
	 */
	public void run()
	{
		try
		{
			// includes waiting for registration to complete
			this.agent.handleRegistration();
			//			just in case. should not get here - if registration failed, should be caught in agent.handleRegistration()
			if (!this.agent.isRegistered(PropertiesNames.REGISTERED_STATE/*"registered"*/))
			{
				String errMsg = "Registration failed - exiting...";
				String dimesURL = "";
				try
				{
					dimesURL = PropertiesBean.getProperty(PropertiesNames.DIMES_LINK/*"DimesLink"*/);
				}
				catch (NoSuchPropertyException e)
				{
					e.printStackTrace();
					Loggers.getLogger(this.getClass()).warning(e.toString());
				}
				errMsg += "Please consult the DIMES team or forums for help.\n" + dimesURL;
				System.err.println(errMsg);
//				JOptionPane.showMessageDialog(null, errMsg, "DIMES Error", JOptionPane.ERROR_MESSAGE);
				//                System.exit(1);
				this.agent.exit();
			}
			//checking registration is inside
			//			if (wasRegistered) //in case was already registered. otherwise, wait for window to close
			this.agent.getWork();
		}
		catch (Exception e)
		{
			StackTraceElement[] traceElements = e.getStackTrace();
			String trace = "";
			for (int i = 0; i < traceElements.length; ++i)
				trace += "\n" + traceElements[i].toString();
			Loggers.getLogger(this.getClass()).severe(e.toString() + trace);
		}

	}

}