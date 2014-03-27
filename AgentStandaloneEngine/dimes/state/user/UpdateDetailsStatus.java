/*
 * Created on 28/02/2005
 *
 */
package dimes.state.user;

import java.util.Iterator;
import java.util.Vector;

import dimes.util.properties.PropertiesBean;

/**
 * @author Ohad Serfaty
 *
 * a representator of update result status
 */
@Deprecated
public class UpdateDetailsStatus
{

	public String userName;
	public String country;
	public String email;
	public String agentName;

	public boolean updateCanceled = false;
	public String errorMessage; // general error message.
	public Vector<String> errorMessages = new Vector<String>();
	public Vector<String> successMessages = new Vector<String>();
	public boolean internalServerError = false;
	public boolean communicationError = false;

	public String groupAction;
	public String groupName;
	public String groupOwner;
	public String hasPassword;

	public String oldPassword;
	public String newPassword;
	public String agentID = null;

	public String autoUpdate;

	public boolean autoUpdateChanged()
	{
		if (autoUpdate != null)
			return true;
		return false;
	}

	public String getChainedErrors()
	{
		return chainMessages(this.errorMessages);
	}

	public String getChainedSuccesses()
	{
		return chainMessages(this.successMessages);
	}

	private String chainMessages(Vector vec)
	{
		if (vec.isEmpty())
			return null;
		String result = "";
		Iterator i = vec.iterator();
		while (i.hasNext())
		{
			String messageToChain = (String) i.next();
			result = result + messageToChain + "\n";
		}
		return result;
	}

	public String toString()
	{
		return (updateCanceled ? "Update canceled" : (internalServerError ? "Internal server error" : ((communicationError ? "Comm error" : userName + " - "
				+ country + " - " + email + " - " + agentName + " Error : " + getChainedErrors() + " success : " + getChainedSuccesses()))));
	}

	public boolean userPropsChanged()
	{
		return isValidValue(userName) || (country != null) || isValidValue(email);
	}

	public boolean agentPropsChanged()
	{
		return isValidValue(agentName);
	}

	public boolean groupPropsChanged()
	{
		return (isValidValue(groupName) && isValidValue(groupAction));
	}

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

	/**
	 * @return
	 */
	public boolean passwordPropsChanged()
	{
		return (isValidValue(oldPassword) || isValidValue(newPassword));
	}

	/**
	 * @return
	 */
	public boolean isValidationRequest()
	{
		return isValidValue(agentID);
	}
}