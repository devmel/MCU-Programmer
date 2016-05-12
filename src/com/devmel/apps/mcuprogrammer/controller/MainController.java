package com.devmel.apps.mcuprogrammer.controller;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.devmel.apps.mcuprogrammer.R;
import com.devmel.apps.mcuprogrammer.datas.DataArray;
import com.devmel.apps.mcuprogrammer.datas.TargetsConfig;
import com.devmel.apps.mcuprogrammer.sections.MemoryHex;
import com.devmel.apps.mcuprogrammer.sections.WiringDiagram;
import com.devmel.apps.mcuprogrammer.view.swing.HexView;
import com.devmel.apps.mcuprogrammer.view.swing.MainView;
import com.devmel.apps.mcuprogrammer.view.swing.WiringDiagramView;
import com.devmel.communication.nativesystem.Uart;
import com.devmel.programming.IProgramming;
import com.devmel.storage.IBase;
import com.devmel.storage.Node;
import com.devmel.storage.SimpleIPConfig;
import com.devmel.tools.Hexadecimal;
import com.devmel.tools.IPAddress;

public class MainController {	
	private final List<HexDataController> hexControllers = new ArrayList<HexDataController>();
	private final Node devices;
	private final DataArray tabdata;
	private final TargetsConfig targetsConfig;
	private final MainView gui;
	
	private Thread thread = null;
	
	//Device
	private DeviceType deviceType;
	private String deviceName;
	//Programmer
	private String programmer;
	private IProgramming program;

	
	public MainController(IBase userPrefs, DataArray tabdata, TargetsConfig targetsConfig, MainView gui){
		this.devices = new Node(userPrefs, R.bundle.getString("MainController.0"));
		this.tabdata=tabdata;
		this.targetsConfig=targetsConfig;
		this.gui=gui;
	}
	
