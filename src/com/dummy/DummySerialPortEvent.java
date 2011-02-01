package com.dummy;

import gnu.io.SerialPort;

public class DummySerialPortEvent {

	private int eventType;

	public DummySerialPortEvent(SerialPort arg0, int arg1, boolean arg2,
			boolean arg3) {
		eventType = arg1;
	}

	public int getEventType() {
		return eventType;
	}
	
}
