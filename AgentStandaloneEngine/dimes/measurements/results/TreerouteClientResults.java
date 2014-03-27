package dimes.measurements.results;

import java.sql.Timestamp;
import java.util.Vector;

import dimes.measurements.Measurements;

public class TreerouteClientResults extends TreerouteResults {


	public TreerouteClientResults(String source, String clientIp, String host, String destIp, long destAddress, int s, String proto, Timestamp ts, String localTime, int n) {
		super(source, clientIp, host, destIp, destAddress, s, proto, ts, localTime, n);
		this.setRawVector(new Vector());
		super.role="CLIENT";
	}

	public String formatOperationDetails(String tabs) {
		// format the traceroute operation strings:
		String serverTraceXml = Measurements.toXML(super.expID , super.scriptID , super.priority , super.getPeerAgentTraceResults(),tabs+"\t\t");
		String destTraceXml = Measurements.toXML(super.expID , super.scriptID , super.priority , super.getDestTraceResults(),tabs+"\t\t");
		String operationDetails = super.formatResultDetails(tabs);
		return operationDetails+
		tabs+"\t<ClientServerTrace>\n"+
		serverTraceXml + 
		tabs+"\t</ClientServerTrace>\n"+
		tabs+"\t<ClientDestTrace>\n"+
		destTraceXml + 
		tabs+"\t</ClientDestTrace>\n"+
		"\t<treeroute>\n" +
		"\t\t<clockOffset>"+super.getMyAgentClockOffset()+"</clockOffset>\n"+
		"\t\t<clockError>"+super.getMyAgentClockError()+"</clockError>\n"+
		"\t\t<RawDetails>\n"+
		super.formatRawDetails("\t\t\t")+
		"\t\t</RawDetails>\n"+
		"\t</treeroute>\n";
	}
	


}
