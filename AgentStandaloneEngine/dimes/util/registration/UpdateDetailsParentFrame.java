/*
 * Created on 03/03/2005
 *
 */
package dimes.util.registration;

import dimes.state.user.UpdateDetailsStatus;

/**
 * @author Ohad Serfaty
 *
 */
public interface UpdateDetailsParentFrame
{

	/**
	 * @param updateStat
	 */
	void returnUpdateState(UpdateDetailsStatus updateStat);

	/**
	 * 
	 */
	void allowExitUpdateFrame();

}