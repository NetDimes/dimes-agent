/*
 * Created on 09/02/2005
 *
 */
package NetGraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import NetGraph.graph.GraphDisplayPanel;
import NetGraph.graph.components.discovery.ComponentDiscoveryDetails;
import NetGraph.graph.util.GraphPainter;
import NetGraph.graph.util.GraphRelaxer;
import NetGraph.graph.util.PaintFilterManager;
import NetGraph.utils.Configuration;
import NetGraph.utils.OverFlowException;
import dimes.comm2server.ConnectionHandlerFactory;
import dimes.comm2server.StandardCommunicator;
import dimes.scheduler.Priority;
import dimes.util.ResourceManager;
import dimes.util.gui.SpringUtilities;
import dimes.util.gui.SwingWorker;
import dimes.util.logging.ASInfoUpdateGraphHandler;
import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * @author Ohad
 * 
 * this class holds a NetGraph as a modular JPanel.
 * note that this function relays on a servlet whose address is in the properties file
 * under the tag "urls.ASInfoURL".
 * 
 */
public class GraphPanel extends JPanel{
	private Logger logger;
    
	JPanel controlPanel;	
	JPanel controlPanel2;	
	public GraphDisplayPanel displayPanel;
	
	// discovering scripts database :
//	Vector discoveringScriptsList = new Vector();
	//	JList scriptSelectionJList = new JList(discoveringScriptsList);
//	JComboBox discoveringListComboBox = new JComboBox();
	
	// Pruning constants :
	final static int PRUNING_LEVEL_MIN = 0;
	final static int PRUNING_LEVEL_MAX = 20;
	final static int PRUNING_LEVEL_INIT = 0;
	
	final static Color DISPLAY_COLOR = new Color(130,150,190);
	final static Color CONTROL_COLOR = new Color(210,210,210);
	final static Color MENU_CONTROL_COLOR = new Color(130,150,170);
	
	public Configuration config = new Configuration();
	
	
    private JMenu userScriptsMenu;
    private JMenu dimesScriptsMenu;
    
	private Vector userScripts = new Vector();
	private Vector dimesScripts = new Vector();
    private JComboBox selectProtocolComboBox;
    private JComboBox selectOperationComboBox;
    private JMenu expScriptsMenu;
        
    
    private JToolBar controlToolbar;
    private TimeRateLimiter postRateLimiter = new TimeRateLimiter(10);
    private JMenuItem selectAllDimesScripts;
    private JMenuItem selectAllUserScripts;
    private  javax.swing.Timer  coloringTimer;
    
	/***************
	 * construct a new PraphPanel.
	 * 
	 * @param containingFrame the frame containing the JPanel
	 * @param hidden indication on hidden.
	 */
	public GraphPanel(JFrame containingFrame, boolean hidden){
	    this.logger = Loggers.getLogger(GraphPanel.class);
	    
		this.setLayout(new BorderLayout());
		URL iconURL = this.getClass().getClassLoader().getResource(ResourceManager.dimesTransparent2_RSRC);
		Image localhostImage = Toolkit.getDefaultToolkit().getImage(iconURL);
		
		displayPanel = new GraphDisplayPanel(containingFrame,localhostImage,hidden);
		
		// create the panels and configure them :
		controlPanel = new JPanel();
		controlPanel2 = new JPanel();
		
		controlToolbar = new JToolBar();		

		controlPanel.setPreferredSize(new Dimension(700,60));
		controlPanel.setVisible(true);
		
		controlPanel2.setPreferredSize(new Dimension(120,600));
		controlPanel2.setBorder(new TitledBorder("Graph control"));
		controlPanel2.setVisible(true);
		
		displayPanel.setPreferredSize(new Dimension(580,560));
		displayPanel.setBackground(DISPLAY_COLOR);
		displayPanel.setVisible(true);
		
		// add the buttons and listeners to the controlPanel :
		designControlPanel();
		
		// add the panels to the main-panel:
		
		
		this.add(controlPanel,BorderLayout.SOUTH);
		this.add(controlPanel2,BorderLayout.WEST);
		this.add(displayPanel,BorderLayout.CENTER);
		
		createASInfoTimer(0);		
	}

	ActionListener taskPerformer = new ActionListener() {
	      public void actionPerformed(ActionEvent evt) {
	          askServerIPInfo(false);
	      }
	  };
	 

	/**
	 * This method is creating the coloringTimer which is responsible for the automatic Get Info aperation.
	 * Since the automatic get Info is limited to move between 5 to 60 min, this method checking the value
	 * of the argument and in case of breaking this borders it initializes the default value as the delay 
	 * time.
	 * Be aware that every call is cancelling the old coloringTimer and creating anew one.
	 *  
	 * @param The value in minutes of the new delay.
	 * @since 0.5.0
	 * @author idob
	 */
	public void createASInfoTimer(int askedDelay) {
		
		// Stopping previous running coloringTimer:
		if( coloringTimer != null && coloringTimer.isRunning() ){
			coloringTimer.stop();
			coloringTimer = null;
		}
		
		// Initialize the ask IP info timer :
		int delay = 10*60*1000;
		
		// Case of getting the delay from the properties file:
		if( askedDelay < 5 || askedDelay > 60){
			try 
			{
				String delayMinutesStr = PropertiesBean.getProperty(PropertiesNames.UPDATE_INFO_RATE);
				int delayMinutes = Integer.parseInt(delayMinutesStr);
				if(delayMinutes < 5 || delayMinutes > 60)
					delay = 10*60*1000;
				else
					delay = delayMinutes*60*1000;
			}
			catch (Exception e) 
			{
				delay = 10*60*1000;
			}
		}
		else
			delay = askedDelay*60*1000;
		
		logger.fine("Creating coloringTimer. Delay: " + delay);
		coloringTimer = new Timer(delay, taskPerformer);
		  coloringTimer.start();
		  
		// add a handler to the as info logger :
		Loggers.getASInfoLogger().addHandler(new ASInfoUpdateGraphHandler(this.displayPanel));
	}

