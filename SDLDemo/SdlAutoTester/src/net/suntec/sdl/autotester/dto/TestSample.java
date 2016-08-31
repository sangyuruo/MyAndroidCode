package net.suntec.sdl.autotester.dto;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.RPCResponse;

/**
 * 测试取样
 * 
 * @author sangjun
 * @mail yeahsj@gmail.com
 */
public class TestSample {
	/*********** 从文件读取 *************/
	int sampleId;
	String jsonFile;
	List<String> dependIds = null; // hashId
	Map<String, Object> params = null;

	/*********** 解析后设置 *************/
	boolean isDependOther = false;
	String functionName;
	RPCRequest req = null;
	RPCResponse res = null;
	boolean isSuccess = false;
	int caseId;
	int correlationId;
	String hashId;
	JSONObject reqJson;

	long timeout;

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getHashId() {
		return hashId;
	}

	public void setHashId(String hashId) {
		this.hashId = hashId;
	}

	public int getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(int correlationId) {
		this.correlationId = correlationId;
	}

	public JSONObject getReqJson() {
		return reqJson;
	}

	public void setReqJson(JSONObject reqJson) {
		this.reqJson = reqJson;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public int getCaseId() {
		return caseId;
	}

	public void setCaseId(int caseId) {
		this.caseId = caseId;
	}

	public RPCRequest getReq() {
		return req;
	}

	public void setReq(RPCRequest req) {
		this.req = req;
	}

	public RPCResponse getRes() {
		return res;
	}

	public void setRes(RPCResponse res) {
		this.res = res;
	}

	public boolean isDependOther() {
		return isDependOther;
	}

	public void setDependOther(boolean isDependOther) {
		this.isDependOther = isDependOther;
	}

	public int getSampleId() {
		return sampleId;
	}

	public void setSampleId(int sampleId) {
		this.sampleId = sampleId;
	}

	public String getJsonFile() {
		return jsonFile;
	}

	public void setJsonFile(String jsonFile) {
		this.jsonFile = jsonFile;
	}

	public List<String> getDependIds() {
		return dependIds;
	}

	public void setDependIds(List<String> dependences) {
		this.dependIds = dependences;
	}
}
