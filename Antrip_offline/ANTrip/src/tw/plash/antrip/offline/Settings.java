package tw.plash.antrip.offline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tw.plash.antrip.offline.LoginDialog;
import tw.plash.antrip.offline.connection.InternetUtility;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;

public class Settings extends PreferenceActivity{
	
	private Context mContext;
	private SharedPreferences pref;
	
	private Preference username;
	private Preference recorder_mode;
	private Preference autostop;
	
	// Facebook Login Button
	private LoginButton authButton;
	private GraphUser user;
	private UiLifecycleHelper uiHelper;
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            //onSessionStateChange(session, state, exception);
        }
    };

    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
        }

        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.d("HelloFacebook", "Success!");
        }
    };
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.antrip_settings);
		setContentView(R.layout.setting);
		mContext = this;
		Preference version = findPreference("version");
		// Facebook Login Button
		authButton = (LoginButton) findViewById(R.id.authButton);
		// Set Facebook Permissions
		authButton.setReadPermissions(Arrays.asList("basic_info","email"));
		//authButton.setPublishPermissions("publish_actions");
		authButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                Settings.this.user = user;
                if(user != null){
                	Log.d("Facebook id", user.getId());
                	Log.d("Facebook name", user.getName());
                	Log.d("Facebook email", (String)user.getProperty("email"));               	
                	//pref.edit().putString("sid", user.getId()).putString("username", user.getName()).commit();
                	fblogin = new fblogin(user.getName().toString(), user.getId().toString(), user.getProperty("email").toString());
                	fblogin.execute();
                } else {
                	Log.d("Facebook no login", "No Login");
					pref.edit().remove("userid").remove("username").commit();                	
                }
            }
        });
		
		try {
			version.setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			version.setSummary("-");
		}
//		version.setSummary(R.string.eowversion);
		
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		pref.registerOnSharedPreferenceChangeListener(ospcl);
		
		username = findPreference("username");
		
		String userid = pref.getString("userid", "-1");
		if(userid.equals("-1")){
			username.setTitle(R.string.setting_title_notlogin);
			username.setSummary(R.string.setting_summary_login);
		} else{
			username.setTitle(getResources().getString(R.string.setting_title_youare) + pref.getString("username", "name"));
			username.setSummary(R.string.setting_summary_logout);
		}
		
		username.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(!pref.getString("userid", "-1").equals("-1")){
					//already logged in, log the user out
					pref.edit().remove("userid").remove("username").commit();
				} else{
					//not logged in yet, should prompt login screen
					Dialog dialog = new LoginDialog(mContext, 0);
					dialog.show();
				}
				return true;
			}
		});
		
		recorder_mode = findPreference("recorder_mode");
		String[] sum = mContext.getResources().getStringArray(R.array.setting_recorder_mode);
		String mode = pref.getString("recorder_mode", "-");
		if(mode.contains("0")){
			recorder_mode.setSummary(sum[0]);
		} else if(mode.contains("1")){
			recorder_mode.setSummary(sum[1]);
		} else if(mode.contains("2")){
			recorder_mode.setSummary(sum[2]);
		} else{
			recorder_mode.setSummary("-");
		}
		
		autostop = findPreference("auto_stop_threshold");
		String autostopSumPrefix = mContext.getString(R.string.setting_title_autostop_summary_prefix);
		String autostopSumPostfix = mContext.getString(R.string.setting_title_autostop_summary_postfix);
		String level = pref.getString("auto_stop_threshold", "-");
		autostop.setSummary(autostopSumPrefix + " " + level + " " + autostopSumPostfix);
		
		findPreference("license").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.plash.tw/html/antrip.html")));
				return true;
			}
		});
		
		findPreference("contact").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ApplicationInfo ai = null;
				try {
					ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), 0);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
