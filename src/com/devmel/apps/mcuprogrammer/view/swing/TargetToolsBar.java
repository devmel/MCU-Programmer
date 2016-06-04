package com.devmel.apps.mcuprogrammer.view.swing;

import javax.swing.JPanel;

import com.devmel.apps.mcuprogrammer.controller.GUIController;
import com.devmel.apps.mcuprogrammer.R;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class TargetToolsBar extends JPanel{
	private static final long serialVersionUID = -257114752446545239L;
	private GUIController controller;
	private final JButton btnReadId;
	private final JButton btnEraseAll;
	private final JLabel targetId;
	
	public TargetToolsBar() {
		btnReadId = new JButton(R.bundle.getString("TargetToolsBar.0"));
		btnReadId.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	        	if(controller!=null){
	        		controller.readIdTargetClick();
	        	}
			}
		});
		add(btnReadId);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setOrientation(SwingConstants.VERTICAL);
		separator_2.setBackground(Color.WHITE);
		add(separator_2);
		
		targetId = new JLabel();
		add(targetId);
		
		JSeparator separator_3 = new JSeparator();
		separator_3.setOrientation(SwingConstants.VERTICAL);
		add(separator_3);
		
		btnEraseAll = new JButton(R.bundle.getString("TargetToolsBar.1"));
		btnEraseAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	        	if(controller!=null){
	        		controller.eraseAllTargetClick();
	        	}
			}
		});
		add(btnEraseAll);
	}
	
	public void setController(GUIController controller) {
		this.controller = controller;
	}
	
	public void setEnabled(boolean enabled){
		btnReadId.setEnabled(enabled);
		btnEraseAll.setEnabled(enabled);
	}
	
	public void setDeviceID(String deviceID){
		targetId.setText(deviceID);
	}
	
}
