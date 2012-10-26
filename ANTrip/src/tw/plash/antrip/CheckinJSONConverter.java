package tw.plash.antrip;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.util.Log;

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
				Log.e("CheckinJSONConverter", "fromQtoCheckinJSON result: " + result.toString());
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
				Log.e("CheckinJSONConverter", "fromLocationtoCheckinJSON result: " + result.toString());
				return result;
			}
		}
		return null;
	}
	
	static public JSONObject fromCCOtoCheckinJSON(CandidateCheckinObject cco){
		if(cco != null){
			if(!cco.getLocation().getProvider().isEmpty() && !cco.getLocation().getProvider().equalsIgnoreCase("null")){
				JSONObject result = new JSONObject();
				try {
					JSONArray array = new JSONArray();
					JSONObject tmp = new JSONObject();
					tmp.put("lat", cco.getLocation().getLatitude());
					tmp.put("lng", cco.getLocation().getLongitude());
					tmp.put("timestamp", cco.getLocation().getTime());
					JSONObject checkin = new JSONObject();
					if(cco.getCheckinText() != null){
						checkin.put("message", cco.getCheckinText());
					}
					if(cco.getEmotionID() != null){
						checkin.put("emotion", cco.getEmotionID());
					}
					if(cco.getPicturePath() != null){
						checkin.put("picture_uri", cco.getPicturePath());
					}
					tmp.put("CheckIn", checkin);
					array.put(tmp);
					result.put("CheckInDataList", array);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Log.e("CheckinJSONConverter", "fromLocationtoCheckinJSON result: " + result.toString());
				return result;
			}
		}
		return null;
	}
}
