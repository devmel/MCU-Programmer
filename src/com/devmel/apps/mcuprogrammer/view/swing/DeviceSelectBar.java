package com.devmel.apps.mcuprogrammer.view.swing;

import javax.swing.JPanel;

import com.devmel.apps.mcuprogrammer.controller.MainController;
import com.devmel.apps.mcuprogrammer.lang.Language;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;

public class DeviceSelectBar extends JPanel{
	private static final long serialVersionUID = -257113780246545239L;
	private MainController controller;
	private final JComboBox<String> deviceSelect;
	private final JCheckBox chckbxLock;
	private final JButton btnAdd;
	private final JButton btnDelete;
	
	public DeviceSelectBar() {
		deviceSelect = new JComboBox<String>();
		deviceSelect.setPrototypeDisplayValue(Language.getString("DeviceSelectBar.0")); //$NON-NLS-1$
		deviceSelect.setToolTipText((String) null);
		deviceSelect.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				final String itemName = e.getItem().toString();
				if(deviceSelect.getSelectedItem()!=null && (deviceSelect.getSelectedItem().toString()).equals(itemName)){
					if(controller!=null){
						controller.selectDevice(itemName);
					}
				}
			}
		});
		this.add(deviceSelect);
		
		chckbxLock = new JCheckBox(Language.getString("DeviceSelectBar.1")); //$NON-NLS-1$
		chckbxLock.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(controller!=null){
					controller.setIPLock(chckbxLock.isSelected());
				}
			}
		});
		this.add(chckbxLock);
		
		btnAdd = new JButton(Language.getString("DeviceSelectBar.2")); //$NON-NLS-1$
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(controller!=null){
					controller.addIPDeviceClick();
				}
			}
		});
		this.add(btnAdd);
		
		btnDelete = new JButton(Language.getString("DeviceSelectBar.3")); //$NON-NLS-1$
		btnDelete.setEnabled(false);
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(controller!=null){
					controller.deleteIPDeviceClick(deviceSelect.getSelectedItem().toString(), false);
				}
			}
		});
		this.add(btnDelete);
		
	}
	
	public void setController(MainController controller) {
		this.controller = controller;
	}

	public void setListDevices(String[] list) {
		deviceSelect.removeAllItems();
		if(list!=null){
			for (String item : list) {
				deviceSelect.addItem(item);
			}
		}
		this.repaint();
	}
	
	public void setDeleteDeviceEnabled(boolean enabled){
		btnDelete.setEnabled(enabled);
	}

	public void setLock(boolean lock){
		this.chckbxLock.setSelected(lock);
	}

	public void addIPDeviceDialog() {
		addIPDeviceDialog(0 , Language.getString("DeviceSelectBar.4"), Language.getString("DeviceSelectBar.5"), null); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void addIPDeviceDialog(int error, String name, String ip, String password) {
		String err = null;
		if(error==-1){
			err = Language.getString("DeviceSelectBar.6"); //$NON-NLS-1$
		}else if(error==-2){
			err = Language.getString("DeviceSelectBar.7"); //$NON-NLS-1$
		}else if(error==-3){
			err = Language.getString("DeviceSelectBar.8"); //$NON-NLS-1$
		}else if(error==-4){
			err = Language.getString("DeviceSelectBar.9"); //$NON-NLS-1$
		}else if(error==-5){
			err = Language.getString("DeviceSelectBar.10"); //$NON-NLS-1$
		}
		IPDeviceAddPanel panel = new IPDeviceAddPanel(err,name,ip,password);
        int ret = JOptionPane.showConfirmDialog(null, panel, Language.getString("DeviceSelectBar.11"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
        if (ret == JOptionPane.OK_OPTION) {
			if(controller!=null){
				controller.addIPDeviceClick(panel.getName(), panel.getIP(), panel.getPassword());
			}
        }
	}
	
	public void removeDeviceConfirm(final String name) {
		int dialogButton = JOptionPane.YES_NO_OPTION;
		int ret = JOptionPane.showConfirmDialog (null, Language.getString("DeviceSelectBar.12")+name+Language.getString("DeviceSelectBar.13"),Language.getString("DeviceSelectBar.14"),dialogButton); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if(ret==JOptionPane.OK_OPTION){
			controller.deleteIPDeviceClick(name, true);
		}
	}

}
