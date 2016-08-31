package net.suntec.sdl.autotester;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.suntec.sdl.MainActivity;
import net.suntec.sdl.autotester.dto.TestCase;
import net.suntec.sdl.autotester.dto.TestSample;
import net.suntec.sdl.rpc.RpcRequestMapper;
import ognl.OgnlContext;

import org.json.JSONObject;

import com.smartdevicelink.proxy.RPCRequest;

/**
 * 
 * @author sangjun
 * @mail   yeahsj@gmail.com
 */
public class SampleTestService {
	TestEnvironment testEnvironment = null;
	TestSample testSample;
	MainActivity mainActivity;

	public SampleTestService(TestSample testSample,
			TestEnvironment testEnvironment, MainActivity mainActivity) {
		super();
		this.testSample = testSample;
		this.testEnvironment = testEnvironment;
		this.mainActivity = mainActivity;
	}

	public void runSampleTest() {
		testEnvironment.runSampleTest(this);
	}

	public void runTest() {
		String hashId = testSample.getHashId();
		int correlationId = testSample.getCorrelationId();
		boolean isDependOthers = testSample.isDependOther();
//		boolean isDependByOthers = testEnvironment.isDependByOtherSample(sampleId);
//		if (isDependByOthers) {
//			CountDownLatch sampleSign = new CountDownLatch(1);
//			testEnvironment.putSampleDownLatch(sampleId, sampleSign);
//			ThreadLogUtil.debug( "sample(" + sampleId + ") is depended by other sample" );
//		}

		List<TestSample> dependsSamples = null;
		if (isDependOthers) {
			List<String> depnedHashIds = testSample.getDependIds();
			for (String dependSampleHashId : depnedHashIds) {
				ThreadLogUtil.debug( "sample(" + hashId + ") is depend other sample(" + dependSampleHashId + ")" );
				try {
					boolean isSuccess = testEnvironment.getSampleDownLatch(dependSampleHashId).await(
							AutoTestConstant.CASE_WAIT_TIME_OUT,
							AutoTestConstant.WAIT_TIME_UNIT);
					ThreadLogUtil.debug( "sample(" + hashId + ") wait depend sample(" + dependSampleHashId + ")  success? " + isSuccess );
				} catch (InterruptedException e) {
					ThreadLogUtil.debug( "sample(" + hashId + ") wait depend sample(" + dependSampleHashId + ")  throw: " + e.getMessage() );
				}
			}
			dependsSamples = new ArrayList<TestSample>();
			//获取依赖Sample
			for (String dependSampleHashId : depnedHashIds) {
				TestSample dpd = testEnvironment.findTestSample(dependSampleHashId);
				dependsSamples.add(dpd);
			} 
		}
		TestCase testCase = testEnvironment.getCases().get( testSample.getCaseId() );
		
		OgnlContext context = new OgnlContext();
		context.put("depends", dependsSamples);
		context.put("sample", testSample.getParams());
		context.put("case", testCase.getParams());
		context.put("global", testEnvironment.getGlobalParams() );
		context.setRoot(dependsSamples);
		JSONObject json = testSample.getReqJson();
		try {
			String rpcName = json.getJSONObject("request")
					.getString("name");
			Hashtable<String, Object> hash = MyJsonRPCMarshaller
					.deserializeJSONObject(json, context);
			if( null == hash ){
				testSample.setReq(null);
			}else{
				RPCRequest req = (RPCRequest) RpcRequestMapper
						.get(rpcName).getConstructor(Hashtable.class)
						.newInstance(hash);
				testSample.setReq(req);
			}
		} catch (Exception e) {
			testSample.setReq(null);
			ThreadLogUtil.error( "Sample runTest err:" + e.getMessage() );
		} 
		
		// TODO Rpc Test
		RPCRequest req = testSample.getReq();
		if( null == req ){
			testSample.setSuccess(false);
			testSample.setRes( null );
		}else{
			testSample.setFunctionName( req.getFunctionName() );
			req.setCorrelationID( correlationId );
			mainActivity.sendSdlMessageToService(req);
		}
	}
	
}
