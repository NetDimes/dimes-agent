//package dimes.util.gui;
//
//import java.io.BufferedInputStream;
//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import java.net.URL;
//import java.net.URLConnection;
//import java.util.ArrayList;
//import java.util.Vector;
//import java.util.logging.Logger;
//
//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;
//
//import org.xml.sax.Attributes;
//import org.xml.sax.SAXException;
//import org.xml.sax.helpers.DefaultHandler;
//
//import dimes.util.logging.Loggers;
//import dimes.util.properties.PropertiesBean;
//import dimes.util.properties.PropertiesNames;
//import dimes.util.properties.PropertiesBean.NoSuchPropertyException;
//
//public class StatisticsReader {
//	private static final Logger logger = Loggers.getLogger(StatisticsReader.class);
//	
//	private Vector vectorResult = new Vector();
//
//	private Vector vectorTotal = new Vector();
//
//	private Vector vectorAgentTotal = new Vector();
//
//	private Vector vectorAgentWeekly = new Vector();
//
//	private Vector vectorTmp = new Vector();
//
//	private String sUserName = "";
//
//	private String sGroupName = "";
//	
//	// Singleton:
//	private static StatisticsReader reader = null;
//
//	public static StatisticsReader getInstance() {
//		if (reader == null) {
//			try {
//				reader = new StatisticsReader();
//			} catch (NoSuchPropertyException e) {
//				logger.severe("Can't run the Statistics update due to problem in the Agent's properties file. Please check.");
//				e.printStackTrace();
//				reader = null;
//			}
//		}
//		return reader;
//	}
//	public Vector getVectorAgentTotal() {
//		return vectorAgentTotal;
//	}
//
//	public Vector getVectorAgentWeekly() {
//		return vectorAgentWeekly;
//	}
//
//	public String getUserName() {
//		return sUserName;
//	}
//
//	public String getGroupName() {
//		return sGroupName;
//	}
//
//	public Vector getTotal() {
//		return vectorTotal;
//	}
//
//	public Object[][] getTotalArray(){
//		Vector<Vector> tmpVec = getTotal();
//		Object[][] totalArray=new Object[tmpVec.size()][4];
//		for (int i=0;i<tmpVec.size();i++)
//			totalArray[i]=tmpVec.get(i).toArray();
//		return totalArray;
//	}
//	/*
//	 * The private constructor is being called from the getInstance method.
//	 * In case of Exception it throws it to the caller to handle.
//	 * 
//	 * 
//	 * @throws NoSuchPropertyException
//	 */
//	private StatisticsReader() throws NoSuchPropertyException {
//	/* 
//	 * The statisticsURL initialization moved to this.updateStatistics to avoid the situation
//	 * that in creating the singleton in first run after installation the url got an empty
//	 * String as PropertiesNames.USER_NAME and the statistics couldn't be initialized.
//	 * 	statisticsURL = PropertiesBean
//			.getProperty(PropertiesNames.STATISTICS_URL)
//			+ "=\'"
//			+ PropertiesBean.getProperty(PropertiesNames.USER_NAME)
//			+ "\'";	
//	*/		
//	}
//
//	public String updateStatistics() throws Exception {
//			String userName = PropertiesBean.getProperty(PropertiesNames.USER_NAME);
//			if( userName == null || userName.length() == 0 ){
//				logger.warning("Agent can not update statistics info. Another try will be made later.");
//				return "No user name found in your properties file";
//			} 
//			String statisticsURL = PropertiesBean
//			.getProperty(PropertiesNames.STATISTICS_URL)
//			+ "=\'"
//			+ userName
//			+ "\'";	
//		
//		URL url = null;
//		URLConnection javaSite = null;
//		InputStream input = null;
//		BufferedInputStream in = null;
//		StringBuffer sbUserStatisticFile = null;
//		SAXParser saxParser = null;
//		byte[] rssFeedByteArray = null;
//		ByteArrayInputStream rssFeedByteArrayInputStream = null;
//
//		try {
//			url = new URL(statisticsURL);
//			javaSite = url.openConnection();
//			input = javaSite.getInputStream();
//			in = new BufferedInputStream(input);
//			sbUserStatisticFile = new StringBuffer();
//			int ich = 0;
//			while ((ich = in.read()) != -1)
//				sbUserStatisticFile.append((char) ich);
//			if( sbUserStatisticFile.length() == 0)
//				return "No response from Statistics Server";
//			String response = sbUserStatisticFile.toString().toUpperCase();
//			if( response.contains("<ERROR>") ) {
//				String errorMessage = response.substring("<ERROR>".length(), (response.length() - "</ERROR>".length() - 2));
//				return errorMessage;
//			}
//			
//			sbUserStatisticFile.insert(0,
//					"<?xml version=\'1.0\' encoding=\'UTF-8\'?> ");
//			
//			// Parse the input
//			saxParser = SAXParserFactory.newInstance().newSAXParser();
//			rssFeedByteArray = sbUserStatisticFile.toString().getBytes("UTF-8");
//			rssFeedByteArrayInputStream = new ByteArrayInputStream(
//					rssFeedByteArray);
//			saxParser.parse(rssFeedByteArrayInputStream,
//					new StatisticsHandler());
//			rssFeedByteArrayInputStream.close();
//		} finally {
//			if( in != null )
//				in.close();
//			if( input != null )
//				input.close();
//		}
//		return null;
//	}
//
//	private class StatisticsHandler extends DefaultHandler {
//		StringBuffer sbcharacters = new StringBuffer();
//
//		ArrayList alHeaders = new ArrayList();
//
//		StringBuffer sbAgentName = new StringBuffer();
//
//		public void startDocument()//	    throws SAXException
//		{
//			vectorTotal.clear();
//			vectorAgentTotal.clear();
//			vectorAgentWeekly.clear();
//
//			try {
//				emit("<?xml version='1.0' encoding='UTF-8'?>");
//				nl();
//			} catch (SAXException e) {
//				System.out.println(e.getMessage());
//			}
//
//		}
//
//		public void endDocument()//	    throws SAXException
//		{
//			// The follow code because changes at GUI statistic screen
//			for (int count = 0; ((count < 2) && (vectorResult != null)); count++) {
//				Vector vectorTmp = new Vector();
//				if( vectorResult != null && vectorResult.size() > 0 ) {
//					vectorTmp = (Vector) vectorResult.remove(0);
//					vectorTmp.remove(0);
//					vectorTmp.remove(0);
//				}
//			
//				if (count == 0)
//					vectorTmp.insertElementAt("Total", 0);
//				else
//					vectorTmp.insertElementAt("Weekly", 0);
//				vectorTotal.add(new Vector(vectorTmp));
//			}
//
//			// The follow code because chaiges at GUI statistic screee - the code shered for two piece
//			// Vector vectorAgentTotal = all total agent data and
//			// Vector vectorAgentWeekly = all weekly agent data.
//			if (vectorResult != null) {
//				while (vectorResult.size() > 0) {
//					Vector vectorTmp = new Vector((Vector) vectorResult
//							.remove(0));
//					if (vectorTmp.get(1).toString().equalsIgnoreCase(
//							"total-statistics")) {
//						vectorTmp.remove(1);
//						vectorAgentTotal.add(new Vector(vectorTmp));
//					} else if (vectorTmp.get(1).toString().equalsIgnoreCase(
//							"weekly-statistics")) {
//						vectorTmp.remove(1);
//						vectorAgentWeekly.add(new Vector(vectorTmp));
//					}
//				}
//			}
//		}
//
//		public void startElement(String namespaceURI, String lName,
//				String qName, Attributes attrs) throws SAXException {
//			String eName = lName; // element name
//			if ("".equals(eName))
//				eName = qName; // namespaceAware = false
//			emit("<" + eName);
//			if (attrs != null) {
//				for (int i = 0; i < attrs.getLength(); i++) {
//					String aName = attrs.getLocalName(i); // Attr name
//					if ("".equals(aName))
//						aName = attrs.getQName(i);
//					emit(" ");
//					emit(aName + "=\"" + attrs.getValue(i) + "\"");
//				}
//			}
//			emit(">");
//			sbcharacters.setLength(0);
//			if (attrs != null) {
//				for (int i = 0; i < attrs.getLength(); i++) {
//					if (attrs.getValue(i).length() > 0) {
//						/////System.out.println("***** startElement = " + " (qName = " + qName + ") attrs.getValue(" + i + " ) = " + attrs.getValue(i)  + "\t");
//						alHeaders.clear();
//						alHeaders.add(attrs.getValue(i));
//
//						if (qName.length() < "agent".length()) {
//							sbAgentName.setLength(0);
//							sbAgentName.append("general");
//						} else if (qName.substring(0, "agent".length())
//								.toString().trim().equalsIgnoreCase("agent")) {
//							sbAgentName.setLength(0);
//							sbAgentName.append(attrs.getValue(i));
//						}
//					}
//					if (sUserName.length() < 1)
//						sUserName = attrs.getValue(i);
//				}
//			}
//			if (alHeaders.size() > 0)
//				alHeaders.add(eName);
//		}
//
//		public void endElement(String namespaceURI, String sName, String qName)
//				throws SAXException {
//			emit("</" + sName + ">");
//			if ((qName.trim().equalsIgnoreCase("measurements"))
//					|| (qName.trim().equalsIgnoreCase("ASNodesNum"))
//					|| (qName.trim().equalsIgnoreCase("ASEdgesNum"))) {
//				if (sbcharacters.length() > 0)
//					vectorTmp.add(new Integer(sbcharacters.toString().trim()));
//				else
//					vectorTmp.add(new Integer(0));
//			} else if ((qName.trim().equalsIgnoreCase("total-statistics"))
//					|| (qName.trim().equalsIgnoreCase("weekly-statistics"))) {
//				vectorTmp.add(0, qName);
//				if (!sbAgentName.equals("general"))
//					vectorTmp.add(0, sbAgentName.toString());
//				else
//					vectorTmp.add(0, sbAgentName.toString());
//				vectorResult.add(new Vector(vectorTmp));
//				vectorTmp.clear();
//			}
//		}
//
//		public void characters(char buf[], int offset, int len) //throws SAXException
//		{
//			try {
//				String s = new String(buf, offset, len);
//				emit(s);
//				if (sGroupName.length() < 1) {
//					if ((s.trim().length() > 0))
//						sGroupName = s.trim();
//				}
//				if ((s.trim().length() > 0) && (sbcharacters.length() < 1))
//					sbcharacters.append(s);
//			} catch (SAXException e) {
//				System.out.println(e.getMessage());
//			}
//			//System.out.println("characters = " + s);
//		}
//
//		//===========================================================
//		// Utility Methods ...
//		//===========================================================
//		// Wrap I/O exceptions in SAX exceptions, to
//		// suit handler signature requirements
//		private void emit(String s) throws SAXException {
//		}
//
//		private void nl() throws SAXException {
//		} // Start a new line
//	}
//}
