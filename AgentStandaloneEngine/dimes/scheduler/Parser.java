/*
 * Created on 22/01/2004
 */
package dimes.scheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dimes.AgentGuiComm.logging.Loggers;
import dimes.measurements.Measurements;
import dimes.measurements.Protocol;
import dimes.scheduler.usertask.UserTaskSource;
import dimes.util.XMLUtil;
import dimes.util.properties.PropertiesNames;
import dimes.util.time.TimeSynchronizationManager;

/**
 * @author anat, BoazH (0.6)
 */
public class Parser
{
	private static final int MIN_QBE_PARAMETERS = 7;

	/* 
	 * state fields - relevant to the last parsing.
	 * Will also contain meta-commands and globals. 
	 */
	private TreeMap<Integer, Vector<Task>> prioritizedTasks; //key - priority, value Hashtable of Vectors of tasks according to priority
	private Logger logger;
	enum measurementTypes {PING, TRACEROUTE, QBE, PARISTRACEROUTE, UPDATE};
//	private String myName;
	
	static
	{
		TimeSynchronizationManager.setDimesServerTime(0);
	}

	/**Public Constructor
	 * 
	 */
	public Parser()
	{
		prioritizedTasks = new TreeMap<Integer, Vector<Task>>();
		logger = Loggers.getLogger(this.getClass());
	}

	/**Checks to see if the script is a properly formated DIMES XML script, wraps it in XML if it is not. 
	 * @param theExId
	 * @param theScriptId
	 * @param commandLineTask
	 * @return A reader containging XML script
	 */
	public Reader getMeasurementScript(String theExId, String theScriptId, UserTaskSource commandLineTask)
	{
		String script=null;
		String tempScript = commandLineTask.getCommandsString().toUpperCase();
		if(tempScript.trim().startsWith("<"+PropertiesNames.PENNY.toUpperCase())){
			try{
				//If we can parse this into XML and verify that there is a "SCRIPT" node,
				//it means that we have a properly formated xml script
				Element scriptRoot = XMLUtil.getRootElement(tempScript); //This should be the "PENNY" element
				
				/*if(scriptRoot.hasAttribute(PropertiesNames.SCRIPT)){
					script = tempScript;
				}*/
				
				if(XMLUtil.getChildElementByName(scriptRoot, "SCRIPT")!=null) script=tempScript;
			}catch(Exception e){//if parse failed, it's not a vaild xml script
				script=wrapScript(tempScript, theScriptId, theExId);
			}
		}else{//if there's no "PENNY" tag, it's not a valid XML script
			script=wrapScript(tempScript, theScriptId, theExId);
		}
		System.out.println("Generated script : " + script);

		StringReader reader = new StringReader(script);
		return reader;
	}

	private String wrapScript(String scriptText, String theScriptId, String theExId){
		String script;
		script = "<" + PropertiesNames.PENNY + ">";
		script += "<" + PropertiesNames.SCRIPT + " " + PropertiesNames.EXPERIMENT_ID + "=\"" + theExId + "\" " + " " + PropertiesNames.SCRIPT_ID + "=\""
				+ theScriptId + "\">\n";
		script += "<" + PropertiesNames.PRIORITY + ">" + Priority.getName(Priority.USER) + "</" + PropertiesNames.PRIORITY + ">\n";
		script += scriptText;//commandLineTask.getCommandsString(); // here are all the script commands 
		script += "\n</" + PropertiesNames.SCRIPT + ">\n";
		script += "</" + PropertiesNames.PENNY + ">";
		return script;
	}
	
	/**Parses a reader containing a DIMES XML script and puts the script commands in  the priorities tasks list
	 * @param reader
	 */
	public void parse(Reader reader) //throws DocumentException
	{
		/**
		 * parses 1 script - contains an ID and then a list of IPs. 
		 */
/*
		BufferedReader in = new BufferedReader(reader);
		String g=null;
		System.out.println("-----------script----------");
			try {
				while (null!=(g=in.readLine()))
				System.out.println(g);
				System.out.println("-----------script----------");
				Thread.sleep(100000);
			} catch (IOException e1) {
				
				e1.printStackTrace();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}*/
		this.prioritizedTasks.clear(); //Clear tasks from last parse op out of list

//		SAXReader saxReader = new SAXReader();
//		saxReader.setValidation(false);
//		Document doc = saxReader.read(reader);
//		Element rootElem = doc.getRootElement();
		
//		InputSource is= new InputSource(reader);
//		java.io.InputStream in = is.getByteStream();

		
		
//--------------------------------------------------Read reader into a String. 
		String scrpitstr=null;
		try{
		  char[] arr = new char[8*1024]; // 8K at a time
		  StringBuffer buf = new StringBuffer();
		  int numChars;

		  while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
		      buf.append(arr, 0, numChars);
		      }
		  scrpitstr = buf.toString();
		}catch(IOException e){
			e.printStackTrace();
		}
		System.out.println("Parser.parse scrpitstr: "+scrpitstr);
		Element rootElem = XMLUtil.getRootElement(scrpitstr);
//--------------------------------------------------
		
