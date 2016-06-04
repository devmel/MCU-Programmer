package com.devmel.apps.mcuprogrammer.controller;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Vector;

import com.devmel.apps.mcuprogrammer.datas.TargetsConfig;
import com.devmel.communication.nativesystem.Uart;
import com.devmel.programming.IProgramming;
import com.devmel.storage.IBase;
import com.devmel.storage.Node;
import com.devmel.storage.SimpleIPConfig;

public abstract class BaseController {
	protected final static URL defaultTargetfiles = BaseController.class.getResource("/targets/");
	protected final IBase baseStorage;
	protected final TargetsConfig targetsConfig;
	
	//Device
	protected DeviceType deviceType = null;
	protected String deviceName = null;
	protected String programmer;
	//Programmer
	protected IProgramming program;


	protected BaseController(IBase baseStorage, TargetsConfig targetsConfig){
		this.baseStorage=baseStorage;
		this.targetsConfig=targetsConfig;
	}
	
	protected void selectDevice(final String name) {
		deviceType = null;
		deviceName = null;
		if (name != null && !name.equals("")) {
			//LinkBus URL
			if(name.startsWith("sp://")){
				try{
					new SimpleIPConfig("LinkBus_CLI", name);
					deviceType = DeviceType.LINKBUS;
					deviceName = name;
				}catch(Exception e){
				}
			//UART Type
			}else if((deviceName = containsCaseInsensitive(name, Uart.list())) != null){
				deviceType = DeviceType.UART;
			//LinkBus
			}else{
				String fName = name;
				String[] names = fName.split(" - ");
				if(names!=null && names.length>0){
					fName = names[0];
				}
				Node devices = new Node(baseStorage, "Linkbus");
				if((deviceName = containsCaseInsensitive(fName, devices.getChildNames())) != null){
					deviceType = DeviceType.LINKBUS;
				}
			}
		}
	}
	
	protected void selectProgrammer(final String programmer){
		if(programmer!=null && programmer.equals("")){
			this.programmer=null;
		}else{
			this.programmer=programmer;
		}
	}

	public String[] getDeviceList(){
		return getDeviceList(new Node(baseStorage, "Linkbus"));
	}
	
	public static String[] getDeviceList(Node devices){
		Vector<String> list = new Vector<String>();
		String[] sysDeviceList = Uart.list();
		if(sysDeviceList!=null){
			for(String devStr:sysDeviceList){
				list.add(devStr);
			}
		}
		String[] ipDeviceList = devices.getChildNames();
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
		return bList;
	}
	
	public static String containsCaseInsensitive(String s, String[] list){
		if(list != null){
			for (String string : list){
				if (string.equalsIgnoreCase(s)){
					return string;
				}
			}
		}
		return null;
	}

	public void loadTargetConfig(String uri){
		if(uri != null){
			if(uri.endsWith(".zip") || uri.endsWith(".jar")){
				uri = "jar:"+uri+"!/";
			}
			baseStorage.saveString("mcuprog.targetfiles", uri);
		}
		loadTargetConfig();
	}
	public void loadTargetConfig(){
		boolean resourcesLoaded = false;
		try {
			URL resources = new URL(baseStorage.getString("mcuprog.targetfiles"));
			resourcesLoaded = targetsConfig.loadResource(resources);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(resourcesLoaded == false){
			targetsConfig.loadResource(defaultTargetfiles);
		}
	}
	
	protected synchronized void loadDevice(){
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
//						e.printStackTrace();
						device=null;
					}
				}
				else if(deviceType==DeviceType.LINKBUS){
					//LinkBus URL
					if(deviceName.startsWith("sp://")){
						device = new SimpleIPConfig("LinkBus_CLI", deviceName);
					}else{
						Node devices = new Node(baseStorage, "Linkbus");
						device = SimpleIPConfig.createFromNode(devices, deviceName);
					}
				}
				if(device!=null){
					try {
						Constructor<?> constructor = c.getConstructor(device.getClass());
						IProgramming o = (IProgramming)constructor.newInstance(device);
						program = o;
					} catch (Exception e) {
//						e.printStackTrace();
					}
				}
			}
		}
	}

	protected String deviceException(IOException exception){
		if(exception==null){
			return "";
		}
		return exception.getMessage();
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
