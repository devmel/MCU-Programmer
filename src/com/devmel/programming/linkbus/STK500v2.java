package com.devmel.programming.linkbus;

import java.io.IOException;

import com.devmel.communication.linkbus.Usart;
import com.devmel.storage.SimpleIPConfig;

public class STK500v2 extends com.devmel.programming.uart.STK500v2{
	final Usart port;
	

	public STK500v2(Usart device) throws IOException {
		super(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}
	public STK500v2(byte[] deviceAddr) throws Throwable {
		this(new Usart(deviceAddr));
	}
	public STK500v2(SimpleIPConfig simpleIPData) throws Throwable {
		this(new Usart(simpleIPData));
	}

	@Override
	public boolean open() throws IOException {
		port.setInterruptMode(true, 0);
		port.setParameters(115200, 8, 1, 0);
		port.open();

		port.setReset(true);
		port.toggleReset(60);
		
		try {
			Thread.sleep(600);
		} catch (InterruptedException e) {
		}
		return super.open();
	}


	@Override
	public void close() {
		super.close();
		port.close();
	}
}