		//-----This section is to allow user scripts to work with or without <penny> tags
		Element secondPenny = XMLUtil.getChildElementByName(rootElem, rootElem.getTagName());
		if (null!=secondPenny) {
			rootElem=secondPenny;
			secondPenny=null;
		}
		System.out.println("SecondPenny");
		//-----------
		
		Element headerElem = XMLUtil.getChildElementByName(rootElem, PropertiesNames.HEADER);//(Element) rootElem.selectSingleNode("//" + PropertiesNames.HEADER);
		if (headerElem != null)
			this.parseHeader(headerElem);
		System.out.println("headerElem");
		//Find all elements named "script" in the xml
		List<Node> scripts =XMLUtil.getNodeListAsList(rootElem.getElementsByTagName(PropertiesNames.SCRIPT));// XMLUtil.getChildNodesByName(rootElem, PropertiesNames.SCRIPT);//rootElem.selectNodes("//" + PropertiesNames.SCRIPT);
		if(scripts.isEmpty()) {
			NodeList n = rootElem.getElementsByTagName(PropertiesNames.SCRIPT.toUpperCase());
			scripts = XMLUtil.getNodeListAsList(n);
		}
		System.out.println("NodeList Scripts");
		//For Each Scrpit element, get priority, ID, and ExID
		Iterator<Node> iter = scripts.iterator();
		while (iter.hasNext())
		{
			Element scriptElem = (Element) iter.next();
			Element priorityElm=XMLUtil.getChildElementByName(scriptElem, PropertiesNames.PRIORITY);
			String priorityStr=Priority.getName(Priority.getDefault()); 
			
			if (priorityElm!=null){
				priorityStr = /*priorityElm==null?null:*/priorityElm.getTextContent();//scriptElem.elementText(PropertiesNames.PRIORITY);
				scriptElem.removeChild(priorityElm);
				priorityElm=null;
			}
			int priority = this.getPriority(priorityStr);
			//Check to see if getAttribute is case sensative
			String ID = scriptElem.getAttribute(PropertiesNames.SCRIPT_ID);//attributeValue(PropertiesNames.SCRIPT_ID);
			String exID = scriptElem.getAttribute(PropertiesNames.EXPERIMENT_ID);//.attributeValue(PropertiesNames.EXPERIMENT_ID);
			if (exID == "")//could happen in old scripts that don't have this tag
				exID = "DimesExperiment";
			System.out.println("Parser.parse ID: "+ID+" exID: "+exID);
			
			//We already saved the info from the script attributes, so we can replace the tag with a simple "SCRIPT" tag
			scriptElem =XMLUtil.getRootElement("<"+PropertiesNames.SCRIPT+">"+parsePingTracerouteToXML(XMLUtil.nodeChildrenToString(scriptElem))+"</"+PropertiesNames.SCRIPT+">");

			try
			{
				SyntaxTree aTree = this.parseScript(scriptElem);
				//System.out.println("Script syntax tree :" + aTree);
				Task aTask = new Task(exID, ID, priority, aTree);
				Integer priorityObj = new Integer(priority);
				//get Task Hashtable according to priority
				Vector<Task> priorityContainer = this.prioritizedTasks.get(priorityObj);//TODO: check

				//create if doesn't exist
				if (priorityContainer == null)
				{
					priorityContainer = new Vector<Task>();
					prioritizedTasks.put(priorityObj, priorityContainer);
				}
				priorityContainer.add(aTask);
				System.out.println("Parser.parse Task XML: "+aTask.toXML());
			}
			catch (Exception e)//IOException / ParseException
			{
				e.printStackTrace();
				this.logger.warning(e.toString());
			}
		}

