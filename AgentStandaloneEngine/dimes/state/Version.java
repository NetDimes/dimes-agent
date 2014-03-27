package dimes.state;
/*
 * Created on 05/07/2004
 */
// todo - should have fields for version characteristics
/**Holds the version of the Agent, returns either as a string or XML 
 * 
 * @author anat
 */
public class Version
{
	private String verStr;

	/**Constructor
	 *  
	 * @param str The Agent version
	 */
	public Version(String str)
	{
		this.verStr = str;
	}

	/**
	 * @return "<version>" + verStr + "</version>"
	 */
	public String toXML()
	{
		return "<version>" + verStr + "</version>";
	}

	public Version parseVersion(String str)
	{
		return new Version(str);
	}

	/**
	 * @return value of version string
	 */
	public String getVerStr()
	{
		return verStr;
	}

	/**
	 * @param string version as string
	 */
	public void setVerStr(String string)
	{
		verStr = string;
	}

	/**Compares the param version to the stored version 
	 * 
	 * @param aVersion
	 * @return int
	 */
	public int compareTo(Version aVersion)
	{
		return this.getVerStr().compareTo(aVersion.getVerStr());
	}

}