	/**
	 * serves as a jbInit()...
	 */
	private void designControlPanel() {

		
	    JPanel pruningLevelPanel = new JPanel();
//		JPanel radioButtonsPanel = new JPanel();
		JPanel buttonsPanel = new JPanel();
		
		// set backgrounds and sizes :
		pruningLevelPanel.setPreferredSize(new Dimension(100,240));	
//		radioButtonsPanel.setPreferredSize(new Dimension(120,230));	
		buttonsPanel.setPreferredSize(new Dimension(100,80));
		
		// add the pruning master:
		
		JSlider pruningLevelSlider = new JSlider(JSlider.HORIZONTAL,
				PRUNING_LEVEL_MIN, PRUNING_LEVEL_MAX, PRUNING_LEVEL_INIT);
		pruningLevelSlider.setBorder(new EmptyBorder(3,5,10,5));
		pruningLevelSlider.setFont(new java.awt.Font("Dialog", 1, 9));
		pruningLevelSlider.addChangeListener(new ChangeListener(){
			
			public void stateChanged(ChangeEvent arg0) {
				int pruningLevel = ((JSlider)arg0.getSource()).getValue();
				displayPanel.prune(pruningLevel);			
			}
		});
		
		pruningLevelSlider.setMajorTickSpacing(10);
		pruningLevelSlider.setMinorTickSpacing(1);
		pruningLevelSlider.setPaintTicks(true);
		pruningLevelSlider.setPaintLabels(true);
		pruningLevelSlider.setBackground(pruningLevelPanel.getBackground());
		pruningLevelSlider.setPreferredSize(new Dimension(120,80));
		JLabel pruningLabel = new JLabel("Pruning Level",JLabel.CENTER);	

		
		pruningLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 13));		
		pruningLabel.setPreferredSize(new Dimension(120,25));

		JSlider damingFactorSlider = new JSlider(JSlider.HORIZONTAL,
				0,20,10);
		damingFactorSlider.setBorder(new EmptyBorder(7,5,7,5));
		damingFactorSlider.setBorder(new EmptyBorder(3,5,10,5));
		damingFactorSlider.setFont(new java.awt.Font("Dialog", 1, 9));
		damingFactorSlider.addChangeListener(new ChangeListener(){
			
			public void stateChanged(ChangeEvent arg0) {
				int dampLevel = 20-((JSlider)arg0.getSource()).getValue();
				displayPanel.setDampingFactor((double)dampLevel*(double)dampLevel*(double)dampLevel * 100.0);
			}
		});
		
		damingFactorSlider.setToolTipText("<html>Set damp level to the graph nodes<br>A bigger damp level will make the<br>" +
				"Nodes move lazily.</html>");
		
		damingFactorSlider.setMajorTickSpacing(10);
		damingFactorSlider.setMinorTickSpacing(1);
		damingFactorSlider.setPaintTicks(true);
		damingFactorSlider.setPaintLabels(true);
		damingFactorSlider.setBackground(pruningLevelPanel.getBackground());
		damingFactorSlider.setPreferredSize(new Dimension(120,80));
		
		JLabel dampFacroteLabelLabel = new JLabel("Damp Factor",JLabel.CENTER);		
		
		dampFacroteLabelLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 13));		
		dampFacroteLabelLabel.setPreferredSize(new Dimension(120,25));
		
		// Script Filter menues :		
		JPanel selectScriptMenuPanel = new JPanel();
		
		JMenuBar menuBar = new JMenuBar();
		
		menuBar.setBackground(MENU_CONTROL_COLOR);
		
		menuBar.setForeground(Color.BLACK);
		menuBar.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		menuBar.setBorder(new EtchedBorder());
		
		expScriptsMenu = new JMenu(PaintFilterManager.ALL_SCRIPTS_STRING);
		
		expScriptsMenu.setBackground(CONTROL_COLOR);
		
		expScriptsMenu.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		
		expScriptsMenu.setForeground(Color.BLACK);
		
		JMenuItem selectAllScriptsItem = new JMenuItem("All Scripts");
		userScriptsMenu = new JMenu("User's Scripts");
		dimesScriptsMenu = new JMenu("Dimes Scripts");
		
		selectAllDimesScripts =  new JMenuItem("All Scripts");
		
		selectAllDimesScripts.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		
		
		selectAllUserScripts =  new JMenuItem("All Scripts");
		
		selectAllUserScripts.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		
		
		// remove all the scripts and leave just "All scripts" of user and dimes.
		rebuildScriptSelectionMenues();
		
		selectAllScriptsItem.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		userScriptsMenu.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		dimesScriptsMenu.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		
		expScriptsMenu.add(selectAllScriptsItem);
		expScriptsMenu.add(userScriptsMenu);
		expScriptsMenu.add(dimesScriptsMenu);
			
		menuBar.add(expScriptsMenu);
		
		selectScriptMenuPanel.add(menuBar);
		
		// action listeners for menu selectors :
		// all users scripts :
		selectAllUserScripts.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Selecting All user's scripts");
                expScriptsMenu.setText("User's Scripts");
                displayPanel.setPriorityDisplayFilter(PaintFilterManager.USER_PRIORITY_FILTER);                
                displayPanel.setScriptDisplayFilter(PaintFilterManager.ALL_SCRIPTS_STRING);
            }
		    
		});
		// all dimes scripts :
		selectAllDimesScripts.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Selecting All Dime's scripts");
                expScriptsMenu.setText("Dimes Scripts");
                displayPanel.setPriorityDisplayFilter(PaintFilterManager.DIMES_PRIORITY_FILTER);
                displayPanel.setScriptDisplayFilter(PaintFilterManager.ALL_SCRIPTS_STRING);
                
            }
		    
		});
		// all scripts :
		selectAllScriptsItem.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent arg0) {   
                expScriptsMenu.setText("All Scripts");
                displayPanel.resetDisplayFilter();
            }
		    
		});
		
		
		
		
		// Select protocol combo :
		
		JPanel selectProtocolMenuPanel = new JPanel();
		selectProtocolMenuPanel.setPreferredSize(new Dimension(110,37));
		
		JMenuBar protocolMenuBar = new JMenuBar();
		protocolMenuBar.setBackground(MENU_CONTROL_COLOR);
		protocolMenuBar.setForeground(Color.BLACK);
		protocolMenuBar.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		protocolMenuBar.setBorder(new EtchedBorder());
		protocolMenuBar.setLayout(new BorderLayout());
		
		final JMenu protocolMenu = new JMenu(PaintFilterManager.ALL_PROTOCOLS_STRING);
		protocolMenu.setBackground(CONTROL_COLOR);
		
