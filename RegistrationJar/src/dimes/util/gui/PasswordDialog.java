package dimes.util.gui;
//package dimes.gui.util;
//
//import java.awt.BorderLayout;
//import java.awt.event.ActionEvent;
//
//import javax.swing.JButton;
//import javax.swing.JDialog;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JPasswordField;
//import javax.swing.SpringLayout;
//
//import dimes.gui.properties.PropertiesFrame;
//
//public class PasswordDialog extends JDialog
//{
//	public final static String NAME = "Password";
//	public final static String HAS_PASSWORD_NAME = "Change Password";
//	public final static String NEW_PASSWORD_NAME = "Create Password";
//	public final static String HAS_PASSWORD_PROPERTY = "hasPswd";
//
//	PropertiesFrame parent = null;
//
//	JPanel panel1 = new JPanel();
//	BorderLayout borderLayout1 = new BorderLayout();
//	JPanel jPanel1 = new JPanel();
//	JButton okButton = new JButton();
//	JButton cancelButton = new JButton();
//	JPanel jPanel2 = new JPanel();
//	JPasswordField currPswdField = new JPasswordField();
//	JPasswordField newPswdField = new JPasswordField();
//	JPasswordField confirmPswdField = new JPasswordField();
//	JLabel currPswdLabel = new JLabel();
//	JLabel newPswdLabel = new JLabel();
//	JLabel confirmPswdLabel = new JLabel();
//	SpringLayout springLayout = new SpringLayout();
//	private String currPswd = "";
//	private String newPswd = "";
//	private String confirmPswd = "";
//
//	public PasswordDialog(PropertiesFrame frame)
//	{
//		super(frame, NAME, true);
//		this.parent = frame;
//		try
//		{
//			jbInit();
//			pack();
//		}
//		catch (Exception ex)
//		{
//			ex.printStackTrace();
//		}
//	}
//
//	/*    //not to be used!
//	 public PasswordDialog()
//	 {
//	 this(null, "", false);
//	 }
//	 */
//	private void jbInit() throws Exception
//	{
//		this.setSize(400, 450);
//		this.setResizable(false);
//		panel1.setLayout(borderLayout1);
//		okButton.setText("OK");
//		okButton.addActionListener(new PasswordDialog_okButton_actionAdapter(this));
//		cancelButton.setText("Cancel");
//		cancelButton.addActionListener(new PasswordDialog_cancelButton_actionAdapter(this));
//		currPswdLabel.setText("current password");
//		newPswdLabel.setText("new password");
//		confirmPswdLabel.setText("confirm new password");
//		currPswdField.setText("");
//		newPswdField.setText("");
//		getContentPane().add(panel1);
//		panel1.add(jPanel1, BorderLayout.SOUTH);
//		jPanel1.add(okButton, null);
//		jPanel1.add(cancelButton, null);
//		panel1.add(jPanel2, BorderLayout.CENTER);
//
//		panel1.setBackground(PropertiesFrame.FRAME_BACKGROUND);
//		panel1.setForeground(PropertiesFrame.TEXT_FOREGROUND);
//		//        panel1.setBackground(new Color(173, 221, 140));
//		//        panel1.setForeground(new Color(0, 104, 76));
//		jPanel1.setOpaque(false);
//		jPanel2.setOpaque(false);
//		currPswdLabel.setOpaque(false);
//		newPswdLabel.setOpaque(false);
//		confirmPswdLabel.setOpaque(false);
//		currPswdField.setBackground(PropertiesFrame.TEXT_BACKGROUND);
//		newPswdField.setBackground(PropertiesFrame.TEXT_BACKGROUND);
//		confirmPswdField.setBackground(PropertiesFrame.TEXT_BACKGROUND);
//		currPswdField.setPreferredSize(PropertiesFrame.TEXT_FIELD_DIMENSION);
//		newPswdField.setPreferredSize(PropertiesFrame.TEXT_FIELD_DIMENSION);
//		confirmPswdField.setPreferredSize(PropertiesFrame.TEXT_FIELD_DIMENSION);
//		//        currPswdField.setBackground(SystemColor.inactiveCaptionText);
//		//        newPswdField.setBackground(SystemColor.inactiveCaptionText);
//		//        confirmPswdField.setBackground(SystemColor.inactiveCaptionText);
//		//        currPswdField.setPreferredSize(new Dimension(180,25));
//		//        newPswdField.setPreferredSize(new Dimension(180,25));
//		//        confirmPswdField.setPreferredSize(new Dimension(180,25));
//
//		jPanel2.setLayout(springLayout);
//		jPanel2.add(currPswdLabel, null);
//		jPanel2.add(currPswdField, null);
//		jPanel2.add(newPswdLabel, null);
//		jPanel2.add(newPswdField, null);
//		jPanel2.add(confirmPswdLabel, null);
//		jPanel2.add(confirmPswdField, null);
//		SpringUtilities.makeCompactGrid(jPanel2, 3, 2, 15, 15, 5, 5);
//	}
//
//	void cancelButton_actionPerformed(ActionEvent e)
//	{
//		this.reset();
//		this.hide();
//	}
//
//	private void reset()
//	{
//		this.currPswdField.setText("");
//		this.newPswdField.setText("");
//		this.confirmPswdField.setText("");
//	}
//
//	public String getCurrentPassword()
//	{
//		return this.currPswd;
//	}
//
//	public String getNewPassword()
//	{
//		return this.newPswd;
//	}
//
//	public boolean passwordConfirmedCorrectly()
//	{
//		return (this.newPswd.equals(this.confirmPswd));
//	}
//
//	public void showCurrPswdField(boolean show)
//	{
//		this.currPswdField.show(show);
//		this.currPswdLabel.show(show);
//	}
//
//	void okButton_actionPerformed(ActionEvent e)
//	{
//		if (this.newPswdField.getText().compareTo(this.confirmPswdField.getText()) != 0)
//		{
//			JOptionPane.showMessageDialog(this, "new password fields don't match. try again.", "DIMES Error", JOptionPane.ERROR_MESSAGE);
//			this.reset();
//			return;
//		}
//
//		currPswd = this.currPswdField.getText().trim();
//		newPswd = this.newPswdField.getText().trim();
//		confirmPswd = this.confirmPswdField.getText().trim();
//
//		this.cancelButton.doClick();//reset and hide
//
//		this.parent.applyPasswordChange();
//
//	}
//
//}
//
//class PasswordDialog_cancelButton_actionAdapter implements java.awt.event.ActionListener
//{
//	PasswordDialog adaptee;
//
//	PasswordDialog_cancelButton_actionAdapter(PasswordDialog adaptee)
//	{
//		this.adaptee = adaptee;
//	}
//	public void actionPerformed(ActionEvent e)
//	{
//		adaptee.cancelButton_actionPerformed(e);
//	}
//}
//
//class PasswordDialog_okButton_actionAdapter implements java.awt.event.ActionListener
//{
//	PasswordDialog adaptee;
//
//	PasswordDialog_okButton_actionAdapter(PasswordDialog adaptee)
//	{
//		this.adaptee = adaptee;
//	}
//	public void actionPerformed(ActionEvent e)
//	{
//		adaptee.okButton_actionPerformed(e);
//	}
//}