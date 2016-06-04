package com.devmel.apps.mcuprogrammer.view.swing;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JButton;

import com.devmel.apps.mcuprogrammer.R;
import com.devmel.apps.mcuprogrammer.tools.SearchQRCode;
import com.devmel.storage.SimpleIPConfig;
import com.devmel.tools.Hexadecimal;

import javax.swing.JCheckBox;

public class IPDeviceAddPanel extends JPanel {
	private static final long serialVersionUID = 7591329824389405447L;
	private final String defaultName;
	private final JTextField fieldName;
	private final JTextField fieldIP;
	private final JPasswordField fieldPassword;
	private final JPanel qrCodePanel;
	private final JButton btnScanQrcodeBeside;
	private final JCheckBox chckbxEnableGateway;

	/**
	 * Create the panel.
	 */
	public IPDeviceAddPanel(String error, String name, String ip, String password, boolean gatewayEnabled) {
		this.setLayout(new GridLayout(0, 1));
		defaultName = name;
		fieldName = new JTextField(name);
		fieldIP = new JTextField(ip);
		fieldPassword = new JPasswordField(password);

		if (error != null) {
			JLabel err = new JLabel(error);
			err.setForeground(new Color(255, 0, 0));
			add(err);
		}
		add(new JLabel(R.bundle.getString("profil_name")+" :"));
		add(fieldName);
		
		qrCodePanel = new JPanel();
		add(qrCodePanel);
		
		btnScanQrcodeBeside = new JButton(R.bundle.getString("scan_lb_unit_code"));
		qrCodePanel.add(btnScanQrcodeBeside);
		btnScanQrcodeBeside.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showWebcam();
			}
		});

		add(new JLabel(" - Local IP -"));
		add(fieldIP);
		add(new JLabel(" - Password -"));
		add(fieldPassword);

		add(new JLabel("   "));
		final String lb = "http://devmel.com/linkbus";
		JLabel info = new JLabel("<html><a href=\"" + lb + "\">"+R.bundle.getString("infos_LB")+"</a></html>", SwingConstants.CENTER);
		info.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(lb));
				} catch (Exception localException) {
				}
			}
		});
		chckbxEnableGateway = new JCheckBox(R.bundle.getString("lbEnableGateway"));
		chckbxEnableGateway.setSelected(gatewayEnabled);
		add(chckbxEnableGateway);
		add(info);
		add(new JLabel("   "));
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
	
	public boolean getGatewayEnabled() {
		return chckbxEnableGateway.isSelected();
	}

	public void showWebcam(){
		SearchQRCode search = null;
		try{
			search = new SearchQRCode();
			if(search.newScan()){
				Object[] options = {R.bundle.getString("ok")};
				JOptionPane.showOptionDialog(null, search.getPanel(), R.bundle.getString("scan_lb_unit_code"), JOptionPane.NO_OPTION, JOptionPane.DEFAULT_OPTION, null, options , options[0]);
				//Fill fields
				if(search.getResult()!=null){
					try{
						SimpleIPConfig config = new SimpleIPConfig("LB", search.getResult());
						if(getName() == null || getName().equals(defaultName)){
							String ip = Hexadecimal.fromBytes(config.getIp());
							if(ip!=null && ip.length()>6)
								fieldName.setText(defaultName+ip.substring(ip.length()-6));
						}
						fieldIP.setText(config.getIpAsText());
						fieldPassword.setText(config.getPasswordAsText());
					}catch(Exception e){
						JOptionPane.showMessageDialog(null, R.bundle.getString("DeviceSelectBar.10"));
					}
				}
			}
		}catch(Exception e){
			JOptionPane.showMessageDialog(null, R.bundle.getString("errorNoWebcam"));
		}finally{
			if(search != null)
				search.close();
		}
	}

}
