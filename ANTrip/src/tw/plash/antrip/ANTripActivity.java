package tw.plash.antrip;

import java.io.File;
import java.util.Locale;
import java.util.PriorityQueue;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
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
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ANTripActivity extends Activity {
	private Context mContext;
	private WebView mWebView;
	
	private Integer previousMode;
	private Integer currentMode;
	
	private CandidateCheckinObject cco;
	
	private Uri imageUri;
	private final int REQUEST_CODE_TAKE_PICTURE = 100;
	
	private SharedPreferences pref;
	
	private PriorityQueue<String> urlQueue;
	private Handler mHandler;
	private boolean canPostAgain;
	
	private BroadcastReceiver br = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//not recording, just positioning
			if(intent.getAction().equals("ACTION_LOCATION_SERVICE_SET_POSITION")){
				Location loc = (Location) intent.getExtras().getParcelable("location");
				String singleLocationUpdateURL = "javascript:setPosition(" + loc.getLatitude() + "," + loc.getLongitude() + ")";;
				//push this location update to html
				queuedLoadURL(singleLocationUpdateURL);
				//recording, syncing the whole position list
			} else if(intent.getAction().equals("ACTION_LOCATION_SERVICE_ADD_POSITION")){
				String addpos = intent.getExtras().getString("location");
				String addPositionUrl = "javascript:addPosition(" + addpos + ")";
				queuedLoadURL(addPositionUrl);
				//recording, adding a new point to the list
			} else if(intent.getAction().equals("ACTION_LOCATION_SERVICE_SYNC_POSITION")){
				String syncpos = intent.getExtras().getString("location");
				String syncPositionUrl = "javascript:syncPosition(" + syncpos + ")";
				queuedLoadURL(syncPositionUrl);
			} else{
				//huh?
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setContentView(R.layout.main);
		
		mContext = this;
		
		mWebView = (WebView) findViewById(R.id.webview);
		
		previousMode = null;
		
		urlQueue = new PriorityQueue<String>();
		mHandler = new Handler();
		canPostAgain = true;
		
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		WebSettings mWebSettings = mWebView.getSettings();
		//javascript must be enabled, of course
		mWebSettings.setJavaScriptEnabled(true);
//		mWebSettings.setDomStorageEnabled(true);
		
		mWebView.setWebViewClient(new WebViewClient()/*{
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}
		}*/);
		mWebView.setWebChromeClient(new WebChromeClient(){
			/**
			 * intercept and replace JavaScript alert dialog with native android for better visual experiance...
			 */
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final JsResult result) {
					new AlertDialog.Builder(mContext)
						.setCancelable(false)
						.setMessage(message)
						.setNeutralButton((Locale.getDefault().getLanguage().equals("zh"))?"¦nªº":"Okay", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//inform javascript the alert dialog is dismissed
								result.confirm();
							}
						})
						.show();
				return true;
			}
			/**
			 * display any message javascript console might have, for debuging purpose
			 */
			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
				Log.e("consolemsg", consoleMessage.message() + " @line: " + consoleMessage.lineNumber() + " from: " + consoleMessage.sourceId());
				return true;
			}
		});
		//name the javascript interface "antrip"
		mWebView.addJavascriptInterface(new jsinter(), "antrip");
		//now after all the settings, load the html file
