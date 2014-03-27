package dimes.AgentGuiComm.comm;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.logging.Level;
import java.nio.CharBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import dimes.AgentGuiComm.util.MessageTypes;
import dimes.AgentGuiComm.comm.channels.SelectorThread;

public class ClientCommunicationsObject {

	private Socket clientSocket;
	private int resultsMask;
	private int logMask;
	private int ID=-1; //The ID is the port number where the conversation originated. 
	private int fingerPrint = -1; //If the port changes, the fingerprint is used to verify the source
	private boolean noSystemMessages=false;
	private AbstractSelectableChannel commChannel;
	private CharBuffer transmittionBuffer = CharBuffer.allocate(50240);
	private SelectionKey selectionKey=null;
	private boolean active=true;
	private CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
	private static int i=1;//debug
	private LinkedList<String> MyExperiments=null;

	
	
	/**create a new ClientCommObject with the socket where the client is 
	 * connected (received from CommunicationsThread) and the default mask
	 * for results and log messages (stored in ClientsBean). All other messages
	 * are assumed to be forwarded (DIMES, Property, Script, update)
	 * 
	 * @param clientSocket
	 * @param resultsMask ALL/NONE/MINE/SERVER
	 * @param logMask INFO/WARNING/ERROR
	 */
	ClientCommunicationsObject(Socket clientSocket,int resultsMask, int logMask, int fingerPrint ){
		this.clientSocket=clientSocket;
		this.resultsMask = resultsMask;
		this.logMask = logMask;
		this.ID=clientSocket.getPort();
		this.fingerPrint=fingerPrint;
	}
	
	/** A constructor to allow the creation of internal (piped) clients. Used for
	 * creating clients for the results and log savers.
	 *  
	 * @param clientPipe
	 * @param resultsMask should be ALL for results, NONE for Logger
	 * @param logMask Should be min log level for logger, MAX_INT for result
	 * @param noSysMsg true if results or log saver
	 */
	public ClientCommunicationsObject(Pipe clientPipe, int resultsMask, int logMask, boolean noSysMsg) {
		commChannel = clientPipe.sink();
		this.resultsMask = resultsMask;
		this.logMask = logMask;
		this.noSystemMessages=noSysMsg;
		this.ID=(resultsMask+logMask)>0?-1*(resultsMask+logMask):resultsMask+logMask;
	}
	
	/** Initiate contact with the incoming client. Set incoming and outgoing
	 * streams, and transmit the default Message Mask
	 * @return
	 */
	boolean init(){
		if(ID>-1)
		try{
			commChannel = clientSocket.getChannel();
			send(MessageTypes.SEND_TYPE_DIMES,"","<CLIENTID>"+ID+"</CLIENTID><KEY>"+fingerPrint+"</KEY>");
			selectionKey= SelectorThread.registerChannel((SocketChannel)commChannel);
			if(null==selectionKey) return false;
			return true;
		}catch (IOException ioe){
			ioe.printStackTrace(); //debug
			return false;
		}
		if (null!=commChannel) return true; //commChannel was initlized in the constructor (Internal Client)
		return false;
	}
	

	/**Determins if a message qualifies for sending and wraps it in 
	 * approriate XML
	 * 
	 * For type RESULTS subtype should be the source port of the entitiy
	 * that originated the request. 0 for server, port number for client. 
	 * IE, a client can determine if a result qualifies under mask MINE if
	 * the subtype is equal to its own port number
	 * 
	 * For type log, the sutype should be a String of type "finest","finer"
	 * ,"fine", "config", "info", "warning","severe"
	 * 
	 * @param type
	 * @param subtype ignored unless type is RESULTS or LOG
	 * @param message
	 * @throws IOException 
	 */
	boolean send(String type,String subtype, String message) throws IOException{
		if (!active)return false;
		System.out.println("Send called. My Logmask:"+logMask+" My ResMask:"+resultsMask+" my port(id):"+ID);
		String outgoingMessage=null;
		if(type.equals(MessageTypes.SEND_TYPE_RESULTS)){//&& resultsMask!=MessageTypes.MASK_NONE){  //Results none, so we ignoe
			System.out.println("message port:"+Integer.parseInt(subtype));
			switch (resultsMask){
			case MessageTypes.MASK_MINE:
				if (!(Integer.parseInt(subtype)==ID)) break; //results mine so we check if port match
				else outgoingMessage=generateMessage(type, message);
			case MessageTypes.MASK_SERVER:
				if (!(Integer.parseInt(subtype)==0)) break; //results server, so we check if port is zero
				else outgoingMessage=generateMessage(type, message);
			case MessageTypes.MASK_NONE: 
				System.out.println("Skipping message for client"+ID);
				break;
			default:
				outgoingMessage=generateMessage(type, message); //results all, or qualify above, we send
			}
		}else if( type.equals(MessageTypes.SEND_TYPE_LOG)){
			System.out.println("Message loglevel:"+Level.parse(subtype).intValue());
			if(Level.parse(subtype).intValue()>=logMask)
				outgoingMessage = "<LOG>"+generateMessage(subtype.toUpperCase(), message)+"</LOG>"; //we send the SUBTYPE (if INFO, etc)
		}else{
			if (!noSystemMessages)outgoingMessage=generateMessage(type, message); //other types of messages are assumes to go on by default, except for internal clients
		}
		
		if (null!=outgoingMessage)transmit(outgoingMessage);			
		return true;
	}
	
