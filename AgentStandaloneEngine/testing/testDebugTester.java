package testing;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

//import dimes.util.debug.Tester;
import dimes.util.properties.PropertiesBean;
import dimes.util.XMLUtil;

import org.dom4j.Document;
import org.dom4j.DocumentException;
//import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class testDebugTester {

	private static String propertiesFileName = "C:\\program files\\DIMES\\Agent\\Classes\\base\\conf\\properties.xml";
	private static Document propertiesDoc = null;
	private static org.dom4j.Element propertiesRoot = null;
	
	/*
	private String source;
	private String name;
	private boolean success;
	private String msg;
	private HashMap<String, String> msgDetails= new HashMap<String, String>();
	
	@Before
	public void setUp() throws Exception {
		source="Engine";
		name = "Unit Test";
		success = true;
	//	msg="<testDebugTester>\n\t<setUp> successful </setUp>/n</testDebugTester>";
		msgDetails.put("setUp", "successful");
		msg=Tester.msgFormatter("testDebugTester", msgDetails);
	}

	@Test
	public void testTestResultFormatter() {
//		assertNotNull(Tester.testResultFormatter(source, name, success, msg));
	//	fail("Not yet implemented");
	}*/

	public void loadProperties() throws DocumentException
	{
		SAXReader reader = new SAXReader();
		//***********************************************************
		// The next line was changed to use a File object. Reason:
		//reader.read kept looking for a url called "C" (from c:\)
		// BoazH, 11/12/2008
		//***********************************************************
		try{
			testDebugTester.propertiesDoc = reader.read(new File(testDebugTester.propertiesFileName));
			testDebugTester.propertiesRoot = testDebugTester.propertiesDoc.getRootElement();
		}catch(IOException ioe){
			System.out.println("Problem loading properties.xml, agent will exit");
			ioe.printStackTrace();
			System.exit(-1);
		}
	}
	
	@Before
	public void setUp(){
		try {
			loadProperties();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAsXml(){
		String Dom4J=testDebugTester.propertiesRoot.asXML();
		String w3c="";
		org.w3c.dom.Element w3cElem;
		try {
			w3cElem = XMLUtil.getRootElement(new File(testDebugTester.propertiesFileName));
			w3c = XMLUtil.nodeToString(w3cElem);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Dom4J +"\n\n");
		System.out.println(w3c);
		assert(Dom4J.equals(w3c));
	}
	
}
