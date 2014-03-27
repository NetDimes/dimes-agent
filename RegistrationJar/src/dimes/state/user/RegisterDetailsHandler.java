/*
 * Created on 27/02/2005
 *
 */
package dimes.state.user;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

//import org.dom4j.Document;
//import org.dom4j.DocumentException;
//import org.dom4j.Element;
//import org.dom4j.io.SAXReader;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dimes.comm2server.ConnectionException;
import dimes.comm2server.StandardCommunicator;
//import dimes.measurements.Measurements;
import dimes.util.IPUtils;
import dimes.platform.PlatformDependencies;
import dimes.state.NetworkInfo;
import dimes.util.FileHandlerBean;
import dimes.util.XMLUtil;
import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * @author Ohad Serfaty, idob (ver 0.5.0)
 *
 * allows the user to register as a new user or a veteran user
 * all methods return a RegistrationStatus object .
 * 
 * Changes in version 0.5.0:
 * <ol>
 * <li>Changing server response from being saved in a file to being saved in a String</li> 
 * <li>Adding sending the Agent's version to be registered in the server side - to allow multiple versions support</li>
 * </ol>
 */
public class RegisterDetailsHandler
{
	private static final String DEFAULT_AGENT_VERSION = "0.6.0";
	StandardCommunicator comm;
	FileHandlerBean handler = new FileHandlerBean();
	Thread currentThread;

	private Logger logger;
	
	// Agent Version: to be registered by the server.
	private String agentVersion;

	public RegisterDetailsHandler(StandardCommunicator aComm)
	{
		comm = aComm;
		this.logger = Loggers.getLogger(this.getClass());
		try {
			agentVersion = PropertiesBean.getProperty(PropertiesNames.AGENT_VERSION);
		} catch (NoSuchPropertyException e) {
			agentVersion = DEFAULT_AGENT_VERSION;
			e.printStackTrace();
		}
	}

