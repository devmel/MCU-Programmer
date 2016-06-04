package com.devmel.apps.mcuprogrammer.sections;

import java.io.IOException;

import com.devmel.programming.IProgramming;

public class MemoryHex extends Memory{
	public final static int maxBlockSize = 128;
	public int type;	//0=No programming ; 1=Read only program ; 2=Program
	public int readStartAddr=0;
	public int writeStartAddr=0;
	public int progAddress;
	public byte[] blank;
	
	public MemoryHex(String name, int startAddr, int readStartAddr, int writeStartAddr, int size, int type, byte[] blank){
		super(name,startAddr,size);
		this.type=type;
		this.readStartAddr = readStartAddr;
		this.writeStartAddr = writeStartAddr;
		this.blank = blank;
	}
	
	public MemoryHex(String name, int startAddr, int size, int type){
		super(name,startAddr,size);
		this.type=type;
		this.blank = new byte[]{(byte) 0xff};
	}
	
	public int verify(IProgramming program, byte[] rawdata, int fromAddr) throws IOException{
		int ret = -1;
		if (program!=null && program.isOpen()==true) {
			progAddress = fromAddr;
			while (progAddress < this.size && !Thread.interrupted()){
				int blockSize = this.size-progAddress;
				if(blockSize>maxBlockSize){
					blockSize = maxBlockSize;
				}
				
				byte[] data = program.read(this.name, readStartAddr+progAddress, blockSize);
				if (data != null) {
					if(data.length==0){
						ret = -4;
						break;
					}
					// Max data
					int maxSize = data.length;
					if ((this.startAddr + progAddress + maxSize) > rawdata.length) {
						maxSize = rawdata.length - this.startAddr - progAddress;
					}
					
					//Compare data
					int eq = -1;
					for(int i=0; i<data.length;i++){
						if(data[i]!=rawdata[i+this.startAddr + progAddress]){
							eq=i+this.startAddr + progAddress;
							break;
						}
					}
					if(eq>=0){
						ret = eq;
						break;
					}
					progAddress += maxSize;
					if(status != null)
						status.update(progAddress, size);
				} else {
					ret = -2;
					break;
				}
			}
		}
		if(ret>0){
			ret -= this.startAddr;
		}
		return ret;
	}
	
	public int read(IProgramming program, byte[] rawdata, int fromAddr) throws IOException{
		int ret = -1;
		if (program!=null && program.isOpen()==true) {
			progAddress = fromAddr;
			while (progAddress < this.size && !Thread.interrupted()){
				int blockSize = this.size-progAddress;
				if(blockSize>maxBlockSize){
					blockSize = maxBlockSize;
				}
				
				byte[] data = program.read(this.name, readStartAddr+progAddress, blockSize);
				if (data != null) {
					if(data.length==0){
						ret = -3;
						break;
					}
					// Max data
					int maxSize = data.length;
					if ((this.startAddr + progAddress + maxSize) > rawdata.length) {
						maxSize = rawdata.length - this.startAddr - progAddress;
					}
					System.arraycopy(data, 0, rawdata, this.startAddr + progAddress, maxSize);
					progAddress += maxSize;
					if(status != null)
						status.update(progAddress, size);
					ret = 0;
				} else {
					ret = -1;
					break;
				}
			}
		}
		return ret;
	}
	public int write(IProgramming program, byte[] rawdata, int fromAddr) throws IOException{
		int ret = -1;
		if (program!=null && program.isOpen()==true) {
			progAddress = fromAddr;
			while (progAddress < this.size && !Thread.interrupted()){
				// Build block
				int maxSize = maxBlockSize;
				if ((this.startAddr + progAddress + maxSize) > rawdata.length) {
					maxSize = rawdata.length - this.startAddr - progAddress;
				}
				if ((progAddress + maxSize) > this.size) {
					maxSize = this.size - progAddress;
				}

				byte[] page = new byte[maxSize];
				System.arraycopy(rawdata, this.startAddr+progAddress, page, 0, page.length);
				
				//Check page
				boolean write = true;
				//TODO check from blank (default value to check not only 0xff)
				for(int i=0;i<page.length;i++){
					if(page[i]!=(byte)0xff){
						write=false;
						break;
					}
				}

				if(write==false){
					write = program.write(this.name, writeStartAddr+progAddress, page);
				}
				if (write) {
					progAddress += maxBlockSize;
					if(status != null)
						status.update(progAddress, size);
					ret = 0;
				} else {
					ret = -1;
					break;
				}
				
			}
		}
		return ret;
	}


}
