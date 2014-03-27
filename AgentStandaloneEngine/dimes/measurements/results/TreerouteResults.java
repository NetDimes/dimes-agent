/*
 * Created on 18/12/2005
 *
 */
package dimes.measurements.results;

import java.sql.Timestamp;
import java.util.Vector;

/**
 * @author Ohad Serfaty
 *
 */
public abstract class TreerouteResults extends Results
{

	/**
	 *  results of my agent's traceroute to the destination
	 */
	public Results destTraceResults;
	
	/**
	 *  result of my agent's traceroute to the Peer Agent.
	 */
	private Results peerAgentTraceResults;
	
	private int myAgentClockOffset;
	private int myAgentClockError;
	private Vector rawVector;

	protected String expID;
	protected String scriptID;
	protected String priority;
	protected String role;
	

	/**
	 * @param source
	 * @param sourceIp
	 * @param host
	 * @param destIp
	 * @param destAddress
	 * @param s
	 * @param comm
	 * @param proto
	 * @param ts
	 * @param localTime
	 * @param n
	 */
	public TreerouteResults(String source, String clientIp, String host, String destIp, 
			long destAddress, int s,  String proto, Timestamp ts, String localTime, int n)
	{
		super(source, clientIp, host, destIp, destAddress, s, "TREEROUTE", proto, ts, localTime, n);
	}

	/**
	 * @return
	 */
	public void  setMyAgentClockOffset(int value)
	{
		this.myAgentClockOffset=value;
	}
	
	public void  setMyAgentClockError(int value)
	{
		this.myAgentClockError=value;
	}
	
	public void setMeasurementTaskDetails(String expID , String scriptID , String priority){
		this.expID = expID;
		this.scriptID = scriptID;
		this.priority = priority;
	}
	
	/**
	 * @return
	 */
	public int  getMyAgentClockOffset()
	{
		return myAgentClockOffset;
	}
	
	public int  getMyAgentClockError()
	{
		return this.myAgentClockError;
	}


	public Vector getRawVector()
	{
		return rawVector;
	}
	/**
	 * @param vector
	 */

	public void setRawVector(Vector vector)
	{
		rawVector = vector;
	}
	
	public void setDestTraceResults(Results destTracerouteResults){
		this.destTraceResults = destTracerouteResults;
	}
	
	public void setPeerAgentTraceResults(Results peerAgentTracerouteResults){
		this.peerAgentTraceResults = peerAgentTracerouteResults;
	}

	public Results getDestTraceResults(){
		return this.destTraceResults ;
	}
	
	public Results getPeerAgentTraceResults(){
		return this.peerAgentTraceResults ;
	}
	
	
	public String formatResultDetails(String tabs){
		String superResultDetails = super.formatResultDetails(tabs);
		// Add the task details to the raqw results format : 
		String myResults = 
			tabs+"<ExID>"+this.expID+"</ExID>\n" +
			tabs+"<ScriptID>"+this.scriptID+"</ScriptID>\n" +
			tabs+"<Priority>" + this.priority + " </Priority>\n"+
			tabs+"<Role>"+role+"</Role>\n"; 
		return myResults + superResultDetails;
	}
	
//	public String formatOperationDetails(String tabs){
//		String regularStr = super.formatResultDetails(tabs);
//		String resultStr = regularStr+
//		tabs+"<ClientClockOffset>" +getClientClockOffset()+ "</ClientClockOffset>" + "\n"+
//		tabs+"<ClientClockError>" +getClientClockError()+ "</ClientClockError>" + "\n"+
//		tabs+"<ServerClockOffset>" +getServerClockOffset()+ "</ServerClockOffset>" + "\n"+
//		tabs+"<ServerClockError>" +getServerClockError()+ "</ServerClockError>" + "\n"+
//		tabs+"<clientAgentIndex>" +clientAgentIndex+ "</clientAgentIndex>" + "\n"+
//		tabs+residualTracerouteXML+"\n";
//		return resultStr;
//	}
	
//	/**
//	 * @param peerAgentIndex
//	 */
//	public void setClientAgentIndex(int clientAgentIndex)
//	{
//		this.clientAgentIndex = clientAgentIndex;
//	}

	public String formatRawDetails(String tabs) {
		String result="";  //tabs+"<Treeroute>\n";
		int size = rawVector.size();
		
				for (int rawIndex = 0; rawIndex < size; ++rawIndex)
				{
					RawResultHolder detail = (RawResultHolder) rawVector.get(rawIndex);
					if (detail == null)
						;
//						System.out.println("detail is null");
					else
						result += detail.toXML(tabs);
				}
//		result+=(tabs+"</Treeroute>\n");
		return result;
	}

	public boolean hasRawDetails() {
		return true;
	}
	
}
