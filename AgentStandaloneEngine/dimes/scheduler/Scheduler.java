/*
 * Created on 22/01/2004
 */
package dimes.scheduler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

//import org.dom4j.DocumentException;

import dimes.comm2server.ConnectionException;
import dimes.comm2server.FileExchangeChannel;
import dimes.measurements.Measurements;
import dimes.measurements.NoSuchOperationException;
import dimes.measurements.operation.MeasurementOp;
import dimes.measurements.operation.Operation;
import dimes.scheduler.usertask.UserTaskSource;
import dimes.state.TaskManagerInfo;
import dimes.util.FileHandlerBean;
import dimes.util.HeaderProducer;
import dimes.AgentGuiComm.comm.ClientsBean;
import dimes.AgentGuiComm.logging.Loggers;
import dimes.AgentGuiComm.util.MessageTypes;
//import dimes.util.logging.Loggers;
import dimes.util.logging.ResultsManager;
import dimes.util.logging.ResultsMemoryHandler;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * <p>
 * Various changes were made in version 0.5.0 to allow 2 main changes:
 * </P>
 * <ol>
 * <li>
 * Running the default.in script in case that the server doesn't reply and there is an Internet connection
 * </li>
 * <li>
 * Some changes in sendRecieve()
 * </li>
 * </ol>
 * @author anat, idob (version 0.5.0)
 */
public final class Scheduler {
	
	private static Scheduler instance = null;
	private HeaderProducer agent = null;
	private OperationManager opManager;
	private FileExchangeChannel comm;
	private FileHandlerBean fileHandler = null;
	private Operation nextOp = null;
	private static final Logger logger = Loggers.getLogger(Scheduler.class);
	private final String myName = "dimes.scheduler.Scheduler";
	private static boolean askForWork=true; //Always true the first time.
	private boolean lastConnectionSucceful = false;
	private int lastHourMeasuremtns=0;
	private int thisHourMeasuremtns=0;
	
	
	public static File statsFile;
	/**
	 * private constractor - singleton - can get an instance only through
	 * getInstance()
	 */
	private Scheduler(FileExchangeChannel aComm, HeaderProducer anAgent) {
		
		agent = anAgent; // save current agent Object
		opManager = OperationManager.getInstance(); // opManager represent single Scheduler		
		comm = aComm; // comm object Overrides dimes.comm2server.Communicator class and adds secured connection
		fileHandler = new FileHandlerBean(); // fileHandler object should fetch files (incoming outgoing)
		
		try{
			String statsFileName = PropertiesBean.getProperty(PropertiesNames.OUTGOING_DIR)+"stats.csv";
			statsFile = new File(statsFileName);
			statsFile.createNewFile();
	//		FileWriter pw = new FileWriter(statsFile);
		}catch(Exception e){
			
		}
		instance = this;
		
	}

	/**
	 * Returns the scheduler object (singleton)
	 * 
	 * @param aComm
	 * @param anAgent
	 * @return reference to the scheduler
	 */
	public static Scheduler getInstance(FileExchangeChannel aComm,
			HeaderProducer anAgent) {
		if (Scheduler.instance == null)
			new Scheduler(aComm, anAgent);
		return Scheduler.instance;
	}

	/**
	 * @param responseReader
	 * @return true if information from reader was successfuly parsed and merged into prioritized tasks
	 */
	public boolean handleResponse(Reader responseReader) {
		boolean result = true;
		try {
			this.opManager.handleResponse(responseReader);
		} catch (Exception e) {
			logger.warning(e.toString());
			result = false;
		}
		return result;
	}

	/**
	 * @param exId
	 * @param scriptId
	 * @param commandLineTask
	 * @return
	 */
	public Reader getMeasurementScript(String exId, String scriptId,
			UserTaskSource commandLineTask) {
		return this.opManager.getMeasurementScript(exId, scriptId,
				commandLineTask);
	}

	/**
	 * <p>
	 * give the agent possibility to send file & receive an answer String from
	 * server. 
	 * </p>
	 * <p>
	 * Changes in version 0.5.0:
	 * <ol>
	 * <li>This method was changed to return boolean value - false in case of
	 * server failure. This will be used as a status if to call the default
	 * measurements script.</li>
	 * <li>The boolean parameter was omitted - not in use.</li>
	 * </ol>
	 * </p>
	 * 
	 * @author idob (Version 0.5.0), Anat
	 * @throws NoSuchPropertyException
	 */

