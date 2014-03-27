package dimes.state.handlers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import dimes.Agent;
import dimes.comm2server.ConnectionHandlerFactory;
import dimes.comm2server.StandardCommunicator;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.XMLUtil;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

public class KeepAliveHandler {
	
		private final String ACK = "ACK";
		private final String UPDATE = "UPDATE";
		private final String METAOP = "METAOP";
		private final String UPDATE_DEFAULT_SCRIPT = "UPDATE_DEFAULT_SCRIPT";
	
		private static final Logger logger = Loggers.getLogger(KeepAliveHandler.class);
		private static boolean keepaliveSuccess;
		private Agent agent;
		
		// URL of servlet for KeepAlive response.
		private String keepAliveURL = null;
		
		// Connects to keepalive servlet
		private StandardCommunicator keepAliveComm = null;
		
		public KeepAliveHandler(Agent agent){
			this.agent = agent;
			try {
//				this.keepAliveURL = PropertiesBean.getProperty(PropertiesNames.SECURE_KEEPALIVE_URL);
				this.keepAliveURL = PropertiesBean.getProperty(PropertiesNames.KEEPALIVE_URL);
			} catch (NoSuchPropertyException e) {
				logger.warning("Properties file Corrupt : A Critical KeepAlive property missing : " + e.getMessage());
			}
			try {
//				this.keepAliveComm = new StandardCommunicator(keepAliveURL, ConnectionHandlerFactory.SECURE_CONNECTION);
				this.keepAliveComm = new StandardCommunicator(keepAliveURL, ConnectionHandlerFactory.NONSECURE_CONNECTION);
			} catch (MalformedURLException e) {
				logger.warning("Can not connect to KeepAlive server. Will try again later.");
				logger.fine(e.getMessage());
			}
		}
		
		public void sendReceiveKeepAlive() { 
			String status = this.agent.getStatus().toXML();
			StringReader strReader = new StringReader(status);
			BufferedReader outReader = new BufferedReader(strReader);
			logger.fine("status is:\n\t" + status);//debug
			ByteArrayOutputStream incomingStream = new ByteArrayOutputStream();
			String incomingMessage = null;
			try {
				keepAliveComm.exchangeFiles(outReader, incomingStream, this.agent.getAgentHeader(false), this.agent.getAgentTrailer());
				incomingMessage = incomingStream.toString();
				incomingStream.close();
				outReader.close();
				strReader.close();
				logger.fine("KAT - after exchangeFiles");//debug
				this.handleKeepAliveReponse(incomingMessage);
				keepaliveSuccess=true;
			} catch (IOException e) {
				logger.warning("KeepAlive Message was not sent to the Server - \n Check Internet connection and Proxy settings.");
				keepaliveSuccess=false;
			} catch (NoSuchPropertyException e) {
				logger.warning("KeepAlive Message was not sent to the Server - \n Properties file is corrupted.");
				keepaliveSuccess=false;
			}
		}
		
		public static boolean getKeepAliveSuccess(){
			return keepaliveSuccess;
		}
		
		private void handleKeepAliveReponse(String incomingMessage) {
			String trimmedMessage;
			String routableIp="";
			if(incomingMessage.contains("<keep")){
				Element keepAliveElement = XMLUtil.getRootElement(incomingMessage);
				routableIp = keepAliveElement.getAttribute("routable-ip");			
				trimmedMessage = keepAliveElement.getAttribute("message");//incomingMessage.trim();
				}else trimmedMessage = incomingMessage.trim();
			
			// for ACK response : 
			if ( trimmedMessage.equals(ACK) )
			{
				logger.finest("got ACK msg");//debug
			if(!"".equals(routableIp)){
					System.out.println("got "+trimmedMessage+" and IP: "+routableIp);
					try {
						PropertiesBean.setProperty(PropertiesNames.IP, routableIp);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return;
			}
			// MetaOP response : 
			if ( trimmedMessage.contains(METAOP) )
			{
				/**
				 * METAOP is disabled right now (June 2009) so this code is commented-out
				 * If the Agent DOES receive a METAOP message, we ask the user to post it
				 * on the forum, since it means that it's not off the way we think. - BoazH
				 */
//				MetaOPHandler metaOPHandler = new MetaOPHandler(this.agent);
//				try {
//					metaOPHandler.handleKeepMetaOpAliveReponse(trimmedMessage);
//				} catch (IOException e) {
//					logger.warning("There is an IO problem. Can't finish Agent update.");
//				} catch (NoSuchPropertyException e) {
//					logger.warning("Please check possibility of corruption of your Properties file.");
//				}
				logger.severe("METAOP message recived. \n METAOP has been disabled on this version of the Agent\n"
						+ "Please post the following message on the DIMES forum to let us know you encountered this:\n "+
						"METAOP message received by Agent "+agent.getStatus().toXML());
				return;
			}

			
			/**KeepAlive Update handling disabled for now (July 09)
			 * Will hopefully make it into this version of the Agent (0.6)
			 * but in a new implementation.
			 * 
			 */
//			// Update response :
//			if ( trimmedMessage.equals(UPDATE) )
//			{
//				logger.fine("got UPDATE msg");//debug
//				UpdateHandler updateHandler = new UpdateHandler(this.agent);
//				updateHandler.handleUpdateResponse();
//			}
			// Update default in script response:
			if ( trimmedMessage.equals(UPDATE_DEFAULT_SCRIPT) ) {
				logger.fine( "got UPDATE_DEFAULT_SCRIPT message" );//debug
				DefaultScriptUpdateHandler defaultScriptUpdateHandler = new DefaultScriptUpdateHandler(agent);
				defaultScriptUpdateHandler.handleUpdateDefaultScriptResponse();
			}

		}

}