		return;
	}
	//TDOD:here

	/**Parses a non-XML (old style) DIMES script to XML
	 * Leaves XML scripts unchanged.
	 * @param str script
	 * 
	 * @return String representing the script in XML format
	 */
	public static String parsePingTracerouteToXML(String str){
		
		if (XMLUtil.isTextXML(str)) return str; //Is the string an XML? Return if true
		
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new StringReader(insertLineBreaks(str)));
		String line = "";
		try {
			while (reader.ready()){
				line = reader.readLine().trim();
				if (null==line) break;
				if(line.startsWith("PING") || line.startsWith("TRACEROUTE")|| line.startsWith("PARISTRACEROUTE")){
					String[] parts = line.split("\\s");
					sb.append("<"+parts[0]+" ADDRESS=\""+parts[1].trim()+"\" ");
					switch (parts.length){
						case 5:	
							sb.append("DESTPORT=\""+parts[4].trim()+"\" ");
						case 4:
							sb.append("SOURCEPORT=\""+parts[3].trim()+"\" ");
						case 3:
							sb.append("PROTOCOL=\""+parts[2].trim()+"\" />");
							break;
						default: 
							sb.append("PROTOCOL=\"ICMP\" ");
							sb.append("SOURCEPORT=\"-1\" ");
							sb.append("DESTPORT=\"-1\" />");
							break;
						}	
				}else sb.append(line);
				line=null;
			}		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException npe){
			//Use reflection to determine what class called this static method and use its logger
//			String callingClassName = sun.reflect.Reflection.getCallerClass(2).getName();
//			Logger.getLogger(callingClassName).log(Level.WARNING, "Malformed line in user script, script aborted. Line:", line);
//			npe.printStackTrace();
		}
