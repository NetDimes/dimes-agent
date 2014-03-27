package dimes.util.registration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

//import org.dom4j.Document;
//import org.dom4j.DocumentException;
//import org.dom4j.Element;
//import org.dom4j.io.SAXReader;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

//import registration.AgentFacade;
//import registration.AgentFrameFacade;
import registration.Registrator;
import sun.reflect.generics.visitor.Reifier;
import dimes.comm2server.StandardCommunicator;
import dimes.gui.registration.RegisterDetailsParentFrame;
import dimes.state.user.PropertiesStatus;
import dimes.state.user.RegisterDetailsHandler;
import dimes.state.user.RegistrationStatus;
import dimes.state.user.RegistrationThread;
import dimes.state.user.UpdateDetailsHandler;
import dimes.state.user.UpdateDetailsStatus;
import dimes.state.user.UpdateDetailsThread;
import dimes.util.FileHandlerBean;
import dimes.util.ResourceManager;
import dimes.util.XMLUtil;
import dimes.util.gui.GroupGetter;
import dimes.util.gui.GuiUtils;
import dimes.util.gui.SpringUtilities;
import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * <p>
 * The registartion frame which aooears ib=n the first run of the Agent - after installation.
 * </p>
 * <p>
 * In version 0.5.0 was added a support in non interactive (service) mode.
 * </p>
 * 
 * @author anat, idob (version 0.5.0)
 */

@SuppressWarnings("all")
public class RegistrationFrame extends JFrame implements UpdateDetailsParentFrame, RegisterDetailsParentFrame
{

	private static final String USERNAME_LABEL = "Username";
	private static final String EMAIL_LABEL = "Email";
	private static final String COUNTRY_LABEL = "Country";

	
	private String propFileName;
	private Logger logger;
	private String myName;
	private static final String PASSWORD_LABEL = "Password";
	private static final String CONFIRM_PASSWORD_LABEL = "Password";

	private static final Color BACKGROUND_COLOR = Color.WHITE;
	private static final Color NEXT_BACK_PANEL_BACKGROUND_COLOR = new Color(230, 240, 250);
	private static final Color HEADER_TEXT_COLOR = new Color(20, 40, 140);
	private static final Color MESSAGES_TEXT_COLOR = new Color(20, 40, 170);
	private static final Color ERROR_MESSAGES_TEXT_COLOR = Color.RED;
	private static final Color OPTION_TEXT_COLOR = new Color(0, 104, 76);
	private static final String AGENTNAME_LABEL = "agent name";
	private static final String GROUP_LABEL = "Group";
	private static final String EXISTING_GROUP_LABEL = "Join group";
	private static final String CREATE_GROUP_LABEL = "Create new group";

	private static final String WHY_IS_THAT_REQUIRED_QUESTION = "   Why is that required ?";
	private static final String WHY_IS_THAT_REQUIRED_MESSAGE = "   You are now a part of the DIMES community. \n"
			+ " The only thing neccesary for you to provide is a user name.\n " + " All other details are stored privately in our records and \n"
			+ " All communication is secure.";

	private static final String WHY_IS_THAT_IMPORTANT_MESSAGE = "A DIMES User may install multiple Agents " + "on different machines.\n"
			+ "If you already have another agent out there, adding " + "This one to your group might double and tripple your "
			+ "measurements number, thus increasing your part in the " + "Internet DIMES Monopoly.\n" + "check out the monopoly on www.netdimes.org\n";

	private static final String WHY_IS_THAT_IMPORTANT_QUESTION = "   Why is that important ?";

	private static final String WHAT_IS_THIS_AGENT_NAME_QUESTION = "   What this 'Agent Name' stands for ?";
	private static final String WHAT_IS_THIS_AGENT_NAME_MESSAGE = "   How the hell should i know ? i am just a program. ";

	private static final String I_NEVER_HAS_A_PASSWORD_QUESTION = "Hey i never had a password...";
	private static final String I_NEVER_HAS_A_PASSWORD_MESSAGE = "You can insert your email address in case you " + "have not visited our login page yet. \n "
			+ "In case of problem please email us at support@www.netdimes.org";

	private static final String WHATS_THIS_GROUPS_THING_MESSAGE = "  Your DIMES Agent is already a part of The Internet Monopoly! \n "
			+ "  Join a specific group in order to share your \"Internet currency\" \n" + "  with some fellow members."
			+ "  So either join a team from the list or create a new one yourself.";

	private static final String WHATS_THIS_GROUPS_THING_QUESTION = "   What's with those groups ?";

	private static final String WHAT_ARE_ALL_THOSE_NAMES_QUESTION = "  What are all those funny names ?";
	protected static final String WHAT_ARE_ALL_THOSE_NAMES_MESSAGE = " Those are the names that you supplied in your previous registration\n"
			+ "You may change them if you wish, or just press 'confirm' if you don't care...\n";

	private static final int JOIN_GROUP_ACTION = 1;
	private static final int CREAT_GROUP_ACTION = 2;
	private static final int NOP_GROUP_ACTION = 3;

	private static final int USER_DECISION_SCREEN = 1;
	private static final int NEW_USER_DETAILS_SCREEN = 2;
	private static final int VETERAN_USER_DETAILS_SCREEN = 3;
	private static final int AGENT_DETAILS_SCREEN = 4;
	private static final int GROUP_DETAILS_SCREEN = 5;
	private static final int REGISTRATION_SUCCESS_SCREEN = 6;
	private static final int NEW_USER_REGISTRATION_PROGRESS_SCREEN = 7;
	private static final int VETERAN_USER_REGISTRATION_PROGRESS_SCREEN = 8;
	private static final int AGENT_NAME_UPDATE_PROGRESS_SCREEN = 9;
	private static final int GROUP_UPDATE_PROGRESS_SCREEN = 10;
	private static final int INFORM_OLD_USER_VALIDATION_SCREEN = 11;
	private static final int VALIDATION_PROGRESS_SCREEN = 12;
	private static final int VALIDATION_RESULT_SCREEN = 13;
	private static final int VALIDATION_RESULT_PROGRES_SCREEN = 14;
	private static final int VALIDATION_SUCCESS_SCREEN = 15;
	private static final int VETERAN_AGENTS_SCREEN = 16;
	private static final int VETERAN_AGENT_REGISTRATION_PROGRESS_SCREEN = 17;

	private JPanel screen1 = new JPanel();
	private JPanel screen2 = new JPanel();
	private JPanel screen3 = new JPanel();
	private JPanel screen4 = new JPanel();
	private JPanel screen5 = new JPanel();
	private JPanel screen6 = new JPanel();
	private JPanel screen7 = new JPanel();
	private JPanel veteranAgentsScreen = new JPanel();

	private JPanel successPanel = new JPanel();
	JPanel nextPrevOptions = new JPanel();

	private JLabel screen2ErrorMessage = new JLabel("");
	private JLabel screen3ErrorMessage = new JLabel("");
	private JLabel screen4ErrorMessage = new JLabel("");
	private JLabel screen5ErrorMessage = new JLabel("");
	private JLabel screen7ErrorMessage = new JLabel("");
	private JLabel veteranAgentsScreenWarning1 = new JLabel("");
	private JLabel veteranAgentsScreenWarning2 = new JLabel("");

	private JButton prevButton;
	private JButton nextButton;

	// Registration Details :
	private String userName = "";
	private String agentName = "";
	private String country = "";
	private String email = "";

	private String groupName;
	private int groupAction = NOP_GROUP_ACTION;

	private int currentScreen = USER_DECISION_SCREEN;

	RegisterDetailsHandler registerHandler;
	UpdateDetailsHandler propertiesUpdateHandler;
	private StandardCommunicator communicator;

	// exposable gui components :
	private JRadioButton addNewUserButton;
	private JRadioButton addAgentToUSerButton;
	private JRadioButton mergeAgentsButton;
	private JRadioButton existingGroupRadioButton;
	private JRadioButton createGroupRadioButton;
	private JComboBox groupTextField;
	private JTextField agentNameTextField;
	private JPasswordField passwordConfirmTextField;
	private JTextField oldUsernameTextField;
	private JPasswordField passwordTextField;
	private JTextField emailTextField;
	private JComboBox countryComboBox;
	private JTextField usernameTextField;
	private RegistrationThread regThread;
	private UpdateDetailsThread updateThread;
	private JButton proxyButton;
	protected ProxyDialog proxyDialog = new ProxyDialog(this);
	private JRadioButton whyIsThatImportantButton;
	private JRadioButton whyIsItRequiredButton;
	private JButton finishButton;
	private JRadioButton whyIsGroupsRequiredButton;
	private ProgressMonitorPanel regMonitorPanel;
	//veteranAgentsScreen
	private JComboBox veteranAgentsComboBox;

