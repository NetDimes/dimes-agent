package dimes.measurements.results;

import java.sql.Timestamp;

import dimes.measurements.QBEHost;

public class DimesQBEResults extends Results {

	public DimesQBEResults(String source, String sourceIp, String host, String destIp, long destAddress, int s, String comm, String proto, Timestamp ts, String localTime, int n) {
		super(source, sourceIp, host, destIp, destAddress, s, comm, proto, ts, localTime, n);
		measurementResult = null;
	}

	private QBEHost measurementResult;
	
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

		s += measurementResult.toString();
		return s;
	}

	public QBEHost getResult()
	{
		return measurementResult;
	}
	/**
	 * @param vector
	 */

	public void setResult(QBEHost result)
	{
		measurementResult = result;
	}
	
	
	public String formatRawDetails(String tabs) {
		return measurementResult.toXML(tabs);
	}

	public boolean hasRawDetails() {
		return measurementResult != null;
	}

	public String formatOperationDetails(String tabs) {
		return super.formatResultDetails(tabs);
	}

}
