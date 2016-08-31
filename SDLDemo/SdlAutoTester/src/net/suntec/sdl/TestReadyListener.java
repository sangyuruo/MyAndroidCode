package net.suntec.sdl;

public class TestReadyListener {
	private volatile boolean hmiReady = false;
	private volatile boolean testCaseReady = false;

	static TestReadyListener instance;
	static {
		instance = new TestReadyListener();
	}

	private TestReadyListener() {

	}

	public static TestReadyListener getInstance() {
		return instance;
	}

	public synchronized void hmiReady() {
		hmiReady = true;
		if (testCaseReady) {
			MainActivity.getInstance().showStartBtn();
		}
	}

	public synchronized void testCaseReady() {
		testCaseReady = true;
		if (hmiReady) {
			MainActivity.getInstance().showStartBtn();
		}
	}

}
