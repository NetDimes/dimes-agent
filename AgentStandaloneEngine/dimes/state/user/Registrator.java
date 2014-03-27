package dimes.state.user;


import java.net.MalformedURLException;

import dimes.comm2server.ConnectionHandlerFactory;
import dimes.comm2server.StandardCommunicator;
import dimes.AgentGuiComm.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;
import dimes.state.user.RegistrationWorker;
import java.io.File;



public class Registrator {

	/**
	 * @param args
	 */
	public Registrator() {

		System.out.println("Entering Registrator");  //Debug
		aRegistrator aR = new aRegistrator();
		aR.doRegistration();	
		System.out.println("Exiting registrator");
		
	}

}
	
class aRegistrator{

	StandardCommunicator sc;
	
	public aRegistrator(){
		
		sc = getPropertiesUpdateCommunicator();

	}
	
	public void doRegistration(){
		String props;
		File propFileLoc;
		
		RegistrationWorker rt = new RegistrationWorker(sc);
		
		try{
			props = PropertiesBean.getProperty("base");
			propFileLoc = new File(props+File.separator+"conf"+File.separator+"registration.xml");

			if (propFileLoc.exists()){
				rt.startRegistration(propFileLoc); 
			}
			else{
				rt.startRegistration();
			}
		}catch(Exception e){
			props = null;
			propFileLoc=null;
			rt.startRegistration();
			
			e.printStackTrace();
			}
	}
	
	public StandardCommunicator getPropertiesUpdateCommunicator()
	{
		StandardCommunicator propertiesChangeComm = null;
		String propertiesUpdateURL;
		String securePropertiesUpdateURL;
		// Get the secure url : property :
		try
		{
			securePropertiesUpdateURL = PropertiesBean.getProperty(PropertiesNames.PROPERTIES_UPDATE_URL);
		}
		catch (NoSuchPropertyException e)
		{
			Loggers.getLogger().severe("properties file incomplete. couldn't find securePropertiesUpdateURL");
			securePropertiesUpdateURL = "https://www.netdimes.org/DIMES/propertiesUpdate";
		}

		try
		{
			propertiesUpdateURL = PropertiesBean.getProperty(PropertiesNames.PROPERTIES_UPDATE_URL);
		}
		catch (NoSuchPropertyException e)
		{
			Loggers.getLogger().severe("properties file incomplete. couldn't find propertiesUpdateURL");
			propertiesUpdateURL = "http://www.netdimes.org/DIMES/propertiesUpdate";
		}

		// Create the Connection :
		try
		{
			// secure connection :propertiesUpdateURL
			//propertiesChangeComm = new StandardCommunicator(securePropertiesUpdateURL, ConnectionHandlerFactory.NONSECURE_CONNECTION);
			propertiesChangeComm = new StandardCommunicator(propertiesUpdateURL, ConnectionHandlerFactory.NONSECURE_CONNECTION);
		}
		catch (MalformedURLException e)
		{
			// On failure : try and generate a non-secure connection.
			Loggers.getLogger().severe("Could not initialize properties communicator :" + e.getMessage() + " . communicating threw Non-Secure connection.");
			try
			{
				propertiesChangeComm = new StandardCommunicator(propertiesUpdateURL, ConnectionHandlerFactory.NONSECURE_CONNECTION);
			}
			catch (MalformedURLException e1)
			{
				Loggers.getLogger().severe("Could not initialize properties communicator :" + e.getMessage() + " . Cennection will fail.");
			}
		}
		return propertiesChangeComm;
	}
}
