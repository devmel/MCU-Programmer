package com.devmel.apps.mcuprogrammer.datas;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import nanoxml.XMLElement;

import com.devmel.apps.mcuprogrammer.sections.MemoryHex;
import com.devmel.apps.mcuprogrammer.sections.MemoryHexWriteSector;
import com.devmel.tools.Hexadecimal;

@SuppressWarnings("unchecked")
public class TargetsConfig {
    private XMLElement root = new XMLElement();
	private URL resources;
	
	public TargetsConfig(){}
	
	public boolean loadResource(URL resources){
		boolean valid = false;
		root = new XMLElement();
		InputStream xml = null;
		try {
			xml = new URL(resources, "devices.xml").openStream();
            Reader reader = new InputStreamReader(xml);
            root.parseFromReader(reader);
			valid = true;
			this.resources=resources;
		} catch (Exception e) {
//			e.printStackTrace();
		}finally{
			try {
				xml.close();
			} catch (Exception e) {
			}
		}
		return valid;
	}
	
	public URL getResources(){
		return resources;
	}

	public Object[] sections(String targetName){
		ArrayList<Object> sectionsList = new ArrayList<Object>();
		XMLElement device = getTarget(targetName);
		if(device!=null){
			try{
            	Vector<XMLElement> devChilds = device.getChildren();
	            for(XMLElement eElement : devChilds){
	               	if(eElement != null){
	               		if(eElement.getName().equalsIgnoreCase("section")){
							String className = eElement.getAttribute("class").toString();
							//Hex class
							if(className.startsWith("Hex")){
								String name = null;
								int startInt = 0;
								int sizeInt = 0;
								int typeInt = 0;
								byte[] blankArray = new byte[]{(byte) 0xff};
								int readStartInt = 0;
								int writeStartInt = 0;
								int pagesizeInt = 0;
								
								//Search childs element
								Vector<XMLElement> sectionChilds = eElement.getChildren();
		    		            for(XMLElement sectionChild : sectionChilds){
				               		if(sectionChild.getName().equals("name")){
				               			name = sectionChild.getContent();
				               		}
				               		if(sectionChild.getName().equals("start")){
				               			String start = sectionChild.getContent();
				               			startInt = Integer.decode(start);
				               		}
				               		if(sectionChild.getName().equals("size")){
				               			String size = sectionChild.getContent();
				               			sizeInt = Integer.decode(size);
				               		}
				               		if(sectionChild.getName().equals("type")){
				               			String type = sectionChild.getContent();
				               			typeInt = Integer.decode(type);
				               		}
				               		if(sectionChild.getName().equals("blank")){
				               			String blank = sectionChild.getContent();
										if(blank!=null && blank.startsWith("0x")){
											blankArray = Hexadecimal.toBytes(blank.substring(2));
										}
				               		}
				               		if(sectionChild.getName().equals("readstart")){
				               			String readstart = sectionChild.getContent();
				               			readStartInt = Integer.decode(readstart);
				               		}
				               		if(sectionChild.getName().equals("writestart")){
				               			String writestart = sectionChild.getContent();
				               			writeStartInt = Integer.decode(writestart);
				               		}
				               		if(sectionChild.getName().equals("pagesize")){
				               			String pagesize = sectionChild.getContent();
				               			pagesizeInt = Integer.decode(pagesize);
				               		}
		    		            }
								if(className.equalsIgnoreCase("HexWritePage")){
									sectionsList.add(new MemoryHexWriteSector(name, startInt, readStartInt, writeStartInt, sizeInt, pagesizeInt, typeInt, blankArray));
								}else if(className.equalsIgnoreCase("Hex")){
									sectionsList.add(new MemoryHex(name, startInt, readStartInt, writeStartInt, sizeInt, typeInt, blankArray));
								}
							}
	               		}
                	}
	            }
			}catch(Exception e){}
		}
		Object[] sections = new Object[sectionsList.size()];
		sections = sectionsList.toArray(sections);
		return sections;
	}