//		String test = sb.toString();
		return sb.toString();
	}
	
	/**
	 * currently only looks for askForAgentIndex
	 * @param theHeaderElem
	 */
	private void parseHeader(Element theHeaderElem)
	{
		//        System.out.println("parsing header");//debug
		Element agentIndexElem = XMLUtil.getChildElementByName(theHeaderElem, PropertiesNames.ASK_FOR_AGENT_INDEX);//theHeaderElem.element(PropertiesNames.ASK_FOR_AGENT_INDEX);
		if (agentIndexElem != null)
		{
			//        System.out.println("index string is: "+agentIndexElem.getTextTrim());//debug
			int index = Integer.parseInt(agentIndexElem.getTextContent().trim());//.getTextTrim());
			Measurements.setAgentIndex(index);
		}
		//  bring this back for testing
//		Element dimesServerTime = theHeaderElem.element(PropertiesNames.SERVER_TIME);
//		if (dimesServerTime!=null)
//		{
//			long serverTime = Long.parseLong(dimesServerTime.getTextTrim());
//			TimeSynchronizationManager.setDimesServerTime(serverTime);
//			this.logger.info("Setting Server time as:"+TimeSynchronizationManager.getDimesServerTime());
//		}
	}

	private int getPriority(String priorityStr)
	{
		if (priorityStr != null)
		{
			if (priorityStr.equalsIgnoreCase(Priority.getName(Priority.URGENT)))
				return Priority.URGENT;
			if (priorityStr.equalsIgnoreCase(Priority.getName(Priority.USER)))
				return Priority.USER;
			if (priorityStr.equalsIgnoreCase(Priority.getName(Priority.LOW)))
				return Priority.LOW;
		}
		return Priority.NORMAL; //default
	}
	
	private short ensureValidPort(short port){
		return (port>1024 && port <65335)?port:(short)(1025+Math.random()*64310);
	}

	private SyntaxTree parseScript(Element aScript) throws IOException, ParseException
	{
		List<Node> lines = XMLUtil.getNodeListAsList(aScript.getChildNodes());
		SyntaxTree tree=new SyntaxTree();
		OpParams params=null;
		Element currentElem=null;
		String protocol="ICMP";
		short sourcePort = -1;
		short destPort = -1;
		
		for(Node e:lines){
			try{
			currentElem = (Element)e;
			}catch (ClassCastException cce){
/*				String str = e.getNodeName();
				if(str.equals("#text")){ 
					str = e.getNodeValue();
					if(null==str){
						System.out.println("NULL NULL NULL");
						}
					else System.out.println(str);
					currentElem = XMLUtil.getRootElement(parsePingTracerouteToXML(str.toUpperCase()));
				}else{
				cce.printStackTrace();
				continue;
				}*/
			}
			if(currentElem.getTagName().equalsIgnoreCase(PropertiesNames.PRIORITY)) continue;
			if (currentElem.hasAttribute("PROTOCOL")) protocol = currentElem.getAttribute("PROTOCOL");
			if (currentElem.hasAttribute("SOURCEPORT")) sourcePort= Short.parseShort(currentElem.getAttribute("SOURCEPORT"));
			if (currentElem.hasAttribute("DESTPORT")) destPort= Short.parseShort(currentElem.getAttribute("DESTPORT"));
			
			switch (measurementTypes.valueOf(currentElem.getTagName())){
			case PING: 
			case TRACEROUTE:
			case PARISTRACEROUTE:
				params = new OpParams(currentElem.getTagName(), currentElem.getAttribute("ADDRESS"), protocol, ensureValidPort(sourcePort),ensureValidPort(destPort));
				break;
			case QBE:
				params = parseQBEOperation(currentElem, ensureValidPort(sourcePort), ensureValidPort(destPort));
				break;
//			case UPDATE:
//				parseUpdateOperation(currentElem);
//				break;
			default:
				break;
			}
			
			if(null!=params) tree.addLast(params);
		}

		
//		StringReader strReader = null;
//		BufferedReader reader = null;

//		SyntaxTree tree;
//		try
//		{
////			strReader = new StringReader("null"/*aScript*/);
////			reader = new BufferedReader(strReader);
//
////			tree = new SyntaxTree();
////			OpParams params;
//			String macroString = "";
//			for (String line = reader.readLine(); line != null; line = reader.readLine())
//			{
//				line = line.trim();
//				if (line.length() == 0 || line.startsWith("//"))
//					continue; //if empty string
//				//System.out.println("Parsing line :" + line);
//				boolean test = isMacroLine(line);
//				if (isMacroLine(line))
//				{
//					macroString += (line+"\n");
//					params = Parser.parseQBEOperation(macroString);
//					//System.out.println("Adding macro operation : " + params);
//					if (params != null)
//						tree.addLast(params);
//				}/*else if (isUpdate(line)){
//					macroString += (line+"\n");
//					params = Parser.parseUpdateOperation(macroString);
//					//System.out.println("Adding macro operation : " + params);
//					if (params != null)  //This should always be null.
//						tree.addLast(params);
//				}*/
//				else
//				{
//					macroString = "";
//					params = this.parseOperation(line);
//					tree.addLast(params);
//				}
//			}
//		}
//		catch (IOException e)
//		{
//			throw e;
//		}
//		catch (ParseException e)
//		{
//			throw e;
//		}
//		finally
//		{
//			if (strReader != null)
//				strReader.close();
//			if (reader != null)
//				reader.close();
//		}

		return tree;
	}

	/** Parses an Update script and starts the update thread immediately. Note that this method
	 * always returns null. An update operation does not enter into the regular execution queue, 
	 * but runs in its own thread.
	 * 
	 * @param updateElem
	 * @return
	 * @throws ParseException
	 */
