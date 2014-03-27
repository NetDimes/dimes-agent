package dimes.util.debug;

import java.util.HashMap;

public abstract class Tester {

	
	/**
	 * @param source Engine or Gui
	 * @param testName classname and method
	 * @param success test succeed
	 * @param msg test result
	 * @return
	 */
	protected String testResultFormatter(String source, String testName, boolean success, String msg ){
		StringBuilder result = new StringBuilder("<Test>\n");
		result.append("\t<TestInfo>\n");
		result.append("\t\t<source>"+source+"</source>\n");
		result.append("\t\t<name>"+testName+"</name>\n");
		result.append("\t\t<success>"+String.valueOf(success)+"</success>\n");
		result.append("\t</TestInfo>\n");
		result.append("\t<resultSet>\n\t\t"+msg+"\n\t</resultSet>\n");
		result.append("</test>\n");
		//debug only:
		System.out.println(result.toString());
		return result.toString();
	}
	
	public static String msgFormatter(String root, HashMap<String, String> message){
		StringBuilder result = new StringBuilder("<"+root+">\n");
		for(String key:message.keySet()){
			result.append("\t\t\t<"+key+">"+message.get(key)+"</"+key+">\n");
		}
		result.append("\t\t</"+root+">\n");
		return result.toString();
	} 
	
}
