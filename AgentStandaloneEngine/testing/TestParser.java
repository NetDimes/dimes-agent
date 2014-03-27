package testing;

import static org.junit.Assert.*;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.TreeMap;
import java.util.Vector;


import org.dom4j.DocumentException;
import org.junit.Before;
import org.junit.Test;

import dimes.measurements.operation.MeasurementOp;
import dimes.scheduler.Parser;
import dimes.scheduler.Task;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;

public class TestParser {

	String script;
	Reader scriptReader;
	Parser parser;
	static TreeMap ST;
	
	@Before
	public void setUp() throws Exception {
		PropertiesBean.init("C:\\Program Files\\DIMES\\Agent\\Classes\\Base\\conf\\properties.xml");
		String test = PropertiesBean.getProperty(PropertiesNames.AGENT_ID);
		File scriptFile = new File("C:\\Program Files\\DIMES\\Agent\\update\\script.txt");
//		
//		script = "<Penny>\n<Script id=\"CustomTEST1\" ExID=\"CustomTEST1\">\n" +
//				"<Priority>URGENT</Priority>\n" +
//				"ping 147.27.14.7 " +
//				"UPDATE-start ID=123 "+
//				"UPDATE-dir \"\" test NEW "+
//				"UPDATE-file testfile UPDATE "+
//				"UPDATE-end "+
//				"</Script>\n" +
//				"</Penny>\n"; 

		scriptReader = new BufferedReader(new FileReader(scriptFile));
		parser = new Parser();
	}

//	@Test
//	public void testParser() {
//		
//		fail("Not yet implemented");
//	}

	@Test
	public void testParse() {
		try {
			parser.parse(scriptReader);
		} catch (Exception e) {
			
			e.printStackTrace();
			fail("Exepction");
		}
		assertNotNull(parser.getTasks());
		ST=parser.getTasks();
	}
	
	@Test 
	public void testSyntaxTree(){	
//		ST.
//		for(int i=0;i<ST.size();i++){
//			Object t = ST.get(i);
//			String T = t.getClass().getCanonicalName();
//			System.out.println(T);
			Vector<Task> T=(Vector<Task>)ST.get(ST.firstKey());
//			MeasurementOp op =(MeasurementOp)T.get(0).getNextOp();
//			op.execute();
//		}
	}

}
