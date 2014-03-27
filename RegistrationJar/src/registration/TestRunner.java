package registration;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JFrame;

import dimes.state.NetworkInfo;
import dimes.util.registration.RegistrationFrame;
import dimes.comm2server.*;
import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

public class TestRunner {

	private static Registrator AFF;
	private static StandardCommunicator SC;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		PropertiesBean.init(args[0]);
		AFF = Registrator.getInstance(args[0]);
		SC = TestRunner.getPropertiesUpdateCommunicator();
		if(args.length==1) System.out.println("result: "+Boolean.toString(registerInteractive()));
		else registerNonInteractive();
		
	}

	private static void registerNonInteractive() {
		AFF.registerAgent(SC, true, false);
	}

	private static boolean registerInteractive()
	{

//			StandardCommunicator SC = new StandardCommunicator("http://www.netdimes.org/DIMES/server", ConnectionHandlerFactory.NONSECURE_CONNECTION);

		RegistrationFrame RF = new RegistrationFrame(AFF, SC, false);
		RF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		RF.setSize(500, 350);
		RF.setVisible(true);
		
		return AFF.getRegistrationStatus();
	}
	
	public static StandardCommunicator getPropertiesUpdateCommunicator()
	{
		StandardCommunicator propertiesChangeComm = null;
		String propertiesUpdateURL;
//		String securePropertiesUpdateURL;
		
		// Get the secure url : property :
/*		try
		{
			securePropertiesUpdateURL = PropertiesBean.getProperty(PropertiesNames.SECURE_PROPERTIES_UPDATE_URL);
		}
		catch (NoSuchPropertyException e)
		{
			Loggers.getLogger().error("properties file incomplete. couldn't find securePropertiesUpdateURL");
			securePropertiesUpdateURL = "https://www.netdimes.org/DIMES/propertiesUpdate";
		}*/

		try
		{
			propertiesUpdateURL = PropertiesBean.getProperty(PropertiesNames.PROPERTIES_UPDATE_URL);
		}
		catch (NoSuchPropertyException e)
		{
			Loggers.getLogger().error("properties file incomplete. couldn't find propertiesUpdateURL");
			propertiesUpdateURL = "http://www.netdimes.org/DIMES/propertiesUpdate";
		}

//		// Create the Connection :
//		try
//		{
//			// secure connection :
//			propertiesChangeComm = new StandardCommunicator(securePropertiesUpdateURL, ConnectionHandlerFactory.SECURE_CONNECTION);
//		}
//		catch (MalformedURLException e)
//		{
			// On failure : try and generate a non-secure connection.
//			Loggers.getLogger().error("Could not initialize properties communicator :" + e.getMessage() + " . communicating threw Non-Secure connection.");
			try
			{
				propertiesChangeComm = new StandardCommunicator(propertiesUpdateURL, ConnectionHandlerFactory.NONSECURE_CONNECTION);
			}
			catch (MalformedURLException e)
			{
				Loggers.getLogger().error("Could not initialize properties communicator :" + e.getMessage() + " . Cennection will fail.");
			}
//		}
		
		return propertiesChangeComm;
	}
	
}