	public boolean sendReceive()throws NoSuchPropertyException {
//		boolean askForWork = false;
//		int outLength=0;
		File[] outgoingFiles = null;		
		File tmpFile = null;
		logger.entering(this.myName, "sendReceive");// debug
		outgoingFiles = this.fileHandler.getNextOutgoing();
//		outLength = outgoingFiles.length;
//		StringBuilder resultsToSend = new StringBuilder();
		
		if(0<outgoingFiles.length){  //Handles Files
			sendFiles(outgoingFiles);			
		}
		
		if(!ResultsManager.hasResultsPending()){ //Measurements are in mem 
			askForWork=true;			
		}
		String resultsString = ResultsManager.poolResults();
//		if (resultsString.contains("</OperationResult>")){
//			String temp = resultsString;
//			int count = 0;
//			while (temp.contains("</OperationResult>")){
//				count++;
//				temp = temp.substring(0,temp.indexOf("</OperationResult>"));
//			}
//			System.out.println("Result Number: " + count+"\n");
//		}

		StringReader resultsReader = new StringReader(resultsString);
		String header = this.agent.getAgentHeader(Scheduler.askForWork);
		String trailer = this.agent.getAgentTrailer();
		StringWriter incomingStrWriter = new StringWriter();
		logger.info("Exchanging files with server");
		BufferedWriter incomingBfrWriter = new BufferedWriter(
				incomingStrWriter);
		BufferedReader outFileReader = new BufferedReader(
				resultsReader);
		try {
			this.comm.exchangeFiles(outFileReader, incomingBfrWriter,
					header, trailer);
			incomingBfrWriter.close();
			outFileReader.close();
			lastConnectionSucceful=true;
		} catch (IOException e) {
			logger.warning("Files exchange stopped - an I/O problem.");
			lastConnectionSucceful=false;
			// case we asked for new work script:
			if (askForWork) {
				
				// If we tried to send an ask for work with empty tmp file that should be deleted:
				if (!(outgoingFiles==null))
				try{
				if (outgoingFiles[0] != null && ((File) outgoingFiles[0]).length() == 0)
					((File) outgoingFiles[0]).delete();
				}catch (ArrayIndexOutOfBoundsException v){}
				String incomingStr = incomingStrWriter.toString();
				
				// Case we got an answer during exchange files:
				if (incomingStr != null && incomingStr.length() > 0) {
					return handleIncomingScript(incomingStr);
					
				} //else return false;
				
			} //else
				// Probably first run and we're trying to send few out files -
				// meanwhile we have nothing to work on:
				//return false;
		}
		
		String incomingStr = incomingStrWriter.toString().toUpperCase();
//		System.out.println("incoming answer: "+incomingStr); //TODO: remove, debug
		if ((incomingStr != null) && (incomingStr.length() > 0)) {
//			this.fileHandler.handleAfterUsage((File) outgoingFiles[i],
//					true);
			if (askForWork) {
				return handleIncomingScript(incomingStr);
			}
		} else {
			logger
			.warning("No message or work response came from server. Results will be saved to file");
			ResultsMemoryHandler.writeResultsToFile(resultsString);
			lastConnectionSucceful=false;
			return false;
		}
		
		// if there are no outgoing files  use an empty file, 
		//so that agent header will be sent to ask for work
//	
//		if (outLength == 0)
//		{			
//			outgoingFiles = new File[1];
//			File resultsDir = new File(PropertiesBean
//					.getProperty(PropertiesNames.RESULTS_DIR));
//			try {
//				tmpFile = this.fileHandler.getOutgoingFileSlot(resultsDir);
//				tmpFile.createNewFile();
//			} catch (IOException e) {
//				logger.severe("Can not create new results file.");
//				throw new RuntimeException(e);
//			}
//
//			outgoingFiles[0] = tmpFile;
//			outLength = outgoingFiles.length;
//		}else{
//			checkFiles(outgoingFiles);
//		}
		

		logger.exiting(this.myName, "sendReceive");// debug
		lastConnectionSucceful=true;
		return true;
		
	}

