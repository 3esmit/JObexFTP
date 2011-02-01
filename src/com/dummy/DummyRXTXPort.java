package com.dummy;

import gnu.io.CommPort;
import gnu.io.RXTXPort;
import gnu.io.SerialPortEvent;

import com.dummy.DummySerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.TooManyListenersException;

public class DummyRXTXPort {

    //private RXTXPort serialPort; 
	@SuppressWarnings("restriction")
	private List<DummySerialPortEventListener> listeners = new LinkedList<DummySerialPortEventListener>();
	private InputStream is;
	public boolean polling;
	String name;
    
	public DummyRXTXPort(String name) { 
		this.name = name;
	}

	public void setSerialPortParams(int baudRate, int databits8, int stopbits1,
			int parityNone) throws UnsupportedCommOperationException {
		//serialPort.setSerialPortParams(baudRate, databits8, stopbits1, parityNone);
	}

	public void setEndOfInputChar(byte b) throws UnsupportedCommOperationException {
		//serialPort.setEndOfInputChar(b);
	}

	public void setFlowControlMode(int flowcontrolNone) {
		//serialPort.setFlowControlMode(flowcontrolNone);
	}

	public InputStream getInputStream() {
		//return serialPort.getInputStream();
		try {
			is = new FileInputStream(name);
			InputPoller poller = new InputPoller(is);
			poller.start();
			return is;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public OutputStream getOutputStream() {
		//return serialPort.getOutputStream();
		try {
			return new FileOutputStream(name);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void enableReceiveTimeout(int i) {
		//serialPort.enableReceiveTimeout(i);
	}

	public void setOutputBufferSize(int i) {
		//serialPort.setOutputBufferSize(i);
	}

	public void setInputBufferSize(int i) {
		//serialPort.setInputBufferSize(i);
	}

	public void notifyOnDataAvailable(boolean b) {
		//serialPort.notifyOnDataAvailable(b);
	}

	public void addEventListener(DummySerialPortEventListener eventListener) throws TooManyListenersException {
		//serialPort.addEventListener(eventListener);
		listeners.add(eventListener);
	}

	public void removeEventListener() {
		listeners.clear();
	}

	public void close() {
		polling = false; // no need for sync block?
		//serialPort.close();
	}

	private class InputPoller extends Thread {
		
		private InputStream is;		
		
		public InputPoller(InputStream is) {
			this.is = is;
			polling = true;
		}
		
		@SuppressWarnings("restriction")
		public void run() {
			while (polling) { // no need for sync block?
				//System.out.println("polling");
				try {
					int available = is.available();
					if (available > 0) {
						//System.out.println("" + available + " bytes available");
						for (DummySerialPortEventListener listener: listeners) {
							DummySerialPortEvent event = new DummySerialPortEvent(null, SerialPortEvent.DATA_AVAILABLE, false, false);
							listener.serialEvent(event);
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
					return;
				}
				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}

