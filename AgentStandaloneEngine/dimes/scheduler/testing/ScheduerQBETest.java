package dimes.scheduler.testing;

import junit.framework.TestCase;
import dimes.scheduler.Scheduler;
import dimes.scheduler.SchedulerTask;
import dimes.util.HeaderProducer;
import dimes.util.LibraryLoader;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

public class ScheduerQBETest extends TestCase {
	
	public void testScheduler(){
		PropertiesBean.init("./conf/developmentProperties.xml");
		LibraryLoader.load();
		LocalFileExchange exchange = new LocalFileExchange("./dimes/scheduler/testing/qbescript.tst");
		Scheduler scheduler = Scheduler.getInstance(exchange, new HeaderProducer(){

			public String getAgentHeader(boolean askForWork) throws NoSuchPropertyException {
				return "";
			}

			public String getAgentTrailer() {
				return "";
			}
			
		});
		SchedulerTask task = new SchedulerTask(scheduler);
		task.run();
		task.run();
		task.run();
		task.run();
	}

}
