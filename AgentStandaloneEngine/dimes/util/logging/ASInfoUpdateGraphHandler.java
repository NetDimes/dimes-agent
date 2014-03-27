/*
 * Created on 22/04/2004
 */
package dimes.util.logging;

import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import NetGraph.graph.GraphDisplayPanel;

/**
 * @author anat
 */
public class ASInfoUpdateGraphHandler extends ConsoleHandler
{
    GraphDisplayPanel displayPanel = null;

	public ASInfoUpdateGraphHandler(GraphDisplayPanel aDisplayPanel)
	{
		this.displayPanel = aDisplayPanel;
		 this.setLevel(Level.ALL);
	}

    public void publish(LogRecord log)
    {
        if (!this.isLoggable(log))
            return;
        String msg = log.getMessage();
        Document ipsDoc = null;
        try
        {
            ipsDoc = DocumentHelper.parseText(msg);
        }
        catch (DocumentException ex)
        {//todo
            Loggers.getLogger().warning("Error while parsing IPs info file receibed from The Server. Some of the Nodes may be Uncolorable.");
            //displayPanel.updateAll(NodeDataUpdate.createRetreivalDataUpdate());
			displayPanel.markAllRetrieved();
        }
        
        List ipsList = ipsDoc.getRootElement().elements();
		Iterator j = ipsList.iterator();
		while (j.hasNext())
		{
			Element elem = (Element) j.next();
			int asNumber = -1;
			try 
			{
				asNumber = Integer.parseInt(elem.attributeValue("ASNumber"));
			}
			catch (Exception numberFormatEx)
			{
				asNumber = -1;
			}
			
			displayPanel.updateIPInfo(elem.attributeValue("address"), 
					elem.attributeValue("Country"), elem.attributeValue("ISP"), asNumber);
		}
    }
}
