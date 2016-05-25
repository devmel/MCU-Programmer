package com.devmel.programming.uart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.devmel.communication.IUart;
import com.devmel.communication.nativesystem.Uart;
import com.devmel.programming.IProgramming;

public class STM32SERIAL implements IProgramming{
    /** DEVICE ID */
	public final static String MEMORY_DEVID = "DEVID";
	
    /** FLASH MEMORY */
	public final static String MEMORY_FLASH = "FLASH";

	private final int blockSize = 256;
	private final InputStream inStream;
	private final OutputStream outStream;
	private byte[] state = null;
	private int cmdtimeoutMs = 4000;
	
	private IUart port;

	public STM32SERIAL(InputStream inStream, OutputStream outStream){
		this.inStream=inStream;
		this.outStream=outStream;
	}
	
	public STM32SERIAL(IUart device) throws IOException {
		this(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}
	public STM32SERIAL(Uart device) throws IOException {
		this(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}

	@Override
	public boolean open() throws IOException {
		if(port!=null){
			port.setParameters(57600, IUart.DATABITS_8, IUart.STOPBITS_1, IUart.PARITY_EVEN);
			port.open();
		}
		if(enterProg()){
			byte[] data = new byte[13];
			if(transfer(0x00, null, data, true) == 1){
				state = data;
			}
		}
		return isOpen();
	}

	@Override
	public void close() {
		int t=cmdtimeoutMs;
		cmdtimeoutMs = 100;
		state=null;
		try {
			outStream.close();
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(port!=null){
			port.close();
		}
		cmdtimeoutMs = t;
	}

	public int version(){
		if(isOpen()){
			return (state[1]&0xff);
		}
		return 0;
	}
	
	@Override
	public String[] getMemories() {
		return new String[]{MEMORY_DEVID,MEMORY_FLASH};
	}


	@Override
	public boolean isOpen() {
		return (state != null) ? true : false;
	}

	@Override
	public boolean erase(String memoryName) throws IOException {
		boolean erased = false;
		if(isOpen()){
			//Erase
			if(state[8] == 0x43){
				byte[][] data = new byte[1][];
				data[0] = new byte[]{(byte) 0xff};
				if(transfer(0x43, data, null, false) == 1){
					erased = true;
				}
			}else if(state[8] == 0x44){
				byte[][] data = new byte[1][];
				data[0] = new byte[]{(byte) 0xff, (byte) 0xff};
				if(transfer(0x44, data, null, false) == 1){
					erased = true;
				}
			}
			//RDP unprotect if not erased
			if(erased == false){
				if(transfer(0x92, null, null, true) == 1){
					erased = true;
				}
			}
		}
		return erased;
	}

	@Override
	public byte[] read(String memoryName, int addr, int size) throws IOException {
		byte[] ret = new byte[0];
		if(memoryName!=null){
			if(memoryName.equals(MEMORY_DEVID) || memoryName.startsWith(MEMORY_DEVID+"-")){
				ret = commandGetID();
			}else if(memoryName.equals(MEMORY_FLASH) || memoryName.startsWith(MEMORY_FLASH+"-")){
				ret = commandReadMemory(addr + 0x8000000, size);
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
			if(memoryName.equals(MEMORY_FLASH) || memoryName.startsWith(MEMORY_FLASH+"-")){
				if(commandWriteMemory(addr + 0x8000000, data) > 0){
					ret = true;
				}
			}else{
				throw new IOException("Not supported");
			}
		}
		return ret;
	}
	
	
	private boolean enterProg() throws IOException{
		// Purge read
		try{
			inStream.reset();
		} catch (Exception e) {}
		//Send the command 0x7f
		outStream.write(new byte[]{0x7f});
		outStream.flush();
		//Wait for the ACK
		int ack = waitForAck();
		if(ack == 0x0){
			//Read device ID
			if(read(MEMORY_DEVID,0,0) != null){
				return true;
			}
		}else if(ack == 0x1){
			return true;
		}
		return false;
	}
	
	private int waitForAck() throws IOException{
		int ack = -1;
		long end = System.currentTimeMillis();
		long timeout = end;
		end += cmdtimeoutMs;
		while (ack == -1 && timeout<end) {
			while (inStream.available() <= 0 && timeout<end)
				timeout = System.currentTimeMillis();
			if(inStream.available()>0)
				ack = inStream.read();
			if (ack < 0) {
				break;
			}
			if(ack != 0x79 && ack != 0x1f){
				ack = -1;
			}
		    timeout = System.currentTimeMillis();
		}
		//ACK
		if(ack == 0x79){
			return 0x1;
		//NACK
		}else if(ack == 0x1f){
			return 0x0;
		}
		// Purge read
		try{
			inStream.reset();
		} catch (Exception e) {}
		return -1;
	}
	
	private int transfer(int command, byte[][] data, byte[] result, boolean lastAck) throws IOException{
		int ret = -1;
		// Purge read
		try{
			inStream.reset();
		} catch (Exception e) {}
		// Write with crc
		outStream.write(new byte[]{(byte) (command&0xff), (byte) (0xff^(command&0xff))});
		outStream.flush();
		//Wait for ACK
		ret = waitForAck();
		//Send data
		if(ret == 1 && data != null && data.length > 0){
			for(int i = 0; ret == 1 && i < data.length ; i++){
				if(data[i] != null && data[i].length > 0){
					byte[] tr = new byte[data[i].length + 1];
					//Calculate crc and send
					int crc = 0;
					for(int j = 0; j < data[i].length; j++){
						crc ^= data[i][j];
					}
					if(data[i].length == 1){
						crc = 0xff ^ crc;
					}
					tr[data[i].length] = (byte) (crc&0xff);
					System.arraycopy(data[i], 0, tr, 0, data[i].length);
					outStream.write(tr);
					outStream.flush();
					ret = waitForAck();
				}
			}
		}
		//Read the answer
		if(ret == 1 && result != null && result.length > 0){
			int curlen = 0;
			long end = System.currentTimeMillis();
			long timeout = end;
			end += cmdtimeoutMs;
			while (curlen < result.length && timeout<end) {
				int c = -1;
				while (inStream.available() <= 0 && timeout<end)
					timeout = System.currentTimeMillis();
				if(inStream.available()>0)
					c = inStream.read();
				if (c < 0) {
					break;
				}
				result[curlen++] = (byte) (c&0xff);
			    timeout = System.currentTimeMillis();
			}
			if(curlen < result.length){
				ret = -1;
			}
		}
		//Wait for ACK
		if(ret == 1 && lastAck){
			ret = waitForAck();
		}
		return ret;
	}
	
	private byte[] commandGetID() throws IOException{
		byte[] ret = new byte[3];
		if(transfer(0x02, null, ret, true) == 1){
			if(ret[0] == 0x1){
				ret = new byte[]{ret[1], ret[2]};
			}else{
				ret = null;
			}
		}else{
			ret = null;
		}
		return ret;
	}
	private byte[] commandReadMemory(int addr, int size) throws IOException{
		byte[] ret = new byte[size];
		int blocks = size / blockSize;
		blocks += (size > 0) ? 1 : 0;
		int endAddr = addr + size;
		for(int i = 0; i < blocks; i++){
			int toRead = endAddr - addr;
			if(toRead > blockSize)
				toRead = blockSize;
			byte[][] data = new byte[2][];
			byte[] block = new byte[toRead];
			data[0] = new byte[]{(byte) ((addr>>24)&0xFF), (byte) ((addr>>16)&0xFF), (byte) ((addr>>8)&0xFF), (byte) ((addr)&0xFF)};
			data[1] = new byte[]{(byte) (block.length -1)};
			if(transfer(0x11, data, block, false) != 1){
				return null;
			}
			System.arraycopy(block, 0, ret, i * blockSize, block.length);
			addr += toRead;
		}
		return ret;
	}
	
	private int commandWriteMemory(int addr, byte[] memory) throws IOException{
		int written = 0;
		if(memory != null && (memory.length%4) == 0){
			int blocks = memory.length / blockSize;
			blocks += (memory.length > 4) ? 1 : 0;
			for(int i = 0; i < blocks; i++){
				int done = commandWriteMemoryBlock(addr+written, memory, written);
				if(done > 0){
					written += done;
				}else{
					break;
				}
			}
		}
		return written;
	}
	private int commandWriteMemoryBlock(int addr, byte[] memory, int memoryOffset) throws IOException{
		int written = 0;
		int toWrite = (memory.length - memoryOffset);
		if(toWrite > blockSize)
			toWrite = blockSize;
		if(memory != null && toWrite > 0){
			byte[][] data = new byte[2][];
			data[0] = new byte[]{(byte) ((addr>>24)&0xFF), (byte) ((addr>>16)&0xFF), (byte) ((addr>>8)&0xFF), (byte) ((addr)&0xFF)};
			data[1] = new byte[toWrite + 1];
			data[1][0] = (byte) ((toWrite&0xff) - 1);
			System.arraycopy(memory, memoryOffset, data[1], 1, toWrite);
			if(transfer(0x31, data, null, false) == 1){
				written += toWrite;
			}
		}
		return written;
	}
}
