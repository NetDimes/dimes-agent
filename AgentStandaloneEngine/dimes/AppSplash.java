/*
 * Created on 18/04/2004
 */
package dimes;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import dimes.AgentGuiComm.GUICommunicator;
import dimes.AgentGuiComm.GUIConnectorBean;
import dimes.AgentGuiComm.comm.channels.SelectorThread;
import dimes.util.FileHandlerBean;
import dimes.util.Lock;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;


/**
 * <p>
 * AppSplash is the entry point of Agent classes when run by AgentLauncher.
 * AgentLauncher waits for the main method of the entry point to be over, and
 * then re-launches according to system property value. This means that main()
 * should end only after the Agent (and AgentFrame) actually end.
 * agentFrameAdapter and agentFrameLock are used for this purpose.
 * </p>
 * <p>
 * In version 0.5.0 this class was adapted to support the non-interactive (service) mode.
 * </p>
 * <p>
 * Version 0.5.2 - parser added to handle command-line arguments with a space in them.<br>
 * Most reflection removed since it no longer necessary.
 * </p>
 * 
 * @author anat, idob (version 0.5.0), BoazH(version 0.5.1,0.5.2, 0.6.0)
 */
/**
 * @author user
 *
 */
/**
 * @author user
 *
 */
public class AppSplash {
	//	@SuppressWarnings("unused")
	private String mainFrameName = null;
//	private static final String splashPath = "/resources/DimesSplash.jpg";
	private static final String VERSION_STR = "0.6.0"; //Unused, since the version number is retrieved from properties.xml.
//	private static boolean GUIAvailable = true; //Is there GUI available in this system? 
//	private JFrame mainClass = null;
//	private Frame splashFrame = null;
	private Logger logger;
	private FileHandlerBean fileHandlerBean = new FileHandlerBean();
	private static GUIConnectorBean ConnectorBean;// = GUIConnectorBean.getInstance();
	private static GUICommunicator GUIComm;
	private static boolean debug = false;


//	public AgentFrame_WindowClosed_WindowAdapter agentFrameAdapter = null; 	// used for identification of agent exiting (AgentFrame closing)
	public Lock agentFrameLock = null;  //On restart, the agentFrameLock tells the Agent when it's ok to open a new frame
//	public static DipslayServer lockSocket = null; // used for keeping a single copy of the application
	private Agent mainObj = null; //Object that holds main Agent class. Could be either an Agent or AgentFrame (depending on interactive or not) so type is Object 

	
	public static final String INTERNAL_RESTART_PROPERTY = "dimes.internal.restart";

	private static final String mainClassName = "dimes.Agent";


