/*
 * Created on 18/12/2005
 *
 */
package dimes.measurements;


/**
 * @author Ohad Serfaty
 *
 */
public class MeasurementsResultFormatter
{

//	/**
//	 * @param res
//	 * @param exception
//	 * @return
//	 */
//	public static String formatOperationDetails(Results res, String tabs , String exception)
//	{
//		String result="";
//		result += tabs + "<ScriptLineNum>" + res.getScriptLine() + "</ScriptLineNum>" + "\n";
//		result += tabs + "<StartTime>" + res.getTimeStamp() + "</StartTime>" + "\n";
//		result += tabs + "<LocalStartTime>" + res.getLocalTime() + "</LocalStartTime>" + "\n"; //7.3.04
//		result += tabs + "<CommandType>" + res.getCommand() + "</CommandType>" + "\n";
//		result += tabs + "<Protocol>" + res.getProtocol() + "</Protocol>" + "\n";//2.2.05
//		result += tabs + "<SourceName>" + res.getSource() + "</SourceName>" + "\n";
//		result += tabs + "<SourceIP>" + res.getSourceIp() + "</SourceIP>" + "\n";
//		result += tabs + "<DestName>" + res.getHost() + "</DestName>" + "\n"; //7.3.04
//		result += tabs + "<DestIP>" + res.getDestIp() + "</DestIP>" + "\n";//7.3.04
//		result += tabs + "<DestAddress>" + res.getDestAddress() + "</DestAddress>" + "\n";//16.12.04
//		result += tabs + "<NumOfTrials>" + res.getRequestedNumOfTrials() + "</NumOfTrials>" + "\n";
//		result += tabs + "<Success>" + res.isSF() + "</Success>" + "\n";
//		result += tabs + "<reachedDest>" + res.isReachedDest() + "</reachedDest>" + "\n";
//		result += tabs + "<Exceptions>" + res.getException() + exception + "</Exceptions>" + "\n";
//		return result;
//	}
//	
//	public static String formatOperationDetails(TreerouteResults res, String tabs , String exception){
//		String regularStr = formatOperationDetails((Results)res , tabs , exception);
//		String resultStr = regularStr+
//		"<clientClockOffset>" +res.getClientClockOffset()+ "</clientClockOffset>" + "\n"+
//		"<clientClockError>" +res.getClientClockError()+ "</clientClockError>" + "\n"+
//		"<serverClockOffset>" +res.getServerClockOffset()+ "</serverClockOffset>" + "\n"+
//		"<serverClockError>" +res.getServerClockError()+ "</serverClockError>" + "\n";
//		return resultStr;
//	}
//	
//
//	public static String rawDetailsToXML( Results res , String tabs){
//		Vector rawVector = res.getRawVector();
//		String result="";
//		int size = rawVector.size();
//		
//				for (int rawIndex = 0; rawIndex < size; ++rawIndex)
//				{
//					RawResultHolder detail = (RawResultHolder) rawVector.get(rawIndex);
//					if (detail == null)
//						Loggers.getLogger().finest("detail is null");
//					result += toXML(detail, tabs);
//				}
//		return result;
//	}



	
//	public static String toXML(RawResultHolder res, String tabs)
//	{
//		return res.toXML(tabs);
//	}
//
//	
//	
//	public static String toXML(NetHost theNetHost, String tabs)
//	{
//		String result = "";
//		result += tabs + "<Detail>\n";
//		tabs += "\t";
//
//		result += tabs + "<sequence>" + theNetHost.getSequence() + "</sequence>" + "\n";
//		result += tabs + "<hopAddress>" + theNetHost.getHopAddress() + "</hopAddress>" + "\n";
//		result += tabs + "<hopAddressStr>" + theNetHost.getHopAddressStr() + "</hopAddressStr>" + "\n";
//		result += tabs + "<hopNameStr>" + theNetHost.getHopNameStr() + "</hopNameStr>" + "\n";
//		result += tabs + "<lostNum>" + theNetHost.getLostNum() + "</lostNum>" + "\n";
//		result += tabs + "<bestTime>" + theNetHost.getBestTime() + "</bestTime>" + "\n";
//		result += tabs + "<worstTime>" + theNetHost.getWorstTime() + "</worstTime>" + "\n";
//		result += tabs + "<avgTime>" + theNetHost.getAvgTime() + "</avgTime>" + "\n";
//		result += tabs + "<exception>" + theNetHost.getException() + "</exception>" + "\n";
//		result += tabs + "<kernelTS>" + theNetHost.isUsingKernelTS() + "</kernelTS>" + "\n";
//
//		Vector alternatives = theNetHost.getAlternatives();
//		if (alternatives == null)
//			return result + tabs + "</Detail>" + "\n";
//		alternatives.trimToSize();
//		int validAlternative = -1;
//		if (alternatives.size() != 0)
//		{
//			for (int i = 0; i < alternatives.size(); ++i)
//				if (alternatives.get(i) != null)
//					validAlternative = i;
//		}
//		if (validAlternative != -1)
//		{
//			result += tabs + "<Alternatives>\n";
//			for (int i = 0; i <= validAlternative; ++i)
//			{
//				result += MeasurementsResultFormatter.toXML((NetHost) alternatives.get(i), tabs + "\t");
//			}
//			result += tabs + "</Alternatives>" + "\n";
//		}
//
//		tabs = tabs.substring(1);
//		result += tabs + "</Detail>" + "\n";
//		return result;
//	}

	
}