	//    private String iconPath = ResourceManager.getResourceName(ResourceManager.HELPICON_GIF_RSRC);
	private boolean agentWasWithValidID = false;
	private boolean veteranAgent = false;//if true, user wants to merge with agent previously installed
	private boolean ignoreActiveAgentWarning = false;//if true, user wants to use a specific agent name, even if it doesn't seem to be uninstalled.
	private static final String NEXT_TEXT = "Next >";
	private static final String BACK_TEXT = "< Back";
	private static final String FINISH_TEXT = "Finish";
	private static final String VALIDATE_TEXT = "Validate >";
	private String agentID;
	private static String AgentVer;
	private JRadioButton whatAreAllThoseNamesButton;
	private JTextField validationAgentnameTextField;
	private static final String WELCOME_VALIDATION_HTML_STRING = "<html>" + "<body>" + "<p align=\"left\">" + "<font color=#113355 face=\"Arial\" size=\"4\">"
			+ " Dimes Registration </font>" + "<br>" + "<hr size=2 width=\"80%\">" + "<font color=#113311 face=\"Arial\" size=\"4\">"
			+ " Your Agent has been upgraded into version 0.5.2<br>" + " In order to work properly it must syncronize with <br>" + " The DIMES Server.<br><br>"
			+ " " + " Please confirm your details with our server." + "</font>" + "</p>" + "</body>" + "</html>";
	private static String REGISTRATION_SUCCESS_HTML_LABEL = "<html>" + "<body>" + "<p align=\"left\">" + "<font color=#113355 face=\"Arial\" size=\"4\">"
			+ " Registration commited succesfuly. </font>" + "<br>" + "<hr size=2 width=\"80%\">" + "<font color=#113311 face=\"Arial\" size=\"4\">"
			+ " Dimes Agent version "+AgentVer+" was successfuly registered.<br>"+ "</font>" + "</p>" + "</body>" + "</html>";
	private JTextField validationUsernameTextField;
	private JComboBox validationCountryComboBox;
	private JTextField validationEmailTextField;
	private String baseDir;;
	private JLabel enterDetailsLabel4;
	private JLabel enterDetailsLabel5;
	private JLabel enterDetailsVeteranAgentLabel1;
	private JLabel enterDetailsVeteranAgentLabel2;;

	// Being initialized in case of non interactive (service) mode.
//	private AgentFacade theAgent;
	private Registrator theRegistrator;
	// Being initialized in case of interactive (Agent) mode.
//	private AgentFrameFacade myParentAgentFrame;
	/**
	 * This constructor is being called in case of non interactive (service) mode - 
	 * In that case the only theAgent is being initialized and myParentAgentFrame is null.
	 * 
	 * @param comm
	 * @param agentIDAlreadyExist
	 * @param theAgent
	 * @author idob
	 * @since 0.5.0
	 */
	public RegistrationFrame(StandardCommunicator comm, boolean agentIDAlreadyExist, Registrator theAgent) {
		this(null, comm, agentIDAlreadyExist);
		this.theRegistrator = theAgent;
	}
	/**
	 * a constructor for the registration frame
	 * 
	 * 
	 * @param frame
	 * @param comm
	 * @param agentIDAlreadyExist indicate whether to register or validate the registration(0.3->0.4)
	 */
	public RegistrationFrame(Registrator frame, StandardCommunicator comm, boolean agentIDAlreadyExist)
	{
		super("Dimes Registration");
		setVisible(false);
		logger = Loggers.getLogger(this.getClass());
		agentWasWithValidID = agentIDAlreadyExist;
		if (agentWasWithValidID)
		{
			this.currentScreen = RegistrationFrame.INFORM_OLD_USER_VALIDATION_SCREEN;
			try
			{
				agentID = PropertiesBean.getProperty(PropertiesNames.AGENT_ID);
				baseDir = PropertiesBean.getProperty(PropertiesNames.BASE_DIR);
				RegistrationFrame.AgentVer=PropertiesBean.getProperty(PropertiesNames.AGENT_VERSION);
			}
			catch (NoSuchPropertyException e)
			{
				Loggers.getLogger().error("Agent informed having agentID without having one. Resuming regular registration");
				agentWasWithValidID = false;
				this.currentScreen = RegistrationFrame.USER_DECISION_SCREEN;
			}
		}
		theRegistrator = frame;
		communicator = comm;
		registerHandler = new RegisterDetailsHandler(comm);
		propertiesUpdateHandler = new UpdateDetailsHandler(comm);
		jbInit();
	}
	
	public String getREGISTRATION_SUCCESS_HTML_LABEL(){
		if(null==RegistrationFrame.AgentVer){
			try
			{
				RegistrationFrame.AgentVer=PropertiesBean.getProperty(PropertiesNames.AGENT_VERSION);
			}
			catch (NoSuchPropertyException e)
			{
				Loggers.getLogger().error("Agent informed having agentID without having one. Resuming regular registration");
				agentWasWithValidID = false;
				this.currentScreen = RegistrationFrame.USER_DECISION_SCREEN;
			}
		}
		return "<html>" + "<body>" + "<p align=\"left\">" + "<font color=#113355 face=\"Arial\" size=\"4\">"
		+ " Registration commited succesfuly. </font>" + "<br>" + "<hr size=2 width=\"80%\">" + "<font color=#113311 face=\"Arial\" size=\"4\">"
		+ " Dimes Agent version "+RegistrationFrame.AgentVer+" was successfuly registered.<br>"+ "</font>" + "</p>" + "</body>" + "</html>";
	}

