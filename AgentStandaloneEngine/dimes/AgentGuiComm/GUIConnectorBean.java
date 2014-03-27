package dimes.AgentGuiComm;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


//import dimes.comm2gui.xml.XMLEncoder;
import dimes.AgentGuiComm.util.ExternalClient;
//import dimes.AgentGuiComm.util.Message;
import dimes.AgentGuiComm.util.MessageTypes;
import dimes.AgentGuiComm.util.DimesMessageBuffer;
import dimes.AgentGuiComm.Event.MessageEventSource;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

public class GUIConnectorBean extends MessageEventSource {

//	private static final int BYTEBUFFER_ALLOCATE=512;
//	private static final int SOCKET_TIMEOUT=30;
	private int connectPort=33334;
	private final String DIMES_ENGINE_ID = "DIMES";
	
	private boolean secure = false; // Flag for marking encrypted or password connections between Agent and GUI. Not implemented in 0.6.0, but may be needed for web GUI.
	private short objectCount=0;
	private String AgentVersion;
	private ServerSocket commSocket;
	private PrintStream  outStream;
	private InetAddress localHost;
	private Logger logger;
	private Thread ConnectorThread;
//	private Calendar cal = Calendar.getInstance();
	private Date ConnectionTimer;
	private static boolean init=false;
	private static GUIConnectorBean me=null;
	private boolean Connected = false;
	private boolean listenEnabled=false;
	private DimesMessageBuffer msgBuffer= new DimesMessageBuffer();
	private ArrayList<ExternalClient> clientsa = new ArrayList<ExternalClient>(2);
	private HashMap<Integer, ExternalClient> clients = new HashMap<Integer, ExternalClient>();
	
