package tw.plash.antrip;

import java.io.File;
import java.util.Locale;
import java.util.PriorityQueue;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ANTripActivity extends Activity {
//	private final boolean useSkyhook = true;
	
	private Context mContext;
	private WebView mWebView;
	
	private Integer previousMode;
	private Integer currentMode;
	
	private View usableArea;
	private boolean loadIndex = false;
	
	private CandidateCheckinObject cco;
	
	private Uri imageUri;
	private final int REQUEST_CODE_TAKE_PICTURE = 100;
	
	private SharedPreferences pref;
	
	private PriorityQueue<String> urlQueue;
	private Handler mHandler;
	private boolean canPostAgain;
	private boolean canCallJavaScript = false;
	
	private BroadcastReceiver br = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// not recording, just positioning
			if (intent.getAction().equals("ACTION_LOCATION_SERVICE_SET_POSITION")) {
				Location loc = (Location) intent.getExtras().getParcelable("location");
				String singleLocationUpdateURL = "javascript:setPosition(" + loc.getLatitude() + ","
						+ loc.getLongitude() + ")";
				// push this location update to html
				queuedLoadURL(singleLocationUpdateURL);
				// recording, syncing the whole position list
			} else if (intent.getAction().equals("ACTION_LOCATION_SERVICE_ADD_POSITION")) {
				String addpos = intent.getExtras().getString("location");
				String addPositionUrl = "javascript:addPosition(" + addpos + ")";
				queuedLoadURL(addPositionUrl);
				// recording, adding a new point to the list
			} else if (intent.getAction().equals("ACTION_LOCATION_SERVICE_SYNC_POSITION")) {
				String syncpos = intent.getExtras().getString("location");
				String syncPositionUrl = "javascript:syncPosition(" + syncpos + ")";
				queuedLoadURL(syncPositionUrl);
			} else if(intent.getAction().equals("ACTION_RELOAD_TRIPLIST")){
				queuedLoadURL("javascript:reloadTripList()");
			}else{
				// huh?
			}
		}
	};
	
	public void onWindowFocusChanged(boolean hasFocus) {
		usableArea = getWindow().findViewById(Window.ID_ANDROID_CONTENT);
		if (hasFocus) {
			//Log.w("DISPLAY", usableArea.getWidth() + " x " + usableArea.getHeight() + " hasFocus la");
		} else {
			//Log.w("DISPLAY", usableArea.getWidth() + " x " + usableArea.getHeight() + " no focus");
		}
		if (loadIndex) {
			mWebView.loadUrl("file:///android_asset/index.html");
			loadIndex = false;
		}
	};
	
	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (LocationService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		mContext = this;
		// perform a self check, upload any unfinished jobs
		// if there are unfinished jobs, it will be uploaded SECRETLY
		// if there are no unfinished jobs, service will stop it self
		startService(new Intent(mContext, UploadService.class).setAction("ACTION_SELF_CHECK_AND_UPLOAD"));
		
		mWebView = (WebView) findViewById(R.id.webview);
		
		previousMode = null;
		
		urlQueue = new PriorityQueue<String>();
		mHandler = new Handler();
		canPostAgain = true;
		
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		//error case: is recording but service is not running
		if(pref.getString("isRecording", null) != null && pref.getString("isRecording", null).equals("true")){
			//is recording = true, now check if service is running or not
			if(!isMyServiceRunning()){
				//service is not running, reset everything
				pref.edit().remove("isRecording").commit();
				startService(new Intent(mContext, LocationService.class).setAction("ACTION_CLEAN_UP"));
			}
		}
		
		WebSettings mWebSettings = mWebView.getSettings();
		// javascript must be enabled, of course
		mWebSettings.setJavaScriptEnabled(true);
		// mWebSettings.setDomStorageEnabled(true);
		
		mWebView.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url) {
				//Log.w("acvitivy", "page finished");
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
						.setNeutralButton((Locale.getDefault().getLanguage().equals("zh")) ? "¦nªº" : "Okay",
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
				//Log.e("consolemsg", consoleMessage.message() + " @line: " + consoleMessage.lineNumber() + " from: "
//						+ consoleMessage.sourceId());
				return true;
			}
		});
		// name the javascript interface "antrip"
		mWebView.addJavascriptInterface(new jsinter(), "antrip");
		// now after all the settings, load the html file
		// mWebView.loadUrl("file:///android_asset/index.html");
		// queuedLoadURL("file:///android_asset/index.html");
		loadIndex = true;
	}
	
	public interface JavaScriptCallback {
	}
	
	private class jsinter implements JavaScriptCallback {
		
		// private final String imagepath =
		// Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
		// TODO:
		// cannot assume getExtFilesDir will always return the path, might be
		// null, need to check before proceeding
		private final String imagepath = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
		
		public void logout() {
			// remove sid and stop stuffs
			//Log.w("logged", "out");
			
		}
		
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
//			if (useSkyhook) {
				startService(new Intent(mContext, LocationService.class).setAction("ACTION_START_RECORDING")
					.putExtra("tid", tid.toString()));
//			} else {
//				startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_START_RECORDING")
//						.putExtra("tid", tid.toString()));
//			}
			return tid.toString();
		}
		
		/**
		 * 
		 */
		public void stopRecording(String tripname) {
//			if (useSkyhook) {
				startService(new Intent(mContext, LocationService.class).setAction("ACTION_STOP_RECORDING")
					.putExtra("tripname", tripname));
//			} else {
//				startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_STOP_RECORDING")
//					.putExtra("tripname", tripname));
//			}
		}
		
		/**
		 * user is naming their trip, don't broadcast positions
		 */
		public void prepareStopRecording(){
//			if (useSkyhook) {
				startService(new Intent(mContext, LocationService.class).setAction("ACTION_GET_CHECKIN_LOCATION"));
//			} else {
//				startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_GET_CHECKIN_LOCATION"));
//			}
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
				// start location service
//				if (useSkyhook) {
					startService(new Intent(mContext, LocationService.class).setAction("ACTION_START_SERVICE"));
					startService(new Intent(mContext, LocationService.class)
							.setAction("ACTION_ACTIVITY_IS_READY_FOR_BROADCAST"));
//				} else {
//					startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_START_SERVICE"));
//					startService(new Intent(mContext, LocationServiceGPS.class)
//							.setAction("ACTION_ACTIVITY_IS_READY_FOR_BROADCAST"));
//				}
				if (isrec != null && isrec.equals("true")) {
//					if (useSkyhook) {
						startService(new Intent(mContext, LocationService.class)
								.setAction("ACTION_LOCATION_SERVICE_SYNC_POSITION"));
//					} else {
//						startService(new Intent(mContext, LocationServiceGPS.class)
//								.setAction("ACTION_LOCATION_SERVICE_SYNC_POSITION"));
//					}
				}
				break;
			case 1:
			case 2:
			case 4:
			default:
				// stop location service if not recording
				
				//Log.w("setMode", "isrec= " + isrec);
				if (isrec == null || !isrec.equals("true")) {
//					if (useSkyhook) {
						stopService(new Intent(mContext, LocationService.class));
//					} else {
//						stopService(new Intent(mContext, LocationServiceGPS.class));
//					}
				}
				break;
			}
			// not sure if I need to know what the previous mode was, save it
			// anyway
			previousMode = currentMode;
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
			
			// should add extra functions to camera, e.g. filters, special
			// effects, stickers, etc.
			// startActivityForResult((new Intent(mContext,
			// test21.class).putExtra("requestCode", 1)), 1);
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
//			if (useSkyhook) {
			Log.e("activity", "start check-in");
				startService(new Intent(mContext, LocationService.class).setAction("ACTION_GET_CHECKIN_LOCATION"));
