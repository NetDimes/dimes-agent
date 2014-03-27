package dimes.util.update;

import java.io.File;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Hashtable;

import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/**A builder that creates UpdateOpParam objects. Centralizes some information that may be required 
 * for UpdateUpParams creation.  
 * 
 * @author user
 *
 */
public class UpdateOpParamsBuilder {
	public enum ops {UPDATE,NEW,DELETE, DELETEEMPTY, ERROR};
//	public static final int UPDATE = 1;
//	public static final int NEW = 2;
//	public static final int DELETE = 3;
//	public static final int ERROR = 4;
	public static Hashtable<String, Integer> names = new Hashtable<String, Integer>();
	
	private String baseDir;
	
//	static{
//		names.put("UPDATE", UPDATE);
//		names.put("NEW", NEW);
//		names.put("DELETE", DELETE); //no need to put in ERROR, it is internal only 
//	}
	
	/**
	 * @param baseDirP the base dir of the AGENT. note this is NOT the location of the "base" directory
	 * rather this is the home directory of the Agent. Ex: c:\program files\DIMES\Agent
	 */
	public UpdateOpParamsBuilder(String baseDirP){
		if (!(null==baseDirP || "".equals(baseDirP)))baseDir=baseDirP;
		else {
			try {
		
			
			String temp=PropertiesBean.getProperty(PropertiesNames.BASE_DIR);
			temp = temp.substring(0,temp.indexOf("gent")+4);
			baseDir=(new File(temp)).getCanonicalPath();
			
			} catch (NoSuchPropertyException e) {
				baseDir=dimes.platform.PlatformDependencies.os==dimes.platform.PlatformDependencies.WINDOWS?"C:\\Program Files\\DIMES\\Agent":null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	/**Builds an UpdateOpParams object that represents one action of the
	 * Update script. (Update/delete/new/deleteempty)
	 * 
	 * @param locationP
	 * @param nameP
	 * @param opP
	 * @param isDirP
	 * @return
	 */
	public UpdateOpParams buildUpdateOpParamsObj(String locationP, String nameP, String opP, boolean isDirP){
		String location = "";
		String name = "";
		int op;
		if ((locationP==null || "".equals(locationP) || "\"\"".equals(locationP))) location=baseDir; //note that this is the base dir for the Agent (c:\program file\dimes\Agent) not the "base" dir
		else{
			if (baseDir.endsWith(File.separator)) location=baseDir+locationP;
			else location=baseDir+File.separator+locationP;
		}
		if ((null==nameP)||("".equals(nameP))){return null;} //no filename is an error
			else name=nameP;		
		location = processDirectoryMarks(location);
		if(!verifyLegalDir(location)) return null;
		if((opP.equalsIgnoreCase("UPDATE")&&isDirP)|| opP.equalsIgnoreCase("DELETEEMPTY")&& !isDirP )return null;
	return new UpdateOpParams(location, name, ops.valueOf(opP), isDirP);	
	}
	
	public UpdateOpParams buildUpdateOpParamsObj(String nameP, String opP, boolean isDirP){
		return this.buildUpdateOpParamsObj(null, nameP, opP, isDirP);
	}
	
	public static int getOp(String in){
		return names.get(in.toUpperCase().trim());
	}
	
    public String getUpdateDir(){
    	return(baseDir+File.separator+"update");
    }
	
	private String processDirectoryMarks(String in){
//		return in.replaceAll(java.util.regex.Matcher.quoteReplacement("\\"), File.separator );
	    final StringBuilder result = new StringBuilder();
	    final StringCharacterIterator iterator = new StringCharacterIterator(in);
	    char character =  iterator.current();
	    while (character != CharacterIterator.DONE ){
	     
	      if (character == '\\') {
	         result.append(File.separator);
	      }
	       else {
	        result.append(character);
	      }

	      
	      character = iterator.next();
	    }
	    return result.toString();

	}
	
	/**Checks the final value of the update directory to see if it's leagal. 
	 * Prevents a situation where someone gives an update dir like:
	 * c:\program files\DIMES\Agent\..\..\..\windows
	 * 
	 * @param dir
	 * @return
	 */
	private boolean verifyLegalDir(String dir){
		File f=new File(dir);
		try {
			String actualLocation = f.getCanonicalPath();
			return (dir.startsWith(baseDir));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return false;
		
	}
}
