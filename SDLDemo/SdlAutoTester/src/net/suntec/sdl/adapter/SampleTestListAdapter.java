package net.suntec.sdl.adapter;

import java.util.List;

import net.suntec.sdl.R;
import net.suntec.sdl.autotester.dto.TestSample;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SampleTestListAdapter extends ArrayAdapter<TestSample> {

	public SampleTestListAdapter instance;

	public SampleTestListAdapter(Context context, List<TestSample> objects) {
		super(context, R.layout.sample_test_listview_row, objects);
	}

	public SampleTestListAdapter(Context context) {
		super(context, R.layout.sample_test_listview_row);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.sample_test_listview_row, null);
		}

		TestSample item = getItem(position);

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
	private void populateView(View view, TestSample item) {
		TextView tv_sampleId = (TextView) view.findViewById(R.id.tv_sampleId);
		TextView tv_rpcName = (TextView) view.findViewById(R.id.tv_rpcName);
		TextView tv_rpcFileName = (TextView) view
				.findViewById(R.id.tv_rpcFileName);

		// set text values based on input message
		try {
			tv_sampleId.setText(String.valueOf( item.getSampleId() ) );
			tv_rpcName.setText(item.getFunctionName());
			tv_rpcFileName.setText(item.getJsonFile());
			boolean isSuccess = item.isSuccess();
			if (isSuccess) {
				tv_sampleId.setTextColor(RESPONSE_SUCCESS_COLOR);
				tv_rpcName.setTextColor(RESPONSE_SUCCESS_COLOR);
				tv_rpcFileName.setTextColor(RESPONSE_SUCCESS_COLOR);
			} else {
				tv_sampleId.setTextColor(RESPONSE_FAILURE_COLOR);
				tv_rpcName.setTextColor(RESPONSE_FAILURE_COLOR);
				tv_rpcFileName.setTextColor(RESPONSE_FAILURE_COLOR);
			}
		} catch (Throwable ex) {
			Log.e("error", ex.getMessage());
		}
	}

	private static final int RESPONSE_SUCCESS_COLOR = 0xFF2D9C08; // dark green
	private static final int RESPONSE_FAILURE_COLOR = Color.RED;

}
