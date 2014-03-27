package dimes.AgentGuiComm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;


import org.w3c.dom.Element;


import dimes.AgentGuiComm.util.ExternalClient;
import dimes.AgentGuiComm.util.MessageTypes;
import dimes.util.XMLUtil;

/** A thread that handles the actual communication with the GUI/External program
 * serves as a psudo-http setver, exchanging XML messages.
 * 
 * @author BoazH
 * @since 0.6.0
 *
 */
public class GUIConnectorThread implements Runnable {

	private ServerSocket comm;
	private Socket lComm;
	private String xml;
	private GUICommunicator communicator;
	private boolean die=false;
	private GUIConnectorBean bean;
	

	@SuppressWarnings("unused")
	private String GUIReply; //not used for anything right now. Left for future GUI/Agent handshaking
	
	/**public constructor for GUIConnectorThread
	 * 
	 * @param serverSocket ServerSocket to listen to (from ConenctorBean)
	 * @param connectorBean The connector bean that invokes the thread
	 */
	public GUIConnectorThread(ServerSocket serverSocket, GUIConnectorBean connectorBean){
		bean=connectorBean;
		comm=serverSocket;
		xml= "<XML><DIMES version=\""+bean.getAgentVersion()/*"TEST"*/+"\" /></XML>"; //
		communicator = GUICommunicator.getInstance(); //We will need this to be able to send commands to Agent
	}
	
	@Override
	public void run() {
		PrintStream outStream=null;
		Element current=null;
		
		BufferedReader br=null;
//		int count=0; //Yes, we initialize this twice. Once for the compiler, and once when we restart
		String incoming;
		restart:
		
			while(!die){
				try {
//					count=0;
					lComm=comm.accept(); //Blocks and waits for incoming connection
					outStream = new PrintStream(lComm.getOutputStream(),true,"UTF-8");
					outStream.println(xml); //As soon as the socket is connected, send out the greeting
				
					br = new BufferedReader(new InputStreamReader(lComm.getInputStream()));
					incoming = br.readLine();
					
					if(XMLUtil.isTextXML(incoming))
						current = XMLUtil.getRootElement(incoming);// This will throw a Null Pointer Exception if content is not XML
					
					if( null == current || !(current.getTagName().equals(MessageTypes.SEND_TYPE_DIMES))){ //This is expecting "<DIMES />" not "<XML><DIMES /></XML>						
							outStream.println("ERROR: Unrecognized input");
							continue;
						}else shakeHands(current, outStream);
//
//					if (!current.hasAttribute("CLIENT"))
//						shakeHands(current, outStream);
//					else
//						handleMessage(current, outStream, current.getAttribute("CLIENT"));
//					bean.setConnectionTimer();
//					bean.setConnected(true);
					
				} catch (IOException e) {					
						e.printStackTrace();
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
				
			}  //End While 1
	
/*			while(!die){
				
				try {
					
//					current = inStream.readElement();
					incoming = br.readLine();
					
					if(incoming.equals("BYE")){ //Simple disconnect mechanizem. May go away in final version
						outStream.println("Goodbye");
						bean.setConnected(false);
						outStream.close();
						continue restart;
					}
					
					if(XMLUtil.isTextXML(incoming)){

					}else{
						outStream.println("ERROR: Unrecognized input: "+incoming);
					}
					
				}catch (NullPointerException npe){ //Handle unexpected disconnect
					npe.printStackTrace();
					if(3<=count++) {
						bean.setConnected(false);
						continue restart;
					}
					continue;
				}catch (java.net.SocketException se){ //Handle unexpected disconnect
					System.out.println("Connection to GUI reset"); //debug
					bean.setConnected(false);
					continue restart;
				}
				catch (IOException e) {
					
					e.printStackTrace();
				}
				
				
			}//End While 2
*/			
		

	}//End Run


	
	private void shakeHands(Element current, PrintStream outStream){
		ExternalClient client=null;
//		int port = lComm.getPort();
		if(current.hasAttributes() || current.hasChildNodes()){
			client = handleExtraInfo(current, outStream);
			}else client = new ExternalClient(lComm); //A client object lets us keep track of who we are talkign to					
		bean.addClient(client, client.getID());	
		outStream.println("<DIMES CLIENT=\""+client.getID()+"\" "+client.showMasks() +"/>"); //return client number back to client
//		outStream.close();
	}
	
	private void handleMessage(Element current, PrintStream outStream, String ID){
		
//			current = XMLUtil.getRootElement(incoming);
//			String type = current.getTagName().toUpperCase();
//			if(MessageTypes.lookup.get(type)==MessageTypes.RECEIVE_DIMES) bean.setConnectionTimer();
//			else if (MessageTypes.lookup.get(type)==MessageTypes.RECEIVE_SHOW) client.handleShowRequest(current);
//			else communicator.dispatch(current);
			int id = -1;
			try{
				id = Integer.parseInt(ID);
			}catch(NumberFormatException nfe){nfe.printStackTrace();}
			
			if(id<0){
				outStream.println("INVALID ID");
				outStream.close();
				return;
			}
			
			bean.dispatch(current, outStream, id);
	}
	
	public void stopThread(){
		die=true;
	}
	

	private ExternalClient handleExtraInfo(Element current, PrintStream outStream){
		int results=0;
		int scripts=0;
		int messages=0;
//		This can probably be done nicer. But it'll work
		if(current.hasChildNodes()){

			Element e = XMLUtil.getChildElementByName(current, MessageTypes.GET_SHOW_RESULTS);
			if (null!=e)
			results = MessageTypes.lookup.get(e.getTextContent());
			
			e = XMLUtil.getChildElementByName(current, MessageTypes.GET_SHOW_SCRIPTS);
			if (null!=e)
			scripts = MessageTypes.lookup.get(e.getTextContent());
			
			 e = XMLUtil.getChildElementByName(current, MessageTypes.GET_SHOW_MESSAGES);
			if (null!=e)
			messages = MessageTypes.lookup.get(e.getTextContent());
		}
		else{
			String s = current.getAttribute(MessageTypes.GET_SHOW_RESULTS);
			results = s.equals("")?0:MessageTypes.lookup.get(s);
			
			s = current.getAttribute(MessageTypes.GET_SHOW_SCRIPTS);
			scripts = s.equals("")?0:MessageTypes.lookup.get(s);
			
			s = current.getAttribute(MessageTypes.GET_SHOW_MESSAGES);
			messages = s.equals("")?0:MessageTypes.lookup.get(s);
		}
		
		return new ExternalClient(lComm,results,scripts,messages);
	}
}
