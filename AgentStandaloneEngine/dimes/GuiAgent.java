/*
 * Created on 08/02/2004
 */
package dimes;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

//import org.dom4j.DocumentException;

import dimes.AgentGuiComm.AgentFrameFacade;
import dimes.AgentGuiComm.GUICommunicator;
import dimes.AgentGuiComm.RegestrationFrameFacade;
import dimes.AgentGuiComm.logging.RemoteCommHandler;
import dimes.AgentGuiComm.logging.ResultSenderHandler;
import dimes.comm2server.ConnectionHandlerFactory;
import dimes.comm2server.ExponentialBackoffCommunicator;
import dimes.comm2server.StandardCommunicator;
//import dimes.AgentGuiComm.logging.GraphHandler;
//import dimes.gui.AgentFrame;
import dimes.measurements.Measurements;
import dimes.measurements.IPUtils;
import dimes.measurements.Protocol;
import dimes.scheduler.Scheduler;
import dimes.scheduler.SchedulerTask;
import dimes.scheduler.usertask.UserTaskSource;
import dimes.state.KeepAliveTask;
import dimes.state.Status;
//import dimes.state.user.PropertiesDetails;
import dimes.state.user.RegistrationStatus;
import dimes.state.user.RegistrationWorker;
import dimes.state.user.UpdateDetailsStatus;
import dimes.state.user.UpdateDetailsThread;
//import dimes.util.DipslayServer;
import dimes.util.HeaderProducer;
import dimes.util.LibraryLoader;
import dimes.util.Lock;
import dimes.util.comState.ComStateDetector;
import dimes.util.gui.GuiUtils;
//import dimes.util.debug.AgentEngineTester;
//import dimes.util.logging.DisplayUpdateHandler;
import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;
import dimes.util.registration.ProgressMonitorComponent;
import dimes.util.registration.RegistrationFrame;
import dimes.util.registration.UpdateDetailsParentFrame;
import dimes.util.registration.UpdateMonitorDialog;

/**
 * Main class of the DIMES agent
 * 
 * Change in version 0.5.0: Adding support in non interactive (service) mode.
 * 
 * @author anat, idob (version 0.5.0)
 */
/**
 * @author user
 *
 */
/**
 * @author user
 *
 */
/**
 * @author user
 *
 */
public class GuiAgent implements HeaderProducer, Agent
{
	//Note that all Agent variables are private. No one should directly change
	//anything in the Agent.
	private final String VERSION_STR;	
	private Status status = null; //keepalive
	private Timer keepAliveTimer = null;
	private KeepAliveTask keepAliveTask = null;
	private boolean restart = false;	
	private Scheduler sched = null; //scheduler + communicator
	private SchedulerTask schedulerTask = null;
	private ExponentialBackoffCommunicator comm = null;
	private Timer schedTimer = null;
	private String propFileName; //properties
	private static final Logger logger = Loggers.getLogger(GuiAgent.class);	//logging
	private Lock registerLock = new Lock(); 	//registering
	private AgentFrameFacade agentFrame;
	private UpdateDetailsThread updateDetailsThread;
	private GUICommunicator server;	
	private Thread keepAliveTimerThread = null;  //Turned to Private+setter and getter 
	private RemoteCommHandler remoteCommHandler;
//	private GraphHandler graphHandler;
	private ResultSenderHandler resultSenderHandler;
	private static GuiAgent me=null;
	private static boolean debug = false;
//	private AgentEngineTester agentEngineTester= null;
	
	public static GuiAgent getInstance() throws MalformedURLException, NoSuchPropertyException{
		if (null==me) me=new GuiAgent(null);
		return me;
	}
	
	public static Agent getInstance(AgentFrameFacade theAgentFrame) throws MalformedURLException, NoSuchPropertyException{
		if (null==me) me=new GuiAgent(theAgentFrame);
		return me;
	}
	
	/**
	 * <p>
	 * This constructor is being called from AppSplash in case the Agent<br>
	 * Is running in Service mode. In that case there is no AgentFrame.
	 * </p>
	 * 
	 * @throws MalformedURLException
	 * @throws NoSuchPropertyException
	 * @author idob
	 * @since 0.5.0
	 */
	private GuiAgent() throws MalformedURLException, NoSuchPropertyException {
		this(null);
		
	}

