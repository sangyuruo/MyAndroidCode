package net.suntec.sdl.autotester;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import net.suntec.sdl.AppConstant;
import net.suntec.sdl.MainActivity;
import net.suntec.sdl.autotester.dto.TestCase;
import net.suntec.sdl.autotester.dto.TestSample;
import android.os.Environment;

/**
 * 
 * @author sangjun
 * @mail yeahsj@gmail.com
 */
public class CaseTestService implements Runnable {
	TestEnvironment testEnvironment = null;
	MainActivity mainActivity;

	public CaseTestService(MainActivity mainActivity,
			TestEnvironment testEnvironment) {
		super();
		this.testEnvironment = testEnvironment;
		this.mainActivity = mainActivity;
	}

	public void run() {
		// create depended sample sign
		Set<String> dependedSamples = testEnvironment.getIsDependedSampleList();
		for (String hashId : dependedSamples) {
			CountDownLatch sampleSign = new CountDownLatch(1);
			testEnvironment.putSampleDownLatch(hashId, sampleSign);
			ThreadLogUtil.debug("sample(" + hashId
					+ ") is depended by other sample");
		}
		// start case test
		Collection<TestCase> cases = testEnvironment.getCases().values();
		for (TestCase testCase : cases) {
			runTestCase(testCase);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		mainActivity.printAutoSampleTestResult(testEnvironment.getSamples());

		exportToFiles(testEnvironment.getSamples());
	}

	private void exportToFiles(List<TestSample> samples) {
		// TODO Auto-generated method stub
		File root = Environment.getExternalStorageDirectory();
		String rootPath = root.getAbsolutePath() + File.separator
				+ AppConstant.APP_AUTO_TEST_DIR + File.separator + "result";
		CsvResult csvResult = new CsvResult(rootPath);
		csvResult.init();
		csvResult.writeResult();
	}

	private void runTestCase(TestCase testCase) {
		List<TestSample> samples = testCase.getSamples();
		int sampleSize = samples.size();
		int caseId = testCase.getCaseId();
		long timeout = 0;
		// AutoTestConstant.CASE_WAIT_TIME_OUT * sampleSize;
		for (TestSample testSample : samples) {
			timeout += testSample.getTimeout();
		}

		CountDownLatch caseSign = new CountDownLatch(sampleSize);

		testEnvironment.putCaseDownLatch(caseId, caseSign);
		for (TestSample testSample : samples) {
			SampleTestService service = new SampleTestService(testSample,
					testEnvironment, mainActivity);
			service.runSampleTest();
		}

		try {
			boolean isSuccess = caseSign.await(timeout,
					AutoTestConstant.WAIT_TIME_UNIT);
			// TODO check case success
			boolean caseSuccess = true;
			for (TestSample testSample : samples) {
				if (!testSample.isSuccess()) {
					caseSuccess = false;
					break;
				}
			}
			testCase.setSuccess(caseSuccess);
			ThreadLogUtil.debug("case(" + caseId + ") run success? "
					+ isSuccess);
		} catch (InterruptedException e) {
			ThreadLogUtil.debug("case(" + caseId + ") run throw:"
					+ e.getLocalizedMessage());
		}
	}

}
