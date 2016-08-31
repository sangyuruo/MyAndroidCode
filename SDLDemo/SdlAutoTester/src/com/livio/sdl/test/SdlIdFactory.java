package com.livio.sdl.test;

import java.util.concurrent.atomic.AtomicInteger;

import com.livio.sdl.utils.UpCounter;

/**
 * 
 * @author sangjun
 * @mail   yeahsj@gmail.com
 */
public class SdlIdFactory {
	private static SdlIdFactory instance;
	protected static AtomicInteger correlationIdGenerator;
//	protected static UpCounter correlationIdGenerator = new UpCounter(100); // id

	// generator
	// for
	// correlation
	// ids

	public static SdlIdFactory getInstance() {
		return instance;
	}

	public static int getNextId() {
		return correlationIdGenerator.getAndIncrement();
	}
	
	public static void reset(){
		correlationIdGenerator = new AtomicInteger(100);
	}

	static {
		instance = new SdlIdFactory();
		correlationIdGenerator = new AtomicInteger(100);
	}

	private SdlIdFactory() {

	}
}