	/**
	 * A constructor to be called in a case of interactive mode. The constructor
	 * determins mode by the presence of an AgentFrame. If theAgentFrame==null it
	 * assumes non-interactive
	 * 
	 * @param theAgentFrame
	 * @param versionStr
	 * @throws IOException
	 * @throws NoSuchPropertyException
	 */
	private GuiAgent(AgentFrameFacade theAgentFrame) throws MalformedURLException, NoSuchPropertyException
	{

		if( theAgentFrame != null ){
			this.agentFrame = theAgentFrame;// Save a reference to the AgentFrame if it exists. Otherwise assume non-interactive
		}
		else {
			agentFrame = AgentFrameFacade.getInstance();
			agentFrame.setAgent(this);
			LibraryLoader.load();  //Loads native libraries (MTR, shared.dll, etc.)
		}
		
		this.VERSION_STR = PropertiesBean.getProperty(PropertiesNames.AGENT_VERSION); 	//get version from properties.xml		
		remoteCommHandler = new RemoteCommHandler(GUICommunicator.getInstance());
		remoteCommHandler.setLevel(Level.INFO);
		logger.addHandler(remoteCommHandler);
		//0.6 duplicate below
//		String resultWriterName = PropertiesBean.getProperty(PropertiesNames.RESULT_WRITER_NAME);
//		Logger resultWriter = Logger.getLogger(resultWriterName);


		GuiAgent.logger.finest("Creating agent ver." + VERSION_STR);//debug

		
		// Create one timer delayed task object and schedules it (one instance) to
		//check communication to internet, if internet disconnected dont check 
		ComStateDetector.getInstance().start(); 

		// An ExponentialBackoffCommunicator object Overrides dimes.comm2server.Communicator class & detects secured connection
		comm = new ExponentialBackoffCommunicator(PropertiesBean.getProperty(PropertiesNames.COMPRESSED_SERVER_URL),
				ConnectionHandlerFactory.NONSECURE_CONNECTION);

		// Create one Scheduler object (one instance)
		sched = Scheduler.getInstance(comm, this);
		
		// Create status object to get/set ver and input detail to xml for agent purpos
		this.status = new Status(this.VERSION_STR, this.sched.getTaskManagerInfo());//check

		// Create Timer object called schedTimer
		this.schedTimer = new Timer();
		
		// Create Timer object called keepAliveTimer
		this.keepAliveTimer = new Timer();

		// Used for keeping a single copy of the application
		server = GUICommunicator.getInstance();//AppSplash.lockSocket;
		
		// Detect Agent object inside a single instance keeper
		server.setAgentFacade(this.agentFrame);
		
		// enable to screen saver to connect with the server
//		DisplayUpdateHandler displayUpdateHandler = new DisplayUpdateHandler(server);
		// Detect at resultWriterName (String var) logger name of result
		String resultWriterName = PropertiesBean.getProperty(PropertiesNames.RESULT_WRITER_NAME /*"names.resultWriter" */);

		// Create at resultWriter a suitable Logger Object follow by logger name (resultWriterName)
		Logger resultWriter = Logger.getLogger(resultWriterName);
		resultSenderHandler =new ResultSenderHandler();//agentFrame);
		resultWriter.addHandler(resultSenderHandler);
		
		// Append to resultWriter current log Handler instance
//		resultWriter.addHandler(displayUpdateHandler);

		// if not exist yet, Create logger header at Loggers object (static Logger object) and append a logging Handler
		//ASInfoLogger logger of the AS
//		Loggers.getASInfoLogger().addHandler(displayUpdateHandler);
//		server.startServer();
//		agentEngineTester=AgentEngineTester.getInstance(this); 
//		System.out.println(agentEngineTester.testConnection());
//		System.out.println(agentEngineTester.testServerConnection());
	}

	
	/* (non-Javadoc)
	 * @see dimes.IAgent#initAgent()
	 */
	public void initAgent() throws NumberFormatException, NoSuchPropertyException, IOException
	{
		//use timer to start the agent, so that the main in AgentFrame can end,
		// allowing splash screen to disappear
		Timer startTimer = new Timer();
		int startDelay = Integer.parseInt(PropertiesBean.getProperty(PropertiesNames.START_DELAY/* "startDelay" */));
		if (startDelay < 0)
			startDelay = 0;
		startTimer.schedule(new StartAgentTask(this), startDelay);//start after
	}

	/**
	 * @throws Exception
	 */
	public void getWork() throws Exception
	{
		/** **** send files and get new work ****** */
		this.sched.sendReceive();
		this.scheduleTimers();
//		System.out.println(agentEngineTester.testPrintSystaxTree()); 
	}/*
		if( agentFrame != null )
			AgentFrame.statisticsPanel.updateStatistics();
	}

	//@added 0.5.2 to allow keepalive to initiate a statisitcs update.
	public void getStats(){
		AgentFrame.statisticsPanel.updateStatistics();
	}*/
	