//		protocolMenu.setBackground(Color.WHITE);
		
		protocolMenu.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		
		
		
		protocolMenu.setForeground(Color.BLACK);
		
		JMenuItem selectAllProtocolsItem = new JMenuItem(PaintFilterManager.ALL_PROTOCOLS_STRING);
		
		JMenuItem selectIcmpItem = new JMenuItem(PaintFilterManager.ICMP_PROTOCOL_FILTER);
		JMenuItem selectUdpItem = new JMenuItem(PaintFilterManager.UDP_PROTOCOL_FILTER);
		
		
		selectAllProtocolsItem.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		selectAllProtocolsItem.setBackground(CONTROL_COLOR);
		selectIcmpItem.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		selectIcmpItem.setBackground(CONTROL_COLOR);
		selectUdpItem.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		selectUdpItem.setBackground(CONTROL_COLOR);
		
	
		protocolMenu.add(selectAllProtocolsItem);
		protocolMenu.add(selectIcmpItem);
		protocolMenu.add(selectUdpItem);
		
		selectAllProtocolsItem.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Selecting Icmp  Protocol");
                protocolMenu.setText(PaintFilterManager.ALL_PROTOCOLS_STRING);
                displayPanel.setProtocolDisplayFilter(PaintFilterManager.ALL_PROTOCOLS_STRING);                
            }		    
		});
		
		
		selectIcmpItem.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent arg0)
            {
                System.out.println("Selecting Icmp  Protocol");
                protocolMenu.setText(PaintFilterManager.ICMP_PROTOCOL_FILTER);
                displayPanel.setProtocolDisplayFilter(PaintFilterManager.ICMP_PROTOCOL_FILTER);                
            }		    
		});
		
		selectUdpItem.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Selecting UDP Protocol");
                protocolMenu.setText(PaintFilterManager.UDP_PROTOCOL_FILTER);
                displayPanel.setProtocolDisplayFilter(PaintFilterManager.UDP_PROTOCOL_FILTER);                
            }
		    
		});
		
		protocolMenuBar.add(protocolMenu,BorderLayout.CENTER);
		selectProtocolMenuPanel.add(protocolMenuBar);
		
		JPanel selectOperationMenuPanel = new JPanel();
		selectOperationMenuPanel.setPreferredSize(new Dimension(110,37));
		
		JMenuBar operationMenuBar = new JMenuBar();
		
		operationMenuBar.setBackground(MENU_CONTROL_COLOR);
		operationMenuBar.setForeground(Color.BLACK);
		operationMenuBar.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		operationMenuBar.setBorder(new EtchedBorder());
		
		final JMenu operationMenu = new JMenu(PaintFilterManager.ALL_OPERATIONS_STRING);
		
		operationMenu.setBackground(CONTROL_COLOR);
		
		operationMenu.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		
		operationMenu.setForeground(Color.BLACK);
		
		JMenuItem selectAllOperationsItem = new JMenuItem(PaintFilterManager.ALL_OPERATIONS_STRING);
		JMenuItem selectTracerouteItem = new JMenuItem(PaintFilterManager.TRACEROUTE_OPERATION_FILTER);
		JMenuItem selectPingItem = new JMenuItem(PaintFilterManager.PING_OPERATION_FILTER);
		
		
		selectAllOperationsItem.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		selectTracerouteItem.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		selectPingItem.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		
		operationMenu.add(selectAllOperationsItem);
		operationMenu.add(selectTracerouteItem);
		operationMenu.add(selectPingItem);
		
		
		selectAllOperationsItem.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Selecting All Operations");
                operationMenu.setText(PaintFilterManager.ALL_OPERATIONS_STRING);
                displayPanel.setOperationDisplayFilter(PaintFilterManager.ALL_OPERATIONS_STRING);
                
            }
		    
		});
		
		
		selectTracerouteItem.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Selecting Traceroute  Operation");
                operationMenu.setText(PaintFilterManager.TRACEROUTE_OPERATION_FILTER);
                displayPanel.setOperationDisplayFilter(PaintFilterManager.TRACEROUTE_OPERATION_FILTER);
                
            }
		    
		});
		
		selectPingItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Selecting ping  Operation");
                operationMenu.setText(PaintFilterManager.PING_OPERATION_FILTER);
                displayPanel.setOperationDisplayFilter(PaintFilterManager.PING_OPERATION_FILTER);
                
            }
		    
		});
		

		operationMenuBar.add(operationMenu);
		selectOperationMenuPanel.add(operationMenuBar);
		
		pruningLevelPanel.setLayout(new GridLayout(8,1));

		pruningLevelPanel.add(selectProtocolMenuPanel);
		pruningLevelPanel.add(selectOperationMenuPanel);
		pruningLevelPanel.add(selectScriptMenuPanel);
		JRadioButton emptyButton2 = new JRadioButton();
		emptyButton2.show(false);
		emptyButton2.enable(true);
		
		
//		pruningLevelPanel.add(emptyButton2);
		
		pruningLevelPanel.add(pruningLabel);
		pruningLevelPanel.add(pruningLevelSlider);
		
//		JRadioButton emptyButton3 = new JRadioButton();
		
//		JRadioButton emptyButton4 = new JRadioButton();
		pruningLevelPanel.add(dampFacroteLabelLabel);
		pruningLevelPanel.add(damingFactorSlider);
		
//		pruningLevelPanel.add(damingFactorSlider/*,BorderLayout.NORTH*/);
//		
//		pruningLevelPanel.add(damingFactorSlider/*,BorderLayout.CENTER*/);
		
		
		
		final JButton collapseButton = new JButton("Collapse");
		JButton colorButton = new JButton("Get info");
