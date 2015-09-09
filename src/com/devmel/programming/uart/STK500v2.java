package com.devmel.programming.uart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.devmel.communication.IUart;
import com.devmel.communication.nativesystem.Uart;
import com.devmel.programming.IProgramming;

public class STK500v2 implements IProgramming{
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
	private int seq = 1;
	private boolean state = false;
	private int cmdtimeoutMs = 3000;
	
	private IUart port;

	public STK500v2(InputStream inStream, OutputStream outStream){
		this.inStream=inStream;
		this.outStream=outStream;
	}
	
	public STK500v2(IUart device) throws IOException {
		this(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}
	public STK500v2(Uart device) throws IOException {
		this(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}

	
	@Override
	public boolean open() throws IOException {
		if(port!=null){
			port.setParameters(115200, 8, 1, 0);
			port.open();
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
			}
		}
		if(commandSignOn()!=null){
			state = true;
		}
		return state;
	}

	@Override
	public void close() {
		state=false;
		int t=cmdtimeoutMs;
		cmdtimeoutMs = 100;
		try {
			transfer(new byte[]{0x11});
			outStream.close();
			inStream.close();
		} catch (IOException e) {
		}
		if(port!=null){
			port.close();
		}
		cmdtimeoutMs = t;
	}
	
	@Override
	public String[] getMemories() {
		return new String[]{MEMORY_DEVID,MEMORY_FLASH,MEMORY_EEPROM,MEMORY_FUSES};
	}


	@Override
	public boolean isOpen() {
		return state;
	}

	@Override
	public boolean erase(String memoryName) throws IOException {
		if(transfer(new byte[]{0x12})!=null){
			return true;
		}
		return false;
	}

	@Override
	public byte[] read(String memoryName, int addr, int size) throws IOException {
		byte[] ret = new byte[0];
		if(memoryName!=null){
			if(memoryName.equals(MEMORY_DEVID)){
				ret = new byte[3];
				ret[0] = commandReadSignature(0);
				ret[1] = commandReadSignature(1);
				ret[2] = commandReadSignature(2);
			}else if(memoryName.equals(MEMORY_FLASH)){
				commandLoadAddress((addr>>1)&0xffffff);
				ret = commandRead(size, false);
			}else if(memoryName.equals(MEMORY_EEPROM)){
				commandLoadAddress((addr>>1)&0xffffff);
				ret = commandRead(size, true);
			}else if(memoryName.equals(MEMORY_FUSES)){
				byte[] fuse = new byte[5];
				fuse[0] = commandReadFuse(0);
				fuse[1] = commandReadFuse(1);
				fuse[2] = commandReadFuse(2);
				fuse[3] = commandReadLock();
				fuse[4] = commandReadCalibration();
				ret = fuse;
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
				commandLoadAddress((addr>>1)&0xffffff);
				ret = commandWrite(data, false);
			}else if(memoryName.equals(MEMORY_EEPROM)){
				commandLoadAddress(addr);
				ret = commandWrite(data, true);
			}
		}
		return ret;
	}


	
	public byte[] transfer(byte[] data) throws IOException {
		// Purge read
		try{
			inStream.reset();
		} catch (Exception e) {}

		// Compose header
		byte buf[] = new byte[data.length + 6]; // max MESSAGE_BODY of 275
												// bytes, 6 bytes overhead
		buf[0] = 0x1B; // MESSAGE_START
		buf[1] = (byte) (seq & 0xff); // Sequence
		buf[2] = (byte) ((data.length / 256) & 0xff); // Size
		buf[3] = (byte) ((data.length % 256) & 0xff); // Size
		buf[4] = 0x0E; // TOKEN
		System.arraycopy(data, 0, buf, 5, data.length);
		// calculate the XOR checksum
		buf[5 + data.length] = 0;
		for (int i = 0; i < (5 + data.length); i++) {
			buf[5 + data.length] ^= buf[i];
		}

		// Write
//		System.out.println("Write : " + Hexadecimal.fromBytes(buf));
		outStream.write(buf);
		outStream.flush();

		// Read and decode response
//		System.out.print("Read : ");
		byte[] result = null;
		int msglen = 0;
		try {
			int curlen = 0;
			byte checksum = 0;
			int state = 0;
			long end = System.currentTimeMillis();
			long timeout = end;
			end += cmdtimeoutMs;
			while ((state < 7) && timeout<end) {
				int c = -1;
				while (inStream.available() <= 0 && timeout<end)
					timeout = System.currentTimeMillis();
				if(inStream.available()>0)
					c = inStream.read();
				if (c < 0) {
					break;
				}
//				System.out.print(Hexadecimal.fromByte((byte) c));
			    checksum ^= (byte)c;
	
			    switch (state) {
			      case 0:
			          if (c == 0x1B) {
			              checksum = 0x1B;
			              state = 1;
			          }else{
			        	  Thread.sleep(1);
			          }
			    	  break;
			      case 1:
			          if ((c & 0xff) == (seq & 0xff)) {
			        	  state = 2;
			        	  seq++;
			          }
			    	  break;
			      case 2:
			          msglen = c * 256;
			          state = 3;
			    	  break;
			      case 3:
			          msglen |= c;
//			          System.out.println("Size "+msglen);
			          result = new byte[msglen];
			          state = 4;
			    	  break;
			      case 4:
			    	  if (c == 0x0E){
			    		  state = 5;
			    	  }else{
			    		  state = 0;
			    	  }
			    	  break;
			      case 5:
			    	  if(curlen<msglen){
			    		  result[curlen++] = (byte) (c&0xff);
			    	  }
			    	  if(curlen==msglen){
			    		  state = 6;
			    	  }
			    	  break;
			      case 6:
			    	  if(checksum==0){
				    	  state = 7;
			    	  }else{
			        	  Thread.sleep(1);
			    		  result = null;
				    	  state = 0;
			    	  }
			    	  break;
			    }
			    timeout = System.currentTimeMillis();
			}
		} catch (Exception e) {
//			e.printStackTrace();
		}
//		System.out.println();
//		System.out.println("Read : "+Hexadecimal.fromBytes(result));
		return result;
	}
	
	private byte[] commandSignOn() throws IOException{
		byte[] res = transfer(new byte[] {0x1});
		if(res!=null && res.length>2){
			byte r[] = new byte[res.length-2];
			System.arraycopy(res, 2, r, 0, r.length);
			return r;
		}
		return null;
	}
	private byte commandReadSignature(int index) throws IOException{
		byte ret = 0;
		byte[] res = transfer(new byte[] {0x1B, 0, 0, 0, (byte) (index&0x3) });
		if(res!=null && res.length>2){
			ret = res[2];
		}
		return ret;
	}
	
	private byte commandReadFuse(int index) throws IOException{
		byte h = (byte) ((index!=1)? 0x50 : 0x0);
		byte l = (byte) ((index>1)? 0x08 : 0x0);
		
		byte ret = 0;
		byte[] res = transfer(new byte[] {0x18, 0, h, l });
		if(res!=null && res.length>2){
			ret = res[2];
		}
		return ret;
	}

	private byte commandReadLock() throws IOException{
		byte ret = 0;
		byte[] res = transfer(new byte[] {0x1A});
		if(res!=null && res.length>2){
			ret = res[2];
		}
		return ret;
	}
	
	private byte commandReadCalibration() throws IOException{
		byte ret = (byte) 0xff;
		byte[] res = transfer(new byte[] {0x05});
		if(res!=null && res.length>1){
			ret = res[1];
		}
		return ret;
	}


	private boolean commandLoadAddress(int wordAddress) throws IOException{
		boolean ret = false;
		byte[] cmd = new byte[]{0x06, (byte) ((wordAddress>>24)&0xFF), (byte) ((wordAddress>>16)&0xFF), (byte) ((wordAddress>>8)&0xFF), (byte) ((wordAddress)&0xFF) };
		byte[] res = transfer(cmd);
		if(res!=null && res.length>=2){
			ret = true;
		}
		return ret;
	}

	private byte[] commandRead(int size, boolean eeprom) throws IOException{
		byte ret[] = null;
		if(size<274){
			byte[] res = transfer(new byte[] {(byte) (eeprom==false ? 0x14 : 0x16), (byte) ((size>>8)&0xff), (byte) (size&0xff)});
			if(res!=null && res.length>=3){
				ret = new byte[res.length-3];
				System.arraycopy(res, 2, ret, 0, ret.length);
			}
		}
		return ret;
	}
	
	private boolean commandWrite(byte[] data, boolean eeprom) throws IOException{
		byte[] buffer = new byte[data.length+10];
		buffer[0] = (byte) (eeprom==false ? 0x13 : 0x15);
		buffer[1] = (byte) ((data.length / 256) & 0xff); // Size
		buffer[2] = (byte) ((data.length % 256) & 0xff); // Size
		System.arraycopy(data, 0, buffer, 10, data.length);

		byte[] res = transfer(buffer);
		if(res!=null && res.length>=2){
			return true;
		}
		return false;
	}

}
