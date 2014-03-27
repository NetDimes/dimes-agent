package dimes.AgentGuiComm.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Element;

import dimes.util.XMLUtil;

public class DimesXMLInputStream extends DataInputStream {

	public DimesXMLInputStream(InputStream in) {
		super(in);
	}

	public Element readElement() throws IOException{
		return XMLUtil.getRootElement(this.readUTF());
	}
}
