/*
 * Created on 01/12/2004
 */
package dimes.comm2server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import dimes.AgentGuiComm.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;

/**
 * @author anat
 */
public class SecureConnectionHandlerImpl extends ConnectionHandler
{
	private Logger logger;

	/**
	 * @param serverURLstr
	 * @throws MalformedURLException
	 */
	SecureConnectionHandlerImpl(String serverURLstr) throws MalformedURLException
	{
		super(serverURLstr);
		// TODO Auto-generated constructor stub

		this.logger = Loggers.getLogger(this.getClass());
	}

	/* (non-Javadoc)
	 * @see dimes.comm2server.ConnectionHandler#initConnection()
	 */
	public void initConnection() throws ConnectionException
	{
		try
		{
			System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");
			SSLContext sslContext = SSLContext.getInstance("TLS");

			// add the SUN Providers to the list :
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			char[] keyPassphrase = "serverPassword".toCharArray();
			String resourceDir = PropertiesBean.getProperty(PropertiesNames.RESOURCES_DIR/*"dirs.resources"*/);

			
//			String rm = resourceDir+"\\client.keystore";
//			Object obj = this.getClass().getClassLoader().getResource(rm);
//			File f = new File(resourceDir+"\\client.keystore");
//			boolean g = f.exists();
//			 URL ul = new URL(f); // this.getClass().getClassLoader().getResource(rm);
//			 String ulS=ul.toString();
				
//			URL clientKeyStoreURL = this.getClass().getClassLoader().getResource(ResourceManager.CLIENT_KEYSTORE_RSRC);
			FileInputStream clientKeyStoreURL= new FileInputStream(new File(resourceDir+File.separator+"client.keystore"));
			keyStore.load(clientKeyStoreURL /*.openStream()*/, keyPassphrase);//check
			keyManagerFactory.init(keyStore, keyPassphrase);

			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			KeyStore trustStore = KeyStore.getInstance("PKCS12");
			char[] trustPassphrase = "serverPassword".toCharArray();

//			URL serverKeyStroeRscr = this.getClass().getClassLoader().getResource(ResourceManager.SERVER_KEYSTORE_RSRC);
			FileInputStream serverKeyStroeRscr = new FileInputStream(new File(resourceDir+File.separator+"server.keystore"));
			trustStore.load(serverKeyStroeRscr/*.openStream()*/, trustPassphrase);//check
			trustManagerFactory.init(trustStore);

			sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
			SSLSocketFactory socketFactory = sslContext.getSocketFactory();
			HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);

			/**********************************************************************/
			HostnameVerifier hv = new CertificateHostnameVerifier();
			HttpsURLConnection.setDefaultHostnameVerifier(hv);

			HttpsURLConnection secConnection;
			try
			{
				secConnection = (HttpsURLConnection) serverURL.openConnection();

				System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
				System.setProperty("sun.net.client.defaultReadTimeout", "30000");
				secConnection.setRequestMethod("POST");
				secConnection.setAllowUserInteraction(false); // system may not ask the user
				secConnection.setDoOutput(true); // we want to send things
				secConnection.setDoInput(true); // we want to receive things

				secConnection.connect();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
				throw new ConnectionException(e1.toString());
			}
			Certificate[] serverCerts = secConnection.getServerCertificates();
			Certificate trustedCer = trustStore.getCertificate("www.netdimes.org");

			for (int i = 0; i < serverCerts.length; ++i)
			{
				Certificate checkedCer = serverCerts[i];
				if (!checkedCer.equals(trustedCer))
				{
					/*System.err.println*/logger.warning("different certificates!");
					secConnection.disconnect();
					return /*null*/;
				}
				/*System.out.println*/logger.fine("same certificate!");
				try
				{
					checkedCer.verify(trustedCer.getPublicKey());
				}
				catch (Exception e3)
				{
					e3.printStackTrace();
					/*System.err.println*/logger.warning("could not verify key!");
					secConnection.disconnect();
					return /*null*/;
				}
				/*System.out.println*/logger.fine("verified!");
			}
			this.currConnection = secConnection;
		}
		catch (ConnectionException e)
		{
			e.printStackTrace();
			throw e;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SecureConnectionException(e.toString());
		}

		//		return secConnection;
	}

	private class CertificateHostnameVerifier implements HostnameVerifier
	{
		public boolean verify(String hostNameFromCer, SSLSession session)
		{
			/*System.out.println*/logger.fine("WARNING: Hostname is not matched for cert.");
			String sessionPeer = session.getPeerHost();
			hostNameFromCer = hostNameFromCer.trim();
			/*System.out.println*/logger.fine("certificate: " + hostNameFromCer + "\tsession: " + sessionPeer);
			try
			{
				InetAddress[] resolvedHostIPs = InetAddress.getAllByName(hostNameFromCer);
				for (int i = 0; i < resolvedHostIPs.length; ++i)
				{
					String resolvedHostIPstr = resolvedHostIPs[i].getHostAddress();
					InetAddress resolvedHostName = InetAddress.getByName(resolvedHostIPstr);
					String resolvedHostNameStr = resolvedHostName.getHostName();
					/*System.out.println*/logger.fine("session: " + sessionPeer + "\tcertificate: " + hostNameFromCer + "\tresolved IP from cer: "
							+ resolvedHostIPstr + "\tresolved name from cer: " + resolvedHostNameStr);
					if (sessionPeer.equalsIgnoreCase(resolvedHostIPstr))
					{
						/*System.out.println*/logger.fine("managed to resolve " + hostNameFromCer + " to IP: " + resolvedHostIPstr
								+ ". equals to session peer: " + sessionPeer);
						return true;
					}
					if (sessionPeer.equalsIgnoreCase(resolvedHostNameStr))
					{
						/*System.out.println*/logger.fine("managed to resolve " + hostNameFromCer + " to name: " + resolvedHostNameStr
								+ ". equals to session peer: " + sessionPeer);
						return true;
					}
				}
			}
			catch (UnknownHostException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

	}

	/*    public static void main(String args[]) throws MalformedURLException, ConnectionException
	 {
	 PropertiesBean.init("c:\\DevelopmentAgent\\conf\\developmentProperties.xml");
	 try
	 {
	 SecureConnectionHandlerImpl conn = new SecureConnectionHandlerImpl("https://www.netdimes.org/DIMES/propertiesUpdate");
	 conn.initConnection();
	 }
	 catch(Exception e){
	 e.printStackTrace();
	 }
	 }
	 */
}