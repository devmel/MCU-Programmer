package com.devmel.programming.uart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.devmel.communication.IUart;
import com.devmel.communication.nativesystem.Uart;
import com.devmel.programming.IProgramming;

public class ESPUART implements IProgramming {
    /** DEVICE ID */
	public final static String MEMORY_DEVID = "DEVID";
	
	private final InputStream inStream;
	private final OutputStream outStream;
	private boolean state = false;
	private int cmdtimeoutMs = 3000;
	
	private IUart port;

	public ESPUART(InputStream inStream, OutputStream outStream){
		this.inStream=inStream;
		this.outStream=outStream;
	}
	
	public ESPUART(IUart device) throws IOException {
		this(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}
	public ESPUART(Uart device) throws IOException {
		this(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}

	@Override
	public boolean open() throws IOException {
		if(port!=null){
			port.setParameters(115200, IUart.DATABITS_8, IUart.STOPBITS_1, IUart.PARITY_NONE);
			port.open();
		}
		state = sync();
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
		return false;
	}

	@Override
	public String[] getMemories() {
		return new String[]{MEMORY_DEVID};
	}

	@Override
	public byte[] read(String memoryName, int addr, int size) throws IOException {
		byte[] ret = new byte[0];
		if(memoryName!=null){
			if(memoryName.equals(MEMORY_DEVID) || memoryName.startsWith(MEMORY_DEVID+"-")){
				long id = readRegister(0x3ff00050);
				if(id != -1){
					ret = new byte[4];
					ret[3] = (byte) ((id >> 24)&0xff);
					id = readRegister(0x3ff00054);
					ret[2] = (byte) ((id)&0xff);
					ret[1] = (byte) ((id >> 8)&0xff);
					ret[0] = (byte) ((id >> 16)&0xff);
				}
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
		return false;
	}
	
	private byte[] readPacket() throws IOException{
		byte[] ret = null;
		boolean escape = false;
		long end = System.currentTimeMillis();
		long timeout = end;
		end += cmdtimeoutMs;
		try {
			ArrayList<Byte> buffer = null;
			while (ret == null && timeout<end) {
				int c = -1;
				while (inStream.available() <= 0 && timeout<end)
					timeout = System.currentTimeMillis();
				if(inStream.available()>0)
					c = inStream.read();
				if (c < 0) {
					break;
				}else{
					if(buffer == null){
						if(c == 0xc0)
							buffer = new ArrayList<Byte>();
					}else{
						if(escape){
							escape = false;
							if(c == 0xdc)
								buffer.add((byte)(0xc0));
							else if(c == 0xdd)
								buffer.add((byte)(0xdb));
						}else if(c == 0xdb){
							escape = true;
						}else if(c == 0xc0){
							ret = new byte[buffer.size()];
							for (int i = 0; i < ret.length; i++) {
								ret[i] = (byte) buffer.get(i);
							}
						}else{
							buffer.add((byte)(c&0xff));
						}
					}
				}
				timeout = System.currentTimeMillis();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.err.println("<<" + Hexadecimal.fromBytes(ret));
		return ret;
	}

	private boolean writePacket(byte[] packet) throws IOException{
		if(packet != null && packet.length > 0){
			//Encapsulate
			int newSize = packet.length;
			for(int i=0; i < packet.length; i++){
				int d = packet[i]&0xff;
				if(d == 0xdb)
					newSize++;
				if(d == 0xc0)
					newSize++;
			}
			newSize += 2;
			byte[] data = new byte[newSize];
			data[0] = (byte) 0xc0;
			data[data.length - 1] = (byte) 0xc0;
			//Escape
			for(int i=0, j=1; i < packet.length; i++){
				if((packet[i]&0xff) == 0xc0){
					data[j++] = (byte) 0xdb;
					data[j++] = (byte) 0xdc;
				}else{
					data[j++] = packet[i];
					if((packet[i]&0xff) == 0xdb){
						data[j++] = (byte) 0xdd;
					}
				}
			}
			// Write
			//System.err.println(">>"+Hexadecimal.fromBytes(data));
			outStream.write(data);
			outStream.flush();
			return true;
		}
		return false;
	}
	
	private byte[] command(int cmd, byte[] data) throws IOException{
		byte[] ret = null;
		if(cmd > 0){
			int packetSize = 8;
			if(data != null && data.length < 65536)
				packetSize += data.length;
			byte[] packet = new byte[packetSize];
			packet[0] = 0x00;
			packet[1] = (byte) (cmd&0xff);
			packet[2] = 0x00;
			packet[3] = 0x00;
			packet[4] = 0x00;
			packet[5] = 0x00;
			packet[6] = 0x00;
			packet[7] = 0x00;
			if(data != null && data.length < 65536){
				packet[2] = (byte) (data.length&0xff);
				packet[3] = (byte) ((data.length&0xff)>>8);
				System.arraycopy(data, 0, packet, 8, data.length);
			}
			writePacket(packet);
		}
		//Decode response
		byte[] response = null;
		do{
			response = readPacket();
			if(response != null && response.length >= 8 && response[0] == 0x1){
				byte rCmd = response[1];
				int rLen = (response[2]&0xff) + ((response[3]&0xff)<<8);
				if((rCmd == cmd || cmd == 0) && (rLen + 8) == response.length){
					ret = new byte[rLen+4];
					System.arraycopy(response, 4, ret, 0, ret.length);
				}
			}
		}while(response != null && ret == null);
		return ret;
	}
	
	private boolean sync() throws IOException{
		int t = cmdtimeoutMs;
		cmdtimeoutMs /= 2;
		// Purge read
		try{
			inStream.reset();
		} catch (Exception e) {}
		//Prepare sync command
		byte[] data = new byte[36];
		data[0] = 0x07;
		data[1] = 0x07;
		data[2] = 0x12;
		data[3] = 0x20;
		for(int i=0; i < 32; i++){
			data[i+4] = 0x55;
		}
        byte[] ret =  command(0x08, data);
        if(ret == null)
        	ret =  command(0x08, data);
        cmdtimeoutMs = t;
        for(int i = 0; ret != null && i < 7 ; i++){
        	ret = command(0, null);
        }
		return ((ret != null) ? true : false);
	}

	private long readRegister(int addr) throws IOException{
		long reg = -1;
		byte[] data = new byte[]{(byte) ((addr)&0xFF),(byte) ((addr>>8)&0xFF),(byte) ((addr>>16)&0xFF),(byte) ((addr>>24)&0xFF)};
        data = command(0x0a, data);
        if(data != null && data.length >= 6 && data[4] == 0 && data[5] == 0){
			reg = (data[0]&0xff) + ((data[1]&0xff)<<8) + ((data[2]&0xff)<<16) + ((data[3]&0xff)<<24);
			reg &= 0xffffffff;
        }
		return reg;
	}
	
}
