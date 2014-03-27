package dimes.util.debug;

public class debugTesterResults {

	private boolean success;
	private String message;
	private String root;
	
	public debugTesterResults (boolean succeessP, String rootP, String messageP){
	
		success=succeessP;
		root=rootP;
		message=messageP;
		
	}
	
	public String toString(){
		return "<success>"+success+"</success>\n<root>"+root+"</root>\n<message>\n\t"+message+"\n</message>\n";
	}
}