	public RegistrationStatus registerAgentToUsersGroup(String userName, String agentName, String ip, String mac, String groupName)
	{
		currentThread = Thread.currentThread();
		RegistrationStatus result = new RegistrationStatus();
		if (userName == null || userName.equals(""))
		{
			result.errorMessage = "Please provide a User Name";
			return result;
		}
/**		
 		The registration XML looks like this:
 
  		<register-details>
			<user> 
				<name>userName</name>
			</user>
			<agent>
				<agentName>agentName</agentName>
				<macAddress>mac</macAdress>
				<IP>ip</IP>
				<platform>PlatformDependencies.PLATFORM</platform>
				<version>agentVersion</version>
				<groupID>groupName</groupID>
			</agent> 
		</register-details>
*/
		// create the registration string :
		// 0.5.1 Changed from String to StringBuffer to avoid creating so many objects
		StringBuffer registrationString = new StringBuffer("\t<register-details>\n");
//		registrationString.append("\t\t<add-agent-to-users-group>" + groupName+ "</add-agent-to-users-group>\n");
		registrationString.append("\t\t<user>\n");

		// write the user details :
		registrationString.append("\t\t\t<name>" + userName + "</name>\n");
//		registrationString.append(password != null ? "\t\t\t<password>" + password + "</password>\n" : "");
		registrationString.append("\t\t</user>\n");

		// Write the agent details :        
		registrationString.append("\t\t<agent>\n");
		registrationString.append("\t\t\t<agentName>"+agentName+"</agentName>\n");
		registrationString.append("\t\t\t<macAddress>" + mac + "</macAddress>\n");
		registrationString.append("\t\t\t<IP>" + ip + "</IP>\n");
		registrationString.append("\t\t\t<platform>" + PlatformDependencies.PLATFORM + "</platform>\n");
		/*
		 *  Ido:
		 *	Added in version 0.5.0: sending the Agent's specific version to be registered by the server - 
		 *  to allow multiple versions registration. This way we can separate the new 0.5.0 QBE Agents from
		 *  the old ones. 
		 */
		registrationString.append("\t\t\t<version>" + agentVersion + "</version>\n");
		registrationString.append("\t\t\t<groupID>" + groupName + "</groupID>\n");
		registrationString.append("\t\t</agent>\n");
		registrationString.append("\t</register-details>\n");

		String test = registrationString.toString(); //Debug. TODO: remove
		return this.register(registrationString.toString());
	}

	
	/***************
	 * communicate with the server and send the string.
	 * waits for the result and parses it.
	 * 
	 * @param registrationFileString - the registration string.
	 * @return a registration status 
	 */
	public RegistrationStatus register(String registrationFileString)
	{
		RegistrationStatus result = new RegistrationStatus();
		StringWriter serverResponse = new StringWriter();

		try
		{
			BufferedReader regStrBuffer = new BufferedReader(new StringReader(registrationFileString));
			BufferedWriter incomingWriter = new BufferedWriter( serverResponse );

			logger.debug("Sending: " + registrationFileString);

			// exchange files :		
			comm.exchangeFiles(regStrBuffer, incomingWriter, "<agent>", "</agent>");
		}
		catch (ConnectionException e)
		{
			Loggers.getLogger().warn("Connect Error while connecting server : " + e.getMessage());
			StackTraceElement[] stack = e.getStackTrace();
			for (int i = 0; i < stack.length; i++)
				Loggers.getLogger().warn(stack[i].toString());
			result.communicationError = true;
			result.errorMessage = "Connect Error while connecting server : \n" + e.getMessage();
			return result;
		}
		catch (IOException e)
		{
			Loggers.getLogger().warn("IO Error while connecting server : " + e.getMessage());
			StackTraceElement[] stack = e.getStackTrace();
			for (int i = 0; i < stack.length; i++)
				Loggers.getLogger().warn(stack[i].toString());
			result.communicationError = true;
			result.errorMessage = "IO Error while connecting server : \n" + e.getMessage();
			return result;
		}
		catch (Exception e)
		{
			Loggers.getLogger().warn("General Error while connecting server : " + e.getMessage());
			StackTraceElement[] stack = e.getStackTrace();
			for (int i = 0; i < stack.length; i++)
				Loggers.getLogger().warn(stack[i].toString());
			result.communicationError = true;
			result.errorMessage = "General Error while connecting server : \n" + e.getMessage();
			return result;
		}

		try
		{
//			SAXReader iregDocReader = new SAXReader();
//			Document regDoc = iregDocReader.read( new StringReader(serverResponse.toString()) );
			Element root = XMLUtil.getRootElement(serverResponse.toString());
			if (root== null)
			{
				result.internalServerError = true;
				return result;
			}

			if (XMLUtil.getChildElementByName(root, "ACK") != null)
			{
				readRegistrationToResult(root, result);
				return result;
			}

			if (XMLUtil.getChildElementByName(root, "NACK") != null)
			{
//				if(ignorewarn) readRegistrationToResult(root, result);
//				else{
				result.errorMessage = XMLUtil.getChildElementByName(root, "NACK").getTextContent();// regDoc.getRootElement().elementText("NACK");
				JOptionPane.showMessageDialog(null, result.errorMessage);
//				}
				return result;
			}
		}
//		catch (DocumentException docEx)
//		{
//			Loggers.getLogger().warn("Error while reading server response: \n" + docEx.getMessage());
//			StackTraceElement[] stack = docEx.getStackTrace();
//			for (int i = 0; i < stack.length; i++)
//				Loggers.getLogger().warn(stack[i].toString());
//			result.internalServerError = true;
//			result.errorMessage = "Error while reading server response: \n" + docEx.getMessage();
//		}
		catch (Exception e)
		{
			Loggers.getLogger().warn("General Error while reading server response: \n" + e.getMessage());
			StackTraceElement[] stack = e.getStackTrace();
			for (int i = 0; i < stack.length; i++)
				Loggers.getLogger().warn(stack[i].toString());
			result.internalServerError = true;
			result.errorMessage = "Error while reading server response: \n" + e.getMessage();
		}

		return result;
	}

	/**********************
	 * register a new user
	 * 
	 * @param userName
	 * @param country
	 * @param email
	 * @param password
	 * @return a registration status object.
	 */
	public RegistrationStatus registerNewUser(String userName, String country, String email, String password)
	{
		currentThread = Thread.currentThread();
		RegistrationStatus result = new RegistrationStatus();
		if (userName == null || userName.equals(""))
		{
			result.errorMessage = "Please provide a User Name";
			return result;
		}

		// find IP :
		String ip = "127.0.0.1";
		String mac = "none";
		try
		{
			//            ip = InetAddress.getLocalHost().getHostAddress();
			ip = IPUtils.getHostIP();
		}
		catch (UnknownHostException e)
		{
		}

		// find mac :
		try
		{
			mac = NetworkInfo.getMacAddress();
		}
		catch (IOException e1)
		{
		}

		//0.5.1 registrationString turned into a StringBuffer to avoid creating so many objects - bh
		// create the registration string :
		StringBuffer registrationString = new StringBuffer("\t<register-details>\n");
		registrationString.append("\t\t<user>\n");

		// write the user details :
		registrationString.append("\t\t\t<name>" + userName + "</name>\n");
		registrationString.append(country != null ? "\t\t\t<country>" + country + "</country>\n" : "");
		registrationString.append(email != null ? "\t\t\t<email>" + email + "</email>\n" : "");
		registrationString.append(password != null ? "\t\t\t<password>" + password + "</password>\n" : "");
		registrationString.append("\t\t</user>\n");

		// Write the agent details :        
		registrationString.append("\t\t<agent>\n");
		registrationString.append("\t\t\t<macAddress>" + mac + "</macAddress>\n");
		registrationString.append("\t\t\t<IP>" + ip + "</IP>\n");
		registrationString.append("\t\t\t<platform>" + PlatformDependencies.PLATFORM + "</platform>\n");
		/*
		 *  Ido:
		 *	Added in version 0.5.0: sending the Agent's specific version to be registered by the server - 
		 *  to allow multiple versions registration. This way we can separate the new 0.5.0 QBE Agents from
		 *  the old ones. 
		 */
		registrationString.append("\t\t\t<version>" + agentVersion + "</version>\n");
		registrationString.append("\t\t</agent>\n");
		registrationString.append("\t</register-details>\n");

		System.out.println("Regestration String For New User:\n"+registrationString.toString());
		return this.register(registrationString.toString());
	}