	private boolean sendFiles(File[] outgoingFiles) throws NoSuchPropertyException{
		String header = this.agent.getAgentHeader(false);
		String trailer = this.agent.getAgentTrailer();
		StringWriter incomingStrWriter = null;
		StringBuilder incomingStringBuilder = new StringBuilder("");
		String xmlString="<Results></Results>";
		int outLength = outgoingFiles.length;
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp=null;

		try {
			
			for (int i = 0; i < outLength; ++i) {
				System.out.print(i+" ");
				if(i%50==0)System.out.println("");
				File currentFile = (File) outgoingFiles[i];
				BufferedReader reader = new BufferedReader(new FileReader(currentFile));
				while (reader.ready()){
					incomingStringBuilder.append(reader.readLine());
				}				
				reader.close();
				xmlString = incomingStringBuilder.toString();
				int opRes =  xmlString.lastIndexOf("</OperationResult>");
				if(-1==opRes){
					xmlString= "<Results></Results>";
					
				}else{				
					int place = xmlString.lastIndexOf("</Results>");
					if (-1 == place){
						xmlString = xmlString.substring(0,opRes+19)+"</Results>";
					}else
						if ((place+10)<xmlString.length()) xmlString=xmlString.substring(0,place+10);
				}
				
				
				StringReader resultsReader = new StringReader(xmlString);
				incomingStrWriter = new StringWriter();
				BufferedWriter incomingBfrWriter = new BufferedWriter(
						incomingStrWriter);
				BufferedReader outFileReader = new BufferedReader(
						resultsReader);
				try {
					this.comm.exchangeFiles(outFileReader, incomingBfrWriter,
							header, trailer);
					incomingBfrWriter.close();
					outFileReader.close();
					currentFile.delete();  //If we'd made it here, the file was already sent. Delete it.
				
				} catch (IOException e) {
					logger.warning("Files exchange stopped - an I/O problem.");
//					// case we asked for new work script:
//					if (askForWork) {
//						
//						// If we tried to send an ask for work with empty tmp file that should be deleted:
//						if (outgoingFiles[0] != null && ((File) outgoingFiles[0]).length() == 0)
//							((File) outgoingFiles[0]).delete();
//						
//						String incomingStr = incomingStrWriter.toString();
//						
//						// Case we got an answer during exchange files:
//						if (incomingStr != null && incomingStr.length() > 0) {
//							return handleIncomingScript(incomingStr);
//							
//						} else 
//							return false;
//						
//					} else
//						// Probably first run and we're trying to send few out files -
//						// meanwhile we have nothing to work on:
						return false;
				}
//				sp=spf.newSAXParser();
//				sp.parse(new InputSource(new ByteArrayInputStream(xmlString.getBytes())),new DefaultHandler());//,errorHandler);
				
//				incomingStrWriter = new StringWriter();
				// If there is nothing else after that to send and we need new
				// work script:
//				if (i == (outLength - 1)/* && needAWork*/) {
//					askForWork = true;
//					header = this.agent.getAgentHeader(askForWork);
//				}
//				BufferedWriter incomingBfrWriter = new BufferedWriter(
//						incomingStrWriter);
//				BufferedReader outFileReader = new BufferedReader(
//						new FileReader((File) outgoingFiles[i]));
//				this.comm.exchangeFiles(outFileReader, incomingBfrWriter,
//						header, trailer);
//				incomingBfrWriter.close();
//				outFileReader.close();
//				String incomingStr = incomingStrWriter.toString();
//				if ((incomingStr != null) && (incomingStr.length() > 0)) {
//					this.fileHandler.handleAfterUsage((File) outgoingFiles[i],
//							true);
//					if (askForWork) {
//						return handleIncomingScript(incomingStr);
//					}
//				} else {
//					logger
//					.warning("No message or work response came from server.");
//					return false;
				}
			
				return true; //If the parsing worked we can return this
//			}
			
			// Case of Exception:
//		}catch (SAXException e) {
/*			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("@main");
			
			int opRes =  xmlString.lastIndexOf("</OperationResult>");
			if(-1==opRes){
				return "<Results></Results>";
				
			}
			
			int place = xmlString.lastIndexOf("</Results>");
			if (-1 == place){
				xmlString = xmlString.substring(0,opRes+19)+"</Results>";
			}else
			if (place >-1 &&(place+10)<xmlString.length()) xmlString=xmlString.substring(0,place+10);
			
//			String endString=errorHandler.getCloseString();
//			if(!"".equals(endString)) xmlString= xmlString+endString;
			System.out.println(xmlString);
			try {
				sp.parse(new InputSource(new ByteArrayInputStream(xmlString.getBytes())),new DefaultHandler());//errorHandler);
				return xmlString;
			} catch (SAXException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} */
		}catch (Exception ioe) {
			
			logger.warning("Files exchange stopped - an I/O problem.");
//			// case we asked for new work script:
//			if (askForWork) {
//				
//				// If we tried to send an ask for work with empty tmp file that should be deleted:
//				if (outgoingFiles[0] != null && ((File) outgoingFiles[0]).length() == 0)
//					((File) outgoingFiles[0]).delete();
//				
//				String incomingStr = incomingStrWriter.toString();
//				
//				// Case we got an answer during exchange files:
//				if (incomingStr != null && incomingStr.length() > 0) {
//					return handleIncomingScript(incomingStr);
//					
//				} else 
//					return false;
//				
//			} else
//				// Probably first run and we're trying to send few out files -
//				// meanwhile we have nothing to work on:
//				return false;
			return false;
		}
	}
	
