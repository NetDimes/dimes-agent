/*
 * Created on 28/02/2005
 *
 */
package dimes.state.user;

import java.util.Vector;


/**
 * @author Ohad Serfaty
 *
 * representator of a registration status
 * 
 *  
 */
public class RegistrationStatus //extends PropertiesStatus
{

	public String userName;
	public String country;
	public String email;
	//    public String agentName;
	public Vector agentNames = new Vector();

	public boolean registrationCanceled = false;
	public String errorMessage="";
	public boolean internalServerError = false;
	public boolean communicationError = false;
	public String groupName;
	public String groupOwner;
	public String hasPassword;

	public String toString()
	{
		String names = "";
		for (int i = 0; i < agentNames.size(); ++i)
		{
			names += (String) agentNames.get(i) + " ";
		}
		return (registrationCanceled ? "Registration canceled" : (internalServerError ? "Internal server error" : ((communicationError
				? "Comm error"
				: userName + " - " + country + " - " + email + " - " + names + " Error : " + errorMessage))));
	}
}