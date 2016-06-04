package com.devmel.apps.mcuprogrammer.controller;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.devmel.apps.mcuprogrammer.R;
import com.devmel.apps.mcuprogrammer.datas.DataArray;
import com.devmel.apps.mcuprogrammer.datas.TargetsConfig;
import com.devmel.apps.mcuprogrammer.sections.Memory;
import com.devmel.apps.mcuprogrammer.sections.MemoryHex;
import com.devmel.apps.mcuprogrammer.view.swing.HexView;
import com.devmel.apps.mcuprogrammer.view.swing.MainView;
import com.devmel.apps.mcuprogrammer.view.swing.WiringDiagramView;
import com.devmel.programming.IProgramming;
import com.devmel.storage.IBase;
import com.devmel.storage.Node;
import com.devmel.storage.SimpleIPConfig;
import com.devmel.tools.Hexadecimal;
import com.devmel.tools.IPAddress;

public class GUIController extends BaseController{
	private final List<HexDataController> hexControllers = new ArrayList<HexDataController>();
	private final WiringDiagramView wiringView;
	private final Node devices;
	private final DataArray tabdata;
	private final MainView gui;
	
	private Thread thread = null;

	public GUIController(IBase userPrefs, TargetsConfig targetsConfig, DataArray tabdata, MainView gui){
		super(userPrefs, targetsConfig);
		this.wiringView = new WiringDiagramView(targetsConfig);
		this.devices = new Node(baseStorage, "Linkbus");
		this.tabdata=tabdata;
		this.gui=gui;
	}
	
	public void initialize(){
		//Splash screen
		if(baseStorage.getString("configStart") == null){
			String[] options = {"Computer", "LinkBus"};
			int ret = JOptionPane.showOptionDialog(null, R.bundle.getString("select_device_port"), R.bundle.getString("select_port"), JOptionPane.NO_OPTION, JOptionPane.DEFAULT_OPTION, null, options , options[0]);
			if(ret >= 0 && ret < options.length){
				baseStorage.saveString("configStart", options[ret]);
			}
		}
		if("LinkBus".equals(baseStorage.getString("configStart"))){
			addIPDeviceClick();
			baseStorage.saveString("configStart", "LB");
		}
		super.loadTargetConfig();
		gui.statusBar.stopProgress();
		gui.hideTargetTools();
		loadManufacturerList();
		loadDeviceList();
	}
	
	public void loadDeviceList(){
		gui.deviceSelectBar.setListDevices(super.getDeviceList());
	}
	
	public MainView getGUI(){
		return this.gui;
	}
	
	/******************************** IP DEVICE *********************************/
	@Override
	public void selectDevice(final String name) {
		deviceUnbuild(false);
		super.selectDevice(name);
		if(deviceType == DeviceType.LINKBUS){
			SimpleIPConfig device = SimpleIPConfig.createFromNode(devices, deviceName);
			if(device!=null){
				gui.deviceSelectBar.setLock(device.getLock());
				gui.deviceSelectBar.setDeleteDeviceEnabled(true);
			}
		}else{
			gui.deviceSelectBar.setDeleteDeviceEnabled(false);
		}
		//Update port in sections
		wiringView.setPortClass(deviceType.packageName);
		updateWiringView();
	}

	public void setIPLock(boolean selected) {
		if(deviceType==DeviceType.LINKBUS && deviceName!=null){
			SimpleIPConfig device = SimpleIPConfig.createFromNode(devices, deviceName);
			if(device!=null){
				device.setLock(selected);
				device.save(devices);
			}
			selectManufacturer("");
		}
	}
	
	public void addIPDeviceClick() {
		gui.deviceSelectBar.addIPDeviceDialog();
	}
	
