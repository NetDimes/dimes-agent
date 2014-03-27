/*
 * Created on 30/08/2005
 *
 */
package dimes.util.gui;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

/**
 * @author Ohad Serfaty
 *
 * This class is an extended JTextPane 
 * 
 */
public class LogViewer extends JTextPane
{

	public LogViewer()
	{
		super();
		// add styles to this messae area :
		Style styleRed = this.addStyle("Red", null);
		StyleConstants.setForeground(styleRed, Color.red);

		Style styleBule = this.addStyle("Blue", null);
		StyleConstants.setForeground(styleBule, Color.BLUE);
	}

}