	public void jbInit()
	{

		screen1.setBackground(RegistrationFrame.BACKGROUND_COLOR);
		screen1.setBorder(new EmptyBorder(15, 15, 15, 15));
		screen1.setVisible(true);
		screen1.setPreferredSize(this.getSize());
		screen1.setLayout(new GridLayout(9, 1));
		// build screen1 : welcome and Register - user or agent choices.

		JLabel welcomeLabel1 = new JLabel("   Welcome to the DIMES Project.", JLabel.LEFT);
		welcomeLabel1.setFont(new java.awt.Font("Sans-serif MS", Font.BOLD, 14));
		welcomeLabel1.setForeground(HEADER_TEXT_COLOR);

		JLabel welcomeLabel2 = new JLabel("Are You already a DIMES User?", JLabel.LEFT);

		welcomeLabel2.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		welcomeLabel2.setForeground(HEADER_TEXT_COLOR);

		addNewUserButton = new JRadioButton("   I am a New User. This is my first installed DIMES Agent.");
		addNewUserButton.setFont(new java.awt.Font("Sans-serif MS", Font.PLAIN, 13));
		addNewUserButton.setForeground(new Color(20, 100, 40));

		addAgentToUSerButton = new JRadioButton("   Yes. I have already installed DIMES Agents on other computers.");
		addAgentToUSerButton.setFont(new java.awt.Font("Sans-serif MS", Font.PLAIN, 13));
		addAgentToUSerButton.setForeground(new Color(20, 100, 40));

		URL helpIconURL = this.getClass().getClassLoader().getResource(ResourceManager.HELPICON_GIF_RSRC);
		if(null==helpIconURL){
			try {
				String resourceDir=PropertiesBean.getProperty(PropertiesNames.RESULTS_DIR);
				String iconName=ResourceManager.HELPICON_GIF_RSRC;
				resourceDir = (resourceDir+iconName.substring(iconName.lastIndexOf("/")));
				java.io.File tempF = new java.io.File(resourceDir); 
				helpIconURL = tempF.toURI().toURL();// new URL(resourceDir);
			} catch (NoSuchPropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mergeAgentsButton = new JRadioButton("   Yes. I once had a DIMES Agent on this computer.");
		mergeAgentsButton.setFont(new java.awt.Font("Sans-serif MS", Font.PLAIN, 13));
		mergeAgentsButton.setForeground(new Color(20, 100, 40));

		ImageIcon helpIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(helpIconURL));

		whyIsThatImportantButton = new JRadioButton(WHY_IS_THAT_IMPORTANT_QUESTION, helpIcon);
		whyIsThatImportantButton.setFont(new java.awt.Font("Sans-serif MS", Font.PLAIN, 13));
		whyIsThatImportantButton.setForeground(new Color(20, 100, 40));

		whyIsThatImportantButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				addNewUserButton.setSelected(true);
				JOptionPane.showMessageDialog(RegistrationFrame.this, new InfoDisplayPanel(WHY_IS_THAT_IMPORTANT_MESSAGE), "Users and Agents",
						JOptionPane.PLAIN_MESSAGE);
			}

		});

		ButtonGroup group = new ButtonGroup();
		group.add(addNewUserButton);
		group.add(addAgentToUSerButton);
		group.add(mergeAgentsButton);
		group.add(whyIsThatImportantButton);

		addNewUserButton.setSelected(true);

		screen1.add(welcomeLabel1);
		screen1.add(new JSeparator());
		screen1.add(welcomeLabel2);
		screen1.add(addNewUserButton);
		screen1.add(addAgentToUSerButton);
		screen1.add(mergeAgentsButton);

		screen1.add(whyIsThatImportantButton);

		//  user details without password :    
		screen2.setBackground(RegistrationFrame.BACKGROUND_COLOR);
		screen2.setBorder(new EmptyBorder(15, 15, 15, 15));
		screen2.setVisible(true);
		screen2.setPreferredSize(this.getSize());
		screen2.setLayout(new GridLayout(2, 1));

		JPanel screen2MessagePanel = new JPanel();
		JPanel screen2DetailsPanel = new JPanel();

		screen2MessagePanel.setLayout(new GridLayout(5, 1));
		screen2MessagePanel.setBackground(RegistrationFrame.BACKGROUND_COLOR);

		JLabel enterDetailsLabel1 = new JLabel("User Details.", JLabel.LEFT);
		enterDetailsLabel1.setFont(new java.awt.Font("Sans-serif MS", Font.BOLD, 13));
		enterDetailsLabel1.setForeground(new Color(20, 40, 170));

		JLabel enterDetailsLabel2 = new JLabel(" Please Enter your details:", JLabel.LEFT);

		enterDetailsLabel2.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		enterDetailsLabel2.setForeground(new Color(20, 100, 40));
		screen2ErrorMessage.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		screen2ErrorMessage.setForeground(Color.WHITE);

		this.whyIsItRequiredButton = new JRadioButton(RegistrationFrame.WHY_IS_THAT_REQUIRED_QUESTION, helpIcon);
		whyIsItRequiredButton.setForeground(new Color(20, 100, 40));

		whyIsItRequiredButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				whyIsItRequiredButton.setSelected(false);
				JOptionPane.showMessageDialog(RegistrationFrame.this, new InfoDisplayPanel(WHY_IS_THAT_REQUIRED_MESSAGE),
				//                        whyIsThatRequiredPanel,
						"Users Information", JOptionPane.PLAIN_MESSAGE);
			}

		});

		screen2MessagePanel.add(enterDetailsLabel1);
		screen2MessagePanel.add(new JSeparator());
		screen2MessagePanel.add(enterDetailsLabel2);
		screen2MessagePanel.add(screen2ErrorMessage);
		screen2MessagePanel.add(whyIsItRequiredButton);

		screen2DetailsPanel.setLayout(new SpringLayout());

		usernameTextField = new JTextField();
		usernameTextField.setBackground(Color.WHITE);
		usernameTextField.setMaximumSize(new Dimension(180, 30));//check
		usernameTextField.setPreferredSize(new Dimension(180, 30));//check
		JLabel usernameLabel = new JLabel();
		usernameLabel.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		usernameLabel.setLabelFor(usernameTextField);
		usernameLabel.setText(USERNAME_LABEL);

		countryComboBox = new JComboBox();

		{
			Object[] countries = new Object[0];
			try
			{
				String countriesFile = PropertiesBean.getProperty(PropertiesNames.COUNTRY_FILE/*"countryFile"*/);
				//                    System.out.println(countriesFile);//debug
				if (PropertiesBean.isValidValue(countriesFile))
					countries = RegistrationFrame.getListFromXml(countriesFile);

			}
			catch (Exception e1)
			{ //todo - should add the property if doesn't exist
				Loggers.getLogger().debug(e1.toString()); //debug
				e1.printStackTrace();//debug - erase
			}
			countryComboBox = new JComboBox(countries);
		}

		countryComboBox.setBackground(SystemColor.WHITE);
		countryComboBox.setMaximumSize(new Dimension(180, 30));//check
		countryComboBox.setPreferredSize(new Dimension(180, 30));//check
		JLabel countryTextLabel = new JLabel();
		countryTextLabel.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		countryTextLabel.setLabelFor(countryComboBox);
		countryTextLabel.setText(RegistrationFrame.COUNTRY_LABEL);

		emailTextField = new JTextField();
		emailTextField.setBackground(Color.WHITE);
		emailTextField.setMaximumSize(new Dimension(180, 30));//check
		emailTextField.setPreferredSize(new Dimension(180, 30));//check
		JLabel emailTextLabel = new JLabel();
		emailTextLabel.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		emailTextLabel.setLabelFor(emailTextField);
		emailTextLabel.setText(RegistrationFrame.EMAIL_LABEL);

		passwordTextField = new JPasswordField();
		passwordTextField.setBackground(SystemColor.WHITE);
		passwordTextField.setMaximumSize(new Dimension(180, 30));//check
		passwordTextField.setPreferredSize(new Dimension(180, 30));//check
		JLabel passwordTextLabel = new JLabel();
		passwordTextLabel.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		passwordTextLabel.setLabelFor(passwordTextField);
		passwordTextLabel.setText(RegistrationFrame.PASSWORD_LABEL);

		//        JLabel whyIsItRequired = new JLabel(RegistrationFrame.WHY_IS_THAT_REQUIRED_QUESTION);

		screen2DetailsPanel.add(usernameLabel);
		screen2DetailsPanel.add(usernameTextField);
		screen2DetailsPanel.add(countryTextLabel);
		screen2DetailsPanel.add(countryComboBox);
		screen2DetailsPanel.add(emailTextLabel);
		screen2DetailsPanel.add(emailTextField);
		screen2DetailsPanel.add(passwordTextLabel);
		screen2DetailsPanel.add(passwordTextField);

		screen2DetailsPanel.setOpaque(false);
		screen2DetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		SpringUtilities.makeCompactGrid(screen2DetailsPanel, 4, 2, 60, 0, 20, 10);

		screen2.add(screen2MessagePanel);
		screen2.add(screen2DetailsPanel);

		// screen of the user detailswith password : :

		screen3.setBackground(RegistrationFrame.BACKGROUND_COLOR);
		screen3.setBorder(new EmptyBorder(15, 15, 15, 15));
		screen3.setVisible(true);
		screen3.setPreferredSize(this.getSize());
		screen3.setLayout(new BorderLayout());

		JPanel screen3MessagePanel = new JPanel();
		JPanel screen3DetailsPanel = new JPanel();

		screen3MessagePanel.setPreferredSize(new Dimension(500, 250));
		screen3DetailsPanel.setPreferredSize(new Dimension(500, 100));

		screen3MessagePanel.setLayout(new GridLayout(7, 1));
		screen3MessagePanel.setBackground(RegistrationFrame.BACKGROUND_COLOR);

		JLabel enterDetailsLabel3 = new JLabel("User Details.", JLabel.LEFT);
		enterDetailsLabel3.setFont(new java.awt.Font("Sans-serif MS", Font.BOLD, 13));
		enterDetailsLabel3.setForeground(new Color(20, 40, 170));

		enterDetailsLabel4 = new JLabel(" We thank you for installing another agent. ", JLabel.LEFT);
		enterDetailsLabel5 = new JLabel(" In order to add this agent to your user group,  ", JLabel.LEFT);
		JLabel enterDetailsLabel6 = new JLabel(" Please Enter your user name and password :", JLabel.LEFT);

		enterDetailsVeteranAgentLabel1 = new JLabel(" We thank you for installing the agent again. ", JLabel.LEFT);
		enterDetailsVeteranAgentLabel2 = new JLabel(" In order to get your agent list,  ", JLabel.LEFT);

		enterDetailsLabel4.setFont(new java.awt.Font("Comic Sans MS", Font.PLAIN, 13));
		enterDetailsLabel4.setForeground(new Color(20, 100, 40));
		enterDetailsLabel5.setFont(new java.awt.Font("Comic Sans MS", Font.PLAIN, 13));
		enterDetailsLabel5.setForeground(new Color(20, 100, 40));
		enterDetailsLabel6.setFont(new java.awt.Font("Comic Sans MS", Font.PLAIN, 13));
		enterDetailsLabel6.setForeground(new Color(20, 100, 40));

		screen3ErrorMessage.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		screen3ErrorMessage.setForeground(Color.RED);

		final JRadioButton iNeverHadPasswordButton = new JRadioButton(I_NEVER_HAS_A_PASSWORD_QUESTION, helpIcon);
		iNeverHadPasswordButton.setFont(new java.awt.Font("Sans-serif MS", Font.PLAIN, 13));
		iNeverHadPasswordButton.setForeground(new Color(20, 100, 40));

		iNeverHadPasswordButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				iNeverHadPasswordButton.setSelected(false);
				JOptionPane.showMessageDialog(RegistrationFrame.this, new InfoDisplayPanel(I_NEVER_HAS_A_PASSWORD_MESSAGE), "Users Information",
						JOptionPane.PLAIN_MESSAGE);
			}

		});

		screen3MessagePanel.add(enterDetailsLabel3);
		screen3MessagePanel.add(new JSeparator());
		screen3MessagePanel.add(enterDetailsLabel4);
		screen3MessagePanel.add(enterDetailsLabel5);
		screen3MessagePanel.add(enterDetailsLabel6);
		screen3MessagePanel.add(screen3ErrorMessage);
		screen3MessagePanel.add(iNeverHadPasswordButton);

		screen3DetailsPanel.setLayout(new SpringLayout());

		oldUsernameTextField = new JTextField();
//		oldUsernameTextField.setBackground(SystemColor.inactiveCaptionText);
		oldUsernameTextField.setBackground(Color.WHITE);
		oldUsernameTextField.setMaximumSize(new Dimension(180, 30));//check
		oldUsernameTextField.setPreferredSize(new Dimension(180, 30));//check
		JLabel oldUsernameLabel = new JLabel();
		oldUsernameLabel.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		oldUsernameLabel.setLabelFor(oldUsernameTextField);
		oldUsernameLabel.setText(USERNAME_LABEL);

		passwordConfirmTextField = new JPasswordField();
		passwordConfirmTextField.setBackground(Color.WHITE);
		passwordConfirmTextField.setMaximumSize(new Dimension(180, 30));//check
		passwordConfirmTextField.setPreferredSize(new Dimension(180, 30));//check
		JLabel confirmPasswordTextLabel = new JLabel();
		confirmPasswordTextLabel.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		confirmPasswordTextLabel.setLabelFor(passwordConfirmTextField);
		confirmPasswordTextLabel.setText(RegistrationFrame.CONFIRM_PASSWORD_LABEL);

		screen3DetailsPanel.add(oldUsernameLabel);
		screen3DetailsPanel.add(oldUsernameTextField);
		//        screen3DetailsPanel.add(passwordTextLabel);
		//        screen3DetailsPanel.add(passwordTextField);
		screen3DetailsPanel.add(confirmPasswordTextLabel);
		screen3DetailsPanel.add(passwordConfirmTextField);

		screen3DetailsPanel.setOpaque(false);
		screen3DetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		SpringUtilities.makeCompactGrid(screen3DetailsPanel, 2, 2, 60, 0, 20, 25);

		screen3.add(screen3MessagePanel, BorderLayout.CENTER);
		screen3.add(screen3DetailsPanel, BorderLayout.SOUTH);

		// screen of the user details with password :        
		screen4.setBackground(Color.WHITE);
		screen4.setBorder(new EmptyBorder(15, 15, 15, 15));
