package dimes.state.handlers;
/**Part of the Handling for METAOP operations via keepalive. As of right now (June 2009)
 * METAOP is broken, so this system is being disabled rather than re-written. If and when
 * we ever re-enable this in the server, this class need to be re-written. - BoazH
 * 
 */

//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.StringReader;
//import java.util.logging.Logger;
//
//import dimes.Agent;
//import dimes.state.user.PropertiesDetails;
//import dimes.util.logging.Loggers;
//import dimes.util.properties.PropertiesBean.NoSuchPropertyException;
//
//class MetaOPHandler {	
//
//	private static final Logger logger = Loggers.getLogger(MetaOPHandler.class);
//	private Agent agent;
//	
//	MetaOPHandler(Agent theAgent) {
//		this.agent = theAgent;
//	}
//	/**
//	 * 
//	 * @param trimmedMessage
//	 * @param keepmetaopalive
//	 *            signals whether to treat the response file as a keepalive
//	 *            reponse (ACK or UPDATE) or as an update JAR.
//	 * @throws NoSuchPropertyException
//	 * @throws IOException
//	 * @throws Exception
//	 */
//	void handleKeepMetaOpAliveReponse(String trimmedMessage) throws IOException, NoSuchPropertyException
//	{
//		// Handle Metaop strings
//		String metaopLine = null;
//		String typeLine = null;
//		String opLine = null;
//		String parmLine = null;
//
//			StringReader reader = new StringReader(trimmedMessage);
//			BufferedReader myInput = new BufferedReader(reader);
//			// Read expected lines
//			while ((metaopLine = myInput.readLine()) != null)
//			{
//				System.out.println("Metaop line : " + metaopLine);
//				String[] parameters = metaopLine.split("\\s");
//				// Check parameters :
//				if (parameters.length < 2)
//				{
//					logger.warning("Not Enough paraters for Meta Command :" + parameters.length + "(" + metaopLine + ")");
//					return;
//				}
//
//				if (parameters.length >= 2)
//					typeLine = parameters[1];
//				if (parameters.length >= 3)
//					opLine = parameters[2];
//				if (parameters.length >= 4)
//					parmLine = parameters[3];
//
//				if (typeLine != null)
//				{
//					logger.fine("Got an operation data from server. Operation type is: " + typeLine);
//					// Set the properties in the properties file
//					if (typeLine.equalsIgnoreCase("set"))
//					{
//						logger.info("Setting parameter:" + opLine + " " + parmLine);
//						PropertiesDetails pd = new PropertiesDetails(opLine, parmLine);
//						this.agent.updatePropertiesFile(pd);
//					}
//					//Pause all mesurments: scheduler, keepalive and
//					// keepmetaopkeepalive tasks
//					else
//						if (typeLine.equalsIgnoreCase("stop"))
//						{
//							logger.info("Pausing Agent.");
//							this.agent.pauseSchedTimer();
//						}
//						// Resume all mesurments: scheduler, keepalive and
//						// keepmetaopkeepalive tasks
//						else
//							if (typeLine.equalsIgnoreCase("start"))
//							{
//								logger.info("Resuming Agent.");
//								this.agent.resumeSchedTimer();
//							}
//							// Exit the agent
//							else
//								if (typeLine.equalsIgnoreCase("exit"))
//								{
//									logger.warning("Agent exited by external Meta operation.");
//									this.agent.exit();
//									return;
//								}
//								// Start the agent
//								else
//									if (typeLine.equalsIgnoreCase("restart"))
//									{
//										/* 
//										 * Order the agent to restart
//										 * Note that this restarts the agent from within AppSplash class
//										 * and doesn't cause the agent to restart using the AutoUpdate 
//										 * mechanism. 
//										 */
//										this.agent.pauseAllTimers();
//										this.agent.restart();
//										return;
//									}
//									else
//										logger.warning("Unknow command returned form server (" + typeLine + ")");
//				}
//				else
//				{
//					logger.warning("Got wrong format of METAOP Command (" + metaopLine + ")");
//				}
//			}
//	}
//}
