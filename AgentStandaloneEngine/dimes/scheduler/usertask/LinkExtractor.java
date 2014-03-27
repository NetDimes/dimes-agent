/*
 * Created on 25/08/2005
 *
 */
package dimes.scheduler.usertask;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.ElementIterator;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
/**
 * @author Ohad Serfaty
 *
 */
public class LinkExtractor
{

	private static EditorKit kit = new HTMLEditorKit();
	private static Logger logger = Loggers.getUserScriptsLogger();
	private static boolean stop = false;
	private static String[] extensions = {".doc", ".pdf", ".jpg", ".ppt", ".xul", "xls", ".zip", ".dtd", ".gz", ".tar", ".mid", ".midi", ".mpga", ".mp3",
			".m3u", ".rm", ".wav", ".ra", ".gif", ".bmp"};

	private static StringFilter extensionFilter = new StringFilter(extensions);
	private static final int MAX_LINKS = 10000;

	/********************
	 * return a set of links from a web page.
	 * 
	 * @param uri the site from which to take the links.
	 * @return
	 * @throws Exception
	 */
	public static HashSet extractHtmlLinks(String uri) throws Exception
	{
		HashSet linksSet = new HashSet();
		logger.info("Reading " + uri + "...\n");

		Reader rd = getReader(uri);

		Document doc = kit.createDefaultDocument();
		doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
		kit.read(rd, doc, 0);
		logger.info("Extracting html links from " + uri + "...");

		ElementIterator it = new ElementIterator(doc);
		javax.swing.text.Element elem;
		while ((elem = it.next()) != null)
		{
			SimpleAttributeSet s = (SimpleAttributeSet) elem.getAttributes().getAttribute(HTML.Tag.A);
			if (s != null)
			{
				String link = null;
				if (s.getAttribute(HTML.Attribute.HREF) != null)
					link = s.getAttribute(HTML.Attribute.HREF).toString();
				else
					link = "";
				if (link.startsWith("http:"))
					linksSet.add(link);
			}
		}
		logger.info("done\n");
		return linksSet;
	}

	public static HashSet extractIPsFromLinksSet(HashSet linksSet)
	{
		HashSet ipLinks = new HashSet();
		Iterator i = linksSet.iterator();
		while (i.hasNext())
		{
			String httpLink = (String) i.next();
			String intermidiateStr = httpLink.replaceFirst("http://", "");
			String ipLink = null;
			logger.info("Scanning link :" + httpLink + "... ");
			if ((intermidiateStr.indexOf('/')) > 0)
				ipLink = intermidiateStr.substring(0, intermidiateStr.indexOf('/'));
			else
				ipLink = intermidiateStr;

			InetAddress addr = null;
			try
			{
				addr = InetAddress.getByName(ipLink);
			}
			catch (UnknownHostException e)
			{
				addr = null;
				logger.warning("Unknown host Exception accepted for:" + ipLink + ". Ignoring\n");
				e.printStackTrace();
			}
			if (addr != null && !ipLinks.contains(addr.getHostAddress()))
			{
				ipLinks.add(addr.getHostAddress());
				logger.info("Adding IP to IPs List:" + addr.getHostAddress());
			}
			logger.info("\n");
		}
		return ipLinks;
	}

	public static HashSet extractHtmlIPs(String uri) throws Exception
	{
		HashSet htmlLinks = extractHtmlLinks(uri);
		return extractIPsFromLinksSet(htmlLinks);
	}

	public static void stop()
	{
		stop = true;
	}

	public static HashSet crawlAndExtractHtmlLinks(String sourceUri, int crawlLevel) throws Exception
	{
		HashSet scannedLinks = new HashSet();
		HashSet linksToScan = new HashSet();

		linksToScan.add(sourceUri);

		for (int i = 1; i <= crawlLevel; i++)
		{
			Iterator j = linksToScan.iterator();
			HashSet accumulatingLinks = new HashSet();
			int linksLeft = linksToScan.size();
			while (j.hasNext())
			{
				if (stop)
				{
					logger.warning("Crawler stopped.\n");
					stop = false;
					return scannedLinks;
				}
				String currentLink = (String) j.next();
				int scannedLinksSize = scannedLinks.size();
				int accumulatedNumber = accumulatingLinks.size();
				logger.fine("Crawl Depth " + i + " (Scanned " + scannedLinksSize + "/" + linksLeft + " links , accumulated " + accumulatedNumber + "): ");
				//                logger.info("scanning link :" +currentLink +"...");
				scannedLinks.add(currentLink);
				if (scannedLinksSize > MAX_LINKS || accumulatedNumber > MAX_LINKS)
					return scannedLinks;
				HashSet linksFromCurrentLink = null;
				if (extensionFilter.endsWith(currentLink))
				{
					logger.warning("Ignoring by extension.\n");
				}
				else
				{
					try
					{
						linksFromCurrentLink = extractHtmlLinks(currentLink);
					}
					catch (Exception e)
					{
						logger.warning("Couldn't read link (Read time out).\n");
					}
				}
				if (linksFromCurrentLink != null)
				{
					Iterator k = linksFromCurrentLink.iterator();
					while (k.hasNext())
					{
						String currentLevel2Link = (String) k.next();
						if (!scannedLinks.contains(currentLevel2Link))
						{
							accumulatingLinks.add(currentLevel2Link);
						}
					}
				}
				j.remove();
			}
			linksToScan.addAll(accumulatingLinks);
			if (linksToScan.size() > MAX_LINKS)
				return scannedLinks;
		}
		return scannedLinks;
	}

	/****************
	 * supply a reader from uri.
	 * 
	 * 
	 */
	static Reader getReader(String uri) throws IOException
	{
		if (uri.startsWith("http:"))
		{
			URLConnection conn = new URL(uri).openConnection();
			System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
			System.setProperty("sun.net.client.defaultReadTimeout", "5000");
			TimeLimitedReader reader = new TimeLimitedReader(conn.getInputStream());
			return reader;
		}
		else
		{
			return new FileReader(uri);
		}
	}

	public static void main(String[] args) throws Exception
	{
		System.out.println(extractIPsFromLinksSet(crawlAndExtractHtmlLinks("http://www.netdimes.org", 3)));
	}

	// TODO : move to different class.
	public static class TimeLimitedReader extends InputStreamReader
	{

		long startTime = -1;
		long defaultTimeLimit = 5000;
		/**
		 * @param in
		 */
		public TimeLimitedReader(InputStream in)
		{
			super(in);
			startTime = System.currentTimeMillis();
		}

		public int read() throws IOException
		{
			long timeSinceStart = System.currentTimeMillis() - startTime;
			if (timeSinceStart > 5000)
				return -1;
			int r = super.read();

			return r;
		}

		public int read(char[] cbuf, int offset, int length) throws IOException
		{

			long timeSinceStart = System.currentTimeMillis() - startTime;
			//            System.out.println("Reading " + r + " at " + timeSinceStart);
			if (timeSinceStart > 5000)
				return -1;
			int r = super.read(cbuf, offset, length);
			return r;
		}

	}

}

