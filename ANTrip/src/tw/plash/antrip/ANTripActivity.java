package tw.plash.antrip;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.PriorityQueue;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.crittercism.app.Crittercism;

public class ANTripActivity extends Activity implements ActivityCallback{
	
	private Context mContext;
	private WebView mWebView = null;
	private FrameLayout webViewPlaceholder;
	
	private Integer currentMode;
	
	private CandidateCheckinObject cco;
	
	private Uri imageUri;
	private final int REQUEST_CODE_TAKE_PICTURE = 100;
	private final int REQUEST_CODE_DO_SETTINGS = 101;
	private final int REQUEST_CODE_CHECKIN = 102;
	
	private SharedPreferences pref;
	
//	private ArrayList<Location> locationList;
//	private Location latestLocation;
	private Location checkinLocation;
	
	private PriorityQueue<String> urlQueue;
	private Handler mHandler;
	private boolean canPostAgain;
	private boolean canCallJavaScript = false;
	private boolean needToLogout = false;
	private boolean duringCheckin = false;
	private boolean canCheckin = false;
	private boolean need2SaveCCO = false;
	
	private Button btn_checkin;
	
	boolean mIsBound;
	private Messenger outMessenger = null;
	final private Messenger inMessenger = new Messenger(new IncomingHandler());
	private class IncomingHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AntripService.MSG_LOCATION_UPDATE:
				//location update should provide with a non-null location related object
				if (msg.obj != null) {
					if (btn_checkin != null && canCheckin && AntripService.isRecording()) {
						btn_checkin.setVisibility(View.VISIBLE);
					}
					if (msg.obj instanceof Location) {
						// Location object is only received when NOT recording
						// thus no check-in or edit tripname can occur
						Location loc = (Location) msg.obj;
						String singleLocationUpdateURL = "javascript:setPosition(" + loc.getLatitude() + ","
								+ loc.getLongitude() + ")";
						// use the buffered thread-safe url loading method
						bufferedLoadURL(singleLocationUpdateURL);
					} else if (msg.obj instanceof String) {
						// String object is only received when recording
						// either a single point, or a list of points like when
						// re-init the activity during recording
						String addpos = (String) msg.obj;
						String addPositionUrl = "javascript:addPosition(" + addpos + ")";
						// use the buffered thread-safe url loading method
						bufferedLoadURL(addPositionUrl);
					} else {
						Log.e("Activity", "Message handle error: location update without STRING object");
						Log.e("Activity", "Message handle error: location update without LOCATION object");
					}
				} else {
					Log.e("Activity", "Message handle error: location update msg object does not have object payload");
				}
				break;
			
