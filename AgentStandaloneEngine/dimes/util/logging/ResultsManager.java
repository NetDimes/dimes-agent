package dimes.util.logging;

import java.io.UnsupportedEncodingException;

public abstract class ResultsManager {

	private static StringBuffer resultsString= null; //use String buffer for thread safe operation in the future
	private static boolean resultsPooled = true; //Have the results been pooled by send/receive? This signals buffer can be reset, so default to true at start
	private static boolean resultsPending = false; //Are the results to be pooled?
	
	private static synchronized void resetBuffer(){
		if (null!=resultsString){
			if(resultsPooled){
				resultsString= new StringBuffer("<Results>");
			}else{
				//TODO: Handle this. Announce results?
			}		
		}else resultsString= new StringBuffer("<Results>");	
	}
	
	public static synchronized String poolResults(){
		if(resultsPending){
			resultsPending=false;
			resultsPooled=true;
			resultsString.append("</Results>");
			try {
				return new String(resultsString.toString().getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Encoding problem", e);
			}
		}else{
			try {
				return new String("<Results></Results>".getBytes(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Encoding problem", e);
			} //Empty results string
		}
	}
	
	public static synchronized int appendResult(String result){
		if(null==resultsString || resultsPooled) resetBuffer();
		resultsString.append(result);
		resultsPending=true;
		resultsPooled=false;
		System.out.println("memory message length: "+resultsString.length()); //TODO: Remove, debug
		return resultsString.length();
	}
	
	public static boolean hasResultsPending(){
		return resultsPending;
	}
	
}
