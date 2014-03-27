package dimes.AgentGuiComm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dimes.AgentGuiComm.util.Message;

/**Query Dispath is a thread which takes a query in the form of a string, executes
 * it using reflection, and returns the result back to the GUI based on the ID
 * 
 * @author BoazH
 * @asof 0.6.0
 *
 */
public class QueryDispatch implements Runnable {


//	private String[] query = new String[2];
	private String className;
	private String methodName;
	private int queryId;
	private Object[] methodParams;
	
	/**Constructor, gets the class name, method name, and ID involved in the query
	 * 
	 * @param msg
	 */
	public QueryDispatch(Message msg){
//		String[] tempQuery=msg.getFirstAttrib().split("[.]");
		
//		query[0]=tempQuery[0];
//		for (int i=1;i< tempQuery.length-1;i++){
//			query[0]=query[0]+"."+tempQuery[i];
//		}
//		query[1]=tempQuery[tempQuery.length-1];
		queryId =msg.getID();
		className = msg.getFirstAttrib();
		methodName = msg.getSecondAttrib();
		methodParams = msg.getParam();
	}
	
	/**Parses, executes, and returns the query
	 * 
	 * 
	 */
	@Override
	public void run() {
	try {
		Class<?> targetClassClass = Class.forName(className); //A Class object of the class whose method is being invoked
		Object targetClassObject=null; //An instance of the class whose method is being invoked
		Method targetMethodObject = null; //A Method object representing the method being invoked
		Class[] paramTypes = null; //An array of Class objects representing the paramater types of the method being invoked
		
//		String[] methodName = query[1].split("[(]");
//		String[] methodParams = methodName[1].split("[,]");
		if (null!=methodParams)
		 paramTypes= new Class[methodParams.length];
//		if (methodParams.length>0 && methodParams[methodParams.length-1].endsWith(")"))
//			methodParams[methodParams.length-1]= methodParams[methodParams.length-1].substring(0, methodParams[methodParams.length-1].length()-1);
		if (null!=methodParams)
		for (int i=0;i<methodParams.length;i++){
//			try{
//				Integer.parseInt(methodParams[i]);
//				paramTypes[i]=Integer.class;
//			}catch (NumberFormatException nfe){
//				if (null==methodParams[i] || ("".equals(methodParams[i]))){
//				paramTypes[i]=null;}
//				else paramTypes[i]=String.class;
//			}
			paramTypes[i]=methodParams[i].getClass();
			
		}
		
		/** In order to invoke a method on an class, we need an Object of that class. In 
		 * reflection you can get an object by calling newInstance, which executes the 
		 * default constructor. If our class is a singleton, this call would fail since
		 * the default constructor is private. So we attempt to manually invoke getInstance
		 * to get the singleton object. If that call fails (targetClass is not a singleton) then
		 * we try the default constructor.  
		 */
		try{
			Method getinst = targetClassClass.getMethod("getInstance",null);
			targetClassObject = getinst.invoke(null, null);
		}catch(NoSuchMethodException n){
			try {
				targetClassObject = targetClassClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}catch (IllegalAccessException e) {
				targetClassObject=null;
			} 
		}
		
		boolean isNull=true?(null==paramTypes):false;
//		for(Object o:paramTypes)
//		{
//			if (null!=o) isNull=false;
//		}
		
//		Method method =null;
		Object response = null;
		if (isNull){
			targetMethodObject = targetClassClass.getMethod(methodName, null);//paramTypes);
		 response = targetMethodObject.invoke(targetClassObject, null);
		}else{ 
			targetMethodObject = targetClassClass.getMethod(methodName, paramTypes);
		 response = targetMethodObject.invoke(targetClassObject, methodParams);
		}
		
		
		GUICommunicator.sendQueryReply(queryId, response);
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (NoSuchMethodException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InvocationTargetException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e){
		//The exceptions above are the result of the refelction. If this exception is 
		//not on of the ones above, we assume it's the resulte of a problem with the invoked
		//method, so we forward it back to the caller.
		GUICommunicator.sendQueryReply(queryId, e); 
	}
	
		
	}
	
	
}