//	private static OpParams parseUpdateOperation(Element updateElem) throws ParseException{
//		String id = updateElem.getAttribute("id");
//		String filename=updateElem.getTextContent().trim();
//		String md5=updateElem.hasAttribute("md5")?updateElem.getAttribute("md5"):"";
//		int filesize=-1;
//		String agentDir="";
//		try{
//			filesize=Integer.parseInt(updateElem.getAttribute("filesize"));
//			String temp;
//			temp=PropertiesBean.getProperty(PropertiesNames.BASE_DIR);
//			agentDir=new File(temp.substring(0,temp.indexOf("gent")+4)).getCanonicalPath();
//		}catch (NumberFormatException nfe){
//			return null;
//		} catch (NoSuchPropertyException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
////		try
////		{
////			if (macroString.substring(0,7).equalsIgnoreCase("<UPDATE") && (macroString.trim().endsWith("</UPDATE>")))//keep itirating until you've got the whole string
////			{
////				StringReader macroReader = new StringReader(macroString);
////				BufferedReader bufReader = new BufferedReader(macroReader);
////				String temp;
////				temp=PropertiesBean.getProperty(PropertiesNames.BASE_DIR);
////				String agentDir = new File(temp.substring(0,temp.indexOf("gent")+4)).getCanonicalPath();
////				UpdateOpParamsBuilder uopb = new UpdateOpParamsBuilder(temp);
////				
////				LinkedList<UpdateOpParams> updateLines = new LinkedList<UpdateOpParams>();
////				for (String line = bufReader.readLine(); line != null; line = bufReader.readLine())
////				{
////					// Parse the update unique id :
////					if (line.startsWith("UPDATE-start"))
////					{
////						String[] parts = line.split("\\s");
////						id = parts[1].trim();
////					}else if(!line.startsWith("UPDATE-end")) {
////					/*	 Parse a UPDATE command of the form :
////					 * 	 UPDATE-DIR [location],[directory],[UPDATE/NEW/DELETE]
////					 * 	 UPDATE-FILE [location],[filename],[UPDATE/NEW/DELETE]
////					 * 
////					 *  location - the location of the file relative to the Agent base directory
////					 *  filename/directory - the file to be changed
////					 *  update/new/delete - action to be taken. Update means delete current file and replace it, new mean add this file, delete means remove this file
////					 */
////						String[] parts = line.split("\\s");
////						if (line.startsWith("UPDATE")){
////							if("FILE".equalsIgnoreCase(parts[1].trim()))
////								filename=parts[2].toLowerCase();
////							else if ("FILESIZE".equalsIgnoreCase(parts[1].trim()))
////								filesize = Integer.parseInt(parts[2].trim());
////							else //md5
////								md5=parts[2];
////						}
/////*						if (line.startsWith("UPDATE-dir")){
////							updateLines.add(uopb.buildUpdateOpParamsObj(parts[1], parts[2], UpdateOpParamsBuilder.getOp(parts[3]),true ));
////						}
////						if (line.startsWith("UPDATE-file")){
////							updateLines.add(uopb.buildUpdateOpParamsObj(parts[1], parts[2], UpdateOpParamsBuilder.getOp(parts[3]),false ));
////						}*/
////					}
////					
//					
////				}
//				if("".equals(filename) || "".equals(id) || "".equals(agentDir) || -1==filesize ) return null;
//				Thread getter = new UpdateFilesGetter(id, filename, agentDir, filesize );
//				getter.start(); //Start the file download
////				OpParams result = new OpParams("UPDATE" ,null ,"UDP", id); //This is where the Measurments.type is set to UPDATE, add additional types in this method
////				result.setUpdateCommands(updateLines);
////				if (checkBuild(updateLines))
////				return result;
////				else return null;
//				
////			}else return null;
////		}catch (Exception e)
////		{
////			e.printStackTrace();
////			if (e instanceof ParseException)
////				throw (ParseException)e;
////			else
////				throw new ParseException(e.getMessage(), "");
////		}
//		return null;
//	}