	/** Returns a single copy of the GUIConnectorBean, initilizes socket if 
	 * needed
	 * 
	 * @return GUIConnectorBean
	 */
	public static GUIConnectorBean getInstance() throws NullPointerException{
		if (null==me) {
			me=new GUIConnectorBean();
		}
		if (!GUIConnectorBean.init) me.initSocket(); //If the bean was not created properly, this can throw an exception.
		return me;
	}
	
	
	/**Private Constructor
	 * 
	 */
	private GUIConnectorBean(){
		logger=Loggers.getLogger(GUIConnectorBean.class);
		
		try {
			localHost = InetAddress.getLocalHost();
		} catch (UnknownHostException ukhe) {
			logger.severe("Could not find localhost address. Using 127.0.0.1\n The GUI may not be able to talk to the Agent service");
			try {
				localHost =  InetAddress.getByAddress(new byte[]{(byte)127,(byte)0,(byte)0,(byte)1});
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		try{
			AgentVersion=PropertiesBean.getProperty(PropertiesNames.AGENT_VERSION);
			connectPort=Integer.parseInt(PropertiesBean.getProperty(PropertiesNames.COMM_COMMPORT));
			listenEnabled = Boolean.parseBoolean(PropertiesBean.getProperty(PropertiesNames.COMM_COMMPORT_ENABLED));
		} catch (NoSuchPropertyException nspe) {
			AgentVersion="0.6.0";
			connectPort=33334;
			//ListenEnabled defaults to false. So we don't init a default
		} catch (NumberFormatException nfe){
			connectPort=33334;
		} 
	}
	
	/**Initiate the connection to the socket, and starts listening for the GUI.
	 *  
	 * In future versions, implementing a loop here to scan for an open socket would
	 * be good. However, in this version (0.6.0) we're going to stay with one port(33333) 
	 * so as to avoid a situation where Agent 0.5 and 0.6 can work side by side.
	 * 
	 * @return true if connection was successful, false otherwise. 
	 */
	private boolean initSocket(){		
			try {

				commSocket=new ServerSocket(connectPort);
				GUIConnectorBean.init=true; //if we got this far it means we successfuly bound the port
				if(listenEnabled){
					new Thread(new GUIConnectorThread(commSocket, me)).start();
				}
				return true; 

			}catch (java.net.BindException be){
				logger.severe("Could not connect to localhost port ");
				return false;
			}catch (IOException e) {
				logger.severe("An IO Exception had occured when attepting to connect to the Localhost Socket.");
				e.printStackTrace();
				return false;
			} 			
	}
	
	
	/**Adds a new client to the bean. Clients are created by the GUIConnectorThread
	 * during handshake and maintain individual masks for type of info they want
	 *  
	 * @param newClient
	 * @return ID of client in the bean
	 */
/*	int addClient(ExternalClient newClient){
		if (!clients.contains(newClient))
			clients.add(newClient);
		 return clients.indexOf(newClient);
	}*/
	
	public boolean clientExists(int port){
			try{
				return clients.get(port).isAlive();
			}catch(NullPointerException npe){ //if get throws exception, Client is not alive
				return false;
			}
		}
	
	public void addClient(ExternalClient newClient, int port){
		clients.put(port, newClient);
	}
	
	public void setConnectionTimer(){
//		ConnectionTimer = cal.getTime();
		System.out.println("CLOCK TICK");
	}
	
	public String getAgentVersion(){
		return AgentVersion;
	}
	
	public boolean getConnected(){
		return Connected;
	}
	
	public static boolean getInit(){
		return init;
	}
	
	
	void setConnected(Boolean state){
		Connected=state;
//		if(state && 0<msgBuffer.getSize())sendBuffer();
	}
	
	
/*	*//**Sends a Message Object over the OutputStream
	 * 
	 * @param msg Message 
	 * @return true if message was send, false if send failed three times. 
	 *//*
	public boolean send(Message msg){
		if(!listenEnabled) return true; //If we are not listening for a GUI, we don't need to save messages for it
		if (false==getConnected()){
			msgBuffer.push(msg);
			return true;
		}
		for (int i=0;i<3;i++){
			try {
				outStream.writeObject(msg);
				objectCount++;
				System.out.println(objectCount+"\n");
				if (32<objectCount){
					Thread.sleep(500); //Give object time to be read by GUI
					outStream.reset();
					objectCount=0;
				}
				return true;
			} catch (IOException e) {
				logger.warning("send failed attempt "+i);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}*/
	
/*	*//**Sends a properly formated XML Element
	 * 
	 * @param msg Message 
	 * @return true if message was send, false if send failed three times. 
	 *//*
	public boolean send(String msg){
		if(!listenEnabled) return true; //If we are not listening for a GUI, we don't need to save messages for it
		if (false==getConnected()){
//			msgBuffer.push(msg);
			return true;
		}
		for (int i=0;i<3;i++){
//			try {
				outStream.println("<XML>"+msg+"</XML");
				if(!outStream.checkError());
				return true;
//				objectCount++;
//				System.out.println(objectCount+"\n");
//				if (32<objectCount){
//					Thread.sleep(500); //Give object time to be read by GUI
//					outStream.reset();
//					objectCount=0;
//				}
				
//			} catch (IOException e) {
//				logger.warning("send failed attempt "+i);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		return false;
	}
	*/

	public boolean send(String msg, int type, int sourceOrSubtype){
		boolean ret=true;
		for(int i: clients.keySet())
			if (!clients.get(i).handleMessage(msg, type, sourceOrSubtype))ret=false;
		return ret;
	}
	
	public boolean send(String msg, int type, int sourceOrSubtype, int id){		
		return true!=Connected?true:clients.get(id).handleMessage(msg, type, sourceOrSubtype);
	}


	public void dispatch(Element current, PrintStream outStream2, int id) {
		// TODO Auto-generated method stub
		
	}
	
/*	private void sendBuffer(){
			try {				
				for (int i=msgBuffer.getSize();i>0;i--) {
					send(msgBuffer.pop());
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}*/
	


//class GUIConnectorThread extends Thread{
//	
//	private ServerSocket comm;
//	private Socket lComm;
//	private GUIConnectorBean bean;
//	private String xml;
//	private String GUIReply; //not used for anything right now. Left for future GUI/Agent handshaking
//	private ObjectOutputStream oOStream;
//	private ObjectInputStream iOStream;
//	private Message Current;
//	private GUICommunicator Communicator;
//	
//	public GUIConnectorThread(ServerSocket in, GUIConnectorBean b){
//		bean=b;
//		comm=in;
//		xml= "<XML><DIMES version=\""+bean.getAgentVersion()+"\" /></XML>";
//	}
//	
//	/**
//	 * Two while loops. The first one handles the GUI Handshake. The second one 
//	 * takes over after handshake is done and handles messages received from GUI
//	 */
//	public void run(){
//		restart:
//		while(true){
//			try {
//				
//				lComm=comm.accept(); //Wait for connection from GUI
//				oOStream = new ObjectOutputStream(lComm.getOutputStream());
//				oOStream.flush();
//				Message msg = new Message(MessageTypes.DIMESACK, -1, "DIMES", new Object[]{bean.getAgentVersion()});
//				oOStream.writeObject(msg);
//				
//				InputStream iStream = lComm.getInputStream();
//				iOStream = new ObjectInputStream(iStream);
//				
//				Current = (Message)iOStream.readObject();
//				if (MessageTypes.DIMESACK==Current.getType());
//				else continue;
////				bean.setOutStream(oOStream);
//				Communicator = GUICommunicator.getInstance();
//				bean.setConnectionTimer();
//				bean.setConnected(true);
//				//break;
//				
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//		
//		while (true){
//				try {
//					Current=(Message)iOStream.readObject();
//					if (MessageTypes.DIMESACK==Current.getType()){
//						bean.setConnectionTimer();
//					}
//					else Communicator.dispatch(Current);
//					
//				} catch (java.net.SocketException se){
//					System.out.println("Connection to GUI reset"); //debug
//					bean.setConnected(false);
//					continue restart;
//				}catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (ClassNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} 
//			}
//		}
//	}
//	
//}
}