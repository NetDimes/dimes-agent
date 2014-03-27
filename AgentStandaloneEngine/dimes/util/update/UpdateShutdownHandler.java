package dimes.util.update;

import java.net.MalformedURLException;

import dimes.NonGuiAgent;
import dimes.Agent;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * Handles shutting down the Agent and GUI, and starting the update program. 
 * 
 * @author BoazH
 *
 */
public abstract class UpdateShutdownHandler {

	static Agent agent=null;
	
	public static void updateUpdate(){
		Thread updateThread = new updateUpdater();
		updateThread.start();
	}
	
	
	
	/**
	 * A Thread to update the update program. This is the only type of update
	 * that doesn't involve shutting down the Agent. 
	 *
	 */
	static class updateUpdater extends Thread{
		
	}
	
	public static void HandleShutdown(){
		try {
			agent = NonGuiAgent.getInstance();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		agent.exit(127);
	}
	
}
