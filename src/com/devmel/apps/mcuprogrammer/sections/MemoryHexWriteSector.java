package com.devmel.apps.mcuprogrammer.sections;

import java.io.IOException;

import com.devmel.programming.IProgramming;

public class MemoryHexWriteSector extends MemoryHex {
	public int pageSize;

	public MemoryHexWriteSector(String name, int startAddr, int readStartAddr, int writeStartAddr, int size, int pageSize,
			int type, byte[] blank) {
		super(name, startAddr, readStartAddr, writeStartAddr, size, type, blank);
		this.pageSize = pageSize;
	}

	@Override
	public int write(IProgramming program, byte[] rawdata, int fromAddr) throws IOException{
		int ret = -1;
		if (program!=null && program.isOpen()==true) {
			progAddress = fromAddr;
			while ( progAddress < this.size && !Thread.interrupted()){
				// Build page
				int maxSize = pageSize;
				if ((this.startAddr + progAddress + maxSize) > rawdata.length) {
					ret = -3;	//Page size error
					break;
				}
				byte[] page = new byte[maxSize];
				System.arraycopy(rawdata, this.startAddr + progAddress, page, 0, page.length);
				
				//Check page
				boolean write = true;
				//TODO (default value to check not only 0xff)
				for(int i=0;i<page.length;i++){
					if(page[i]!=(byte)0xff){
						write=false;
						break;
					}
				}

				if(write==false){
					write = program.writeSector(this.name, writeStartAddr+progAddress, page);
				}
				if (write) {
					progAddress += pageSize;
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
