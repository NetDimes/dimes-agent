/*
 * Created on 03/06/2004
 */
package dimes.util.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JFrame;

/**
 * @author anat
 */
public class GuiUtils
{

	private static JFrame mainFrame;

	private static JFrame dialogFrame = new JFrame();

	//private c'tor - don't need an instance
	private GuiUtils()
	{
	}

	public static void limitFrameSize(JFrame frame, boolean pack)
	{
		if (pack)
			frame.pack();
		else
			frame.validate();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
		}
	}

	public static void showFrame(JFrame frame)
	{
		frame.setVisible(true);
	}

	public static void centerFrame(Window frame/*JFrame frame*/)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
	}

	// Place the frame in the south-east part of the screen
	// TODO : check this function.
	public static void southEastFrame(Window frame)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		frame.setLocation(screenSize.width - frameSize.width, screenSize.height - frameSize.height);
	}

	public static void setMainFrame(JFrame aFrame)
	{
		mainFrame = aFrame;
	}

	/**
	 * @return Returns the mainFrame.
	 */
	public static JFrame getMainFrame()
	{
		return mainFrame;
	}
	/**
	 * @param showingDialog signifies whether dialogFrame will be used to show a dialog.
	 * if true, can help to add some functionality before the dialog box is shown.
	 * @return Returns the dialogFrame.
	 */
	public static JFrame getDialogFrame(boolean showingDialog)
	{
		if (showingDialog)
		{
			//todo - do better. shouldn't cast
		//	((AgentFrame) mainFrame).allowExit(false);//disable systray exit
			dialogFrame.toFront();
		}
		return dialogFrame;
	}

}