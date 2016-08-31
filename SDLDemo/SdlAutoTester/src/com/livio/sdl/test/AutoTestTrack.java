package com.livio.sdl.test;

import java.util.ArrayList;
import java.util.List;

import android.util.SparseArray;

import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.RPCResponse;

/**
 * 收集测试结果
 * 
 * @author sangjun
 * @mail yeahsj@gmail.com
 */
public class AutoTestTrack {
	private SparseArray<AutoTestBean> testBeans = new SparseArray<AutoTestBean>();
	private static AutoTestTrack instance;

	public void init() {
		testBeans = new SparseArray<AutoTestBean>();
	}

	public void init(int capacity) {
		testBeans = new SparseArray<AutoTestBean>(capacity);
	}

	public void add(int correlationID, AutoTestBean autoTestBean) {
		testBeans.append(correlationID, autoTestBean);
	}

	public void update(int correlationID, AutoTestBean autoTestBean) {
		testBeans.put(correlationID, autoTestBean);
	}

	public void update(int correlationID, RPCRequest request) {
		AutoTestBean autoTestBean = testBeans.get(correlationID);
		if (null != autoTestBean) {
			autoTestBean.setRequest(request);
			testBeans.put(correlationID, autoTestBean);
		}
	}

	public void update(RPCResponse response) {
		int correlationID = response.getCorrelationID();
		AutoTestBean autoTestBean = testBeans.get(correlationID);
		if (null != autoTestBean) {
			autoTestBean.setResponse(response);
			autoTestBean.setSuccess(response.getSuccess());
			testBeans.put(correlationID, autoTestBean);
		}
	}

	public void update(int correlationID, RPCResponse response) {
		AutoTestBean autoTestBean = testBeans.get(correlationID);
		if (null != autoTestBean) {
			autoTestBean.setResponse(response);
			autoTestBean.setSuccess(response.getSuccess());
			testBeans.put(correlationID, autoTestBean);
		}
	}

	public AutoTestBean get(int correlationID) {
		return testBeans.get(correlationID);
	}

	public List<AutoTestBean> list() {
		List<AutoTestBean> list = new ArrayList<AutoTestBean>();
		int size = testBeans.size();
		for (int i = 0; i < size; i++) {
			list.add(testBeans.valueAt(i));
		}
		return list;
	}

	static {
		instance = new AutoTestTrack();
	}

	public static AutoTestTrack getInstance() {
		return instance;
	}

	private AutoTestTrack() {
		testBeans = new SparseArray<AutoTestBean>();
	}

}