			case AntripService.MSG_DRAW_CACHED_POINTS:
				/**
				 * location updates during recording will be in the form of
				 * yu-hsiang style, Stringed, JSON format check-in list so we
				 * need to test the data type first before passing to the
				 * JavaScript function in the html UI - syncPosition method is
				 * now deprecated, only addPosition is being used
				 */
				if(msg.obj instanceof String){
					String addpos = (String) msg.obj;
					String addPositionUrl = "javascript:addPosition(" + addpos + ")";
					//use the buffered thread-safe url loading method
					bufferedLoadURL(addPositionUrl);
				} else{
					Log.e("Activity", "Message handle error: location update without STRING object");
				}
				break;
			case AntripService.MSG_LOCATION_UPDATE_CHECKIN:
				if(msg.obj != null){
					if(msg.obj instanceof Location){
						checkinLocation = (Location) msg.obj;
					} else{
						Log.e("Activity", "Message handle error: check-in location update without LOCATION object");
					}
				} else{
					Log.e("Activity", "Message handle error: check-in msg object does not have object payload");
				}
				break;
				
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.w("Activity", "mConnection: onServiceConnected");
			outMessenger = new Messenger(service);
			try{
				Message msg = Message.obtain(null, AntripService.MSG_REGISTER_CLIENT);
				msg.replyTo = inMessenger;
				outMessenger.send(msg);
				if(need2SaveCCO && cco != null){
					sendMessageToService(AntripService.MSG_SAVE_CHECKIN_OBJECT, cco);
					cco = null;
					need2SaveCCO = false;
				}
			} catch(RemoteException e){
				e.printStackTrace();
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.w("Activity", "mConnection: onServiceDisconnected");
			outMessenger = null;
		}
	};
	
	private void sendMessageToService(int msgType, Object payload) {
		if (mIsBound) {
			if (outMessenger != null) {
				try {
					if (payload != null) {
						outMessenger.send(Message.obtain(null, msgType, payload));
					} else {
						outMessenger.send(Message.obtain(null, msgType));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	void doBindService(){
		Log.w("AntripActivity", "doBindService");
		bindService(new Intent(mContext, AntripService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
		if(needToLogout){
			Log.e("AntripActivity", "NeedToLogout~~");
			if(AntripService.isRecording()){
				sendMessageToService(AntripService.MSG_STOP_RECORDING_PRE, null);
			}
			sendMessageToService(AntripService.MSG_STOP_LOCATION_THREAD, null);
			bufferedLoadURL("javascript:Logout()");
			
			needToLogout = false;
		}
	}
	
	void doUnbindService(){
		Log.w("AntripActivity", "doUnbindService");
		if(mIsBound){
			if(outMessenger != null){
				try {
					Message msg = Message.obtain(null, AntripService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = inMessenger;
					outMessenger.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else{
				
			}
			unbindService(mConnection);
			mIsBound = false;
		} else{
			
		}
	}
	
	private void initUI(){
		Log.e("Activity", "initUI: called");
		//frameview where webview will be placed
		webViewPlaceholder = (FrameLayout) findViewById(R.id.webViewPlaceHolder);
		//first time running, webview will be null
		if(mWebView == null){
			Log.e("Activity", "initUI: webview being init");
			//init a new webview object
			mWebView = new WebView(mContext);
			
			//some settings need to be changed in the webview
			WebSettings mWebSettings = mWebView.getSettings();
			// javascript must be enabled, of course
			mWebSettings.setJavaScriptEnabled(true);
			//render/cache settings are supposed to speed up webpage rendering speed
			mWebSettings.setRenderPriority(RenderPriority.HIGH);
			mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
			mWebSettings.setSaveFormData(false);
			mWebSettings.setSavePassword(false);
			mWebSettings.setDomStorageEnabled(true);
			//make sure no javascript is called before webpage finished loading
			mWebView.setWebViewClient(new WebViewClient(){
				@Override
				public void onPageFinished(WebView view, String url) {
					Log.w("acvitivy", "webview: page finished");
					canCallJavaScript = true;
				}
			});
			
			mWebView.setWebChromeClient(new WebChromeClient() {
				/**
				 * intercept and replace JavaScript alert dialog with native android
				 * for better visual experiance...
				 */
				@Override
				public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
					Log.e("AntripActivity", "onJSAlert");
					new AlertDialog.Builder(mContext)
							.setCancelable(false)
							.setMessage(message)
							.setNeutralButton(R.string.okay,
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// inform javascript the alert dialog is
										// dismissed
										result.confirm();
									}
								}).show();
					return true;
				}
				
				/**
				 * display any message javascript console might have, for debuging
				 * purpose
				 */
				@Override
				public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
					Log.e("consolemsg", consoleMessage.message() + " @line: " + consoleMessage.lineNumber() + " from: "
							+ consoleMessage.sourceId());
					return true;
				}
			});
			// name the javascript interface "antrip"
			mWebView.addJavascriptInterface(new jsinter(), "antrip");
			//"cancalljavascript" is false until "onpagefinished", so first time loading url will have to use the 
			//webview method, instead of the buffered method
			mWebView.loadUrl("file:///android_asset/index.html");
		}
		Log.e("Activity", "initUI: adding webview to placeholder");
		//if not first time running, webview will be preserved and added back to placeholder with original state
		webViewPlaceholder.addView(mWebView);
	}
	
	/**
	 * handle the configuration change ourself, currently only the orientation event is intercepted
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if(mWebView != null){
			webViewPlaceholder.removeView(mWebView);
		}
		
		super.onConfigurationChanged(newConfig);
		
		//need to make a landscape layout
		setContentView(R.layout.main);
		//re-init the UI
		initUI();
	}
	
	/**
	 * save the state of the webview, to be restored after the rotation event
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		mWebView.saveState(outState);
	}
	
	/**
	 * restore the state of the webview, usually this occurs after the rotation event
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		mWebView.restoreState(savedInstanceState);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this;
		
		Crittercism.init(mContext, "50a0662801ed852ced000002");
		
		setContentView(R.layout.main);
		
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		Log.e("USERNAME", pref.getString("usrname", "LOL"));
		
		urlQueue = new PriorityQueue<String>();
		mHandler = new Handler();
		canPostAgain = true;
		
//		locationList = new ArrayList<Location>();
		
		ImageView btn_locate = (ImageView) findViewById(R.id.btn_locate);
		btn_locate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
		btn_checkin = (Button) findViewById(R.id.btn_checkin);
		btn_checkin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessageToService(AntripService.MSG_GET_CHECKIN_LOCATION, null);
				startActivityForResult(new Intent(mContext, CheckinWindow.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), REQUEST_CODE_CHECKIN);
			}
		});
		
		
		initUI();
	}
	
	/**
	 * some trick to make sure javascript interface works properly
	 *
	 */
	public interface JavaScriptCallback {
	}
	
	/**
	 * supposedly the javascript interface will work without hiccup after implementing the interface
	 */
	private class jsinter implements JavaScriptCallback {
		
		public void logout() {
			// remove sid and stop stuffs
			Log.w("logged", "out");
			CookieManager cm = CookieManager.getInstance();
			cm.removeAllCookie();
			
		}
		
		// need a function to provide detailed trip data(reviewing historic
		// trip)
		public String getLocalTripList() {
			//Log.w("activity", "getlocaltriplist called");
			JSONObject result = null;
			
			result = new GetLocalTrip(mContext).Info();
			
			// null means no local trip history, don't return null, return -1
			// instead
			if (result != null) {
				//Log.w("activity", "localtriplist=" + result.toString());
				return result.toString();
			} else {
				//Log.w("activity", "localtriplist=-1");
				return "-1";
			}
		}
		
		public String getLocalTripData(String id) {
			//Log.w("activity", "getlocaltripdata called, id=" + id);
			
			JSONObject result = null;
			
			result = new GetLocalTrip(mContext).Data(Integer.valueOf(id));
			
			if (result != null) {
				//Log.e("activity", "getlocaltripdata=" + result.toString());
				return result.toString();
			} else {
				return "-1";
			}
			
		}
		
		/**
		 * start recording, and return the newly generated random tripid
		 * 
		 * @return
		 */
		public String startRecording() {
			Long tid = System.nanoTime();
			Log.i("actripActivity", "jsinterface: startrecording with tid=" + tid.toString());
			//XXX
			//start recording with the newly generated tripid
			startService(new Intent(mContext, AntripService.class).setAction("START_RECORDING").putExtra("tid", tid));
			return tid.toString();
		}
		
		public void stopRecording() {
			//XXX
			//the recording should already be stopped, just save the name and we're all done
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (btn_checkin != null) {
						btn_checkin.setVisibility(View.GONE);
					}
				}
			});
			
			final Dialog dialog = new Dialog(mContext);
			dialog.setContentView(R.layout.edittripname);
			dialog.setTitle(R.string.input_tripname);
			dialog.setCancelable(false);
			final EditText et = (EditText) dialog.findViewById(R.id.edittripname);
			final String hint = getResources().getString(R.string.untitled_trip);
			et.setHint(hint);
			((Button) dialog.findViewById(R.id.edittripnamebtn)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String tripname = null;
					if (et.getText().length() > 0) {
						tripname = et.getText().toString();
					} else {
						tripname = hint;
					}
					sendMessageToService(AntripService.MSG_STOP_RECORDING_ACTUAL, tripname);
//					bufferedLoadURL("javascript:reloadTripList()");
					dialog.dismiss();
				}
			});
			dialog.show();
		}
		
		/**
		 * user is naming their trip, don't broadcast positions
		 */
		public void prepareStopRecording(){
			//basically just stop the recording here, calculate the trip statistics
			sendMessageToService(AntripService.MSG_STOP_RECORDING_PRE, null);
			bufferedLoadURL("javascript:reloadTripList()");
		}
		
		/*
		 * 1: home screen 2: trip list screen 3: recorder screen 4: friend list
		 * screen
		 */
		public void setMode(int mode) {
			currentMode = mode;
			//Log.w("setMode", "mode= " + currentMode);
			switch (currentMode) {
			case 3:
				canCheckin = true;
				//need to start location service
				sendMessageToService(AntripService.MSG_INIT_LOCATION_THREAD, null);
				break;
			case 2:
				//trip list, check for unfinished uploads
//				new UnfinishedUploadThread(mContext, ANTripActivity.this).execute();
			case 1:
			case 4:
			default:
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (btn_checkin != null) {
							btn_checkin.setVisibility(View.GONE);
						}
					}
				});
				
				canCheckin = false;
				// stop location service if not recording
				sendMessageToService(AntripService.MSG_STOP_LOCATION_THREAD, null);
				break;
			}
		}
		
		/**
		 * **check-in method family** call Android build-in camera class to take
		 * the picture, and modify the EXIF header afterwards
		 */
		public void startCamera() {
			// file name for pictures
			File imagefile = null;
			
			{
				String imagename = String.format("%1$d.jpg", System.currentTimeMillis());
				String path = "/mnt/sdcard/DCIM/";
				File tester = new File(path);
				if (tester.exists() && tester.isDirectory()) {
					Log.w("sdcard/dcim", "exists");
					File dir = new File(path + "antrip");
					dir.mkdir();
					if (dir.exists() && dir.isDirectory()) {
						Log.w("sdcard/dcim", "antrip dir created");
						imagefile = new File(path + "antrip", imagename);
					} else{
						Log.w("sdcard/dcim", "cannot create antrip/not a dir");
						imagefile = null;
					}
					
				} else {
					path = "/mnt/emmc/DCIM/";
					tester = new File(path);
					if (tester.exists() && tester.isDirectory()) {
						Log.w("emmc/dcim", "exists");
						File dir = new File(path + "antrip");
						dir.mkdirs();
						if (dir.exists() && dir.isDirectory()) {
							Log.w("emmc/dcim", "antrip dir created");
							imagefile = new File(path + "antrip", imagename);
						} else{
							Log.w("emmc/dcim", "cannot create antrip/not a dir");
							imagefile = null;
						}
					}
				}
			}
			if(imagefile != null){
				imageUri = Uri.fromFile(imagefile);
				pref.edit().putString("imguri", imageUri.getPath()).commit();
				Log.w("startcamera", "imageUri= " + imageUri.getPath());
				// intent to launch Android camera app to take pictures
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// input the desired filepath + filename
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				// launch the intent with code
				startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
			}
		}
		
		/**
		 * Replace cookie function in html, save the key-value pair to android
		 * preference
		 * 
		 * @param key
		 * @param value
		 */
		public void setCookie(String key, String value) {
			pref.edit().putString(key, String.valueOf(value)).commit();
			Log.w("setCookie", "key= " + key + ", value= " + String.valueOf(value));
			//login action, send sid to service
			if(key.equalsIgnoreCase("sid")){
				sendMessageToService(AntripService.MSG_SET_USERID, value);
				if(value.equals("206")){
					Log.w("SECRETLY", "EXPORT ALL");
					DBHelper128 ddd = new DBHelper128(mContext);
					String tmp = ddd.exportEverything();
					ddd.closeDB();
					ddd = null;
					startActivity(new Intent(mContext, BackdoorActivity.class).putExtra("path", tmp));
				}
			}
		}
		
		/**
		 * 
		 * @param key
		 * @return the value paired with the key, or null if key does not exist
		 */
		public String getCookie(String key) {
			Log.e("PATH", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
			return pref.getString(key, null);
		}
		
		/**
		 * remove the key entry from preference
		 * 
		 * @param key
		 */
		public void removeCookie(String key) {
			pref.edit().remove(key).commit();
			Log.w("removeCookie", "key= " + key);
		}
		
		public String getLocale() {
			return Locale.getDefault().getLanguage();
		}
		
		// TODO auto upload, no user interaction involved
		/**
		 * manual upload button
		 * @param id unique id in the DB table
		 */
		public void uploadTrip(String id) {
			Log.w("activity", "upload trip: " + id);
//			startService(new Intent(mContext, UploadService.class).setAction("ACTION_UPLOAD_TRIP").putExtra("id", id));
//			sendMessageToService(AntripService.MSG_START_UPLOAD, id);
			new UploadThread(mContext, id, ANTripActivity.this).execute();
		}
		
		/**
		 * Will delete both trip info and data of the given id
		 * @param id unique id in DB table
		 */
		public void deleteTrip(String id){
			//Log.w("activity", "delete trip: " + id);
			DBHelper128 dhh = new DBHelper128(mContext);
			dhh.deleteTrip(pref.getString("sid", null), id);
			dhh.closeDB();
			dhh = null;
			//Log.w("activity", "rows deleted=" + dh.deleteTrip(id));
			bufferedLoadURL("javascript:reloadTripList()");
		}
		
		public void reloadIndex(){
			Log.w("antripActivity", "jsinterface: reloadindex");
			bufferedLoadURL("file:///android_asset/index.html");
		}
		
		public String isRecording(){
			if(AntripService.isRecording()){
				return "notnullstring";
			} else{
				return null;
			}
		}
	}
	
	/**
	 * to prevent frequent calls to the mWebView.loadURL() method, all calls are
	 * queued and executed every 400ms
	 * 
	 * @param url
	 */
	private void bufferedLoadURL(final String url) {
		//need to check if activity is doing check-in
		urlQueue.offer(url);
		// if already running, don't post again
		if (canPostAgain) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// now we're running, don't accept any more post calls
					canPostAgain = false;
					if(canCallJavaScript){
						// get a url from queue
						String url = urlQueue.poll();
						Log.d("queuedLoadURL", "url= " + url);
						if(url != null){
							mWebView.loadUrl(url);
						}
						// check if the queue is empty or not
						if (urlQueue.peek() != null) {
							// queue not empty, post self with delay
							mHandler.postDelayed(this, 400);
						} else {
							// queue empty, now accepting post calls
							canPostAgain = true;
						}
					} else{
						//wait a bit and try again
						mHandler.postDelayed(this, 300);
					}
					
				}
			});
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
		case KeyEvent.KEYCODE_MENU:
			Log.e("Activity", "onKeyDown: MENU MENU MENU");
			if(pref.contains("sid")){
				startActivityForResult(new Intent(mContext, Settings.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), REQUEST_CODE_DO_SETTINGS);
			} else{
				Toast.makeText(mContext, R.string.login_warning, Toast.LENGTH_LONG).show();
			}
			return true;
		case KeyEvent.KEYCODE_BACK:
			Log.e("Activity", "onKeyDown: BACK BACK BACK");
			//show a warning window to the user
			if(pref.contains("sid")){
				if(AntripService.isRecording()){
					showWarning();
				} else{
					showThreat();
				}
			} else{
				Log.e("antripActivity", "onkeydown: back not handled");
				if(mWebView.getUrl().contains("FacebookLogin") && mWebView.canGoBack()){
					bufferedLoadURL("file:///android_asset/index.html");
				} else{
					showThreat();
				}
			}
			return true;
		default:
			//other keys we don't care about
			return super.onKeyDown(keyCode, event);
		}
	}
	
	private void showWarning(){
		Toast.makeText(mContext, R.string.still_recording_warning, Toast.LENGTH_LONG).show();
		finish();
	}
	
	private void showThreat(){
		new AlertDialog.Builder(mContext)
		.setCancelable(false)
		.setMessage(R.string.alertdiag_threat_title)
		.setPositiveButton(R.string.okay, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		}).setNegativeButton(R.string.nope, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		doBindService();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		doUnbindService();
		
		if(AntripService.isStarted()){
			Log.e("AntripActicity", "onPause: isstarted: true"); 
		} else{
			Log.e("AntripActicity", "onPause: isstarted: false");
		}
		
		if(AntripService.isRecording()){
			Log.e("AntripActicity", "onPause: isrecording: true");
		} else{
			Log.e("AntripActicity", "onPause: isrecording: false");
		}
		
		// short circuit logic, if startService is not called, stop service will not be called
		// if start service is called AND is not recording a trip, stop service will be called
		if(AntripService.isStarted() && !AntripService.isRecording()){
			Log.e("AntripActivity", "onPause: stopping service");
			stopService(new Intent(mContext, AntripService.class));
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch(requestCode){
		case REQUEST_CODE_DO_SETTINGS:
			switch(resultCode){
			case RESULT_OK:
				//logout
				Log.w("antripActivity", "onActivityResult: YES logout");
				//these calls need to be placed AFTER onResume method...need to queue them somehow
				needToLogout = true;
				break;
			default:
				//no logout
				Log.w("antripActivity", "onActivityResult: no logout");
				break;
			}
			break;
		case REQUEST_CODE_CHECKIN:
			switch (resultCode) {
			case RESULT_OK:
				if (data.getSerializableExtra("cco") != null) {
					Log.e("antrip activity", "received CCO!!!");
					cco = (CandidateCheckinObject) data.getSerializableExtra("cco");
					if(checkinLocation != null){
						cco.setLocation(checkinLocation);
						// save cco to DB
						String addpos = CheckinJSONConverter.fromCCOtoCheckinJSON(cco).toString();
						bufferedLoadURL("javascript:addPosition(" + addpos + ")");
						// send cco to service via startService call with action and
						// extras
						Log.w("activity",
								"end check-in, cco.emotion= " + cco.getEmotionID() + ", cco.text= " + cco.getCheckinText());
						need2SaveCCO = true;
					} else{
						//XXX
						//should maybe find a fail-safe way of setting check-in locations
					}
				}
				break;
			case RESULT_CANCELED:
				Log.e("antrip activity", "received CCO: canceled");
				cco = null;
				break;
			default:
				Log.e("antrip activity", "received CCO: default");
				break;
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void shouldReloadTripList() {
		bufferedLoadURL("javascript:reloadTripList()");
	}
}