package com.livio.sdl.test;

import java.util.List;

import net.suntec.sdl.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AutoTestAdapter extends ArrayAdapter<AutoTestBean> {

	public AutoTestAdapter instance;

	public AutoTestAdapter(Context context, List<AutoTestBean> objects) {
		super(context, R.layout.sample_test_listview_row, objects);
	}

	public AutoTestAdapter(Context context) {
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

		AutoTestBean item = getItem(position);

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
	private void populateView(View view, AutoTestBean item) {
		TextView tv_rpcName = (TextView) view.findViewById(R.id.tv_rpcName);
		TextView tv_rpcFileName = (TextView) view
				.findViewById(R.id.tv_rpcFileName);

		// set text values based on input message
		tv_rpcName.setText(item.getRequest().getFunctionName());
		tv_rpcFileName.setText(item.getFilePath());

		boolean isSuccess = item.isSuccess();
		if (isSuccess) {
			tv_rpcName.setTextColor(RESPONSE_SUCCESS_COLOR);
			tv_rpcFileName.setTextColor(RESPONSE_SUCCESS_COLOR);
		} else {
			tv_rpcName.setTextColor(RESPONSE_FAILURE_COLOR);
			tv_rpcFileName.setTextColor(RESPONSE_FAILURE_COLOR);
		}
	}

	private static final int RESPONSE_SUCCESS_COLOR = 0xFF2D9C08; // dark green
	private static final int RESPONSE_FAILURE_COLOR = Color.RED;

}
