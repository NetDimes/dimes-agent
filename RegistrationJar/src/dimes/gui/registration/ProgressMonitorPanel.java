/*
 * Created on 28/02/2005
 *
 */
package dimes.gui.registration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import dimes.state.user.CancelInputListener;

/**
 * @author Ohad Serfaty
 *
 */
public class ProgressMonitorPanel extends JPanel implements ProgressMonitorComponent
{

	JLabel mainMessageLabel;
	JEditorPane textArea;

	private static final Color CANCEL_PANEL_BACKGROUND_COLOR = new Color(230, 240, 250);
	private static final Color BACKGROUND_COLOR = Color.WHITE;
	private static final Color CONFIRM_COLOR = new Color(0, 139, 69);

	private static int FRAME_DEFAULT_SIZE = 450;

	String message = "";
	private JProgressBar progressBar;
	private CancelInputListener listener;
	private JButton okButton;
	private JButton cancelButton;

	public ProgressMonitorPanel(String aMessage)
	{
		super();
		message = aMessage;
		this.jbInit();
	}

	public void jbInit()
	{

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBackground(ProgressMonitorPanel.BACKGROUND_COLOR);

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(false);//start without progress

		progressBar.setPreferredSize(new Dimension(FRAME_DEFAULT_SIZE, 30));

		mainMessageLabel = new JLabel(message);
		mainMessageLabel.setFont(new java.awt.Font("Comic Sans MS", Font.PLAIN, 14));
		mainMessageLabel.setPreferredSize(new Dimension(FRAME_DEFAULT_SIZE, 50));

		textArea = new JEditorPane();
		textArea.setPreferredSize(new Dimension(FRAME_DEFAULT_SIZE, 250));
		textArea.setContentType("text/html");
		textArea.setEditable(false);
		//      Put the editor pane in a scroll pane.
		JScrollPane editorScrollPane = new JScrollPane(textArea);
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		editorScrollPane.setPreferredSize(new Dimension(FRAME_DEFAULT_SIZE, 250));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));
		editorScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

		mainMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(mainMessageLabel);
		JLabel emptyLabel = new JLabel("");
		emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(emptyLabel);

		mainPanel.add(new JSeparator());

		mainPanel.add(Box.createRigidArea(new Dimension(FRAME_DEFAULT_SIZE, 15)));
		mainPanel.add(progressBar);
		mainPanel.add(Box.createRigidArea(new Dimension(FRAME_DEFAULT_SIZE, 25)));
		mainPanel.add(Box.createVerticalGlue());

		mainPanel.add(editorScrollPane);

		JPanel cancelPanel = new JPanel();
		cancelPanel.setBackground(ProgressMonitorPanel.CANCEL_PANEL_BACKGROUND_COLOR);
		cancelButton = new JButton("cancel");

		cancelButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				listener.taskCanceled();

			}

		});

		okButton = new JButton("OK");

		okButton.setEnabled(false);

		okButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				listener.taskFinished();

			}

		});

		cancelPanel.add(cancelButton);
		cancelPanel.add(okButton);

		this.setLayout(new BorderLayout());

		this.add(cancelPanel, BorderLayout.SOUTH);
		this.add(mainPanel, BorderLayout.CENTER);
		this.validate();

	}

	public void setCancelListener(CancelInputListener aListener)
	{
		listener = aListener;
	}

	public void setMainMessage(String mainMessage)
	{
		message = mainMessage;
		mainMessageLabel.setText(mainMessage);
		this.validate();
		this.repaint();
	}

	public void startProgress()
	{
		progressBar.setIndeterminate(true);
		this.validate();
		this.repaint();
	}

	public void stopProgress()
	{
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
		this.validate();
		this.repaint();
	}

	/**
	 * 
	 */
	public void resetMessage()
	{
		textArea.setText("");
		this.validate();
		this.repaint();
	}

	public void enableFinish()
	{
		cancelButton.setEnabled(false);
		okButton.setEnabled(true);
		this.validate();
	}

	/**
	 * @param string
	 */
	public void showConfirmMessage(String string)
	{
		textArea.setText("<html><font face=\"Comic Sans MS\" color=009966><b>" + string.replaceAll("\n", "<br>") + "</b></font></html>");
	}

	/* (non-Javadoc)
	 * @see dimes.gui.ProgressMonitorComponent#showErrorMessage(java.lang.String)
	 */
	public void showErrorMessage(String string)
	{
		textArea.setText("<html><font face=\"Comic Sans MS\" color=#EE0000><b>" + string.replaceAll("\n", "<br>") + "</b></font></html>");
	}

	/* (non-Javadoc)
	 * @see dimes.gui.registration.ProgressMonitorComponent#showMessages(java.util.Vector, java.util.Vector)
	 */
	public void showMessages(Vector errorMessages, Vector successMessages)
	{
		String htmlString = "<html><center>";
		if (!errorMessages.isEmpty())
		{
			Iterator i = errorMessages.iterator();
			while (i.hasNext())
			{
				String message = (String) i.next();
				htmlString += "<font face=\"Comic Sans MS\" color=#EE0000><b>" + message.replaceAll("\n", "<br>") + "</b></font><br>";
			}
		}

		if (!successMessages.isEmpty())
		{
			Iterator i = successMessages.iterator();
			while (i.hasNext())
			{
				String message = (String) i.next();
				htmlString += "<font face=\"Comic Sans MS\" color=#009966><b>" + message.replaceAll("\n", "<br>") + "</b></font><br>";
			}
		}

		htmlString += "</center></html>";
		textArea.setText(htmlString);
	}

	public static void main(String args[])
	{

		JFrame testFrame = new JFrame();
		testFrame.setSize(FRAME_DEFAULT_SIZE, 320);
		testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ProgressMonitorPanel me = new ProgressMonitorPanel("  A Test Message!");
		me.setSize(FRAME_DEFAULT_SIZE, 320);
		testFrame.getContentPane().add(me);
		testFrame.show();
	}

	/* (non-Javadoc)
	 * @see dimes.gui.registration.ProgressMonitorComponent#taskCanceled()
	 */
	public void taskCanceled()
	{
		// TODO Auto-generated method stub

	}

}