	/********************
	 * register an agent to a user group
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	public RegistrationStatus registerAgentToUsersGroup(String userName, String password)
	{
		currentThread = Thread.currentThread();
		RegistrationStatus result = new RegistrationStatus();
		if (userName == null || userName.equals(""))
		{
			result.errorMessage = "Please provide a User Name";
			return result;
		}

		//  find IP :
		String ip = "127.0.0.1";
		String mac = "none";
		try
		{
			//        ip = InetAddress.getLocalHost().getHostAddress();
			ip = IPUtils.getHostIP();
		}
		catch (UnknownHostException e)
		{
		}

		// find mac :
		try
		{
			mac = NetworkInfo.getMacAddress();
		}
		catch (IOException e1)
		{
		}

		// create the registration string :		
		StringBuffer registrationString = new StringBuffer("\t<register-details>\n");
		registrationString.append("\t\t<add-agent-to-users-group/>\n");
		registrationString.append("\t\t<user>\n");

		// write the user details :
		registrationString.append("\t\t\t<name>" + userName + "</name>\n");
		registrationString.append(password != null ? "\t\t\t<password>" + password + "</password>\n" : "");
		registrationString.append("\t\t</user>\n");

		// Write the agent details :        
		registrationString.append("\t\t<agent>\n");
		registrationString.append("\t\t\t<macAddress>" + mac + "</macAddress>\n");
		registrationString.append("\t\t\t<IP>" + ip + "</IP>\n");
		registrationString.append("\t\t\t<platform>" + PlatformDependencies.PLATFORM + "</platform>\n");
		/*
		 *  Ido:
		 *	Added in version 0.5.0: sending the Agent's specific version to be registered by the server - 
		 *  to allow multiple versions registration. This way we can separate the new 0.5.0 QBE Agents from
		 *  the old ones. 
		 */
		registrationString.append("\t\t\t<version>" + agentVersion + "</version>\n");
		registrationString.append("\t\t</agent>\n");
		registrationString.append("\t</register-details>\n");

