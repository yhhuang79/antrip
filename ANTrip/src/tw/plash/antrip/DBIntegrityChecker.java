package tw.plash.antrip;

import java.util.HashSet;

import android.content.Context;
import android.preference.PreferenceManager;

public class DBIntegrityChecker {
	
	private Long currentTid;
	private String sid;
	private DBHelper128 dh;
	private Context mContext;
	
	public DBIntegrityChecker(Context context) {
		currentTid = null;
		dh = null;
		mContext = context;
		sid = PreferenceManager.getDefaultSharedPreferences(mContext).getString("sid", "-1");
	}
	
	public void run(){
		//make sure user already logged in, and userid is saved in shared preference
		if(!sid.equalsIgnoreCase("-1")){
			//if a trip is currently recording, exclude the current trip id in DB scan
			if(AntripService.isRecording()){
				//if service is recording, get the current trip id to avoid generating (possibly duplicating)current trip info
				currentTid = AntripService.getCurrentTid();
			}
			dh = new DBHelper128(mContext);
			
			//get a summary of trip data
			//get all trip IDs in trip data table
			HashSet<String> data = dh.getTripIdsFromTripData(sid, currentTid.toString());
			//get all trip IDs in trip info table
			HashSet<String> info = dh.getTripIdsFromTripInfo(sid, currentTid.toString());
			
			//compare trip data summary with trip info
			if(data != null && info != null){
				//remove all trip id of trip info table from trip data
				data.removeAll(info);
				//the residual trip ids are the ones we need to update
				HashSet<String> residual = data;
				
				//resolve inconsistency, if there is any to be resolved
				if(residual != null && !residual.isEmpty()){
					for(String item: residual){
						
					}
				}
			}
			
			
			
			//all done now, closes DB
			dh.closeDB();
			dh = null;
		}
	}
}
