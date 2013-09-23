package tw.plash.antrip.offline.trip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class ShareTripListLoader extends AsyncTaskLoader<List<String>> {
	
	List<String> trips;
	
	public ShareTripListLoader(Context context) {
		super(context);
	}
	
	@Override
	public List<String> loadInBackground() {
		Log.e("loader", "start loading");
		trips = new ArrayList<String>();
		
		HttpURLConnection conn = null;
		
		try {
			
			URL url = new URL("http://192.168.56.102/getTrips.php?userid=123");
			
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(5000);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String readInputLine = null;
			readInputLine = in.readLine();
			
			JSONObject result = new JSONObject(new JSONTokener(readInputLine));
			in.close();
			
			int code = result.getInt("status code");
			if (code == 200) {
				JSONArray data = result.getJSONArray("query result");
				for (int i = 0; i < data.length(); i++) {
					JSONObject obj = data.getJSONObject(i);
					String name = obj.getString("tripname");
					String time = obj.getString("starttime");
					trips.add(name + "\n" + time);
				}
				Log.e("loader", "got data");
				return trips;
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		
		Log.e("loader", "error");
		return null;
	}
	
	@Override
	public void deliverResult(List<String> data) {
		if (isStarted()) {
			super.deliverResult(data);
		}
	}
	
	@Override
	protected void onStartLoading() {
		if (trips != null) {
			deliverResult(trips);
		}
		if (takeContentChanged() || trips == null) {
			forceLoad();
		}
	}
	
	@Override
	protected void onStopLoading() {
		cancelLoad();
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		
		onStopLoading();
		
		if (trips != null) {
			trips = null;
		}
	}
}
