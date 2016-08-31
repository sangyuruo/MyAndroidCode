package net.suntec.sdl;

import java.util.Collection;

import net.suntec.sdl.adapter.CaseTestListAdapter;
import net.suntec.sdl.autotester.TestEnvironment;
import net.suntec.sdl.autotester.dto.TestCase;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * 自动测试 case list 页
 * 
 * @author sangjun
 * @mail yeahsj@gmail.com
 */
public class CaseListActivity extends Activity {
	
	ListView lv_caseList;
	private CaseTestListAdapter caseTestListViewAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.case_list);
		init();
		loadDatas();
	}

	private void loadDatas(){
		Collection<TestCase> cases = TestEnvironment.getInstance().getCases().values();
		caseTestListViewAdapter.addAll(cases);
		caseTestListViewAdapter.notifyDataSetChanged();
	}
	
	private void init() {
		lv_caseList = (ListView) findViewById(R.id.list_case_list);
		
		caseTestListViewAdapter = new CaseTestListAdapter(this);
		lv_caseList.setAdapter(caseTestListViewAdapter);
		lv_caseList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TestCase testCase = caseTestListViewAdapter.getItem(position);
				Intent intent = new Intent(CaseListActivity.this, CaseResultActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.setAction(Intent.ACTION_RUN);
				intent.putExtra("caseId", testCase.getCaseId() );
				CaseListActivity.this.startActivity(intent);
			}
		});
	}
}
