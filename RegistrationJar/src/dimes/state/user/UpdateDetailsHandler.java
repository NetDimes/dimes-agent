/* Created on 27/02/2005
 *
 */
package dimes.state.user;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import org.apache.log4j.Logger;

//import org.dom4j.Document;
//import org.dom4j.DocumentException;
//import org.dom4j.Element;
//import org.dom4j.io.SAXReader;

import org.w3c.dom.Element;

import dimes.comm2server.StandardCommunicator;
import dimes.util.XMLUtil;
import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * @author Ohad Serfaty
 *
 * allows the user to change the following properties :
 * 1. username, country and email.
 * 2. password
 * 3. join group
 * 4. create group.
 * 5.agent name
 * 
 * all methods return an UpdateDetailsStatus.
 */
public class UpdateDetailsHandler
{

	private StandardCommunicator comm;

	private Logger logger;
	
	private  String currentAgentName="";

	public UpdateDetailsHandler(StandardCommunicator aComm)
	{
		comm = aComm;
		this.logger = Loggers.getLogger(this.getClass());
	}

	/*************
	 * change a password
	 * 
	 * @param currPassword
	 * @param newPdw
	 * @return
	 */
	public PropertiesStatus changePassword(String currPassword, String newPdw)
	{

		PropertiesStatus request = new UpdateDetailsStatus();
		request.oldPassword = currPassword;
		request.newPassword = newPdw;

		PropertiesStatus stauts = this.generalUpdate(request);
		return stauts;
	}

	//    /*****************
	//     * change some of the users details
	//     * 
	//     * @param userName
	//     * @param country
	//     * @param email
	//     * @return
	//     */
	//    public UpdateDetailsStatus changeUserDetails(String userName, 
	//            String country, String email , String agentName){
	//        String currentAgentName=null;
	//        try 
	//        {
	//            currentAgentName = PropertiesBean.getProperty("agentName");
	//        }
	//        catch (NoSuchPropertyException e) 
	//        {
	//            UpdateDetailsStatus result = new UpdateDetailsStatus();
	//            result.errorMessage = " Properties file corrupt. please check properties.xml";
	//            return result;
	//        }
	//        
	//        String header = "<agent name=\""+currentAgentName+"\">";
	//        String trailer = "</agent>";
	//        
	//        String registrationString = "\t<update-details>\n";
	//        // Write the agent details :        
	//        registrationString+="\t\t<user>\n";
	//        registrationString+=isValidValue(userName)?"\t\t\t<name>" +userName+ "</name>\n":"";
	//        registrationString+=(country != null)?"\t\t\t<country>" +country+ "</country>\n":"";
	//        registrationString+=isValidValue(email)?"\t\t\t<email>" +email+ "</email>\n":"";
	//        registrationString+="\t\t</user>\n";      
	//        
	//        System.out.println("Agent name is : " + agentName);
	//        
	//        if (agentName != null)
	//        {
	//            registrationString+="\t\t<agent>\n";
	//            registrationString+="\t\t\t<name>" +agentName+ "</name>\n";
	//            registrationString+="\t\t</agent>\n";        
	//        }
	//        
	//        registrationString+="\t</update-details>\n"; 
	//        
	//        return updateDetails(registrationString,header,trailer);
	//    }
	//    
	//    
	/**
	 * @param country
	 * @return
	 */
	private boolean isValidValue(String string)
	{
		if (string == null)
			return false;
		return (PropertiesBean.isValidValue(string) && !(string.equals("")));
	}

	/*******************
	 * join a group
	 * 
	 * @param groupName
	 * @return
	 */
	//    public UpdateDetailsStatus joinGroup(String groupName){
	//        String currentAgentName=null;
	//        try 
	//        {
	//            currentAgentName = PropertiesBean.getProperty("agentName");
	//        }
	//        catch (NoSuchPropertyException e) 
	//        {
	//            UpdateDetailsStatus result = new UpdateDetailsStatus();
	//            result.errorMessage = " Properties file corrupt. please check properties.xml";
	//            return result;
	//        }
	//        
	//        String header = "<agent name=\""+currentAgentName+"\">";
	//        String trailer = "</agent>";
	//        
	//        String registrationString = "\t<update-details>\n";
	//        // Write the agent details :        
	//        registrationString+="\t\t<user>\n";
	//        registrationString+="\t\t\t<group action=\"join\">" +groupName+ "</group>\n";      
	//        registrationString+="\t\t</user>\n";        
	//        registrationString+="\t</update-details>\n"; 
	//        
	//        return updateDetails(registrationString,header,trailer);
	//    }
	public PropertiesStatus changeAutoUpdate(String anAutoUpdate)
	{
		PropertiesStatus details = new UpdateDetailsStatus();
		details.autoUpdate = anAutoUpdate;
		return generalUpdate(details);

	}

