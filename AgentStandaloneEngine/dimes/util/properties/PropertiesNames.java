/*
 * Created on 07/04/2005
 */
package dimes.util.properties;

/**
 * @author anat
 */
public class PropertiesNames
{
	/***************** agent properties *******************************/
	public static final String PROP_FILENAME_PROPERTY = "dimes.propertyFile";

	public static final String USER_NAME = "userName";
	public static final String AGENT_NAME = "agentName";
	public static final String EMAIL = "email";
	public static final String COUNTRY = "country";
	public static final String COUNTRY_FILE = "countryFile";
	public static final String GROUP = "group";
	public static final String GROUP_OWNER = "groupOwner";
	public static final String IP = "IP";
	public static final String MAC = "mac";
	public static final String AGENT_VERSION = "agentVersion";
	public static final String AUTOMATIC_UPDATE = "init.policies.automaticUpdate";

	public static final String BASE_DIR = "init.dirs.base";
	public static final String OUTGOING_DIR = "init.outgoing_dir";
	public static final String LOG_DIR = "init.log_dir";
	public static final String RESULTS_DIR = "init.results_dir";
	public static final String HISTORY_DIR = "init.dirs.history";
	public static final String RESOURCES_DIR = "init.dirs.resources";
	public static final String JAR_DIR = "init.jarDir";

	public static final String AFTER_USAGE = "init.policies.after_usage";

	public static final String COMM_TRIALS_NUM = "init.policies.comm.num_of_trials";
	public static final String CONNECT_TIMEOUT = "init.policies.comm.connectTimeout";
	public static final String READ_TIMEOUT = "init.policies.comm.readTimeout";
	public static final String USE_PROXY = "init.policies.comm.useProxy";
	public static final String PROXY_HOST = "init.policies.comm.proxyHost";
	public static final String PROXY_PORT = "init.policies.comm.proxyPort";
	public static final String COMM_COMMPORT = "init.policies.comm.commport";
	public static final String COMM_COMMPORT_ENABLED = "init.policies.comm.commport_enable";
	
	
	public static final String SCHEDULER_DELAY = "init.policies.scheduler.delay";
	public static final String SCHEDULER_PERIOD = "init.policies.scheduler.period";
	public static final String SCHEDULER_TURBO = "init.policies.scheduler.turbo";

	public static final String KEEPALIVE_DELAY = "init.policies.keepalive.delay";
	public static final String KEEPALIVE_PERIOD = "init.policies.keepalive.period";

	public static final String KEEPMETAOPALIVE_DELAY = "init.policies.keepmetaopalive.delay";
	public static final String KEEPMETAOPALIVE_PERIOD = "init.policies.keepmetaopalive.period";

	public static final String COMM_STATE_DELAY = "init.policies.comState.delay";
	public static final String COMM_STATE_PERIOD = "init.policies.comState.period";
	
	public static final String FILE_TRANSFER_RATE = "init.policies.fileTransfer.rate";
	public static final String UPDATE_INFO_RATE = "init.policies.updateInfo.rate";
	public static final String SHOW_SPLASH = "init.policies.gui.showSplash";
	public static final String START_DELAY = "init.policies.startDelay";
	public static final String STARTUP = "init.policies.startup";

	public static final String PROTOCOL_INFO = "init.policies.measurements.protocol";
	public static final String DEFAULT_PROTOCOL = "init.policies.measurements.protocol.default";
	public static final String UDP_BLOCKED = "init.policies.measurements.protocol.UDP.blocked";

	public static final String NETWORK_INTERFACE = "init.networkInterface";
	public static final String SKIN_NAME = "init.skinName";
	public static final String SKIN_FILE = "init.skinFile";
	public static final String ICMP_BLOCKED = "init.policies.measurements.protocol.ICMP.blocked";
	
	public static final String STATISTICS_UPDATE_MINUTE = "init.policies.statistics_update_minute";

	public static final String TREEROUTE_INFO = "init.policies.measurements.treeroute";
	public static final String TREEROUTE_NTP= "init.policies.measurements.treeroute.NTP";
	public static final String TREEROUTE_NAT = "init.policies.measurements.treeroute.NAT";
	public static final String TREEROUTE_SPOOFER= "init.policies.measurements.treeroute.Spoofer";
	public static final String TREEROUTE_CLIENT= "init.policies.measurements.treeroute.ClientEnabled";
	public static final String TREEROUTE_SERVER= "init.policies.measurements.treeroute.ServerEnabled";
	public static final String TREEROUTE_PROHIBITED = "init.policies.measurements.treeroute.SpoofingProhibited";
	
	public static final String LOG_CONFIG_FILE = "init.files.log_config_file";
	public static final String DERFAULT_IN_FILE = "init.files.default_in_file";

