package dimes.scheduler;

/**This class replaces the import of ParseException from
 * skinlf.jar, which is not used in this Agent, since it
 * has no GUI. It matches both constructor types, and responds
 * the same way. 
 * 
 * @author user
 *
 */
@SuppressWarnings("serial")
public class ParseException extends Exception {
	
	String type;
	String mismatched="";
	
	public ParseException(String message){
		super(message);
		type=message;
	}


	public ParseException(String message, String aMismatched)
	{
		super(message);
		type = message;
		mismatched = aMismatched;
	}

	 
	public String toString()
	{
		return "Could not parse script.\tType: " + type + " mismatched: " + mismatched;
	}
}
