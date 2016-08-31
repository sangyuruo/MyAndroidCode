package net.suntec.sdl.autotester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import net.suntec.sdl.AppConstant;
import net.suntec.sdl.FileUtil;
import net.suntec.sdl.FileUtil.LoopDirFilter;
import net.suntec.sdl.autotester.dto.TestCase;
import net.suntec.sdl.autotester.dto.TestSample;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.livio.sdl.test.SdlIdFactory;
import com.smartdevicelink.util.ext.LogUtil;

/**
 * 测试环境准备
 * 
 * @author sangjun
 * @mail yeahsj@gmail.com
 */
public class TestEnvironment {
	String sdCardPath;
	// static String APP_AUTO_TEST_DIR = "sdl_auto_test";
	// static String GLOBAL_FILE_SUFFIX = ".json";
	// static String RPC_FILE_SUFFIX = ".rpc";
	// static String TEST_CASE_FILE_SUFFIX = ".case";
	List<File> globalFiles = new ArrayList<File>();
	List<File> rpcFiles = new ArrayList<File>();
	// List<File> privateRpcFiles = new ArrayList<File>();
	List<File> caseFiles = new ArrayList<File>();

	public Map<Integer, TestCase> cases = new TreeMap<Integer, TestCase>();

	Map<String, Object> globalParams = null;
	// key: hashId , value: sample
	public volatile Map<String, TestSample> samples = new TreeMap<String, TestSample>();
	// key: fileName , value: json
	public Map<String, JSONObject> rpcs = new HashMap<String, JSONObject>();
	// 收集所有的被依赖sample( 注意采用 hash(caseId, sampleId)
	public Set<String> isDependedSampleList = new HashSet<String>();

	public Map<Integer, CountDownLatch> caseDownLatchs = new HashMap<Integer, CountDownLatch>();
	public Map<String, CountDownLatch> sampleDownLatchs = new HashMap<String, CountDownLatch>();
	static TestEnvironment instance = null;

	public Map<Integer, String> correlationIdMap = new HashMap<Integer, String>();

	public int findCaseId(String hashId) {
		if (samples.containsKey(hashId)) {
			TestSample sample = samples.get(hashId);
			return sample.getCaseId();
		} else {
			return 0;
		}
	}

