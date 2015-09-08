package com.devmel.programming.linkbus;

import java.io.IOException;

import com.devmel.communication.linkbus.Usart;
import com.devmel.storage.SimpleIPConfig;

public class STK500 extends com.devmel.programming.uart.STK500{
	final Usart port;
	

	public STK500(Usart device) throws IOException {
		super(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}
	public STK500(byte[] deviceAddr) throws Throwable {
		this(new Usart(deviceAddr));
	}
	public STK500(SimpleIPConfig simpleIPData) throws Throwable {
		this(new Usart(simpleIPData));
	}

	@Override
	public boolean open() throws IOException {
		port.open();
		//Try multiples rates
		port.setInterruptMode(true, 500);
		port.setReset(true);
		boolean open = false;
		for(int i=0; i<baudrates.length && open==false; i++){
			port.setParameters(baudrates[i], 8, 1, 0);
			port.toggleReset(60);
			open = super.open();
		}
		return open;
	}


	@Override
	public void close() {
		super.close();
		port.close();
	}
}
