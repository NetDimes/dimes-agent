package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.w3c.dom.Element;

import util.PropertiesBean;

import agentGuiComm.comm.Client;
import agentGuiComm.comm.Receiver;
import agentGuiComm.comm.Sender;
import agentGuiComm.dataStructures.*;
import agentGuiComm.util.XMLUtil;
import gui.*;

/** Controller is the controller class in this MVC application, its job is to monitor the communication
	 * status, and through it the status of the Agent and update the StateHolder (Model). The GUI (View) then 
	 * uses the StateHolder to display the information. In the other direction, the Controller passes commands
	 * from the User to the Agent via the comm package
 * @author Boaz2
 *
 */
public class Controller implements ItemReceiverListener, Runnable{

	Lock runLock = new ReentrantLock();
	private Receiver dimesReceiver;
	private Sender dimesSender = new Sender();
	private Client dimesClient;		
	private MainGUIThread guiThread;
	private static Controller me=null;
	private Timer connectionTimer; 
	
	public static void main(String[] args) {
		PropertiesBean.init(args[0]);
		new Thread(Controller.getInstance()).start();
	}
	
	/**Private Constructor. Sets up new threads for communicator and GUI and then locks until
	 * program ends.
	 * 
	 */
	private Controller(){
		//set up GUI thread.
		guiThread = new MainGUIThread();
		if(guiThread.isSystemTraySupported()) SwingUtilities.invokeLater(guiThread);
		
		//set up communications thread.
		dimesClient = new Client(0, Integer.parseInt(PropertiesBean.getProperty(PropertiesBean.PORT)));
		StateHolder.setConnectedGUI(connectToAgent());
		guiThread.checkState();
		
		connectionTimer = new Timer(30000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				StateHolder.setConnectedGUI(connectToAgent());
				guiThread.checkState();
			}
		});
		connectionTimer.start();
		
		//lock until program ends
		runLock.lock();
	}
	
	public static Controller getInstance(){
		return me==null?new Controller():me;
	}
	
	
	/*
	 * Connecting to Agent service, getting receiver with notifiers the object can register on. 
	 */
	private boolean connectToAgent(){
		if(null==dimesReceiver)
		try{
			dimesReceiver = dimesClient.connect();
			dimesReceiver.addLogItemListener(this);
			dimesReceiver.addResultItemListener(this);
			dimesReceiver.addMessageItemListener(this);
			dimesReceiver.addstatusItemListener(this);
			dimesReceiver.setStatusReporting(true);
			dimesSender.setClient(dimesClient);
			if(!dimesClient.getConnected()) return false;
			dimesClient.start();
			//clientThread.start();
			return true;
		}catch(Exception e){
			return false;
		}else {
			if(!dimesClient.getConnected()){
				System.out.println("dimeClient not connected, connecting");
				dimesClient.connect();
				System.out.println("dimesclient connect resutl: "+dimesClient.getConnected());
				if(dimesClient.getConnected()){ 
					dimesClient.getConnected();
					dimesClient.start();
					}
				System.out.println("Thread start requested");
			}
		}return dimesClient.getConnected();
	}
	
	/**Handles an incoming message from Agent and updates the StateHolder accordingly
	 * 
	 * @param arg0
	 */
	@Override
	public void ItemReceived(ReceivedItem arg0) {
//		System.out.println(arg0.getRawMessage());
		if(arg0.getType().equals("STATUS"))
			updateStatus((StatusReceivedItem)arg0);
		if(arg0.getType().equals("RESULT"))
			updateResultStatus((ResultReceivedItem)arg0);
	}

	/**Handles a result message from Agent by updating the number of successful
	 * measurements if the measurement was successful
	 * 
	 * @param arg0
	 */
	private void updateResultStatus(ResultReceivedItem arg0) {
		Element rootelm = XMLUtil.getRootElement(arg0.getRawMessage());
		Element opResult = XMLUtil.getChildElementByName(rootelm, "OperationResult");
		Element success = XMLUtil.getChildElementByName(opResult, "Success");
		if (Boolean.parseBoolean(success.getTextContent())) StateHolder.incerementResultCount();
		
	}
	
	/**Handles a status message from Agent. Updates state with current connection status and
	 *  current measurement info 
	 * @param arg0
	 */
	private void updateStatus(StatusReceivedItem arg0) {
		StateHolder.setConenctedAgent(arg0.isLastConnectionSuccess());
		StateHolder.setLastMeasurement(arg0.getCurrentMeasurement()+"\n ExID: "+arg0.getCurrentExId());	
		guiThread.checkState();
	}	

	/**Releases the lock and notifies the the thread, causing the program to exit.
	 * 
	 */
	public void shutdown(){
		connectionTimer.stop();
		dimesClient.hangup();
		StateHolder.setShutdownRequested(true);
		guiThread.checkState();
		System.exit(0);
	}

	@Override
	public void run() {
		synchronized (runLock) {
			while (true){		
				if (runLock.tryLock()) System.exit(0);
				try {
					runLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}	
		}
	}
}
