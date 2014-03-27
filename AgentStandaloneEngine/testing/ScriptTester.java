package testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ScriptTester {

	/**This is a standalone script loader for the Agent. It takes two parameters:
	 * a filename with a .txt extension, and a port number (optional). 
	 * Upon running, the loader will attempt to connect to the port given, or
	 * to the default DIMES port 33333 if there's no port argument, and deliver the
	 * script as a local user script. This is used instead of the userscript window
	 * in the GUI to test the Agent in systems where GUI is not available. 
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length<1 || args.length>2){
			System.out.println("USE:\n scriptloader [scriptfile.txt] [port(optional)]");
			System.exit(-1);
		}
	
		String name=null;
		int port=33333;
		if (args[0].toLowerCase().endsWith(".txt")){
			name=args[0];
		}else{
			System.out.println("USE:\n scriptloader [scriptfile.txt] [port(optional)]");
			System.exit(-1);
		}
		if(args.length>1){
			try{
				port = Integer.parseInt(args[1]);
			}catch(NumberFormatException nfe){
				System.out.println("invalid port specified. Default port number 33333 will be used instead");
				port=33333;
			}
		}
		ScriptTester st =new ScriptTester(name,port);
	}
	
	public ScriptTester(String filename, int port){
//		File scriptFile = new File(filename);
//		String script=null;
//		
//		//Get the script text from file
//		try{
//			StringBuilder sb = new StringBuilder("");
//			BufferedReader br = new BufferedReader(new FileReader(scriptFile));
//			while(null!=(script=br.readLine())){ //we can do this because "script" is empty for now
//				sb.append(script);
//			}
//			script=sb.toString();
//		}catch (IOException ioe){
//			System.out.println("Error reading file "+filename);
//			System.exit(-1);
//		}
//		
		//Connect to Agent and deliver script
		try{
			Socket outSocket = new Socket();
			outSocket.connect(new InetSocketAddress(InetAddress.getLocalHost(),port));//connect to socket
			
			BufferedReader br = new BufferedReader(new InputStreamReader(outSocket.getInputStream()));
			String incoming = br.readLine(); //get DIMES greeting message to make sure we're connecting to Agent
			if (!incoming.startsWith("<XML><DIMES")){
				System.out.println("service at port "+port+" is not a DIMES Agent");
				System.exit(-1);
			}
			
			PrintStream outstream = new PrintStream(outSocket.getOutputStream());//Assuming we found a DIMES Agent transmit script
			outstream.println("<DIMES></DIMES>");
			
			incoming = br.readLine();
			if(incoming.startsWith("<DIMES"));
			outstream.println("<SCRIPT type=\"file\" filename=\""+filename+"\" />");
			
			outstream.flush();//cleanup
			outstream.close();
			outSocket.close();
			
			System.out.println("Script tranmited successufuly.");
			System.exit(0);
		}catch(IOException ioe2){
			System.out.println("Error connecting to Agent ");
			System.exit(-1);
		}
	}
}
