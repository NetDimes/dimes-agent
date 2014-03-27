/*
 * Created on 25/08/2005
 *
 */
package dimes.scheduler.usertask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import dimes.measurements.MeasurementType;
//import dimes.measurements.Measurements;
import dimes.measurements.IPUtils;
import dimes.measurements.Protocol;
import dimes.scheduler.Parser;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;

/**
 * @author Ohad Serfaty
 *
 */
public class FileUserTaskSource implements UserTaskSource
{

	private final File taskFile;
	private String commandString = "";
	private int defaultOperation = MeasurementType.TRACEROUTE;
	private int defaultProtocol = Protocol.UDP;
	private Logger logger = Loggers.getUserScriptsLogger();

	public FileUserTaskSource(File taskFile)
	{
		this.taskFile = taskFile;
	}

	public FileUserTaskSource(File taskFile, int defaultOperation, int defaultProtocol)
	{
		this.taskFile = taskFile;
		this.defaultProtocol = defaultProtocol;
		this.defaultOperation = defaultOperation;
	}

	public void parse(){
		FileReader reader;
		try {
			reader = new FileReader(taskFile);
			BufferedReader buf = new BufferedReader(reader);
			StringBuilder str = new StringBuilder("");
			String line=null;
			while ((line = buf.readLine()) != null)
				str.append(line);
			commandString = Parser.parsePingTracerouteToXML(str.toString());
			System.out.println(" final string is :"+commandString);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
//	
//	public void parse() throws UserTaskPerserException
//	{
//		try
//		{
//			// read line by line :
//			FileReader reader = new FileReader(taskFile);
//			BufferedReader buf = new BufferedReader(reader);
//			String line = null;
//			Vector<String> ipOperands = new Vector<String>();
//
//			// for each line :
//			while ((line = buf.readLine()) != null)
//			{
//				line = line.trim();
//				logger.info("Reading line :" + line + "\n");
//				StringTokenizer tokenizer = new StringTokenizer(line, " ");
//				if (tokenizer.countTokens() > 0)
//				{
//					String firstToken = tokenizer.nextToken();
//					// determine the measurement type (default is traceroute) :
//					String measurementType = MeasurementType.getName(defaultOperation);
//					if (firstToken.equalsIgnoreCase(MeasurementType.getName(MeasurementType.PING))) // find out ping...
//						measurementType = MeasurementType.getName(MeasurementType.PING);
//					else
//					if (firstToken.startsWith(MeasurementType.getName(MeasurementType.QBE))) // find out ping...
//						measurementType = MeasurementType.getName(MeasurementType.QBE);
//					else
////						if (firstToken.equalsIgnoreCase(MeasurementType.getName(MeasurementType.PACKETTRAIN))) // find out ping...
////							measurementType = MeasurementType.getName(MeasurementType.PACKETTRAIN);
////					else
//						if (firstToken.equalsIgnoreCase(MeasurementType.getName(MeasurementType.TRACEROUTE))) // find out ping...
//							measurementType = MeasurementType.getName(MeasurementType.TRACEROUTE);
//						else if (measurementType.equals(MeasurementType.getName(MeasurementType.PARIS_TRACEROUTE))) {
//							measurementType = MeasurementType.getName(MeasurementType.PARIS_TRACEROUTE);
//						}
//					
//
//					logger.info("First token : " + firstToken + " Measurement type :" + measurementType);
//					if (measurementType.equals(MeasurementType.getName(MeasurementType.TRACEROUTE)) || 
//						measurementType.equals(MeasurementType.getName(MeasurementType.PING)) ||
//						measurementType.equals(MeasurementType.getName(MeasurementType.PARIS_TRACEROUTE)))
//					{
////						 determine the ips from the first operand :
//						if (IPUtils.isValidAddress(firstToken))
//							ipOperands.add(firstToken);
//
//						String protocol = Protocol.getName(defaultProtocol);
//						String port = "";
//						while (tokenizer.hasMoreTokens())
//						{
//							String nextToken = tokenizer.nextToken();
//							if (IPUtils.isValidAddress(nextToken))
//								ipOperands.add(nextToken);
//							else
//							if (nextToken.equalsIgnoreCase(Protocol.getName(Protocol.ICMP)))
//								protocol = nextToken;
//							else
//							if (nextToken.equalsIgnoreCase(Protocol.getName(Protocol.UDP)))
//									protocol = nextToken;
//							else
//								port= nextToken;
//								
//						}
//	
//						Iterator<String> i = ipOperands.iterator();
//						while (i.hasNext())
//						{
//							String ip = /*(String)*/ i.next();
//							commandString += measurementType + " " + ip + " " +protocol+ " " +port+ "\n";
///*							commandString += ip + " ";
//							commandString += protocol + " ";
//							commandString += "\n";*/
//						}
//					}
//					else
//					{
//						// For QBE matters : add the line as it was given to you :
//						commandString += (line);
//						commandString += "\n";
//					}
//				}
//				ipOperands.clear();
//			}
//			logger.info("Creating user script with following commands:");
//			logger.info(commandString);
//			//            System.out.println(" final string is :"+commandString);
//
//		}
//		catch (FileNotFoundException e)
//		{
//			e.printStackTrace();
//			throw new UserTaskPerserException("File not found: :" + taskFile.getAbsolutePath());
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//			throw new UserTaskPerserException("Error while reading File:\n" + e.getMessage());
//		}
//
//	}
	
	

	public String getCommandsString()
	{
		return commandString;
	}

	public String getScriptID()
	{
		return taskFile.getName();
	}

}