	public void addIPDeviceClick(final String name, final String localIP6, final String password, final boolean gatewayEnabled) {
		int err = 0;
		if (this.devices.isChildExist(name)) {
			err = -1;
		} else {
			try {
				byte[] ip = IPAddress.toBytes(localIP6);
				if(name==null || name.length()==0){
					err = -2;
				}else if(ip==null){
					err = -3;
				}else if(password==null || password.length()==0){
					err = -4;
				}else{
					SimpleIPConfig device = new SimpleIPConfig(name);
					device.setIp(ip);
					device.setPassword(password);
					device.setGateway(gatewayEnabled);
					device.save(devices);
				}
				loadDeviceList();
			} catch (Exception e1) {
				e1.printStackTrace();
				err = -5;
			}
		}
		if (err < 0) {
			gui.deviceSelectBar.addIPDeviceDialog(err, name, localIP6, password, gatewayEnabled);
		}
	}

	public void deleteIPDeviceClick(final String name, final boolean confirm) {
		if (confirm == true) {
			if(name.contains(" - ")){
				String[] names = name.split(" - ");
				if(names!=null && names.length>0){
					this.devices.removeChild(names[0]);
				}
			}
			loadDeviceList();
		} else {
			gui.deviceSelectBar.removeDeviceConfirm(name);
		}
	}

	
	/******************************** TARGET *********************************/
	public void selectManufacturer(String manufacturer){
		String[] names = targetsConfig.nameList(manufacturer);
		gui.targetSelectionBar.setDeviceList(names);
		selectTarget(gui.targetSelectionBar.getDevice());
		wiringView.setManufacturer(manufacturer);
		updateWiringView();
	}
	
	public void selectTarget(String target){
		deviceUnbuild(false);
		//Get voltage
		wiringView.setTarget(target);
		wiringView.setVoltage(targetsConfig.getVoltage(target));
		//Get config
		String[] programmers = targetsConfig.programmers(target);
		if(programmers!=null && programmers.length>0){
			gui.targetSelectionBar.setProgrammerList(programmers);
			Object[] sections = targetsConfig.sections(target);
			if(sections!=null){
				tabdata.sections.clear();
				for(int i=0;i<sections.length;i++){
					if(sections[i] instanceof Memory){
						((Memory)sections[i]).setStatusListener(gui.statusBar);
					}
					tabdata.sections.add(sections[i]);
				}
				tabdata.sectionsLock = true;
				tabdata.sectionsCalculation();
				gui.showTagetTools();
			}
		}else{
			gui.hideTargetTools();
			gui.targetSelectionBar.setProgrammerList(new String[] {R.bundle.getString("MainController.3")});
			tabdata.sectionsLock = false;
			//Change all section type
			for (int i = 0; i < tabdata.sections.size(); i++) {
				Object section = tabdata.sections.get(i);
				if(section instanceof MemoryHex){
					((MemoryHex)section).type=0;
				}
			}
		}
		//Select
		reloadSections();
		selectProgrammer(gui.targetSelectionBar.getProgrammer());
	}
	
	@Override
	public void selectProgrammer(String programmer){
		deviceUnbuild(false);
		super.selectProgrammer(programmer);
		//Update programmer in sections
		wiringView.setProgrammer(programmer);
		updateWiringView();
	}
	
	
	
	/******************************** TARGET ACTIONS *********************************/
	public void openTargetClick(){
		boolean c = false;
		if(program!=null){
			c=program.isOpen();
		}
		final boolean close = c;
		startCommunication();
		Runnable r = new Runnable() {
			public void run() {
				String status = null;
				if(deviceName!=null){
					if(close==false){
						loadDevice();
						if(program!=null){
							//Open target
							try {
								if(program.open()){
									gui.targetSelectionBar.setOpened(true);
								}else{
									status = (R.bundle.getString("MainController.5"));
								}
							} catch (IOException e) {
								status = deviceException(e);
							}
						}else if(programmer!=null){
							status = (R.bundle.getString("MainController.4"));
						}else{
							status = (R.bundle.getString("MainController.6"));
						}
					}else{
						deviceUnbuild(true);
					}
				}else{
					status = (R.bundle.getString("MainController.7"));
				}
				stopCommunication(status);
			}
		};
		execute(r);
	}
	
