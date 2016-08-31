package com.livio.sdl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UsbSignal {
	CountDownLatch usbSignal = new CountDownLatch(1);

	public static UsbSignal getInstance() {
		if (null == instance) {
			instance = new UsbSignal();
		}
		return instance;
	}

	public void reset() {
		usbSignal = new CountDownLatch(1);
	}

	public void await() throws InterruptedException {
		usbSignal.await(10, TimeUnit.SECONDS);
	}

	public void countDown() {
		usbSignal.countDown();
	}

	private UsbSignal() {
	}

	static UsbSignal instance = null;
	static {
		instance = getInstance();
	}
}
