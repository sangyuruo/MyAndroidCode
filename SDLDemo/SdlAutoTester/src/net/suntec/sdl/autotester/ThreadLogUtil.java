package net.suntec.sdl.autotester;

import android.util.Log;

public final class ThreadLogUtil {
	static String TAG = "thread";
	public static void debug(String msg){
		Log.d(TAG,msg);
	}
	
	public static void info(String msg){
		Log.i(TAG,msg);
	}
	
	public static void error(String msg){
		Log.e(TAG,msg);
	}
}