//		JButton postButton = new JButton("Post gallery");
		
//		postButton.addActionListener(new GraphPanel_postButton_actionAdapter(this));
		
	
		colorButton.requestFocus();		
		collapseButton.setFont(new java.awt.Font("Comic Sans MS", 1, 12));
		colorButton.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		
		
//		postButton.setFont(new java.awt.Font("Comic Sans MS", 1, 11));
		
		colorButton.setToolTipText("<html>Query the server for Country/ISP information.<br>" +
											"The frequency of automatic update can be changed in the Properties window" +
									"</html>");
		collapseButton.setToolTipText("<html>Collapse all nodes and <br>" +
				"Edges between two vantage points into <br>" +
				"One single edge.<br>" +
				"You can use the right mouse click to collapse <br>" +
				"a single vantage point.</html>");
		
		collapseButton.addActionListener(new ActionListener(){
			boolean isCollapsed = false;
			
			public void actionPerformed(ActionEvent arg0) {
				if (!isCollapsed)
				{
					collapseButton.setLabel("Expand");
					displayPanel.makeAllCollapse();
				}
				else
				{
					collapseButton.setLabel("Collapse");
					displayPanel.makeAllExpand();
				}
				isCollapsed = !isCollapsed;
				
			}
			
		});
		
		colorButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker colorGrpahWorker = new SwingWorker(){
                    public Object construct() {
                        askServerIPInfo();
                        return null;
                    }
				};
				colorGrpahWorker.start();
			}
			
		});
		
		buttonsPanel.setLayout(new GridLayout(2,1) );
		//		buttonsPanel.add(listScroller);
		buttonsPanel.add(collapseButton);
		buttonsPanel.add(colorButton);
//		buttonsPanel.add(postButton);
		
		// Graph toolbar Panel :
		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setVisible(true);
		toolBarPanel.setBorder(new EmptyBorder(10,10,10,10));
		toolBarPanel.setPreferredSize(new Dimension(100,80));
		toolBarPanel.setLayout(new GridLayout(2,2) );
		
		URL magnifyIcon = this.getClass().getClassLoader().getResource("resources/m1.GIF");
		URL relaxNormalyIcon = this.getClass().getClassLoader().getResource("resources/handtool.gif");
		URL activeDragIcon = this.getClass().getClassLoader().getResource("resources/a1.GIF");
		URL strictDragIcon = this.getClass().getClassLoader().getResource("resources/glue.gif");
		
		Image magnifyImage=null;
		Image relaxNormalyImage=null;
		Image activeDragImage=null;
		Image strictDragImage=null;
		
//		if (magnifyIcon!=null)
//		{
		    magnifyImage  = Toolkit.getDefaultToolkit().getImage(magnifyIcon).getScaledInstance(20,20,Image.SCALE_SMOOTH);
		    relaxNormalyImage  = Toolkit.getDefaultToolkit().getImage(relaxNormalyIcon);
		    activeDragImage  = Toolkit.getDefaultToolkit().getImage(activeDragIcon).getScaledInstance(20,20,Image.SCALE_SMOOTH);
		    strictDragImage  = Toolkit.getDefaultToolkit().getImage(strictDragIcon).getScaledInstance(20,20,Image.SCALE_SMOOTH);
