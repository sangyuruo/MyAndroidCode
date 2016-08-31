package net.suntec.sdl;

import java.io.File;

public class FileUtil {
	public interface LoopDirFilter {
		public void filter(File file);
	}

	public static void loopDir(File dir, LoopDirFilter filter) {
		File[] listFile = dir.listFiles();
		if (listFile != null) {
			for (int i = 0; i < listFile.length; i++) {
				if (listFile[i].isDirectory()) {
					loopDir(listFile[i], filter);
				} else {
					filter.filter(listFile[i]);
				}
			}
		}
	}
}
