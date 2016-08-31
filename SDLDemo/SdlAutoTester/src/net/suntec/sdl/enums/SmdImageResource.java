package net.suntec.sdl.enums;

import net.suntec.sdl.R;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.smartdevicelink.proxy.rpc.enums.FileType;

/**
 * 资源图片
 * 
 * @author html5app
 *
 */
public enum SmdImageResource {
	IC_GO_HOME("GO HOME", FileType.GRAPHIC_PNG, R.drawable.gohome, 1001, true), IC_LIGHT_A(
			"Light a", FileType.GRAPHIC_PNG, R.drawable.lights_a, 1002, true), IC_AC_A(
			"ac a", FileType.GRAPHIC_PNG, R.drawable.ac_a, 1003, true), IC_GARAGE_A(
			"garage a", FileType.GRAPHIC_PNG, R.drawable.garage_a, 1004, true);

	private String friendlyName;
	private int imageId;
	private FileType fileType;
	private int buttonId;
	boolean isHighLight;

	private SmdImageResource(String friendlyName, FileType type, int imageId,
			int buttonId, boolean gohomeFlag) {
		this.friendlyName = friendlyName;
		this.imageId = imageId;
		this.fileType = type;
		this.buttonId = buttonId;
		this.isHighLight = gohomeFlag;
	}

	@Override
	public String toString() {
		return friendlyName;
	}

	public int getImageId() {
		return imageId;
	}

	public FileType getFileType() {
		return fileType;
	}

	public int getButtonId() {
		return buttonId;
	}

	public boolean isHighLight() {
		return isHighLight;
	}

	public Bitmap getBitmap(Resources res) {
		Bitmap result = BitmapFactory.decodeResource(res, imageId);
		return result;
	}

	public static Bitmap getBitmap(Resources res, SmdImageResource image) {
		int resId = image.getImageId();
		Bitmap result = BitmapFactory.decodeResource(res, resId);
		return result;
	}
}