//		}
		
		final JToggleButton magnifyButton = new JToggleButton(new ImageIcon(magnifyImage));
		
		final JToggleButton normalRelaxButton = new JToggleButton(new ImageIcon(relaxNormalyImage));
		final JToggleButton activeDragRelaxButton = new JToggleButton(new ImageIcon(activeDragImage));
		final JToggleButton strictDragRelaxButton = new JToggleButton(new ImageIcon(strictDragImage));
		
		normalRelaxButton.setSelected(true);
		
		magnifyButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                GraphPanel.this.displayPanel.setMagnify(magnifyButton.isSelected());
            }
		});
		magnifyButton.setToolTipText("<html> Pressing this button will cause<br>" +
				" a magnifying effect on<br> the nodes which are close to <br> the mouse </html>");
		normalRelaxButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                GraphPanel.this.displayPanel.setRelaxManner(GraphRelaxer.RELAX_NORMAL);
            }
		});
		normalRelaxButton.setToolTipText("<html>Pressing this button will cause<br>" +
											   "The Nodes to loosely relax on <br>" +
											   "The screen.</html>");
		activeDragRelaxButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                GraphPanel.this.displayPanel.setRelaxManner(GraphRelaxer.RELAX_ACTIVE_DRAG);
            }
		});
		activeDragRelaxButton.setToolTipText("<html>Pressing this button will encourage<br>" +
				   								   "The Nodes to stand on a straight line <br>" +
												   "from source to target node.</html>" );
		
		strictDragRelaxButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
              GraphPanel.this.displayPanel.setRelaxManner(GraphRelaxer.RELAX_STRICT_DRAG);
            }
		});
		
		strictDragRelaxButton.setToolTipText("<html>Pressing this button will force<br>" +
				   "The Nodes to stand on a straight line <br>" +
				   "from source to target node.<br>" +
				   "try dragging a target node <br>" +
				   "to notice the effect.</html>" );
		ButtonGroup relaxButtonGroup = new ButtonGroup();
		relaxButtonGroup.add(normalRelaxButton);
		relaxButtonGroup.add(activeDragRelaxButton);
		relaxButtonGroup.add(strictDragRelaxButton);
		
		toolBarPanel.add(magnifyButton);
		toolBarPanel.add(normalRelaxButton);
		toolBarPanel.add(activeDragRelaxButton);
		toolBarPanel.add(strictDragRelaxButton);
		
		// add to the control panel :
		controlPanel2.add(pruningLevelPanel,BorderLayout.NORTH);
		controlPanel2.add(toolBarPanel,BorderLayout.CENTER);
		controlPanel2.add(buttonsPanel,BorderLayout.SOUTH);
		
		
		// paint all as dots at first :
		config.put(config.NodeLabel,3);	
		
		JRadioButton countryColorDisplayButton = new JRadioButton("Country");
		JRadioButton ISPColorDisplayButton = new JRadioButton("ISP");
		ButtonGroup colorDisplayGroup = new ButtonGroup();
		colorDisplayGroup.add(countryColorDisplayButton);
		colorDisplayGroup.add(ISPColorDisplayButton);
		ISPColorDisplayButton.setSelected(true);
		
		countryColorDisplayButton.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		countryColorDisplayButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				displayPanel.setColorDisplay(GraphPainter.COUNTRY_COLOR_DISPLAY);			
			}});
		
		ISPColorDisplayButton.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		ISPColorDisplayButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				displayPanel.setColorDisplay(GraphPainter.ISP_COLOR_DISPLAY);			
			}});
		
		JLabel colorByLabel = new JLabel("Color By:");
		colorByLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		
		JRadioButton dotAsNmaeDisplayButton = new JRadioButton("dot");
		JRadioButton machineNameDisplayButton = new JRadioButton("machine");
		JRadioButton completeNameDisplayButton = new JRadioButton("complete name");
		JRadioButton ipDisplayButton = new JRadioButton("IP");
		JRadioButton countryNameDisplayButton = new JRadioButton("Country");
		JRadioButton ISPNameDisplayButton = new JRadioButton("ISP");
		ButtonGroup nameDisplayGroup = new ButtonGroup();
		
		nameDisplayGroup.add(dotAsNmaeDisplayButton);
		nameDisplayGroup.add(machineNameDisplayButton);
		nameDisplayGroup.add(completeNameDisplayButton);
		nameDisplayGroup.add(ipDisplayButton);
		nameDisplayGroup.add(ISPNameDisplayButton);
		nameDisplayGroup.add(countryNameDisplayButton);
		
		dotAsNmaeDisplayButton.setSelected(true);
		
		dotAsNmaeDisplayButton.setFont(new java.awt.Font("Comic Sans MS", 1, 13));	
		dotAsNmaeDisplayButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				config.put(config.NodeLabel,3);
				displayPanel.ChangeLabel(3);		
			}});
		
		machineNameDisplayButton.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		machineNameDisplayButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				config.put(config.NodeLabel,0);
				displayPanel.ChangeLabel(0);		
			}});
		
		completeNameDisplayButton.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		completeNameDisplayButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				config.put(config.NodeLabel,2);
				displayPanel.ChangeLabel(2);		
			}});
		
		ipDisplayButton.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		ipDisplayButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				config.put(config.NodeLabel,1);
				displayPanel.ChangeLabel(1);		
			}});
		
		ISPNameDisplayButton.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		ISPNameDisplayButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				config.put(config.NodeLabel,4);
				displayPanel.ChangeLabel(4);		
			}});
		
		countryNameDisplayButton.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		countryNameDisplayButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				config.put(config.NodeLabel,5);
				displayPanel.ChangeLabel(5);		
			}});
		
		JLabel nameByLabel = new JLabel("Name By:");
		nameByLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		
		
		
		JLabel groupByLabel = new JLabel("Group By:");
		groupByLabel.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
		
		JRadioButton groupByNoneButton = new JRadioButton("None");
		JRadioButton groupByCountryButton = new JRadioButton("Country");
		JRadioButton groupByISPButton = new JRadioButton("ISP");
		
		ButtonGroup groupByRelaxGroup = new ButtonGroup();
		groupByRelaxGroup.add(groupByNoneButton);
		groupByRelaxGroup.add(groupByCountryButton);
		groupByRelaxGroup.add(groupByISPButton);
		
		groupByNoneButton.setSelected(true);
		
		
		groupByNoneButton.setFont(new java.awt.Font("Comic Sans MS", 1, 13));	
		groupByNoneButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				displayPanel.startRelaxingByDefault();		
			}});
		
		groupByCountryButton.setFont(new java.awt.Font("Comic Sans MS", 1, 13));	
		groupByCountryButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				displayPanel.startRelaxingByCountry();	
			}});
		
		groupByISPButton.setFont(new java.awt.Font("Comic Sans MS", 1, 13));	
		groupByISPButton.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				displayPanel.startRelaxingByISP();		
			}});
		
		
		
		
		
//		 Radio Buttons : 
		JPanel colorRadioPanel = new JPanel();
		JPanel nameRadioPanel = new JPanel();
		JPanel groupRadioPanel = new JPanel();
		
		colorRadioPanel.setBorder(new TitledBorder("Color by"));
		nameRadioPanel.setBorder(new TitledBorder("Name by"));
		groupRadioPanel.setBorder(new TitledBorder("Group by"));
		
		colorRadioPanel.setLayout(new SpringLayout());
		nameRadioPanel.setLayout(new SpringLayout());
		groupRadioPanel.setLayout(new SpringLayout());
		
		
		
		JPanel emptySpacePanel = new JPanel();
		
		colorRadioPanel.add(countryColorDisplayButton);
		colorRadioPanel.add(ISPColorDisplayButton);
		SpringUtilities.makeCompactGrid(colorRadioPanel,1,2,0,0,0,0);
		
		nameRadioPanel.add(dotAsNmaeDisplayButton);
		nameRadioPanel.add(machineNameDisplayButton);
		nameRadioPanel.add(ipDisplayButton);
		nameRadioPanel.add(ISPNameDisplayButton);
		nameRadioPanel.add(countryNameDisplayButton);
		
		SpringUtilities.makeCompactGrid(nameRadioPanel,1,5,0,0,0,0);
		
		JRadioButton emptyButton = new JRadioButton();
		emptyButton.show(false);
		emptyButton.enable(false);
		groupRadioPanel.add(groupByNoneButton);
		groupRadioPanel.add(groupByCountryButton);
		groupRadioPanel.add(groupByISPButton);
		SpringUtilities.makeCompactGrid(groupRadioPanel,1,3,0,0,0,0);
		

		emptySpacePanel.setPreferredSize(new Dimension(120,10));
		emptySpacePanel.setBackground(controlPanel.getBackground());
		
		controlPanel.setLayout(new BorderLayout());
		controlPanel.add(colorRadioPanel,BorderLayout.WEST);
		controlPanel.add(nameRadioPanel,BorderLayout.CENTER);
		controlPanel.add(groupRadioPanel,BorderLayout.EAST);

	}
	
	
	
	/**
     * 
     * 
     */
    private void rebuildScriptSelectionMenues() {
        dimesScriptsMenu.removeAll();
		userScriptsMenu.removeAll();
		dimesScriptsMenu.add(selectAllDimesScripts);
		userScriptsMenu.add(selectAllUserScripts);
    }



