package dimes.util.registration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import dimes.util.gui.SpringUtilities;
import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

public class ProxyDialog extends JDialog
{
	public static final String MY_TITLE = "Configure Proxy";
	JFrame parent = null;

	JPanel panel1 = new JPanel();
	BorderLayout borderLayout1 = new BorderLayout();
	JPanel jPanel1 = new JPanel();
	JButton okButton = new JButton();
	JButton cancelButton = new JButton();
	JPanel jPanel2 = new JPanel();
	JCheckBox useProxyCheckBox = new JCheckBox();
	JLabel proxyNameLabel = new JLabel();
	JLabel proxyPortLabel = new JLabel();
	JTextField proxyNameTextField = new JTextField();
	JTextField proxyPortTextField = new JTextField();
	JLabel useProxyLabel = new JLabel();

	SpringLayout springLayout = new SpringLayout();

	public ProxyDialog(JFrame frame)
	{
		super(frame, MY_TITLE, true);
		this.parent = frame;
		try
		{
			jbInit();
			pack();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception
	{

		this.setSize(400, 450);
		this.setResizable(false);
		panel1.setLayout(borderLayout1);
		okButton.setText("OK");
		okButton.addActionListener(new ProxyDialog_okButton_actionAdapter(this));
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ProxyDialog_cancelButton_actionAdapter(this));
		useProxyLabel.setText("Use Proxy");
		proxyNameLabel.setText("Name");
		proxyPortLabel.setText("Port");
		proxyPortTextField.setText("");
		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.SOUTH);
		jPanel1.add(okButton, null);
		jPanel1.add(cancelButton, null);
		panel1.add(jPanel2, BorderLayout.CENTER);

		panel1.setBackground(Color.WHITE);
		panel1.setForeground(new Color(0, 104, 76));
		jPanel1.setOpaque(false);
		jPanel2.setOpaque(false);
		useProxyLabel.setOpaque(false);
		proxyPortLabel.setOpaque(false);
		proxyNameLabel.setOpaque(false);
		proxyPortLabel.setBackground(SystemColor.inactiveCaptionText);
		proxyPortTextField.setBackground(SystemColor.inactiveCaptionText);
		proxyNameTextField.setBackground(SystemColor.inactiveCaptionText);
		proxyPortTextField.setPreferredSize(new Dimension(180, 25));
		proxyNameTextField.setPreferredSize(new Dimension(180, 25));

		useProxyCheckBox.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				setProxyTextFields();

			}

		});

		jPanel2.setLayout(new SpringLayout());

		jPanel2.add(useProxyLabel, null);
		jPanel2.add(useProxyCheckBox, null);
		jPanel2.add(proxyNameLabel, null);
		jPanel2.add(proxyNameTextField, null);
		jPanel2.add(proxyNameLabel, null);
		jPanel2.add(proxyNameTextField, null);
		jPanel2.add(proxyPortLabel, null);
		jPanel2.add(proxyPortTextField, null);
		SpringUtilities.makeCompactGrid(jPanel2, 3, 2, 15, 15, 5, 5);

	}

	private void setProxyTextFields()
	{
		// TODO Auto-generated method stub
		boolean useProxy = this.useProxyCheckBox.isSelected();
		proxyPortTextField.setEnabled(useProxy);
		proxyNameTextField.setEnabled(useProxy);
		proxyNameLabel.setEnabled(useProxy);
		proxyPortLabel.setEnabled(useProxy);
	}

	void cancelButton_actionPerformed(ActionEvent e)
	{
		this.reset();
		this.hide();
	}

	void reset()
	{
		String proxyName = "";
		boolean useProxy = false;
		String proxyPort = "";
		try
		{
			useProxy = Boolean.valueOf(PropertiesBean.getProperty(PropertiesNames.USE_PROXY/*"comm.useProxy"*/)).booleanValue();
			proxyName = PropertiesBean.getProperty(PropertiesNames.PROXY_HOST/*"proxyHost"*/);
			if (!PropertiesBean.isValidValue(proxyName))
				proxyName = "";
			proxyPort = PropertiesBean.getProperty(PropertiesNames.PROXY_PORT/*"proxyPort"*/);
			if (!PropertiesBean.isValidValue(proxyPort))
				proxyPort = "";
		}
		catch (NoSuchPropertyException e)
		{ //todo - add the property if doesn't exist
			Loggers.getLogger().fine(e.toString()); //debug
		}
		useProxyCheckBox.setSelected(useProxy);
		proxyNameLabel.setEnabled(useProxy);
		proxyNameTextField.setEnabled(useProxy);
		proxyNameTextField.setText(proxyName);
		proxyPortLabel.setEnabled(useProxy);
		proxyPortTextField.setEnabled(useProxy);
		proxyPortTextField.setText(proxyPort);
	}

	void okButton_actionPerformed(ActionEvent e)
	{
		if (this.useProxyCheckBox.isSelected())
		{
			if (this.proxyNameTextField.getText().equals(""))
			{
				JOptionPane.showMessageDialog(this, "Please enter a Proxy name.", "DIMES Error", JOptionPane.ERROR_MESSAGE);

				return;
			}

			if (this.proxyPortTextField.getText().equals(""))
			{
				JOptionPane.showMessageDialog(this, "Please enter a Proxy Port.", "DIMES Error", JOptionPane.ERROR_MESSAGE);

				return;
			}

		}
		try
		{
			PropertiesBean.setProperty(PropertiesNames.USE_PROXY/*"comm.useProxy"*/, Boolean.toString(useProxyCheckBox.isSelected()));
			PropertiesBean.setProperty(PropertiesNames.PROXY_HOST/*"proxyHost"*/, proxyNameTextField.getText());
			PropertiesBean.setProperty(PropertiesNames.PROXY_PORT/*"proxyPort"*/, proxyPortTextField.getText());
		}
		catch (IOException e1)
		{
			JOptionPane.showMessageDialog(this, "Could not update Properties file.\n Please check properties.xml", "DIMES Error", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
			return;
		}

		this.reset();
		this.hide();

	}
}

class ProxyDialog_cancelButton_actionAdapter implements java.awt.event.ActionListener
{
	ProxyDialog adaptee;

	ProxyDialog_cancelButton_actionAdapter(ProxyDialog adaptee)
	{
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e)
	{
		adaptee.cancelButton_actionPerformed(e);
	}
}

class ProxyDialog_okButton_actionAdapter implements java.awt.event.ActionListener
{
	ProxyDialog adaptee;

	ProxyDialog_okButton_actionAdapter(ProxyDialog adaptee)
	{
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton_actionPerformed(e);
	}
}