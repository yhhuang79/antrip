package tw.plash.antrip;

import java.io.File;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class ANTripActivity extends Activity {
	private Context mContext;
	private WebView mWebView;
	
	private Integer previousMode;
	
	private CandidateCheckinObject cco;
	
	private Uri imageUri;
	private final int REQUEST_CODE_TAKE_PICTURE = 100;
	
	private SharedPreferences pref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setContentView(R.layout.main);
		
		mContext = this;
		
		mWebView = (WebView) findViewById(R.id.webview);
		
		previousMode = null;
		
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
						.setNeutralButton("clicky", new OnClickListener() {
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
		mWebView.loadUrl("file:///android_asset/index.html");
	}
	
	public interface JavaScriptCallback{}
	
	private class jsinter implements JavaScriptCallback{
		
		private final String imagepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
		
		
		public void hello(String sid){
			Toast.makeText(mContext, "hola~ " + sid, Toast.LENGTH_LONG).show();
		}
		
		public void logout(){
			//remove sid and stop stuffs
			Log.e("logged", "out");
			pref.edit().remove("sid").commit();
		}
		
		public String getSid(){
			return pref.getString("sid", null);
		}
		
		//save userid from a successful login responce message
		public void saveSid(String sid){
			pref.edit().putString("sid", sid).commit();
			Log.e("saveSid", "= " + sid);
		}
		
		// need a function to provide trip list(both local the server side)
		
		// need a function to store check-in values
		
		// need a function to provide detailed trip data(reviewing historic trip)
		public String getLocalTripList(){
			Toast.makeText(mContext, "getLocalTripList", Toast.LENGTH_SHORT).show();
			JSONObject result = null;
			
			result = new GetLocalTripList(mContext).execute();
			
			//null means no local trip history, don't return null, return -1 instead
			if(result != null){
				return result.toString();
			} else{
				return "-1";
			}
		}
		
		
		
		
		/**
		 * start recording, and return the newly generated random tripid
		 * @return
		 */
		public int startRecording(){
			
			return -1;
		}
		
		/**
		 * 
		 */
		public void stopRecording(){
			
		}
		
		/*
		 * 1: home screen
		 * 2: trip list screen
		 * 3: recorder screen
		 * 4: friend list screen
		 */
		public void setMode(int mode){
			
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
Log.e("startcamera", "imageUri= \"" + imageUri.getPath()+"\"");
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
				cco.setEmotionID(id);
			}
		}
		/**
		 * **check-in method family**
		 * save the inputted text to candidate check-in object
		 */
		public void setText(String text){
			if(cco != null){
				cco.setCheckinText(text);
			}
		}
		/**
		 * **check-in method family**
		 * create a new candidate check-in object
		 * also request a coordinate from location service
		 */
		public void startCheckin(){
			cco = new CandidateCheckinObject();
		}
		/**
		 * **check-in method family**
		 * confirmed check-in action, pass the candidate check-in object to location service to be saved
		 */
		public void endCheckin(){
			//send cco to service via startService call with action and extras
			startService(new Intent(mContext, LocationService.class).setAction("ACTION_SAVE_CCO").putExtra("cco", cco));
		}
		/**
		 * **check-in method family**
		 * check-in action is canceled, drop the candidate check-in object
		 */
		public void cancelCheckin(){
			cco = null;
		}
		
		/**
		 * Replace cookie function in html, save the key-value pair to android preference
		 * @param key
		 * @param value
		 */
		public void setCookie(String key, String value){
			pref.edit().putString(key, value).commit();
			Log.e("setCookie", "key= " + key + ", value= " + value);
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
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// need to notify location service to send location update
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// need to notify location service not to send location update
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CODE_TAKE_PICTURE){
			switch(resultCode){
			case RESULT_OK:
				//need to grab the generated filename plus filepath and return it to html for display purpose
				String imageURL = "javascript:showPicture('" + imageUri.getPath() + "')";
				mWebView.loadUrl(imageURL);
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
				mWebView.loadUrl(noImageURL);
				break;
			}
		}
	}
}