package dimes.platform;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

//import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

//import dimes.gui.AgentFrame;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;



/**
 * @author guyr, BoazH (0.5.2)
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PlatformDependencies
{

	public static final String[] linuxLibraryNames = {"libcallmtr","NetworkStack"};//libcallmtr needed for load, callmtr needed for loadLibrary
	public static final String[] macosxLibraryNames = {"callmtr"};
	public static final String[] windowsLibraryNames = {"MTR", "packettrain", "CommunicationDetector","treeroute","NetworkStack"};

	public static final String linuxLibraryExtension = "so";
	public static final String macosxLibraryExtension = ".jnilib";
	public static final String windowsLibraryExtension = "dll";

	public static final int linuxTraceNum = 4;
	public static final int macosxTraceNum = 4; // TODO MACOSX How was this value get chosen?
	public static final int windowsTraceNum = 10; // Measurements.MTR_PING_NUM

	//individual frames removed as of 0.5.2 - Boazh
	//public static final String linuxMainFrame = "dimes.gui.platform.LinuxAgentFrame";
	//public static final String macosxMainFrame = "dimes.gui.platform.LinuxAgentFrame";
	//public static final String windowsMainFrame = "dimes.gui.platform.WindowsAgentFrame";
	public static final String windowsMainFrame = "dimes.gui.platform.AgentFrame";
	
	public static final String windowsJarsigner = "jarsigner.exe";
	public static final String linuxJarsigner = "jarsigner";
	
	public static final int LINUX = 1;
	public static final int WINDOWS = 2;
	public static final int MACOSX = 3;
	
	// current detected settings :
	public static String[] currentLibraryNames;
	public static String currentLibraryExtension;
	public static int currentTraceNum;
	
	//currentMainFrame remains here as a legacy support. TODO: remove this after all reflection has been removed - Boazh
	public static String currentMainFrame = "dimes.gui.AgentFrame";  
	public static String jarsignerExecutable;
	public static int os;
	public static String PLATFORM;
	private static String themePackResource;

	static
	{
		System.out.print("detecting Operating system...");
		// TODO: Make the full OS version a Entry, so we know what ver of Windows etc. we have
		//		System.out.print(System.getProperty("os.name"));
		if (System.getProperty("os.name").startsWith("Linux"))
		{
			os = LINUX;
			PLATFORM="LIN";
//			if (System.getProperty("user.home").startsWith("/root"))
//				System.setProperty("user.home", "/tmp");
//			currentLibraryNames = linuxLibraryNames;
//			currentLibraryExtension = linuxLibraryExtension;
//			currentTraceNum = linuxTraceNum;
////			currentMainFrame = AgentFrame;
//			jarsignerExecutable = linuxJarsigner;
		}
		else
			if (System.getProperty("mrj.version") != null)
			{
				/* Believe it or not, this is how you check for Java 
				 * running on a MAC.
				 */
				os = MACOSX;
				PLATFORM="MAC";
//				if (System.getProperty("user.home").startsWith("/root"))
//					System.setProperty("user.home", "/tmp");
//				currentLibraryNames = macosxLibraryNames;
//				currentLibraryExtension = macosxLibraryExtension;
//				currentTraceNum = macosxTraceNum;
////				currentMainFrame = macosxMainFrame;
//				jarsignerExecutable = linuxJarsigner;
			}
			// else assume windows
			else
			{
				os = WINDOWS;
				PLATFORM="WIN";
//				currentLibraryNames = windowsLibraryNames;
//				currentLibraryExtension = windowsLibraryExtension;
//				currentTraceNum = windowsTraceNum;
//				currentMainFrame = windowsMainFrame;
//				jarsignerExecutable = windowsJarsigner;
			}
		System.out.println(os==WINDOWS ? "WINDOWS" : os==LINUX? "LINUX" : "MACOSX");
	}
	/**
	 * Set the look and feel for the application based on the current
	 * platform.
	 **/
