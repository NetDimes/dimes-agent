package dimes.measurements.nio;

import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;


/**
 * @author Ohad Serfaty
 *
 * a static class meant to serve as a public ip inquirer. 
 * A service should run on a distant machine which returns 
 * 
 */
public class PublicIPInquirer {

	private static InetSocketAddress[] addresses;
	public static final InetSocketAddress[] DEFAULT_RESPONDERS = {
		new InetSocketAddress("132.66.48.22" , 51675) };
	public static final InetSocketAddress[] LOCALHOST_RESPONDERS = {
		new InetSocketAddress("localhost" , 51675) };

	static 
	{
		PublicIPInquirer.setResponders(DEFAULT_RESPONDERS);
	}
	
	/**
	 * Set the responders from which to query the IP address from.
	 * 
	 * @param addresses an array of the server addresses.
	 */
	public static void setResponders(InetSocketAddress[] addresses){
		PublicIPInquirer.addresses = addresses;
	}

	/**
	 * @return the public ip of the localhost , as queried from the 
	 * addresses given as input to setResponders().
	 */
	public static InetSocketAddress queryPublicIP(){
		for (int i=0; i<addresses.length; i++){
			try
			{
			Socket sock = new Socket();
			sock.connect(addresses[i]);
			InetSocketAddress address = (InetSocketAddress) new ObjectInputStream(sock.getInputStream()).readObject();
			// close the socket with brut force :
			try
			{
				sock.shutdownInput();
				sock.shutdownOutput();
				sock.close();
			}
			catch (Exception e){}
			return address;
			}
			catch (Exception e)
			{
//				e.printStackTrace();
			}
			
		}
		return null;
	}
	
	public static String queryMachineIP() {
		try
		{
			Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements())
			{
				NetworkInterface intf = (NetworkInterface) interfaces.nextElement();
				Enumeration inetAddresses = intf.getInetAddresses();
				while (inetAddresses.hasMoreElements())
				{
					InetAddress inetAd = (InetAddress) inetAddresses.nextElement();
					String ip = inetAd.getHostAddress();
					System.out.println("\nip"+ip+"isPrivateIP(ip)" + isPrivateIP(ip)+"\nisAutoIP(ip)" +isAutoIP(ip)+
							"\nisInvalidIP(ip)"+isInvalidIP(ip));
					if (!isPrivateIP(ip) && !isAutoIP(ip) &&!isInvalidIP(ip))
					{
						System.out.println("--------- returning ip:"+ip+
								" \ninetAd.getHostAddress()"+inetAd.getHostAddress());
						return inetAd.getHostAddress();
					}
				}
			}
			System.out.println("returning "+InetAddress.getLocalHost().getHostAddress());
			return InetAddress.getLocalHost().getHostAddress();
		}
		catch (Exception e)
		{
			System.err.println("Could not Discover Network interfaces. Using default settings.");
			try 
			{
				return InetAddress.getLocalHost().getHostAddress();
			}
			catch (UnknownHostException e1) 
			{}
			return "127.0.0.1";
		}
		
	}
	
	static public boolean isPrivateIP(String anIP)
	{
		if (anIP.startsWith("10.") || anIP.startsWith("192.168") || anIP.equals("127.0.0.1"))
			return true;
		return false;
	}
	
	static public boolean isAutoIP(String anIP)
	{
		if (anIP.startsWith("169.254"))
			return true;
		return false;
	}
	
	public static boolean isInvalidIP(String anIP){
		if (anIP.startsWith("0."))
			return true;
		return false;
	}

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		System.out.println(PublicIPInquirer.queryMachineIP());
	}

	
}
