/*package dimes.state.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;

import dimes.Agent;
import dimes.comm2server.ConnectionHandlerFactory;
import dimes.comm2server.StandardCommunicator;
import dimes.gui.PopUpWindow;
import dimes.gui.util.GuiUtils;
import dimes.state.UpdateVerifier;
import dimes.util.FileHandlerBean;
import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

class UpdateHandler {

	// logging
	private static final Logger logger = Loggers.getLogger(UpdateHandler.class);
	
	// URL of servlet for getting updated JAR.
	private String secureUpdateURL = null;
	
	// Reference to JAR dir.
	private File jarDir = null;

	public static final String RESTART_PROPERTY = "dimes.restart";
	
	// Connects to update servlet
	private StandardCommunicator updateComm = null;
	
	// For getting tmp file befaure creating the updated JAR.
	private FileHandlerBean fileHandler = null;
	
	Agent agent;
	
	UpdateHandler(Agent theAgent) {
		this.agent = theAgent;
		String jarPath = null;
		try
		{
//			this.secureUpdateURL = PropertiesBean.getProperty(PropertiesNames.SECURE_UPDATE_URL);
			this.secureUpdateURL = PropertiesBean.getProperty(PropertiesNames.UPDATE_URL);
			jarPath = PropertiesBean.getProperty(PropertiesNames.JAR_DIR);
		}
		catch (NoSuchPropertyException e)
		{
			logger.warning("Properties file Corrupt : A Critical update property is missing.");
			logger.info(e.getMessage());
		}
		this.jarDir = new File(jarPath);
		this.fileHandler = new FileHandlerBean();
		try {
//			updateComm = new StandardCommunicator(secureUpdateURL, ConnectionHandlerFactory.SECURE_CONNECTION);
			updateComm = new StandardCommunicator(secureUpdateURL, ConnectionHandlerFactory.NONSECURE_CONNECTION);
		} catch (MalformedURLException e) {
			logger.warning("Can not connect to Update server. Will try again later.");
			logger.fine(e.getMessage());
		}
	}

	private File sendRecieveUpdate() {
		String status = this.agent.getStatus().toXML();
		logger.finest("status is:\n\t" + status);//debug
		StringReader strReader = new StringReader(status);
		BufferedReader outReader = new BufferedReader(strReader);
		File response;
		try
		{
			response = this.fileHandler.getDefaultIncomingSlot(this.jarDir);
		}
		catch (Exception e1)
		{
			logger.warning("Error in Creating file : ");
			StackTraceElement[] elements = e1.getStackTrace();//debug
			String msg = e1.toString();
			for (int i = 0; i < elements.length; ++i)//debug
			{
				msg += elements[i].toString() + "\n";
			}
			logger.warning(msg);
			response = new File(this.jarDir, "Try" + String.valueOf(System.currentTimeMillis()) + ".jar");//todo - change
		}

		OutputStream incomingStream = null;

		try
		{
			incomingStream = new FileOutputStream(response);
			logger.fine("KAT - before exchangeFiles");//debug
			updateComm.exchangeFiles(outReader, incomingStream, this.agent.getAgentHeader(false), this.agent.getAgentTrailer());
			incomingStream.close();
			outReader.close();
			strReader.close();
			logger.fine("KAT - after exchangeFiles");//debug
			return response;
		}

		catch (Exception e)
		{
			// Close the InpuStream :			
			try
			{
				if (incomingStream != null)
					incomingStream.close();
				outReader.close();
				strReader.close();
				fileHandler.handleAfterUsage(response, false);
			}
			catch (Exception e3)
			{
				StackTraceElement[] elements = e3.getStackTrace();//debug
				String msg = e3.toString();
				for (int i = 0; i < elements.length; ++i)//debug
				{
					msg += elements[i].toString() + "\n";
				}
				logger.fine(msg);
			}
			logger.warning("KeepAlive Communication with Server failed: " + e.toString());
			e.printStackTrace();//debug
			this.agent.pauseAllTimers();//so that nothing happens until some option is picked
				JOptionPane.showMessageDialog(GuiUtils.getDialogFrame(true)getMainFrame(), "Update failed - exitting!", "DIMES Error",
						JOptionPane.ERROR_MESSAGE);
				response.renameTo(new File(response.getAbsolutePath() + ".unsafe")); //check            	
				stopLiving(false);
				return null;
		}
	
	}

	void handleUpdateResponse() {

		this.agent.pauseAllTimers();//so that nothing happens until some option is picked

		boolean automaticUpdate = false;
		try
		{
			automaticUpdate = Boolean.valueOf(PropertiesBean.getProperty(PropertiesNames.AUTOMATIC_UPDATE)).booleanValue();
		}
		catch (NoSuchPropertyException exp)
		{
			logger.warning("Could not find automatic updtae property : using default(false)");
			automaticUpdate = false;
		}
		// see if the automatic update flag is set :
		boolean updateAuthorized = false;
		if (!automaticUpdate)
		{
			// Ask the user for the update :
			int userChoice = JOptionPane.showConfirmDialog(GuiUtils.getMainFrame(), "A DIMES update is available. OK to update?", "DIMES Question",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (userChoice == JOptionPane.YES_OPTION)
			{
				logger.fine("user chose to update");//debug
				updateAuthorized = true;
			}
			else
			{
				// Try again , last time :
				logger.fine("user chose NOT to update - checking again...");//debug
				Object[] buttons = new Object[]{"Update", "Exit Agent"};
				this.agent.pauseAllTimers();//so that nothing happens
				// until some option is
				// picked
				int restartChoice = JOptionPane
						.showOptionDialog(
								GuiUtils.getDialogFrame(true) getMainFrame() ,
								"DIMES agent needs to be updated in order to function properly.\n"
										+ "If you do not wish to use the auto-update feature, please exit\nand download the latest version from our website:\n\twww.netDimes.org",
								"DIMES Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, buttons, buttons[0]);
				if (restartChoice == JOptionPane.YES_OPTION)
				{
					logger.fine("user chose to update");//debug
					updateAuthorized = true;
				}
				else
					updateAuthorized = false;
			}

		}
		else
		{
			updateAuthorized = true;
			PopUpWindow window = new PopUpWindow();
			JTextPane textPane = new JTextPane();
			textPane.setContentType("text/html");
			textPane.setText("<html><body>" + "<center><b>DIMES Agent will Auto update " + "<br> " + "in 3 seconds..." + "</b></center>"
					+ "</body></html>");
			textPane.setSize(250, 100);
			window.showMessage(textPane, 3);

		}
		
		File updatedJar = null;
		if (updateAuthorized)
		{
			logger.info("Getting update...");//debug
			updatedJar = this.sendRecieveUpdate();
			if ( updatedJar == null ){
				logger.warning("The updated JAR wasn't created properly. Please try again later.");
				PopUpWindow window = new PopUpWindow();
				JTextPane textPane = new JTextPane();
				textPane.setContentType("text/html");
				textPane.setText("<html><body>" + "<center><b>DIMES Agent Auto update " + "<br> " + "wasn't work" + "</b></center>"
						+ "</body></html>");
				textPane.setSize(250, 100);
				window.showMessage(textPane, 5);
			}
			else
				this.handleNewJar(updatedJar);
		}
		else
		{
			this.stopLiving(false);
		}	
	}

	public void handleNewJar(File response) {

		boolean automaticUpdate = false;
		try
		{
			automaticUpdate = Boolean.valueOf(PropertiesBean.getProperty(PropertiesNames.AUTOMATIC_UPDATE)).booleanValue();
		}
		catch (NoSuchPropertyException exp)
		{
			logger.warning("Could not find automatic updtae property : using default(false)");
			automaticUpdate = false;
		}
		boolean verified = false;
		try {
			verified = UpdateVerifier.verify(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if (verified) //JAR is verified -> new
		// update
		{
			logger.info("Updated JAR was verified");//debug
			File movedResponse = new File(this.jarDir, response.getName());//move JAR to update dir
			if (!response.renameTo(movedResponse))
				logger.info("couldn't rename " + response.getAbsolutePath() + " to " + movedResponse.getAbsolutePath());
			movedResponse = this.fileHandler.turnToActive(movedResponse);//remove .tmp
			movedResponse = this.fileHandler.removeExtension(movedResponse);//remove .in
			this.fileHandler.addExtension(movedResponse, ".jar");//add .jar so that AgentLauncher.getLatestJar will find this file

			this.agent.pauseAllTimers();
			if (!automaticUpdate)
				JOptionPane.showMessageDialog(GuiUtils.getDialogFrame(true)getMainFrame(), "Update succeeded - restarting agent...", "DIMES Message",
						JOptionPane.INFORMATION_MESSAGE);
			this.stopLiving(true);
		}
		else
		{
			logger.warning("Could not verify update JAR - " + "will try again on the next keepalive.");//debug
			this.agent.pauseAllTimers();//so that nothing happens until some option is picked
			if (!automaticUpdate)
				JOptionPane.showMessageDialog(GuiUtils.getDialogFrame(true)getMainFrame(), "Update failed - exitting!", "DIMES Error",
						JOptionPane.ERROR_MESSAGE);
			response.renameTo(new File(response.getAbsolutePath() + ".unsafe")); //check
			this.stopLiving(false);
		}
	
		
	}
	*//*********************  Agent control functions   ****************//*

	
	 * Stop the agent from living and set a property which indicates
	 * to the AutoUpdate mechanism whether to restart or not.
	 * 
	 * @param aRestart
	 
	private void stopLiving(boolean aRestart)
	{
		logger.fine("in stopLiving - restart is " + aRestart);//debug
		System.setProperty(RESTART_PROPERTY, String.valueOf(aRestart));
		this.agent.exit();
	}

}
*/