//				String appname = (ai != null)?mContext.getPackageManager().getApplicationLabel(ai).toString():"a";
				String appname = "tw.plash.antrip.offline";
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appname)));
				} catch (android.content.ActivityNotFoundException anfe) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+appname)));
				}
				return true;
			}
		});

				
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		pref.unregisterOnSharedPreferenceChangeListener(ospcl);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
    }

	private interface GraphObjectWithId extends GraphObject {
        String getId();
    }
	
	final private OnSharedPreferenceChangeListener ospcl = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//			Log.e("pref changed", "key: " + key + ", value: " + sharedPreferences.getString(key, "999"));
			if(key.equals("userid")){
				if(sharedPreferences.getString(key, "-1").equals("-1")){
					//logged out
					username.setTitle(R.string.setting_title_notlogin);
					username.setSummary(R.string.setting_summary_login);
					setResult(RESULT_CANCELED);
				} else{
					//logged in
					username.setTitle(getResources().getString(R.string.setting_title_youare) + sharedPreferences.getString("username", "name"));
					username.setSummary(R.string.setting_summary_logout);
					setResult(RESULT_OK);
				}
				
			}
			if(key.equals("recorder_mode")){
				String[] sum = mContext.getResources().getStringArray(R.array.setting_recorder_mode);
				String mode = sharedPreferences.getString("recorder_mode", "-");
				if(mode.contains("0")){
					recorder_mode.setSummary(sum[0]);
				} else if(mode.contains("1")){
					recorder_mode.setSummary(sum[1]);
				} else if(mode.contains("2")){
					recorder_mode.setSummary(sum[2]);
				} else{
					recorder_mode.setSummary("-");
				}
			}
			if(key.equals("auto_stop_threshold")){
				String autostopSumPrefix = mContext.getString(R.string.setting_title_autostop_summary_prefix);
				String autostopSumPostfix = mContext.getString(R.string.setting_title_autostop_summary_postfix);
				String level = sharedPreferences.getString("auto_stop_threshold", "-");
				autostop.setSummary(autostopSumPrefix + " " + level + " " + autostopSumPostfix);
			}
		}
	};
	
	
	
	private fblogin fblogin;
	private class fblogin extends AsyncTask<Void, Void, Integer>{
		
		private ProgressDialog diag;
		
		final private String uname;
		final private String facebookid;
		final private String email;
		
		public fblogin(String uname, String facebookid, String email) {
			this.uname = uname;
			this.facebookid = facebookid;
			this.email = email;
		}
		
		@Override
		protected void onPreExecute() {
			diag = new ProgressDialog(mContext);
			diag.setCancelable(true);
			diag.setCanceledOnTouchOutside(false);
			diag.setIndeterminate(true);
			diag.setMessage(mContext.getResources().getString(R.string.progressdialog_logginin));
			diag.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if(fblogin != null){
						fblogin.cancel(true);
					}
				}
			});
			diag.show();
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			
				//String url = "http://plash2.iis.sinica.edu.tw/api/AntripFBLogin.php?username=" + InternetUtility.encode(uname) + "&facebookid=" + InternetUtility.encode(facebookid) + "&email=" + InternetUtility.encode(email);
			String url = "http://plash2.iis.sinica.edu.tw/api/AntripFBLogin.php?facebookid=" + facebookid + "&email=" + email;

			//				String url = "http://plash2.iis.sinica.edu.tw/api/login.php?username=xsirh82&password=44b91147489c44542d1e17b141bd5c4d";
				Log.d("FB Url", url);
				HttpGet getRequest = new HttpGet(url);
				
				HttpParams httpParameters = new BasicHttpParams();
				// Set the timeout in milliseconds until a connection is established. 
				// The default value is zero, that means the timeout is not used.
				HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
				// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
				int timeoutSocket = 10000;
				HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
				
				DefaultHttpClient client = new DefaultHttpClient(httpParameters);
				try{
				HttpResponse response = client.execute(getRequest);
				
				Integer statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					
					BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					//Log.d("FBLogin", in.readLine().toString());
					JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
					in.close();
					if(result.getString("sid").equals("0")){
						//login failed
						return -3;
					} else{
						//otherwise assume login is good
						pref.edit().putString("userid", result.getString("sid")).putString("username", uname).commit();
						return 0;
					}
				} else {
					
					
					// connection error
					return -2;
				}
			} catch(IOException e){
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} finally{
				client.getConnectionManager().shutdown();
			}
			return -1;
		}
		
		@Override
		protected void onCancelled() {
			Toast.makeText(mContext, R.string.toast_login_canceled, Toast.LENGTH_LONG).show();
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			diag.dismiss();
			switch(result){
			case 0:
				Toast.makeText(mContext, R.string.toast_login_success, Toast.LENGTH_LONG).show();
				//LoginDialog.this.dismiss();
				break;
			case -1:
			case -2:
			case -3:
			default:
				Toast.makeText(mContext, R.string.toast_login_failed, Toast.LENGTH_LONG).show();
				break;
			
			}
		}
	}
	
}
