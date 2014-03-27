package dimes.AgentGuiComm.comm.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dimes.AgentGuiComm.comm.Dispatcher;
import dimes.AgentGuiComm.util.MessageTypes;
import dimes.util.XMLUtil;

public class SelectorThread implements Runnable {

	static private Selector selector;
	static private Dispatcher dispatcher;
	static private boolean die=false;
	static private final String Delimiter = "MESSAGE";
	
	/**Public constructor 
	 * 
	 * @param dis - A dispatcher to handle parsed requests 
	 */
	public SelectorThread(Dispatcher dis){
		try {
			selector = Selector.open();
		} catch (IOException e) {

			e.printStackTrace();
		}
		dispatcher=dis;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		int readyChannels;
		int port;
		Channel sourceChannel = null;
		
		//Outer loop, check for ready channels to be read
		while(!die){
			try {
				readyChannels = selector.selectNow();				
				if(readyChannels==0) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

				//Inner loop, find a readable channel, read it, and send message to be parsed
				while(keyIterator.hasNext()){
					SelectionKey key = keyIterator.next();
					keyIterator.remove();
					boolean keyReadable;
					try{
						keyReadable = key.isReadable();
					}catch(CancelledKeyException cke){
						keyReadable=false;
					}
					
					if(keyReadable){
						sourceChannel = key.channel();
						if(sourceChannel instanceof SocketChannel)
							port = ((SocketChannel)sourceChannel).socket().getPort();
						else port=-1;
						parseAndDispatch(readChannel(key),port);
					}
				}
			} catch (IOException e) {

				System.out.println("Disconnect detected");
				e.printStackTrace();				
				//If the channel disconnected we remove it. Selector thread keeps going
				if(sourceChannel instanceof SocketChannel) deregisterChannel((SocketChannel)sourceChannel);
				continue;
			} catch (java.lang.NullPointerException npe){
				npe.printStackTrace();
			}
				continue;
		}
		System.out.println("Selector Thread DIED");
	}

	/**Register a channel in the selector thread, to be pooled by the thread
	 * 
	 * @param c Channel to be registered
	 * @return Selection key of Channel if registration succeed. Null if not. 
	 */
	public static SelectionKey registerChannel(SocketChannel c){
		try {
			c.configureBlocking(false);
			SelectionKey retKey = c.register(SelectorThread.selector, SelectionKey.OP_READ, null);
			return retKey;
		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;
	}
	
	/**De-register a channel that has been disconnected from the selector thread
	 * @param c Channel to be de-registered
	 */
	public static void deregisterChannel(SocketChannel c){
		try{
			c.keyFor(selector).cancel();
			c.close();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}catch(NullPointerException npe){
			System.out.println("execption in deregister");
			npe.printStackTrace();
		}
	}
	
	/** Read a message from a channel that's marked as ready. Returns message as Uppercase String
	 * @param key
	 * @return
	 * @throws IOException
	 */
	private String readChannel(SelectionKey key) throws IOException{
		try{
		ByteBuffer buff = ByteBuffer.allocate(65536);
		StringBuffer message = new StringBuffer();
		SocketChannel channel = (SocketChannel) key.channel();
		int bytesRead = channel.read(buff);
		
		while(bytesRead != 0){
			byte[] ba =buff.array();
			String S2 = new String (ba, "UTF-8").trim();
			message.append(S2);
			buff.clear();
			ba=null;
			bytesRead = channel.read(buff);
		}
		
		System.out.println(message.toString().toUpperCase());
		return "<"+Delimiter+">"+message.toString().toUpperCase()+"</"+Delimiter+">";
		}catch(IOException e){
			throw e;
		}
	}
	
	/** Marks selector thread to gracefully stop.
	 * 
	 */
	public void stopThread(){
		die=true;
		selector.wakeup(); //will unblock selector and restart while loop, which then ends
	}
	
	/** Determines the type of message from the XML and
	 * invokes the dispatcher's dispatch method with the
	 * appropriate arguments
	 * 
	 * @param incomingMessage
	 */
	private void parseAndDispatch(String incomingMessage, int source){
		Element delimiterElm = XMLUtil.getRootElement(incomingMessage);
		List<Node> messages = XMLUtil.getNodeListAsList(delimiterElm.getChildNodes());
		for(Node n: messages){
		String type=n.getNodeName();	
		if(MessageTypes.SEND_TYPES_ARRAY.contains(type))
			dispatcher.dispatch(type, XMLUtil.nodeToString(n), source);
		}
	}
}
