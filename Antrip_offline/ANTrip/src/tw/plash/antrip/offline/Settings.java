package tw.plash.antrip.offline;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.antrip_settings);
		Preference usrname = findPreference("usrname");
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		usrname.setTitle(getResources().getString(R.string.login_title) + pref.getString("usrname", "NULL"));
		usrname.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(AntripService2.isRecording()){
					//ask if user wants to logout even a trip is still recording
					new AlertDialog.Builder(Settings.this)
						.setMessage(R.string.logout_warning)
						.setNegativeButton(R.string.nope, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//
//								setResult(RESULT_CANCELED);
//								finish();
							}
						})
						.setPositiveButton(R.string.okay, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//call logout function
								setResult(RESULT_OK);
								finish();
							}
						})
						.show();
				} else{
					//call logout function
					setResult(RESULT_OK);
					finish();
				}
				return true;
			}
		});
	}
}
