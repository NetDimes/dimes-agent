/*
 * Created on 07/03/2005
 *
 */
package dimes.util.registration;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * @author Ohad Serfaty
 *
 * a class that servers as a standard way to display 
 * information/help messages in DIMES.
 * 
 * it extends JPanel and intended to be used as an internal panel
 * or this way :
 * 
 * JOptionPane.showConfirmMessage( new InfoDisplayPanel("a message") ,"Frame message");
 *
 */
public class InfoDisplayPanel extends JPanel
{

	private String message;
	private static final Color MESSAGES_TEXT_COLOR = new Color(20, 40, 170);

	public static int DEFAULT_COLUMNS_NUM = 50;
	public static int DEFAULT_ROWS_NUM = 7;

	public InfoDisplayPanel(String aMessage)
	{
		super();
		message = aMessage;
		jbInit(DEFAULT_COLUMNS_NUM, DEFAULT_ROWS_NUM);
	}

	public InfoDisplayPanel(String aMessage, int columns, int rows)
	{
		super();
		message = aMessage;
		jbInit(columns, rows);
	}

	/**
	 * @param rows
	 * @param columns
	 * 
	 */
	private void jbInit(int columns, int rows)
	{
		// TODO : add a scroll bar :
		this.setBackground(Color.WHITE);
		JTextArea messageTextArea = new JTextArea();
		messageTextArea.setText(message);
		messageTextArea.setLineWrap(true);
		messageTextArea.setForeground(MESSAGES_TEXT_COLOR);
		messageTextArea.setFont(new java.awt.Font("Sans-serif MS", Font.PLAIN, 13));
		messageTextArea.setColumns(columns);
		messageTextArea.setRows(rows);
		this.add(messageTextArea);
	}

}