	public static final String SERVER_URL = "init.urls.serverURL";
	public static final String COMPRESSED_SERVER_URL = "init.urls.compressedServerURL";
	public static final String DIMES_LINK = "init.urls.DimesLink";
	public static final String BASIC_TRACE_DEST = "init.urls.basicTraceDest";
	public static final String KEEPALIVE_URL = "init.urls.keepaliveURL";
	public static final String SECURE_KEEPALIVE_URL = "init.urls.secureKeepaliveURL";
	public static final String KEEPMETAOPALIVE_URL = "init.urls.keepmetaopaliveURL";
	public static final String UPDATE_URL = "init.urls.updateURL";
	public static final String SECURE_UPDATE_URL = "init.urls.secureUpdateURL";
	public static final String PROPERTIES_UPDATE_URL = "init.urls.propertiesUpdateURL";
	public static final String SECURE_PROPERTIES_UPDATE_URL = "init.urls.securePropertiesUpdateURL";
	public static final String SCRIPT_UPDATE_URL = "init.urls.scriptUpdateURL";
	public static final String SECURE_SCRIPT_UPDATE_URL = "init.urls.secureScriptUpdateURL";
	public static final String AS_INFO_URL = "init.urls.ASInfoURL";
	public static final String GROUP_NAMES_URL = "init.urls.groupNamesURL";
	public static final String RENDEZVOUS_URL = "init.urls.rendezvousServerURL";
	public static final String STATISTICS_URL = "init.urls.agentStatisticsURL";
//	public static final String HELP_URL = "init.urls.helpURL";
	public static final String FORUMS_URL = "init.urls.forumsURL";
	public static final String FAQ_URL = "init.urls.faqURL";
	
	public static final String LOGGER_NAME = "init.names.logger";
	public static final String RESULT_WRITER_NAME = "init.names.resultWriter";

	public static final String DEBUG_STATE = "state.debug";
	public static final String REGISTERED_STATE = "state.registered";
	public static final String HAS_PSWD_STATE = "state.hasPswd";
	public static final String ENABLE_SKIN_STATE = "state.enableSkin";
	
	public static final String AGENT_ID = "id";
	public static final String LAST_UPDATE_ID = "lastUpdate"; 

	/******************* server-agent comm properties ***************/
/*	public static final String PENNY = "Penny";
	public static final String SCRIPT = "Script";
	public static final String EXPERIMENT_ID = "ExID";
	public static final String SCRIPT_ID = "id";
	public static final String PRIORITY = "Priority";
	public static final String TASK = "Task";
	public static final String DONE_OPS = "doneOps";
	public static final String TOTAL_OPS = "totalOps";
	public static final String TASK_NUM = "taskNum";
	public static final String STATUS = "status";
	public static final String PROTOCOL = "protocol";
	public static final String HEADER = "header";
	public static final String DEFAULT = "default";
	public static final String UDP = "UDP";
	public static final String ICMP = "ICMP";
	public static final String BLOCKED = "blocked";
	public static final String ASK_FOR_WORK = "askForWork";
	public static final String ASK_FOR_AGENT_INDEX = "askForAgentIndex";
	public static final String SERVER_TIME = "ServerTime"; */
	
	public static final String PENNY = "PENNY";
	public static final String SCRIPT = "SCRIPT";
	public static final String EXPERIMENT_ID = "EXID";
	public static final String SCRIPT_ID = "ID";
	public static final String PRIORITY = "PRIORITY";
	public static final String TASK = "TASK";
	public static final String DONE_OPS = "DONEOPS";
	public static final String TOTAL_OPS = "TOTALOPS";
	public static final String TASK_NUM = "TASKNUM";
	public static final String STATUS = "STATUS";
	public static final String PROTOCOL = "PROTOCOL";
	public static final String HEADER = "HEADER";
	public static final String DEFAULT = "DEFAULT";
	public static final String UDP = "UDP";
	public static final String ICMP = "ICMP";
	public static final String BLOCKED = "BLOCKED";
	public static final String ASK_FOR_WORK = "ASKFORWORK";
	public static final String ASK_FOR_AGENT_INDEX = "ASKFORAGENTINDEX";
	public static final String SERVER_TIME = "SERVERTIME"; 
	

	/******************* util methods *****************************/
	public static String getOpeningTag(String tagName)
	{
		return "<" + tagName + ">";
	}

	public static String getClosingTag(String tagName)
	{
		return "</" + tagName + ">";
	}

	public static String addAttributeToTag(String tagStr, String attribute, String value)
	{
		String tagWithAttribute = tagStr.substring(0, tagStr.lastIndexOf('>'));
		tagWithAttribute += " " + attribute + "=\"" + value + "\"";
		tagWithAttribute += ">";
		return tagWithAttribute;
	}
}