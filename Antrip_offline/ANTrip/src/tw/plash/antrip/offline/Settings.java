package tw.plash.antrip.offline;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings extends PreferenceActivity{
	
	private Context mContext;
	private SharedPreferences pref;
	
	private Preference username;
	private Preference recorder_mode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.antrip_settings);
		mContext = this;
		
		Preference version = findPreference("version");
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
				String appname = "tw.plash.antrip";
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
		}
	};
}
