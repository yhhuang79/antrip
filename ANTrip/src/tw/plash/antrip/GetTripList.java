package tw.plash.antrip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class GetTripList {

	private Context mContext;
	private JSONObject result;

	public GetTripList(Context c) {
		mContext = c;
		result = new JSONObject();
	}

	public JSONObject execute() {

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String uid = pref.getString("sid", null);
		if (uid != null) {
			try {
				String url = "http://plash2.iis.sinica.edu.tw/antrip/lib/php/GetTripInfoComponent.php?userid="
						+ uid;
				Log.e("url", "= " + url);
				HttpClient client = new DefaultHttpClient();
//				client.getParams().setIntParameter(
//						HttpConnectionParams.CONNECTION_TIMEOUT, 5000);
//				client.getParams().setIntParameter(
//						HttpConnectionParams.SO_TIMEOUT, 5000);
				HttpGet request = new HttpGet(url);
				HttpResponse response = client.execute(request);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String rawResult = in.readLine();
				// need to trim result because of the excess "(" at the beginning
				// and the ");" at the end, remove these and JSONTokner can
				// parse the result correctly
				if(rawResult.isEmpty()){
					// do not proceed if there's no result
					// also just return the empty jsonobject
					return result;
				}
				// if there's at least one result, trim the excess stuff for jsontokner
				String trimResult = rawResult.substring(1, rawResult.length()-2);
				Log.e("raw", trimResult);
//				Log.e("gettriplist", "result=" + result.toString());
				result = new JSONObject(new JSONTokener(trimResult));
//				Log.e("gettriplist", "result=" + result.toString());
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		} else {
			return null;
		}
	}
}
