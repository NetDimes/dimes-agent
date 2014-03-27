package agentGuiComm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;


public abstract class MessageTypes {
	
//-------------------------------------------------------------------------------------
// General Language Strings
//-------------------------------------------------------------------------------------
	//Main Types:
	public static final String SEND_TYPE_RESULTS = "RESULTS";
	public static final String SEND_TYPE_SCRIPT= "SCRIPT";
	public static final String SEND_TYPE_LOG = "LOG";
	public static final String SEND_TYPE_PROPERTY = "PROPERTY";
	public static final String SEND_TYPE_DIMES = "DIMES";
	public static final String SEND_TYPE_SHOW="SHOW";
	public static final List<String> SEND_TYPES_ARRAY = new ArrayList<String>(Arrays.asList(new String[]{SEND_TYPE_RESULTS,SEND_TYPE_SCRIPT,SEND_TYPE_LOG,SEND_TYPE_PROPERTY,SEND_TYPE_DIMES, SEND_TYPE_SHOW}));

	
	//Subtypes:
	public static final String SUB_GOODBYE = "GOODBYE";
	public static final String SUB_PAUSE = "PAUSE";
	public static final String SUB_SET = "SET";
	public static final String SUB_GET = "GET";
	public static final String SUB_VERSION = "VERSION";
	public static final String SUB_VALUE = "VALUE";
	public static final String SUB_LEVEL = "LEVEL";
	
	
//---------------------------------------------------------------------------------------
//	Agent to Client values
//---------------------------------------------------------------------------------------


/*Masks for the types of log messages a client wants to receive
 * These values corrospond to the values of java.util.logging.Level so
 * you can get the level by invoking Level.parse (example Level.parse(1000) 
 * would give you a Level object of level severe 
 */
	public static final int LOGGER_LEVEL_SEVERE = 1000;
	public static final int LOGGER_LEVEL_WARNING = 900;
	public static final int LOGGER_LEVEL_INFO = 800;
	public static final int LOGGER_LEVEL_CONFIG = 700;
	public static final int LOGGER_LEVEL_FINE = 500;
	public static final int LOGGER_LEVEL_FINER = 400;
	public static final int LOGGER_LEVEL_FINEST = 300;

//----------------------------------------------------------------------------------------

	/*Structure of XML from Client to Agent:
	 *
	<Property> - Commands to deal with the properties file which is managed by the Agent
	  <set> - Allows setting properties from the properties frame into the properties file
	  <get> - Allows retrieving properties from file

	<script> - Allows sending a user script to the Agent

	<show> - Controls what information the Agent will send to the GUI
	  <results> - Type of results the user wants to see (All/none/mine/server)
	  <scripts> - Information on current running script (None/NameOnly/AllContent)
	  <log> - Log messages (none/status/error/exception)

	<command> - Commands to the Agent 
	  <Shutdown>
	  <restart>
	  <pause>
	  <resume>

	<update> - Commands to the Update mechanism
	  <check> - Check for updates
	  <do> - do update
	 */
	
	/*Masks for the types of results a client wants to receive
	 * note that these have corresponding entries in the lookup table
	 * 
	 */
		public static final int MASK_NONE = 0;
		public static final int MASK_ALL = 1;
		public static final int MASK_MINE=2;
		public static final int MASK_SERVER=3;
//--------------------------------------------------------------------------------------------

//	public static final int QUERY = 4;
//	public static final int QUERY_REPLY = 5;
	public static final int LOG = 6;
	public static final int GRAPH = 7;
	public static final int COMMAND = 8;

	
	public static final int USER_TRACE = 100;
	public static final int USER_CRAWL_URL=200;
	public static final int USER_CRAWL_STOP=999;
	public static final int USER_LOAD_URL=300;
	public static final int USER_LOAD_FILE=400;

	public static final int USER_PROTOCOL_ICMP=1;
	public static final int USER_PROTOCOL_UDP=2;
	public static final int USER_PROTOCOL_TCP=3;
	
	public static final int USER_OPERATION_PING=10;
	public static final int USER_OPERATION_TRACEROUTE=20;
	
