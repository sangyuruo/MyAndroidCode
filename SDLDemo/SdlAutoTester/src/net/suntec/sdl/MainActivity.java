package net.suntec.sdl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.suntec.sdl.FileUtil.LoopDirFilter;
import net.suntec.sdl.autotester.CaseTestService;
import net.suntec.sdl.autotester.TestEnvironment;
import net.suntec.sdl.autotester.ThreadLogUtil;
import net.suntec.sdl.autotester.dto.TestSample;
import net.suntec.sdl.enums.SmdImageResource;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.livio.sdl.IpAddress;
import com.livio.sdl.SdlImageItem;
import com.livio.sdl.SdlImageItem.SdlImageItemComparator;
import com.livio.sdl.SdlLogMessage;
import com.livio.sdl.SdlRequestFactory;
import com.livio.sdl.SdlService;
import com.livio.sdl.UsbSignal;
import com.livio.sdl.adapters.SdlMessageAdapter;
import com.livio.sdl.dialogs.BaseAlertDialog;
import com.livio.sdl.dialogs.IndeterminateProgressDialog;
import com.livio.sdl.dialogs.JsonFlipperDialog;
import com.livio.sdl.dialogs.ListViewDialog;
import com.livio.sdl.enums.EnumComparator;
import com.livio.sdl.enums.SdlButton;
import com.livio.sdl.enums.SdlCommand;
import com.livio.sdl.menu.MenuItem;
import com.livio.sdl.test.SdlIdFactory;
import com.livio.sdl.utils.AndroidUtils;
import com.livio.sdl.utils.Timeout;
import com.livio.sdl.utils.WifiUtils;
import com.livio.sdltester.DisplayLayoutType;
import com.livio.sdltester.HelpActivity;
import com.livio.sdltester.LivioSdlTesterPreferences;
import com.livio.sdltester.dialogs.SdlConnectionDialog;
import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.RPCMessage;
import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.proxy.rpc.ListFiles;
import com.smartdevicelink.proxy.rpc.SetDisplayLayout;
import com.smartdevicelink.proxy.rpc.enums.FileType;
import com.smartdevicelink.proxy.rpc.enums.TextAlignment;
import com.smartdevicelink.proxy.rpc.enums.UpdateMode;
import com.smartdevicelink.transport.USBTransport;
import com.smartdevicelink.util.ext.LogUtil;

public class MainActivity extends Activity {
	/**
	 * Used when requesting information from the SDL service, these constants
	 * can be used to perform different tasks when the information is
	 * asynchronously returned by the service.
	 *
	 * @author Mike Burke
	 *
	 */
	private static final class ResultCodes {
		private static final class SubmenuResult {
			private static final int ADD_COMMAND_DIALOG = 0;
			private static final int DELETE_SUBMENU_DIALOG = 1;
		}

		private static final class CommandResult {
			private static final int DELETE_COMMAND_DIALOG = 0;
		}

		private static final class ButtonSubscriptionResult {
			private static final int BUTTON_SUBSCRIBE = 0;
			private static final int BUTTON_UNSUBSCRIBE = 1;
		}

		private static final class InteractionSetResult {
			private static final int PERFORM_INTERACTION = 0;
			private static final int DELETE_INTERACTION_SET = 1;
		}

		private static final class PutFileResult {
			private static final int PUT_FILE = 0;
			private static final int ADD_COMMAND = 1;
			private static final int CHOICE_INTERACTION_SET = 2;
			private static final int DELETE_FILE = 3;
			private static final int SET_APP_ICON = 4;
			private static final int SHOW = 5;
			private static final int SCROLLABLE_MESSAGE = 6;
			private static final int ALERT = 7;
		}
	}

	private static enum ConnectionStatus {
		CONNECTED("Connected"), OFFLINE_MODE("Offline Mode"), ;

		private final String friendlyName;

		private ConnectionStatus(String friendlyName) {
			this.friendlyName = friendlyName;
		}

		@Override
		public String toString() {
			return friendlyName;
		}
	}

	private static final int CONNECTING_DIALOG_TIMEOUT = 30000; // duration to
																// attempt a
																// connection
																// (30s)

	private String connectionStatusFormat;

	private ListView lv_messageLog;
	private TextView tv_connectionStatus;
	private SdlMessageAdapter listViewAdapter;

	/* Messenger for communicating with service. */
	private Messenger serviceMsgr = null;

	private boolean isBound = false;

	private BaseAlertDialog connectionDialog;
	private IndeterminateProgressDialog connectingDialog;
	private Timeout connectionTimeout;

	private boolean artworkSet = false;

	// cache for all images available to send to SDL service
	private HashMap<String, SdlImageItem> imageCache;
	private List<MenuItem> submenuCache = null;

	private List<SdlCommand> commandList = null;