	public void initialize(){
		String[] names = targetsConfig.manufacturerList();
		String[] nnames = new String[names.length+1];
		nnames[0] = R.bundle.getString("MainController.1");
		for(int i=0;i<names.length;i++){
			nnames[i+1]=names[i];
		}
		gui.targetSelectionBar.setManufacturerList(nnames);
		selectManufacturer(gui.targetSelectionBar.getManufacturer());
		gui.statusBar.stopProgress();
		gui.hideTargetTools();
		initializeDeviceList();
	}
	public void initializeDeviceList(){
		Vector<String> list = new Vector<String>();
		String[] sysDeviceList = Uart.list();
		if(sysDeviceList!=null){
			for(String devStr:sysDeviceList){
				list.add(devStr);
			}
		}
		
		String[] ipDeviceList = this.devices.getChildNames();
		if(ipDeviceList!=null){
			for(String devStr:ipDeviceList){
				SimpleIPConfig dev = SimpleIPConfig.createFromNode(devices, devStr);
				if(dev!=null){
					devStr = devStr+" - "+dev.getIpAsText();
					list.add(devStr);
				}
			}
		}
		String[] bList = new String[list.size()];
		list.toArray(bList);
		gui.deviceSelectBar.setListDevices(bList);
	}

	
	/******************************** IP DEVICE *********************************/
	public void selectDevice(final String name) {
		deviceUnbuild(false);
		deviceType = null;
		deviceName = null;
		if (name != null && !name.equals(R.bundle.getString("MainController.2"))) {
			if(name.contains(" - ")){
				String[] names = name.split(" - ");
				if(names!=null && names.length>0){
					String[] ipDeviceList = this.devices.getChildNames();
					for(String devStr:ipDeviceList){
						if(devStr.equals(names[0])){
							deviceType = DeviceType.LINKBUS;
							deviceName = devStr;
							SimpleIPConfig device = SimpleIPConfig.createFromNode(devices, deviceName);
							if(device!=null){
								gui.deviceSelectBar.setLock(device.getLock());
								gui.deviceSelectBar.setDeleteDeviceEnabled(true);
							}
							break;
						}
					}
				}
			}else{
				String[] sysDeviceList = Uart.list();
				for(String devStr:sysDeviceList){
					if(devStr.equals(name)){
						deviceType = DeviceType.UART;
						deviceName = name;
						gui.deviceSelectBar.setDeleteDeviceEnabled(true);
						break;
					}
				}
			}
			//Update port in sections
			if(gui.tabbedPane != null){
				for(int i = 0; i<gui.tabbedPane.getTabCount();i++){
					Component c = gui.tabbedPane.getComponent(i);
					if(c instanceof WiringDiagramView){
						if(deviceType!=null)
							((WiringDiagramView)c).setPortClass(deviceType.packageName);
					}
				}
			}
		}
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
				initializeDeviceList();
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
			initializeDeviceList();
		} else {
			gui.deviceSelectBar.removeDeviceConfirm(name);
		}
	}

	
	/******************************** TARGET *********************************/
	public void selectManufacturer(String manufacturer){
		String[] names = targetsConfig.nameList(manufacturer);
		gui.targetSelectionBar.setDeviceList(names);
		selectTarget(gui.targetSelectionBar.getDevice());
	}
	
	public void selectTarget(String target){
		deviceUnbuild(false);
		//Get config
		String[] programmers = targetsConfig.programmers(target);
		if(programmers!=null && programmers.length>0){
			gui.targetSelectionBar.setProgrammerList(programmers);
			Object[] sections = targetsConfig.sections(target);
			if(sections!=null){
				tabdata.sections.clear();
				for(int i=0;i<sections.length;i++){
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
	
	public void selectProgrammer(String programmer){
		deviceUnbuild(false);
		if(programmer!=null && programmer.equals("")){
			this.programmer=null;
		}else{
			this.programmer=programmer;
		}
		//Update programmer in sections
		if(gui.tabbedPane != null){
			for(int i = 0; i<gui.tabbedPane.getTabCount();i++){
				Component c = gui.tabbedPane.getComponent(i);
				if(c instanceof WiringDiagramView){
					if(deviceType!=null)
						((WiringDiagramView)c).setPortClass(deviceType.packageName);
					((WiringDiagramView)c).setProgrammer(programmer);
				}
			}
		}
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
						deviceBuild();
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
						if(memory.read(gui.statusBar, program, tabdata.rawdata, 0)<0){
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
						if(memory.write(gui.statusBar, program, tabdata.rawdata, 0)<0){
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
						int verif = memory.verify(gui.statusBar, program, tabdata.rawdata, 0);
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

	
	private void reloadSections(){
		//Clear controllers
		stopCommunication();
		hexControllers.clear();
		gui.tabbedPane.removeAll();
		for (int i = 0; i < tabdata.sections.size(); i++) {
			Object section = tabdata.sections.get(i);
			if(section instanceof WiringDiagram){
				WiringDiagramView view = new WiringDiagramView(((WiringDiagram)section).imageName, ((WiringDiagram)section).voltage);
				gui.tabbedPane.addTab(R.bundle.getString("MainController.25"), null, view, R.bundle.getString("MainController.23"));
			}
			else if(section instanceof MemoryHex){
				MemoryHex mem = (MemoryHex) section;
				HexView view =  new HexView();
				HexDataController hexController = new HexDataController(tabdata, mem, view, this);
				view.setController(hexController);
				hexControllers.add(hexController);
				gui.tabbedPane.addTab(mem.name, null, view, R.bundle.getString("MainController.23"));
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
	
	private String deviceException(IOException exception){
		if(exception==null){
			return "";
		}
		return exception.getMessage();
	}
	
	private synchronized void deviceBuild(){
		if(program == null && programmer != null 
				&& deviceType != null && deviceName != null){
			//Load class
			Class<?> c = null;
			try {
				c = Class.forName(deviceType.packageName+"."+programmer);
			} catch (ClassNotFoundException e) {
			}
			if(c!=null){
				Object device = null;

				if(deviceType==DeviceType.UART){
					try {
						device = new Uart(deviceName);
					} catch (IOException e) {
						e.printStackTrace();
						device=null;
					}
				}
				else if(deviceType==DeviceType.LINKBUS){
					device = SimpleIPConfig.createFromNode(devices, deviceName);
				}
				if(device!=null){
					try {
						Constructor<?> constructor = c.getConstructor(device.getClass());
						IProgramming o = (IProgramming)constructor.newInstance(device);
						program = o;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
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
	
	public enum DeviceType{
		LINKBUS("com.devmel.programming.linkbus"),
		UART("com.devmel.programming.uart");

		public final String packageName;
		
		DeviceType(String packageName) {
			this.packageName = packageName;
		}
	}
}