//			} else {
//				startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_GET_CHECKIN_LOCATION"));
//			}
		}
		
		/**
		 * **check-in method family** confirmed check-in action, pass the
		 * candidate check-in object to location service to be saved
		 */
		public void endCheckin() {
			// send cco to service via startService call with action and extras
//			if (useSkyhook) {
			Log.w("activity", "end check-in, cco.emotion= " + cco.getEmotionID() + ", cco.text= " + cco.getCheckinText());
				startService(new Intent(mContext, LocationService.class).setAction("ACTION_SAVE_CCO").putExtra("cco",
						cco));
//			} else {
//				startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_SAVE_CCO").putExtra(
//						"cco", cco));
//			}
		}
		
		/**
		 * **check-in method family** check-in action is canceled, drop the
		 * candidate check-in object
		 */
		public void cancelCheckin() {
			cco = null;
//			if (useSkyhook) {
			Log.e("activity", "cancel check-in");
				startService(new Intent(mContext, LocationService.class).setAction("ACTION_CANCEL_CHECKIN"));
//			} else {
//				startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_CANCEL_CHECKIN"));
//			}
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
			//if 
			if(key.equals("sid")){
				startService(new Intent(mContext, UploadService.class).setAction("ACTION_SELF_CHECK_AND_UPLOAD"));
			}
			// if the sid equals cszu's sid, export all info and data to file
//			if (key.equals("sid") && value.equals("206")) {
//				//Log.w("SECRETLY", "EXPORT ALL");
//				DBHelper128 ddd = new DBHelper128(mContext);
//				ddd.exportEverything();
//				ddd.closeDB();
//				ddd = null;
//			}
		}
		
		/**
		 * 
		 * @param key
		 * @return the value paired with the key, or null if key does not exist
		 */
		public String getCookie(String key) {
			//Log.w("getCookie", "key= " + key + ", value= " + pref.getString(key, null));
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
			// //Log.e("locale/getDisplayLanguage",
			// Locale.getDefault().getDisplayLanguage());
			// //Log.e("locale/getCountry", Locale.getDefault().getCountry());
			// //Log.e("locale/getDisplayCountry",
			// Locale.getDefault().getDisplayCountry());
			// //Log.e("locale/getDisplayName",
			// Locale.getDefault().getDisplayName());
			// //Log.e("locale/getDisplayVariant",
			// Locale.getDefault().getDisplayVariant());
			// //Log.e("locale/getISO3Country",
			// Locale.getDefault().getISO3Country());
			// //Log.e("locale/getISO3Language",
			// Locale.getDefault().getISO3Language());
			// //Log.e("locale/getLanguage", Locale.getDefault().getLanguage());
			// //Log.e("locale/getVariant", Locale.getDefault().getVariant());
			return Locale.getDefault().getLanguage();
		}
		
		// TODO auto upload, no user interaction involved
		/**
		 * manual upload button
		 * @param id unique id in the DB table
		 */
		public void uploadTrip(String id) {
			//Log.w("activity", "upload trip: " + id);
			startService(new Intent(mContext, UploadService.class).setAction("ACTION_UPLOAD_TRIP").putExtra("id", id));
		}
		
		/**
		 * Will delete both trip info and data of the given id
		 * @param id unique id in DB table
		 */
		public void deleteTrip(String id){
			//Log.w("activity", "delete trip: " + id);
			DBHelper128 dh = new DBHelper128(mContext);
			//Log.w("activity", "rows deleted=" + dh.deleteTrip(id));
			queuedLoadURL("javascript:reloadTripList()");
		}
		
		/**
		 * return screen density value
		 * 
		 * @return
		 */
		public float getdpi() {
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			return dm.density;
		}
		
		/**
		 * return all display related metrics in the following order density;
		 * scaled density; x dpi; y dpi; width pixels; height pixels
		 * 
		 * @return float[] containing the 6 display metrics
		 * @throws JSONException
		 */
		public String getScreenInfo() throws JSONException {
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			// return new float[]{dm.density, dm.scaledDensity, dm.xdpi,
			// dm.ydpi, usableArea.getWidth(), usableArea.getHeight()};
			String s = "{\"density\":\"" + dm.density 
					+ "\", \"scaledDensity\":\"" + dm.scaledDensity
					+ "\", \"xdpi\":\"" + dm.xdpi 
					+ "\", \"ydpi\":\"" + dm.ydpi 
					+ "\", \"width\":\""+ usableArea.getWidth() 
					+ "\", \"height\":\"" + usableArea.getHeight() + "\"}";
			//Log.e("screeninfo", s);
			return new JSONObject(s).toString();
		}
		
		public void reloadIndex(){
			mWebView.loadUrl("file:///android_asset/index.html");
		}
	}
	
	/**
	 * to prevent frequent calls to the mWebView.loadURL() method, all calls are
	 * queued and executed every 400ms
	 * 
	 * @param url
	 */
	private void queuedLoadURL(final String url) {
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
	protected void onResume() {
		super.onResume();
		// need to notify location service to send location update
		IntentFilter filter = new IntentFilter();
		filter.addAction("ACTION_LOCATION_SERVICE_SET_POSITION");
		filter.addAction("ACTION_LOCATION_SERVICE_ADD_POSITION");
		filter.addAction("ACTION_LOCATION_SERVICE_SYNC_POSITION");
		registerReceiver(br, filter);
		// only notify service to start broadcasting if it is running, either
		// isrec = true, or we're in mode 3
		String isrec = pref.getString("isRecording", null);
		if (isrec != null && isrec.equals("true")) {
//			if (useSkyhook) {
				startService(new Intent(mContext, LocationService.class)
						.setAction("ACTION_ACTIVITY_IS_READY_FOR_BROADCAST"));
				startService(new Intent(mContext, LocationService.class)
						.setAction("ACTION_LOCATION_SERVICE_SYNC_POSITION"));
//			} else {
//				startService(new Intent(mContext, LocationServiceGPS.class)
//						.setAction("ACTION_ACTIVITY_IS_READY_FOR_BROADCAST"));
//				startService(new Intent(mContext, LocationServiceGPS.class)
//						.setAction("ACTION_LOCATION_SERVICE_SYNC_POSITION"));
//			}
		} else if (currentMode != null && currentMode == 3) {
			// need to re-start the service if it is in recorder mode
//			if (useSkyhook) {
				startService(new Intent(mContext, LocationService.class).setAction("ACTION_START_SERVICE"));
				startService(new Intent(mContext, LocationService.class)
						.setAction("ACTION_ACTIVITY_IS_READY_FOR_BROADCAST"));
//			} else {
//				startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_START_SERVICE"));
//				startService(new Intent(mContext, LocationServiceGPS.class)
//						.setAction("ACTION_ACTIVITY_IS_READY_FOR_BROADCAST"));
//			}
		}
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// save to pref that activity is NOT ready for broadcast
		String isrec = pref.getString("isRecording", null);
		// only 2 cases where service is running, isrec = true, or we're in mode
		// 3
		if (isrec != null && isrec.equals("true")) {
//			if (useSkyhook) {
				startService(new Intent(mContext, LocationService.class)
						.setAction("ACTION_ACTIVITY_NOT_READY_FOR_BROADCAST"));
//			} else {
//				startService(new Intent(mContext, LocationServiceGPS.class)
//						.setAction("ACTION_ACTIVITY_NOT_READY_FOR_BROADCAST"));
//			}
		} else {
			// not recording
//			if (useSkyhook) {
				stopService(new Intent(mContext, LocationService.class));
//			} else {
//				stopService(new Intent(mContext, LocationServiceGPS.class));
//			}
		}
		// need to notify location service not to send location update
		unregisterReceiver(br);
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
				String imageURL = null;
				if (imageuri != null) {
					imageURL = "javascript:showPicture('" + imageuri + "')";
				} else {
					imageURL = noImageURL;
				}
				
				// mWebView.loadUrl(imageURL);
				queuedLoadURL(imageURL);
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
				queuedLoadURL(noImageURL);
				break;
			}
		}
	}
}