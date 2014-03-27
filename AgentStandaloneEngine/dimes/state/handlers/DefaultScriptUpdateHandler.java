package dimes.state.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import dimes.Agent;
import dimes.comm2server.ConnectionHandlerFactory;
import dimes.comm2server.StandardCommunicator;
import dimes.util.FileHandlerBean;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * This class is handling the Default script update process.
 * It is part of the handlers systems and it taking the new default.in from the server and replacing
 * the old one.
 * 
 * The DefaultScriptUpdateHandler is being called when the KeepAliveHandler gets an 'UPDATE_DEFAULT_SCRIPT'
 * message from the server. Then it connects a secured servlet and getting the update.
 * 
 * @author idob
 * @since 0.5.0
 */
class DefaultScriptUpdateHandler {

	// logging
	private static final Logger logger = Loggers.getLogger(DefaultScriptUpdateHandler.class);
	
	private static final  String DEFAULT_IN_NAME = "default.in";
	
	Agent agent;
	
	// URL of servlet for getting updated default in script.
	private String scriptUpdateURL = null;
	
	// Connects to update default 
	private StandardCommunicator scriptUpdateComm = null;
	
	private FileHandlerBean fileHandler = new FileHandlerBean();
	
	File resourcesDir = null;
	
	DefaultScriptUpdateHandler(Agent theAgent) {
		String resourcesDirPath = null;
		try {
//			this.scriptUpdateURL = PropertiesBean.getProperty(PropertiesNames.SCRIPT_UPDATE_URL);
			this.scriptUpdateURL = PropertiesBean.getProperty(PropertiesNames.SECURE_SCRIPT_UPDATE_URL);
			resourcesDirPath = PropertiesBean.getProperty(PropertiesNames.RESOURCES_DIR);
			resourcesDir = new File(resourcesDirPath);
		} catch (NoSuchPropertyException e) {
			logger.warning("Properties file Corrupt : A Critical KeepAlive property missing.");
			logger.fine(e.getMessage());
		}
		
		try {
			this.scriptUpdateComm = new StandardCommunicator(scriptUpdateURL, ConnectionHandlerFactory.SECURE_CONNECTION);
//			this.scriptUpdateComm = new StandardCommunicator(scriptUpdateURL, ConnectionHandlerFactory.NONSECURE_CONNECTION);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.agent = theAgent;
	}
	
	void handleUpdateDefaultScriptResponse() {
		File newDefaultIn = null;
		try {
			newDefaultIn = sendRecieveDefaultScript();
		} catch (Exception e) {
			logger.warning(
					"Default Measurements Script can not be updated due to an error. Another try will be made later.");
			e.printStackTrace();
			return;
		}
		if( newDefaultIn == null || newDefaultIn.length() == 0 ) {
			logger.warning(
			"Default Measurements Script can not be updated through to an error. Another try will be made later.");
			if( newDefaultIn.exists() )
				newDefaultIn.delete();
			return;
		}
		File oldDefaultIn = new File(resourcesDir, DEFAULT_IN_NAME);
		if( oldDefaultIn.exists() ){
			logger.fine("Deleting old default.in");
			oldDefaultIn.delete();
		} else
			logger.fine("No old default.in exists");
		newDefaultIn.renameTo(oldDefaultIn);
		logger.info("New default measurements file was created. Will run in case of server crash.");
	}

	private File sendRecieveDefaultScript() throws Exception {
		File newDefaultIn = null;
		BufferedWriter outWriter = null;
		BufferedReader outReader = null;
		try {
			newDefaultIn = this.fileHandler.getDefaultIncomingSlot(resourcesDir);
			outWriter = new BufferedWriter(new FileWriter(newDefaultIn));
			String status = this.agent.getStatus().toXML();
			StringReader strReader = new StringReader(status);
			outReader = new BufferedReader(strReader);
			scriptUpdateComm.exchangeFiles(outReader, outWriter, this.agent.getAgentHeader(), this.agent.getAgentTrailer());
		} catch (Exception e) {
			if( newDefaultIn != null && newDefaultIn.exists() ) {
				newDefaultIn.delete();
			throw e;
			}
		}finally {
			IOUtils.closeQuietly(outWriter);
			IOUtils.closeQuietly(outReader);
		}
		return newDefaultIn;
	}
}
