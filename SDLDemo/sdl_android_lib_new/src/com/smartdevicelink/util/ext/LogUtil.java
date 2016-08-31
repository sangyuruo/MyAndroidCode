package com.smartdevicelink.util.ext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import android.os.Environment;
import android.util.Log;

/**
 * 
 * @author sangjun
 * @mail yeahsj@gmail.com
 */
public class LogUtil {
	public static void debug(String msg) {
		Log.d("debug", msg);
	}

	public static void info(String msg) {
		Log.i("info", msg);
	}

	public static void warm(String msg) {
		Log.w("warm", msg);
	}

	public static void error(String msg) {
		Log.e("error", msg);
	}

	public static void debugEglStep(String msg) {
		Log.i("eglStep", msg);
	}

	public static void debugEncoder(String msg) {
		Log.i("eglEncoder", msg);
	}

	public static void debugWriteSDL(String msg) {
		Log.i("writeSDL", msg);
	}

	public static void debugEglTime(String msg) {
		Log.i("eglTime", msg);
	}

	public static void debugSDLTrans(String msg) {
		Log.i("sdlTrans", msg);
		if (null != logHandler) {
			logHandler.queueMessage(new NaviLogBean("sdlTrans.csv", msg));
		}

	}

	public static void debugSdlService(String msg) {
		Log.i("sdlService", msg);
		if (null != logHandler) {
			logHandler.queueMessage(new NaviLogBean("sdlService.csv", msg));
		}
	}

	public static void debugUSB(String msg) {
		Log.i("usb", msg);
		if (null != logHandler) {
			logHandler.queueMessage(new NaviLogBean("usb.csv", msg));
		}
	}

	public static void debugViaUSB(String msg) {
		if (null != logHandler) {
			logHandler.queueMessage(new NaviLogBean("usbTrans.csv", msg));
		}
	}

	public static void debugNav(String msg) {
		if (null != logHandler) {
			logHandler.queueMessage(new NaviLogBean("nav.csv", msg));
		}
	}

	public synchronized static void writeLogToFile(String filename, String msg) {
		File file = new File(Environment.getExternalStorageDirectory(),
				filename);
		Log.d("EglHelper", Environment.getExternalStorageDirectory().getPath());
		// Environment.getExternalStorageDirectory()获取当前手机默认的sd卡路径
		BufferedWriter out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true)));
			out.write(msg);
			out.newLine();
			out.flush();
			out.close();
		} catch (Exception e) {
			Log.e("EglHelper", "err:" + e.getMessage());
		}
	}

	public void destroyLogThread() {
		if (null != logHandler) {
			logHandler.dispose();
			logHandler = null;
		}
	}

	public void initLogThread() {
		synchronized (LOG_MESSAGE_QUEUE_THREAD_LOCK) {
			// Ensure incomingProxyMessageDispatcher is null
			if (logHandler != null) {
				logHandler.dispose();
				logHandler = null;
			}

			logHandler = new MessageDispatcher<NaviLogBean>(
					"INCOMING_MESSAGE_DISPATCHER",
					new DispatchingStrategy<NaviLogBean>() {
						@Override
						public void dispatch(NaviLogBean message) {
							LogUtil.writeLogToFile(message.getFileName(),
									message.getMsg());
						}

						@Override
						public void handleDispatchingError(String info,
								Exception ex) {
							LogUtil.writeLogToFile("dispatchingError.txt",
									ex.getMessage());
						}

						@Override
						public void handleQueueingError(String info,
								Exception ex) {
							LogUtil.writeLogToFile("queueingError.txt",
									ex.getMessage());
						}
					});
		}
	}

	public static MessageDispatcher<NaviLogBean> getLogHandler() {
		return logHandler;
	}

	public static MessageDispatcher<NaviLogBean> logHandler;
	private static final Object LOG_MESSAGE_QUEUE_THREAD_LOCK = new Object();

	private static LogUtil instance = null;
	static {
		instance = new LogUtil();
	}

	public static LogUtil getInstance() {
		return instance;
	}

	private LogUtil() {
	}
}