	public void readIdTargetClick(){
		final IProgramming program = this.program;
		startCommunication();
		Runnable r = new Runnable() {
			public void run() {
				String status = null;
				if(program!=null && program.isOpen()==true){
					try{
						byte[] targetID = program.read("DEVID",0,3);
						if(targetID!=null){
							gui.targetToolsBar.setDeviceID(Hexadecimal.fromBytes(targetID));
						}else{
							status = R.bundle.getString("MainController.8");
						}
					}catch(IOException e) {
						status = deviceException(e);
					}
				}else{
					status = R.bundle.getString("MainController.9");
				}
				stopCommunication(status);
			}
		};
		execute(r);
	}
	public void eraseAllTargetClick(){
		startCommunication();
		Runnable r = new Runnable() {
			public void run() {
				String status = null;
				if(program!=null && program.isOpen()==true){
					try{
						if(program.erase(null)){	//Erase All
							status = R.bundle.getString("MainController.10");
						}else{
							status = R.bundle.getString("MainController.11");
						}
					}catch(IOException e) {
						status = deviceException(e);
					}
				}else{
					status = R.bundle.getString("MainController.12");
				}
				stopCommunication(status);
			}
		};
		execute(r);
	}
	
	public void readMemoryClick(final MemoryHex memory){
		startCommunication();
		Runnable r = new Runnable() {
			public void run() {
				String status = null;
				if(program!=null && program.isOpen()==true){
					try{
						if(memory.read(program, tabdata.rawdata, 0)<0){
							status = R.bundle.getString("MainController.13")+Integer.toHexString(memory.progAddress).toUpperCase();
						}
					}catch(IOException e) {
						status = deviceException(e);
					}
				}else{
					status = R.bundle.getString("MainController.14");
				}
				stopCommunication(status);
			}
		};
		execute(r);
	}

	public void writeMemoryClick(final MemoryHex memory){
		startCommunication();
		Runnable r = new Runnable() {
			public void run() {
				String status = null;
				if(program!=null && program.isOpen()==true){
					try{
						if(memory.write(program, tabdata.rawdata, 0)<0){
							status = R.bundle.getString("MainController.15")+Integer.toHexString(memory.progAddress).toUpperCase();
						}
					}catch(IOException e) {
						status = deviceException(e);
					}
				}else{
					status = R.bundle.getString("MainController.16");
				}
				stopCommunication(status);
			}
		};
		execute(r);
	}

	public void verifyMemoryClick(final MemoryHex memory){
		startCommunication();
		Runnable r = new Runnable() {
			public void run() {
				String status = null;
				if(program!=null && program.isOpen()==true){
					try{
						int verif = memory.verify(program, tabdata.rawdata, 0);
						if(verif<-1){
							status = R.bundle.getString("MainController.17")+Integer.toHexString(memory.progAddress).toUpperCase();
						}else if(verif==-1){
							status = R.bundle.getString("MainController.18");
						}else{
							status = R.bundle.getString("MainController.19")+Integer.toHexString(verif).toUpperCase();
						}
					}catch(IOException e) {
						status = deviceException(e);
					}
				}else{
					status = R.bundle.getString("MainController.20");
				}
				stopCommunication(status);
			}
		};
		execute(r);
	}

	/******************************** MENUBAR *********************************/
	public void fileMenuClick(){
		if (tabdata.filePath != null) {
			gui.saveEnable(true);
		} else {
			gui.saveEnable(false);
		}
	}

	public void openClick(){
		gui.showOpenDialog(tabdata.filePath);
	}
	public void saveClick(){
		tabdata.saveFile();
	}
	public void saveAsClick(){
		gui.showSaveDialog(tabdata.filePath);
	}

	public void openFile(File file){
		if(tabdata.openFile(file)==0){
			reloadSections();
			gui.setTitle(R.bundle.getString("MainController.21") + tabdata.filePath);
		}
	}
	public void saveFile(File file){
		if(file!=null){
			String filePath = file.getAbsolutePath();
			if(tabdata.saveFile(filePath)==0){
				gui.setTitle(R.bundle.getString("MainController.22") + tabdata.filePath);
			}
		}
	}