//		screen4.show();  derecated
		screen4.setVisible(true);
		screen4.setPreferredSize(this.getSize());
		screen4.setLayout(new GridLayout(2, 1));

		JPanel screen4AgentMessagePanel = new JPanel();
		JPanel screen4AgentDetailsPanel = new JPanel();

		screen4AgentMessagePanel.setLayout(new GridLayout(5, 1));
		screen4AgentMessagePanel.setBackground(RegistrationFrame.BACKGROUND_COLOR);

		JLabel enterAgentDetailsLabel1 = new JLabel("Agent Details.", JLabel.LEFT);
		enterAgentDetailsLabel1.setFont(new java.awt.Font("Sans-serif MS", Font.BOLD, 13));
		enterAgentDetailsLabel1.setForeground(RegistrationFrame.HEADER_TEXT_COLOR);

		JLabel enterAgentDetailsLabel2 = new JLabel(" The DIMES Server accepted your registration request.", JLabel.LEFT);

		enterAgentDetailsLabel2.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		enterAgentDetailsLabel2.setForeground(new Color(20, 100, 40));

		JLabel enterAgentDetailsLabel3 = new JLabel(" Please choose your Agent's name :", JLabel.LEFT);

		enterAgentDetailsLabel3.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		enterAgentDetailsLabel3.setForeground(new Color(20, 100, 40));
		screen4ErrorMessage.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		screen4ErrorMessage.setForeground(Color.RED);

		screen4AgentMessagePanel.add(enterAgentDetailsLabel1);
		screen4AgentMessagePanel.add(new JSeparator());
		screen4AgentMessagePanel.add(enterAgentDetailsLabel2);
		screen4AgentMessagePanel.add(enterAgentDetailsLabel3);
		screen4AgentMessagePanel.add(screen4ErrorMessage);

		screen4AgentDetailsPanel.setLayout(new SpringLayout());

		agentNameTextField = new JTextField();
		agentNameTextField.setBackground(RegistrationFrame.BACKGROUND_COLOR);
		agentNameTextField.setMaximumSize(new Dimension(180, 30));//check
		agentNameTextField.setPreferredSize(new Dimension(180, 30));//check
		JLabel agentNameLabel = new JLabel();
		agentNameLabel.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		agentNameLabel.setLabelFor(agentNameTextField);
		agentNameLabel.setText(AGENTNAME_LABEL);

		screen4AgentDetailsPanel.add(agentNameLabel);
		screen4AgentDetailsPanel.add(agentNameTextField);

		screen4AgentDetailsPanel.setOpaque(false);
		screen4AgentDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		SpringUtilities.makeCompactGrid(screen4AgentDetailsPanel, 1, 2, 60, 0, 20, 20);

		screen4.add(screen4AgentMessagePanel);
		screen4.add(screen4AgentDetailsPanel);

		//      a screen for joining and creating Groups :        
		screen5.setBackground(RegistrationFrame.BACKGROUND_COLOR);
		screen5.setBorder(new EmptyBorder(15, 15, 15, 15));
		screen5.setVisible(true);
		screen5.setPreferredSize(this.getSize());
		screen5.setLayout(new GridLayout(2, 1));

		JPanel screen5GroupMessagePanel = new JPanel();
		JPanel screen5GroupDetailsPanel = new JPanel();

		screen5GroupMessagePanel.setLayout(new GridLayout(6, 1));
		screen5GroupMessagePanel.setBackground(Color.WHITE);

		JLabel enterGroupDetailsLabel1 = new JLabel("Teams.", JLabel.LEFT);
		enterGroupDetailsLabel1.setFont(new java.awt.Font("Sans-serif MS", Font.BOLD, 13));
		enterGroupDetailsLabel1.setForeground(RegistrationFrame.HEADER_TEXT_COLOR);

		JLabel enterGroupetailsLabel2 = new JLabel(" You may either join an existing user's Team", JLabel.LEFT);

		enterGroupetailsLabel2.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		enterGroupetailsLabel2.setForeground(new Color(20, 100, 40));

		JLabel enterGroupetailsLabel3 = new JLabel("   or create a new one :", JLabel.LEFT);

		enterGroupetailsLabel3.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		enterGroupetailsLabel3.setForeground(new Color(20, 100, 40));
		screen5ErrorMessage.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		screen5ErrorMessage.setForeground(Color.RED);

		this.whyIsGroupsRequiredButton = new JRadioButton(RegistrationFrame.WHATS_THIS_GROUPS_THING_QUESTION, helpIcon);
		whyIsGroupsRequiredButton.setForeground(new Color(20, 100, 40));

		whyIsGroupsRequiredButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				whyIsGroupsRequiredButton.setSelected(false);
				JOptionPane.showMessageDialog(RegistrationFrame.this, new InfoDisplayPanel(WHATS_THIS_GROUPS_THING_MESSAGE), "Groups! just join them.",
						JOptionPane.PLAIN_MESSAGE);
			}
		});

		screen5GroupMessagePanel.add(enterGroupDetailsLabel1);
		screen5GroupMessagePanel.add(new JSeparator());
		screen5GroupMessagePanel.add(enterGroupetailsLabel2);
		screen5GroupMessagePanel.add(enterGroupetailsLabel3);
		screen5GroupMessagePanel.add(screen5ErrorMessage);
		screen5GroupMessagePanel.add(whyIsGroupsRequiredButton);

		screen5GroupDetailsPanel.setLayout(new SpringLayout());

		groupTextField = new JComboBox();
		groupTextField.setBackground(RegistrationFrame.BACKGROUND_COLOR);
		groupTextField.setMaximumSize(new Dimension(180, 25));//check
		//        groupTextField.setMinimumSize(new Dimension(180,25));//check
		groupTextField.setPreferredSize(new Dimension(180, 25));//check
		groupTextField.setEditable(true);

		JLabel groupLabel = new JLabel();
		groupLabel.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		//        groupLabel.setLabelFor(groupComboBox);
		groupLabel.setLabelFor(groupTextField);
		groupLabel.setText(GROUP_LABEL);

		 //       groupTextField.setEditable(true);//group name can either be written or chosen from the menu

		existingGroupRadioButton = new JRadioButton();
		createGroupRadioButton = new JRadioButton();

		existingGroupRadioButton.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		existingGroupRadioButton.setOpaque(false);
		existingGroupRadioButton.setText(EXISTING_GROUP_LABEL);

		createGroupRadioButton.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		createGroupRadioButton.setOpaque(false);
		createGroupRadioButton.setText(CREATE_GROUP_LABEL);

		createGroupRadioButton.setMaximumSize(new Dimension(180, 25));//check
		//        createGroupRadioButton.setMinimumSize(new Dimension(180,25));//check
		createGroupRadioButton.setPreferredSize(new Dimension(180, 25));//check

		ButtonGroup groupButtonGroup = new ButtonGroup();
		groupButtonGroup.add(existingGroupRadioButton);
		groupButtonGroup.add(createGroupRadioButton);

		screen5GroupDetailsPanel.add(existingGroupRadioButton);
		screen5GroupDetailsPanel.add(createGroupRadioButton);
		screen5GroupDetailsPanel.add(groupLabel);
		screen5GroupDetailsPanel.add(groupTextField);

		screen5GroupDetailsPanel.setOpaque(false);
		screen5GroupDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		SpringUtilities.makeCompactGrid(screen5GroupDetailsPanel, 2, 2, 80, 20, 20, 20);

		screen5.add(screen5GroupMessagePanel);
		screen5.add(screen5GroupDetailsPanel);

		// success panel : 

		successPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
		successPanel.setBackground(RegistrationFrame.BACKGROUND_COLOR);
		successPanel.add(new JLabel(getREGISTRATION_SUCCESS_HTML_LABEL()));

		// Nex/previous/Finish panel :
		nextButton = new JButton(NEXT_TEXT);
		nextButton.setFont(new java.awt.Font("Sans-serif MS", Font.PLAIN, 13));
		prevButton = new JButton(BACK_TEXT);
		prevButton.setFont(new java.awt.Font("Sans-serif MS", Font.PLAIN, 13));

		proxyButton = new JButton("Configure Proxy");
		proxyButton.setFont(new java.awt.Font("Sans-serif MS", Font.PLAIN, 13));

		finishButton = new JButton(FINISH_TEXT);
		finishButton.setFont(new java.awt.Font("Sans-serif MS", Font.PLAIN, 13));

		finishButton.setEnabled(false);

		nextButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				nextRegistrationPhase();
			}
		});

		finishButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				finishRegistration();

			}
		});

		prevButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				prevRegistrationPhase();
			}
		});

		proxyButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				proxyDialog.reset();
				GuiUtils.centerFrame(proxyDialog);
//				proxyDialog.show();
				proxyDialog.setVisible(true);
			}

		});

		// Validation screens :
		// Screen 6 - explains the user about the validation 

		screen6.setBackground(RegistrationFrame.BACKGROUND_COLOR);
		screen6.setBorder(new EmptyBorder(15, 15, 15, 15));
		screen6.setVisible(true);
		screen6.setPreferredSize(this.getSize());
		screen6.add(new JLabel(WELCOME_VALIDATION_HTML_STRING));

		// screen 7 - the user has to fill his details :
		//  user details without password :
		screen7.setBackground(RegistrationFrame.BACKGROUND_COLOR);
		screen7.setBorder(new EmptyBorder(15, 15, 15, 15));