//		mWebView.loadUrl("file:///android_asset/index.html");
		queuedLoadURL("file:///android_asset/index.html");
	}
	
	public interface JavaScriptCallback{}
	
	private class jsinter implements JavaScriptCallback{
		
//		private final String imagepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
		//TODO:
		// cannot assume getExtFilesDir will always return the path, might be null, need to check before proceeding
		private final String imagepath = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
		
		public void logout(){
			//remove sid and stop stuffs
			Log.e("logged", "out");
		}
		
		// need a function to provide detailed trip data(reviewing historic trip)
		public String getLocalTripList(){
			Log.e("activity", "getlocaltriplist called");
//			Toast.makeText(mContext, "getLocalTripList", Toast.LENGTH_SHORT).show();
			JSONObject result = null;
			
			result = new GetLocalTrip(mContext).Info();
			
			//null means no local trip history, don't return null, return -1 instead
			if(result != null){
				Log.e("activity", "localtriplist=" + result.toString());
				return result.toString();
			} else{
				Log.e("activity", "localtriplist=-1");
				return "-1";
			}
		}
		
		public String getLocalTripData(String id){
			Log.e("activity", "getlocaltripdata called, id=" + id);
			
			JSONObject result = null;
			
			result = new GetLocalTrip(mContext).Data(Integer.valueOf(id));
			
			if(result != null){
				Log.e("activity", "localtrip tid=" + "" + ", result=" + result.toString());
				return result.toString();
			} else{
				return "-1";
			}
			
		}
		
		/**
		 * start recording, and return the newly generated random tripid
		 * @return
		 */
		public String startRecording(){
			Long tid = System.nanoTime();
//			startService(new Intent(mContext, LocationService.class).setAction("ACTION_START_RECORDING").putExtra("tid", tid.toString()));
			startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_START_RECORDING").putExtra("tid", tid.toString()));
			return tid.toString();
		}
		
		/**
		 * 
		 */
		public void stopRecording(String tripname){
//			startService(new Intent(mContext, LocationService.class).setAction("ACTION_STOP_RECORDING").putExtra("tripname", tripname));
			startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_STOP_RECORDING").putExtra("tripname", tripname));
		}
		
		/*
		 * 1: home screen
		 * 2: trip list screen
		 * 3: recorder screen
		 * 4: friend list screen
		 */
		public void setMode(int mode){
			currentMode = mode;
			Log.e("setMode", "mode= " + currentMode);
			String isrec = pref.getString("isRecording", null);
			switch(currentMode){
			case 3:
				//start location service
//				startService(new Intent(mContext, LocationService.class).setAction("ACTION_START_SERVICE"));
//				startService(new Intent(mContext, LocationService.class).setAction("ACTION_ACTIVITY_IS_READY_FOR_BROADCAST"));
				startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_START_SERVICE"));
				startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_ACTIVITY_IS_READY_FOR_BROADCAST"));
				if(isrec != null && isrec.equals("true")){
//					startService(new Intent(mContext, LocationService.class).setAction("ACTION_LOCATION_SERVICE_SYNC_POSITION"));
					startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_LOCATION_SERVICE_SYNC_POSITION"));
				}
				break;
			case 1:
			case 2:
			case 4:
			default:
				//stop location service if not recording
				
				Log.e("setMode", "isrec= " + isrec);
				if(isrec == null || !isrec.equals("true")){
//					stopService(new Intent(mContext, LocationService.class));
					stopService(new Intent(mContext, LocationServiceGPS.class));
				}
				break;
			}
			// not sure if I need to know what the previous mode was, save it anyway
			previousMode = currentMode;
		}
		
		/**
		 * **check-in method family**
		 * call Android build-in camera class to take the picture, and modify the EXIF header afterwards 
		 */
		public void startCamera(){
			//file name for pictures
			String imagename = String.format("%1$d.jpg", System.currentTimeMillis());
			//complete file path for picture
			imageUri = Uri.fromFile(new File(imagepath, imagename));
Log.e("startcamera", "imageUri= " + imageUri.getPath());
			//intent to launch Android camera app to take pictures
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			//input the desired filepath + filename
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			//launch the intent with code
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
			
			// should add extra functions to camera, e.g. filters, special effects, stickers, etc.
//			startActivityForResult((new Intent(mContext, test21.class).putExtra("requestCode", 1)), 1);
		}
		/**
		 * **check-in method family**
		 * save the emotion id to candidate check-in object
		 */
		public void setEmotion(int id){
			if(cco!=null){
				Log.e("activity", "setEmotion= " + id);
				cco.setEmotionID(id);
			}
		}
		/**
		 * **check-in method family**
		 * save the inputted text to candidate check-in object
		 */
		public void setText(String text){
			if(cco != null){
				Log.e("activity", "setText= " + text);
				cco.setCheckinText(text);
			}
		}
		/**
		 * **check-in method family**
		 * create a new candidate check-in object
		 * also request a coordinate from location service
		 */
		public void startCheckin(){
			Log.e("activity", "start checkin called");
			cco = new CandidateCheckinObject();
			//request a check-in location from service
//			startService(new Intent(mContext, LocationService.class).setAction("ACTION_GET_CHECKIN_LOCATION"));
			startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_GET_CHECKIN_LOCATION"));
		}
		/**
		 * **check-in method family**
		 * confirmed check-in action, pass the candidate check-in object to location service to be saved
		 */
		public void endCheckin(){
			//send cco to service via startService call with action and extras
//			startService(new Intent(mContext, LocationService.class).setAction("ACTION_SAVE_CCO").putExtra("cco", cco));
			startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_SAVE_CCO").putExtra("cco", cco));
		}
		/**
		 * **check-in method family**
		 * check-in action is canceled, drop the candidate check-in object
		 */
		public void cancelCheckin(){
			cco = null;
//			startService(new Intent(mContext, LocationService.class).setAction("ACTION_CANCEL_CHECKIN"));
			startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_CANCEL_CHECKIN"));
		}
		
		/**
		 * Replace cookie function in html, save the key-value pair to android preference
		 * @param key
		 * @param value
		 */
		public void setCookie(String key, String value){
			pref.edit().putString(key, String.valueOf(value)).commit();
			Log.e("setCookie", "key= " + key + ", value= " + String.valueOf(value));
		}
		
		/**
		 * 
		 * @param key
		 * @return the value paired with the key, or null if key does not exist
		 */
		public String getCookie(String key){
			Log.e("getCookie", "key= " + key + ", value= " + pref.getString(key, null));
			return pref.getString(key, null);
		}
		
		/**
		 * remove the key entry from preference
		 * @param key
		 */
		public void removeCookie(String key){
			pref.edit().remove(key).commit();
			Log.e("removeCookie", "key= " + key);
		}
		
		public String getLocale(){
//			Log.e("locale/getDisplayLanguage", Locale.getDefault().getDisplayLanguage());
//			Log.e("locale/getCountry", Locale.getDefault().getCountry());
//			Log.e("locale/getDisplayCountry", Locale.getDefault().getDisplayCountry());
//			Log.e("locale/getDisplayName", Locale.getDefault().getDisplayName());
//			Log.e("locale/getDisplayVariant", Locale.getDefault().getDisplayVariant());
//			Log.e("locale/getISO3Country", Locale.getDefault().getISO3Country());
//			Log.e("locale/getISO3Language", Locale.getDefault().getISO3Language());
//			Log.e("locale/getLanguage", Locale.getDefault().getLanguage());
//			Log.e("locale/getVariant", Locale.getDefault().getVariant());
			return Locale.getDefault().getLanguage();
		}
		
		//TODO auto upload, no user interaction involved
//		public void uploadTrip(String tripid){
//			startService(new Intent(mContext, UploadService.class)
//			.setAction("ACTION_UPLOAD_TRIP")
//			.putExtra("tripid", tripid)
//			.putExtra("userid", pref.getString("sid", null)));
//		}
	}
	
	/**
	 * to prevent frequent calls to the mWebView.loadURL() method, all calls are queued and executed every 400ms
	 * @param url
	 */
	private void queuedLoadURL(String url){
		urlQueue.offer(url);
		//if already running, don't post again
		if(canPostAgain){
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					//now we're running, don't accept any more post calls
					canPostAgain = false;
					//get a url from queue
					String url = urlQueue.poll();
					Log.d("queuedLoadURL", "url= " + url);
					mWebView.loadUrl(url);
					//check if the queue is empty or not
					if(urlQueue.peek() != null){
						//queue not empty, post self with delay
						mHandler.postDelayed(this, 400);
					} else{
						//queue empty, now accepting post calls
						canPostAgain = true;
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
		// only notify service to start broadcasting if it is running, either isrec = true, or we're in mode 3
		String isrec = pref.getString("isRecording", null);
		if(isrec != null && isrec.equals("true") || (currentMode != null && currentMode == 3)){
//			startService(new Intent(mContext, LocationService.class).setAction("ACTION_ACTIVITY_IS_READY_FOR_BROADCAST"));
			startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_ACTIVITY_IS_READY_FOR_BROADCAST"));
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//save to pref that activity is NOT ready for broadcast
		String isrec = pref.getString("isRecording", null);
		//only 2 cases where service is running, isrec = true, or we're in mode 3
		if(isrec != null && isrec.equals("true") || (currentMode != null && currentMode == 3)){
//			startService(new Intent(mContext, LocationService.class).setAction("ACTION_ACTIVITY_NOT_READY_FOR_BROADCAST"));
			startService(new Intent(mContext, LocationServiceGPS.class).setAction("ACTION_ACTIVITY_NOT_READY_FOR_BROADCAST"));
		}
		// need to notify location service not to send location update
		unregisterReceiver(br);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CODE_TAKE_PICTURE){
			switch(resultCode){
			case RESULT_OK:
				//need to grab the generated filename plus filepath and return it to html for display purpose
				String imageURL = "javascript:showPicture('" + imageUri.getPath() + "')";
//				mWebView.loadUrl(imageURL);
				queuedLoadURL(imageURL);
Log.e("onActivityResult", "imageURL= " + imageURL);
				if(cco!=null){
					cco.setPicturePath(imageUri.getPath());
				}
				break;
			case RESULT_CANCELED:
				//decides to not take a picture after all
//				break;
			case RESULT_FIRST_USER:
				//not sure when will this method be called
//				break;
			default:
				//handle all exceptions
				String noImageURL = "javascript:showPicture(-1)";
//				mWebView.loadUrl(noImageURL);
				queuedLoadURL(noImageURL);
				break;
			}
		}
	}
}