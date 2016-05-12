package com.devmel.apps.mcuprogrammer.datas;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.devmel.apps.mcuprogrammer.sections.Memory;
import com.devmel.apps.mcuprogrammer.sections.MemoryHex;
import com.devmel.tools.IntelHex;

public class DataArray {
	private byte defaultValue = (byte) 0xFF;

	public String filePath;
	
	//Mask
	public final List<Object> sections = new ArrayList<Object>();
	public boolean sectionsLock = false;
	
	//RAW Data Binary
	public byte[] rawdata;
	
	
	public byte[] initdata(int size){
		byte[] data = new byte[size];
		for(int i=0;i<data.length;i++){
			data[i]=defaultValue;
		}
		return data;
	}
	public void sectionsCalculation(){
		//Search segment
		int newSize = 0;
		for (int i = 0; i < sections.size(); i++) {
			Object section = sections.get(i);
			if(section instanceof Memory){
				Memory mem = (Memory) section;
				int s = mem.startAddr+mem.size;
				if(newSize<s){
					newSize = s;
				}
			}
		}
		if(rawdata==null || newSize>rawdata.length){
			byte[] data = initdata(newSize);
			if(rawdata!=null){
				System.arraycopy(rawdata, 0, data, 0, rawdata.length);
			}
			rawdata = data;
		}
	}

	public int openFile(File file) {
		int ret = 0;
		try {
			if((file.length()/1024)>2000){	//2Mo file size max
				ret=-2;
			}else{
				String filePath = file.getAbsolutePath();
				if (filePath.endsWith("bin")) {
					rawdata = new byte[(int) file.length()];
					DataInputStream dis = new DataInputStream(new FileInputStream(file));
					dis.readFully(rawdata);
					dis.close();
					if(sectionsLock==false){
						sections.clear();
						sections.add(new MemoryHex("Section[ 0x0 ]", 0, rawdata.length, 0));
					}
					sectionsCalculation();
				} else if (filePath.endsWith("hex")) {
					IntelHex hex = IntelHex.importFile(filePath);
					int highSegment = 0;
					for (int i = 0; i < hex.getSegmentsCount(); i++) {
						if(hex.getSegmentStartAddr(i)>hex.getSegmentStartAddr(highSegment)){
							highSegment = i;
						}
					}
					
					//To binary
					int size = hex.getSegmentStartAddr(highSegment) + hex.getSegment(highSegment).length;
					rawdata = initdata(size);
					
					//Copy each segments
					if(sectionsLock==false){
						sections.clear();
					}
					for (int i = 0; i < hex.getSegmentsCount(); i++) {
						int start = hex.getSegmentStartAddr(i);
						if(start>=0){
							byte[] cpy = hex.getSegment(i);
							System.arraycopy(cpy, 0, rawdata, start, cpy.length);
							if(sectionsLock==false){
								sections.add(new MemoryHex("Section[ 0x"+ String.format("%x", start) + " ]", start, cpy.length, 0));
							}
//						}else{
//							System.out.println("Error Start : "+start);
						}
					}
					sectionsCalculation();
				}
				this.filePath = file.getAbsolutePath();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ret = -1;
		} catch (Exception e) {
			e.printStackTrace();
			ret = -10;
		}
		return ret;
	}
	
	public int saveFile() {
		return saveFile(this.filePath);
	}
	public int saveFile(String filePath) {
		int ret = 0;
		try {
			File file = new File(filePath);
			if (filePath.endsWith("bin")) {
				OutputStream output = null;
				try {
					output = new BufferedOutputStream(new FileOutputStream(filePath));
					output.write(rawdata);
				} finally {
					output.close();
				}
			} else if (filePath.endsWith("hex")) {
				IntelHex h = new IntelHex();
				for (int i = 0; i < sections.size(); i++) {
					//Build segments
					Object section = sections.get(i);
					if(section instanceof Memory){
						Memory mem = (Memory) section;
						int start = mem.startAddr;
						//Trim start section
						int j=0;
						for(j=0;j<mem.size;j++){
							if(rawdata[j+start]!=defaultValue){
								break;
							}
						}
						start += j;
						if(j<mem.size){
							//Trim end section
							for(j=mem.size-1-j;j>=0;j--){
								if(rawdata[j+start]!=defaultValue){
									break;
								}
							}
							byte[] cpy = new byte[j+1];
							System.arraycopy(rawdata, start, cpy, 0, cpy.length);
							h.addSegment(start, cpy);
						}
					}
				}
				h.exportFile(filePath);
			}
			this.filePath = file.getAbsolutePath();
		} catch (FileNotFoundException e) {
			ret = -1;
		} catch (Exception e) {
			ret = -10;
		}
		return ret;
	}



}
