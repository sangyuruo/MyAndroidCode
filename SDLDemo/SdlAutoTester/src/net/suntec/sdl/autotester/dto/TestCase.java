package net.suntec.sdl.autotester.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestCase {
	int caseId;
	String caseName;
	List<TestSample> samples = new ArrayList<TestSample>();
	Map<String, Object> params = null;
	boolean isSuccess = false;

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public int getCaseId() {
		return caseId;
	}

	public void setCaseId(int caseId) {
		this.caseId = caseId;
	}

	public String getCaseName() {
		return caseName;
	}

	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}

	public List<TestSample> getSamples() {
		return samples;
	}

	public void setSamples(List<TestSample> samples) {
		this.samples = samples;
	}

}
