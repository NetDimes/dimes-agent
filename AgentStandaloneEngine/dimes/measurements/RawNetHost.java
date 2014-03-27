package dimes.measurements;

/**
 * @author Ohad Serfaty
 *
 * This class implements The most basic representation of a host in the network,
 * consisting of  an Address and a hostname. squence is for convinience only.  
 * 
 */
public class RawNetHost {

	protected int sequence = -1;
	protected long hopAddress = 0;
	protected String hopAddressStr; //by ip
	protected String hopNameStr; // by name
	
	/**
	 * @return Returns the hopAddressStr.
	 */
	public String getHopAddressStr()
	{
		return this.hopAddressStr;
	}
	/**
	 * @return Returns the hopNameStr.
	 */
	public String getHopNameStr()
	{
		return this.hopNameStr;
	}
	
	/**
	 * @return Returns the sequence.
	 */
	public int getSequence()
	{
		return this.sequence;
	}
	
	public void setSequence(int value){
		this.sequence = value;
	}
	
	/**
	 * @return Returns the hopAddress.
	 */
	public long getHopAddress()
	{
		return this.hopAddress;
	}
	
	/**
	 * @param theHopAddress The hopAddress to set.
	 */
	public void setHopAddress(long theHopAddress)
	{
		this.hopAddress = theHopAddress;
	}
	/**
	 * @param theHopAddressStr The hopAddressStr to set.
	 */
	public void setHopAddressStr(String theHopAddressStr)
	{
		this.hopAddressStr = theHopAddressStr;
	}
	/**
	 * @param theHopNameStr The hopNameStr to set.
	 */
	public void setHopNameStr(String theHopNameStr)
	{
		this.hopNameStr = theHopNameStr;
	}
	
}
