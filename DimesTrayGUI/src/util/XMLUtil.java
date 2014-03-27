package util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author BoazH
 *
 */
public abstract class XMLUtil {
	
	private static DocumentBuilderFactory dbf=null;
	private static DocumentBuilder db=null;
	private static XPath xp=null;
	

	/**Returns the first ELEMENT with name str, while ignoring whitespace nodes
	 * 
	 * @param elm 
	 * @param str
	 * @return the String value of a child node of Element elm that has a name str
	 * or null if no match
	 */
	public static Element getChildElementByName(Element elm, String str){
		NodeList nList = elm.getElementsByTagName(str);
		return nList==null?null:(Element)nList.item(0);
	}
	
	/**a Convenience method that turns a NodeList into a LinkedList of Nodes 
	 * which allows for iteration
	 * @param elm
	 * @param str
	 * @return
	 */
	public static List<Node> getNodeListAsList(NodeList nList){//Element elm, String str){
		List<Node> aList = new LinkedList<Node>();
		for (int i=0;i<nList.getLength();i++) aList.add(nList.item(i)); 
		return aList;
	}
	
	public static Element getRootElement (File XmlFile) throws IOException{
		FileInputStream fis = null;
//		try {
			fis = new FileInputStream(XmlFile);
/*		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
		return getRootElement(fis);
	}
	
	/**treats string as a complete XML, returns root element
	 * @param str
	 * @return
	 */
	public static Element getRootElement (String str){
		return XMLUtil.getRootElement(new ByteArrayInputStream(str.getBytes()));	
	}
	
	public static Element getRootElement (InputStream is){
		return getRootElement(new InputSource(is));
	}
	
	public static Element getRootElement(InputSource is){
		Document doc = null;
		
		//Initialize only one document builder and factory. Saves the over head of doing it every time.
		if (null==XMLUtil.dbf){
			try {
				XMLUtil.dbf = DocumentBuilderFactory.newInstance();
				XMLUtil.db=dbf.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		
		try {
			doc=XMLUtil.db.parse(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc.getDocumentElement();
	}
	
	
	/**Get a NodeLIst of child nodes, ignoring nodes that have White space
	 * @param e
	 * @return
	 */
	public static NodeList getChildNodesNoWS(Element e){
		NodeList children = e.getChildNodes();
		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node child = children.item(i);
			if (child instanceof Text && ((Text) child).getData().trim().length() == 0) {
				e.removeChild(child);
			}
		
		}
		return e.getChildNodes();
	}
	
	
	/**Returns the entire node (including the node itself, child nodes, and text)
	 *  as a String
	 * 
	 * 
	 * @param node
	 * @return
	 */
	public static String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
		System.out.println("nodeToString Transformer Exception");
		}
		return sw.toString().trim();
		}
	
	/** Returns the entire value of a node (child nodes and text) as a String
	 * @param node
	 * @return
	 */
	public static String nodeChildrenToString(Node node){
		StringWriter sw = new StringWriter();
		try {
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		NodeList nl = node.getChildNodes();
		for (int i=0;i<nl.getLength();i++)
		t.transform(new DOMSource(nl.item(i)), new StreamResult(sw));
		} catch (TransformerException te) {
		System.out.println("nodeToString Transformer Exception");
		} 
		return sw.toString().trim();
	}
	
	public static List<Node> evaluateXPath(String path, Element elm){
		NodeList list=null;
		if(null==xp) xp= XPathFactory.newInstance().newXPath();
		try {
			list = (NodeList) xp.evaluate(path, elm, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
				e.printStackTrace();
		}
		return XMLUtil.getNodeListAsList(list);
	}
	
	public static Node selectSingleNode(String xpath, Element elm){
		NodeList list = null;
		list = (NodeList) evaluateXPath(xpath, elm);
		if (0<list.getLength()) return list.item(0);
		return null;		
	}
	
	public static boolean isTextXML(String text){
		text=text.trim();
		if(!(text.startsWith("<") && text.endsWith(">")))return false; //If text doesn't begin with "<" it's not XML
		
		int firstClose = text.indexOf(">");
		int lastOpen = text.lastIndexOf("<");
		
		if(lastOpen==0 && text.lastIndexOf("/")==firstClose-1) return true; //Example "<DIMES />
		if (text.substring(1, firstClose+1).equals(text.substring(lastOpen+2)))return true; //<XML> blah </XML>
		
		return false;
	}
	
	public static String getXMLStringFromStream(BufferedReader br){
		try{
			return getXMLStringFromStreamThrowing(br);
		}catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getXMLStringFromStreamThrowing (BufferedReader br) throws IOException{
		StringBuilder sb = new StringBuilder("");
		String line;
		String elementName;
		String elementEnd;
	
			line = br.readLine();
			line = line.substring(line.indexOf("<"));
			elementName = line.substring(1, line.indexOf(">"));
			elementEnd = "</"+elementName+">";
			if (line.contains(elementEnd)) return line;
			do{
				sb.append(line);
				line=br.readLine();
			}while(!line.contains(elementEnd));
			sb.append(line.substring(0,line.indexOf(elementEnd)+elementEnd.length()));

		
		return sb.toString();
	}
}