//    /**
//     * @param image
//	 * @throws NoSuchPropertyException
//	 * @throws IOException
//     */
//    protected void postImage(BufferedImage image) throws IOException, NoSuchPropertyException {
//       if (!postRateLimiter.allowAttempt())
//       {
//           JOptionPane.showMessageDialog(this,
//					"Image post failed.\nYou may send one capture" +
//					" every 10 minutes.",
//					"DIMES Error",
//					JOptionPane.ERROR_MESSAGE);
//           return;
//       }
//           
//        FileHandlerBean fileHandler = new FileHandlerBean();
//        File outgoing = fileHandler.getOutgoingFileSlot();
//        
//        ImageIO.write(image,"JPEG",outgoing);
//        
////        String agentName = PropertiesBean.getProperty(PropertiesNames.AGENT_NAME/*"agentName"*/);
////        String galleryPostURL = PropertiesBean.getProperty(PropertiesNames.GALLERY_POST_URL/*"galleryPostURL"*/);
//        
////        BinaryCommunicator comm = new BinaryCommunicator(galleryPostURL+"?agentName=\""+agentName+"\"",ConnectionHandlerFactory.NONSECURE_CONNECTION);
////        
////        File incoming = fileHandler.getIncomingFileSlot();
////        
////        System.out.println("Exchanging photo...");
////        try
////        {           
////            comm.exchangeFiles(outgoing,incoming);
////        }
////        catch (IOException ex)
////        {            
////            ex.printStackTrace();
////        }
////        
////        if (incoming == null || !ResponseHandler.isAck(incoming))
////        {
////            JOptionPane.showMessageDialog(this,
////					"Image post failed.\nPlease try later " +
////					"\n or contact us at support@www.netdimes.org",
////					"DIMES Error",
////					JOptionPane.ERROR_MESSAGE);
////        }
////        else
////        {
////            JOptionPane.showMessageDialog(this,
////					"Image posted succesfully.\nPlease visit our gallery at " +
////					"\n www.netdimes.org",
////					"DIMES",
////					JOptionPane.INFORMATION_MESSAGE);
////        }
////        fileHandler.handleAfterUsage(outgoing,true);
////        fileHandler.handleAfterUsage(incoming,true);
////        
//    }

    
    
    
    /************
	 * an interface function which starts the displayPanel thread.
	 *
	 */
	public void start(){
		displayPanel.start();
	}
	/************
	 * an interface function which stops the displayPanel thread.
	 *
	 */
	public void stop(){
	    coloringTimer.stop();
		displayPanel.stop();
	}	
	
	public void askServerIPInfo(){
	    askServerIPInfo(true);
	}
	
	
	/**
	 * <p>
	 * this function is used whenever a user presses the Get Info button.
	 * it connects to the server, asks the IPs information and publishes it to the graph.
	 * </p>
	 * 
	 * <p>
	 * The method was changed to use ByteArrayInputStream for the incoming data - 
	 * instead of file - in order to decrease the file system dependancy.
	 * </p>
	 * 
	 * @author idob
	 * @since 0.5.0
	 */
	private void askServerIPInfo(boolean notifyErrors){
		StandardCommunicator comm = null;
		Logger asInfoLogger = Loggers.getASInfoLogger();
		String asInfoUrl = null;
		try {
			asInfoUrl = PropertiesBean.getProperty(PropertiesNames.AS_INFO_URL/*"urls.ASInfoURL"*/);

			comm = new StandardCommunicator(asInfoUrl,ConnectionHandlerFactory.NONSECURE_CONNECTION);
			
		} catch (MalformedURLException e) {
		    if (notifyErrors)
		    {
			Loggers.getLogger().warning("Error while coloring Graph:" + e.getMessage());
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"The URL used to communicate with DIMES Server is Malformed. \n " +
					"Please check it in propertis.xml: \n"+asInfoUrl,
					"Communication failure",
					JOptionPane.ERROR_MESSAGE);
		    }
			return;
		}
		catch (NoSuchPropertyException e) {
		    if (notifyErrors)
		    {
			Loggers.getLogger().warning("Error while coloring Graph:" + e.getMessage());
			JOptionPane.showMessageDialog(this,
					"This URL used to communicate with DIMES Server is non existant. \n " +
					"your propertis.xml is probably corrupt. \n"+asInfoUrl,
					"Properties failure",
					JOptionPane.ERROR_MESSAGE);
		    }
			return;
		}
		
		try 
		{
			// create the header and the Trailer :
			String userID;			
			userID = PropertiesBean.getProperty(PropertiesNames.AGENT_NAME/*"agentName"*/);			
			String header = "<agent name=\"" + userID +"\">\n";
			String trailer = "</agent>";
			
			// create the outgoing string as a BufferedReader :
			Vector unretreivedIPs = displayPanel.getUnretreivedIPs();
			// mark all retrieved for the user not to retreive again :
			displayPanel.markAllRetrieved();
			if (unretreivedIPs.size() == 0)
				return;
			
			String outgoingString = formatIPRequestString(unretreivedIPs);
			
			BufferedReader ipsBuffer = new BufferedReader(new StringReader(outgoingString));
			
			// create the incomingFile Stream :			
			ByteArrayOutputStream retrievedIPs = new ByteArrayOutputStream(); 
			// exchange files :
			try
			{
				comm.exchangeFiles(ipsBuffer, retrievedIPs, header, trailer);
			}
			catch (Exception e)
			{
			    if (notifyErrors)
			    {
				Loggers.getLogger().warning("Error while coloring Graph:" + e.getMessage());
				JOptionPane.showMessageDialog(this,
						"Communication with the DIMES Server failed. \n Please try again later. ",
						"Communication failure",
						JOptionPane.ERROR_MESSAGE);
			    }
				return;
			}	
			
			String ipsTrimmedStr = retrievedIPs.toString().trim(); 
			if (ipsTrimmedStr != null){
				logger.info("Publishing Nodes Info in Graph");
			    asInfoLogger.finest(ipsTrimmedStr);
			}    
			else
			    Loggers.getLogger().warning("Info Request returned null.");
			retrievedIPs.close();
		} catch (IOException e1)
		{		    
			Loggers.getLogger().warning("Error while coloring Graph:" + e1.getMessage());
		} catch (NoSuchPropertyException e) {
			Loggers.getLogger().warning("Error while coloring Graph:" + e.getMessage());
		} 
	}
	
	public String formatIPRequestString(Vector unretreivedIPs){
	    Iterator i = unretreivedIPs.iterator();
		StringBuffer outgoingString = new StringBuffer("<request-for-IP-info>\n");
		
		while (i.hasNext()){
			String ipToRetreive = (String) i.next();
			outgoingString.append("<ip address=\"");
			outgoingString.append(ipToRetreive);
			outgoingString.append("\"/>\n");			
		}
		outgoingString.append("</request-for-IP-info>\n");
		return outgoingString.toString();
	}
	
	/**
	 * @param localHost
	 */
	public void findNode(InetAddress host) {
		displayPanel.findNode(host,this.getSize());	
	}
	
	/**
	 * @param b
	 */
	public void setHidden(boolean hidden) {
		displayPanel.setHidden(hidden);
		
	}
	
	/**
	 * 
	 */
	public void centerLocalhost() {
		displayPanel.centerLocalhost(this.getSize());
		
	}
	
	/**
	 * clears the graph brutally.
	 */
	public synchronized void Clear() {
	    
		displayPanel.Clear();
		rebuildScriptSelectionMenues();
		this.validate();
		// TODO : clear the scripts list :
		// getActiveScriptsList...
		// getActiveExperimentsList...
	}
	
	/**
	 * @param i
	 */
	public void ChangeLabel(int i) {
		displayPanel.ChangeLabel(i);
		
	}
	
	public void repaint(){
		super.repaint();
		if (displayPanel != null)
			displayPanel.repaint();
	}
	
	
	/**
	 * clears the graph in a soft way. 
	 */
	public void softClear() {
		displayPanel.softClear();
		// TODO : clear the scripts list :
		
	}
	
	
	
	/**
	 * collapse all the nodes in the Grpah.
	 */
	public void makeAllCollapse() {
		displayPanel.makeAllCollapse();
		
	}
	
	
	/************************
	 * use this function in order to add traces into the Graph.
	 * 
	 * @param hops a Vector of InetAddress Objects.
	 * @param scriptName the script name 
	 * @throws OverFlowException
	 */
	public synchronized void addTrace(Vector hops, String expName, 
	        String scriptName , String protocol, 
	        String priority ) throws /*UnknownHostException, */OverFlowException
	{
	    
	    /*System.out.println*/logger.finer("Adding : " +scriptName + " - " + expName 
	            + " - " + PaintFilterManager.TRACEROUTE_OPERATION_FILTER + " - " + protocol + " - " + priority);
	    if (priority.equals(Priority.getName(Priority.USER)))
	    {
	        priority = PaintFilterManager.USER_PRIORITY_FILTER;
	    	if (!userScripts.contains(scriptName))
	    	{
	    	    	
	    	    	final String finalScriptName = new String(scriptName);
	    	    	JMenuItem scriptItem = new JMenuItem(scriptName);
	    	    	scriptItem = new JMenuItem(scriptName);
	    	    	scriptItem.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
	    	    	scriptItem.addActionListener(new ActionListener(){

                        public void actionPerformed(ActionEvent arg0) {
                            /*System.out.println*/logger.finer("Selecting script:" +finalScriptName );
                            expScriptsMenu.setText((finalScriptName.length()<10)?finalScriptName:finalScriptName.substring(0,9));
                            displayPanel.setPriorityDisplayFilter(PaintFilterManager.USER_PRIORITY_FILTER);
                            displayPanel.setScriptDisplayFilter(finalScriptName);                            
                        }
	    	    	    
	    	    	});
	    	    	
	    	    	userScriptsMenu.add(scriptItem);
	    	        userScripts.add(scriptName);
	    	        this.validate();
	    	}
	    }
	    else	    
	    {
	        priority = PaintFilterManager.DIMES_PRIORITY_FILTER;
	        if (!dimesScripts.contains(scriptName))
	        {
	            dimesScripts.add(scriptName);
	            final String finalScriptName = new String(scriptName);
    	    	JMenuItem scriptItem = new JMenuItem(scriptName);
    	    	scriptItem.setFont(new java.awt.Font("Comic Sans MS", 1, 13));
    	    	scriptItem.addActionListener(new ActionListener(){

                    public void actionPerformed(ActionEvent arg0) {
                        System.out.println("Selecting script:" +finalScriptName );
                        expScriptsMenu.setText((finalScriptName.length()<10)?finalScriptName:finalScriptName.substring(0,9));
                        displayPanel.setPriorityDisplayFilter(PaintFilterManager.DIMES_PRIORITY_FILTER);
                        displayPanel.setScriptDisplayFilter(finalScriptName);                            
                    }
    	    	    
    	    	});
    	    	
    	    	dimesScriptsMenu.add(scriptItem);
    	        dimesScripts.add(scriptName);
    	        this.validate();
	        }
	    }
	        
	    
	    	
	    	
//		if (!discoveringScriptsList.contains(scriptName)){
//			discoveringListComboBox.addItem(scriptName);
//			discoveringScriptsList.add(scriptName);
//		}
		
//		if (scriptName == null)
//			scriptName = "DefaultScript";
		
		
		InetAddress oldIP=null, newIP=null;
		OverFlowException ex = null;
		try
		{
			oldIP = InetAddress.getLocalHost();
		}
		catch (UnknownHostException e1)
		{
			try
			{
				oldIP = InetAddress.getByName("127.0.0.1");
			}
			catch (UnknownHostException e)//not possible to get here...
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		boolean headingAnOverFlow = false;
		
		for (int i=0; i<hops.size(); ++i)
		{
			newIP = (InetAddress)hops.get(i);//InetAddress.getByName((String)hops.get(i));
			try
			{
				//			ecranprincipal.graph.addEdge(oldIP,newIP,55,null);
			    ComponentDiscoveryDetails details = new ComponentDiscoveryDetails(scriptName , expName,
			            PaintFilterManager.TRACEROUTE_OPERATION_FILTER
			            ,protocol,priority);
			    displayPanel.addEdge(oldIP,newIP,55,details);            
			}
			catch (OverFlowException e2)
			{
				if(e2.getOverflowClossness() < hops.size()-i )
				    Clear();
				headingAnOverFlow=true;
				ex=e2;
			}
			oldIP = newIP;
			try
			{//in order to see the trace advancing
				Thread.sleep(100);//check - anat
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (headingAnOverFlow)
			Clear();
		if (ex != null)
			throw ex;
		
	}
	
	/**
	 * expand all the nodes that are collapsed.
	 */
	public void makeAllExpand() {
		this.displayPanel.makeAllExpand();	
	}
	
	/***************
	 * a test frame fro GraphPanel.
	 * @param args
	 * @throws Exception
	 */
/*	public static void main(String[] args) throws Exception{
		
		SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack("resources/toxicthemepack.zip"));
		UIManager.setLookAndFeel("com.l2fprod.gui.plaf.skin.SkinLookAndFeel");
		//		create a Frame :
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		
		JFrame f = new JFrame();
		f.setSize(700,600);
		// add the grap panel :
		final GraphPanel g = new GraphPanel(f,false);
		f.getContentPane().add(g);
		g.start();
		f.show();
		f.addWindowListener(new WindowListener(){
			
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void windowClosing(WindowEvent arg0) {
				g.stop();
				System.exit(0);
				
			}
			
			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}*/
	
	


    /**
     * @param e
     */
    public void saveButton_actionPerformed(ActionEvent e)
    {
		displayPanel.saveGraph();        
    }
    
    public String getCurrectGraphStateRecord(){
        return this.displayPanel.export();         
     }

    
    
//    /**
//     * @param e
//     */
//    public void postButton_actionPerformed(ActionEvent e)
//    {
//    	// rate limit the gallery post:
//        long currentTime = System.currentTimeMillis();
//        
//		SwingWorker worker = new SwingWorker(){
//
//            public Object construct() {
//                BufferedImage image = displayPanel.getGraphImage();
//                try 
//                {
//                    GraphPanel.this.postImage(image);
//                    image.flush();//check - free image memory
//                } 
//                catch (IOException e) {
//                    JOptionPane.showMessageDialog(GraphPanel.this,
//        					"Image post failed.\nreason is : \n"+
//        					e.getMessage()+
//        					"\n Please try later or contact us at support@www.netdimes.org",
//        					"DIMES",
//        					JOptionPane.ERROR_MESSAGE);
//                } catch (NoSuchPropertyException e) {
//                    JOptionPane.showMessageDialog(GraphPanel.this,
//        					"Image post failed.\nInvalid Properties file:\n"+
//        					e.getMessage()+
//        					"\n Please fix properties.xml or contact us at support@www.netdimes.org",
//        					"DIMES",
//        					JOptionPane.ERROR_MESSAGE);
//                }
//                return null;
//            }
//		    
//		};
//		worker.start();   
//    }



    /**
     * @param destIP
     * @param bestTime
     * @param scriptName
     * @param protocol
     * @param protocol2
     * @param priority
     */
    public void addPing(
            String destIP, int bestTime, String scriptName, 
            String protocol, String protocol2, String priority) 
    throws OverFlowException{
        
        
    }
	
	
}


class GraphPanel_saveButton_actionAdapter implements java.awt.event.ActionListener
{
	GraphPanel adaptee;

	GraphPanel_saveButton_actionAdapter(GraphPanel adaptee)
	{
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e)
	{
		adaptee.saveButton_actionPerformed(e);
	}
}

//class GraphPanel_postButton_actionAdapter implements java.awt.event.ActionListener
//{
//	GraphPanel adaptee;
//
//	GraphPanel_postButton_actionAdapter(GraphPanel adaptee)
//	{
//		this.adaptee = adaptee;
//	}
//	public void actionPerformed(ActionEvent e)
//	{
//		adaptee.postButton_actionPerformed(e);
//	}
//}

//class MagnifyAction extends AbstractAction
//{
//    public MagnifyAction(String text, ImageIcon icon)
//    {
//        super(text, icon);
//    }
//    /* (non-Javadoc)
//     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//     */
//    public void actionPerformed(ActionEvent e)
//    {
//        System.out.println("Here...");
////        this.setEnabled(false);
////        pauseAction.setEnabled(true);
////        resumeButton_actionPerformed(e);
//    }       
//}


/****************
* 
* @author Ohad Serfaty
*
*/
class TimeRateLimiter{
    
    static final long MILISECONDS_IN_MINUTE = 60000;
    int minutesBeforeReAllow;    
    private long lastSuccessfulAttemp = -1;
    
    public TimeRateLimiter(int minutesBetweenAllows){
        minutesBeforeReAllow = minutesBetweenAllows;
    }
    
    public boolean allowAttempt(){
        if (lastSuccessfulAttemp == -1)
	        return success();
        
        long currentTime = System.currentTimeMillis();
        if (( (currentTime-lastSuccessfulAttemp)/MILISECONDS_IN_MINUTE) > minutesBeforeReAllow)
            return success();

        return false;
    }
    
    public boolean success()
    {
        lastSuccessfulAttemp = System.currentTimeMillis();
        return true;
    }
        
}

