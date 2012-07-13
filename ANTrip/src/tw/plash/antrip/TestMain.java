package tw.plash.antrip;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class TestMain extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		PreferenceManager.getDefaultSharedPreferences(this).edit().putString("sid", "154").commit();
		JSONObject result = new GetTripList(this).execute();
		
		Log.e("result", "= " + result.toString());
	}
}
