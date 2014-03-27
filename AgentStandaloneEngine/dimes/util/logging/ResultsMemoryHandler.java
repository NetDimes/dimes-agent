package dimes.util.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.LogRecord;

import org.w3c.dom.Element;

import dimes.util.FileHandlerBean;
import dimes.util.XMLUtil;

/** A class that handles the results gathered from measurements
 * and preserves them in memory (via ResultsManager). When the 
 * results hit the preset size limit, this class will announce() 
 * to listeners and cause filesizeexceeded.
 * 
 * @author boazh
 * @since 0.5.5
 *
 */
public class ResultsMemoryHandler extends ResultsAnnouncer {

	private static ResultsMemoryHandler me=new ResultsMemoryHandler();
	private ResultsMemoryHandler(){}
	
	private static int count=0;
	
	public static ResultsMemoryHandler getInstance(){
		if(null==me)me = new ResultsMemoryHandler();
		return me;
	}
	
	final int MEASUREMENTS_LIMIT=250000; //250K
	
	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void publish(LogRecord record) {
		System.out.println("Count inside"+(++count)+"\n");
		if (!this.isLoggable(record)) return;
		String msg = record.getMessage();
		publish(msg);
	}

	public void publish(String msg) {
		
		Element rootElm = XMLUtil.getRootElement(msg);
		String command = XMLUtil.getChildElementByName(rootElm, "CommandType").getTextContent().trim();
		System.out.println("Command type (mem): "+command);
		if(MEASUREMENTS_LIMIT<=ResultsManager.appendResult(msg)){
			this.announce();
			System.out.println("DEBUGLOG Measurement Limit exceeded");
		}
		
	}

	/**A static method that will write a result string (from memory) 
	 * into a result file. Used for when there is no connection with server, 
	 * or when Agent is shutting down.
	 * 
	 * @author boazh
	 * @since 0.5.5
	 * @param resultsString
	 * @return true is write successful
	 */
	public static boolean writeResultsToFile(String resultsString){
		try {
			File outputFile = FileHandlerBean.getAnOutgoingFile();
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outputFile));
			fileWriter.write(resultsString);
			fileWriter.flush();
			fileWriter.close();
			return true;
		} catch (IOException e) {
//			dimes.util.logging.Loggers.getLogger(ResultsMemoryHandler.class).severe("Results could not be saved to file and may be lost");
			dimes.AgentGuiComm.logging.Loggers.getLogger(ResultsMemoryHandler.class).severe("Results could not be saved to file and may be lost");
			e.printStackTrace();
			return false;
		}
	}
	
}
