package com.devmel.programming.uart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.devmel.communication.IUart;
import com.devmel.communication.nativesystem.Uart;
import com.devmel.programming.IProgramming;

public class STK500 implements IProgramming{
    /** DEVICE ID */
	public final static String MEMORY_DEVID = "DEVID";
	
    /** FLASH MEMORY */
	public final static String MEMORY_FLASH = "FLASH";
	
    /** EEPROM MEMORY */
	public final static String MEMORY_EEPROM = "EEPROM";
		
	public final int[] baudrates = new int[]{19200,57600,115200,38400,9600,4800,1200};
	private final InputStream inStream;
	private final OutputStream outStream;
	private boolean state = false;
	private int cmdtimeoutMs = 3000;
	
	private IUart port;

	public STK500(InputStream inStream, OutputStream outStream){
		this.inStream=inStream;
		this.outStream=outStream;
	}
	
	public STK500(IUart device) throws IOException {
		this(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}
	public STK500(Uart device) throws IOException {
		this(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}

	@Override
	public boolean open() throws IOException {
		if(port!=null){
			boolean open = false;
			for(int i=0; i<baudrates.length && open==false; i++){
				try{
					port.setParameters(baudrates[i], 8, 1, 0);
					port.open();
					open = enterProg();
				}catch(IOException e){
				}
			}
			state = open;
		}else{
			state = enterProg();
		}
		return state;
	}

	@Override
	public void close() {
		int t=cmdtimeoutMs;
		cmdtimeoutMs = 100;
		state=false;
		try {
			transfer(new byte[]{'Q',' '},0);
			outStream.close();
			inStream.close();
		} catch (IOException e) {
		}
		if(port!=null){
			port.close();
		}
		cmdtimeoutMs = t;
	}
	
	public byte[] version() throws IOException{
		return transfer(new byte[]{'1',' '},7);
	}
	
	@Override
	public String[] getMemories() {
		return new String[]{MEMORY_DEVID,MEMORY_FLASH,MEMORY_EEPROM};
	}


	@Override
	public boolean isOpen() {
		return state;
	}

	@Override
	public boolean erase(String memoryName) throws IOException {
		if(transfer(new byte[]{'R',' '},0)!=null){
			return true;
		}
		return false;
	}

	@Override
	public byte[] read(String memoryName, int addr, int size) throws IOException {
		byte[] ret = new byte[0];
		if(memoryName!=null){
			if(memoryName.equals(MEMORY_DEVID)){
				ret = transfer(new byte[]{'u',' '},3);
			}else if(memoryName.equals(MEMORY_FLASH)){
				commandLoadAddress((addr>>1)&0xffff);
				ret = commandRead(size, false);
			}else if(memoryName.equals(MEMORY_EEPROM)){
				commandLoadAddress((addr>>1)&0xffff);
				ret = commandRead(size, true);
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
			if(memoryName.equals(MEMORY_FLASH)){
				commandLoadAddress((addr>>1)&0xffff);
				ret = commandWrite(data, false);
			}else if(memoryName.equals(MEMORY_EEPROM)){
				commandLoadAddress((addr>>1)&0xffff);
				ret = commandWrite(data, true);
			}else{
				throw new IOException("Not supported");
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
//			System.out.print("Read : ");
			int state = 0;
			result = new byte[resultSize];
			try {
				int curlen = 0;
				long end = System.currentTimeMillis();
				long timeout = end;
				end += cmdtimeoutMs;
				while ((state < 3) && timeout<end) {
					int c = 0;
					if(inStream.available()>0)
						c = inStream.read();
					if (c < 0) {
						break;
					}

					switch (state) {
				      case 0:
				          if (c == 0x14) {
				        	  if(resultSize>0){
				        		  state = 1;
				        	  }else{
				        		  state = 2;
				        	  }
				          }else{
				        	  Thread.sleep(1);
				          }
				    	  break;
				      case 1:
				    	  if(curlen<resultSize){
				    		  result[curlen++] = (byte) (c&0xff);
				    	  }
				    	  if(curlen==resultSize){
				    		  state = 2;
				    	  }
				    	  break;
				      case 2:
				    	  //Check 0x10 last byte
				          if (c == 0x10) {
				              state = 3;
				          }else{
				              state = 0;
				          }
				    	  break;
				    }
				    timeout = System.currentTimeMillis();
				}
			} catch (Exception e) {
	//			e.printStackTrace();
			}
			if(state!=3){
				result = null;
			}
	//		System.out.println();
	//		System.out.println("Read : "+Hexadecimal.fromBytes(result));
		}
		return result;
	}
	
	private boolean enterProg() throws IOException{
		boolean ret = false;
		int t=cmdtimeoutMs;
		cmdtimeoutMs = 150;
		//Enter programming mode
		transfer(new byte[]{'P',' '},0);
		//Enter programming mode
		if(read(MEMORY_DEVID,0,0)!=null){
			ret=true;
		}
		cmdtimeoutMs = t;
		return ret;
	}
	
	private byte[] commandRead(int size, boolean eeprom) throws IOException{
		byte ret[] = null;
		if(size<=256){
			ret = transfer(new byte[] {'t' , (byte) ((size>>8)&0xff), (byte) (size&0xff), (byte) (eeprom==false ? 'F' : 'E'), ' '}, size);
		}
		return ret;
	}

	private boolean commandLoadAddress(int wordAddress) throws IOException{
		byte[] cmd = new byte[]{'U', (byte) ((wordAddress)&0xFF), (byte) ((wordAddress>>8)&0xFF), ' '};
		if(transfer(cmd,0)!=null){
			return true;
		}
		return false;
	}

	private boolean commandWrite(byte[] data, boolean eeprom) throws IOException{
		byte[] buffer = new byte[data.length+5];
		buffer[0] = 'd';
		buffer[1] = (byte) ((data.length / 256) & 0xff); // Size
		buffer[2] = (byte) ((data.length % 256) & 0xff); // Size
		buffer[3] = (byte) (eeprom==false ? 'F' : 'E');
		System.arraycopy(data, 0, buffer, 4, data.length);
		buffer[buffer.length-1] = ' ';

		if(transfer(buffer,0)!=null){
			return true;
		}
		return false;
	}

}
