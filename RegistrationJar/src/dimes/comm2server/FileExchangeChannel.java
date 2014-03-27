/**
 * 
 */
package dimes.comm2server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * @author Ohad Serfaty
 *
 */
public interface FileExchangeChannel {
	
	public File exchangeFiles(File outgoing, File incoming, String header, String trailer) throws ConnectionException, IOException;
	public void exchangeFiles(BufferedReader outReader, OutputStream incomingStream, String header, String trailer) throws ConnectionException, IOException ;
	public void exchangeFiles(BufferedReader outReader, OutputStream outStream) throws IOException, ConnectionException;
	public void exchangeFiles(BufferedReader outReader, BufferedWriter writer, String header, String trailer) throws IOException, ConnectionException;

	public boolean dummyAttempt();
	public File sendReceive(File file, String header, String trailer) throws NumberFormatException, NoSuchPropertyException, TaskBackedOffException;
	
}