//		screen7.show();
		screen7.setVisible(true);
		screen7.setPreferredSize(this.getSize());
		screen7.setLayout(new GridLayout(2, 1));

		JPanel screen7MessagePanel = new JPanel();
		JPanel screen7DetailsPanel = new JPanel();
		screen7MessagePanel.setLayout(new GridLayout(5, 1));
		screen7MessagePanel.setBackground(RegistrationFrame.BACKGROUND_COLOR);

		JLabel enterDetailsLabel7 = new JLabel("Confirm your user details.", JLabel.LEFT);
		enterDetailsLabel7.setFont(new java.awt.Font("Sans-serif MS", Font.BOLD, 13));
		enterDetailsLabel7.setForeground(new Color(20, 40, 170));

		JLabel enterDetailsLabel8 = new JLabel(" Confirm or change your details:", JLabel.LEFT);

		enterDetailsLabel8.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		enterDetailsLabel8.setForeground(new Color(20, 100, 40));
		screen7ErrorMessage.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		screen7ErrorMessage.setForeground(Color.RED);

		this.whatAreAllThoseNamesButton = new JRadioButton(RegistrationFrame.WHAT_ARE_ALL_THOSE_NAMES_QUESTION, helpIcon);
		whatAreAllThoseNamesButton.setForeground(new Color(20, 100, 40));

		whatAreAllThoseNamesButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				whyIsItRequiredButton.setSelected(false);
				JOptionPane.showMessageDialog(RegistrationFrame.this, new InfoDisplayPanel(WHAT_ARE_ALL_THOSE_NAMES_MESSAGE), "Users Information",
						JOptionPane.PLAIN_MESSAGE);
			}

		});

		validationAgentnameTextField = new JTextField();
		validationAgentnameTextField.setBackground(Color.WHITE);
		validationAgentnameTextField.setMaximumSize(new Dimension(180, 30));//check
		validationAgentnameTextField.setPreferredSize(new Dimension(180, 30));//check
		JLabel validationAgentnameLabel = new JLabel();
		validationAgentnameLabel.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		validationAgentnameLabel.setLabelFor(validationAgentnameTextField);
		validationAgentnameLabel.setText("Agent Name");

		validationUsernameTextField = new JTextField();
		validationUsernameTextField.setBackground(Color.WHITE);
		validationUsernameTextField.setMaximumSize(new Dimension(180, 30));//check
		validationUsernameTextField.setPreferredSize(new Dimension(180, 30));//check
		JLabel validationUsernameLabel = new JLabel();
		validationUsernameLabel.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		validationUsernameLabel.setLabelFor(validationUsernameTextField);
		validationUsernameLabel.setText(USERNAME_LABEL);

		validationCountryComboBox = new JComboBox();

		{
			Object[] countries = new Object[0];
			try
			{
				String countriesFile = PropertiesBean.getProperty(PropertiesNames.COUNTRY_FILE/*"countryFile"*/);
				//                    System.out.println(countriesFile);//debug
				if (PropertiesBean.isValidValue(countriesFile))
					countries = RegistrationFrame.getListFromXml(countriesFile);

			}
			catch (Exception e1)
			{ //todo - should add the property if doesn't exist
				Loggers.getLogger().debug(e1.toString()); //debug
				e1.printStackTrace();//debug - erase
			}
			validationCountryComboBox = new JComboBox(countries);
		}

		validationCountryComboBox.setBackground(SystemColor.WHITE);
		validationCountryComboBox.setMaximumSize(new Dimension(180, 30));//check
		validationCountryComboBox.setPreferredSize(new Dimension(180, 30));//check
		JLabel validationCountryTextLabel = new JLabel();
		validationCountryTextLabel.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		validationCountryTextLabel.setLabelFor(validationCountryComboBox);
		validationCountryTextLabel.setText(RegistrationFrame.COUNTRY_LABEL);

		validationEmailTextField = new JTextField();
		validationEmailTextField.setBackground(Color.WHITE);
		validationEmailTextField.setMaximumSize(new Dimension(180, 30));//check
		validationEmailTextField.setPreferredSize(new Dimension(180, 30));//check
		JLabel validationEmailTextLabel = new JLabel();
		validationEmailTextLabel.setForeground(RegistrationFrame.OPTION_TEXT_COLOR);
		validationEmailTextLabel.setLabelFor(validationEmailTextField);
		validationEmailTextLabel.setText(RegistrationFrame.EMAIL_LABEL);

		// Add to the screen :
		screen7MessagePanel.add(enterDetailsLabel7);
		screen7MessagePanel.add(new JSeparator());
		screen7MessagePanel.add(enterDetailsLabel8);
		screen7MessagePanel.add(screen7ErrorMessage);
		screen7MessagePanel.add(whatAreAllThoseNamesButton);

		screen7DetailsPanel.setLayout(new SpringLayout());

		screen7DetailsPanel.add(validationAgentnameLabel);
		screen7DetailsPanel.add(validationAgentnameTextField);
		screen7DetailsPanel.add(validationUsernameLabel);
		screen7DetailsPanel.add(validationUsernameTextField);
		screen7DetailsPanel.add(validationCountryTextLabel);
		screen7DetailsPanel.add(validationCountryComboBox);
		screen7DetailsPanel.add(validationEmailTextLabel);
		screen7DetailsPanel.add(validationEmailTextField);

		screen7DetailsPanel.setOpaque(false);
		screen7DetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		SpringUtilities.makeCompactGrid(screen7DetailsPanel, 4, 2, 60, 0, 20, 10);

		screen7.add(screen7MessagePanel);
		screen7.add(screen7DetailsPanel);

		/********** veteran agents screen**********************/
		//  agent names:    
		veteranAgentsScreen.setBackground(RegistrationFrame.BACKGROUND_COLOR);
		veteranAgentsScreen.setBorder(new EmptyBorder(15, 15, 15, 15));
		veteranAgentsScreen.setVisible(true);
		veteranAgentsScreen.setPreferredSize(this.getSize());
		veteranAgentsScreen.setLayout(new GridLayout(2, 1));

		JPanel veteranAgentsScreenMessagePanel = new JPanel();
		JPanel veteranAgentsScreenDetailsPanel = new JPanel();

		veteranAgentsScreenMessagePanel.setLayout(new GridLayout(5, 1));
		veteranAgentsScreenMessagePanel.setBackground(RegistrationFrame.BACKGROUND_COLOR);

		JLabel agentNamesLabel = new JLabel("Agent Names.", JLabel.LEFT);
		agentNamesLabel.setFont(new java.awt.Font("Sans-serif MS", Font.BOLD, 13));
		agentNamesLabel.setForeground(new Color(20, 40, 170));

		JLabel selectAgentLabel = new JLabel(" Please select the agent that was installed on your computer:", JLabel.LEFT);

		selectAgentLabel.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		selectAgentLabel.setForeground(new Color(20, 100, 40));
		veteranAgentsScreenWarning1.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		veteranAgentsScreenWarning1.setForeground(Color.ORANGE);
		veteranAgentsScreenWarning2.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 13));
		veteranAgentsScreenWarning2.setForeground(Color.ORANGE);

		veteranAgentsScreenMessagePanel.add(agentNamesLabel);
		veteranAgentsScreenMessagePanel.add(new JSeparator());
		veteranAgentsScreenMessagePanel.add(selectAgentLabel);
		veteranAgentsScreenMessagePanel.add(veteranAgentsScreenWarning1);
		veteranAgentsScreenMessagePanel.add(veteranAgentsScreenWarning2);

		Object[] agents = new Object[0];
		veteranAgentsComboBox = new JComboBox(agents);

		veteranAgentsComboBox.setBackground(SystemColor.WHITE);
		veteranAgentsComboBox.setMaximumSize(new Dimension(180, 30));
		veteranAgentsComboBox.setPreferredSize(new Dimension(180, 30));

		veteranAgentsScreenDetailsPanel.add(veteranAgentsComboBox);

		veteranAgentsScreenDetailsPanel.setOpaque(false);
		veteranAgentsScreenDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		veteranAgentsScreen.add(veteranAgentsScreenMessagePanel);
		veteranAgentsScreen.add(veteranAgentsScreenDetailsPanel);

		/******************************************************/

		// Next /prev/validate/finish  buttons panel :        
		nextPrevOptions.setBackground(RegistrationFrame.NEXT_BACK_PANEL_BACKGROUND_COLOR);
		nextPrevOptions.add(proxyButton);
		nextPrevOptions.add(prevButton);
		nextPrevOptions.add(nextButton);

		nextPrevOptions.add(finishButton);

		this.getContentPane().setLayout(new BorderLayout());
		rearangeGUI();

	}

	//todo - maybe move to general util class
	public static Object[] getListFromXml(String inPath)// throws DocumentException
	{
		Vector countries = new Vector();
		try
		{

			File inFile = new File(inPath);

//			SAXReader reader = new SAXReader();
//			Document doc = reader.read(inFile);
			Element root = XMLUtil.getRootElement(inFile);// doc.getRootElement();

			List<Node> elements =XMLUtil.getNodeListAsList(XMLUtil.getChildNodesNoWS(root));//.elements();
			int num = elements.size();

			String value = "";
			for(Node n: elements){
				value = n.getTextContent();
				countries.add(value);
			}
//			for (int i = 0; i < num; ++i)
//			{
//				value = ((Element) elements.get(i)).getText();
//				countries.add(value);
//			}
			countries.trimToSize();
		}
		catch (MalformedURLException e)
		{
			// TODO: handle exception
		}catch(IOException ioe){
			
		}
		if (countries.isEmpty())
			countries.add("");
		return countries.toArray();

	}

	/**
	 * 
	 * this function is called to indicate the registration/validation was finished.
	 * it informs the parent frame that the registration succeeded.
	 * 
	 * An update in version 0.5.0: In case of non interactive (service) mode - 
	 * this.theAgent.applyRegistrationSuccess() is being called (myParentAgentFrame is null).
	 */
	protected void finishRegistration()
	{
//		if( theAgent != null )
//			this.theAgent.applyRegistrationSuccess();
//		else
		this.theRegistrator.applyRegistrationSuccess(this);
		this.setVisible(false);
		this.dispose();
		
		//We re-init the PropertiesBean to make sure teh statistics work. 
//		PropertiesBean.init(FileHandlerBean.getPropertiesLocation());   
//		writeRegistrationDetails(regStat);
	}

	/**
	 * 
	 * an action that takes place whenever the "back" button 
	 * is pressed.
	 * basically, it moves the currentScreen into the 
	 * privious screen.
	 * 
	 */
	protected void prevRegistrationPhase()
	{
		resetErrorMessages();
		switch (this.currentScreen)
		{
			case RegistrationFrame.USER_DECISION_SCREEN :
				// Do nothing...
				break;

			case RegistrationFrame.NEW_USER_DETAILS_SCREEN :
				currentScreen = RegistrationFrame.USER_DECISION_SCREEN;
				break;

			case RegistrationFrame.VETERAN_USER_DETAILS_SCREEN :
				currentScreen = RegistrationFrame.USER_DECISION_SCREEN;
				this.veteranAgent = false;
				break;

			case RegistrationFrame.AGENT_DETAILS_SCREEN :
				// Do nothing...
				break;
			case RegistrationFrame.GROUP_DETAILS_SCREEN :
				currentScreen = RegistrationFrame.AGENT_DETAILS_SCREEN;
				break;
			case RegistrationFrame.REGISTRATION_SUCCESS_SCREEN :
				currentScreen = RegistrationFrame.GROUP_DETAILS_SCREEN;
				break;
			case RegistrationFrame.VALIDATION_SUCCESS_SCREEN :
				currentScreen = RegistrationFrame.VALIDATION_RESULT_SCREEN;
			case RegistrationFrame.VETERAN_AGENTS_SCREEN :
				currentScreen = RegistrationFrame.VETERAN_USER_DETAILS_SCREEN;
				this.ignoreActiveAgentWarning = false;
				break;
			default :
				break;

		}
		rearangeGUI();
	}

	/**
	 *  reset all error messages on all screens.
	 * usually done when moving from screen to screen.
	 * 
	 */
	private void resetErrorMessages()
	{
		screen2ErrorMessage.setText("");
		screen3ErrorMessage.setText("");
		screen4ErrorMessage.setText("");
		screen5ErrorMessage.setText("");
		screen7ErrorMessage.setText("");
		veteranAgentsScreenWarning1.setText("");
		veteranAgentsScreenWarning2.setText("");
	}

	/*****************************
	 * paints the right panel on the screen and enables/disables the proper buttons.
	 * usually done when moving from screen to screen.
	 *
	 */
	private void rearangeGUI()
	{
		// switch screens according to the currentScreen: 
		switch (this.currentScreen)
		{
			/*  Registration screens :   */
			case RegistrationFrame.USER_DECISION_SCREEN :
				moveToScreen(screen1);
				this.prevButton.setEnabled(false);
				break;

			case RegistrationFrame.NEW_USER_DETAILS_SCREEN :
				moveToScreen(screen2);
				this.prevButton.setEnabled(true);
				this.usernameTextField.requestFocus();
				break;

			case RegistrationFrame.VETERAN_USER_DETAILS_SCREEN :
				if (this.veteranAgent)
				{
					enterDetailsLabel4.setText(enterDetailsVeteranAgentLabel1.getText());
					enterDetailsLabel5.setText(enterDetailsVeteranAgentLabel2.getText());
				}
				moveToScreen(screen3);
				this.oldUsernameTextField.requestFocus();
				this.prevButton.setEnabled(true);
				break;

			case RegistrationFrame.VETERAN_AGENTS_SCREEN :
				moveToScreen(veteranAgentsScreen);
				this.veteranAgentsComboBox.requestFocus();
				this.prevButton.setEnabled(true);
				break;

			case RegistrationFrame.AGENT_DETAILS_SCREEN :
				moveToScreen(screen4);
				this.agentNameTextField.requestFocus();
				this.finishButton.setEnabled(true);
				this.prevButton.setEnabled(false);
				break;

			case RegistrationFrame.GROUP_DETAILS_SCREEN :
				moveToScreen(screen5);
				this.groupTextField.requestFocus();
				this.prevButton.setEnabled(true);
				this.nextButton.setEnabled(true);
				break;

			case RegistrationFrame.REGISTRATION_SUCCESS_SCREEN :
				this.nextButton.setEnabled(false);
				this.prevButton.setEnabled(false);
				this.finishButton.setEnabled(true);
				moveToScreen(successPanel);
				break;

			/*   Validation screens :   */
			case RegistrationFrame.INFORM_OLD_USER_VALIDATION_SCREEN :
				this.nextButton.setEnabled(true);
				this.prevButton.setEnabled(false);
				this.nextButton.setText(VALIDATE_TEXT);
				moveToScreen(screen6);
				break;

			case RegistrationFrame.VALIDATION_RESULT_SCREEN :
				this.nextButton.setEnabled(true);
				this.validationUsernameTextField.requestFocus();
				this.prevButton.setEnabled(false);
				this.nextButton.setText(NEXT_TEXT);
				moveToScreen(screen7);
				break;

			case RegistrationFrame.VALIDATION_SUCCESS_SCREEN :
				this.finishButton.setEnabled(true);
				this.prevButton.setEnabled(true);
				moveToScreen(successPanel);
				break;
			default :
				break;

		}
		// validate and repaint :
		this.validate();
		this.repaint();
	}

	/****************
	 * moves the screen to a special progressPanel :
	 * note that there is no control panel in the bottom
	 * and the entire frame is flled with the newScreen parameter.
	 * 
	 * @param newScreen the jpanel to place on screen
	 */
	private void moveToProgressScreen(JPanel newScreen)
	{
		this.getContentPane().removeAll();
		this.getContentPane().add(newScreen, BorderLayout.CENTER);
		this.validate();
		this.repaint();
	}

	/***************
	 * moves to a regular screen with the control panel in the bottom
	 */
	private void moveToScreen(JPanel newScreen)
	{
		this.getContentPane().removeAll();
		this.getContentPane().add(newScreen, BorderLayout.CENTER);
		this.getContentPane().add(nextPrevOptions, BorderLayout.SOUTH);
	}

	/***********************
	 * move the frame and the UI into the next reg phase.
	 * inside this function all communication with the server is being 
	 * called.
	 *
	 */
	private void nextRegistrationPhase()
	{
		RegistrationStatus regStat = null;
		String errMsg = null;
		switch (this.currentScreen)
		{
			// First screen :
			case RegistrationFrame.USER_DECISION_SCREEN :
				if (this.addNewUserButton.isSelected())
				{
					currentScreen = RegistrationFrame.NEW_USER_DETAILS_SCREEN;
				}
				if (this.addAgentToUSerButton.isSelected())
				{
					currentScreen = RegistrationFrame.VETERAN_USER_DETAILS_SCREEN;
				}
				if (this.mergeAgentsButton.isSelected())
				{
					this.veteranAgent = true;
					currentScreen = RegistrationFrame.VETERAN_USER_DETAILS_SCREEN;
				}
				break;

			/****   register a new User    ******/
			case RegistrationFrame.NEW_USER_DETAILS_SCREEN :
			{
				if (this.usernameTextField.getText().equals(""))
				{
					this.screen2ErrorMessage.setText("Please provide a user name");
				}
				else
				{
					if (regMonitorPanel != null)
						regMonitorPanel.stopProgress();
					regMonitorPanel = new ProgressMonitorPanel("Registering A new User...");
					this.moveToProgressScreen(regMonitorPanel);
					regThread = new RegistrationThread(this.communicator, this, regMonitorPanel);
					regMonitorPanel.setCancelListener(regThread);

					regThread.startNewUserRegistration(usernameTextField.getText(), countryComboBox.getSelectedItem().toString(), emailTextField.getText(),
							(passwordTextField.getText().length() > 0 ? (passwordTextField.getText()) : null));
					currentScreen = RegistrationFrame.NEW_USER_REGISTRATION_PROGRESS_SCREEN;
				}
			}
				break;

			/********** Register the agent to a veteran user :   **********/
			case RegistrationFrame.VETERAN_USER_DETAILS_SCREEN :
			{
				// check that the user name is not empty :
				if (this.oldUsernameTextField.getText().equals(""))
				{
					this.screen3ErrorMessage.setText("Please provide a user name");
				}
				else
					if (!this.veteranAgent)
					{
						if (regMonitorPanel != null)
							regMonitorPanel.stopProgress();
						regMonitorPanel = new ProgressMonitorPanel("Registering Agent ...");
						this.moveToProgressScreen(regMonitorPanel);
						regThread = new RegistrationThread(this.communicator, this, regMonitorPanel);
						regMonitorPanel.setCancelListener(regThread);

						regThread.startVeteranUserRegistration(this.oldUsernameTextField.getText(), this.passwordConfirmTextField.getText());
						currentScreen = RegistrationFrame.VETERAN_USER_REGISTRATION_PROGRESS_SCREEN;
					}
					else
					//veteran agent
					{
						if (this.passwordConfirmTextField.getText().equals(""))
							this.screen3ErrorMessage.setText(("Please provide a password."));
						else
						{
							if (regMonitorPanel != null)
								regMonitorPanel.stopProgress();
							regMonitorPanel = new ProgressMonitorPanel("Getting agent list for " + this.oldUsernameTextField.getText() + "...");
							this.moveToProgressScreen(regMonitorPanel);
							regThread = new RegistrationThread(this.communicator, this, regMonitorPanel);
							regMonitorPanel.setCancelListener(regThread);

							regThread.startVeteranUserAgentRetrieval(this.oldUsernameTextField.getText(), this.passwordConfirmTextField.getText());
							currentScreen = RegistrationFrame.VETERAN_USER_REGISTRATION_PROGRESS_SCREEN;

						}

					}
			}
				break;

			/****   Select agent name    ******/
			case RegistrationFrame.VETERAN_AGENTS_SCREEN :
			{
				String selectedAgent = (String) this.veteranAgentsComboBox.getSelectedItem();
				if ((selectedAgent == null) || selectedAgent.equals(""))
				{
					this.veteranAgentsScreenWarning1.setText("Please select an agent name");
				}
				else
				{
					/*System.out.println*/logger.debug("selected agent: " + selectedAgent);//debug
					if (regMonitorPanel != null)
						regMonitorPanel.stopProgress();
					regMonitorPanel = new ProgressMonitorPanel("Registering agent " + this.veteranAgentsComboBox.getSelectedItem().toString() + "...");
					this.moveToProgressScreen(regMonitorPanel);
					regThread = new RegistrationThread(this.communicator, this, regMonitorPanel);
					regMonitorPanel.setCancelListener(regThread);

					regThread.startVeteranAgentRegistration(selectedAgent, this.ignoreActiveAgentWarning);

					currentScreen = RegistrationFrame.VETERAN_AGENT_REGISTRATION_PROGRESS_SCREEN;
				}
			}
				break;

			// Register the agent details :
			case RegistrationFrame.AGENT_DETAILS_SCREEN :
			{

				if (this.agentNameTextField.getText().equals(""))
				{
					screen4ErrorMessage.setText("Please provide a User name");
				}
				else
					if (!agentName.equals(this.agentNameTextField.getText()))
					{
						if (regMonitorPanel != null)
							regMonitorPanel.stopProgress();
						regMonitorPanel = new ProgressMonitorPanel("Updating Agent Name...");
						this.moveToProgressScreen(regMonitorPanel);
						updateThread = new UpdateDetailsThread(this.communicator, this, regMonitorPanel);
						regMonitorPanel.setCancelListener(updateThread);
						updateThread.startAgentNameUpdate(this.agentName, this.agentNameTextField.getText());
						currentScreen = RegistrationFrame.AGENT_NAME_UPDATE_PROGRESS_SCREEN;
					}
					else
						currentScreen = RegistrationFrame.GROUP_DETAILS_SCREEN;
			}
				break;

			//        	  Register the group details :
			case RegistrationFrame.GROUP_DETAILS_SCREEN :
			{
				// join or create a group:
				String groupAction = "nop";
				if (this.existingGroupRadioButton.isSelected())
					groupAction = "join";
				if (this.createGroupRadioButton.isSelected())
					groupAction = "create";
				if (!groupAction.equals("nop"))
				{
					if (!this.groupTextField.getSelectedItem().equals(""))
					{
						if (regMonitorPanel != null)
							regMonitorPanel.stopProgress();
						regMonitorPanel = new ProgressMonitorPanel("Updating Agent Name...");
						this.moveToProgressScreen(regMonitorPanel);
						updateThread = new UpdateDetailsThread(this.communicator, this, regMonitorPanel);
						regMonitorPanel.setCancelListener(updateThread);
						updateThread.startGroupDetailsUpdate((String) this.groupTextField.getSelectedItem(), groupAction);
						currentScreen = RegistrationFrame.GROUP_UPDATE_PROGRESS_SCREEN;
					}
					else
					{
						screen5ErrorMessage.setText("Please Provide a group name.");
					}
				}
				else
					currentScreen = RegistrationFrame.REGISTRATION_SUCCESS_SCREEN;

			}
				break;

			/***********   end of registration proccess     ************/
			case RegistrationFrame.REGISTRATION_SUCCESS_SCREEN :
//				if( myParentAgentFrame!= null )
//					myParentAgentFrame.applyRegistrationSuccess();
//				else
					theRegistrator.applyRegistrationSuccess(this);
				this.dispose();
				break;

			/**********   agent details validation -information screen:      **********/
			case RegistrationFrame.INFORM_OLD_USER_VALIDATION_SCREEN :
			{
				if (regMonitorPanel != null)
					regMonitorPanel.stopProgress();
				regMonitorPanel = new ProgressMonitorPanel("Validating Agent details from DIMES Server");
				this.moveToProgressScreen(regMonitorPanel);
				updateThread = new UpdateDetailsThread(this.communicator, this, regMonitorPanel);
				regMonitorPanel.setCancelListener(regThread);
				updateThread.validateAgentDetails(this.agentID);
				currentScreen = RegistrationFrame.VALIDATION_PROGRESS_SCREEN;
				break;
			}

			/*******   agent details confiramtion screen            **********/
			case RegistrationFrame.VALIDATION_RESULT_SCREEN :
			{
				if (regMonitorPanel != null)
					regMonitorPanel.stopProgress();
				PropertiesStatus updateRequest = getValidationUpdateDetails(); // retuns null if no changes were done.
				// in case that there were changes : update!
				if (updateRequest != null)
				{
					regMonitorPanel = new ProgressMonitorPanel("Validating Agent details from DIMES Server");
					this.moveToProgressScreen(regMonitorPanel);
					updateThread = new UpdateDetailsThread(this.communicator, this, regMonitorPanel);
					regMonitorPanel.setCancelListener(updateThread);
					updateThread.startGeneralUpdate(updateRequest, new Vector(), new Vector());
					currentScreen = RegistrationFrame.VALIDATION_RESULT_PROGRES_SCREEN;
				}
				else
					currentScreen = RegistrationFrame.VALIDATION_SUCCESS_SCREEN;
				break;
			}

			default :
				break;
		}
		rearangeGUI();
	}

	/**
	 * 
	 * @return the update Details object of changes were done, null if no change was done.
	 */
	private PropertiesStatus getValidationUpdateDetails()
	{
		PropertiesStatus validationDetails = new UpdateDetailsStatus();

		try
		{
			country = PropertiesBean.getProperty("country");
		}
		catch (NoSuchPropertyException e)
		{
			Loggers.getLogger().warn("Can't find country property.");
			validationDetails.country = this.validationCountryComboBox.getSelectedItem().toString();
		}

		try
		{
			email = PropertiesBean.getProperty("email");
		}
		catch (NoSuchPropertyException e1)
		{
			Loggers.getLogger().warn("can't find email property");
			validationDetails.email = this.validationEmailTextField.getText();
		}
		try
		{
			userName = PropertiesBean.getProperty("userName");
			agentName = PropertiesBean.getProperty("agentName");
		}
		catch (NoSuchPropertyException e2)
		{
			Loggers.getLogger().error("could not find critical property:" + e2.getMessage());
			return null;
		}
		if (!this.validationCountryComboBox.getSelectedItem().toString().equals(country))
			validationDetails.country = this.validationCountryComboBox.getSelectedItem().toString();
		if (!this.validationEmailTextField.getText().equals(email))
			validationDetails.email = this.validationEmailTextField.getText();
		if (!this.validationAgentnameTextField.getText().equals(agentName))
			validationDetails.agentName = this.validationAgentnameTextField.getText();
		if (!this.validationUsernameTextField.getText().equals(userName))
			validationDetails.userName = this.validationUsernameTextField.getText();
		// if nothing is changed : return null :
		if (validationDetails.userName == null && validationDetails.agentName == null && validationDetails.email == null && validationDetails.country == null)
			return null;
		return validationDetails;
	}

	/**
	 * a function that is called when an update returns
	 * 
	 * @param updateStat
	 */
	public void returnUpdateState(PropertiesStatus updateStat)
	{
		if (regMonitorPanel != null)
			regMonitorPanel.stopProgress();
		/*System.out.println*/logger.debug("returned with update. current screen is :" + currentScreen);

		/*******  if there was some error message form agent/server    ******/
		if (updateStat.errorMessage != null || !updateStat.errorMessages.isEmpty())
		{
			// get the first error message :
			if (updateStat.errorMessage == null)
				updateStat.errorMessage = (String) updateStat.errorMessages.get(0);

			if (this.currentScreen == RegistrationFrame.AGENT_NAME_UPDATE_PROGRESS_SCREEN)
			{
				this.screen4ErrorMessage.setText(updateStat.errorMessage);
				this.currentScreen = RegistrationFrame.AGENT_DETAILS_SCREEN;
			}
			if (this.currentScreen == RegistrationFrame.GROUP_UPDATE_PROGRESS_SCREEN)
			{
				this.screen5ErrorMessage.setText(updateStat.errorMessage);
				this.currentScreen = RegistrationFrame.GROUP_DETAILS_SCREEN;
			}
			if (this.currentScreen == RegistrationFrame.VALIDATION_RESULT_PROGRES_SCREEN)
			{
				this.screen7ErrorMessage.setText(updateStat.errorMessage);
				this.currentScreen = RegistrationFrame.VALIDATION_RESULT_SCREEN;
			}

		}

		/**************   if the user has canceled          ***********/
		else
		{
			if (updateStat.updateCanceled)
			{
				if (this.currentScreen == RegistrationFrame.AGENT_NAME_UPDATE_PROGRESS_SCREEN)
				{
					this.screen4ErrorMessage.setText("Agent name Registration Canceled. Please Try again.");
					this.currentScreen = RegistrationFrame.AGENT_DETAILS_SCREEN;
				}
				if (this.currentScreen == RegistrationFrame.GROUP_UPDATE_PROGRESS_SCREEN)
				{
					this.screen5ErrorMessage.setText("Group Registration Canceled. Please try again.");
					this.currentScreen = RegistrationFrame.GROUP_DETAILS_SCREEN;
				}
				if (this.currentScreen == RegistrationFrame.VALIDATION_PROGRESS_SCREEN)
				{
					this.currentScreen = RegistrationFrame.INFORM_OLD_USER_VALIDATION_SCREEN;
				}
				if (this.currentScreen == RegistrationFrame.VALIDATION_RESULT_PROGRES_SCREEN)
				{
					this.screen7ErrorMessage.setText("Details update Canceled. Please try again.");
					this.currentScreen = RegistrationFrame.VALIDATION_RESULT_SCREEN;
				}

			}
			else
			/*********   update success        ********/
			{
				/*********  agent name updated    ************/
				if (this.currentScreen == RegistrationFrame.AGENT_NAME_UPDATE_PROGRESS_SCREEN)
				{
					this.screen4ErrorMessage.setText("");
					agentName = updateStat.agentName;
					try
					{
						PropertiesBean.setProperty(PropertiesNames.AGENT_NAME/*"agentName"*/, agentName);
						this.currentScreen = RegistrationFrame.GROUP_DETAILS_SCREEN;
					}
					catch (IOException e)
					{
						// Properties file wasn't updated : notify...
						e.printStackTrace();
						this.currentScreen = RegistrationFrame.AGENT_DETAILS_SCREEN;
						this.screen5ErrorMessage.setText("Properties file could not be updated. Please check properties.xml");
					}

				}

				/*********   group update succeeded      *********/
				if (this.currentScreen == RegistrationFrame.GROUP_UPDATE_PROGRESS_SCREEN)
				{
					this.screen5ErrorMessage.setText("");
					this.currentScreen = RegistrationFrame.REGISTRATION_SUCCESS_SCREEN;
				}

				/***********   validation result returned :         *************/
				if (this.currentScreen == RegistrationFrame.VALIDATION_PROGRESS_SCREEN)
				{
					this.screen7ErrorMessage.setText("");
					if (updateStat.agentName != null)
						this.validationAgentnameTextField.setText(updateStat.agentName);
					if (updateStat.country != null)
						this.validationCountryComboBox.setSelectedItem(updateStat.country);
					if (updateStat.userName != null)
						this.validationUsernameTextField.setText(updateStat.userName);
					if (updateStat.email != null)
						this.validationEmailTextField.setText(updateStat.email);

					this.currentScreen = RegistrationFrame.VALIDATION_RESULT_SCREEN;
				}

				/************    user entered detasils and they returned with success :   *******/
				if (this.currentScreen == RegistrationFrame.VALIDATION_RESULT_PROGRES_SCREEN)
				{
					currentScreen = RegistrationFrame.VALIDATION_SUCCESS_SCREEN;
				}

				this.setUpdateProperties(updateStat);
			}
		}

		rearangeGUI();
	}

	/**
	 * a function that is called when the registration returns.
	 * 
	 * @param regStat
	 */
	public void returnRegistrationState(PropertiesStatus regStat)
	{
		if (regMonitorPanel != null)
			regMonitorPanel.stopProgress();
		if (regStat.errorMessage != null)
		{

			if (this.currentScreen == RegistrationFrame.NEW_USER_REGISTRATION_PROGRESS_SCREEN)
			{
				this.screen2ErrorMessage.setText(regStat.errorMessage);
				this.currentScreen = RegistrationFrame.NEW_USER_DETAILS_SCREEN;
			}
			if (this.currentScreen == RegistrationFrame.VETERAN_USER_REGISTRATION_PROGRESS_SCREEN)
			{
				this.screen3ErrorMessage.setText(regStat.errorMessage);
				this.currentScreen = RegistrationFrame.VETERAN_USER_DETAILS_SCREEN;
			}
			if (this.currentScreen == RegistrationFrame.VETERAN_AGENT_REGISTRATION_PROGRESS_SCREEN)
			{
				/*System.err.println*/logger.debug("error: " + regStat.errorMessage);//debug
				this.veteranAgentsScreenWarning1.setText(regStat.errorMessage);
				this.veteranAgentsScreenWarning2.setText("	Press Next if you wish to continue.");
				this.ignoreActiveAgentWarning = true;
				this.currentScreen = RegistrationFrame.VETERAN_AGENTS_SCREEN;
			}
		}

		else
		{
			if (regStat.registrationCanceled)
			{
				if (this.currentScreen == RegistrationFrame.NEW_USER_REGISTRATION_PROGRESS_SCREEN)
				{
					this.screen2ErrorMessage.setText("Registration Canceled. Please Try again.");
					this.currentScreen = RegistrationFrame.NEW_USER_DETAILS_SCREEN;
				}
				if (this.currentScreen == RegistrationFrame.VETERAN_USER_REGISTRATION_PROGRESS_SCREEN)
				{
					this.screen3ErrorMessage.setText("Registration Canceled. Please try again.");
					this.currentScreen = RegistrationFrame.VETERAN_USER_DETAILS_SCREEN;
				}
				if (this.currentScreen == RegistrationFrame.VETERAN_AGENT_REGISTRATION_PROGRESS_SCREEN)
				{
					this.veteranAgentsScreenWarning1.setText("Registration Canceled. Please try again.");
					this.currentScreen = RegistrationFrame.VETERAN_AGENTS_SCREEN;
				}
			}
			else
			{
				// All went debug : move on!

				if (!this.veteranAgent)
				{
					this.setRegistrationProperties(regStat);
					agentName = (String) regStat.agentNames.get(0)/*regStat.agentName*/;
					agentNameTextField.setText(agentName/*regStat.agentName*/);
					this.currentScreen = RegistrationFrame.AGENT_DETAILS_SCREEN;

					this.resetGroupNamesCombo();
				}
				else
				{
					if (this.currentScreen == RegistrationFrame.VETERAN_USER_REGISTRATION_PROGRESS_SCREEN)
					{
						this.resetAgentNamesCombo(regStat.agentNames);
						this.currentScreen = RegistrationFrame.VETERAN_AGENTS_SCREEN;
					}
					else
						if (this.currentScreen == RegistrationFrame.VETERAN_AGENT_REGISTRATION_PROGRESS_SCREEN)
						{
							this.setRegistrationProperties(regStat);
							this.currentScreen = RegistrationFrame.REGISTRATION_SUCCESS_SCREEN;
						}

				}

			}
		}
		rearangeGUI();
	}

	/**
	 * 
	 */
	private void resetGroupNamesCombo()
	{
		GroupGetter getter = new GroupGetter();
		Vector groups = getter.getGroups();
		for (int i = 0; i < groups.size(); ++i)
		{
			this.groupTextField.addItem(groups.get(i));
		}
	}

	private void resetAgentNamesCombo(Vector agents)
	{
		this.veteranAgentsComboBox.removeAllItems();
		for (int i = 0; i < agents.size(); ++i)
			this.veteranAgentsComboBox.addItem(agents.get(i));
	}

	/**
	 * call the parent agent frame to apply the registration details.
	 * 
	 * @param regStat
	 */
	private void setRegistrationProperties(PropertiesStatus regStat)
	{
			writeRegistrationDetails(regStat);
	}

	/**
	 * a function that is called to set the update properties.
	 * 
	 * @param updateStat
	 */
	private void setUpdateProperties(PropertiesStatus updateStat)
	{
//		if (!theRegistrator.writeUpdatedDetails(updateStat,this))
//			JOptionPane.showMessageDialog(this, "Could not update Properties file.\n Please check properties.xml", "DIMES Error", JOptionPane.ERROR_MESSAGE);
		this.writeRegistrationDetails(updateStat);
	}

	/* (non-Javadoc)
	 * @see dimes.gui.UpdateDetailsParentFrame#allowExitUpdateFrame()
	 */
	public void allowExitUpdateFrame()
	{
		// TODO Auto-generated method stub

	}
	
	public void writeRegistrationDetails(PropertiesStatus regStat) 
	{
		try{
		//String agentName = (String) regStat.agentNames.get(0);
		
		PropertiesBean.setProperty(PropertiesNames.REGISTERED_STATE/* "registered" */, "true");
		if(null==regStat.agentName){
			if (!(null==regStat.agentNames))
				PropertiesBean.setProperty(PropertiesNames.AGENT_NAME/* "agentName" */, regStat.agentNames.get(0).toString()/* regStat.agentName */);
		}else 	PropertiesBean.setProperty(PropertiesNames.AGENT_NAME/* "agentName" */, regStat.agentName/* regStat.agentName */);
		PropertiesBean.setProperty(PropertiesNames.EMAIL/* "email" */, regStat.email);
		PropertiesBean.setProperty(PropertiesNames.COUNTRY/* "country" */, regStat.country);
		PropertiesBean.setProperty(PropertiesNames.USER_NAME/* "userName" */, regStat.userName);
		
		if (regStat.hasPassword != null)
			PropertiesBean.setProperty(PropertiesNames.HAS_PSWD_STATE/* "hasPswd" */, "true");
		
		if (regStat.groupName != null)
			PropertiesBean.setProperty(PropertiesNames.GROUP/* "group" */, regStat.groupName);
		
		if (regStat.groupOwner != null)
			PropertiesBean.setProperty(PropertiesNames.GROUP_OWNER/* "groupOwner" */, regStat.groupOwner);
		}catch (IOException ioe){
			JOptionPane.showMessageDialog(this, "Could not update Properties file.\n Please check properties.xml", "DIMES Error", JOptionPane.ERROR_MESSAGE);
			System.err.println("Could not modify Properties file. Please check file permissions and try again");
			ioe.printStackTrace();
			System.exit(-1);
		}
	}

}