	public static final int COMMAND_DEST_AGENT = 1;
	public static final int COMMAND_DEST_PROPERTIES = 2;
	public static final int COMMAND_DEST_REGISTRATION = 3;
	
	
	public static final String[] LOGGER_NAMES = new String[]{"","finest","finer","fine", "config", "info", "warning","severe" };
//------------------------------------------
	
	//Strings Used in sending XML 

	
	public static final String ACK_TYPE_SHOW = "SHOW";
	public static final String ACK_TYPE_PROPERTY = SEND_TYPE_PROPERTY;
	
	public static final String SEND_MESSAGE_LOG = "LOG";
	public static final String SEND_MESSAGE_USERSCRIPT = "USERSCRIPT";
	public static final String SEND_MESSAGE_ERROR = "ERROR";
	public static final String SEND_MESSAGE_EXCEPTION = "EXCEPTION";
	public static final String SEND_MESSAGE_UPDATE = "UPDATE";
	
	public static final int SEND_RESULT_SOURCE_SERVER = 0;
	public static final int SEND_RESULT_SOURCE_MINE = 1;
	
	//Types of messages for "show" command
	public static final int TYPE_DIME = 0;
	public static final int TYPE_RESULTS = 1;
	public static final int TYPE_SCRIPT = 2;
	public static final int TYPE_MESSAGE = 3;
	public static final int MESSAGE_SUBTYPE_LOG=31;
	public static final int MESSAGE_SUBTYPE_USERSCRIPT=32;
	public static final int MESSAGE_SUBTYPE_ERROR=33;
	public static final int MESSAGE_SUBTYPE_EXCEPTION=34;
	public static final int MESSAGE_SUBTYPE_UPDATE=35;
	public static final int TYPE_PROPERTY =4;
	public static final int TYPE_STATE=5;

	
	public static final int RECEIVE_DIMES=0;
	public static final int RECEIVE_PROPERTY = 1;
	public static final int RECEIVE_SCRIPT = 2;
	public static final int RECEIVE_SHOW = 3;
	public static final int RECEIVE_COMMAND=4;
	
	public static final String GET_PROPERTY_GET = "GET";
	public static final String GET_PROPERTY_SET = "SET";
	public static final String GET_SCRIPT_NAME = "NAME";
	public static final String GET_SHOW_RESULTS = "RESULTS";
	public static final String GET_SHOW_SCRIPTS = "SCRIPTS";
	public static final String GET_SHOW_MESSAGES = "MESSAGES";
	
	public static final String GET_COMMAND_SHUTDOWN = "SHUTDOWN";
	public static final String GET_COMMAND_RESTART = "RESTART";
	public static final String GET_COMMAND_PAUSE = "PAUSE";
	public static final String GET_COMMAND_RESUME = "RESUME";
	public static final String GET_COMMAND_UPDATE = "UPDATE";
	public static final String GET_COMMAND_UPDATE_CHECK ="CHECK";
	public static final String GET_COMMAND_UPDATE_DO = "DO";
	
	public static Hashtable<Integer, String> names = new Hashtable<Integer,String>();
	public static Hashtable<String, Integer> lookup= new Hashtable<String, Integer>();
	static{
		 names.put(USER_PROTOCOL_ICMP, "ICMP");
		 names.put(USER_PROTOCOL_TCP,"TCP");
		 names.put(USER_PROTOCOL_UDP, "UDP");
		 
		 names.put(USER_OPERATION_TRACEROUTE, "TRACEROUTE");
		 names.put(USER_OPERATION_PING, "PING");
		 
		 lookup.put("DIMES", RECEIVE_DIMES);
		 lookup.put("PROPERTY", RECEIVE_PROPERTY);
		 lookup.put("SCRIPT", RECEIVE_SCRIPT);
		 lookup.put("PENNY", RECEIVE_SCRIPT);
		 lookup.put("SHOW", RECEIVE_SHOW);
		 lookup.put("COMMAND", RECEIVE_COMMAND);
		 
		 lookup.put("NONE", MASK_NONE);
		 lookup.put("ALL", MASK_ALL);
		 lookup.put("MINE", MASK_MINE);
		 lookup.put("SERVER", MASK_SERVER);
		 
	 }
	 
	
		
		
}
