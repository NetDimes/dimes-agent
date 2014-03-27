package dimes.AgentGuiComm.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;

import org.w3c.dom.Element;

import dimes.AgentGuiComm.util.MessageTypes;
import dimes.util.XMLUtil;
import dimes.util.properties.*;

/**
 * @author Boaz
 * 
 * This thread serves as a server for incoming clients. It handles the 
 * initial contact and handshake, and then transfers control of the communications
 * with a specific client to a Communications Object
 *
 */
public class CommunicationsThread implements Runnable {

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private final String XMLGreating;
	private static CommunicationsThread me=null;
	private String AgentVersion = "0.6.0"; //Default in case we can't find the real one 
	private PrintStream outStream=null;
	private Element current=null;
	private BufferedReader br=null;
	private boolean die=false;
	
	public static CommunicationsThread getInstance(int port) throws IOException{
		if(null==me){
			me = new CommunicationsThread();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

			serverSocketChannel.socket().bind(new InetSocketAddress(port));

//			me.serverSocket = new ServerSocket(port);
			me.serverSocket = serverSocketChannel.socket();
		}
		return me;
	}
	
	private CommunicationsThread(){
		XMLGreating =" <DIMES><VERSION>"+getAgentVersion()+"</VERSION></DIMES>" ;		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() { //Runs a loop for accepting incoming connections from clients
		String incoming;
		restart:
		while(!die){
			try {
				clientSocket=serverSocket.accept();  //Will block and wait for incoming connection
				outStream = new PrintStream(clientSocket.getOutputStream(),true,"UTF-8");
				outStream.println(XMLGreating); //As soon as the socket is connected, send out the greeting
				InputStream inSt = clientSocket.getInputStream();
				br = new BufferedReader(new InputStreamReader(inSt));//clientSocket.getInputStream()));
				incoming = br.readLine();
				
				if(XMLUtil.isTextXML(incoming))
					current = XMLUtil.getRootElement(incoming);// This will throw a Null Pointer Exception if content is not XML
				
				if( null == current || !(current.getTagName().equals(MessageTypes.SEND_TYPE_DIMES))){ //This is expecting "<DIMES />" not "<XML><DIMES /></XML>						
					outStream.println("ERROR: Unrecognized input");
					continue;
				}else shakeHands();
				
			} catch (IOException e) {					
				e.printStackTrace();
				outStream.close();
				continue restart;
			} catch (NullPointerException npe){
				npe.printStackTrace(); //debug
				for(int i=0;i<npe.getStackTrace().length;i++){  //If XMLUtil throw an exception, the input was not XML
					if(npe.getStackTrace()[i].getClassName().equals("dimes.util.XMLUtil"));
					{
						outStream.println("ERROR: Unrecognized input");
						break;
					}
				}					
				outStream.close();
				continue restart;
			}
		}
		
	}

	/**Establish permanent connection with client. Handles case were client is a new client
	 * or when a client is an existing client who lost connection. 
	 * 
	 */
	private void shakeHands() {
		ClientsBean.checkAndStartSelectionThread();
		if (!"".equals(current.getAttribute("ID"))&& !"".equals(current.getAttribute("KEY")))
			{
			if (! ClientsBean.getExistingCommunicationsObject(Integer.parseInt(current.getAttribute("ID")),Integer.parseInt(current.getAttribute("KEY")) ,clientSocket))
				try{
				outStream.println("ERROR: Could not create communications object for this client");
				}catch(Exception e){
					System.out.println("Could not create Communications object for this client");
					e.printStackTrace();
				}
			}
		else if (! ClientsBean.getNewCommunicationsObject(clientSocket))
			try{
			outStream.println("ERROR: Could not create communications object for this client");
			}catch(Exception e){
				System.out.println("Could not create Communications object for this client");
				e.printStackTrace();
			}
	}

	/**Gets the current version of the Agent from the properties file
	 * @return
	 */
	private String getAgentVersion(){		
			try {
				AgentVersion = PropertiesBean.getProperty(PropertiesNames.AGENT_VERSION);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		return AgentVersion;
	}
	
	public void stopThread(){
		try {
			serverSocket.close();
		} catch (IOException e) {
			
		}
		die=true;
	}
}