	/*
	 * Target we publish for clients to send messages to IncomingHandler.
	 * �����ڷ�����Ϣ��ReplyTo
	 * ���棬�ڷ�����Ӧʱ����Դ���������Ӧ��IncomingHandler���Ǵ��?����Ӧ�ĺ���
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	// ListView lv_autoTest;
	// private SampleTestAdapter autoTestListViewAdapter;
	// private AutoTestAdapter autoTestListViewAdapter;
	TestEnvironment testEnvironment = null;
	CaseTestService caseTestService = null;

	// // TODO
	// private String audioStatusFormat;
	// private TextView tv_audioStatus;
	// // private String videoStatusFormat;
	// // private TextView tv_videoStatus;
	// AudioService audioService = null;

	// VideoService videoService = null;
	public static MainActivity getInstance() {
		return instance;
	}

	static MainActivity instance;

	@SuppressLint("HandlerLeak")
	private class IncomingHandler extends Handler {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			// see SdlService.java in the com.livio.sdl package for details on
			// these incoming messages

			switch (msg.what) {
			case SdlService.ClientMessages.SDL_CONNECTED:
				updateConnectionStatus(ConnectionStatus.CONNECTED);

				// clear the command log since we're starting fresh from here
				clearSdlLog();

				// dismiss the connecting dialog if it's showing
				if (connectingDialog != null && connectingDialog.isShowing()) {
					connectingDialog.dismiss();
				}
				if (connectionTimeout != null) {
					connectionTimeout.cancel();
				}

				// set up the app icon once we're connected
				// setAppIcon();
				synchronized (lock) {
					setAppIcon();
					setSmdImageResources();
				}
				break;
			case SdlService.ClientMessages.SDL_DISCONNECTED:
				resetService();
				updateConnectionStatus(ConnectionStatus.OFFLINE_MODE);
				clearSdlLog();
				break;
			case SdlService.ClientMessages.SDL_HMI_FIRST_DISPLAYED:
				setInitialHmi();
				break;
			case SdlService.ClientMessages.ON_MESSAGE_RESULT:
				// TODO response message
				onMessageResponseReceived((RPCMessage) msg.obj);
				break;
			case SdlService.ClientMessages.SUBMENU_LIST_RECEIVED:
				onSubmenuListReceived((List<MenuItem>) msg.obj, msg.arg1);
				break;
			case SdlService.ClientMessages.COMMAND_LIST_RECEIVED:
				onCommandListReceived((List<MenuItem>) msg.obj, msg.arg1);
				break;
			case SdlService.ClientMessages.BUTTON_SUBSCRIPTIONS_RECEIVED:
				onButtonSubscriptionsReceived((List<SdlButton>) msg.obj,
						msg.arg1);
				break;
			case SdlService.ClientMessages.INTERACTION_SETS_RECEIVED:
				onInteractionListReceived((List<MenuItem>) msg.obj, msg.arg1);
				break;
			case SdlService.ClientMessages.PUT_FILES_RECEIVED:
				onPutFileListReceived((List<String>) msg.obj, msg.arg1);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}

	/**
	 * Sends the input message to the SDL service through the service messenger.
	 * 
	 * @param msg
	 *            The message to send
	 */
	private void sendMessageToService(Message msg) {
		try {
			serviceMsgr.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends an RPCRequest to the SDL service through the service messenger and
	 * adds the request to the list view.
	 * 
	 * @param request
	 *            The request to send
	 */
	public void sendSdlMessageToService(RPCRequest request) {
		Message msg = Message.obtain(null,
				SdlService.ServiceMessages.SEND_MESSAGE);
		msg.obj = request;
		sendMessageToService(msg);
	}

	/**
	 * Sends a request for the most up-to-date submenu list with a request code
	 * so this activity knows what to do when the response comes back.
	 * 
	 * @param reqCode
	 *            The request code to associate with the request
	 */
	private void sendSubmenuListRequest(int reqCode) {
		Message msg = Message.obtain(null,
				SdlService.ServiceMessages.REQUEST_SUBMENU_LIST);
		msg.replyTo = mMessenger;
		msg.arg1 = reqCode;
		sendMessageToService(msg);
	}

	/**
	 * Sends a request for the most up-to-date command list with a request code
	 * so this activity knows what to do when the response comes back.
	 * 
	 * @param reqCode
	 *            The request code to associate with the request
	 */
	private void sendCommandListRequest(int reqCode) {
		Message msg = Message.obtain(null,
				SdlService.ServiceMessages.REQUEST_COMMAND_LIST);
		msg.replyTo = mMessenger;
		msg.arg1 = reqCode;
		sendMessageToService(msg);
	}

	/**
	 * Sends a request for the most up-to-date list of button subscriptions with
	 * a request code so this activity knows what to do when the response comes
	 * back.
	 * 
	 * @param reqCode
	 *            The request code to associate with the request
	 */
	private void sendButtonSubscriptionRequest(int reqCode) {
		Message msg = Message.obtain(null,
				SdlService.ServiceMessages.REQUEST_BUTTON_SUBSCRIPTIONS);
		msg.replyTo = mMessenger;
		msg.arg1 = reqCode;
		sendMessageToService(msg);
	}

	/**
	 * Sends a request for the most up-to-date list of button subscriptions with
	 * a request code so this activity knows what to do when the response comes
	 * back.
	 * 
	 * @param reqCode
	 *            The request code to associate with the request
	 */
	private void sendInteractionSetRequest(int reqCode) {
		Message msg = Message.obtain(null,
				SdlService.ServiceMessages.REQUEST_INTERACTION_SETS);
		msg.replyTo = mMessenger;
		msg.arg1 = reqCode;
		sendMessageToService(msg);
	}

	/**
	 * Sends a request for the most up-to-date list of images added so far with
	 * a request code so this activity knows what to do when the response comes
	 * back.
	 * 
	 * @param reqCode
	 *            The request code to associate with the request
	 */
	private void sendPutFileRequest(int reqCode) {
		Message msg = Message.obtain(null,
				SdlService.ServiceMessages.REQUEST_PUT_FILES);
		msg.replyTo = mMessenger;
		msg.arg1 = reqCode;
		sendMessageToService(msg);
	}

	/**
	 * Resets the SDL service.
	 */
	private void resetService() {
		artworkSet = false;
		Intent sdlService = new Intent(MainActivity.this, SdlService.class);
		stopService(sdlService);
		startService(sdlService);
	}

	/**
	 * Adds the input RPCMessage to the list view.
	 * 
	 * @param request
	 *            The message to log
	 */
	private void logSdlMessage(RPCMessage request) {
		listViewAdapter.add(new SdlLogMessage(request));
		listViewAdapter.notifyDataSetChanged();

		// after adding a new item, auto-scroll to the bottom of the list
		lv_messageLog.setSelection(listViewAdapter.getCount() - 1);
	}

	public void printAutoSampleTestResult(final List<TestSample> samples) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Collections.sort(samples , new Comparator<TestSample>() {
				// @Override
				// public int compare(TestSample lhs, TestSample rhs) {
				// return lhs.getCorrelationId() - rhs.getCorrelationId() ;
				// }
				// });
				displayShowResultBtn();
				// autoTestListViewAdapter.clear();
				// autoTestListViewAdapter.addAll(samples);
				// autoTestListViewAdapter.notifyDataSetChanged();
			}
		});
	}

	private void clearSdlLog() {
		listViewAdapter.clear();
		listViewAdapter.notifyDataSetChanged();
	}

	/*
	 * Sets up the App's icon using PutFile & SetAppIcon commands
	 * ע�⣺���ڳ���˽���ͼƬ
	 */
	private void setAppIcon() {
		// first, let's send our app icon image through the PutFile command
		SdlTesterImageResource appIcon = SdlTesterImageResource.IC_APP_ICON;
		String appIconName = appIcon.toString();
		FileType appIconFileType = appIcon.getFileType();
		Bitmap appIconBitmap = imageCache.get(appIconName).getBitmap();
		// create the image as raw bytes to send over
		byte[] appIconBytes = AndroidUtils.bitmapToRawBytes(appIconBitmap,
				Bitmap.CompressFormat.PNG);

		// create & send the PutFile command
		RPCRequest putFileMsg = SdlRequestFactory.putFile(appIconName,
				appIconFileType, false, appIconBytes);
		sendSdlMessageToService(putFileMsg);
	}

	/*
	 * Sets up the initial HMI through the Show command.
	 */
	private void setInitialHmi() {
		show();
		// since this is a media app, we have to clear out the 4th line of text
		// through the SetMediaClockTimer command
		RPCRequest clockMsg = SdlRequestFactory
				.setMediaClockTimer(UpdateMode.CLEAR);
		sendSdlMessageToService(clockMsg);
		TestReadyListener.getInstance().hmiReady();
	}

	private void displayShowResultBtn() {
		findViewById(R.id.btn_show_result).setVisibility(View.VISIBLE);
		findViewById(R.id.btn_start_test).setVisibility(View.GONE);
	}