	public String[] manufacturerList(){
		Set<String> manufacturerList = new HashSet<String>();
		if(root != null){
			try{
				Vector<XMLElement> nList = root.getChildren();
	            for(XMLElement nNode : nList){
	            	Vector<XMLElement> devChilds = nNode.getChildren();
		            for(XMLElement devChild : devChilds){
		               	if(devChild != null && devChild.getName().equals("manufacturer")){
		               		manufacturerList.add(devChild.getContent());
	                	}
		            }
	            }
			}catch(Exception e){}
		}
		String[] names = new String[manufacturerList.size()];
		manufacturerList.toArray(names);
		return names;
	}

	public String[] nameList(String manufacturer){
		Set<String> nameList = new HashSet<String>();
		if(root != null && manufacturer != null){
			try{
				Vector<XMLElement> nList = root.getChildren();
	            for(XMLElement nNode : nList){
	            	String name = null;
	            	boolean isInList = false;
	            	Vector<XMLElement> devChilds = nNode.getChildren();
		            for(XMLElement devChild : devChilds){
		               	if(devChild != null){
		               		if(devChild.getName().equals("manufacturer") && manufacturer.equals(devChild.getContent())){
			               		isInList = true;
		               		}
		               		if(devChild.getName().equals("name")){
			               		name = devChild.getContent();
		               		}
	                	}
		            }
		            if(isInList && name != null){
		            	nameList.add(name);
		            }
	            }
			}catch(Exception e){}
		}
		String[] names = new String[nameList.size()];
		nameList.toArray(names);
		return names;
	}
	
	public String[] programmers(String targetName){
		ArrayList<String> programmers = new ArrayList<String>();
		XMLElement device = getTarget(targetName);
		if(device!=null){
			try{
            	Vector<XMLElement> devChilds = device.getChildren();
	            for(XMLElement devChild : devChilds){
	               	if(devChild != null){
	               		if(devChild.getName().equalsIgnoreCase("programmer")){
	    	            	Vector<XMLElement> progChilds = devChild.getChildren();
	    		            for(XMLElement progChild : progChilds){
			               		if(progChild.getName().equals("name")){
			               			programmers.add(progChild.getContent());
			               		}
	    		            }
	               		}
                	}
	            }
			}catch(Exception e){}
		}
		String[] names = new String[programmers.size()];
		names = programmers.toArray(names);
		return names;
	}
	
	public String getVoltage(String targetName){
		String voltage = null;
		XMLElement device = getTarget(targetName);
		if(device!=null){
			try{
            	Vector<XMLElement> devChilds = device.getChildren();
	            for(XMLElement devChild : devChilds){
	               	if(devChild != null){
	               		if(devChild.getName().equalsIgnoreCase("voltage")){
	               			voltage = devChild.getContent();
	               		}
                	}
	            }
			}catch(Exception e){}
		}
		return voltage;
	}

	public byte[] getId(String targetName){
		byte[] id = null;
		XMLElement device = getTarget(targetName);
		if(device!=null){
			try{
            	Vector<XMLElement> devChilds = device.getChildren();
	            for(XMLElement devChild : devChilds){
	               	if(devChild != null){
	               		if(devChild.getName().equalsIgnoreCase("id")){
	               			String val = devChild.getContent();
							id = Hexadecimal.toBytes(val.substring(2));
	               		}
                	}
	            }
			}catch(Exception e){}
		}
		return id;
	}

	private XMLElement getTarget(String targetName){
		XMLElement ret = null;
		if(root != null && targetName != null){
			try{
				Vector<XMLElement> nList = root.getChildren();
	            for(XMLElement nNode : nList){
	            	Vector<XMLElement> devChilds = nNode.getChildren();
		            for(XMLElement devChild : devChilds){
		               	if(devChild != null){
		               		if(devChild.getName().equals("name") && targetName.equals(devChild.getContent())){
		               			ret = nNode;
		               			break;
		               		}
	                	}
		            }
	            }
			}catch(Exception e){}
		}
		return ret;
	}
}