	//changes how files are handled after usage (handleAfterUsage)
	/* (non-Javadoc)
	 * @see dimes.IAgent#debugMode()
	 */
	public void debugMode(/*boolean debug*/)
	{
		Loggers.debugMode(debug);
		try
		{
			if (debug)
			{
				PropertiesBean.setProperty(PropertiesNames.AFTER_USAGE + ".in"/* "policies.after_usage.in" */, "4");
				PropertiesBean.setProperty(PropertiesNames.AFTER_USAGE + ".out"/* "policies.after_usage.out" */, "4");
			}
			else
			{
				PropertiesBean.setProperty(PropertiesNames.AFTER_USAGE + ".in"/* "policies.after_usage.in" */, "1");
				PropertiesBean.setProperty(PropertiesNames.AFTER_USAGE + ".out"/* "policies.after_usage.out" */, "1");
			}
			PropertiesBean.loadProperties();//reload
		}
		catch (IOException e)
		{
			GuiAgent.logger.warning(e.toString());//debug
		}
		catch (Exception e)
		{
			GuiAgent.logger.warning(e.toString());//debug
		}
	}

	/**
	 * @throws NumberFormatException
	 * @throws NoSuchPropertyException
	 * @throws MalformedURLException
	 */
	private void scheduleTimers() throws NumberFormatException, NoSuchPropertyException, MalformedURLException
	{
		if (this.schedTimer != null)
		{
			this.schedulerTask = new SchedulerTask(this.sched);//, agentFrame);
			this.schedTimer.schedule(this.schedulerTask, this.schedulerTask.getDelay(), this.schedulerTask.getPeriod());
		}

		this.keepAliveTask = new KeepAliveTask(this.keepAliveTimer, this);
		this.keepAliveTimer.schedule(this.keepAliveTask, this.keepAliveTask.getDelay(), this.keepAliveTask.getPeriod());
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#rescheduleSchedTimer()
	 */
	public void rescheduleSchedTimer()
	{
		this.pauseSchedTimer();
		this.resumeSchedTimer();
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#pauseSchedTimer()
	 */
	public void pauseSchedTimer()
	{
		if (this.schedTimer != null)
		{
			this.schedTimer.cancel();
			this.schedTimer = null;
		}
		ComStateDetector.getInstance().stop();
	}

	
	/* (non-Javadoc)
	 * @see dimes.IAgent#pauseAllTimers()
	 */
	public void pauseAllTimers()
	{
		this.pauseSchedTimer();  //Stops the schedTimer thread 
		if (this.keepAliveTimer != null)
		{
			this.keepAliveTimer.cancel();
			this.keepAliveTimer = null;
		}

	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#resumeAllTimers()
	 */
	public void resumeAllTimers() throws MalformedURLException
	{
		this.resumeSchedTimer(); //"resumes" the Measurements thread
		// Keep alive :
		this.keepAliveTimer = new Timer();
		this.keepAliveTask = new KeepAliveTask(this.keepAliveTimer, this);
		this.keepAliveTimer.schedule(this.keepAliveTask, this.keepAliveTask.getDelay(), this.keepAliveTask.getPeriod());

	}

	
	/* (non-Javadoc)
	 * @see dimes.IAgent#resumeSchedTimer()
	 */
	public void resumeSchedTimer()
	{
		this.schedTimer = new Timer();
		this.schedulerTask = new SchedulerTask(this.sched);//, this.agentFrame);
		//starts after period so that would not wait less than period between
		// tasks
		this.schedTimer.schedule(this.schedulerTask, this.schedulerTask.getPeriod(), this.schedulerTask.getPeriod());

		ComStateDetector.getInstance().start();
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#exit(java.lang.Object)
	 */
	public void exit(/*AgentFrame*/Object theAgentFrame)
	{

		GUICommunicator.sendLog(Level.SEVERE, "", "Agent is exiting");
		this.pauseAllTimers();
		server.stopCommunicator();
		this.registerLock.release();//check
		System.exit(0);

	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#exit()
	 */
	public void exit()
	{
		this.agentFrame.StopButton_actionPerformed(null, false);
	}
	
	
	/* (non-Javadoc)
	 * @see dimes.IAgent#exit(int)
	 */
	public void exit(int code){
		this.pauseAllTimers();
		server.stopCommunicator();
		this.registerLock.release();//check
		System.exit(code);
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#restart()
	 */
	public void restart()
	{
		
		int iter =0;
		
		System.setProperty(AppSplash.INTERNAL_RESTART_PROPERTY, String.valueOf(true));
		GuiAgent.logger.fine("property is :" + System.getProperty(AppSplash.INTERNAL_RESTART_PROPERTY));
		
		while(!Boolean.valueOf(System.getProperty(AppSplash.INTERNAL_RESTART_PROPERTY)).booleanValue()){ //Wait until we're sure that the Internal property is set. 
			GuiAgent.logger.fine("Waiting for INTERNAL_RESTART_PROPERTY"+iter); //debug 
			
			if (iter == 100)  break; 
			 else iter++;
			
			try {
				Thread.sleep(100);				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		};
		
		GuiAgent.logger.fine("Agent frame Restart = " + restart);
		this.agentFrame.StopButton_actionPerformed(null, true);
		this.sched.reset();		// make the old scheduler go away...
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#isRegistered(java.lang.String)
	 */
	public boolean isRegistered(String registeredProp)
	{
		
		try
		{
			if (PropertiesBean.hasValidValue(registeredProp) && PropertiesBean.hasValidValue(PropertiesNames.USER_NAME) 
					&& PropertiesBean.hasValidValue(PropertiesNames.AGENT_NAME)) 
				return Boolean.valueOf(PropertiesBean.getProperty(registeredProp)).booleanValue();

			return false;
		} catch (NoSuchPropertyException e)
		{
			GuiAgent.logger.fine(e.toString());//debug
			return false;
		}
		
	}

	/*Unused as far as  can tell. BoazH 6/09
	 *  
	/** Sets the Registered state of the Agent.
	 * 
	 * @param flag boolean
	 * @param registeredProp
	 * @throws IOException
	 */
/*	public void setRegistered(boolean flag, String registeredProp) throws IOException
	{
		PropertiesBean.setProperty(registeredProp, String.valueOf(flag));
	}
*/
	/** Launches the registration process either for the interactive or non-interactive
	 * modes 
	 * 
	 * @param agentIDAlreadyExist
	 */
	private void register(boolean agentIDAlreadyExist)
	{
		try {
			String propertiesDir= PropertiesBean.getProperty(PropertiesNames.BASE_DIR)+"conf"+File.separator;
			File regFile = new File(propertiesDir+"registration.xml");
			if(regFile.exists()){
				
				register(getPropertiesUpdateCommunicator(), regFile);
				return;
			}
		} catch (NoSuchPropertyException e) {
			e.printStackTrace();
		}
/*		if( agentFrame != null ) {
			this.agentFrame.register(getPropertiesUpdateCommunicator(), agentIDAlreadyExist);		
		}
		else {*/
			register(getPropertiesUpdateCommunicator(), agentIDAlreadyExist);
//		}
		
		this.registerLock.waitFor();//waiting for applyPropertiesButton to be clicked
		GuiAgent.logger.exiting("dimes.Agent", "register");//debug
	}
	private void register(StandardCommunicator registrationComm, File regFile){
		//TODO add auto register here
		RegistrationWorker rw = new RegistrationWorker(registrationComm);
	try{	
		rw.startRegistration(regFile);
	}catch(Exception e){
		regFile = null;
		rw.startRegistration();
		
		e.printStackTrace();
		}
	}
	
	/**
	 * Activate the registration frame in non interactive (service) mode. 
	 * Changed to private since this really shouldn't be called from anywhere
	 * other than from handleRegistration() 
	 * 
	 * @param registrationComm
	 * @param agentIDAlreadyExist
	 */
	private void register(StandardCommunicator registrationComm, boolean agentIDAlreadyExist)
	{

		RegistrationFrame regFrame = new RegistrationFrame(registrationComm, agentIDAlreadyExist, this);
		regFrame.setSize(500, 350);
		GuiUtils.centerFrame(regFrame);
		regFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		regFrame.setVisible(true);
		
	}


	/* (non-Javadoc)
	 * @see dimes.IAgent#applyRegistrationSuccess()
	 */
	public void applyRegistrationSuccess()
	{
		this.registerLock.release();

	}

	/**
	 * Determines agent id (mac address) and default values of name and IP
	 *
	 * @throws IOException
	 * @throws NoSuchPropertyException
	 */
	private void setDefaultDetails() throws IOException, NoSuchPropertyException
	{
		try
		{
			String IP = IPUtils.getHostIP();
			PropertiesBean.setProperty(PropertiesNames.IP/* "IP" */, IP);
		}
		catch (UnknownHostException e)
		{
			logger.fine(e.toString());//debug
		}
		return;
	}

	
	/* (non-Javadoc)
	 * @see dimes.IAgent#blockedProtocolsWereChecked()
	 */
	public boolean blockedProtocolsWereChecked()
	{
		
		boolean udpBlockedSet = true;
		boolean icmpBlockedSet = true;
		
		try
		{
			udpBlockedSet = PropertiesBean.hasValidValue(PropertiesNames.UDP_BLOCKED);
			icmpBlockedSet = PropertiesBean.hasValidValue(PropertiesNames.ICMP_BLOCKED);		
		} catch (NoSuchPropertyException e2)
		{
			GuiAgent.logger.warning(e2.toString());
		}
		
		return udpBlockedSet && icmpBlockedSet;

	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#checkBlockedProtocols()
	 */
	public void checkBlockedProtocols()
	{
		logger.finest("checking blocking... ");//debug
		Measurements.waitWhileExecuting();//check

		boolean checkedBlocking = this.blockedProtocolsWereChecked();
		boolean udpBlocked = IPUtils.isProtocolBlocked(Protocol.UDP);
		boolean icmpBlocked = IPUtils.isProtocolBlocked(Protocol.ICMP);
		
		try
		{
			PropertiesBean.setProperty(PropertiesNames.UDP_BLOCKED, String.valueOf(udpBlocked));
		}
		catch (IOException e)
		{
			GuiAgent.logger.warning(e.toString());
		}
		
		try
		{
			PropertiesBean.setProperty(PropertiesNames.ICMP_BLOCKED, String.valueOf(icmpBlocked));
		}
		catch (IOException e1)
		{
			GuiAgent.logger.warning(e1.toString());
		}
		
		if (!checkedBlocking)//if this is the 1st time checking protocol blocking, set the default protocol accordingly.
		{
			int defaultProto = Protocol.getDefault();
			switch (defaultProto)
			{
				case Protocol.UDP :
					if (udpBlocked)
						try
						{
							if (!icmpBlocked)
								PropertiesBean.setProperty(PropertiesNames.DEFAULT_PROTOCOL, Protocol.getName(Protocol.ICMP));
						}
						catch (IOException e3)
						{
							GuiAgent.logger.warning(e3.toString());
						}
					break;
				case Protocol.ICMP :
					if (icmpBlocked)
						try
						{
							if (!udpBlocked)
								PropertiesBean.setProperty(PropertiesNames.DEFAULT_PROTOCOL, Protocol.getName(Protocol.UDP));
						}
						catch (IOException e3)
						{
							GuiAgent.logger.warning(e3.toString());
						}
			}
		}

		Measurements.setExecuting(false);//check
		logger.finest("finished checking blocking.");//debug
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#startUserTask(dimes.scheduler.usertask.UserTaskSource)
	 */
	public void startUserTask(UserTaskSource commandLineTask)
	{
		
		System.out.println("Starting Agent task : " + commandLineTask);

		String exId; //use agent name as script id
		DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		String time = dateFormatter.format(new Date(System.currentTimeMillis()));
		String scriptId = commandLineTask.getScriptID() + "-" + time; //use traceURL_systime as script id
		
		try
		{
			exId = PropertiesBean.getProperty(PropertiesNames.USER_NAME/* "userName" */);
		}
		catch (NoSuchPropertyException e1)
		{
			exId = "User";
			GuiAgent.logger.warning(e1.toString());//debug
		}
		
		Reader scriptReader = this.sched.getMeasurementScript(exId, scriptId, commandLineTask);	// create a script reader
		this.sched.handleResponse(scriptReader);
		
		try
		{
			scriptReader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * handles the entire registering process, including server side
	 * 
	 * @return
	 * @throws IOException
	 * @throws NoSuchPropertyException
	 */
	public boolean handleRegistration() throws IOException, NoSuchPropertyException
	{

		/** **** check registration ****** */
		if (!isRegistered(PropertiesNames.REGISTERED_STATE/* "registered" */))
		{
			GuiAgent.logger.fine("not registered");//debug
			this.setDefaultDetails();
			this.register(agentIDExist());
			return false;
		}
		else
		{
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#agentIDExist()
	 */
	public boolean agentIDExist() { 
		
		boolean result;
		
		try
		{
			
			result = PropertiesBean.hasValidValue(PropertiesNames.AGENT_ID);
			
		} catch (NoSuchPropertyException e)
		{
			Loggers.getLogger().info("IP Property not found : initiating Clean installation process.");
			return false;
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#getAgentHeader(boolean, boolean)
	 */
	public String getAgentHeader(boolean askForWork, boolean askForAgentIndex) throws NoSuchPropertyException
	{

		String agentName = PropertiesBean.getProperty(PropertiesNames.AGENT_NAME);
		String userName = PropertiesBean.getProperty(PropertiesNames.USER_NAME);
		StringBuilder header = new StringBuilder( "<agent name=\"" + agentName + "\" >\n" + "<header>\n");
		String IP;
		
		try
		{
			IP = IPUtils.getHostIP();
		}
		catch (UnknownHostException e)
		{
			GuiAgent.logger.fine(e.toString());//debug
			IP = "unknown";
		}
		
		header.append("\t<IP>" + IP + "</IP>\n");
		header.append("\t" + PropertiesNames.getOpeningTag(PropertiesNames.ASK_FOR_WORK) + askForWork + PropertiesNames.getClosingTag(PropertiesNames.ASK_FOR_WORK)
				+ "\n");
		header.append("\t" + PropertiesNames.getOpeningTag(PropertiesNames.TASK_NUM) + this.sched.getTaskNum()
				+ PropertiesNames.getClosingTag(PropertiesNames.TASK_NUM) + /*
				 * "
				 * </taskNum>"
				 */"\n");
		header.append("\t" + this.status.getCodeVersion().toXML() + "\n");

		if (askForAgentIndex)
		{
			header.append("\t" + PropertiesNames.getOpeningTag(PropertiesNames.ASK_FOR_AGENT_INDEX) + askForAgentIndex
					+ PropertiesNames.getClosingTag(PropertiesNames.ASK_FOR_AGENT_INDEX) + "\n");
		}
		header.append("</header>");
		return header.toString();
	}
	
	/* (non-Javadoc)
	 * @see dimes.IAgent#getAgentHeader()
	 */
	public String getAgentHeader() throws NoSuchPropertyException
	{
		String agentName = PropertiesBean.getProperty(PropertiesNames.AGENT_NAME);
		String header = "<agent agentName=\"" + agentName + "\" >\n" + "<header>\n";
		String IP;

		try
		{
			IP = IPUtils.getHostIP();
		}
		catch (UnknownHostException e)
		{
			GuiAgent.logger.fine(e.toString());//debug
			IP = "unknown";
		}

		header += "\t<IP>" + IP + "</IP>\n";
		header += "</header>";

		return header;
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#getAgentHeader(boolean)
	 */
	public String getAgentHeader(boolean askForWork) throws NoSuchPropertyException
	{
		return this.getAgentHeader(askForWork, false);
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#getAgentTrailer()
	 */
	public String getAgentTrailer()
	{
		return "</agent>";
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#getKeepAliveTimerThread()
	 */
	public Thread getKeepAliveTimerThread() {
		return keepAliveTimerThread;
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#setKeepAliveTimerThread(java.lang.Thread)
	 */
	public void setKeepAliveTimerThread(Thread keepAliveTimerThread) {
		this.keepAliveTimerThread = keepAliveTimerThread;
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#getStatus()
	 */
	public Status getStatus()
	{
		return this.status;
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#getRestart()
	 */
	public boolean getRestart()
	{
		return this.restart;
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#setRestart(boolean)
	 */
	public void setRestart(boolean theRestart)
	{
		this.restart = theRestart;
	}

	
	/* (non-Javadoc)
	 * @see dimes.IAgent#applyUserPropertiesChange(dimes.util.registration.UpdateDetailsParentFrame, dimes.util.registration.ProgressMonitorComponent, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void applyUserPropertiesChange(UpdateDetailsParentFrame frame, ProgressMonitorComponent monitorComponent, String userName, String agentName,
			String country, String email)
	{
		
		String currentUserName = "";
		String currentCountry = "";
		String currentEmail = "";
		String currentAgentName = "";
		
		try
		{
			currentUserName = PropertiesBean.getProperty(PropertiesNames.USER_NAME/* "userName" */);
			currentCountry = PropertiesBean.getProperty(PropertiesNames.COUNTRY/* "country" */);
			currentEmail = PropertiesBean.getProperty(PropertiesNames.EMAIL/* "email" */);
			currentAgentName = PropertiesBean.getProperty(PropertiesNames.AGENT_NAME/* "agentName" */);
		}		catch (NoSuchPropertyException e)
		{
			e.printStackTrace();
		}
		
		updateDetailsThread = new UpdateDetailsThread(getPropertiesUpdateCommunicator(), frame, monitorComponent);
		monitorComponent.setCancelListener(updateDetailsThread);
		updateDetailsThread.startUserDetailsUpdate(currentUserName.equals(userName) ? null : userName, currentCountry.equals(country) ? null : country,
				currentEmail.equals(email) ? null : email, (agentName == null) ? null : currentAgentName.equals(agentName) ? null : agentName);
	}

////// --> Called by MmetaOPHandler and is part of the currently-disabled METAOP system
//
//	/**
//	 * This method checks which property isn't signed as null and then update
//	 * it's property parm
//	 */
//	public boolean updatePropertiesFile(PropertiesDetails properties)
//	{
//		boolean success = true;
//		System.out.println("updating properties file :" + properties);
//		try
//		{
//			if (properties.getFileTransferRate() != null)
//				PropertiesBean.setProperty(PropertiesNames.FILE_TRANSFER_RATE, properties.getFileTransferRateParm());
//			if (properties.mesurmentRate != null)
//			{
//				PropertiesBean.setProperty(PropertiesNames.SCHEDULER_PERIOD, properties.mesurmentRateParm);
//				changeMeasurementRate();
//			}
//			if (properties.defaultProtocol != null)
//				PropertiesBean.setProperty(PropertiesNames.DEFAULT_PROTOCOL, properties.defaultProtocolParm);
//			if (properties.useProxy != null)
//				PropertiesBean.setProperty(PropertiesNames.USE_PROXY, properties.useProxyParm);
//			if (properties.proxyHost != null)
//				PropertiesBean.setProperty(PropertiesNames.PROXY_HOST, properties.proxyHostParm);
//			if (properties.proxyPort != null)
//				PropertiesBean.setProperty(PropertiesNames.PROXY_PORT, properties.proxyPortParm);
//			if (properties.automaticUpdate != null)
//				PropertiesBean.setProperty(PropertiesNames.AUTOMATIC_UPDATE, properties.automaticUpdateParm);
//			
//			agentFrame.refreshSettingFrame();
//			}
//		catch (Exception e)
//		{
//			success = false;
//		}
//		return success;
//	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#writeUpdatedDetails(dimes.state.user.UpdateDetailsStatus)
	 */
	public boolean writeUpdatedDetails(UpdateDetailsStatus updateStat)
	{
		boolean success = true;
		try
		{
			if (updateStat.agentName != null)
				PropertiesBean.setProperty(PropertiesNames.AGENT_NAME/* "agentName" */, updateStat.agentName);
			if (updateStat.userName != null)
				PropertiesBean.setProperty(PropertiesNames.USER_NAME/* "userName" */, updateStat.userName);
			if (updateStat.country != null)
				PropertiesBean.setProperty(PropertiesNames.COUNTRY/* "country" */, updateStat.country);
			if (updateStat.email != null)
				PropertiesBean.setProperty(PropertiesNames.EMAIL/* "email" */, updateStat.email);
			if (updateStat.groupName != null)
				PropertiesBean.setProperty(PropertiesNames.GROUP/* "group" */, updateStat.groupName);
			if (updateStat.groupOwner != null)
				PropertiesBean.setProperty(PropertiesNames.GROUP_OWNER/* "groupOwner" */, updateStat.groupOwner);
			if (updateStat.hasPassword != null)
				PropertiesBean.setProperty(PropertiesNames.HAS_PSWD_STATE/* "hasPswd" */, "true");

		}
		catch (Exception e)
		{
			success = false;
		}
		return success;
	}

	
	/* (non-Javadoc)
	 * @see dimes.IAgent#applyGroupDetailsUpdate(dimes.util.registration.UpdateDetailsParentFrame, dimes.util.registration.UpdateMonitorDialog, java.lang.String, java.lang.String)
	 */
	public void applyGroupDetailsUpdate(UpdateDetailsParentFrame frame, UpdateMonitorDialog monitorComponent, String groupName, String groupAction)
	{

		updateDetailsThread = new UpdateDetailsThread(getPropertiesUpdateCommunicator(), frame, monitorComponent);
		monitorComponent.setCancelListener(updateDetailsThread);
		updateDetailsThread.startGroupDetailsUpdate(groupName, groupAction);
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#applyDetails(dimes.util.registration.UpdateDetailsParentFrame, dimes.util.registration.UpdateMonitorDialog, dimes.state.user.UpdateDetailsStatus, java.util.Vector, java.util.Vector)
	 */
	public void applyDetails(UpdateDetailsParentFrame frame, UpdateMonitorDialog monitorComponent, UpdateDetailsStatus updateDetails, Vector<String> successMessages,
			Vector<String> errorMessages)
	{

		updateDetailsThread = new UpdateDetailsThread(getPropertiesUpdateCommunicator(), frame, monitorComponent);
		monitorComponent.setCancelListener(updateDetailsThread);
		updateDetailsThread.startGeneralUpdate(updateDetails, successMessages, errorMessages);

	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#applyPasswordChange(dimes.util.registration.UpdateDetailsParentFrame, dimes.util.registration.UpdateMonitorDialog, java.lang.String, java.lang.String)
	 */
	public void applyPasswordChange(UpdateDetailsParentFrame frame, UpdateMonitorDialog monitorComponent, String currentPassword, String newPassword)
	{

		updateDetailsThread = new UpdateDetailsThread(getPropertiesUpdateCommunicator(), frame, monitorComponent);
		monitorComponent.setCancelListener(updateDetailsThread);
		updateDetailsThread.startPasswordUpdate(currentPassword, newPassword);

	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#getPropertiesUpdateCommunicator()
	 */
	public StandardCommunicator getPropertiesUpdateCommunicator()
	{
		StandardCommunicator propertiesChangeComm = null;
		String propertiesUpdateURL;
		String securePropertiesUpdateURL;
		
		// Get the secure url : property :
		try
		{
			securePropertiesUpdateURL = PropertiesBean.getProperty(PropertiesNames.SECURE_PROPERTIES_UPDATE_URL);
		}
		catch (NoSuchPropertyException e)
		{
			Loggers.getLogger().severe("properties file incomplete. couldn't find securePropertiesUpdateURL");
			securePropertiesUpdateURL = "https://www.netdimes.org/DIMES/propertiesUpdate";
		}

		try
		{
			propertiesUpdateURL = PropertiesBean.getProperty(PropertiesNames.PROPERTIES_UPDATE_URL);
		}
		catch (NoSuchPropertyException e)
		{
			Loggers.getLogger().severe("properties file incomplete. couldn't find propertiesUpdateURL");
			propertiesUpdateURL = "http://www.netdimes.org/DIMES/propertiesUpdate";
		}

		// Create the Connection :
		try
		{
			// secure connection :
			propertiesChangeComm = new StandardCommunicator(securePropertiesUpdateURL, ConnectionHandlerFactory.SECURE_CONNECTION);
		}
		catch (MalformedURLException e)
		{
			// On failure : try and generate a non-secure connection.
			Loggers.getLogger().severe("Could not initialize properties communicator :" + e.getMessage() + " . communicating threw Non-Secure connection.");
			try
			{
				propertiesChangeComm = new StandardCommunicator(propertiesUpdateURL, ConnectionHandlerFactory.NONSECURE_CONNECTION);
			}
			catch (MalformedURLException e1)
			{
				Loggers.getLogger().severe("Could not initialize properties communicator :" + e.getMessage() + " . Cennection will fail.");
			}
		}
		
		return propertiesChangeComm;
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#writeRegistrationDetails(dimes.state.user.RegistrationStatus)
	 */
	public void writeRegistrationDetails(RegistrationStatus regStat) throws IOException
	{
		String agentName = (String) regStat.agentNames.get(0);
		
		PropertiesBean.setProperty(PropertiesNames.REGISTERED_STATE/* "registered" */, "true");
		PropertiesBean.setProperty(PropertiesNames.AGENT_NAME/* "agentName" */, agentName/* regStat.agentName */);
		PropertiesBean.setProperty(PropertiesNames.EMAIL/* "email" */, regStat.email);
		PropertiesBean.setProperty(PropertiesNames.COUNTRY/* "country" */, regStat.country);
		PropertiesBean.setProperty(PropertiesNames.USER_NAME/* "userName" */, regStat.userName);
		
		if (regStat.hasPassword != null)
			PropertiesBean.setProperty(PropertiesNames.HAS_PSWD_STATE/* "hasPswd" */, "true");
		
		if (regStat.groupName != null)
			PropertiesBean.setProperty(PropertiesNames.GROUP/* "group" */, regStat.groupName);
		
		if (regStat.groupOwner != null)
			PropertiesBean.setProperty(PropertiesNames.GROUP_OWNER/* "groupOwner" */, regStat.groupOwner);
	}

	/* (non-Javadoc)
	 * @see dimes.IAgent#getCurrectGraphStateRecord()
	 */
	public String getCurrectGraphStateRecord()
	{
		return agentFrame.getCurrectGraphStateRecord();
	}

	//agentEngineTester and the debug mode were an idea that started out in 
	//version 0.6 and never got completed. The idea is to have a package which can
	//prob different asspects of the Agent remotly and give us an understanding as
	//to how the Agent behaves in different environments (User's computers) and
	//if a user complains that their agent "doesn't work" it may help us to find out
	//why. This was never implemented in any meaningful way, but the code for the
	//Agent Engine Tester can be found in dimes. util debug. - BoazH
	/* (non-Javadoc)
	 * @see dimes.IAgent#toggleDebug(boolean)
	 */
	public void toggleDebug(boolean state){
		debug=state;
		debugMode(); //change log behavior
/*		if (debug){
			agentEngineTester = AgentEngineTester.getInstance(this);
		}
		else
			agentEngineTester=null; //might already be null, but doesn't make a difference
	*/}
	
	
//Replaced by a direct call to rescheduleSchdTimer()	
//	/**
//	 * 
//	 */
//	public void changeMeasurementRate()
//	{
//		this.rescheduleSchedTimer();
//	}

}