	public PropertiesStatus joinGroup(String groupName)
	{
		PropertiesStatus details = new UpdateDetailsStatus();
		details.groupName = groupName;
		details.groupAction = "join";
		return this.generalUpdate(details);
	}

	public PropertiesStatus createGroup(String groupName)
	{
		PropertiesStatus details = new UpdateDetailsStatus();
		details.groupName = groupName;
		details.groupAction = "create";
		return this.generalUpdate(details);
	}

	/*****************
	 * change some of the users details
	 * 
	 * @param userName
	 * @param country
	 * @param email
	 * @return
	 */
	public PropertiesStatus changeUserDetails(String userName, String country, String email, String agentName)
	{
		PropertiesStatus details = new UpdateDetailsStatus();
		details.userName = userName;
		details.country = country;
		details.email = email;
		details.agentName = agentName;

		return this.generalUpdate(details);
	}

	//    public UpdateDetailsStatus changeAgentName(String currentAgentName,String agentName){
	//        UpdateDetailsStatus details = new UpdateDetailsStatus();
	//        details.agentName = agentName;
	//        return this.generalUpdate(details);
	//    }

	//    public UpdateDetailsStatus createGroup(String groupName){
	//        String currentAgentName=null;
	//        try 
	//        {
	//            currentAgentName = PropertiesBean.getProperty("agentName");
	//        }
	//        catch (NoSuchPropertyException e) 
	//        {
	//            UpdateDetailsStatus result = new UpdateDetailsStatus();
	//            result.errorMessage = " Properties file corrupt. please check properties.xml";
	//            return result;
	//        }
	//        
	//        String header = "<agent name=\""+currentAgentName+"\">";
	//        String trailer = "</agent>";
	//        
	//        String registrationString = "\t<update-details>\n";
	//        // Write the agent details :        
	//        registrationString+="\t\t<user>\n";
	//        registrationString+="\t\t\t<group action=\"create\">" +groupName+ "</group>\n";      
	//        registrationString+="\t\t</user>\n";        
	//        registrationString+="\t</update-details>\n"; 
	//        
	//        return updateDetails(registrationString,header,trailer);
	//    }

	public PropertiesStatus changeAgentName(String currentName, String agentName)
	{

		//        String header = "<agent name=\""+currentAgentName+"\">";
		//        String trailer = "</agent>";
		//        
		//        String registrationString = "\t<update-details>\n";
		//        // Write the agent details :        
		//        registrationString+="\t\t<agent>\n";
		//        registrationString+="\t\t\t<name>" +agentName+ "</name>\n";
		//        registrationString+="\t\t</agent>\n";        
		//        registrationString+="\t</update-details>\n"; 
		currentAgentName  = currentName;
		PropertiesStatus details = new UpdateDetailsStatus();
		details.agentName = agentName;

		PropertiesStatus status = generalUpdate(details);
		return status;
	}

	//    /**
	// * @param details
	// * @param status
	// * @return
	// */
	//private UpdateDetailsStatus formatSuccessMessages(UpdateDetailsStatus details, UpdateDetailsStatus status) {
	//    // TODO Auto-generated method stub
	//    return null;
	//}

	/**
	 * change a group details
	 * 
	 * @param group
	 * @param groupAction
	 * @return
	 */
	public PropertiesStatus changeGroupDetails(String group, String groupAction)
	{
		if (groupAction.equals("join"))
			return this.joinGroup(group);
		if (groupAction.equals("create"))
			return this.createGroup(group);
		// default : return empty.
		return new UpdateDetailsStatus();
	}

