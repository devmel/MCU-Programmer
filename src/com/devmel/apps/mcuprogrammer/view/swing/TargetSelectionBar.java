package com.devmel.apps.mcuprogrammer.view.swing;

import javax.swing.JPanel;

import com.devmel.apps.mcuprogrammer.controller.MainController;
import com.devmel.apps.mcuprogrammer.R;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JButton;

public class TargetSelectionBar extends JPanel{
	private static final long serialVersionUID = -257114752589545239L;
	private MainController controller;
	private final JComboBox<String> manufacturerSelect;
	private final JComboBox<String> deviceSelect;
	private final JComboBox<String> programmerSelect;
	private final JButton btnOpen;
	
	public TargetSelectionBar() {
		manufacturerSelect = new JComboBox<String>();
		manufacturerSelect.addItemListener(new ListManufacturer());
		add(manufacturerSelect);

		deviceSelect = new JComboBox<String>();
		deviceSelect.addItemListener(new ListTarget());
		add(deviceSelect);
		
		programmerSelect = new JComboBox<String>();
		programmerSelect.addItemListener(new ListProgrammer());
		add(programmerSelect);
		
		btnOpen = new JButton(R.bundle.getString("TargetSelectionBar.0")); //$NON-NLS-1$
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	        	if(controller!=null){
	        		controller.openTargetClick();
	        	}
			}
		});
		add(btnOpen);
	}
	
	public void setController(MainController controller) {
		this.controller = controller;
	}
	
	public void setManufacturerList(String[] deviceList){
		manufacturerSelect.setModel(new DefaultComboBoxModel<String>(deviceList));
	}
	public void setDeviceList(String[] deviceList){
		deviceSelect.setModel(new DefaultComboBoxModel<String>(deviceList));
	}
	public void setProgrammerList(String[] programmerList){
		programmerSelect.setModel(new DefaultComboBoxModel<String>(programmerList));
	}
	
	public void setOpened(boolean opened) {
		if(opened==true){
			btnOpen.setText(R.bundle.getString("TargetSelectionBar.1")); //$NON-NLS-1$
		}else{
			btnOpen.setText(R.bundle.getString("TargetSelectionBar.2")); //$NON-NLS-1$
		}
	}

	
	public String getManufacturer(){
		String ret = null;
		if(manufacturerSelect.getSelectedItem()!=null){
			ret = manufacturerSelect.getSelectedItem().toString();
		}
		return ret;
	}
	public String getDevice(){
		String ret = null;
		if(deviceSelect.getSelectedItem()!=null){
			ret = deviceSelect.getSelectedItem().toString();
		}
		return ret;
	}
	public String getProgrammer(){
		String ret = null;
		if(programmerSelect.getSelectedItem()!=null){
			ret = programmerSelect.getSelectedItem().toString();
		}
		return ret;
	}
	
	private class ListManufacturer implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			final String itemName = e.getItem().toString();
			if(getManufacturer()!=null && getManufacturer().equals(itemName)){
				if(controller!=null){
					controller.selectManufacturer(getManufacturer());
				}
			}
		}
	}

	private class ListTarget implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			final String itemName = e.getItem().toString();
			if(getDevice()!=null && getDevice().equals(itemName)){
				if(controller!=null){
					controller.selectTarget(getDevice());
				}
			}
		}
	}
	
	private class ListProgrammer implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			final String itemName = e.getItem().toString();
			if(getProgrammer()!=null && getProgrammer().equals(itemName)){
				if(controller!=null){
					controller.selectProgrammer(getProgrammer());
				}
			}
		}
	}
	
}
