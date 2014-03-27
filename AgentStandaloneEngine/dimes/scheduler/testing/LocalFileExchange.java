/**
 * 
 */
package dimes.scheduler.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import dimes.comm2server.ConnectionException;
import dimes.comm2server.FileExchangeChannel;
import dimes.comm2server.TaskBackedOffException;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**
 * @author Ohad Serfaty
 *
 */
public class LocalFileExchange implements FileExchangeChannel {

	private final String fileName;

	public LocalFileExchange(String fileName) {
		this.fileName = fileName;
	}

	public File exchangeFiles(File outgoing, File incoming, String header, String trailer) throws ConnectionException, IOException {
		return null;
	}

	public void exchangeFiles(BufferedReader outReader, OutputStream incomingStream, String header, String trailer) throws ConnectionException, IOException {
		// TODO Auto-generated method stub
		
	}

	public void exchangeFiles(BufferedReader outReader, OutputStream outStream) throws IOException, ConnectionException {
		// TODO Auto-generated method stub
		
	}

	public void exchangeFiles(BufferedReader outReader, BufferedWriter writer, String header, String trailer) throws IOException, ConnectionException {
		// TODO Auto-generated method stub
		
	}

	public boolean dummyAttempt() {
		// TODO Auto-generated method stub
		return false;
	}

	public File sendReceive(File file, String header, String trailer) throws NumberFormatException, NoSuchPropertyException, TaskBackedOffException {
		return new File(fileName);
	}

}
