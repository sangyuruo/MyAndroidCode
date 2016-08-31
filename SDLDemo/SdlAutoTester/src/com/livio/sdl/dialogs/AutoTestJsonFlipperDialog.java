package com.livio.sdl.dialogs;

import java.util.List;

import net.suntec.sdl.R;
import net.suntec.sdl.autotester.dto.TestSample;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.livio.sdl.utils.SdlUtils;

/**
 * This dialog shows a single JSON message, but allows the ability to flip back
 * and forth between all available messages via the arrow buttons at the bottom
 * of the dialog. This dialog will not be updated when new messages are sent
 * with this dialog open. The dialog must be closed and re-opened in order to
 * refresh with new values.
 *
 * @author Mike Burke
 *
 */
public class AutoTestJsonFlipperDialog extends BaseAlertDialog {

//	private List<AutoTestBean> jsonMessages;
	private List<TestSample> jsonMessages;
	private int currentPosition;

	private TextView tv_result ;
	private ImageButton leftButton, rightButton;

	public AutoTestJsonFlipperDialog(Context context,
			List<TestSample> jsonMessages, int startPosition) {
		super(context, SdlUtils.makeJsonTitle(jsonMessages.get(startPosition)
				.getSampleId()), R.layout.auto_test_json_flipper_dialog);
		this.jsonMessages = jsonMessages;
		this.currentPosition = startPosition;
		createDialog();

		// since refresh updates the dialog's title, this must be after
		// createDialog() so the dialog isn't null
		refresh();
	}

	@Override
	protected void findViews(View parent) {
		tv_result = (TextView) parent.findViewById(R.id.tv_result);
		// set up left button
		leftButton = (ImageButton) parent.findViewById(R.id.ib_moveLeft);
		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// if we can move left, do it. if not, do nothing
				if (currentPosition > 0) {
					currentPosition--;
					refresh();
				}
			}
		});

		// set up right button
		rightButton = (ImageButton) parent.findViewById(R.id.ib_moveRight);
		rightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// if we can move right, do it. if not, do nothing
				if (currentPosition < (jsonMessages.size() - 1)) {
					currentPosition++;
					refresh();
				}
			}
		});
	}

	// refresh the buttons & the text for this dialog
	private void refresh() {
		refreshButtons();
		refreshText();
	}

	// refreshes the buttons with new position. disables the buttons when we're
	// at the edges of the list.
	private void refreshButtons() {
		boolean atStart = (currentPosition == 0);
		boolean atEnd = (currentPosition == jsonMessages.size() - 1);

		leftButton.setEnabled(!atStart);
		rightButton.setEnabled(!atEnd);
	}

	// refreshes the text of the dialog - both the title and the main text.
	private void refreshText() {
		TestSample currentMessage = jsonMessages.get(currentPosition);
		dialog.setTitle(SdlUtils.makeJsonTitle(currentMessage
				.getSampleId()));
		String reqJson = SdlUtils
				.getJsonString(currentMessage.getReq());
		String resJson = "";
		if( null != currentMessage.getRes() ){
			resJson = SdlUtils.getJsonString(currentMessage.getRes());
		}
		tv_result.setText(reqJson + "\n...........................\n" + resJson );
	}

}