	private void checkFiles(File[] outgoingFiles) {
		for(File aFile:outgoingFiles){
			if(0==aFile.length()){ 
				aFile.delete();
				continue;
			}
			char[] read= new char[(int) aFile.length()];
//			FileInputStream fis=null;
			FileReader fis = null;
			try {
//				 fis = new FileInputStream(aFile);
				fis = new FileReader(aFile);
				fis.read(read);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String begining =new String(read);
			if(!(begining.startsWith("<Results>"))){
				String tmpName=aFile.getPath()+"tempfile.tmp";
				String fileName =aFile.getPath();
//				String filename2 =aFile.getName();
				File tmpFile = new File(tmpName);
				try {
					tmpFile.createNewFile();
//					FileOutputStream fos = new FileOutputStream(tmpFile);
					FileWriter fos = new FileWriter(tmpFile);
/*					fos.write("<Results>");
					fos.write(new String(read));
					int i=1;
					while(fis.read(read)!=-1){
						fos.write(new String(read));
//						fos.write(("\n-------"+i+"--------\n").getBytes());
//						fos.flush();
//						i++;
						read = new byte[2048];
					}
					fos.write("</Results>");*/
					fos.write("<Results>"+begining+"</Results>");
					fos.flush();
					fos.close();
					fis.close();
					aFile.delete();
					tmpFile.renameTo(new File(fileName));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

	/**
	 * @param incomingStr
	 * @return true if script was parsed and merged into prioritized tasks successfully
	 */
	private boolean handleIncomingScript(String incomingStr) {
		StringReader incomingStrReader = new StringReader(incomingStr);
		boolean success = this.handleResponse(incomingStrReader);
		if (success) {
			this.updateNextOp();
			return true;
		} else
			return false;
	}

	/**
	 * A method which is called from SchedulerTask in case that sendRecieve was
	 * failed but Internet connection exists<br>
	 * (a case of server failure). In this case the default.in measurements will
	 * be processed.
	 * 
	 * @throws NoSuchPropertyException
	 * @throws FileNotFoundException
	 * 
	 * @author idob
	 * @since 0.5.0
	 */
	public boolean runDefaultScript() {
		
		boolean success = false;
		String defaultScriptFileName = null;
		BufferedReader defaultScriptReader = null;
		
		//Get default script file name
		try {
			defaultScriptFileName = PropertiesBean
					.getProperty(PropertiesNames.DERFAULT_IN_FILE);
		} catch (NoSuchPropertyException e) {
			logger
					.severe("Agent can not find the default measurements file in properties.xml. Check possibility of corruption.");
			e.printStackTrace();
			return false;
		}
		
		//Get the actual file 
		try {
			defaultScriptReader = new BufferedReader(new FileReader(
					defaultScriptFileName));
		} catch (FileNotFoundException e) {
			logger.severe("Agent can not find the default measurements file");
			e.printStackTrace();
			return false;
		}
		
		//Send the file to be parsed and merged
		success = this.handleResponse(defaultScriptReader);

		if (success) {
			this.updateNextOp();
			try {
				this.executeOperation();
			} catch (NoOperationsException e) {
				return false;
			}
			return true;
		} else
			return false;
	}

	/**
	 * Called by the timer when it wakes up the Scheduler, executes one measurement op
	 * 
	 * @return true if execution was successful
	 * @throws NoOperationsException
	 */
	public boolean executeOperation() throws NoOperationsException {
		Measurements.setExecuting(true);
		try {
			
			if (this.nextOp == null) {
				/* System.err.println */logger.finest("nextOp is null");// debug
				throw new NoOperationsException();
			}
			
			logger.finest("executing operation of task: "
					+ this.nextOp.getContainingTask().getID());// debug
			boolean operationSuccess = this.nextOp.execute(this);
			ClientsBean.send(MessageTypes.SENT_TYPE_STATUS, getStatus());
			System.out.println(getStatus());
			if (operationSuccess) {
				this.nextOp.getContainingTask().updateTakenOp(nextOp);
				this.updateNextOp();// current operation is completed
				return true;
			} else {
				logger
						.info("Operation was not executed due to communication problems.\nWill Try again soon...");// debug
				return false;
			}
			
		} catch (NoSuchOperationException e) {
			// TODO - possible security breach
			logger.warning(e.getMessage());// debug
			return false;
		} finally {
			Measurements.setExecuting(false);
		}
	}

	/**
	 * called by the Operation nextVer - There should be a method per each
	 * subtype of Operation, not only MeasurementOp
	 * 	 
	 * @param op dimes.measurements.operation.MeasurementOp
	 * @return true if measurement was executed
	 * @throws NoSuchOperationException
	 */
	public boolean execute(MeasurementOp op) throws NoSuchOperationException {
		return Measurements.execute(op);
	}

	/**
	 * To be called by the timer or by the Scheduler itself before execution of
	 * the Operation.
	 */
	void updateNextOp()// throws NoOperationsException
	{
		this.nextOp = this.opManager.getNextOp();
	}

	// todo - these next 2 shouldn't be here.
	// nextVer - will not be necessary once sending of files is parallel
/*	public void resetFinished() {
		this.opManager.resetFinished();
	}
*/
/*	public boolean areFinishedTasks() {
		return this.opManager.areFinishedTasks();
	}*/

	/**
	 * @return true if there are more tasks 
	 */
	public boolean areTasks() {
		return this.opManager.areTasks();
	}

	/**
	 * @return number of tasks remaining 
	 */
	public int getTaskNum() {
		return this.opManager.getTaskNum();
	}

	/**
	 * @return TaskManagerInfo - wrapper to the TaskManager that can task info, but allows no 
	 * other access to the underlying TaskManager
	 */
	public TaskManagerInfo getTaskManagerInfo() {
		return this.opManager.getTaskManagerInfo();
	}

	/**
	 * 
	 * reset the scheduler so that Agent will be restarted with new tasks from
	 * the server.
	 * 
	 */
	public void reset() {

		this.opManager.reset();
		// wait until the measurement is done to cancel next operation.
		Measurements.waitWhileExecuting();
		this.updateNextOp();
		Measurements.setExecuting(false);
	}
	
	public static void setNeedWork(){
		Scheduler.askForWork=true;
	}
	
	public String getStatus(){
		StringBuilder sb = new StringBuilder("");
		sb.append("<LAST_CONNECTION_SUCCEFUL>"+Boolean.toString(lastConnectionSucceful)+"</LAST_CONNECTION_SUCCEFUL>");
		sb.append("<CURRENT_MEASURMENT>"+this.nextOp.getContainingTask().getCurrentMeasurmentIP()+"</CURRENT_MEASURMENT>");
		sb.append("<CURRENT_EXID>"+this.nextOp.getContainingTask().getExID()+"</CURRENT_EXID>");
	
		
		return sb.toString();
		}

}
