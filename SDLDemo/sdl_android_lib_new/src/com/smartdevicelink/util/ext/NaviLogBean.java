package com.smartdevicelink.util.ext;

public class NaviLogBean {
	String msg;
	String fileName;

	public NaviLogBean(String fileName, String msg) {
		super();
		this.msg = msg;
		this.fileName = fileName;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
