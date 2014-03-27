package registration;

import java.io.IOException;

import dimes.state.user.RegistrationStatus;

/** A class that represents a "dummy" Agent for 
 * purposes of the registration process. Designed to 
 * let registration continue without exposing the actual
 * Agent
 * @author user
 *
 */
 class AgentFacade {

	void applyRegistrationSuccess() {
		System.out.println("AgentFacade.ApplyRegistrationSuccess");
		
	}

	void writeRegistrationDetails(RegistrationStatus regStat)throws IOException {
		// TODO Auto-generated method stub
		
	}

}