		return this.register(registrationString.toString());
	}

	/*******************
	 * parse the registration result file into a RegistrationStatus object.
	 * note that this function changes the object given to it, 
	 * and not returning it, to maintain this object's previous
	 * measures.
	 *  
	 * @param regDoc
	 * @param result
	 */
	private void readRegistrationToResult(Element regDoc, RegistrationStatus result)
	{
		Element ackElement = XMLUtil.getChildElementByName(regDoc, "ACK");
		Element userElement = XMLUtil.getChildElementByName(ackElement,"user");
		List<Node> agentElements =XMLUtil.getNodeListAsList(XMLUtil.getChildNodesNoWS(ackElement));//.getRootElement().element("ACK").elements("agent");

		/*
		 * <agent>
	<query-details>
		<get-agent-list-for-user/>
		<user>
			<name>BoazH</name>
			<platform>WIN</platform>
			<password>BoazH</password>
		</user>
	</query-details>
	<query-details>		<get-agent-list-for-user/>		<user>			<name>BoazH</name>			<platform>WIN</platform>			<password>BoazH</password>		</user>	</query-details>
</agent>
		 */
		
		result.userName = safeGetTextContent(XMLUtil.getChildElementByName(userElement, "name"));//.elementText("name");
		result.country = safeGetTextContent(XMLUtil.getChildElementByName(userElement, "country"));//userElement.elementText("country");
		result.email = safeGetTextContent(XMLUtil.getChildElementByName(userElement, "email"));//userElement.elementText("email");
		result.groupName = safeGetTextContent(XMLUtil.getChildElementByName(userElement, "group"));//userElement.elementText("group");
		result.groupOwner = safeGetTextContent(XMLUtil.getChildElementByName(userElement, "groupOwner"));//userElement.elementText("groupOwner");
		result.hasPassword = safeGetTextContent((XMLUtil.getChildElementByName(userElement, "password"))).equals("") ? null : "true";//(userElement.element("password") == null) ? null : "true";

		result.agentNames = new Vector();
		for(Node n:agentElements){
			if (n.getNodeName().equals("agent"))
			{
				Element name = XMLUtil.getChildElementByName((Element)n, "ID");
				result.agentNames.add(name.getTextContent());
			}
		}
//		for (int i = 0; i < agentElements.size(); ++i)
//		{
//			Element name = ((Element) agentElements.get(i)).element("ID");
//			result.agentNames.add(name.getText());
//		}
	}
	
	private String safeGetTextContent(Element e ){
		String text="";
		try{
			text=e.getTextContent();
		}catch(Exception ex){
		 
		}
		return text;
	}

	/**
	 * returns agent list belonging to userName, if exists
	 * @param userName
	 * @param password
	 * @return
	 */
	public RegistrationStatus retrieveAgentsForExistingUser(String userName, String password)
	{
		currentThread = Thread.currentThread();
		RegistrationStatus result = new RegistrationStatus();
		if (userName == null || userName.equals(""))
		{
			result.errorMessage = "Please provide a User Name";
			return result;
		}
		if (password == null || password.equals(""))
		{
			result.errorMessage = "Please provide a password";
			return result;
		}

		// create the registration string :
		// 0.5.1 Changed from String to StringBuffer to avoid creating so many objects
		StringBuffer registrationString = new StringBuffer("\t<query-details>\n");
		registrationString.append("\t\t<get-agent-list-for-user/>\n");
		registrationString.append("\t\t<user>\n");

		// write the user details :
		registrationString.append("\t\t\t<name>" + userName + "</name>\n");
		registrationString.append("\t\t\t<platform>" + PlatformDependencies.PLATFORM + "</platform>\n");
		registrationString.append(password != null ? "\t\t\t<password>" + password + "</password>\n" : "");
		registrationString.append("\t\t</user>\n");

		registrationString.append("\t</query-details>\n");

		result = this.register(registrationString.toString());

		/*System.out.println*/logger.debug("retrieveAgentsForExistingUser: returning result: \n" + result);//debug
		return result;

	}

	/**
	 * @param userName
	 * @param agentName
	 * @param ignorewarn
	 * @return
	 */
	public RegistrationStatus registerExistingAgent(String agentName, boolean ignorewarn)
	{
		
		if(ignorewarn){ 
			System.out.println("quite un: "+quietUnregister(agentName));
		}
		/*System.out.println*/logger.debug("Registering existing agent: " + agentName);//debug
		currentThread = Thread.currentThread();
		RegistrationStatus result = new RegistrationStatus();
		if (agentName == null || agentName.equals(""))
		{
			result.errorMessage = "Please provide an agent name";
			return result;
		}

		// create the registration string :
		StringBuffer registrationString = new StringBuffer("\t<update-details>\n");
		registrationString.append("\t\t<update-agent-registration ignorewarn=\"" + String.valueOf(ignorewarn) + "\"/>\n");

		// Write the agent details :        
		registrationString.append("\t\t<agent>\n");
		registrationString.append("\t\t\t<name>" + agentName + "</name>\n");
		registrationString.append("\t\t\t<platform>" + PlatformDependencies.PLATFORM + "</platform>\n");
		/*
		 *  Ido:
		 *	Added in version 0.5.0: sending the Agent's specific version to be registered by the server - 
		 *  to allow multiple versions registration. This way we can separate the new 0.5.0 QBE Agents from
		 *  the old ones. 
		 */
		registrationString.append("\t\t\t<version>" + agentVersion + "</version>\n");
		registrationString.append("\t\t</agent>\n");

		registrationString.append("\t</update-details>\n");

		return this.register(registrationString.toString());
	}

	private boolean quietUnregister(String agentName){	


		try {
		URL serverURL = new URL("http://www.netdimes.org/DIMES/notifyUninstall?agentName="+agentName);
		InputStream in = serverURL.openStream();
		HttpURLConnection UC = (HttpURLConnection) serverURL.openConnection();
		if (UC.getResponseCode() == HttpURLConnection.HTTP_OK)
			UC.disconnect();

		return true;
		
		} catch (Exception e) {
	
			e.printStackTrace();
		}
		
		return false;
	}
}