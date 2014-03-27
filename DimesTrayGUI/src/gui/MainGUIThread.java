package gui;


import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import main.Controller;
import util.PropertiesBean;


/**
 * <p>
 * Class to represent main GUI Thread of the Agent SysTray. Responsible for creating the different tray icons and the sub components and manage them.
 * </p>
 *  Bug fixes in ver.2:
 *  <ol>
 *  <li>Icon 'blinks' each calling to checkState() - changed by removing the current icon and adding new one only if there is a real state change</li>
 *  <li>Icon vanished - changed by creating 3 stable icons and make changes between them.</li>
 *  </ol>
 * @author BoazH(ver. 1), Idob(ver.2)
 * @version 2.0
 *
 */
public class MainGUIThread implements Runnable {

	// A flag to inform the Controller either system-tray is supported on this system.
	private boolean systemTraySupported=true;
	
	// Different tray icons to be changed when state is changed
	private TrayIcon trayIconRed; 
	private TrayIcon trayIconYellow;
	private TrayIcon trayIconGreen;
	
	// Current tray icon in use
	private TrayIcon currentTrayIcon;
	
	String resourcePath = PropertiesBean.getResourcePath() + File.separator;
	private PopupMenu popup = new PopupMenu();

	public boolean isSystemTraySupported() {
		return systemTraySupported;
	}

	// The constructor update the gui flag to be used by controller
	public MainGUIThread() {
		if (!SystemTray.isSupported()) {
           systemTraySupported=false; 
        }		
	}
	
	@Override
	public void run() {
		createTrayPopup();
		createTrayIcons();
		checkState();
	}

	
	/**
	 * Create the 3 different tray icons.
	 */
	private void createTrayIcons() {
		
		ImageIcon redIcon = new ImageIcon(resourcePath + PropertiesBean.getProperty(PropertiesBean.ICON_RED));
		ImageIcon greenIcon = new ImageIcon(resourcePath + PropertiesBean.getProperty(PropertiesBean.ICON_GREEN));
		ImageIcon yellowIcon = new ImageIcon(resourcePath + PropertiesBean.getProperty(PropertiesBean.ICON_YELLOW));
		
        trayIconRed = new TrayIcon(redIcon.getImage(), "Dimes Agent");
        trayIconGreen = new TrayIcon(greenIcon.getImage(), "Dimes Agent");
        trayIconYellow = new TrayIcon(yellowIcon.getImage(), "Dimes Agent");
		
		MouseListener trayIconListener = new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (1 == e.getButton()) {
					((TrayIcon) e.getSource()).displayMessage(
							null,
							"Measurements: "
									+ StateHolder.getNumberOfMeaurements(),
							MessageType.INFO);
				}

			}
		};
		
		trayIconRed.addMouseListener(trayIconListener);
		trayIconGreen.addMouseListener(trayIconListener);
		trayIconYellow.addMouseListener(trayIconListener);
		  
	}

	/**
	 * Created the popup menu of the icon.
	 */
	private void createTrayPopup() {
		
	    // Create a popup menu components
        MenuItem About = new MenuItem("About");
        MenuItem statusItem = new MenuItem("Status");
        MenuItem exitItem = new MenuItem("Exit");
        
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	StateHolder.setShutdownRequested(true);
            	Controller.getInstance().shutdown();
            }
        });
        
        statusItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "Current Measurement: "+StateHolder.getLastMeasurement()+"\nMeasurements taken: "+StateHolder.getNumberOfMeaurements());
            }
        });
        
        About.addActionListener(new ActionListener() {
        	JLabel htmlText = new JLabel(getAboutFileText());
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, htmlText);
            }
        });
    
        //Add components to popup menu
        popup.add(About);
        popup.add(statusItem);
        popup.addSeparator();

        popup.add(exitItem);

	}
	
	/**
	 * Checking the GUI state against StateHolder. According to state sends the relevant icon to setIcon.
	 *     
	 */
    public void checkState(){
    	if(StateHolder.isShutdownRequested()) setIcon(null);
    	if (StateHolder.isConenctedAgent())	setIcon(trayIconGreen);
    	else if(StateHolder.isConnectedGUI()) setIcon(trayIconYellow);
    	else setIcon(trayIconRed);
    }
    
    /**
     * Called by checkState for changing the icon in the systray.
     * Change will be made only if the requested icon is different from the current one
     * to avoid unnecessary icon change that created blinks in the task bar.
     * 
     * @param chosenTrayIcon The trayicon to add to systemtray
     */
    private void setIcon(TrayIcon chosenTrayIcon){
    	
    	SystemTray tray = SystemTray.getSystemTray();
    	
    	if(currentTrayIcon != null) {
    		if(currentTrayIcon == chosenTrayIcon) {
    			return;
    		}
    		else {
    			currentTrayIcon.setPopupMenu(null);
        		tray.remove(currentTrayIcon);
    		}
    	}
    	
    	if(chosenTrayIcon != null) {
    		chosenTrayIcon.setPopupMenu(popup);
            try {
    			tray.add(chosenTrayIcon);
    		} catch (AWTException e) {
    			throw new RuntimeException("TrayIcon can not be added to systemtray", e);
    		}    	
    	}
    	
    	currentTrayIcon = chosenTrayIcon;	
    }
    
    
    /**
     * Reading the about.html file for being added to the about popup label
     */
    private String getAboutFileText(){
    	StringBuilder sb = new StringBuilder();
    	BufferedReader br = null;
    	try {
			br = new BufferedReader(new FileReader(resourcePath + PropertiesBean.getProperty(PropertiesBean.ABOUT_FILENAME)));
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            line = br.readLine();
	        }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
	        try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
    	return sb.toString();   	
    }
}