	/** Constructor. Initiates:<br>
	 * FileHandlerBean<br>
	 * PropertiesBean<br>
	 * Logger<br>
	 * lockSocket and agentFrameLock<br>
	 * Launches the Agent in either interactive or non-interactive mode.
	 *  
	 * @param args String array that represent the command prompt arguments.
	 * @throws IOException 
	 */
	public AppSplash(String[] args) throws IOException {

		//Instead of referring to args[0] multiple times, we'll save the location of the properties.xml
		//file once, and refer to that. (The StackTrace doesn't get logged because there is no logger yet)
		try{
			FileHandlerBean.setPropertiesLocation(parseArgs(args));
		}catch(Exception e){e.printStackTrace();}

		//We initialize the PropertiesBean before the Instance check so that we can get a logger
		PropertiesBean.init(FileHandlerBean.getPropertiesLocation());                
		this.logger = Loggers.getLogger(this.getClass());
		initResources();
//		ConnectorBean = GUIConnectorBean.getInstance();
		GUIComm = GUICommunicator.getInstance();
		GUIComm.sendLog(Level.INFO,"", "Test Log Info");
		try {
			new Thread(dimes.AgentGuiComm.comm.CommunicationsThread.getInstance(33333)).start();
			new Thread(new dimes.AgentGuiComm.comm.channels.SelectorThread( dimes.AgentGuiComm.comm.Dispatcher.getInstance(dimes.AgentGuiComm.AgentFrameFacade.getInstance(), dimes.AgentGuiComm.PropertiesFrameFacade.getInstance()))).start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		logger.log(Level.parse("1000"), "Test INT log, Severe");
		
		try {
			// make sure that just one dims application instance will exist at this machine.
			if (!AppSplash.acquireFirstInstance()){
				System.out.println("First Instance fail");
//				if (GUIAvailable)
//				JOptionPane.showMessageDialog(null, "The DIMES Agent cannot start. \nPlease make sure that no other agents \n(including the DIMES Service) are currently running. ");
				System.exit(0); // exit if one dimes application instance already exist.
			}else logger.info("First Instance OK");
		} catch (IOException e) {
			logger.severe(" Agent Can't check for existance of first instance");
			e.printStackTrace();
			return;
		}
		
		/*
		 * Checks if all the necessary working directories exists. In case that
		 * not - creates them and returns true.
		 */
		fileHandlerBean.verifyWorkingDirectories();

		/* clean the old log files and prepare out files for send. */
		try {
			fileHandlerBean.cleanDirectories();
		} catch (Exception e) {
			logger
			.severe("The Agent can't prepare his files. Please consult The DIMES Team.");
			e.printStackTrace();
		}

		//Logger moved up so that First Instance can log
		//	this.logger = Loggers.getLogger(this.getClass());
		this.agentFrameLock = new Lock();


		try {
				debug=Boolean.parseBoolean(PropertiesBean.getProperty(PropertiesNames.DEBUG_STATE));
		} catch (NoSuchPropertyException e) {
				debug=false; //default
		}

		
		//launch Agent in either interactive or non-interactive mode. Default is interactive.
//		if (false) {
			// Showing the SplashImage
//			showSplashImage();
//			showMainFrame();//args);
//		} else {
			runNonInteractiveMode();

//		}
		
//		// Dispose of the splash screen
//		if (splashFrame != null)
//			splashFrame.dispose();
	}

	/**<p>
	 * parseArg deals with malformed command-line argument for the location of properties.xml
	 * In case that there are no arguments, it will prompt the user.
	 * In case that there are more than one argument, it assumes that the string has spaces in it 
	 * (Example: C:\program Files\DIMES)  and parse all the arguments into one sting.
	 * </p>
	 * 
	 * @param:argsIn
	 *  @author:BoazH
	 */
	private String parseArgs(String[] argsIn){
		StringBuffer argOut=null;

		if (argsIn.length!=1){

			//No arguments at all
			if(argsIn.length==0){
				String userInput = JOptionPane.showInputDialog("The DIMES agent can't locate the file: properties.xml\n" +
				"Please enter the file location below, including file name. ");
				java.util.StringTokenizer st = new java.util.StringTokenizer(userInput);
				argOut = new StringBuffer(st.nextToken());
				
				while(st.hasMoreTokens()){
					argOut.append(" ");
					argOut.append(st.nextToken());
				}                     
			}
			
			//More then one argument (spaces in argument string)
			else{
				argOut = new StringBuffer(argsIn[0]);
				
				for(int i=1;i<argsIn.length;i++){
//					if("NOGUI".equals(argsIn[i])) GUIAvailable=false;
//					else{
					argOut.append(" ");
					argOut.append(argsIn[i]);
					}
				}
			

		}
		else argOut=new StringBuffer(argsIn[0]);


		return argOut.toString();
	}


	/**Launches the Agent in non-interactive (service) mode. 
	 * Calls the default Agent constructor and then exits if there are no problems.
	 *	 
	 * @author idob
	 * 
	 * @since 0.5.0 
	 */
	private void runNonInteractiveMode() { 
		try {
//			if(GUIAvailable)
//				mainObj = GuiAgent.getInstance();
//			else 
				mainObj = NonGuiAgent.getInstance();
			
		} catch (Exception e) {
			logger.severe("A main class object can not be initialized");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
//		if (!(mainObj instanceof Agent)) {
//			throw new RuntimeException("Unexpected main class: "
//					+ mainObj.getClass().getName());
//		}
//		else {
			try {
				mainObj.initAgent();
			} catch (Exception e) {
				logger.severe("An Exception occured.");
				e.printStackTrace();
				throw new RuntimeException(e);
			} 	
//		}
		if (debug) mainObj.toggleDebug(debug);
	}

	public Agent getAgent(){
		try{
			return mainObj;
		}
		catch(ClassCastException cce){
			return null;
		}
	}
	

	/**
	 * Launches the Agent in interactive mode. It's purpose -
	 * creating the main Agent frame.
	 * 
	 * @author idob
	 * 
	 * @since 0.5.0
	 */
//	private void showMainFrame(){
//		
//		mainFrameName = PlatformDependencies.currentMainFrame;
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					mainObj = new AgentFrame();
//
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//				
//				if (!(mainObj instanceof JFrame))
//					throw new RuntimeException("Unexpected main class: "
//							+ mainObj.getClass().getName());
//				
//				agentFrameAdapter = new AgentFrame_WindowClosed_WindowAdapter();
//				((JFrame) mainObj).addWindowListener(agentFrameAdapter); //Add a listener to release the lock when the AgentFrame closes
//			}
//		});
//
//	}

	/**
	 * This method checks to see if the SHOW_SPLASH property in the properties
	 * file is true. If the answer is yes - it creates the SplashScreen window
	 * and shows it on screen.
	 * 
	 * @author idob
	 * 
	 * @since 0.5.0
	 */
//	private void showSplashImage() {
//		
//		boolean showSplash = false;
//		String showSplashStr = null;
//		
//		//check to see if properties.xml has a SHOW_SPLASH proeprty
//		try {
//			showSplashStr = PropertiesBean
//			.getProperty(PropertiesNames.SHOW_SPLASH);
//		} catch (NoSuchPropertyException e) {
//			logger.warning("SHOW_SPLASH: No such property ");
//			e.printStackTrace();
//			return;
//		}
//		
//		//If the property exits, but has no valid value, set it to true
//		if (!PropertiesBean.isValidValue(String.valueOf(showSplashStr))) {
//			// Default:
//			showSplash = true;
//			try {
//				PropertiesBean.setProperty(PropertiesNames.SHOW_SPLASH, "true");
//			} catch (IOException e) {
//				logger.warning("SHOW_SPLASH: Property could not be set");
//				e.printStackTrace();
//				return;
//			}
//		} else //Property exists and has a value
//			showSplash = Boolean.valueOf(showSplashStr).booleanValue();
//		
//		//process the property
//		if (showSplash) {
//			try {
//				// Read the image data and display the splash screen
//				SwingUtilities.invokeAndWait(new Runnable() {
//					public void run() {
//						URL imageURL = AppSplash.class.getResource(splashPath);
//						if (imageURL != null) {
//							splashFrame = SplashScreen.splash(Toolkit
//									.getDefaultToolkit().createImage(imageURL));
//						} else {
//							logger.warning("Splash image not found");
//						}
//					}
//				});
//			} catch (Exception e) {
//				logger.warning("Splash image can not be displayed");
//				e.printStackTrace();
//			}
//		}
//	}

	/**
	 * Make sure that just one instance will be exist. 
	 * 
	 * checks whether there's already another instance of the Agent running, by
	 * trying to open a ServerSocket on a specific port.
	 * 
	 * 
	 * @return true if this is the first instance
	 * @throws IOException
	 */
	public static boolean acquireFirstInstance() throws IOException {
		//	lockSocket = new DipslayServer(33333);
		return GUIConnectorBean.getInit();
	}

	/** Main method of AppSplash. If there is no AgentLauncher, this is where execution starts.
	 * Calls the AppSplash constructor and passes on external arguments. Then waits for the agentFrameLock
	 * to release (IE Agent is shutting down). When lock is available this method does some housekeeping and
	 * either exits or restarts the Agent as neccessary.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {		

		
		// workaround flicker bug when dragging window
//		if (interactive) {
//			System.setProperty("sun.awt.noerasebackground", "true");
//			Toolkit.getDefaultToolkit().setDynamicLayout(true);
//		}
//		
		boolean restart = true; //restart is initially set to true to run the loop. It is then set to false by default. A check of the internal property sets it again at the end to let the Agent restart if desired
	
		while (restart) {
			
			System.setProperty(AppSplash.INTERNAL_RESTART_PROPERTY, String
					.valueOf(false));
			AppSplash app = new AppSplash(args);

			app.agentFrameLock.waitFor();
			
//			if( interactive ) {
//				
//				// After disposing the application, the following code will run,
//				((JFrame) app.mainObj).removeWindowListener(app.agentFrameAdapter);
//				WindowListener[] listeners = ((JFrame) app.mainObj).getWindowListeners();
//
//				for (int i = 0; i < listeners.length; i++) {
//					System.out.println(listeners[i]);
//					System.out.println(listeners[i].getClass());
//					((JFrame) app.mainObj).removeWindowListener(listeners[i]);
//				}
//				((JFrame) app.mainObj).dispose();
//			}
			
			//TODO: Instead of this, get the "connected" value from the connector. If still connected, disconenct. 
//			if ((AppSplash.lockSocket != null))//&& !AgentFrame.getMinToTray())
//				try {
//					AppSplash.lockSocket.close();
//				} catch (IOException e) {
//					/* System.err.println */app.logger
//					.warning("couldn't release lock socket: "
//							+ e.toString());
//				}

			restart = Boolean.valueOf( //If the resart property is true, the variable is set to true, and the while loops runs again (Agent restart)
					System.getProperty(AppSplash.INTERNAL_RESTART_PROPERTY))
					.booleanValue();
			System.out.println("Restart = " + restart);
			Loggers.resetLoggers();
		}

	}

