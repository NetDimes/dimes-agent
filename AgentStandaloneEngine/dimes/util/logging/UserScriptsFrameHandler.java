package dimes.util.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import dimes.gui.UserScriptsPanel;
import dimes.gui.util.LogViewer;

/**
 * <p> </p>
 * <p> </p>
 * <p> </p>
 * <p>Company: DIMES</p>
 * @author Ohad Serfaty
 *
 */

public class UserScriptsFrameHandler extends ConsoleHandler
{
	private static final int maxCharactersNum = 25000;
	private final UserScriptsPanel userScriptsPanel;

	public UserScriptsFrameHandler(UserScriptsPanel userScriptsPanel)
	{
		this.userScriptsPanel = userScriptsPanel;
		this.setLevel(Level.ALL);
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	public void publish(LogRecord log)
	{
		if (!this.isLoggable(log))
			return;
		String msg = "";

		LogViewer messageArea = this.userScriptsPanel.getMessageArea();
		StyledDocument doc = messageArea.getStyledDocument();

		Style style = null;
		if (log.getLevel() == Level.WARNING)
			style = messageArea.getStyle("Red");
		if (log.getLevel() == Level.FINE)
			style = messageArea.getStyle("Blue");
		try
		{
			messageArea.getStyledDocument().insertString(doc.getLength(), log.getMessage(), style);
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
		messageArea.setCaretPosition(messageArea.getDocument().getLength());
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#flush()
	 */
	public void flush()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#close()
	 */
	public void close() throws SecurityException
	{
		// TODO Auto-generated method stub

	}

}