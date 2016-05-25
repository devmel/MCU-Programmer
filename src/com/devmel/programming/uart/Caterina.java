package com.devmel.programming.uart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.devmel.communication.IUart;
import com.devmel.communication.nativesystem.Uart;
import com.devmel.programming.IProgramming;

public class Caterina implements IProgramming {
    /** DEVICE ID */
	public final static String MEMORY_DEVID = "DEVID";
	
    /** FLASH MEMORY */
	public final static String MEMORY_FLASH = "FLASH";
	
    /** EEPROM MEMORY */
	public final static String MEMORY_EEPROM = "EEPROM";
	
    /** FUSES BITS (Low ; High ; Extended ; Lock ; Calibration) */
	public final static String MEMORY_FUSES = "FUSES";

	private final InputStream inStream;
	private final OutputStream outStream;
	private boolean state = false;
	private int cmdtimeoutMs = 3000;
	
	private IUart port;

	public Caterina(InputStream inStream, OutputStream outStream){
		this.inStream=inStream;
		this.outStream=outStream;
	}
	
	public Caterina(IUart device) throws IOException {
		this(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}
	public Caterina(Uart device) throws IOException {
		this(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}

	@Override
	public boolean open() throws IOException {
		if(port!=null){
			try{
				port.setParameters(19200, 8, 1, 0);
				port.open();
			}catch(IOException e){
			}
		}
		state = (transfer(new byte[]{'P'}) == '\r') ? true : false;
		return state;
	}

	@Override
	public boolean isOpen() {
		return state;
	}

	@Override
	public void close() {
		if(port!=null){
			port.close();
		}
		state=false;
	}

	@Override
	public boolean erase(String memoryName) throws IOException {
		return ((transfer(new byte[]{'e'}) == '\r') ? true : false);
	}

	@Override
	public String[] getMemories() {
		return new String[]{MEMORY_DEVID,MEMORY_FLASH,MEMORY_EEPROM,MEMORY_FUSES};
	}

	@Override
	public byte[] read(String memoryName, int addr, int size) throws IOException {
		byte[] ret = new byte[0];
		if(memoryName!=null){
			if(memoryName.equals(MEMORY_DEVID) || memoryName.startsWith(MEMORY_DEVID+"-")){
				byte[] id = transfer(new byte[]{'s'}, 3);
				if(id != null){
					ret = reverseByteArray(id);
				}
			}else if(memoryName.startsWith(MEMORY_FLASH) || memoryName.startsWith(MEMORY_FLASH+"-")){
				if(transfer(new byte[]{'A', (byte) ((addr>>9)&0xFF), (byte) ((addr>>1)&0xFF)}) == '\r'){
					ret = transfer(new byte[]{'g', (byte) ((size>>8)&0xFF), (byte) ((size)&0xFF), 'F'}, size);
				}
			}else if(memoryName.equals(MEMORY_EEPROM) || memoryName.startsWith(MEMORY_EEPROM+"-")){
				if(transfer(new byte[]{'A', (byte) ((addr>>8)&0xFF), (byte) ((addr)&0xFF)}) == '\r'){
					ret = transfer(new byte[]{'g', (byte) ((size>>8)&0xFF), (byte) ((size)&0xFF), 'E'}, size);
				}
			}else if(memoryName.equals(MEMORY_FUSES) || memoryName.startsWith(MEMORY_FUSES+"-")){
				byte[] fuse = new byte[5];
				fuse[0] = (byte) (transfer(new byte[]{'F'}) &0xff);
				fuse[1] = (byte) (transfer(new byte[]{'N'}) &0xff);
				fuse[2] = (byte) (transfer(new byte[]{'Q'}) &0xff);
				fuse[3] = (byte) (transfer(new byte[]{'r'}) &0xff);
				fuse[4] = (byte) 0xff;
				ret = fuse;
			}else{
				throw new IOException("Not supported");
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
			String memory = memoryName;
			int blProtection = -1;
			String[] mem = memoryName.split("-");
			if(mem!=null && mem.length>1){
				memory = mem[0];
				blProtection = Integer.decode(mem[1]);
			}
			if(memory.equals(MEMORY_FLASH)){
				if(blProtection < 0 || addr < blProtection){
					ret = writeBlock(addr>>1, data, false);
				}
			}else if(memory.equals(MEMORY_EEPROM)){
				ret = writeBlock(addr, data, true);
			}
		}
		return ret;
	}

	private int transfer(byte[] data) throws IOException {
		int ret = -1;
		byte[] result = transfer(data, 1);
		if(result != null && result.length == 1){
			ret = result[0];
		}
		return ret;
	}
	private byte[] transfer(byte[] data, int resultSize) throws IOException {
		byte[] result = null;
		// Purge read
		try{
			inStream.reset();
		} catch (Exception e) {}

		// Write
		outStream.write(data);
		outStream.flush();

		// Read
		if(resultSize>=0){
			result = new byte[resultSize];
			int readSize = 0;
			long end = System.currentTimeMillis();
			long timeout = end;
			end += cmdtimeoutMs;
			try {
				while ((readSize < resultSize) && timeout<end) {
					int c = -1;
					while (inStream.available() <= 0 && timeout<end)
						timeout = System.currentTimeMillis();
					if(inStream.available()>0)
						c = inStream.read();
					if (c < 0) {
						break;
					}else{
						result[readSize++] = (byte) (c&0xff);
					}
				}
			} catch (Exception e) {
	//			e.printStackTrace();
			}
			if(readSize != resultSize){
				result = null;
			}
		}
		return result;
	}
	
	private boolean writeBlock(int addr, byte[] data, boolean eeprom) throws IOException {
		boolean ret = false;
		if(transfer(new byte[]{'A', (byte) ((addr>>8)&0xFF), (byte) ((addr)&0xFF)}) == '\r'){
			byte[] tr = new byte[data.length + 4];
			tr[0] = 'B';
			tr[1] = (byte) ((data.length>>8)&0xFF);
			tr[2] = (byte) ((data.length)&0xFF);
			tr[3] = (byte) ((eeprom==true) ? 'E' : 'F');
			System.arraycopy(data, 0, tr, 4, data.length);
			ret = (transfer(tr) == '\r') ? true : false;
		}
		return ret;
	}


	private static byte[] reverseByteArray(byte[] original) {
		byte[] ret = new byte[original.length];
	    for (int i = original.length-1, j = 0; i >= 0; i--, j++) {
	    	ret[j] = original[i];
	    }
	    return ret;
	}
}
