/*
 * Created on 03/03/2005
 *
 */
package dimes.gui.registration;

import dimes.state.user.PropertiesStatus;
import dimes.state.user.RegistrationStatus;

/**
 * @author Ohad Serfaty
 *
 */
public interface RegisterDetailsParentFrame
{

	/**
	 * @param regStat
	 */
	void returnRegistrationState(PropertiesStatus regStat);

}