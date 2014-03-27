package dimes.measurements.results;

import java.sql.Timestamp;

import dimes.measurements.Measurements;

public class TreerouteServerResults extends TreerouteResults {

	public String clientServerTracerouteResults;
	public String clientDestTracerouteResults;
	private String clientName;
	private int clientClockOffset;
	private int clientClockError;
	
	public TreerouteServerResults(String source, String clientIp, String host, String destIp, long destAddress, int s, String proto, Timestamp ts, String localTime, int n) {
		super(source, clientIp, host, destIp, destAddress, s, proto, ts, localTime, n);
		super.role="SERVER";
	}
	
	public void setClientDestTracerouteString(String clientDestTracerouteResults){
		this.clientDestTracerouteResults = clientDestTracerouteResults;
	}
	
	public void setClientServerTracerouteString(String clientServerTracerouteResults){
		this.clientServerTracerouteResults = clientServerTracerouteResults;
	}
	

	public void setPeerAgentID(String clientName) {
		this.clientName = clientName;
	}

	public String formatOperationDetails(String tabs) {
		String rawOperationDetails = super.formatResultDetails(tabs);
		String clientTraceXml = Measurements.toXML(super.expID , super.scriptID , super.priority , super.getPeerAgentTraceResults());
		String destTraceXml = Measurements.toXML(super.expID , super.scriptID , super.priority , super.getDestTraceResults());
		
		String clocksString = 
			tabs+"<ClientClockOffset>" +this.clientClockOffset+ "</ClientClockOffset>" + "\n"+
			tabs+"<ClientClockError>" +this.clientClockError+ "</ClientClockError>" + "\n"+
			tabs+"<ServerClockOffset>" +getMyAgentClockOffset()+ "</ServerClockOffset>" + "\n"+
			tabs+"<ServerClockError>" +getMyAgentClockError()+ "</ServerClockError>" + "\n";
		String clientIndexString = 
			tabs+"<ClientAgentIndex>" + clientName + "</ClientAgentIndex>\n";
		
		return rawOperationDetails
		+clocksString + 
		clientIndexString+
		"<ServerDestTrace>\n" + destTraceXml +"</ServerDestTrace>\n"+
		"<ServerClientTrace>\n" + clientTraceXml + "</ServerClientTrace>\n"
		+this.clientDestTracerouteResults + "\n"+
		this.clientServerTracerouteResults + "\n";
	}

	public void setClientClockOffset(int clientClockOffset) {
		this.clientClockOffset = clientClockOffset;
	}

	public void setClientClockError(int clientClockError) {
		this.clientClockError = clientClockError;
	}
	

}