	public TestCase findCase(String hashId) {
		if (samples.containsKey(hashId)) {
			int caseId = samples.get(hashId).getCaseId();
			if (cases.containsKey(caseId)) {
				return cases.get(caseId);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public TestSample findTestSample(int correlationId) {
		if (null != correlationIdMap
				&& correlationIdMap.containsKey(correlationId)) {
			String hashId = correlationIdMap.get(correlationId);
			if (null != samples && samples.containsKey(hashId)) {
				return samples.get(hashId);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public TestSample findTestSample(String hashId) {
		if (null != samples && samples.containsKey(hashId)) {
			return samples.get(hashId);
		} else {
			return null;
		}
	}

	public void updateTestSample(String hashId, TestSample sample) {
		samples.put(hashId, sample);
	}

	public void init() {
		loadAllFile();
		parseGlobalFile();
		parseRpcFile();
		parseCaseFile();
	}

	public void loadAllFile() {
		String rootPath = sdCardPath + File.separator
				+ AppConstant.APP_AUTO_TEST_DIR;
		File globalFile = new File(rootPath + File.separator + "global");
		File caseFile = new File(rootPath + File.separator + "case");
		File rpcFile = new File(rootPath + File.separator + "rpc");
		FileUtil.loopDir(globalFile, new LoopDirFilter() {
			@Override
			public void filter(File file) {
				if (file.getName().endsWith(AppConstant.GLOBAL_FILE_SUFFIX)) {
					globalFiles.add(file);
				}
			}
		});

		FileUtil.loopDir(caseFile, new LoopDirFilter() {
			@Override
			public void filter(File file) {
				if (file.getName().endsWith(AppConstant.TEST_CASE_FILE_SUFFIX)) {
					caseFiles.add(file);
				}
			}
		});

		FileUtil.loopDir(rpcFile, new LoopDirFilter() {
			@Override
			public void filter(File file) {
				if (file.getName().endsWith(AppConstant.RPC_FILE_SUFFIX)) {
					rpcFiles.add(file);
				}
			}
		});
	}

	public void parseGlobalFile() {
		if (!globalFiles.isEmpty()) {
			globalParams = new HashMap<String, Object>();
			for (File jsonFile : globalFiles) {
				String jsonStr = loadJSONFromFile(jsonFile);
				LogUtil.debug(jsonFile.getName());
				if (null != jsonStr && !jsonStr.isEmpty()) {
					LogUtil.debug(jsonStr);
					JSONObject json;
					try {
						json = new JSONObject(jsonStr);
						Map<String, Object> params = jsonToMap(json);
						globalParams.putAll(params);
						// globalParams = jsonToMap(json);
					} catch (JSONException e) {
						LogUtil.error(e.getLocalizedMessage());
					} catch (IllegalArgumentException e) {
						LogUtil.error(e.getLocalizedMessage());
					}
				}
			}
		}
	}

	public void parseRpcFile() {
		this.parseRpcFile(rpcFiles);
	}

	public void parseRpcFile(List<File> rpcFiles) {
		for (File jsonFile : rpcFiles) {
			String jsonStr = loadJSONFromFile(jsonFile);
			jsonStr = jsonStr.replaceAll("\r|\n", "");
			LogUtil.debug(jsonFile.getName());
			if (null != jsonStr && !jsonStr.isEmpty()) {
				LogUtil.debug(jsonStr);
				JSONObject json;
				try {
					json = new JSONObject(jsonStr);
					rpcs.put(jsonFile.getName(), json);
					// Hashtable<String, Object> hash = RPCStruct
					// .deserializeJSONObject(json);
					// MyJsonRPCMarshaller mJsonRPCMarshaller = new
					// MyJsonRPCMarshaller();
					// Hashtable<String, Object> hash = mJsonRPCMarshaller
					// .deserializeJSONObject(json);
					// if (mJsonRPCMarshaller.hasInsert) {
					//
					// } else {
					// String rpcName = json.getJSONObject("request")
					// .getString("name");
					// RPCRequest rpc = (RPCRequest) RpcRequestMapper
					// .get(rpcName).getConstructor(Hashtable.class)
					// .newInstance(hash);
					// rpcs.put(jsonFile.getName(), rpc);
					// }
				} catch (JSONException e) {
					LogUtil.error(e.getLocalizedMessage());
				} catch (IllegalArgumentException e) {
					LogUtil.error(e.getLocalizedMessage());
				}
			}
		}
	}

	public void parseCaseFile() {
		for (File file : caseFiles) {
			String jsonStr = loadJSONFromFile(file);
			if (null != jsonStr && !jsonStr.isEmpty()) {
				try {
					JSONObject json = new JSONObject(jsonStr);
					int caseId = json.getInt("caseId");
					String caseName = json.getString("caseName");
					JSONArray jsonSamples = json.getJSONArray("samples");
					int sampleSize = jsonSamples.length();
					List<TestSample> samples = new ArrayList<TestSample>(
							sampleSize);
					for (int i = 0; i < sampleSize; i++) {
						JSONObject sampleJson = (JSONObject) jsonSamples.get(i);
						TestSample testSample = parseSample(sampleJson, caseId);
						samples.add(testSample);
					}
					TestCase testCase = new TestCase();
					testCase.setCaseId(caseId);
					testCase.setCaseName(caseName);
					testCase.setSamples(samples);

					if (json.has("params")) {
						JSONObject paramsJson = json.getJSONObject("params");
						Map<String, Object> params = jsonToMap(paramsJson);
						testCase.setParams(params);
					} else {
						testCase.setParams(null);
					}
					cases.put(caseId, testCase);
				} catch (IllegalArgumentException e) {
					LogUtil.error(e.getLocalizedMessage());
				} catch (JSONException e) {
					LogUtil.error(e.getLocalizedMessage());
				}
			}
		}

	}

	private JSONObject findRpcJson(String jsonFile) {
		if (rpcs.containsKey(jsonFile)) {
			return rpcs.get(jsonFile);
		} else {
			return null;
		}
	}

	public String hash(int caseId, int sampleId) {
		return caseId + "-" + sampleId;
	}

	public TestSample parseSample(JSONObject sampleJson, int caseId)
			throws JSONException {
		TestSample testSample = new TestSample();
		int sampleId = sampleJson.getInt("id");
		int correlationId = SdlIdFactory.getNextId();
		String hashId = hash(caseId, sampleId);
		String jsonFile = sampleJson.getString("file");
		if (sampleJson.has("timeout")) {
			long timeout = sampleJson.getLong("timeout");
			testSample.setTimeout(timeout);
		} else {
			testSample.setTimeout(AutoTestConstant.CASE_WAIT_TIME_OUT);
		}
		testSample.setCaseId(caseId);
		testSample.setCorrelationId(correlationId);
		testSample.setSampleId(sampleId);
		testSample.setJsonFile(jsonFile);
		testSample.setReqJson(findRpcJson(jsonFile));
		testSample.setHashId(hashId);

		if (sampleJson.has("depends")) {
			JSONArray depends = sampleJson.getJSONArray("depends");
			int dependSize = depends.length();
			List<String> dependSamples = new ArrayList<String>(dependSize);
			for (int i = 0; i < dependSize; i++) {
				if (depends.get(i) instanceof Integer) {
					int dependSampleId = depends.getInt(i);
					String dependHashId = hash(caseId, dependSampleId);
					dependSamples.add(dependHashId);
					isDependedSampleList.add(dependHashId);
				} else if (depends.get(i) instanceof String) {
					String dependHashId = depends.getString(i);
					dependSamples.add(dependHashId);
					isDependedSampleList.add(dependHashId);
				}
			}
			testSample.setDependIds(dependSamples);
			testSample.setDependOther(true);
		} else {
			testSample.setDependOther(false);
		}

		if (sampleJson.has("params")) {
			JSONObject paramsJson = sampleJson.getJSONObject("params");
			Map<String, Object> params = jsonToMap(paramsJson);
			testSample.setParams(params);
		} else {
			testSample.setParams(null);
		}

		// TODO key change sampleId to correlationId
		samples.put(hashId, testSample);
		correlationIdMap.put(correlationId, hashId);
		return testSample;
	}

	// Json生成Map
	public Map<String, Object> jsonToMap(JSONObject jsonObject)
			throws JSONException {
		// JSONObject必须以"{"开头
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Iterator<String> iter = jsonObject.keys();
		String key = null;
		Object value = null;
		while (iter.hasNext()) {
			key = iter.next();
			value = jsonObject.get(key);
			resultMap.put(key, value);
		}
		return resultMap;
	}

	/**
	 * 读取json文件
	 * 
	 * @param jsonFileName
	 * @return
	 */
	public String loadJSONFromFile(File file) {
		String json = null;
		try {
			InputStream is = new FileInputStream(file);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			json = new String(buffer, "UTF-8");
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;
	}

	public void putCaseDownLatch(int key, CountDownLatch countDownLatch) {
		caseDownLatchs.put(key, countDownLatch);
	}

	public CountDownLatch getCaseDownLatch(int key) {
		return caseDownLatchs.get(key);
	}

	public void countDownCaseDownLatch(int key) {
		caseDownLatchs.get(key).countDown();
	}

	public void putSampleDownLatch(String key, CountDownLatch countDownLatch) {
		sampleDownLatchs.put(key, countDownLatch);
	}

	public CountDownLatch getSampleDownLatch(String key) {
		return sampleDownLatchs.get(key);
	}

	public boolean containSampleDownLatch(String key) {
		return sampleDownLatchs.containsKey(key);
	}

	public void countDownSampleDownLatch(String key) {
		sampleDownLatchs.get(key).countDown();
	}

	public void addIsDependedSampleList(String hashId) {
		isDependedSampleList.add(hashId);
	}

	public void addIsDependedSampleList(List<String> hashIds) {
		isDependedSampleList.addAll(hashIds);
	}

	public boolean isDependByOtherSample(String hashId) {
		return isDependedSampleList.contains(hashId);
	}

	public void runSampleTest(final SampleTestService service) {
		Runnable sampleThread = new Runnable() {
			@Override
			public void run() {
				service.runTest();
			}
		};
		new Thread(sampleThread).start();
	}

	public Map<Integer, TestCase> getCases() {
		return cases;
	}

	public void setCases(Map<Integer, TestCase> cases) {
		this.cases = cases;
	}

	public List<TestSample> getSamples() {
		List<TestSample> smp = new ArrayList<TestSample>();
		smp.addAll(samples.values());
		return smp;
	}

	public Map<String, Object> getGlobalParams() {
		return globalParams;
	}

	public void setGlobalParams(Map<String, Object> globalParams) {
		this.globalParams = globalParams;
	}

	public Set<String> getIsDependedSampleList() {
		return isDependedSampleList;
	}

	private TestEnvironment(String sdCardPath) {
		this.sdCardPath = sdCardPath;
	}

	public static TestEnvironment getInstance() {
		return instance;
	}

	public static TestEnvironment createOnce(String sdCardPath) {
		if (null != instance) {
			instance.clear();
			instance = null;
		}
		instance = new TestEnvironment(sdCardPath);
		return instance;
	}

	private void clear() {
		// TODO Auto-generated method stub
		rpcFiles = new ArrayList<File>();
		caseFiles = new ArrayList<File>();
		cases = new TreeMap<Integer, TestCase>();
		samples = new HashMap<String, TestSample>();
		rpcs = new HashMap<String, JSONObject>();
		// 收集所有的被依赖sample
		isDependedSampleList = new HashSet<String>();
		caseDownLatchs = new HashMap<Integer, CountDownLatch>();
		sampleDownLatchs = new HashMap<String, CountDownLatch>();
	}

}
