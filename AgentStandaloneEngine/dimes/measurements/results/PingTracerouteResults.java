package dimes.measurements.results;

import java.sql.Timestamp;
import java.util.Vector;

import dimes.measurements.NetHost;

public class PingTracerouteResults extends Results {

	public PingTracerouteResults(String source, String sourceIp, String host, String destIp, long destAddress, int s, String comm, String proto, Timestamp ts, String localTime, int n) {
		super(source, sourceIp, host, destIp, destAddress, s, comm, proto, ts, localTime, n);
	}

	private Vector<NetHost> rawVector;
	
	public String toString()
	{
		s = "Results of Pinging the host " + this.host + " with ip address " + this.destIp + " / " + this.destAddress + " from source " + this.source
				+ " with ip address " + this.sourceIp + " The Command is " + command + "\n";
		if (SF)
		{
			s = s + "The request was a success!\n";
		}
		else
		{
			s = s + "The request was a failure!\n";
		}

		for (int i = 0; i < this.rawVector.size(); ++i)
		{
			NetHost aHost = this.rawVector.get(i);
			s += aHost.toString();
		}
		return s;
	}

	public Vector<NetHost> getRawVector()
	{
		return rawVector;
	}
	/**
	 * @param vector
	 */

	public void setRawVector(Vector<NetHost> vector)
	{
		rawVector = vector;
	}
	
	
	public String formatRawDetails(String tabs) {
		Vector rawVector = getRawVector();
		String result = "";
		int size = rawVector.size();

		for (int rawIndex = 0; rawIndex < size; ++rawIndex) {
			RawResultHolder detail = (RawResultHolder) rawVector.get(rawIndex);
			if (detail == null)
				;
			else
				result += detail.toXML(tabs);
		}
		return result;
	}

	public boolean hasRawDetails() {
		return this.getRawVector()!=null;
	}

	public String formatOperationDetails(String tabs) {
		return super.formatResultDetails(tabs);
	}

}