/*	private static boolean checkBuild(LinkedList<UpdateOpParams> updateLines) {
		for(UpdateOpParams U: updateLines){
			if (UpdateOpParamsBuilder.ERROR == U.op) return false;
		}
		
		
		return true;
	}*/

	/**
	 * Parse a macro operation from a complete macro string
	 * Please note that a Macro has to be started and stopped using a special
	 * indication such as 'QBE-Start' or 'QBE-Stop'
	 * 
	 * 
	 * @param macroString the Macro string that was read upuntil now.
	 * @return OpParams object if the macro string is a complete macro 
	 * @throws IOException
	 * 
	 *  <QBE stamp="305419896">
	 *  <QPT iterations="200" numberofPackets="100" TimeBetweenTrains="1" destination="147.27.14.8" PacketSize="60" />
	 *  <WAIT time="1000" />
	 *  </QBE> 
	 */
	private static OpParams parseQBEOperation(Element QBEexp, short randomSource, short randomDest/*String macroString*/) throws IOException  , ParseException{
		int experimentUniqueId = -1;
		try
		{
			//System.out.println("Macro string :*" + macroString+"*");
//
			//System.out.println("--> matches");
//			StringReader macroReader = new StringReader(macroString);
//			BufferedReader bufReader = new BufferedReader(macroReader);
			
//			Element QBEexp = XMLUtil.getRootElement(macroString);			
			LinkedList<QPacketTrainOpParams> qptCommands = new LinkedList<QPacketTrainOpParams>();
			LinkedList<Integer> sleepPeriods =new LinkedList<Integer>();
			List<Node> QBEparts = XMLUtil.getNodeListAsList(QBEexp.getChildNodes());
			Element currentElm=null;
			
			experimentUniqueId = Integer.parseInt(QBEexp.getAttribute("stamp"));
			
			for (Node n:QBEparts)
			{
				try{
					currentElm= (Element)n;
				}catch(ClassCastException cce){
					if (n.getNodeType()==Node.TEXT_NODE) continue; //Node is a whitespace
				}
				// Parse athe experiment unique id :
//				if (line.startsWith("QBE-Start"))
//				{
//					String[] parts = line.split("\\s");
//					experimentUniqueId = Integer.parseInt(parts[1].trim());
//				}
//				else
				// Parse a QPT command of the form :
				// QBE-QPT <iterations> <inter-pt-delay(ms)> <number-of-trains> <experiment-unique-id> [IPs] [packet-size] [protocols] [tos's] ...
				// ... [ttl's] [source-ports] [dest-ports] 
				if (currentElm.getTagName().equals("QPT")){
					
//						String[] parts = line.split("\\s");
//						if (parts.length < MIN_QBE_PARAMETERS)
//							throw new ParseException("Not enough parameters in qpt command :" + parts.length +"<"+MIN_QBE_PARAMETERS,"" );
//						int counter = 1;
						int iterations = Integer.parseInt(currentElm.getAttribute("Iterations"));//parts[counter++].trim());
						int delayBetweenPT = Integer.parseInt(currentElm.getAttribute("TimeBetweenTrains"));//parts[counter++].trim());
						short numberOfPacketsInTrain = (short)Integer.parseInt(currentElm.getAttribute("NumberOfPackets"));//parts[counter++].trim());
						
						String[] packetIPs = ParserUtilities.parseStringArray(currentElm.getAttribute("Destination"));//parts[counter++].trim());
						int[] packetSizes = ParserUtilities.parseIntArray(currentElm.getAttribute("PacketSize"));//parts[counter++].trim());
						String temp = currentElm.getAttribute("Protocol");
						String[] protocols = {temp};//ParserUtilities.parseStringArray(currentElm.getAttribute("Protocol"));//parts[counter++].trim());
						int[] tosArray = ParserUtilities.parseIntArray(currentElm.getAttribute("Tos"));//parts[counter++].trim());
						int[] ttlsArray = ParserUtilities.parseIntArray(currentElm.getAttribute("TTLs"));//parts[counter++].trim());
						int[] sourcePorts = ParserUtilities.parseIntArray(currentElm.getAttribute("SourcePort"));//parts[counter++].trim());
						int[] destPorts = ParserUtilities.parseIntArray(currentElm.getAttribute("DestinationPort"));//parts[counter++].trim());
						int[] possibleArraySizes = new int[]{ numberOfPacketsInTrain , 1 };
						int[][] intArraysToVerify = new int[][]{  packetSizes ,  tosArray , ttlsArray ,  
								sourcePorts , destPorts };
						String[][] stringArraysToVerify = new String [][]{  packetIPs ,  protocols };
						
						if (packetIPs.length > 1)
							Loggers.getLogger().warning("Operation accepted with more than one IP");
						if (sourcePorts.length > 1)
							Loggers.getLogger().warning("Operation accepted with more than one source ports");
						if (destPorts.length > 1)
							Loggers.getLogger().warning("Operation accepted with more than one dest ports");
						
						// verify that the arrays are correct :
						ParserUtilities.verifyArraySizes(intArraysToVerify , possibleArraySizes);
						ParserUtilities.verifyArraySizes(stringArraysToVerify , possibleArraySizes);
						
						// create the operation details :
						QPacketTrainOpParams params = new QPacketTrainOpParams(iterations , delayBetweenPT,numberOfPacketsInTrain);
						for (short i=0; i<numberOfPacketsInTrain; i++){
							QPacketInTrain packetInTrain = new QPacketInTrain(
									getStringParameter(packetIPs , i) ,
									(short)getIntParameter(packetSizes , i) , 
									(short)getIntParameter(tosArray , i) , 
									(short)Protocol.getHeaderId(getStringParameter(protocols , i)) ,
									(short)getIntParameter(sourcePorts , i) ,
									(short)getIntParameter(destPorts , i) ,
									(short)getIntParameter(ttlsArray , i) ,
									(short)(i+1) ,
									numberOfPacketsInTrain);
/*										
							public QPacketInTrain (String IP , 
									short packetSize ,
									short packetTos ,
									short protocol ,
									short sourcePort ,
									short destPort ,
									short packetTTL , 
									short sequenceNumber,
									short trainsize){
*/
							
							
							
							packetInTrain.setAgentIndex(Measurements.getAgentIndex());
							params.addPacket(packetInTrain);
						}
						qptCommands.addLast(params);
						
					
				}
				// Parse a QBE-Wait command of the form :
				// QBE-Wait <sleep-period>
				if (currentElm.getTagName().equals("WAIT")){
				//	String[] parts = line.split("\\s");
					int sleepPeriod = Integer.parseInt(currentElm.getAttribute("time"));//Integer.parseInt(parts[1]);
					sleepPeriods.addLast(new Integer(sleepPeriod));
				}
			}
//			if (sleepPeriods.size() != qptCommands.size()-1)
//				throw new ParseException("QBE wait commands is invalid. must be QPT commans - 1" , "");
			OpParams result = new OpParams("QBE" ,null ,"UDP", randomSource, randomDest); //This is where the Measurments.type is set to QBE, add additional types in this method
			result.setQPTCommands(qptCommands);
			result.setSleepPeriods(sleepPeriods);
			result.setExperimentUniqueId(experimentUniqueId);
			return result;
		}
//		else
//			return null;
//		}
		catch (Exception e)
		{
			e.printStackTrace();
			if (e instanceof ParseException)
				throw (ParseException)e;
			else
				throw new ParseException(e.getMessage(), "");
		}
	}

	

	private static int getIntParameter(int[] parametersArray, int i) {
		if (parametersArray.length == 1)
			return parametersArray[0];
		return parametersArray[i];
	}

	private static String getStringParameter(String[] parametersArray, int i) {
		if (parametersArray.length == 1)
			return parametersArray[0];
		return parametersArray[i];
	}

