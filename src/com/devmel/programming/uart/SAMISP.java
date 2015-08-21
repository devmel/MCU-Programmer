package com.devmel.programming.uart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.devmel.communication.IUart;
import com.devmel.communication.nativesystem.Uart;
import com.devmel.programming.IProgramming;

public class SAMISP implements IProgramming{
    /** DEVICE ID */
	public final static String MEMORY_DEVID = "DEVID";
	
    /** FLASH MEMORY */
	public final static String MEMORY_FLASH = "FLASH";
	
	
	protected final InputStream inStream;
	protected final OutputStream outStream;
	private boolean state = false;
	
	private IUart port;


	public SAMISP(InputStream inStream, OutputStream outStream){
		this.inStream=inStream;
		this.outStream=outStream;
	}
	
	public SAMISP(IUart device) throws IOException {
		this(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}
	public SAMISP(Uart device) throws IOException {
		this(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}
	
	@Override
	public boolean open() throws IOException {
		if(port!=null){
			port.open();
			port.setParameters(115200, 8, 1, 0);
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
			}
		}
		byte[] ret = transfer(new byte[]{(byte) 0x80,(byte) 0x80,'#'}, 3);
		ret = transfer(new byte[]{'N','#'}, 2);
		if(ret!=null && ret.length==2 && ret[0]==0x0a){
			state=true;
		}
		return state;
	}

	@Override
	public void close() {
		state=false;
		try {
			outStream.close();
			inStream.close();
		} catch (IOException e) {
		}
		if(port!=null){
			port.close();
		}
	}
	
	@Override
	public String[] getMemories() {
		return new String[]{MEMORY_DEVID,MEMORY_FLASH};
	}


	@Override
	public boolean isOpen() {
		return state;
	}

	@Override
	public boolean erase(String memoryName) throws IOException {
		return false;
	}

	@Override
	public byte[] read(String memoryName, int addr, int size) throws IOException {
		byte[] ret = new byte[0];
		if(memoryName!=null){
			if(memoryName.equals(MEMORY_DEVID)){
				ret = readWord(0x400e0940);
				byte r = ret[0];
				ret[0]=ret[3];
				ret[3]=r;
				r=ret[1];
				ret[1]=ret[2];
				ret[2]=r;
			}else if(memoryName.equals(MEMORY_FLASH)){
				
				
			}
		}
		return ret;
	}

	@Override
	public boolean write(String memoryName, int addr, byte[] data) throws IOException {
		return false;
	}

	@Override
	public boolean writeSector(String memoryName, int addr, byte[] data) throws IOException {
		boolean ret = false;
		if(memoryName!=null && data!=null){
			if(memoryName.equals(MEMORY_FLASH)){
			}
		}
		return ret;
	}
	
	public byte[] transfer(byte[] data, int resultSize) throws IOException {
		byte[] result = null;
		// Purge read
		try{
			inStream.reset();
		} catch (Exception e) {}

		// Write
//		System.out.println("Write : " + Hexadecimal.fromBytes(data));
		outStream.write(data);
		outStream.flush();

		// Read and decode response
		if(resultSize>=0){
	//		System.out.print("Read : ");
			result = new byte[resultSize];
			int curlen = 0;
			try {
				long end = System.currentTimeMillis();
				long timeout = end;
				end += 3000;
				while (curlen<resultSize && timeout<end) {
					int c = inStream.read();
					if (c < 0) {
						break;
					}
//					System.out.print(Hexadecimal.fromByte((byte) c));
					result[curlen++] = (byte) (c&0xff);
				    timeout = System.currentTimeMillis();
				}
			} catch (Exception e) {
//				e.printStackTrace();
			}
			if(curlen!=resultSize){
				result = null;
			}
	//		System.out.println();
	//		System.out.println("Read : "+Hexadecimal.fromBytes(result));
		}
		return result;
	}
	
	private byte[] readWord(long addr) throws IOException{
	    byte[] cmd = (String.format("w%08X,4#", addr)).getBytes();
		return transfer(cmd, 4);
	}

}
