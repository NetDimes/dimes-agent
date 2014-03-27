package dimes.state.user;

import java.util.Iterator;
import java.util.Vector;

import dimes.util.properties.PropertiesBean;

/**
 * @author Boaz
 * 
 * This class replaces both RegistrationStatus and UpdateDetails status
 * since both classes have basically the same components and structure
 * replacing them with one class means we don't have to have separate methods
 * to handle each. 
 *
 */
public abstract class PropertiesStatus {

	public String userName;
	public String country;
	public String email;
	public String agentName;
	public boolean updateCanceled = false;
	public String errorMessage;
	public Vector<String> errorMessages = new Vector<String>();
	public Vector<String> successMessages = new Vector<String>();
	public Vector agentNames = new Vector();
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
	public boolean registrationCanceled;

	public PropertiesStatus() {
		super();
	}

	public boolean autoUpdateChanged() {
		if (autoUpdate != null)
			return true;
		return false;
	}

	public String getChainedErrors() {
		return chainMessages(this.errorMessages);
	}

	public String getChainedSuccesses() {
		return chainMessages(this.successMessages);
	}

	private String chainMessages(Vector<String> vec) {
		if (vec.isEmpty())
			return null;
		String result = "";
		Iterator<String> i = vec.iterator();
		while (i.hasNext())
		{
			String messageToChain = (String) i.next();
			result = result + messageToChain + "\n";
		}
		return result;
	}

	public String toString() {
		return (updateCanceled ? "Update canceled" : (internalServerError ? "Internal server error" : ((communicationError ? "Comm error" : userName + " - "
				+ country + " - " + email + " - " + agentName + " Error : " + getChainedErrors() + " success : " + getChainedSuccesses()))));
	}

	public boolean userPropsChanged() {
		return isValidValue(userName) || (country != null) || isValidValue(email);
	}

	public boolean agentPropsChanged() {
		return isValidValue(agentName);
	}

	public boolean groupPropsChanged() {
		return (isValidValue(groupName) && isValidValue(groupAction));
	}

	/**
	 * @param country
	 * @return
	 */
	private boolean isValidValue(String string) {
		if (string == null)
			return false;
		return (PropertiesBean.isValidValue(string) && !(string.equals("")));
	}

	/**
	 * @return
	 */
	public boolean passwordPropsChanged() {
		return (isValidValue(oldPassword) || isValidValue(newPassword));
	}

	/**
	 * @return
	 */
	public boolean isValidationRequest() {
		return isValidValue(agentID);
	}

}