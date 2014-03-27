package dimes.AgentGuiComm.logging;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import dimes.AgentGuiComm.comm.ClientsBean;
import dimes.AgentGuiComm.util.MessageTypes;
import dimes.AgentGuiComm.GUICommunicator;

public class RemoteLogHandler extends ConsoleHandler {

	private static final int maxLineNum = 500;
	private static final int maxCharactersNum = 25000;
	private boolean rawFormat=false;
	private GUICommunicator guiComm;
	
/*	public RemoteLogHandler(GUICommunicator gc){
		guiComm=gc;
	}*/
	
	public void publish(LogRecord log)
	{
		if (!this.isLoggable(log))
			return;

		String msgInfo = "";
		String msg = "";
		
		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		msgInfo += format.format(new Date(log.getMillis())) + " " + log.getSourceClassName() + " " + log.getSourceMethodName() + "\n";
		msg += (this.rawFormat?
				( log.getParameters()!=null ? "["+log.getParameters()[0].toString()+"]":"")
				:log.getLevel().getName()+": ")  + log.getMessage() + "\n";
		
		if (!this.rawFormat)
		{
			Object[] params = log.getParameters();
			if (params != null)
				for (int i = 0; i < params.length; ++i)
					msg += params[i].toString();
		}
		
//		if (!this.rawFormat)
			System.out.print(msg); //TODO:debug
			ClientsBean.send(MessageTypes.SEND_TYPE_LOG,log.getLevel().toString(), msg);
//			guiComm.sendLog(log.getLevel(), msgInfo, msg);
/*		else{
			System.out.print(msg);
			guiComm.sendLog(log.getLevel(), "", msg);
		}*/
	}
	

	public void setRawFormat(boolean rawFormat){
		this.rawFormat = rawFormat;
	}

}
