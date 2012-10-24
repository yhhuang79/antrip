package tw.plash.antrip;

import java.io.File;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard.Key;
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
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class ANTripActivity extends Activity {
	
	private Context mContext;
	private WebView mWebView = null;
	private FrameLayout webViewPlaceholder;
	
	private Integer currentMode;
	
	private CandidateCheckinObject cco;
	
	private Uri imageUri;
	private final int REQUEST_CODE_TAKE_PICTURE = 100;
	
	private SharedPreferences pref;
	
	private LinkedBlockingQueue<Location> locationQueue;
	
	private PriorityQueue<String> urlQueue;
	private Handler mHandler;
	private boolean canPostAgain;
	private boolean canCallJavaScript = false;
	
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
					//will be provided with different location object in recording/non-recording situation
					if (AntripService.isRecording()) {
						/**
						 * location updates during recording will be in the form of yu-hsiang style, Stringed, JSON format check-in list
						 * so we need to test the data type first before passing to the JavaScript function in the html UI
						 * - syncPosition method is now deprecated, only addPosition is being used
						 */
						if(msg.obj instanceof String){
							String addpos = (String) msg.obj;
							String addPositionUrl = "javascript:addPosition(" + addpos + ")";
							//use the buffered thread-safe url loading method
							bufferedLoadURL(addPositionUrl);
						} else{
							Log.e("Activity", "Message handle error: location update without STRING object");
						}
					} else {
						/**
						 * non recording location updates will be in the form of Android Location object
						 * 
						 */
						if(msg.obj instanceof Location){
							Location loc = (Location) msg.obj;
							// setPosition
							String singleLocationUpdateURL = "javascript:setPosition(" + loc.getLatitude() + "," + loc.getLongitude() + ")";
							//use the buffered thread-safe url loading method
							bufferedLoadURL(singleLocationUpdateURL);
						} else{
							Log.e("Activity", "Message handle error: location update without LOCATION object");
						}
					}
				} else{
					Log.e("Activity", "Message handle error: location update msg object does not have object payload");
				}
				break;
			case AntripService.MSG_LOCATION_UPDATE_CHECKIN:
				if(msg.obj != null){
					if(msg.obj instanceof Location){
						if(cco != null){
							cco.setLocation((Location) msg.obj);
						} else{
							Log.e("Activity", "Check-in object error: check-in object is null, cannot set location");
						}
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
	
	private void sendMessageToService(int msgType, Object payload){
		if(mIsBound){
			if(outMessenger != null){
				try {
					switch(msgType){
					//set mode = map view
					case AntripService.MSG_INIT_LOCATION_THREAD:
						outMessenger.send(Message.obtain(null, msgType));
						break;
					//set mode != map view
					case AntripService.MSG_STOP_LOCATION_THREAD:
						outMessenger.send(Message.obtain(null, msgType));
						break;
//					case AntripService.MSG_START_RECORDING:
//						outMessenger.send(Message.obtain(null, msgType, payload));
//						break;
					case AntripService.MSG_STOP_RECORDING_PRE:
						outMessenger.send(Message.obtain(null, msgType));
						break;
					case AntripService.MSG_STOP_RECORDING_ACTUAL:
						outMessenger.send(Message.obtain(null, msgType, payload));
						break;
					case AntripService.MSG_GET_CHECKIN_LOCATION:
						outMessenger.send(Message.obtain(null, msgType));
						break;
					case AntripService.MSG_SAVE_CHECKIN_LOCATION:
						outMessenger.send(Message.obtain(null, msgType, payload));
						break;
					case AntripService.MSG_SET_USERID:
						outMessenger.send(Message.obtain(null, msgType, payload));
						break;
					case AntripService.MSG_LOGOUT_EVENT:
						outMessenger.send(Message.obtain(null, msgType));
						break;
					default:
						break;
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else{
				
			}
		} else{
			
		}
	}
	
	void doBindService(){
		bindService(new Intent(mContext, AntripService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}
	
	void doUnbindService(){
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
		
		setContentView(R.layout.main);
		
		mContext = this;

		urlQueue = new PriorityQueue<String>();
		mHandler = new Handler();
		canPostAgain = true;
		
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
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
		
		// private final String imagepath =
		// Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
		// TODO:
		// cannot assume getExtFilesDir will always return the path, might be
		// null, need to check before proceeding
		private final String imagepath = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
		
//		public void logout() {
//			// remove sid and stop stuffs
//			Log.w("logged", "out");
//			sendMessageToService(AntripService.MSG_LOGOUT_EVENT, null);
//		}
		
		// need a function to provide detailed trip data(reviewing historic
		// trip)
		public String getLocalTripList() {
			//Log.w("activity", "getlocaltriplist called");
			// Toast.makeText(mContext, "getLocalTripList",
			// Toast.LENGTH_SHORT).show();
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
			//XXX
			//start recording with the newly generated tripid
			startService(new Intent("START_RECORDING", null, mContext, AntripService.class).putExtra("tid", tid));
			return tid.toString();
		}
		
		/**
		 * 
		 */
		public void stopRecording(String tripname) {
			//XXX
			//the recording should already be stopped, just save the name and we're all done
			sendMessageToService(AntripService.MSG_STOP_RECORDING_ACTUAL, tripname);
		}
		
		/**
		 * user is naming their trip, don't broadcast positions
		 */
		public void prepareStopRecording(){
			//basically just stop the recording here, calculate the trip statistics
			sendMessageToService(AntripService.MSG_STOP_RECORDING_PRE, null);
		}
		
		/*
		 * 1: home screen 2: trip list screen 3: recorder screen 4: friend list
		 * screen
		 */
		public void setMode(int mode) {
			currentMode = mode;
			//Log.w("setMode", "mode= " + currentMode);
			String isrec = pref.getString("isRecording", null);
			switch (currentMode) {
			case 3:
				//need to start location service
				sendMessageToService(AntripService.MSG_INIT_LOCATION_THREAD, null);
				break;
			case 1:
			case 2:
			case 4:
			default:
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
			String imagename = String.format("%1$d.jpg", System.currentTimeMillis());
			// complete file path for picture
			imageUri = Uri.fromFile(new File(imagepath, imagename));
			pref.edit().putString("imguri", imageUri.getPath()).commit();
			//Log.w("startcamera", "imageUri= " + imageUri.getPath());
			// intent to launch Android camera app to take pictures
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// input the desired filepath + filename
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			// launch the intent with code
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
		}
		
		/**
		 * **check-in method family** save the emotion id to candidate check-in
		 * object
		 */
		public void setEmotion(int id) {
			if (cco != null) {
				Log.w("activity", "setEmotion= " + id);
				cco.setEmotionID(id);
			}
		}
		
		/**
		 * **check-in method family** save the inputted text to candidate
		 * check-in object
		 */
		public void setText(String text) {
			if (cco != null) {
				Log.w("activity", "setText= " + text);
				cco.setCheckinText(text);
			}
		}
		
		/**
		 * **check-in method family** create a new candidate check-in object
		 * also request a coordinate from location service
		 */
		public void startCheckin() {
			//Log.w("activity", "start checkin called");
			cco = new CandidateCheckinObject();
			// request a check-in location from service
			Log.e("activity", "start check-in");
		}
		
		/**
		 * **check-in method family** confirmed check-in action, pass the
		 * candidate check-in object to location service to be saved
		 */
		public void endCheckin() {
			// send cco to service via startService call with action and extras
			Log.w("activity", "end check-in, cco.emotion= " + cco.getEmotionID() + ", cco.text= " + cco.getCheckinText());
			sendMessageToService(AntripService.MSG_SAVE_CHECKIN_LOCATION, cco);
		}
		
		/**
		 * **check-in method family** check-in action is canceled, drop the
		 * candidate check-in object
		 */
		public void cancelCheckin() {
			cco = null;
			Log.e("activity", "cancel check-in");
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
			//Log.w("setCookie", "key= " + key + ", value= " + String.valueOf(value));
			//login action, send sid to service
			if(key.equalsIgnoreCase("sid")){
				sendMessageToService(AntripService.MSG_SET_USERID, value);
				if(value.equals("206")){
					Log.w("SECRETLY", "EXPORT ALL");
					DBHelper128 ddd = new DBHelper128(mContext);
					ddd.exportEverything();
					ddd.closeDB();
					ddd = null;
				}
			}
		}
		
		/**
		 * 
		 * @param key
		 * @return the value paired with the key, or null if key does not exist
		 */
		public String getCookie(String key) {
			//Log.w("getCookie", "key= " + key + ", value= " + pref.getString(key, null));
			//
			return pref.getString(key, null);
		}
		
		/**
		 * remove the key entry from preference
		 * 
		 * @param key
		 */
		public void removeCookie(String key) {
			pref.edit().remove(key).commit();
			//Log.w("removeCookie", "key= " + key);
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
			//Log.w("activity", "upload trip: " + id);
//			startService(new Intent(mContext, UploadService.class).setAction("ACTION_UPLOAD_TRIP").putExtra("id", id));
		}
		
		/**
		 * Will delete both trip info and data of the given id
		 * @param id unique id in DB table
		 */
		public void deleteTrip(String id){
			//Log.w("activity", "delete trip: " + id);
			DBHelper128 dh = new DBHelper128(mContext);
			//Log.w("activity", "rows deleted=" + dh.deleteTrip(id));
			bufferedLoadURL("javascript:reloadTripList()");
		}
		
		public void reloadIndex(){
			bufferedLoadURL("file:///android_asset/index.html");
//			mWebView.loadUrl("file:///android_asset/index.html");
		}
	}
	
	/**
	 * to prevent frequent calls to the mWebView.loadURL() method, all calls are
	 * queued and executed every 400ms
	 * 
	 * @param url
	 */
	private void bufferedLoadURL(final String url) {
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
						mWebView.loadUrl(url);
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
			
			return true;
		case KeyEvent.KEYCODE_BACK:
			Log.e("Activity", "onKeyDown: BACK BACK BACK");
			//show a warning window to the user
			new AlertDialog.Builder(mContext)
				.setCancelable(false)
				.setMessage("are you sure you want to quit ANTRIP?")
				.setPositiveButton("yeah", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).setNegativeButton("nah...", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
			return true;
		default:
			//other keys we don't care about
			return false;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		doBindService();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		try{
			doUnbindService();
		} catch(Throwable t){
			Log.e("AntripActivity", "Failed to unbind from AntripService");
		}
		
		// short circuit logic, if startService is not called, stop service will not be called
		// if start service is called AND is not recording a trip, stop service will be called
		if(AntripService.isStarted() && !AntripService.isRecording()){
			stopService(new Intent(getApplicationContext(), AntripService.class));
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		final String noImageURL = "javascript:showPicture(-1)";
		
		if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
			switch (resultCode) {
			case RESULT_OK:
				// need to grab the generated filename plus filepath and return
				// it to html for display purpose
				String imageuri = pref.getString("imguri", null);
				
				//resize
				SafeBitmapResizer.resize(imageuri);
				
				String imageURL = null;
				if (imageuri != null) {
					imageURL = "javascript:showPicture('" + imageuri + "')";
				} else {
					imageURL = noImageURL;
				}
				
				// mWebView.loadUrl(imageURL);
				bufferedLoadURL(imageURL);
				//Log.e("onActivityResult", "imageURL= " + imageURL);
				if (cco != null) {
					cco.setPicturePath(imageuri);
				}
				break;
			case RESULT_CANCELED:
				// decides to not take a picture after all
				// break;
			case RESULT_FIRST_USER:
				// not sure when will this method be called
				// break;
			default:
				// handle all exceptions
				
				// mWebView.loadUrl(noImageURL);
				bufferedLoadURL(noImageURL);
				break;
			}
		}
	}
}