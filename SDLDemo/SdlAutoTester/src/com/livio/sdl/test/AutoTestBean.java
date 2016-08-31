package com.livio.sdl.test;

import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.RPCResponse;

/**
 * 测试case
 * 
 * @author sangjun
 * @mail yeahsj@gmail.com
 */
public class AutoTestBean {
	int correlationID;
	String filePath;
	RPCRequest request;
	RPCResponse response;
	boolean isSuccess;

	public int getCorrelationID() {
		return correlationID;
	}

	public void setCorrelationID(int correlationID) {
		this.correlationID = correlationID;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public RPCRequest getRequest() {
		return request;
	}

	public void setRequest(RPCRequest request) {
		this.request = request;
	}

	public RPCResponse getResponse() {
		return response;
	}

	public void setResponse(RPCResponse response) {
		this.response = response;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

}