	private void show() {
		// set up the main lines of text
		String showText1 = "Livio SDL Tester";
		String showText2 = "Send SDL Commands";
		String showText3 = "";
		// showText4 is not applicable since this is set up as a media
		// application

		// set up the image to show
		String appIconName = SdlTesterImageResource.IC_APP_ICON.toString();

		// create & send the Show command
		RPCRequest showMsg = SdlRequestFactory.show(showText1, showText2,
				showText3, null, null, TextAlignment.LEFT_ALIGNED, appIconName);
		sendSdlMessageToService(showMsg);
	}

	/*
	 * Class for interacting with the main interface of the service.
	 * 
	 * ��ǰActivity����󶨵�service������������
	 */
	private final ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// Ҫע��mConnection
			// ��Activity����󶨵�service�������������service��Messenger
			// ����Activity�ͳ���Ӧ�� ͨѶ��������
			serviceMsgr = new Messenger(service);

			Message msg = Message.obtain(null,
					SdlService.ServiceMessages.REGISTER_CLIENT);
			msg.replyTo = mMessenger;
			sendMessageToService(msg);
			UsbSignal.getInstance().countDown();
			if (!isUsb || SdlService.getInstance().offline()) {
				showSdlConnectionDialog();
			}

		}

		public void onServiceDisconnected(ComponentName className) {
			// process crashed - make sure nobody can use messenger instance.
			serviceMsgr = null;
		}
	};

	/**
	 * Binds this activity to the SDL service, using the service connection as a
	 * messenger between the two.
	 */
	private void doBindService() {
		if (!isBound) {
			bindService(new Intent(MainActivity.this, SdlService.class),
					mConnection, Context.BIND_AUTO_CREATE);
			isBound = true;
		}
	}

	/**
	 * Unbinds this activity from the SDL service.
	 */
	private void doUnbindService() {
		if (isBound) {
			if (serviceMsgr != null) {
				Message msg = Message.obtain(null,
						SdlService.ServiceMessages.UNREGISTER_CLIENT);
				msg.replyTo = mMessenger;
				sendMessageToService(msg);
			}

			// Detach our existing connection.
			unbindService(mConnection);
			stopService(new Intent(MainActivity.this, SdlService.class));
			isBound = false;
		}
	}

	/* ********** Android Life-Cycle ********** */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SdlIdFactory.reset();
		initLogThread();
		isUsb = false;
		// ���沼������
		setContentView(R.layout.main);

		SdlService.setDebug(true);
		// ����Ҫ���͵������ͼƬ���浽�ڴ���.���Ժ�����ٷ���
		createImageCache();
		// ������Ϣ��ʼ����������״̬����ť���ݵ�.
		init();
		// ��service
		doBindService();
		// initTestCase();
		checkUsbAccessoryIntent();
		instance = this;
	}

	private void initTestCase() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String sdCardPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			testEnvironment = TestEnvironment.createOnce(sdCardPath);
			testEnvironment.init();
			caseTestService = new CaseTestService(MainActivity.this,
					testEnvironment);
		}
	}

	public void showStartBtn() {
		findViewById(R.id.btn_start_test).setVisibility(View.VISIBLE);
		findViewById(R.id.btn_show_result).setVisibility(View.GONE);
	}

	// initializes views in the main activity
	private void init() {
		// set up the command log
		lv_messageLog = (ListView) findViewById(R.id.list_main_commandList);
		listViewAdapter = new SdlMessageAdapter(this);
		lv_messageLog.setAdapter(listViewAdapter);
		lv_messageLog.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// when an item is clicked, show it in the JSON flipper dialog.
				// first, we must copy over all the messages that have been
				// created so far.
				int size = listViewAdapter.getCount();
				List<SdlLogMessage> allLogs = new ArrayList<SdlLogMessage>(size);
				for (int i = 0; i < size; i++) {
					allLogs.add(listViewAdapter.getItem(i));
				}

				BaseAlertDialog jsonDialog = new JsonFlipperDialog(
						MainActivity.this, allLogs, position);
				jsonDialog.show();
			}
		});

		tv_connectionStatus = (TextView) findViewById(R.id.tv_connectionStatus);

		findViewById(R.id.btn_init_case).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						initTestCase();
						TestReadyListener.getInstance().testCaseReady();
					}
				});

		// TODO Auto Test
		findViewById(R.id.btn_start_test).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// startAutoTestPre();
						Thread caseTestThread = new Thread(caseTestService);
						caseTestThread.start();
					}
				});

		findViewById(R.id.btn_show_result).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(MainActivity.this,
								CaseListActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Intent.ACTION_RUN);
						MainActivity.this.startActivity(intent);
					}
				});

		// findViewById(R.id.btn_start_test).setVisibility(View.GONE);
		findViewById(R.id.btn_init_case).setVisibility(View.VISIBLE);
		findViewById(R.id.btn_start_test).setVisibility(View.GONE);
		findViewById(R.id.btn_show_result).setVisibility(View.GONE);

		// lv_autoTest = (ListView) findViewById(R.id.list_main_autoTestList);
		// autoTestListViewAdapter = new AutoTestAdapter(this);
		// autoTestListViewAdapter = new SampleTestAdapter(this);

		// lv_autoTest.setAdapter(autoTestListViewAdapter);
		// lv_autoTest.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// // when an item is clicked, show it in the JSON flipper dialog.
		// // first, we must copy over all the messages that have been
		// // created so far.
		// int size = autoTestListViewAdapter.getCount();
		// // List<AutoTestBean> allLogs = new ArrayList<AutoTestBean>(size);
		// List<TestSample> allLogs = new ArrayList<TestSample>(size);
		// for (int i = 0; i < size; i++) {
		// allLogs.add(autoTestListViewAdapter.getItem(i));
		// }
		//
		// BaseAlertDialog jsonDialog = new AutoTestJsonFlipperDialog(
		// MainActivity.this, allLogs, position);
		// jsonDialog.show();
		// }
		// });
	}

	// private void startAutoTestPre() {
	// try {
	// // TODO test
	// AutoTestTrack.getInstance().init(100);
	// if (!listRpcFilesInSdCard("pre")) {
	// listRpcFilesInAssets("pre");
	// }
	// } catch (IOException e) {
	// LogUtil.error(e.getMessage());
	// }
	// }
	//
	// private void startAutoTestCondition() {
	// try {
	// // TODO test
	// AutoTestTrack.getInstance().init(100);
	// if (!listRpcFilesInSdCard("condition")) {
	// listRpcFilesInAssets("condition");
	// }
	// } catch (IOException e) {
	// LogUtil.error(e.getMessage());
	// }
	// }

	// updates the connection status TextView
	private void updateConnectionStatus(ConnectionStatus status) {
		if (connectionStatusFormat == null) {
			connectionStatusFormat = getResources().getString(
					R.string.connection_status_format);
		}

		if (status == ConnectionStatus.OFFLINE_MODE) {
			sendMessageToService(Message.obtain(null,
					SdlService.ServiceMessages.OFFLINE_MODE));
		}

		String text = new StringBuilder().append(connectionStatusFormat)
				.append(" ").append(status.toString()).toString();
		tv_connectionStatus.setText(text);
	}

	@Override
	protected void onDestroy() {
		sendMessageToService(Message.obtain(null,
				SdlService.ServiceMessages.DISCONNECT));
		doUnbindService();
		LogUtil.getInstance().destroyLogThread();
		UsbSignal.getInstance().reset();
		super.onDestroy();
	}

	/**
	 * Called when a message has been received from the head-unit.
	 * 
	 * @param response
	 *            The response that was received
	 */
	private void onMessageResponseReceived(RPCMessage response) {
		// logSdlMessage(response);
		ThreadLogUtil.debug("recv msg:" + response.getFunctionName());
		if (response instanceof RPCResponse) {
			RPCResponse res = (RPCResponse) response;
			int correlationID = res.getCorrelationID();
			if( null != testEnvironment ){
				TestSample sample = testEnvironment.findTestSample(correlationID);
				if (null != sample) {
					String hashId = sample.getHashId();
					sample.setRes(res);
					sample.setSuccess(res.getSuccess());
					ThreadLogUtil.debug("recv response msg:" + correlationID);
					if (testEnvironment.containSampleDownLatch(hashId)) {
						ThreadLogUtil.debug("count down sampleSign:" + hashId);
						testEnvironment.countDownSampleDownLatch(hashId);
					}
	
					int caseId = testEnvironment.findCaseId(hashId);
					if (caseId != 0) {
						ThreadLogUtil.debug("count down caseSign:" + caseId);
						testEnvironment.countDownCaseDownLatch(caseId);
					}
				}
			}
			// printAutoSampleTestResult();
		}

		if (!artworkSet
				&& response.getMessageType().equals(RPCMessage.KEY_RESPONSE)
				&& response.getFunctionName().equals(FunctionID.PUT_FILE)) {
			artworkSet = true;

			// create & send the SetAppIcon command
			RPCRequest setAppIconMsg = SdlRequestFactory
					.setAppIcon(SdlTesterImageResource.IC_APP_ICON.toString());
			sendSdlMessageToService(setAppIconMsg);
		}

		// if (response.getMessageType().equals(RPCMessage.KEY_RESPONSE)
		// && response.getFunctionName().equals(
		// FunctionID.SET_DISPLAY_LAYOUT)) {
		// show();
		// }
	}

	/**
	 * Called when the up-to-date list of submenus is received. The request code
	 * can be used to perform different operations based on the request code
	 * that is sent with the initial request.
	 * 
	 * @param submenuList
	 *            The list of submenu items
	 * @param reqCode
	 *            The request code that was sent with the request
	 */
	private void onSubmenuListReceived(List<MenuItem> submenuList, int reqCode) {

	}

	/**
	 * Called when the up-to-date list of commands is received. The request code
	 * can be used to perform different operations based on the request code
	 * that is sent with the initial request.
	 * 
	 * @param commandList
	 *            The list of command items
	 * @param reqCode
	 *            The request code that was sent with the request
	 */
	private void onCommandListReceived(List<MenuItem> commandList, int reqCode) {

	}

	/**
	 * Called when the up-to-date list of button subscriptions is received. The
	 * request code can be used to perform different operations based on the
	 * request code that is sent with the initial request.
	 * 
	 * @param buttonSubscriptionList
	 *            The list of button subscriptions
	 * @param reqCode
	 *            The request code that was sent with the request
	 */
	private void onButtonSubscriptionsReceived(
			List<SdlButton> buttonSubscriptionList, int reqCode) {

	}

	/**
	 * Called when the up-to-date list of interaction sets is received. The
	 * request code can be used to perform different operations based on the
	 * request code that is sent with the initial request.
	 * 
	 * @param interactionSetList
	 *            The list of interaction sets
	 * @param reqCode
	 *            The request code that was sent with the request
	 */
	private void onInteractionListReceived(List<MenuItem> interactionSetList,
			int reqCode) {

	}

	/**
	 * Called when the up-to-date list of put file images have been received.
	 * The request code can be used to perform different operations based on the
	 * request code that is sent with the initial request.
	 * 
	 * @param putFileList
	 *            The list of put file image names
	 * @param reqCode
	 *            The request code that was sent with the request
	 */
	private void onPutFileListReceived(List<String> putFileList, int reqCode) {
		List<SdlImageItem> availableItems;
	}

	/**
	 * Filters out any images that have already been added through the PutFile
	 * command.
	 * 
	 * @param putFileList
	 *            The list of images that have been added through the PutFile
	 *            command
	 * @return The list of images that have <b>not</b> been added through the
	 *         PutFile command
	 */
	private List<SdlImageItem> filterAddedItems(List<String> putFileList) {
		int itemsInFilteredList = imageCache.size() - putFileList.size();
		if (itemsInFilteredList == 0) {
			return Collections.emptyList();
		}

		// first, we'll grab all image cache keys (aka image names) into a copy
		Set<String> cacheKeys = new TreeSet<String>(imageCache.keySet());
		// then, we'll remove all images that have been added
		cacheKeys.removeAll(putFileList);

		List<SdlImageItem> result = new ArrayList<SdlImageItem>(
				itemsInFilteredList);

		// now, we'll loop through the remaining image names and create a list
		// from them
		for (String name : cacheKeys) {
			result.add(imageCache.get(name));
		}

		Collections.sort(result, new SdlImageItemComparator());

		return result;
	}

	/**
	 * Filters out any images that have <b>not</b> been added through the
	 * PutFile command.
	 * 
	 * @param putFileList
	 *            The list of images that have been added through the PutFile
	 *            command
	 * @return The list of images that have been added through the PutFile
	 *         command
	 */
	private List<SdlImageItem> filterUnaddedItems(List<String> putFileList) {
		List<SdlImageItem> result = new ArrayList<SdlImageItem>(
				putFileList.size());
		for (String name : putFileList) {
			result.add(imageCache.get(name));
		}

		Collections.sort(result, new SdlImageItemComparator());
		return result;
	}

	/**
	 * Finds any buttons that are <b>not</b> in the input list of button
	 * subscriptions and adds them to the listview adapter.
	 * 
	 * @param buttonSubscriptions
	 *            A list of buttons that have been subscribed to
	 */
	private List<SdlButton> filterSubscribedButtons(
			List<SdlButton> buttonSubscriptions) {
		final SdlButton[] buttonValues = SdlButton.values();
		final int numItems = buttonValues.length - buttonSubscriptions.size();
		List<SdlButton> result = new ArrayList<SdlButton>(numItems);

		for (SdlButton button : buttonValues) {
			if (!buttonSubscriptions.contains(button)) {
				result.add(button);
			}
		}

		return result;
	}

	/**
	 * Shows the SDL connection dialog, which allows the user to enter the IP
	 * address for the core component they would like to connect to. ��ʾ sdl
	 * connection ���洰�� <b>IMPORTANT NOTE</b>
	 * <p>
	 * WiFi connections are for testing purposes only and will not be available
	 * in production environments. WiFi can be used to test your application on
	 * a virtual machine running Ubuntu 12.04, but WiFi should not be used for
	 * production testing. Production-level testing should be performed on a TDK
	 * utilizing a Bluetooth connection.
	 */
	private void showSdlConnectionDialog() {
		// restore any old IP address from preferences
		String savedIpAddress = LivioSdlTesterPreferences
				.restoreIpAddress(MainActivity.this);
		String savedTcpPort = LivioSdlTesterPreferences
				.restoreTcpPort(MainActivity.this);
		int transportType = LivioSdlTesterPreferences
				.restoreTransportChoice(MainActivity.this);

		if (savedIpAddress != null && savedTcpPort != null) {
			// if there was an old IP stored in preferences, initialize the
			// dialog with those values
			connectionDialog = new SdlConnectionDialog(this, transportType,
					savedIpAddress, savedTcpPort);
		} else {
			// if no IP address was in preferences, initialize the dialog with
			// no input strings
			connectionDialog = new SdlConnectionDialog(this, transportType, "",
					"12345");
		}

		// set us up the dialog
		connectionDialog.setCancelable(false);
		// ��ok/cancel�ᴥ�� listener
		connectionDialog.setListener(new BaseAlertDialog.Listener() {
			@Override
			public void onResult(Object resultData) {
				if (resultData == null) {
					// dialog cancelled
					updateConnectionStatus(ConnectionStatus.OFFLINE_MODE);
					return;
				}

				IpAddress result = (IpAddress) resultData;

				String addressString = result.getIpAddress();
				String portString = result.getTcpPort();

				boolean ipAddressValid = false, ipPortValid = false;

				if (addressString == null && portString == null) { // bluetooth
					// result = null;
					// TODO: enable bluetooth if not enabled
				} else { // wifi
					ipAddressValid = WifiUtils.validateIpAddress(addressString);
					ipPortValid = WifiUtils.validateTcpPort(portString);
					// TODO: enable wifi if not enabled
				}

				// if user selected bluetooth mode or if they selected wifi mode
				// with valid address & port - attempt a connection
				if (result.getConnType() > 1 || (ipAddressValid && ipPortValid)) {
					// if the user entered valid IP settings, save them to
					// preferences so they don't have to re-enter them next time
					if (ipAddressValid) {
						LivioSdlTesterPreferences.saveIpAddress(
								MainActivity.this, addressString);
					}
					if (ipPortValid) {
						LivioSdlTesterPreferences.saveTcpPort(
								MainActivity.this, portString);
					}

					LivioSdlTesterPreferences
							.saveTransportChoice(
									MainActivity.this,
									(result == null) ? LivioSdlTesterPreferences.PREF_TRANSPORT_BLUETOOTH
											: LivioSdlTesterPreferences.PREF_TRANSPORT_WIFI);

					// show an indeterminate connecting dialog
					connectingDialog = new IndeterminateProgressDialog(
							MainActivity.this, "Connecting");
//					connectingDialog.show();

					// and start a timeout thread in case the connection isn't
					// successful
					if (connectionTimeout == null) {
						connectionTimeout = new Timeout(
								CONNECTING_DIALOG_TIMEOUT,
								new Timeout.Listener() {
									@Override
									public void onTimeoutCancelled() {
									}

									@Override
									public void onTimeoutCompleted() {
										if (connectingDialog != null
												&& connectingDialog.isShowing()) {
											// if we made it here without being
											// interrupted, the connection was
											// unsuccessful - dismiss the dialog
											// and enter offline mode
											connectingDialog.dismiss();
										}

										Toast.makeText(MainActivity.this,
												"Connection timed out",
												Toast.LENGTH_SHORT).show();
										sendMessageToService(Message
												.obtain(null,
														SdlService.ServiceMessages.OFFLINE_MODE));
										updateConnectionStatus(ConnectionStatus.OFFLINE_MODE);
									}
								});
					}
					connectionTimeout.start();

					// message the SDL service, telling it to attempt a
					// connection with the input IP address
					Message msg = Message.obtain(null,
							SdlService.ServiceMessages.CONNECT);
					msg.obj = result;
					sendMessageToService(msg);
				}
				// wifi address or port was invalid - re-show the dialog until
				// user enters a valid value
				else {
					// user input was invalid
					Toast.makeText(MainActivity.this,
							"Input was invalid - please try again",
							Toast.LENGTH_SHORT).show();
					showSdlConnectionDialog();
				}
			}
		});
		connectionDialog.show();
	}

	/**
	 * Launches the appropriate dialog for whichever command item was clicked.
	 * �������command������Ӧ����Ϊ�� ��ʾ��Ӧ�� sdl command ���洰��
	 * 
	 * @param command
	 *            The command that was clicked
	 */
	private void showCommandDialog(SdlCommand command) {
		if (command == null) {
			// shouldn't happen, but if an invalid command gets here, let's
			// throw an exception.
			throw new IllegalArgumentException(getResources().getString(
					R.string.not_an_sdl_command));
		}

		switch (command) {
		case PUT_FILE:
			// the put file dialog needs a list of images that have been added
			// so far, so let's request
			// that list here and we'll actually show the dialog when it gets
			// returned by the service. See onPutFileListReceived().
			sendPutFileRequest(ResultCodes.PutFileResult.PUT_FILE);
			break;
		case DELETE_FILE:
			// the delete file dialog needs a list of images that have been
			// added so far, so let's request
			// that list here and we'll actually show the dialog when it gets
			// returned by the service. See onPutFileListReceived().
			sendPutFileRequest(ResultCodes.PutFileResult.DELETE_FILE);
			break;
		case ALERT:
			sendPutFileRequest(ResultCodes.PutFileResult.ALERT);
			break;
		case SPEAK:
			break;
		case SHOW:
			// the put file dialog needs a list of images that have been added
			// so far, so let's request
			// that list here and we'll actually show the dialog when it gets
			// returned by the service. See onPutFileListReceived().
			sendPutFileRequest(ResultCodes.PutFileResult.SHOW);
			break;
		case SUBSCRIBE_BUTTON:
			// the subscribe button dialog needs a list of buttons that have
			// been subscribed to so far, so let's request
			// that list here and we'll actually show the dialog when it gets
			// returned by the service. See onButtonSubscriptionsReceived().
			sendButtonSubscriptionRequest(ResultCodes.ButtonSubscriptionResult.BUTTON_SUBSCRIBE);
			break;
		case UNSUBSCRIBE_BUTTON:
			// the unsubscribe button dialog needs a list of buttons that have
			// been subscribed to so far, so let's request
			// that list here and we'll actually show the dialog when it gets
			// returned by the service. See onButtonSubscriptionsReceived().
			sendButtonSubscriptionRequest(ResultCodes.ButtonSubscriptionResult.BUTTON_UNSUBSCRIBE);
			break;
		case ADD_COMMAND:
			// the add command dialog needs a list of submenus that the command
			// could be added to, so let's request that list here and
			// we'll actually show the dialog when the list gets returned by the
			// service. See onSubmenuListReceived().
			sendSubmenuListRequest(ResultCodes.SubmenuResult.ADD_COMMAND_DIALOG);
			break;
		case DELETE_COMMAND:
			// the delete command dialog needs a list of commands that have been
			// added so far so the user can select which command to delete,
			// so let's request the list here and we'll show the dialog when
			// it's returned by the service. See onCommandListReceived().
			sendCommandListRequest(ResultCodes.CommandResult.DELETE_COMMAND_DIALOG);
			break;
		case ADD_SUBMENU:
			break;
		case DELETE_SUB_MENU:
			// the delete submenu dialog needs a list of commands that have been
			// added so far so the user can select which submenu to delete,
			// so let's request the list here and we'll show the dialog when
			// it's returned by the service. See onSubmenuListReceived().
			sendSubmenuListRequest(ResultCodes.SubmenuResult.DELETE_SUBMENU_DIALOG);
			break;
		case CREATE_INTERACTION_CHOICE_SET:
			// the CreateInteractionChoiceSet dialog needs a list of images that
			// have been added so far, so let's request
			// that list here and we'll actually show the dialog when it gets
			// returned by the service. See onPutFileListReceived().
			sendPutFileRequest(ResultCodes.PutFileResult.CHOICE_INTERACTION_SET);
			break;
		case PERFORM_INTERACTION:
			// the perform interaction dialog needs a list of interaction sets
			// that have been added so far, so let's request
			// that list here and we'll actually show the dialog when it gets
			// returned by the service. See onInteractionListReceived().
			sendInteractionSetRequest(ResultCodes.InteractionSetResult.PERFORM_INTERACTION);
			break;
		case DELETE_INTERACTION_CHOICE_SET:
			// the delete interaction dialog needs a list of interaction sets
			// that have been added so far, so let's request
			// that list here and we'll actually show the dialog when it gets
			// returned by the service. See onInteractionListReceived().
			sendInteractionSetRequest(ResultCodes.InteractionSetResult.DELETE_INTERACTION_SET);
			break;
		case SCROLLABLE_MESSAGE:
			// the scrollable message dialog needs a list of images that have
			// been added so far, so let's request
			// that list here and we'll actually show the dialog when it gets
			// returned by the service. See onPutFileListReceived().
			sendPutFileRequest(ResultCodes.PutFileResult.SCROLLABLE_MESSAGE);
			break;
		case LIST_FILES:
			// list files command doesn't accept any parameters, so we can send
			// it directly.
			sendSdlMessageToService(new ListFiles());
			break;
		case SET_APP_ICON:
			// the set app icon dialog needs a list of images that have been
			// added so far, so let's request
			// that list here and we'll actually show the dialog when it gets
			// returned by the service. See onPutFileListReceived().
			sendPutFileRequest(ResultCodes.PutFileResult.SET_APP_ICON);
			break;
		case SET_DISPLAY_LAYOUT:
			createSetDisplayLayoutDialog();
		default:
			break;
		}
	}

	// listener to be used when receiving a single RPCRequest from a dialog.
	// SDL Command ���洰�ڵ㰴ť�����¼�
	private final BaseAlertDialog.Listener singleMessageListener = new BaseAlertDialog.Listener() {
		@Override
		public void onResult(final Object resultData) {
			// resultData��ÿ�������dialog�й��������ص��dialog�İ�ť�󴥷��˴��¼�
			if (resultData != null) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						// ��serviceͨѶ���̶�ͳ���ͨѶ
						sendSdlMessageToService((RPCRequest) resultData);
					}
				}).start();
			}
		}
	};

	// listener to be used when receiving a list of RPCRequests from a dialog.
	// SDL Command ���洰�ڵ㰴ť�����¼�
	private final BaseAlertDialog.Listener multipleMessageListener = new BaseAlertDialog.Listener() {
		@Override
		public void onResult(final Object resultData) {
			if (resultData != null) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						@SuppressWarnings("unchecked")
						List<RPCRequest> msgList = (List<RPCRequest>) resultData;
						for (RPCRequest request : msgList) {
							sendSdlMessageToService(request);
						}
					}
				}).start();
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		android.view.MenuItem connectToSdl = (android.view.MenuItem) menu
				.findItem(R.id.menu_connect);
		android.view.MenuItem disconnectFromSdl = (android.view.MenuItem) menu
				.findItem(R.id.menu_disconnect);
		android.view.MenuItem resetSdl = (android.view.MenuItem) menu
				.findItem(R.id.menu_reset);
		android.view.MenuItem clearListView = (android.view.MenuItem) menu
				.findItem(R.id.menu_clear_list);

		// show/hide connect/disconnect menu items
		boolean connected = tv_connectionStatus.getText().toString()
				.contains(ConnectionStatus.CONNECTED.friendlyName);
		connectToSdl.setVisible(!connected); // if we are not connected, show
												// the connect item
		disconnectFromSdl.setVisible(connected); // if we are connected, show
													// the disconnect item
		resetSdl.setVisible(connected); // if we are connected, show reset SDL
										// item

		boolean listHasItems = (listViewAdapter != null && listViewAdapter
				.getCount() > 0);
		clearListView.setVisible(listHasItems); // if the list has items, show
												// the clear list item

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		int menuItemId = item.getItemId();
		switch (menuItemId) {
		case R.id.menu_connect:
			showSdlConnectionDialog();
			return true;
		case R.id.menu_disconnect:
			sendMessageToService(Message.obtain(null,
					SdlService.ServiceMessages.DISCONNECT));
			return true;
		case R.id.menu_reset:
			artworkSet = false;
			sendMessageToService(Message.obtain(null,
					SdlService.ServiceMessages.RESET));
			return true;
		case R.id.menu_clear_list:
			clearSdlLog();
			return true;
		case R.id.menu_help:
			Intent helpIntent = new Intent(MainActivity.this,
					HelpActivity.class);
			startActivity(helpIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private List<DisplayLayoutType> typeList = null;

	private void createSetDisplayLayoutDialog() {
		if (null == typeList) {
			typeList = Arrays.asList(DisplayLayoutType.values());
			Collections.sort(typeList, new EnumComparator<DisplayLayoutType>());
		}
		Context context = MainActivity.this;
		String dialogTitle = context.getResources().getString(
				R.string.sdl_command_dialog_title);

		BaseAlertDialog commandDialog = new ListViewDialog<DisplayLayoutType>(
				context, dialogTitle, typeList);
		commandDialog.setListener(new BaseAlertDialog.Listener() {
			@Override
			public void onResult(Object resultData) {
				DisplayLayoutType type = (DisplayLayoutType) resultData;
				Log.d("SDLTest", "type is " + type.toString());
				SetDisplayLayout cmd = new SetDisplayLayout();
				cmd.setDisplayLayout(type.toString());
				sendSdlMessageToService(cmd);
			}
		});
		commandDialog.show();
	}

	private void checkUsbAccessoryIntent() {
		final Intent intent = getIntent();
		String action = intent.getAction();

		if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						LogUtil.debugUSB("wait sdl service init");
						isUsb = true;
						UsbSignal.getInstance().await();
						LogUtil.debugUSB("sdl service init success or timeout");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Intent usbAccessoryAttachedIntent = new Intent(
							USBTransport.ACTION_USB_ACCESSORY_ATTACHED);
					usbAccessoryAttachedIntent.putExtra(
							UsbManager.EXTRA_ACCESSORY,
							intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY));
					usbAccessoryAttachedIntent.putExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED,
							intent.getParcelableExtra(UsbManager.EXTRA_PERMISSION_GRANTED));
					sendBroadcast(usbAccessoryAttachedIntent);
					LogUtil.debugUSB("sendBroadcast success");
					startAppLinkUSB(intent);
				}
			}).start();
		}
	}

	private void startAppLinkUSB(Intent intent) {
		LogUtil.debugUSB("start applink via usb");
		IpAddress result = new IpAddress(null, null, 3);
		Message msg = Message.obtain(null, SdlService.ServiceMessages.CONNECT);
		msg.obj = result;
		sendMessageToService(msg);
	}

	private void initLogThread() {
		LogUtil.getInstance().initLogThread();
	}

	boolean isUsb = false;

	private void setSmdImageResources() {
		LogUtil.debug("send other icon to service... ");
		SmdImageResource[] values2 = SmdImageResource.values();
		for (final SmdImageResource img : values2) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					setSmdImageResource(img);
				}
			}).start();
		}
	}

	private void setSmdImageResource(SmdImageResource appIcon) {
		try {
			String appIconName = appIcon.toString();
			FileType appIconFileType = appIcon.getFileType();
			Bitmap appIconBitmap = imageCache.get(appIconName).getBitmap();
			LogUtil.debug("image name is " + appIconName);
			LogUtil.debug("image height is " + appIconBitmap.getHeight());
			LogUtil.debug("image width is " + appIconBitmap.getWidth());
			LogUtil.debug("image RowBytes is " + appIconBitmap.getRowBytes());
			// create the image as raw bytes to send over
			byte[] appIconBytes = AndroidUtils.bitmapToRawBytes(appIconBitmap,
					Bitmap.CompressFormat.PNG);

			// create & send the PutFile command
			RPCRequest putFileMsg = SdlRequestFactory.putFile(appIconName,
					appIconFileType, false, appIconBytes);
			sendSdlMessageToService(putFileMsg);
		} catch (Exception ex) {
			LogUtil.error(ex.getMessage());
		}
	}

	private void createImageCache() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// grab information from the image resources enum
				SdlTesterImageResource[] values = SdlTesterImageResource
						.values();
				imageCache = new HashMap<String, SdlImageItem>(values.length);
				SmdImageResource[] values2 = SmdImageResource.values();
				Options bitmapDecodeOption = new Options();
				bitmapDecodeOption.inScaled = false;

				synchronized (lock) {
					for (SdlTesterImageResource img : values) {
						// create an SdlImageItem object for each image
						Bitmap bitmap = BitmapFactory.decodeResource(
								getResources(), img.getImageId());
						String imageName = img.toString();
						FileType imageType = img.getFileType();
						SdlImageItem item = new SdlImageItem(bitmap, imageName,
								imageType);

						// map the image name to its associated SdlImageItem
						// object
						imageCache.put(imageName, item);
					}

					for (SmdImageResource img : values2) {
						// create an SdlImageItem object for each image
						Bitmap bitmap = BitmapFactory.decodeResource(
								getResources(), img.getImageId(),
								bitmapDecodeOption);
						String imageName = img.toString();
						FileType imageType = img.getFileType();
						SdlImageItem item = new SdlImageItem(bitmap, imageName,
								imageType);

						// map the image name to its associated SdlImageItem
						// object
						imageCache.put(imageName, item);
					}
					loadImageFromSdCard();
				}
			}
		}).start();
	}

	Object lock = new Object();

	private void loadImageFromSdCard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String sdCardPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			String rootPath = sdCardPath + File.separator
					+ AppConstant.APP_AUTO_TEST_DIR;
			File resFile = new File(rootPath + File.separator + "res");
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			FileUtil.loopDir(resFile, new LoopDirFilter() {
				@Override
				public void filter(File file) {
					boolean isImg = false;
					String imageName = null;
					FileType imageType = null;
					String fullName = file.getName();
					if (file.getName().endsWith(AppConstant.PNG_SUFFIX)) {
						isImg = true;
						imageType = FileType.GRAPHIC_PNG;
						imageName = fullName.substring(0, fullName.length() - 4);
					} else if (file.getName().endsWith(AppConstant.BMP_SUFFIX)) {
						isImg = true;
						imageType = FileType.GRAPHIC_BMP;
						imageName = file.getName();
						imageName = fullName.substring(0, fullName.length() - 4);
					} else if (file.getName().endsWith(AppConstant.JPEG_SUFFIX)) {
						isImg = true;
						imageType = FileType.GRAPHIC_JPEG;
						imageName = file.getName();
						if (fullName.endsWith(".jpg")) {
							imageName = fullName.substring(0,
									fullName.length() - 4);
						} else {
							imageName = fullName.substring(0,
									fullName.length() - 5);
						}
					}

					if (isImg) {
						Bitmap bitmap = BitmapFactory.decodeFile(
								file.getAbsolutePath(), options);
						SdlImageItem item = new SdlImageItem(bitmap, imageName,
								imageType);
						if (!imageCache.containsKey(imageName)) {
							imageCache.put(imageName, item);
							Log.i("LOAD_IMG", "image name is " + imageName);
						}
					}
				}
			});
		}
	}

	// public boolean listRpcFilesInSdCard(String path) throws IOException {
	// if (Environment.getExternalStorageState().equals(
	// Environment.MEDIA_MOUNTED)) {
	// File file = new File(Environment.getExternalStorageDirectory(),
	// "AutoTest/" + path);
	// if (!file.exists()) {
	// file.mkdirs();
	// }
	// List<File> returnVal = loopDir(file);
	// for (File jsonFile : returnVal) {
	// String jsonStr = loadJSONFromFile(jsonFile);
	// LogUtil.debug(jsonFile.getName());
	// if (null != jsonStr && !jsonStr.isEmpty()) {
	// LogUtil.debug(jsonStr);
	// try {
	// JSONObject json = new JSONObject(jsonStr);
	// Hashtable<String, Object> hash = RPCStruct
	// .deserializeJSONObject(json);
	// String rpcName = json.getJSONObject("request")
	// .getString("name");
	// RPCRequest rpc = (RPCRequest) RpcRequestMapper
	// .get(rpcName).getConstructor(Hashtable.class)
	// .newInstance(hash);
	// // TODO add testbean to track
	// int correlationID = SdlIdFactory.getNextId();
	// rpc.setCorrelationID(correlationID);
	// AutoTestBean autoTestBean = new AutoTestBean();
	// autoTestBean.setCorrelationID(correlationID);
	// autoTestBean.setFilePath(jsonFile.getAbsolutePath());
	// AutoTestTrack.getInstance().add(correlationID,
	// autoTestBean);
	// // RPCRequest rpc = (RPCRequest) RpcRequestMapper.get(
	// // rpcName ).newInstance(hash);
	// sendSdlMessageToService(rpc);
	// Thread.sleep(10);
	//
	// // if( rpcName.equals("Show") ){
	// // Show show = new Show(hash);
	// // sendSdlMessageToService(show);
	// // }else if( rpcName.equals("Alert") ){
	// // Alert alert = new Alert(hash);
	// // sendSdlMessageToService(alert);
	// // }else if( rpcName.equals("Speak") ){
	// // Speak speak = new Speak(hash);
	// // sendSdlMessageToService(speak);
	// // }else if( rpcName.equals("Speak") ){
	// // Speak speak = new Speak(hash);
	// // sendSdlMessageToService(speak);
	// // }
	// } catch (InstantiationException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// } catch (IllegalAccessException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// } catch (IllegalArgumentException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// } catch (InvocationTargetException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// } catch (NoSuchMethodException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// } catch (JSONException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// } catch (InterruptedException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// }
	// }
	// }
	// return true;
	// } else {
	// return false;
	// }
	// }
	//
	// /**
	// * 轮询 /assets 文件夹,获取所有的json文件.
	// *
	// * @return
	// * @throws IOException
	// */
	// public List<String> listRpcFilesInAssets(String path) throws IOException
	// {
	// List<String> returnVal = loopRpcFiles("rpc/" + path);
	// for (String fileName : returnVal) {
	// String jsonStr = loadJSONFromAsset(fileName);
	// LogUtil.debug(fileName);
	// if (null != jsonStr && !jsonStr.isEmpty()) {
	// LogUtil.debug(jsonStr);
	// try {
	// JSONObject json = new JSONObject(jsonStr);
	// Hashtable<String, Object> hash = RPCStruct
	// .deserializeJSONObject(json);
	// String rpcName = json.getJSONObject("request").getString(
	// "name");
	// RPCRequest rpc = (RPCRequest) RpcRequestMapper.get(rpcName)
	// .getConstructor(Hashtable.class).newInstance(hash);
	// // TODO add testbean to track
	// int correlationID = SdlIdFactory.getNextId();
	// rpc.setCorrelationID(correlationID);
	// AutoTestBean autoTestBean = new AutoTestBean();
	// autoTestBean.setCorrelationID(correlationID);
	// autoTestBean.setFilePath(fileName);
	// AutoTestTrack.getInstance()
	// .add(correlationID, autoTestBean);
	// // RPCRequest rpc = (RPCRequest) RpcRequestMapper.get(
	// // rpcName ).newInstance(hash);
	// sendSdlMessageToService(rpc);
	// Thread.sleep(10);
	//
	// // if( rpcName.equals("Show") ){
	// // Show show = new Show(hash);
	// // sendSdlMessageToService(show);
	// // }else if( rpcName.equals("Alert") ){
	// // Alert alert = new Alert(hash);
	// // sendSdlMessageToService(alert);
	// // }else if( rpcName.equals("Speak") ){
	// // Speak speak = new Speak(hash);
	// // sendSdlMessageToService(speak);
	// // }else if( rpcName.equals("Speak") ){
	// // Speak speak = new Speak(hash);
	// // sendSdlMessageToService(speak);
	// // }
	// } catch (InstantiationException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// } catch (IllegalAccessException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// } catch (IllegalArgumentException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// } catch (InvocationTargetException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// } catch (NoSuchMethodException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// } catch (JSONException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// } catch (InterruptedException e) {
	// LogUtil.error(e.getLocalizedMessage());
	// }
	// }
	// }
	// return returnVal;
	// }

	// public List<File> loopDir(File dir) {
	// List<File> returnVal = new ArrayList<File>();
	// String pdfPattern = ".json";
	// File[] listFile = dir.listFiles();
	// if (listFile != null) {
	// for (int i = 0; i < listFile.length; i++) {
	// if (listFile[i].isDirectory()) {
	// List<File> tmp = loopDir(listFile[i]);
	// returnVal.addAll(tmp);
	// } else {
	// if (listFile[i].getName().endsWith(pdfPattern)) {
	// returnVal.add(listFile[i]);
	// }
	// }
	// }
	// }
	// return returnVal;
	// }

	// /**
	// * 轮询 /assets下的文件夹
	// *
	// * @param path
	// * @return
	// * @throws IOException
	// */
	// public List<String> loopRpcFiles(String path) throws IOException {
	// List<String> returnVal = new ArrayList<String>();
	// String[] files = MainActivity.this.getAssets().list(path);
	// if (files.length > 0) {
	// for (String string : files) {
	// List<String> tmp = loopRpcFiles(path + "/" + string);
	// returnVal.addAll(tmp);
	// }
	// } else {
	// returnVal.add(path);
	// }
	// return returnVal;
	// }

	// /**
	// * 读取json文件
	// *
	// * @param jsonFileName
	// * @return
	// */
	// public String loadJSONFromAsset(String jsonFileName) {
	// String json = null;
	// try {
	// InputStream is = MainActivity.this.getAssets().open(jsonFileName);
	// int size = is.available();
	// byte[] buffer = new byte[size];
	// is.read(buffer);
	// is.close();
	// json = new String(buffer, "UTF-8");
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// return null;
	// }
	// return json;
	// }

	// /**
	// * 读取json文件
	// *
	// * @param jsonFileName
	// * @return
	// */
	// public String loadJSONFromFile(File file) {
	// String json = null;
	// try {
	// InputStream is = new FileInputStream(file);
	// int size = is.available();
	// byte[] buffer = new byte[size];
	// is.read(buffer);
	// is.close();
	// json = new String(buffer, "UTF-8");
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// return null;
	// }
	// return json;
	// }

	public HashMap<String, SdlImageItem> getImageCache() {
		return imageCache;
	}
}
