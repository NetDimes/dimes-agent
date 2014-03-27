package dimes.AgentGuiComm.util;

import java.io.Serializable;


/**Message(byte t, short i, String a, Object[] p)
 * A class representing a message between Agent and gui. 
 * type = type of message (see MessageTypes)
 * attrib = an attribute of the message as follows:
 * 			Query, QueryReply, Comm: Class.method to envoke
 * 			Log: severity (see MessageTypes)
 * 			Graph: null 
 * ID = a Unique Identifier for a query/reply or comm/reply pair
 * 		DIMESACK: ID=-1 means Attrib contains XML String
 * 		Query: ID=-1 means GUI is asking Agent to send a DIMESACK (check conenction)
 * param = An array of Objects that consititute the variables being passed to the 
 * 			remote call if any.
 * 
 * @author BoazH
 *
 */
public class Message implements Serializable{

	private static final long serialVersionUID = 1L;
	private int type;
	private int ID;
	private String attrib1;
	private String attrib2;
	private Object[] param;
	
	public int getType() {
		return type;
	}
	
	public int getID(){
		return ID;
	}
	
	public String getFirstAttrib(){
		return attrib1;
	} 
	
	public String getSecondAttrib(){
		return attrib2;
	}
	
	public Object[] getParam(){
		return param;
	}
	
	public Object getParam(int i){
		return param[i];
	}
	
	public Message(int t, int i, String a, Object[] p){
		this(t,i,a,"",p);
	}
	
	public Message(int t, int i, String a1, String a2, Object[] p){
		type=t;
		ID=i;
		attrib1=a1;
		attrib2=a2;
		param=p;
	}

}
