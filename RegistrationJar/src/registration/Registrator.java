package registration;


import java.net.MalformedURLException;
import javax.swing.JFrame;
import dimes.util.registration.RegistrationFrame;
import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;
import dimes.util.Lock;
import dimes.comm2server.ConnectionHandlerFactory;
import dimes.comm2server.StandardCommunicator;;

/**
 * @author Boaz
 * Registrator front end to handle both interactive and scripted registration
 *
 */
public class Registrator {

	private static Registrator me=null;
	private StandardCommunicator SC;
	private Lock registrationLock= new Lock();
	private boolean registrationSuccess=false;
	
	/**
	 * @param standardCommunicator
	 * @param AgentIDExists
	 * @param interactive type of registration method desired
	 * @return boolean registration was successful
	 */
	public boolean registerAgent(StandardCommunicator standardCommunicator, boolean AgentIDExists, boolean interactive){
		

		if(interactive){
			
			SC = getPropertiesUpdateCommunicator();
			RegistrationFrame RF = new RegistrationFrame(this, SC, false);
			RF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			RF.setSize(500, 350);
			RF.setVisible(true);
			registrationLock.waitFor();			
		}
		else{
			dimes.state.user.NonInteractiveRegistrator.register();
			registrationSuccess = getRegistrationStatus();
		}	
		return registrationSuccess;
	}
	

	private void initPropBean(String propertiesFileLocation){
		PropertiesBean.init(propertiesFileLocation);
	}
	
	public static Registrator getInstance(String propertiesFileLocation){
		if(null==me) me = new Registrator();
		me.initPropBean(propertiesFileLocation);
		return me;
	}
	

	public void applyRegistrationSuccess(RegistrationFrame f) {
		registrationSuccess=getRegistrationStatus();
		registrationLock.release();
	}
	
	boolean getRegistrationStatus() {
		try {
			if ("*agentName*".equals(PropertiesBean.getProperty(PropertiesNames.AGENT_NAME)) || "*registered*".equals(PropertiesBean.getProperty(PropertiesNames.REGISTERED_STATE)))
					return false;
		} catch (NoSuchPropertyException e) {
			System.err.println("PropertiesBean Not Initilized. Registrator.getRegistrationStatus");
			e.printStackTrace();
			System.exit(-1);
		}
		return true;
	}

	private static StandardCommunicator getPropertiesUpdateCommunicator()
	{
		StandardCommunicator propertiesChangeComm = null;
		String propertiesUpdateURL;
		try
		{
			propertiesUpdateURL = PropertiesBean.getProperty(PropertiesNames.PROPERTIES_UPDATE_URL);
		}
		catch (NoSuchPropertyException e)
		{
			Loggers.getLogger().error("properties file incomplete. couldn't find propertiesUpdateURL");
			propertiesUpdateURL = "http://www.netdimes.org/DIMES/propertiesUpdate";
		}

			try
			{
				propertiesChangeComm = new StandardCommunicator(propertiesUpdateURL, ConnectionHandlerFactory.NONSECURE_CONNECTION);
			}
			catch (MalformedURLException e)
			{
				Loggers.getLogger().error("Could not initialize properties communicator :" + e.getMessage() + " . Cennection will fail.");
			}
		
		return propertiesChangeComm;
	}
	
}
