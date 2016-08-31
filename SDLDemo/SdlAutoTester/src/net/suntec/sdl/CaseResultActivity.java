package net.suntec.sdl;

import java.util.ArrayList;
import java.util.List;

import net.suntec.sdl.adapter.SampleTestListAdapter;
import net.suntec.sdl.autotester.TestEnvironment;
import net.suntec.sdl.autotester.dto.TestCase;
import net.suntec.sdl.autotester.dto.TestSample;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.livio.sdl.dialogs.AutoTestJsonFlipperDialog;
import com.livio.sdl.dialogs.BaseAlertDialog;

/**
 * 自动测试Case详细页
 * 
 * @author sangjun
 * @mail yeahsj@gmail.com
 */
public class CaseResultActivity extends Activity {
	ListView lv_autoTest;
	private SampleTestListAdapter sampleTestListAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.case_list);
		init();
		int caseId = this.getIntent().getExtras().getInt("caseId");
		loadDatas(caseId);
	}

	private void init() {
		lv_autoTest = (ListView) findViewById(R.id.list_case_list);
		sampleTestListAdapter = new SampleTestListAdapter(this);
		lv_autoTest.setAdapter(sampleTestListAdapter);
		lv_autoTest.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// when an item is clicked, show it in the JSON flipper dialog.
				// first, we must copy over all the messages that have been
				// created so far.
				int size = sampleTestListAdapter.getCount();
				List<TestSample> allLogs = new ArrayList<TestSample>(size);
				for (int i = 0; i < size; i++) {
					allLogs.add(sampleTestListAdapter.getItem(i));
				}

				BaseAlertDialog jsonDialog = new AutoTestJsonFlipperDialog(
						CaseResultActivity.this, allLogs, position);
				jsonDialog.show();

			}
		});
	}

	private void loadDatas(int caseId) {
		TestCase testCase = TestEnvironment.getInstance().getCases()
				.get(caseId);
		List<TestSample> samples = testCase.getSamples();
		sampleTestListAdapter.addAll(samples);
		sampleTestListAdapter.notifyDataSetChanged();
	}
}