	private void initResources() throws IOException//boolean lookForResources, boolean resourcesFromStartup) throws MalformedURLException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchMethodException
	{
		File classJar = null;
		String agentResourcesPath=null;
		String agentBasePath = null;
		URLClassLoader classLoader = null;
		
//	    System.out.println("-- reset - lookForResources: "+lookForResources);
		Vector resources = new Vector();
		try {
			agentBasePath = PropertiesBean.getProperty(PropertiesNames.BASE_DIR);
			agentResourcesPath = PropertiesBean.getProperty(PropertiesNames.RESOURCES_DIR);
		} catch (NoSuchPropertyException e) {
			e.printStackTrace();
		}
/*		if (lookForResources)//true after runStartup
        {
		    Object[] result = new Object[1];
		    Object[] params = new Object[] {new Boolean(resourcesFromStartup), this.agentResourcesPath};
		    this.callStartupMethod("getLatestResources",params, result);
//            resources = this.getLatestResources(resourcesFromStartup);
		    
		    resources = (Vector)result[0];
		    
            //clean old resources
    		File resourcesDir = new File(this.agentResourcesPath);
    		File[] allResources = resourcesDir.listFiles(
    		        new FileFilter()
    				{public boolean accept(File pathname) 
    				{return pathname.getName().endsWith(".jar") || pathname.getName().endsWith(".dll");}
    			});
    		
    		Vector old = new Vector(Arrays.asList(allResources));
    		old.removeAll(resources);
    		this.callStartupMethod("handleOldVersions", new Object[] {old}, null);
    		
    		if (this.oldJars!= null)
            {
                this.callStartupMethod("handleOldVersions", new Object[] {this.oldJars}, null);
                this.oldJars = null;
            }    		    
        }*/

		//call gc only on 2nd call to init, when lookForResources=true
//		this.reset(/*lookForResources*/);

//		    Object[] result = new Object[1];
//		    Object[] params = new Object[] {new Boolean(resourcesFromStartup), agentResourcesPath};
//		    this.callStartupMethod("getLatestResources",params, result);
//            resources = this.getLatestResources(resourcesFromStartup);
		    
//		    resources = (Vector)result[0];
		    
            //clean old resources

		File resourcesDir = new File(agentResourcesPath);
		File[] allResources = resourcesDir.listFiles(
		        new FileFilter()
				{public boolean accept(File pathname) 
				{return pathname.getName().endsWith(".jar") || pathname.getName().endsWith(".dll");}
			});
		
//TODO		URI jarURI = classJar.toURI();  //.toURL() is deprecated, Java recommends converting to URI first. 
//TODO		URL jarURL = jarURI.toURL();
		Vector jarResources = new Vector();
		for (int i=0; i<allResources.length; ++i)
		{
		    File aResource = allResources[i];//(File)resources.get(i);
		    if (aResource.getName().endsWith(".jar"))
		        jarResources.add(aResource);
		}
		jarResources.trimToSize();
		
		URL[] urls = new URL[jarResources.size()+1];//classJar + resources
//TODO		urls[0] = jarURL;
		for (int i=1; i<urls.length; ++i)
			try {
				urls[i] = (((File)jarResources.get(i-1)).toURI()).toURL();
				System.out.println(urls[i].toString());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}//.toURL();
			classLoader = (URLClassLoader)this.getClass().getClassLoader();Class sysclass = URLClassLoader.class;
		    for(URL u:urls)
			try {
		        Method method = sysclass.getDeclaredMethod("addURL", URL.class);
		        method.setAccessible(true);
		        method.invoke(classLoader, new Object[] { u });
		    } catch (Throwable t) {
		        throw new IOException("Error, could not add URL to system classloader");
		    }

//		classLoader = new URLClassLoader(urls);
	}
	
	
	/*
	 * The following two methods are unused as far as I can tell. I'm leaving them here
	 * just in case, but they should be removed eventually. - Boazh 6/09
	 */
	
/*	public void stop() {
		System.out.println("AppSplash stop");// debug
		try {
			if (acquireFirstInstance())// no running instance of agent
			{
				System.out.println("no running instance - returning");// debug
				releaseInstance();
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		if (mainObj == null) {
			System.err.println("mainObj is null");// debug
			return;
		}
		if (mainClass == null) {
			System.err.println("mainClass is null");// debug
			return;
		}
		try {   //Below are explicit casts to make the compiler happy. They mean nothing.
			//		mainClass.getMethod("exit",(Class<?>[])null).invoke(mainObj,(Object)null);
			((AgentFrame)mainObj).StopButton_actionPerformed(new ActionEvent(null, 0, ""), false);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}*/

/*	void releaseInstance() {
		if (AppSplash.lockSocket != null)
			try {
				AppSplash.lockSocket.close();
			} catch (IOException e) {
				System.err.println("couldn't release lock socket: "
						+ e.toString());
			}
	}*/

	/**
	 * Called by the service wrapper to signal AppSplash that
	 * the Agent is running in non-interactive mode (no GUI).  
	 */
//	public static void setNonInteractive() {
//		AppSplash.interactive = false;
//	}

	/**
	 * Adaptor class to release the frame lock when the Agent closes. 
	 * 
	 */
//	class AgentFrame_WindowClosed_WindowAdapter extends WindowAdapter {
//		public void windowClosed(WindowEvent e) {
//			agentFrameLock.release();
//		}
//	}
}