	//    /***************
	//     * communicate with the server with a specific string and 
	//     * return a result update status.
	//     * 
	//     * @param updateFileString
	//     * @param header
	//     * @param trailer
	//     * @return
	//     */
	//    private UpdateDetailsStatus updateDetails(String updateFileString ,
	//            String header, String trailer ){
	//        FileHandlerBean fileHandler = new FileHandlerBean();
	//		File serverResponse = null;
	//		
	//        UpdateDetailsStatus result = new UpdateDetailsStatus();
	//        try
	//		{
	//        BufferedReader regStrBuffer = new BufferedReader(new StringReader(updateFileString));
	//        serverResponse = fileHandler.getIncomingFileSlot();
	//		// create the incomingFile Stream :
	//		
	//		FileOutputStream incomingFileOutputStream = new FileOutputStream(serverResponse);
	//		
	//		System.out.println("Sending: " +updateFileString);
	//		
	//		// exchange files :
	//			comm.exchangeFiles(regStrBuffer,incomingFileOutputStream,header,trailer);
	//			
	//			// show the response file to output stream :			
	//			FileReader reader = new FileReader(serverResponse);
	//			int readChar=0;
	//			System.out.println("Got in return :");
	//			while ( (readChar = reader.read()) != -1)
	//			    System.out.print((char)readChar);
	//			reader.close();
	//			
	//			//			 read the file and update the display :
	//			SAXReader iregDocReader = new SAXReader();
	//			Document regDoc = iregDocReader.read(serverResponse);
	//			if (regDoc == null)
	//			{
	//			    result.internalServerError=true;
	//			    fileHandler.handleAfterUsage(serverResponse,false);
	//			    return result;
	//			}
	//			
	//			if (regDoc.getRootElement().element("ACK") != null){
	//			    readUpdateToResult(regDoc,result);		   
	//			}
	//			
	//			if (regDoc.getRootElement().element("NACK") != null)
	//			{
	//			    result.errorMessage = regDoc.getRootElement().elementText("NACK");
	//			    fileHandler.handleAfterUsage(serverResponse,false);
	//			    return result;
	//			}
	//			 fileHandler.handleAfterUsage(serverResponse,true);
	//		}
	//		catch (Exception e)
	//		{
	//		    // handle after usage :		    
	//		    try 
	//		    {
	//                fileHandler.handleAfterUsage(serverResponse,false);
	//            }
	//		    catch (NoSuchPropertyException e1) 
	//		    {
	//                Loggers.getLogger().warning("file handle after usage failed :" + e1.getMessage()+" . please check properties file.");
	//            }
	//		    
	//		    e.printStackTrace();
	//			Loggers.getLogger().warning("Error while connecting server : " + e.getMessage());
	//			result.communicationError = true;
	//			result.errorMessage = "Error while connecting server: \n " + e.getMessage() + " Please check your internet connection or configure proxy.";			 
	//			return result;			
	//		}				
	//      return result;
	//    }

	/***************
	 * <p>
	 * communicate with the server with a specific string and 
	 * return a result update status.
	 * </p>
	 * 
	 * <p>
	 * The method was changed to use a String for the return value instead of file.
	 * @since 0.5.0
	 * @author idob
	 * </p>
	 * 
	 * @param updateFileString the details of the requested update which is sent to the server
	 * @param header header of the request
	 * @param trailer trailer of the request
	 * @return The acknowledge document
	 * @throws IOException
	 * @throws NoSuchPropertyException
	 * @throws DocumentException
	 * @throws MalformedURLException
	 */
	private Element updateDetails(String updateFileString, String header, String trailer) throws IOException, MalformedURLException//, DocumentException
	{
		StringWriter serverResponse = new StringWriter();
		BufferedWriter bfrWriter = new BufferedWriter(serverResponse);
		BufferedReader regStrBuffer = new BufferedReader(new StringReader(updateFileString));

		logger.debug("Sending: " + updateFileString);

		// exchange files :
		comm.exchangeFiles(regStrBuffer, bfrWriter, header, trailer);
			
		logger.debug("Got in return :" + serverResponse.toString());

		// read and update the display :
//		StringReader serverResponseReader = new StringReader(serverResponse.toString());
//		SAXReader iregDocReader = new SAXReader();
		Element result = XMLUtil.getRootElement(serverResponse.toString());// iregDocReader.read(serverResponseReader);
		return result;
	}

