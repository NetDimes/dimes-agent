/*
 * Created on 03/03/2005
 *
 */
package dimes.util.registration;

import dimes.state.user.PropertiesStatus;

/**
 * @author Ohad Serfaty
 *
 */
public interface UpdateDetailsParentFrame
{

	/**
	 * @param updateStat
	 */
	void returnUpdateState(PropertiesStatus updateStat);

	/**
	 * 
	 */
	void allowExitUpdateFrame();

}