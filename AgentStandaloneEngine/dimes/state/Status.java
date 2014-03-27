package dimes.state;

import dimes.util.properties.PropertiesNames;

/**CLass which stores contains information about the current Agent
 * 
 * @author anat
 */
public class Status
{
	private Version codeVersion;
	private ProtocolInfo protocolInfo;
	private final TaskManagerInfo taskManagerInfo;
	private TreerouteInfo treerouteInfo;
	private DefaultScriptInfo defaultScriptInfo;

	
	/** Constructor. Gets the version and TaskManager info from params,
	 * queries the rest independently.
	 * 
	 * @param versionStr
	 * @param aTaskManagerInfo
	 */
	public Status(String versionStr, TaskManagerInfo aTaskManagerInfo)
	{
		this.taskManagerInfo = aTaskManagerInfo;
		this.codeVersion = new Version(versionStr);
		this.protocolInfo = new ProtocolInfo();
		this.treerouteInfo = new TreerouteInfo();
		this.defaultScriptInfo = new DefaultScriptInfo();
	}

	/**
	 * @return
	 */
	public Version getCodeVersion()
	{
		return codeVersion;
	}

	/**
	 * @param version
	 */
	public void setCodeVersion(Version version)
	{
		codeVersion = version;
	}

	/**
	 * Returns the current status of the Agent, including
	 * code version, protocolInfo, taskManagerInfo, treeRouteInfo, And defaultScriptInfo
	 * 
	 * @return complete current status in XML form. 
	 */
	public String toXML()
	{
		return PropertiesNames.getOpeningTag(PropertiesNames.STATUS) + "\n" +
				this.codeVersion.toXML() + "\n"
				+ this.taskManagerInfo.getInfo()+ "\n" +
				this.protocolInfo.toXML() + "\n" +
				this.treerouteInfo.toXML() +"\n" +
				this.defaultScriptInfo.toXML() +"\n" +
				 PropertiesNames.getClosingTag(PropertiesNames.STATUS);
	}
}