package net.suntec.sdl.adapter;

import java.util.List;

import net.suntec.sdl.R;
import net.suntec.sdl.autotester.dto.TestCase;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CaseTestListAdapter extends ArrayAdapter<TestCase> {

	public CaseTestListAdapter(Context context, List<TestCase> objects) {
		super(context, R.layout.case_test_listview_row, objects);
	}

	public CaseTestListAdapter(Context context) {
		super(context, R.layout.case_test_listview_row);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.case_test_listview_row, null);
		}

		TestCase item = getItem(position);

		if (item != null) {
			populateView(view, item);
		}

		return view;
	}

	/**
	 * Populate the input parent view with information from the SDL log message.
	 * 
	 * @param view
	 *            The view to populate
	 * @param item
	 *            The data with which to populate the view
	 */
	private void populateView(View view, TestCase item) {
		TextView tv_caseId = (TextView) view.findViewById(R.id.tv_caseId);
		TextView tv_caseName = (TextView) view.findViewById(R.id.tv_caseName);

		// set text values based on input message
		try {
			tv_caseId.setText(String.valueOf(item.getCaseId()));
			tv_caseName.setText(item.getCaseName());
			boolean isSuccess = item.isSuccess();
			if (isSuccess) {
				tv_caseId.setTextColor(RESPONSE_SUCCESS_COLOR);
				tv_caseName.setTextColor(RESPONSE_SUCCESS_COLOR);
			} else {
				tv_caseId.setTextColor(RESPONSE_FAILURE_COLOR);
				tv_caseName.setTextColor(RESPONSE_FAILURE_COLOR);
			}
		} catch (Exception ex) {
			Log.e("error", ex.getMessage());
		}
		
	}

	private static final int RESPONSE_SUCCESS_COLOR = 0xFF2D9C08; // dark green
	private static final int RESPONSE_FAILURE_COLOR = Color.RED;

}
