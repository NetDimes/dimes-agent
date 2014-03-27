package testing.dimes.AgentGuiComm.comm;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import dimes.AgentGuiComm.comm.CommunicationsThread;

public class CommunicationsThreadTest {

	CommunicationsThread CT=null;
	
	@Before
	public void setUp() throws Exception {
		dimes.util.properties.PropertiesBean.init("D:\\Program Files\\DIMES10\\DIMES\\Agent\\Classes\\Base\\conf\\properties.xml");
	}

	@Test
	public void testGetInstance() {
		try {
			CT=CommunicationsThread.getInstance(33333);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(CT);
		assertTrue(CT instanceof CommunicationsThread);
	//	fail("Not yet implemented");
	}

	@Test
	public void testRun() {
		try{
		Thread t = new Thread(CommunicationsThread.getInstance(33333));
		t.start();
		assertTrue(true);
		}catch(Exception E){
		fail("Not yet implemented");
		}
	}

	@Test
	public void testStopThread() {
		//fail("Not yet implemented");
	}

}
