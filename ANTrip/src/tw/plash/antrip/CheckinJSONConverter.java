package tw.plash.antrip;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

public class CheckinJSONConverter {
	
	static public JSONObject fromQtoCheckinJSON(LinkedBlockingQueue<Location> q){
		if(q != null){
			if(!q.isEmpty()){
				JSONObject result = new JSONObject();
				try {
					JSONArray array = new JSONArray();
					Iterator<Location> ite = q.iterator();
					do {
						Location loc = ite.next();
						JSONObject tmp = new JSONObject();
						tmp.put("lat", loc.getLatitude());
						tmp.put("lng", loc.getLongitude());
						tmp.put("timestamp", loc.getTime());
						array.put(tmp);
					} while (ite.hasNext());
					result.put("CheckInDataList", array);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return result;
			}
		}
		return null;
	}
	
	static public JSONObject fromLocationtoCheckinJSON(Location loc){
		if(loc != null){
			if(!loc.getProvider().isEmpty() && !loc.getProvider().equalsIgnoreCase("null")){
				JSONObject result = new JSONObject();
				try {
					JSONArray array = new JSONArray();
					JSONObject tmp = new JSONObject();
					tmp.put("lat", loc.getLatitude());
					tmp.put("lng", loc.getLongitude());
					tmp.put("timestamp", loc.getTime());
					array.put(tmp);
					result.put("CheckInDataList", array);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return result;
			}
		}
		return null;
	}
}
