package dimes.AgentGuiComm.comm;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Random;

import dimes.AgentGuiComm.comm.channels.SelectorThread;

import dimes.AgentGuiComm.util.MessageTypes;

/**
 * @author Boaz
 * 
 * Allocates and maintains a list of client CommObjects
 * Maintains a reference to the client Comm thread
 * Maintains the default Message Mask
 * 
 *
 */
public abstract class ClientsBean {
	
	private static LinkedList<ClientCommunicationsObject> commObjectsList = new LinkedList<ClientCommunicationsObject>();
	private static SelectorThread selectionThread=null;
	private static Random randomGenrator = new Random();
	
	/** creates a new ClientCommunications object 
	 * sets the default message mask to: all results types, and log level info or above
	 * @param clientSocket
	 * @return true if 
	 */
	static boolean getNewCommunicationsObject(Socket clientSocket){
		ClientCommunicationsObject obj = new ClientCommunicationsObject(clientSocket, MessageTypes.MASK_ALL, MessageTypes.LOGGER_LEVEL_FINE, randomGenrator.nextInt());
		boolean success = obj.init();
		if (success) commObjectsList.add(obj);
		return success;
	}
	
	static boolean getExistingCommunicationsObject(int ID, int FingerPrint, Socket clientSocket){
		ClientCommunicationsObject client = findClient(ID);
		if (!(null==client)){
			if(client.getFingerPrint()==FingerPrint){
				client.setID(clientSocket.getPort());
				client.init();
				return true;
			}
		}
		return false;
	}
	
	/**Sends a message to all clients currently registered
	 * Clients will individually choose whether to receive message 
	 * based on their own message mask
	 * 
	 * @param type MessageTypes SEND_TYPE type
	 * @param subtype Only applicable for RESULTS and LOG, see ClientCommuncationsObject.send
	 * @param message raw message. Should be properly XML formatted
	 */
	public static synchronized void send(String type, String subtype, String message){
		ClientCommunicationsObject obj = null;
		LinkedList<ClientCommunicationsObject> toBeRemoved=null;
		try {
			for(ClientCommunicationsObject o: commObjectsList){
				obj=o;
					if (!obj.send(type, subtype, message)) //if Send didn't work check if object is still active
					{
						if(!obj.getActive()){
						if(null==toBeRemoved) toBeRemoved= new LinkedList<ClientCommunicationsObject>();
						toBeRemoved.add(obj); //if not active, remove from list
						//if(!obj.getActive()) commObjectsList.remove(obj); //Can't remove from list while we're still iterating through it. so do it afterwards
						}
					}
			}
			if(null!=toBeRemoved){
				for(ClientCommunicationsObject o:toBeRemoved){
					if(commObjectsList.contains(o)) commObjectsList.remove(o);
				}
			}
		} catch (IOException e) {
				System.err.println("Could send message to client"+obj.getID() );
				e.printStackTrace();
		} catch (java.util.ConcurrentModificationException cme){
				System.err.println("Object List modified. Aborting send");
		}
		
	}
	
	/**Sends a message to all clients currently registered
	 * Clients will individually choose weither to receive message 
	 * based on their own message mask
	 * 
	 * @param type MessageTypes SEND_TYPE type
	 * @param message raw message. Should be properly XML formatted
	 */
	public static synchronized void send(String type, String message){
		send(type,"",message);
	}

	/**Hangs up a specific client, by ID(port) number and removes it
	 * from the clients list
	 * @param port
	 * @throws IOException
	 */
	public static void hangupClient(int port) throws IOException{
		ClientCommunicationsObject obj = findClient(port);
		obj.hangup();
		commObjectsList.remove(obj);
	}
	
	/**Hangs up on all currently registred clients and removes them
	 * from the clients list
	 * @throws IOException
	 */
	public static void hangupAll() throws IOException{
		for(ClientCommunicationsObject obj: commObjectsList){
			obj.hangup();
			commObjectsList.remove(obj);
		}
	}
	
	static ClientCommunicationsObject findClient(int port){
		for(ClientCommunicationsObject obj: commObjectsList)
			if (port==obj.getID())return obj;
			return null;
		
	}
	
	static void checkAndStartSelectionThread(){
		if (null==selectionThread) selectionThread=new dimes.AgentGuiComm.comm.channels.SelectorThread( dimes.AgentGuiComm.comm.Dispatcher.getInstance(dimes.AgentGuiComm.AgentFrameFacade.getInstance(), dimes.AgentGuiComm.PropertiesFrameFacade.getInstance()));
		//else selectionThread.
	} 
}
