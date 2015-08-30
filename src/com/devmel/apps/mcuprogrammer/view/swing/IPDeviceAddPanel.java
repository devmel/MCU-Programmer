package com.devmel.apps.mcuprogrammer.view.swing;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.devmel.apps.mcuprogrammer.lang.Language;

public class IPDeviceAddPanel extends JPanel {
	private static final long serialVersionUID = 7591329824389405447L;
	private JTextField fieldName;
	private JTextField fieldIP;
	private JPasswordField fieldPassword;

	/**
	 * Create the panel.
	 */
	public IPDeviceAddPanel(String error, String name, String ip, String password) {
		this.setLayout(new GridLayout(0, 1));
		fieldName = new JTextField(name);
		fieldIP = new JTextField(ip);
		fieldPassword = new JPasswordField(password);

		if (error != null) {
			JLabel err = new JLabel(error);
			err.setForeground(new Color(255, 0, 0));
			add(err);
		}
		add(new JLabel(Language.getString("IPDeviceAddPanel.0"))); //$NON-NLS-1$
		add(fieldName);
		add(new JLabel(Language.getString("IPDeviceAddPanel.1"))); //$NON-NLS-1$
		add(fieldIP);
		add(new JLabel(Language.getString("IPDeviceAddPanel.2"))); //$NON-NLS-1$
		add(fieldPassword);

		add(new JLabel(Language.getString("IPDeviceAddPanel.3"))); //$NON-NLS-1$
		final String lb = Language.getString("IPDeviceAddPanel.4"); //$NON-NLS-1$
		JLabel info = new JLabel(Language.getString("IPDeviceAddPanel.5") + lb + Language.getString("IPDeviceAddPanel.6"), SwingConstants.CENTER); //$NON-NLS-1$ //$NON-NLS-2$
		info.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(lb));
				} catch (Exception localException) {
				}
			}
		});

		add(info);
		add(new JLabel(Language.getString("IPDeviceAddPanel.7"))); //$NON-NLS-1$
	}

	public String getName() {
		return fieldName.getText();
	}

	public String getIP() {
		return fieldIP.getText();
	}

	public String getPassword() {
		return new String(fieldPassword.getPassword());
	}

}
