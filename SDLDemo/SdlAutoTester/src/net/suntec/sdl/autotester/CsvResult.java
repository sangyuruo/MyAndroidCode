package net.suntec.sdl.autotester;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.suntec.sdl.autotester.dto.TestCase;
import net.suntec.sdl.autotester.dto.TestSample;

public class CsvResult {
	String baseFold; // 文件基础目录
	String fileName;
	// = "result.csv";
	String filePath;

	public CsvResult(String baseFold) {
		this.baseFold = baseFold;
	}

	public void init() {
		Date d = new Date();
		SimpleDateFormat foldFormat = new SimpleDateFormat("yyyyMMdd");
		String dateNowStr = foldFormat.format(d);
		SimpleDateFormat filenameFormat = new SimpleDateFormat("HHmmss");
		String fileNameStr = filenameFormat.format(d);

		fileName = fileNameStr + ".csv";
		filePath = baseFold + File.separator + dateNowStr;
		File fold = new File(filePath);
		if (!fold.exists()) {
			fold.mkdirs();
		}
	}

	public void writeResult() {
		File gpxfile = new File(filePath, fileName);
		FileWriter writer = null;
		try {
			writer = new FileWriter(gpxfile);
			writeHead(writer);
			Collection<TestCase> cases = TestEnvironment.getInstance()
					.getCases().values();
			for (TestCase testCase : cases) {
				writeCase(writer, testCase);
				List<TestSample> samples = testCase.getSamples();
				if (null == samples || samples.size() == 0) {
					continue;
				}
				for (TestSample testSample : samples) {
					writeSample(writer, testSample);
				}
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
		}

	}

	private void writeHead(FileWriter writer) throws IOException {
		writer.append("case id");
		writer.append(',');
		writer.append("case name");
		writer.append(',');
		writer.append("case result");
		writer.append(',');
		writer.append("sample id");
		writer.append(',');
		writer.append("rpc name");
		writer.append(',');
		writer.append("sample result");
		writer.append(',');
		writer.append("sample result code");
		writer.append(',');
		writer.append("sample msg");
		writer.append('\n');
	}

	private void writeCase(FileWriter writer, TestCase testCase)
			throws IOException {
		writer.append("" + testCase.getCaseId());
		writer.append(',');
		writer.append(testCase.getCaseName());
		writer.append(',');
		writer.append("" + testCase.isSuccess());
		writer.append('\n');
	}

	private void writeSample(FileWriter writer, TestSample testSample)
			throws IOException {
		writer.append(',');
		writer.append(',');
		writer.append(',');
		writer.append("" + testSample.getSampleId());
		writer.append(',');
		writer.append("" + testSample.getFunctionName());
		writer.append(',');
		writer.append("" + testSample.isSuccess());
		if (!testSample.isSuccess()) {
			writer.append(',');
			writer.append("" + testSample.getRes().getResultCode());
			writer.append(',');
			writer.append("" + testSample.getRes().getInfo());
		}
		writer.append('\n');
	}
}
