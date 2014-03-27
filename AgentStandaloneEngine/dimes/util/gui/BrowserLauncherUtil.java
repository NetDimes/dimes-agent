//package dimes.util.gui;
//
//import java.util.List;
//import java.util.logging.Logger;
//
//import javax.swing.JOptionPane;
//
//import dimes.util.logging.Loggers;
//import dimes.util.properties.PropertiesBean;
//import dimes.util.properties.PropertiesNames;
//import dimes.util.properties.PropertiesBean.NoSuchPropertyException;
//import edu.stanford.ejalbert.BrowserLauncher;
//import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
//import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
//
///**
// * <p>
// * This utility class was added in order to allow the opening of web pages in browser when the Help / FAQ / Forums<br>
// *  MenyItems are being selected in the Help menu.
// * </p>
// * <p>
// * The class creates a singleton instance.
// * </p>
// * <p>
// * It is using the <a href="http://browserlaunch2.sourceforge.net/">BrowserLauncher</a>
// * open source project. 
// * </p>
// * 
// * @author idob
// * @since 0.5.0
// */
//public class BrowserLauncherUtil {
//	// logger:
//	private static final Logger logger = Loggers
//	.getLogger(BrowserLauncherUtil.class);
//	
//	// Default URL in case of not finding one of the URLs:
//	private static final String DIMES_DEFAULT_URL = "http://www.netdimes.org";
//	
//	// References to optional URLs - to be used in calls to openURLinBrowser:
//	public static final int FAQ_URL = 1;
////	public static final int HELP_URL = 2;
//	public static final int FORUMS_URL = 3;
//	public static final int DIMES_URL = 4;
//	
//	// Singleton instance:
//	private static BrowserLauncherUtil browserLauncherUtil;
//
//	// The URL's addresses which is used in the help menu.
//	private String faqURL;
//	private String helpURL;
//	private String forumsURL;
//
//		
//	private BrowserLauncher launcher;
//	private List browsersList;
//	
//	// Private constructor - to be used by the singleton method getInstance()
//	private BrowserLauncherUtil() {
//		try {
//			launcher = new BrowserLauncher();
//			List tmpBrowsersList = launcher.getBrowserList();
//			if( tmpBrowsersList.size() == 0) {
//				logger.warning("BrowserLauncher can not find any browser for opening FAQ / Help.");
//				launcher = null;
//			}
//			else {
//				launcher.setNewWindowPolicy(true);
//				this.browsersList = tmpBrowsersList;
//				try {
//					faqURL = PropertiesBean.getProperty(PropertiesNames.FAQ_URL);
//				} catch (NoSuchPropertyException e) {
//					logger.warning("The FAQ URL can not be found. The Agent will try to use a default URL.");
//					e.printStackTrace();
//					faqURL = DIMES_DEFAULT_URL;
//				}
////				try {
////					helpURL = PropertiesBean.getProperty(PropertiesNames.HELP_URL);
////				} catch (NoSuchPropertyException e) {
////					logger.warning("The Help URL can not be found. The Agent will try to use a default URL.");
////					e.printStackTrace();
////					helpURL = DIMES_DEFAULT_URL;
////				}
//				try {
//					forumsURL = PropertiesBean.getProperty(PropertiesNames.FORUMS_URL);
//				} catch (NoSuchPropertyException e) {
//					logger.warning("The Forums URL can not be found. The Agent will try to use a default URL.");
//					e.printStackTrace();
//					forumsURL = DIMES_DEFAULT_URL;
//				}
//			}
//		} catch (BrowserLaunchingInitializingException e) {
//			logger
//					.warning("The HELP / FAQ Window can not be initialized due to System Problem.");
//			e.printStackTrace();
//			JOptionPane.showMessageDialog(null, "The HELP / FAQ Window can not be initialized due to System Problem.", "DIMES Warning", JOptionPane.WARNING_MESSAGE);
//		} catch (UnsupportedOperatingSystemException e) {
//			logger
//					.warning("The HELP / FAQ Window can not be initialized due to Unsupported Operating System Problem.");
//			e.printStackTrace();
//			JOptionPane.showMessageDialog(null, "The HELP / FAQ Window can not be initialized due to Unsupported Operating System Problem.", "DIMES Warning", JOptionPane.WARNING_MESSAGE);
//		}
//	}
//
//	
//	/**
//	 * This method is being called when the application wants to launch a web browser with  a specific url.<br>
//	 * The method checks if there is a launcher object (null if no browser was identified by the BrowserLauncher)<br>
//	 * And launching the appropriate URL.
//	 * 
//	 * 
//	 * @param url
//	 */
//	public void openURLinBrowser(int url) {
//		if( launcher == null ) {
//			logger.warning("BrowserLauncher can not open the asked URL. Please go to our website (http://www.netdimes.org) for getting the asked information.");
//			JOptionPane.showMessageDialog(null, "The HELP / FAQ Window can not be initialized due to System Problem.", "DIMES Warning", JOptionPane.WARNING_MESSAGE);
//			return;
//		}
//			
//		switch ( url ) {
//		case FAQ_URL:
//			logger.info("Opening The FAQ web page in DIMES web site.");
//			launcher.openURLinBrowser(browsersList, faqURL);
//			break;
///*		case HELP_URL:
//			logger.info("Opening The Help web page in DIMES web site.");
//			launcher.openURLinBrowser(browsersList, helpURL);
//			break;
//*/			
//		case FORUMS_URL:
//			logger.info("Opening The Forums web page in DIMES web site.");
//			launcher.openURLinBrowser(browsersList, forumsURL);
//			break;
//		case DIMES_URL:
//			logger.info("Opening The DIMES web site.");
//			launcher.openURLinBrowser(browsersList, DIMES_DEFAULT_URL);
//			break;	
//		default:
//			logger.warning("Unknown web site was asked to be opened. No Action will be taken.");
//			break;
//		}
//	}
//	public static BrowserLauncherUtil getInstance() {
//		if (browserLauncherUtil == null)
//			browserLauncherUtil = new BrowserLauncherUtil();
//		return browserLauncherUtil;
//	}
//}
