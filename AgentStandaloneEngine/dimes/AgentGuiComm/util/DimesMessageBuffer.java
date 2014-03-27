package dimes.AgentGuiComm.util;

import java.util.concurrent.ArrayBlockingQueue;

public class DimesMessageBuffer {
	
	private ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(10);

	public void push(String msg){
		if (!queue.offer(msg)) 
			{
			queue.poll();
			queue.offer(msg);
			}
	}
	
	public String pop(){
		return queue.poll();
	}
	
	public int getSize(){
		return queue.size();
	}
/*	private Message[] buffer = new Message[10];
	private int head=0;
	private int tail=0;
	private int size=0;
	
	public DimesMessageBuffer(){
		for (int i=0;i<10;i++) buffer[i]=null;
	}
	
	public void push(Message msg){
		buffer[head]=msg;
		if (!(10==size)) size++;
		head=((head+1)%10);
		if (null!=buffer[(head+1)%10])
			tail = ((head+1)%10);		
	}
	
	public Message pop(){
		if (null==buffer[tail]) return null;
		Message temp = buffer[tail];
		buffer[tail]=null;
		if (0<tail) size--;
		else tail=9;
//		tail = (tail>0)?tail--:(tail=9);
		size=(size==0?size:size--);
		return temp;
	}
	
	public int getSize(){
		return size;
	}*/
}
