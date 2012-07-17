package tw.plash.antrip;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GetLocalTripList {

	private Context mContext;
	private JSONObject result;

	public GetLocalTripList(Context c) {
		mContext = c;
		result = new JSONObject();
	}
	
	public JSONObject execute() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		String uid = pref.getString("sid", null);
		if (uid != null) {
			
			return result;
		} else {
			//don't return null, handle the error in Java not in JavaScript
			return new JSONObject();
		}
	}
}