	public void exitClick(){
		deviceUnbuild(true);
		System.exit(0);
	}
	
	public void targetDatabaseClick(){
		gui.showTargetDatabaseDialog("");
	}
	
	public void openTargetDatabaseFile(File file){
		String uri = null;
		if(file != null){
			uri = file.toURI().toString();
			super.loadTargetConfig(uri);
			loadManufacturerList();
		}
	}
	
	private void loadManufacturerList(){
		String[] names = targetsConfig.manufacturerList();
		String[] nnames = new String[names.length+1];
		nnames[0] = R.bundle.getString("MainController.1");
		for(int i=0;i<names.length;i++){
			nnames[i+1]=names[i];
		}
		gui.targetSelectionBar.setManufacturerList(nnames);
		selectManufacturer(gui.targetSelectionBar.getManufacturer());
	}

	private void reloadSections(){
		//Clear controllers
		stopCommunication();
		hexControllers.clear();
		gui.tabbedPane.removeAll();
		//Load sections
		for (int i = 0; i < tabdata.sections.size(); i++) {
			Object section = tabdata.sections.get(i);
			if(section instanceof MemoryHex){
				MemoryHex mem = (MemoryHex) section;
				HexView view =  new HexView();
				HexDataController hexController = new HexDataController(tabdata, mem, view, this);
				view.setController(hexController);
				hexControllers.add(hexController);
				gui.tabbedPane.addTab(mem.name, null, view, R.bundle.getString("MainController.23"));
			}
		}
	}
	
	private void updateWiringView(){
		if(wiringView.isReady()){
			if(gui.tabbedPane != null){
				gui.tabbedPane.insertTab(R.bundle.getString("MainController.25"), null, wiringView, R.bundle.getString("MainController.23"), 0);
				boolean c = wiringView.isChanged();
				if(program!=null){
					c=!program.isOpen();
				}
				if(c == true){
					gui.tabbedPane.setSelectedIndex(0);
				}
			}
		}else{
			if(gui.tabbedPane != null){
				for(int i = 0; i<gui.tabbedPane.getTabCount();i++){
					Component c = gui.tabbedPane.getComponent(i);
					if(c instanceof WiringDiagramView){
						gui.tabbedPane.remove(c);
					}
				}
			}
		}
	}
		
	public void startCommunication(){
		gui.targetToolsBar.setEnabled(false);
		gui.tabbedPane.setEnabled(false);
		for (int i = 0; i < hexControllers.size(); i++) {
			hexControllers.get(i).startCommunication();
		}
		setStatus(null, true);
	}
	public void stopCommunication(){
		gui.targetToolsBar.setEnabled(true);
		gui.tabbedPane.setEnabled(true);
		for (int i = 0; i < hexControllers.size(); i++) {
			hexControllers.get(i).stopCommunication();
		}
		setStatus(null, false);
	}
	public void stopCommunication(String status){
		stopCommunication();
		setStatus(status, false);
	}
	
	private void setStatus(String status, boolean progress){
		if(status==null || status.length()==0){
			status = R.bundle.getString("MainController.24");
		}
		gui.statusBar.setStatus(status);
		if(progress==true){
			gui.statusBar.startProgress(-1);
		}else{
			gui.statusBar.stopProgress();
		}
	}
	
	
	/******************************** DEVICE COMMUNICATION *********************************/
	
	
	private synchronized void deviceUnbuild(boolean wait){
		cancel();
		Runnable r = new Runnable() {
			public void run() {
				if(program!=null){
					IProgramming tmp = program;
					program = null;
					gui.targetSelectionBar.setOpened(false);
					tmp.close();
					stopCommunication(null);
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
		if(wait == true){
			while(t.isAlive()==true){
				try {Thread.sleep(1);} catch (InterruptedException e) {}
			}
		}
	}

	private void execute(Runnable r){
		cancel();
		thread = new Thread(r);
		thread.start();
	}
	private void cancel(){
		if(thread!=null){
			thread.interrupt();
			thread = null;
		}
	}
	
}
