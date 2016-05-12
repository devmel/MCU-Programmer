package com.devmel.apps.mcuprogrammer.datas;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.devmel.apps.mcuprogrammer.sections.MemoryHex;
import com.devmel.apps.mcuprogrammer.sections.MemoryHexWriteSector;
import com.devmel.apps.mcuprogrammer.sections.WiringDiagram;

public class TargetsConfig {
	private Document doc;
	
	public TargetsConfig(){
		try {
			InputStream xml = TargetsConfig.class.getResourceAsStream("/res/devices.xml");
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = dBuilder.parse(xml);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public Object[] sections(String deviceName){
		ArrayList<Object> sectionsList = new ArrayList<Object>();
		Element device = getDevice(deviceName);
		if(device!=null){
			NodeList nList = device.getChildNodes();
			for (int i = 0; i < nList.getLength(); i++) {
				try{
					Node nNode = nList.item(i);
					if(nNode.getNodeName().equalsIgnoreCase("section")){
						Element eElement = (Element) nNode;
						String className = eElement.getAttributeNode("class").getNodeValue();
						//Wiring
						if(className.equals("WiringDiagram")){
							String imageName = null;
							String voltage = null;
							try {
								imageName = eElement.getElementsByTagName("image").item(0).getTextContent();
								voltage = eElement.getElementsByTagName("voltage").item(0).getTextContent();
							} catch (Exception e) {}
							if(imageName != null && voltage != null)
								sectionsList.add(new WiringDiagram(imageName, voltage));
						}
						//Hex class
						else if(className.startsWith("Hex")){
							int readStartInt = 0;
							int writeStartInt = 0;
							
							String name = eElement.getElementsByTagName("name").item(0).getTextContent();
							String start = eElement.getElementsByTagName("start").item(0).getTextContent();
							int startInt = Integer.decode(start);
							String size = eElement.getElementsByTagName("size").item(0).getTextContent();
							int sizeInt = Integer.decode(size);
							NodeList b = eElement.getElementsByTagName("blank");
							byte[] blankArray = new byte[]{(byte) 0xff};
							if(b!=null && b.item(0)!=null){
								String blank = b.item(0).getTextContent();
								if(blank!=null && blank.startsWith("0x")){
									int tab = (blank.length()/2)-1;
									blankArray = new byte[tab];
									for(int z=0;z<tab;z++){
										blankArray[z] = (byte) (Integer.parseInt(blank.substring(z*2+2, (z*2)+4),16) & 0xff);
									}
								}
							}
							
							String type = eElement.getElementsByTagName("type").item(0).getTextContent();
							int typeInt = Integer.decode(type);
							
							NodeList nodeDevicestart = eElement.getElementsByTagName("readstart");
							if(nodeDevicestart!=null && nodeDevicestart.item(0)!=null){
								String devicestart = nodeDevicestart.item(0).getTextContent();
								readStartInt = Integer.decode(devicestart);
							}

							nodeDevicestart = eElement.getElementsByTagName("writestart");
							if(nodeDevicestart!=null && nodeDevicestart.item(0)!=null){
								String devicestart = nodeDevicestart.item(0).getTextContent();
								writeStartInt = Integer.decode(devicestart);
							}
							
							if(className.equalsIgnoreCase("HexWritePage")){
								String pagesize = eElement.getElementsByTagName("pagesize").item(0).getTextContent();
								int pagesizeInt = Integer.decode(pagesize);
								sectionsList.add(new MemoryHexWriteSector(name, startInt, readStartInt, writeStartInt, sizeInt, pagesizeInt, typeInt, blankArray));
							}else if(className.equalsIgnoreCase("Hex")){
								sectionsList.add(new MemoryHex(name, startInt, readStartInt, writeStartInt, sizeInt, typeInt, blankArray));
							}
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		Object[] sections = new Object[sectionsList.size()];
		sections = sectionsList.toArray(sections);
		return sections;
	}

	public String[] manufacturerList(){
		Set<String> manufacturerList = new HashSet<String>();
		
		NodeList nList = doc.getElementsByTagName("device");
		for (int i = 0; i < nList.getLength(); i++) {
			try{
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					manufacturerList.add(eElement.getElementsByTagName("manufacturer").item(0).getTextContent());
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		String[] names = new String[manufacturerList.size()];
		manufacturerList.toArray(names);
		return names;
	}

	public String[] nameList(String manufacturer){
		Set<String> nameList = new HashSet<String>();
		NodeList nList = doc.getElementsByTagName("device");
		
		for (int i = 0; i < nList.getLength(); i++) {
			try{
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String mnf = eElement.getElementsByTagName("manufacturer").item(0).getTextContent();
					if(manufacturer==null || manufacturer.equalsIgnoreCase(mnf)){
						nameList.add(eElement.getElementsByTagName("name").item(0).getTextContent());
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		String[] names = new String[nameList.size()];
		nameList.toArray(names);
		return names;
	}
	
	public String[] programmers(String deviceName){
		ArrayList<String> programmers = new ArrayList<String>();
		
		Element device = getDevice(deviceName);
		if(device!=null){
			NodeList nList = device.getChildNodes();
			
			for (int i = 0; i < nList.getLength(); i++) {
				try{
					Node nNode = nList.item(i);
					if(nNode.getNodeName().equalsIgnoreCase("programmer")){
						Element eElement = (Element) nNode;
						programmers.add(eElement.getChildNodes().item(0).getTextContent());
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}		
		String[] names = new String[programmers.size()];
		names = programmers.toArray(names);
		return names;
	}
	
	private Element getDevice(String deviceName){
		Element eElement = null;
		NodeList nList = doc.getElementsByTagName("device");
		for (int i = 0; i < nList.getLength(); i++) {
			try{
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					eElement = (Element) nNode;
					if(eElement.getElementsByTagName("name").item(0).getTextContent().equalsIgnoreCase(deviceName)){
						break;
					}
					eElement = null;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return eElement;
	}
}