	/**
	 * parse an update document nto the result update status object.
	 * 
	 * @param regDoc
	 * @param result
	 * @param result2
	 */
	private PropertiesStatus readUpdateToResult(Element regDoc, PropertiesStatus generalRequest)
	{

		PropertiesStatus result = new UpdateDetailsStatus();
		/*System.out.println*/logger.debug("Reading update to result...");
		Element rootElem = regDoc;//.getRootElement();
		Element ackElement = XMLUtil.getChildElementByName(rootElem, "ACK");
		Element userElement = null;
		Element agentElement = null;
		Element groupElement = null;
		if (ackElement != null)
		{
			userElement = XMLUtil.getChildElementByName(ackElement, "user");
			agentElement = XMLUtil.getChildElementByName(ackElement, "agent");
			groupElement = XMLUtil.getChildElementByName(ackElement, "group");
		}
		// check user element response :
		if (userElement != null)
		{
			if (XMLUtil.getChildElementByName(userElement,"NACK") != null)
			{
				result.errorMessages.add(XMLUtil.getChildElementByName(userElement,"NACK").getTextContent());
			}
			else
			{
//				result.userName = userElement.elementText("name");
//				result.country = userElement.elementText("country");
//				result.email = userElement.elementText("email");
//				result.groupName = userElement.elementText("group");
//				result.groupOwner = userElement.elementText("groupOwner");
//				result.hasPassword = (userElement.element("password") == null) ? null : "true";

				result.userName = safeGetTextContent(XMLUtil.getChildElementByName(userElement, "name"));//.elementText("name");
				result.country = safeGetTextContent(XMLUtil.getChildElementByName(userElement, "country"));//userElement.elementText("country");
				result.email = safeGetTextContent(XMLUtil.getChildElementByName(userElement, "email"));//userElement.elementText("email");
				result.groupName = safeGetTextContent(XMLUtil.getChildElementByName(userElement, "group"));//userElement.elementText("group");
				result.groupOwner = safeGetTextContent(XMLUtil.getChildElementByName(userElement, "groupOwner"));//userElement.elementText("groupOwner");
				result.hasPassword = safeGetTextContent((XMLUtil.getChildElementByName(userElement, "password"))).equals("") ? null : "true";//(userElement.element("password") == null) ? null : "true";

				if (generalRequest.userName != null)
					result.successMessages.add("User name changed to:" + result.userName);
				if (generalRequest.country != null)
					result.successMessages.add("Country changed to:" + result.country);
				if (generalRequest.email != null)
					result.successMessages.add("Email changed to:" + result.email);
				if ((generalRequest.newPassword != null) && (result.hasPassword != null))
					result.successMessages.add("Password changed successfly.");
			}
		}

		// check agent element response :
		if (agentElement != null)
		{
			if (XMLUtil.getChildElementByName(agentElement,"NACK") != null)
			{
				result.errorMessages.add(XMLUtil.getChildElementByName(agentElement,"NACK").getTextContent());
			}
			else
			{
				result.successMessages.add("Agent name changed to:" + XMLUtil.getChildElementByName(agentElement,"ID").getTextContent());
				result.agentName = XMLUtil.getChildElementByName(agentElement,"ID").getTextContent();
			}
		}

		// check group element response :
		if (groupElement != null)
		{
			if (XMLUtil.getChildElementByName(groupElement,"NACK") != null)
			{
				result.errorMessages.add(XMLUtil.getChildElementByName(groupElement,"NACK").getTextContent());
			}
			else
			{
				result.groupName = XMLUtil.getChildElementByName(groupElement,"groupName").getTextContent();
//				 groupElement.elementText("groupName");
				result.groupOwner = XMLUtil.getChildElementByName(groupElement,"groupOwner").getTextContent();
//				result.groupOwner = groupElement.elementText("groupOwner");
				if (result.groupOwner != null && result.groupOwner.equals("true"))
					result.successMessages.add("You are now group owner of '" + result.groupName + "'");
				else
					result.successMessages.add("You are now member of '" + result.groupName + "'");
			}
		}
		return result;
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
	 * @param generalRequest
	 * @return
	 */
	public PropertiesStatus generalUpdate(PropertiesStatus generalRequest)
	{
//		String currentAgentName = null;

		String header = null;
		String trailer = "</agent>";
		// generate the header :
		if (!generalRequest.isValidationRequest())
		{
/*			try
			{
				currentAgentName = PropertiesBean.getProperty(PropertiesNames.AGENT_NAME"agentName");
			}
			catch (NoSuchPropertyException e)
			{
				PropertiesStatus result = new UpdateDetailsStatus();
				result.errorMessage = " Properties file corrupt.\n please check properties.xml";
				return result;
			}*/
			header = "<agent name=\"" + currentAgentName + "\">";
		}
		else
		{
			header = "<agent id=\"" + generalRequest.agentID + "\">";
		}

		PropertiesStatus result = new UpdateDetailsStatus();

		// form the string to send to the server :
		String registrationString = this.generateRequestString(generalRequest);
		Element resultDoc = null;

		// get the document :
		try
		{
			resultDoc = updateDetails(registrationString, header, trailer);
		}
		catch (MalformedURLException e1)
		{
			// Internal agent  error :
			Loggers.getLogger().warn("Internal Agent error :\n" + e1.getMessage());
			result.errorMessage = "Internal Agent error :\n" + e1.getMessage();
			return result;
		}
		catch (IOException e1)
		{
			// communication error :
			Loggers.getLogger().warn("Error while connecting server : " + e1.getMessage());
			result.communicationError = true;
			result.errorMessage = "Error while connecting server: \n " + e1.getMessage()
					+ "\n Please check your internet connection \nor configure your proxy settings.";
			return result;
		}
/*		catch (DocumentException e1)
		{
			// document reading error :
			Loggers.getLogger().warning("Error while trying to read server resopnse: " + e1.getMessage());
			result.internalServerError = true;
			return result;
		}*/

		// parse the result and return a UpdateDetailsStatus object:
		return readUpdateToResult(resultDoc, generalRequest);
	}

	/**
	 * @param generalRequest
	 * @return
	 */
	private String generateRequestString(PropertiesStatus generalRequest)
	{
		String registrationString = "\t<update-details>\n";
		// Write the agent details :                      

		if (generalRequest.userPropsChanged() || generalRequest.groupPropsChanged() || generalRequest.passwordPropsChanged())
		{
			registrationString += "\t\t<user>\n";
			if (generalRequest.userPropsChanged())
			{
				registrationString += isValidValue(generalRequest.userName) ? "\t\t\t<name>" + generalRequest.userName + "</name>\n" : "";
				registrationString += (generalRequest.country != null) ? "\t\t\t<country>" + generalRequest.country + "</country>\n" : "";
				registrationString += isValidValue(generalRequest.email) ? "\t\t\t<email>" + generalRequest.email + "</email>\n" : "";
			}
			if (generalRequest.groupPropsChanged())
			{
				// group action :  
				registrationString += "\t\t\t<group action=\"" + generalRequest.groupAction + "\">" + generalRequest.groupName + "</group>\n";
			}

			//            System.out.println("old psw="+generalRequest.oldPassword);
			//            System.out.println("new psw="+generalRequest.newPassword);

			if (generalRequest.passwordPropsChanged())
			{
				registrationString += "\t\t\t<password>\n";
				registrationString += isValidValue(generalRequest.oldPassword) ? "\t\t\t\t<current>" + generalRequest.oldPassword + "</current>\n" : "";
				registrationString += isValidValue(generalRequest.newPassword) ? "\t\t\t\t<new>" + generalRequest.newPassword + "</new>\n" : "";
				registrationString += isValidValue(generalRequest.newPassword) ? "\t\t\t\t<confirmNew>" + generalRequest.newPassword + "</confirmNew>\n" : "";
				registrationString += "\t\t\t</password>\n";
			}

			registrationString += "\t\t</user>\n";

		}

		if (generalRequest.agentPropsChanged())
		{
			registrationString += "\t\t<agent>\n";
			registrationString += "\t\t\t<name>" + generalRequest.agentName + "</name>\n";
			registrationString += "\t\t</agent>\n";
		}

		if (generalRequest.autoUpdateChanged())
		{
			registrationString += "\t\t\t<autoupdate>" + generalRequest.autoUpdate + "</autoupdate>\n";
		}

		if (generalRequest.isValidationRequest())
			return "<query-details/>";

		registrationString += "\t</update-details>\n";
		return registrationString;
	}

	/**
	 * @param agentID
	 * @return
	 */
	public PropertiesStatus validateAgentDetails(String agentID)
	{
		/*System.out.println*/logger.debug("validating agent details. agent name is :" + agentID);
		Loggers.getLogger().info("validating agent details. agent name is :" + agentID);
		PropertiesStatus details = new UpdateDetailsStatus();
		details.agentID = agentID;

		return this.generalUpdate(details);
	}

}