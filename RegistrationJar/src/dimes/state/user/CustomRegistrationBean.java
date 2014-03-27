package dimes.state.user;
/**
 * Created 11/2008
 * 
 * CustomRegistrationBean looks for a file called registration.xml, and reads out of it properties
 * that can identify this user without having to prompt the user themselves. If the file does not
 * exit, it assumes a truly anonymous user.
 * 
 *    @author BoazH
 *    @version 0.5.1
 */
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
//import org.dom4j.Document;
//import org.dom4j.DocumentException;
//import org.dom4j.Element;
//import org.dom4j.io.SAXReader;
import org.w3c.dom.Element;
import dimes.util.XMLUtil;

public abstract class CustomRegistrationBean {

	private static String regName;
	private static String regGroup;
//	private static Document doc;
	private static Element rootElement;
	
	public static void init(File loc){
		if (loc.exists()){
			try{
//			    SAXReader xmlReader = new SAXReader();
//			    doc = xmlReader.read(loc);
			    rootElement = XMLUtil.getRootElement(loc);// doc.getRootElement();
			    regName = XMLUtil.getChildElementByName(rootElement, "Name").getTextContent();
			    regGroup = XMLUtil.getChildElementByName(rootElement, "Group").getTextContent();//rootElement.selectSingleNode("Group").getText();
/*			}catch(DocumentException de){
				System.out.println("CustomRegistrationBean - Document Exception");
				de.printStackTrace();*/
			}catch(MalformedURLException mue){
				System.out.println("CustomRegistrationBean - MalformedURLException Exception");
				mue.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			regName="Anonymous";
			regGroup=null;
		}
	}
	
	//If this class ever gets more complicated, these getters should be replaced with a
	//system based of property name (like PropertiesBean. For right now, this should be enough	
	public static String getRegName(){
		return regName;
	}
	
	//If there's a group name specified, this will return it. Otherwise, return null so that 
	//the server doesn't register the agent to any group. 
	public static String getGroup(){		
		if(!(regGroup.compareToIgnoreCase("None")==0)) return regGroup;
		else return null;
	}
}
