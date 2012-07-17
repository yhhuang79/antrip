package tw.plash.antrip;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class ANTripActivity extends Activity {
	private Context mContext;
	private WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		setContentView(R.layout.main);
		
		mContext = this;
		
		mWebView = (WebView) findViewById(R.id.webview);
		WebSettings mWebSettings = mWebView.getSettings();
		mWebSettings.setJavaScriptEnabled(true);
//		mWebSettings.setDomStorageEnabled(true);
		mWebView.setWebViewClient(new WebViewClient());
		mWebView.setWebChromeClient(new WebChromeClient(){
			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
				Log.e("consolemsg", consoleMessage.message() + ", " + consoleMessage.lineNumber() + ", " + consoleMessage.sourceId());
				return true;
			}
		});
//		mWebView.addJavascriptInterface(new JSInterface(), "aa");
		mWebView.addJavascriptInterface(new jsinter(), "antrip");
		mWebView.loadUrl("file:///android_asset/index.html");
		
		/* mWebView.setWebViewClient(new WebViewClient(){
			boolean firstload = true;
            @Override
            public void onPageFinished(WebView view, String url) {
            	if(firstload){
	            	if(url.contains("#map") || url.contains("#friendlist") || url.contains("#triplist")){
	            		mWebView.reload();
	            		firstload = false;
	            	}
            	}
                super.onPageFinished(view, url);
           }  
		}); */
		
		
	}
	
	public interface JavaScriptCallback{}
	
	private class jsinter implements JavaScriptCallback{
		public void hello(String sid){
			Toast.makeText(mContext, "hola~ " + sid, Toast.LENGTH_LONG).show();
		}
		
		public void logout(){
			//remove sid and stop stuffs
			Log.e("logged", "out");
//			PreferenceManager
		}
		
		public String getSid(){
			return PreferenceManager.getDefaultSharedPreferences(mContext).getString("sid", null);
		}
		
		//save userid from a successful login responce message
		public void saveSid(String sid){
			PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("sid", sid).commit();
			Log.e("saveSid", "= " + sid);
		}
		
		// need a function to provide trip list(both local the server side)
		
		// need a function to store check-in values
		
		// need a function to provide detailed trip data(reviewing historic trip)
		public String getLocalTripList(){
			JSONObject result = null;
			Toast.makeText(mContext, "start gettriplist", Toast.LENGTH_SHORT).show();
			result = new GetLocalTripList(mContext).execute();
			Toast.makeText(mContext, "done gettriplist", Toast.LENGTH_SHORT).show();
			
			Log.e("gettriplist", result.toString());
			//result is never null, can return safely
			return result.toString();
		}
		
		
		public void startCamera(){
			startActivityForResult((new Intent(mContext, test21.class).putExtra("requestCode", 1)), 1);
		}
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// need to notify location service to send location update
		
		
		
	}
	
	@Override
	protected void onPause() {
		// need to notify location service not to send location update
		
		
		
		super.onPause();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(resultCode){
		case 1:
			String imageLocation = data.getExtras().getString("photoLocation");
			Log.e("on act result", "at " + imageLocation);
			String imageURL = "javascript:showPicture('" + imageLocation + "')";
			mWebView.loadUrl(imageURL);
			//mWebView.loadUrl("http://plash2.iis.sinica.edu.tw/antrip/index.html#map&ui-state=dialog");
			break;
		default:
			String noImageURL = "javascript:showPicture(-1)";
			mWebView.loadUrl(noImageURL);
			break;
		}
		Log.e("on act result", "done");
	}
	
	
}