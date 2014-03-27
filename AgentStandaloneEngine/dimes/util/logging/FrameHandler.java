package dimes.util.logging;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import dimes.gui.util.LogViewer;

/**
 * <p> </p>
 * <p> </p>
 * <p> </p>
 * <p>Company: DIMES</p>
 * @author anat
 *
 */

public class FrameHandler extends ConsoleHandler
{
	private static final int maxLineNum = 500;
	private static final int maxCharactersNum = 25000;
	private final LogViewer logViewer;
	private boolean rawFormat=false;

	public FrameHandler(LogViewer logViewer)
	{
		this.logViewer = logViewer;
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	public void publish(LogRecord log)
	{
		if (!this.isLoggable(log))
			return;

		String msgInfo = "";
		String msg = "";
		Style msgInfoStyle = null;
		Style messageStyle = null;

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

		StyledDocument doc = logViewer.getStyledDocument();
		// define the styles : 

		if (log.getLevel() == Level.WARNING)
		{
			msgInfoStyle = logViewer.getStyle("Red");
			messageStyle = logViewer.getStyle("Red");
		}
		if (log.getLevel() == Level.INFO)
		{
			msgInfoStyle = null;
			messageStyle = logViewer.getStyle("Blue");
		}

		try
		{
			if (!this.rawFormat)
				logViewer.getStyledDocument().insertString(doc.getLength(), msgInfo, msgInfoStyle);
			else
				System.out.print(msg);
			logViewer.getStyledDocument().insertString(doc.getLength(), msg, messageStyle);
		}
		catch (BadLocationException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		int diff = doc.getLength() - maxCharactersNum;
		if (diff > 0)//longer than maxNumChars
		{
			try
			{
				doc.remove(0, diff);
			}
			catch (BadLocationException e)
			{
				e.printStackTrace();
			}

		}
		logViewer.setCaretPosition(logViewer.getDocument().getLength());

	}
	
	public void setRawFormat(boolean rawFormat){
		this.rawFormat = rawFormat;
	}

}