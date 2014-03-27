//package dimes.measurements.results;
//
//import java.sql.Timestamp;
//import java.util.StringTokenizer;
//
//public class ResultUtils {
//
//	public static String getLocalHost(){
//		
//	}
//	
//	public static String getLocalIP(){
//		
//	}
//	
//	public String getLocalCurrentTime(){
//		
//	}
//	
//	public String getDestHost(String destination){
//		
//	}
//	
//	public String getDestIP(String destination){
//		
//	}
//	
//	public Timestamp getCurrentTimeStamp(){
//		
//	}
//	
//	public static long ipToLong(String ip)
//	{
//		StringTokenizer prefixDotTokenizer = new StringTokenizer(ip, ".");
//		int ipPart1 = Integer.parseInt(prefixDotTokenizer.nextToken());
//		int ipPart2 = Integer.parseInt(prefixDotTokenizer.nextToken());
//		int ipPart3 = Integer.parseInt(prefixDotTokenizer.nextToken());
//		int ipPart4 = Integer.parseInt(prefixDotTokenizer.nextToken());
//
//		int result = (ipPart1 << 24) + (ipPart2 << 16) + (ipPart3 << 8) + ipPart4;
//
//		if (result < 0)
//		{
//			result = 2 ^ 32 - result;
//		}
//
//		return result;
//	}
//	
//}
