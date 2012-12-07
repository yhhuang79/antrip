package tw.plash.antrip.offline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GetLocalTrip {
	
	private Context mContext;
	private JSONObject result;
	private DBHelper dh;
	private SharedPreferences pref;
	private String uid;
	
	public GetLocalTrip(Context c) {
		mContext = c;
		result = new JSONObject();
		dh = new DBHelper(c);
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		uid = pref.getString("sid", null);
	}
	
	public JSONObject Info() {
		if (uid != null) {
			//query all trip infos with the input uid
			result = dh.getAllTripInfoForHTML(uid, pref.getString("trip_id", null));
			if(dh.DBIsOpen()){
				dh.closeDB();
			}
			return result;
		} else {
			return null;
		}
	}
	
	public JSONObject Data(Integer id){
		if(uid != null){
			result = dh.getOneTripData(uid, id);
			if(dh.DBIsOpen()){
				dh.closeDB();
			}
			return result;
		} else{
			return null;
		}
	}
}