//	/**
//	 * identifies a Macro operation line.
//	 * 
//	 * @param line
//	 * @return
//	 */
//	private boolean isMacroLine(String line) {
//		return line.startsWith("<QBE ");// || line.substring(0,4).equals("WAIT");
//	}
//
//	private boolean isUpdate(String line){		
//		return line.substring(0,7).equalsIgnoreCase("<UPDATE");
//	}
//	
	//todo
	/*
	 * Operation's syntax is: COMMAND IP [PROTOCOL [INITIAL_PORT] ] where:
	 * COMMAND is either PING or TRACEROUTE
	 * steger: COMMAND PACKETTRAIN NoROBIN DT_USEC PACKSIZE PROTOCOL PORT IP1 [IP2 [IP3] ...]
	 * treeroute : TREEROUTE Role[Client/Server] PeerAgentID DestAddress MeasurementTime 
	 * IP is the ip address
	 * PROTOCOL is either UDP or ICMP (default is UDP)
	 * INITIAL_PORT is the initial destination port for UDP packets (default is 33435)
	 */
/*	OpParams parseOperation(String str) throws ParseException
	{
		String[] parts = str.split("\\s");
		//3rd parameter (protocol) is optional
		// steger -- conditions of ParseExceptions() modified
//		boolean packettrain = parts[0].equalsIgnoreCase("PACKETTRAIN");
//		boolean treeroute = parts[0].equalsIgnoreCase("TREEROUTE");
		boolean tracerouteOrPing = (parts[0].equalsIgnoreCase("TRACEROUTE") || parts[0].equalsIgnoreCase("PING"));
//		boolean peerPacketTrain = parts[0].equalsIgnoreCase("PEERTRAIN");
//		boolean trainMeasurement = (parts[0].equalsIgnoreCase("TRAINMEASUREMENT"));
		boolean parisTraceRoute = (parts[0].equalsIgnoreCase("PARISTRACEROUTE")); //for right now this is actually Bi-QBE type (October 09)
		// check number of arguments :
		if ((tracerouteOrPing || parisTraceRoute) && (parts.length < 2) || (parts.length > 4))
			throw new ParseException("operation", "\"" + str + "\"");
		else
		if (packettrain && (parts.length < 7))
			throw new ParseException("operation", "\"" + str + "\"");
		else
			if (treeroute && (parts.length < 6))
				throw new ParseException("operation", "\"" + str + "\"");
			else
				if (peerPacketTrain && (parts.length < 6))
					throw new ParseException("operation", "\"" + str + "\"");

		if (packettrain)
			return parsePacketTrainOp(parts);
//		if (treeroute)
//			return parseTreerouteOp(parts);
		if (peerPacketTrain)
			return parsePeerTrainOp(parts);

		// standard operation parameters :
		String protocol = "";
		int initialPort = -1;
		if (parts.length == 3)
			protocol = parts[2];

		if (parts.length == 4)
		{
			protocol = parts[2];
			initialPort = Integer.parseInt(parts[3]);
		}
		System.out.println(str);
		return new OpParams(parts[0], parts[1], protocol, initialPort);
	}
*/
	/******
	 * parse a command type of the format :
	 * TREEROUTE Role[Client/Server] PeerAgentID DestAddress Protocol MeasurementTime
	 * @param parts
	 * @return
	 */
