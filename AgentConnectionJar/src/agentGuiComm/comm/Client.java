package agentGuiComm.comm;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import agentGuiComm.util.XMLUtil;




/**
 * A client is Thread that simulates an Agent Gui Client or 
 * third party app that makes use of Agent services via XML
 * Since this is a dumb client, both up and downstream communications
 * are handled through the same class.
 * 
 * @author user
 *
 */
public class Client extends Thread{

	private Socket commSocket;
	private boolean die=false;
	private boolean connected=false;
	private BufferedReader iOStream;
	private PrintStream oOStream;
	private int myid;
	private int remotePort;
	private int port;
	private String resMask = "ALL";
	private String logMask = "INFO";
	private String last="";
	private Receiver dispatcher;
	private String remoteIP=null;
	
	/**Public constractor
	 * @param id 
	 * @param port
	 */
	public Client(int id, int port){
		myid=id;
		this.port = port;
		dispatcher = new Receiver();
	}
	
	public Client(int id, String ip, int port){
		myid=id;
		this.port = port;
		this.remoteIP= ip;
		dispatcher = new Receiver();
	}
	
	@Override
	public void run() {
		System.out.println("Client Started");
		try {
			while (!die){

				last = XMLUtil.getXMLStringFromStreamThrowing(iOStream);
				if(last.equals("")){
						connected=false;
						die=true;
						return;			
				}

				if(last.contains("<CLIENTID>") && last.contains("</CLIENTID>")){
					int loc = last.indexOf("<CLIENTID>")+10;
					last = last.substring(loc);
					last = last.substring(0, last.indexOf("<"));
					try{
					remotePort = Integer.parseInt(last);

					}catch(NumberFormatException nfe){
						System.err.println("Last: "+last);
					}
					
				}
				dispatcher.dispatch(last);
				
			}
			sendRaw("<DIMES><GOODBYE /></DIMES>");
			connected=false;

			commSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(0);
		}catch(SocketException se){
			if(die) System.out.println("Force Closed socket");
			System.exit(0);
			
		}catch (IOException e) {
			die=true;
			connected=false;
			try {
				commSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
			
		} /*catch (InterruptedException e) {

			e.printStackTrace();
			System.exit(0);
		}*/ catch(Exception e){
			e.printStackTrace();
//			System.exit(0);
		}
	}
	
/*
 <SHOW><RESULTS>ALL</RESULTS><LOG>INFO</LOG></RESULTS></SHOW>  
 */
	private String generateMaskMessageText() {
		StringBuilder message = new StringBuilder("<SHOW>");
		message.append("<RESULTS>"+getResMask()+"</RESULTS>");
		message.append("<LOG>"+getLogMask()+"</LOG>");
		message.append("</SHOW>");
		return message.toString();
	}
	

	
	public void hangup(){
		die=true;
		sendRaw("<DIMES><GOODBYE /></DIMES>");
		connected=false;
		try {
			commSocket.close();
		} catch (IOException e) {
			System.out.println("Force Closed socket");
		}
	}
	
	public boolean getConnected(){
		return connected;
	}
	
	public synchronized void sendRaw(final String text){
		if(connected){
			oOStream.println(text);
			System.out.println("SENDING: "+text);
		}
	}
	
	public int getPort(){
		return remotePort;
	}
	
	public int getID(){
		return myid;
	}
	
	public void setPort(int p){
		port=p;
	}

	public String getResMask() {
		return resMask;
	}

	public void setResMask(String resMask) {
		this.resMask = resMask;
//		if(connected) sendRaw(generateMaskMessageText());
	}

	public String getLogMask() {
		return logMask;
	}

	public void setLogMask(String logMask) {
		this.logMask = logMask;
//		if(connected) sendRaw(generateMaskMessageText());
	}


	public Receiver connect() {
		//debug from here
		commSocket = new Socket();
		InetAddress host;
		try {
			if (null!=remoteIP)
				host = InetAddress.getLocalHost();
			else 
				host = InetAddress.getByName(remoteIP);
			InetSocketAddress socketAddress = new InetSocketAddress(host,port);
			commSocket.connect(socketAddress, 3000);		
			iOStream = new BufferedReader(new InputStreamReader(commSocket.getInputStream()));
			String msg = iOStream.readLine();
			System.out.println(msg);
			dispatcher.dispatch(msg);
			OutputStream ouSt = commSocket.getOutputStream();
			oOStream = new PrintStream(ouSt,true,"UTF-8");
			oOStream.println("<DIMES />");
			connected = true;
			oOStream.println(generateMaskMessageText());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		return this.dispatcher;
	}
	
	public void reconnect(){
		die=false;
		connect();
		new Thread(this).start();//run();
	}
	
	public PrintStream getStream(){
		if(connected) return oOStream;
		else return null;
	}

	
}
