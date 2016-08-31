package com.livio.sdl.utils;

import android.util.Log;

public final class LogUtil {
	static String TAG_SDL_SERVICE = "SdlService";
	static boolean DEBUG_SDL_SERVICE = true;

	public static void debugSdlService(String msg) {
		if (DEBUG_SDL_SERVICE) {
			Log.d(TAG_SDL_SERVICE, msg);
		}
	}
}