//	private OpParams parseTreerouteOp(String[] commandline) {		
//		OpParams params = new OpParams("TREEROUTE", commandline[3], commandline[4],34553);
//		params.treerouteRole = (commandline[1].equalsIgnoreCase("SERVER") ? TreerouteOp.SERVER : TreerouteOp.CLIENT );
//		params.peerAgentId = new String(commandline[2]);
//		params.measurementTime=Calendar.getInstance();
//		long measurementTime = Long.parseLong(commandline[5]);
//		params.measurementTime.setTimeInMillis(measurementTime);
//		// Options : 
//		// OVERRIDE - override treeroute incapability :
//		params.overrideTreerouteCapability = (commandline.length == 6 ? true  : 
//			!commandline[6].equalsIgnoreCase("OVERRIDE"));
//		return params;
//	}
//
//	private OpParams parsePacketTrainOp(String[] commandline) {
//		OpParams params = new OpParams("PACKETTRAIN", null , commandline[4],34553);
//		params.sourcePort = Integer.parseInt(commandline[5]);
//		params.numberOfRobins = Integer.parseInt(commandline[1]);
//		params.delay_usec = Integer.parseInt(commandline[2]);
//		params.packetsize = Integer.parseInt(commandline[3]);
//		params.ipList = new String[commandline.length - 6];
//		for (int i = 6; i < commandline.length; i++)
//		{
//			params.ipList[i - 6] = new String(commandline[i]);
//		}
//		return params;
//	}
//	
//	private OpParams parsePeerTrainOp(String[] commandline) {
//		OpParams params = new OpParams("PEERTRAIN", null , commandline[4],34553);
//		params.sourcePort = Integer.parseInt(commandline[5]);
//		params.numberOfRobins = Integer.parseInt(commandline[1]);
//		params.delay_usec = Integer.parseInt(commandline[2]);
//		params.packetsize = Integer.parseInt(commandline[3]);
//		params.ipList = new String[commandline.length - 6];
//		for (int i = 6; i < commandline.length; i++)
//		{
//			params.ipList[i - 6] = new String(commandline[i]);
//		}
//		return params;
//	}

	static String insertLineBreaks(String str){
		StringTokenizer strTk = new StringTokenizer(str.trim());
		StringBuffer strAssmbled = new StringBuffer(strTk.nextToken()+" ");
		String line;
		
		while(strTk.hasMoreTokens()){
			line = strTk.nextToken().trim();
			if(line.startsWith("PING") || line.startsWith("TRACEROUTE")|| line.startsWith("PARISTRACEROUTE")){
				strAssmbled.append("\n"+line+" ");
			}else strAssmbled.append(line+" ");
		}		
		return strAssmbled.toString();
		
	}

	//todo
	String parseID(String str) throws ParseException
	{
		return str;
	}

	//todo
	String parseHostIP(String str) throws ParseException
	{
		return str;
	}
	/**
	 * @return
	 */
	public TreeMap<Integer, Vector<Task>> getTasks()
	{
		return prioritizedTasks;
	}

}


/*
//todo - where should it be defined?
class ParseException extends Exception
{
	String type;
	String mismatched;

	ParseException(String aType, String aMismatched)
	{
		type = aType;
		mismatched = aMismatched;
	}
	 (non-Javadoc)
	 * @see java.lang.Object#toString()
	 
	public String toString()
	{
		return "Could not parse script.\tType: " + type + " mismatched: " + mismatched;
	}

}*/