	/**Hangs up this client. Closes the channel
	 * and de-registers it from the selector
	 * 
	 * @throws IOException
	 */
	void hangup() throws IOException{
		send(MessageTypes.SEND_TYPE_DIMES,"","GOODBYE");
		kill();
	}
	
	private void kill() throws IOException{
		System.err.println("Connection lost - killing socket");
		active=false;		
		SelectorThread.deregisterChannel(clientSocket.getChannel());
		selectionKey.cancel();
		commChannel.close();
		clientSocket.close();
	}
	
	private void transmit(String message) throws IOException{

		if(transmittionBuffer.capacity()<(message.length()+10))
			transmittionBuffer = CharBuffer.allocate(message.length()+10);
		transmittionBuffer.clear();
		transmittionBuffer.put(message);
		transmittionBuffer.put("\n");//debug
		transmittionBuffer.flip();
		i++; //debug
		try{
		while(transmittionBuffer.hasRemaining())
			if (commChannel instanceof SocketChannel)
				((SocketChannel) commChannel).write(encoder.encode(transmittionBuffer));
/*			{
				PrintStream outStream = new PrintStream(((SocketChannel) commChannel).socket().getOutputStream(),true,"UTF-8");
				outStream.println(message);
				transmittionBuffer.clear();
			}*/
			else ((Pipe.SinkChannel)commChannel).write(encoder.encode(transmittionBuffer));
			System.out.println("Sent to client "+ID+" Message:\n----------\n"+message+"\n------------------\n");
		}catch(IOException ioe){
			ioe.printStackTrace();
			kill();
		}
	}
	
	private String generateMessage(String type, String content){
		return "<"+type+">" + content + "</" + type+">";
	}
	
	/**return the ID number of this client, which is the port 
	 * number for external clients, and a negative number for internal 
	 * clients
	 * 
	 * @return
	 */
	public int getID(){
		return ID;
	}
	
	/**Sets the log mask and results mask for this client.
	 * Masks are found in MessageTypes
	 * input -1 for a parameter to preserve existing value
	 * 
	 * @param logMask 
	 * @param resultsMask
	 */
	public void setMask(int logMask, int resultsMask){
		if((0<logMask)) this.logMask=logMask;
		if((0<=resultsMask)) this.resultsMask = resultsMask;
		System.out.println("Set mask called on client "+ID+" Message log,res:"+logMask+","+resultsMask+" Current log,res:"+this.logMask+","+this.resultsMask);
	}
	
	/**Sets the active status for this clientObject. If object is not 
	 * active, no messages will be sent to client (client is paused)
	 * @param active
	 */
	public void setActive(boolean active){
		this.active=active;
	}
	
	/**
	 * @return client is active or not 
	 */
	public boolean getActive(){
		return active;
	}
	
	public void listenForScript(String exID){
		if(null==MyExperiments) MyExperiments= new LinkedList<String>();
		MyExperiments.add(exID);
	}

	public int getFingerPrint() {
		return fingerPrint;
	}

	public void setID(int id) {
		ID = id;
	}

	public void setCommChannel(AbstractSelectableChannel commChannel) {
		this.commChannel = commChannel;
	}

	public void setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}
}
