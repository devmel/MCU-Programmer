package com.devmel.programming.linkbus;

import java.io.IOException;

import com.devmel.communication.IUart;
import com.devmel.communication.linkbus.Usart;
import com.devmel.storage.SimpleIPConfig;

public class STM32SERIAL extends com.devmel.programming.uart.STM32SERIAL{
	final Usart port;
	

	public STM32SERIAL(Usart device) throws IOException {
		super(device.getInputStream(), device.getOutputStream());
		this.port = device;
	}
	public STM32SERIAL(byte[] deviceAddr) throws Throwable {
		this(new Usart(deviceAddr));
	}
	public STM32SERIAL(SimpleIPConfig simpleIPData) throws Throwable {
		this(new Usart(simpleIPData));
	}

	@Override
	public boolean open() throws IOException {
		port.setInterruptMode(true, 0);
		port.setParameters(115200, IUart.DATABITS_8, IUart.STOPBITS_1, IUart.PARITY_EVEN);
		port.open();
		port.setVTG(true);
		port.setReset(true);
		port.toggleReset(60);
		return super.open();
	}

	@Override
	public void close() {
		try {
			port.setVTG(false);
			port.toggleReset(60);
		} catch (IOException e) {
		}
		super.close();
		port.close();
	}
}