//	public static void setLookAndFeel()
//	{
//		switch (os)
//		{
//			case LINUX :
//			case WINDOWS :
//				try
//				{
//					//System.out.println("setting default look and feel...");
//					//URL themePackResource = AgentFrame.class.getClassLoader().getResource(ResourceManager.TOXIC_THEME_PACK_RSRC);
//					themePackResource =PropertiesBean.getProperty("resources")+java.io.File.separatorChar + PropertiesBean.getProperty("skinFile");// toxicthemepack.zip"";
//				}catch(NoSuchPropertyException nspe){
//					try
//					{
//						System.out.println("Skin setting not found. Using default look and feel...");
//						//URL themePackResource = AgentFrame.class.getClassLoader().getResource(ResourceManager.TOXIC_THEME_PACK_RSRC);
//						themePackResource =PropertiesBean.getProperty("resources")+java.io.File.separatorChar+"toxicthemepack.zip";}catch(NoSuchPropertyException nspe1){
//							nspe1.printStackTrace();
//						}
//				}
//				
//				try{
//					if(!Boolean.parseBoolean(PropertiesBean.getProperty(PropertiesNames.ENABLE_SKIN_STATE)))
//						{
//						String laf = UIManager.getSystemLookAndFeelClassName();
//						UIManager.setLookAndFeel(laf);
//						}
//					else
//						{
//						SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(themePackResource));
//						Class lafClass = AgentFrame.class.getClassLoader().loadClass("com.l2fprod.gui.plaf.skin.SkinLookAndFeel");
//						LookAndFeel laf = (LookAndFeel) lafClass.newInstance();
//						UIManager.setLookAndFeel(laf);
//						}
////					GtkLookAndFeel dLook = new GtkLookAndFeel();
////					String dLook = UIManager.getSystemLookAndFeelClassName();
////					SynthLookAndFeel laf = new SynthLookAndFeel();
////					laf.load(new URL("file:///"+PropertiesBean.getProperty("resources")+java.io.File.separatorChar+"synthlaf.xml"));
//					
////					UIManager.put("RootPaneUI", new com.l2fprod.gui.plaf.skin.SkinRootPaneUI());
////					UIManager.put("WindowButtonUI", new com.l2fprod.gui.plaf.skin.SkinWindowButtonUI());
////					UIManager.put("RadioButtonUI", new com.l2fprod.gui.plaf.skin.SkinRadioButtonUI());
////					UIManager.put("RadioButtonMenuItemUI", new com.l2fprod.gui.plaf.skin.SkinRadioButtonMenuItemUI());
////					UIManager.put("ScrollBarUI", new com.l2fprod.gui.plaf.skin.SkinScrollBarUI());
////					UIManager.put("ScrollPaneUI", new javax.swing.plaf.basic.BasicScrollPaneUI());
////					UIManager.setLookAndFeel(dLook);
////					UIManager.setLookAndFeel(laf);
////					UIManager.put("com.l2fprod.gui.plaf.skin.SkinTabbedPaneUI",null);
//					
////					U .put("ScrollBar.highlight",new ColorUIResource(255,0,0));
////					UIManager.setLookAndFeel(dLook);
////					LookAndFeel LAF = UIManager.getLookAndFeel();
////					LAF.installColors(javax.swing.JScrollBar, Color.CYAN.toString(), Color.BLUE.toString());
////					UIManager.put("ScrollBar.highlight",new ColorUIResource(255,0,0));
////					UIManager.put("TextPane.font", new FontUIResource("Dialog", Font.PLAIN, 14));
////					UIManager.put("ScrollPane.font", new FontUIResource("Dialog", Font.PLAIN, 14));
////					ImageIcon closeIcon = new ImageIcon("file:///"+PropertiesBean.getProperty("resources")+java.io.File.separatorChar+"kde/close.png", "close");
////					UIManager.put("RadioButton.light", new ColorUIResource(255,0,0) );
////					UIManager.put("RadioButton.shadow", new ColorUIResource(0,255,0) );
////					UIManager.put("RadioButton.select", new ColorUIResource(0,255,0) );
//					
////					UIManager.put("RadioButtonUI", new BasicRadioButtonUI());
//					
////					UIManager.put("RadioButton.background", new ColorUIResource(0,0,255) );
//					UIManager.getLookAndFeelDefaults().put("ClassLoader", AgentFrame.class.getClassLoader());
////					UIManager.setLookAndFeel(dLook);
////					UIDefaults defaults = UIManager.getDefaults(); 
////					StringBuilder std = new StringBuilder("");
////					java.util.Enumeration enm = defaults.elements();
////					Object ob;
////					while (enm.hasMoreElements()){
////						 ob = enm.nextElement();
////						 if (ob.toString().contains("com.l2f"))
////						 std.append(ob.toString()+"\n");
////						
////					}
////					std.append(defaults.get("com.l2fprod.gui.plaf.skin.SkinSplitPaneUI")+"\n");
////					std.append(defaults.get("com.l2fprod.gui.plaf.skin.SkinTabbedPaneUI")+"\n");
////					 javax.swing.JOptionPane.showMessageDialog(null, std.toString());
////					LookAndFeel LAF = UIManager.getLookAndFeel();
//					
//				}
//				catch (Exception ex)//This catches four or five different exceptions raised by look and feel. 
//				{
//					// TODO Auto-generated catch block
//					ex.printStackTrace();
//				}
//				;
//				break;
//			case MACOSX :
//		// We use the default look and feel
//		}
//	}

	public static int getCurSysType()
	{
		return os;
	}
}