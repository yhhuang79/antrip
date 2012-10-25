package tw.plash.antrip;

import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

public class AntripService extends Service implements LocationPublisher{
	
	private static boolean isRunning;
	private static boolean isStarted;
	private static boolean isRecording;
	
	private LinkedBlockingQueue<Location> locationQueue;
	
	private DBHelper128 dh;
	
	private Long currentTid;
	private String currentUid;
	private SharedPreferences pref;
	private TripStats stats;
	
	//make sure both stop_recording_pre and stop_recording_actual are called
	//if the pre-actual action is incomplete, will save default values to DB in onDestroy
	private boolean stopRecordingActionUnfinished;
	
	//these static fields should be put together in a separate class
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_INIT_LOCATION_THREAD = 3;
	static final int MSG_STOP_LOCATION_THREAD = 4;
	static final int MSG_START_RECORDING = 5;
	static final int MSG_PAUSE_RECORDING = 6;
	static final int MSG_STOP_RECORDING_PRE = 7;
	static final int MSG_STOP_RECORDING_ACTUAL = 13;
	static final int MSG_START_UPLOAD = 8;
	static final int MSG_GET_CHECKIN_LOCATION = 9;
	static final int MSG_SAVE_CHECKIN_LOCATION = 10;
	static final int MSG_SET_USERID = 11;
	static final int MSG_LOGOUT_EVENT = 12;
	
	static final int MSG_LOCATION_UPDATE = 20;
//	static final int MSG_LOCATION_UPDATE_NULL = 21;
	
	static final int MSG_LOCATION_UPDATE_CHECKIN = 22;
	
	private SkyhookLocation skyhookLocation;
	
	private Messenger outMessager = null;
	final private Messenger inMessenger = new Messenger(new IncomingHandler());
	
	private class IncomingHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_REGISTER_CLIENT:
				Log.i("AntripService", "IncomingHandler: case-MSG_REGISTER_CLIENT");
				//save the client info, so we can send messages back later
				outMessager = msg.replyTo;
				break;
			case MSG_UNREGISTER_CLIENT:
				Log.i("AntripService", "IncomingHandler: case-MSG_UNREGISTER_CLIENT");
				//if not recording but skyhook location is running, stop it
				if(!isRecording && skyhookLocation != null){
					skyhookLocation.cancel();
					skyhookLocation = null;
				}
				//if the client unregisters, we nullify the messenger
				if(outMessager == msg.replyTo){
					outMessager = null;
				}
				break;
			case MSG_INIT_LOCATION_THREAD:
				Log.i("AntripService", "IncomingHandler: case-MSG_INIT_LOCATION_THREAD");
				if(skyhookLocation != null){
					//ignore this call, skyhookLocation is already running
					Log.w("AntripService", "IncomingHandler: init location thread called when skyhooklocation!=null");
				} else{
					skyhookLocation = new SkyhookLocation(getApplicationContext(), AntripService.this);
					skyhookLocation.run(10);
				}
				break;
			case MSG_STOP_LOCATION_THREAD:
				Log.i("AntripService", "IncomingHandler: case-MSG_STOP_LOCATION_THREAD");
				if(isRecording){
					//ignore this call because a trip is recording
					Log.w("AntripService", "IncomingHandler: stop location thread called when isrecording=true");
				} else{
					if(skyhookLocation != null){
						skyhookLocation.cancel();
						skyhookLocation = null;
					} else{
						Log.e("AntripService", "IncomingHandler: msg_stop_location_thread error");
					}
				}
				break;
			case MSG_STOP_RECORDING_PRE:
				Log.i("AntripService", "IncomingHandler: case-MSG_STOP_RECORDING_PRE");
				//close the DB here, if trip name needs to be saved, re-open it
				if(dh != null){
					//need to finalize trip stats
					isRecording = false;
					dh.saveTripStats(currentUid, currentTid.toString(), stats);
					dh.closeDB();
					dh = null;
				} else{
					//dh is null, no trip is recording, who called this?
					Log.e("AntripService", "IncomingHandler: stop recording called when dh is null");
				}
				break;
			case MSG_STOP_RECORDING_ACTUAL:
				Log.i("AntripService", "IncomingHandler: case-MSG_STOP_RECORDING_ACTUAL");
				if(msg.obj instanceof String){
					if(dh != null){
						//do nothing, but seriously this should not happen
					} else{
						dh = new DBHelper128(getApplicationContext());
					}
					dh.setTripName(currentUid, currentTid.toString(), (String)msg.obj);
					dh.closeDB();
					dh = null;
				}
				break;
			case MSG_GET_CHECKIN_LOCATION:
				Log.i("AntripService", "IncomingHandler: case-MSG_GET_CHECKIN_LOCATION");
				//activity requesting a loaction for check-in points, send a location back
				sendMessageToUI(MSG_LOCATION_UPDATE_CHECKIN, skyhookLocation.getLastNonNullLocation());
				break;
			case MSG_SAVE_CHECKIN_LOCATION:
				Log.i("AntripService", "IncomingHandler: case-MSG_SAVE_CHECKIN_LOCATION");
				if(dh != null){
					if(msg.obj instanceof CandidateCheckinObject){
						dh.insert((CandidateCheckinObject)msg.obj, currentUid, currentTid.toString());
					} else{
						Log.e("AntripService", "IncomingHandler: save cco called without cco object as payload");
					}
				} else{
					Log.e("AntripService", "IncomingHandler: save cco called when dh is null/DB is closed");
				}
				break;
			case MSG_START_UPLOAD:
				Log.i("AntripService", "IncomingHandler: case-MSG_START_UPLOAD");
				//XXX
				break;
			case MSG_SET_USERID:
				Log.i("AntripService", "IncomingHandler: case-MSG_SET_USERID");
				if(msg.obj instanceof String){
					currentUid = (String) msg.obj;
				} else{
					Log.e("AntripService", "IncomingHandler: set userid called without proper userid string");
				}
				break;
			default:
				Log.i("AntripService", "IncomingHandler: case-default");
				super.handleMessage(msg);
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		
		return inMessenger.getBinder();
	}
	
	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		if(isRecording){
			//i think this only happens when you are doing check-in...
			if(locationQueue != null && !locationQueue.isEmpty()){
				sendMessageToUI(MSG_LOCATION_UPDATE, CheckinJSONConverter.fromQtoCheckinJSON(locationQueue));
			}
		}
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		
		return true; //return true to use "onRebind"
	}
	
	public void sendMessageToUI(int msgType, Object payload) {
		try {
			// activity binded, leMessager can deliver message
			switch (msgType) {
			case MSG_LOCATION_UPDATE_CHECKIN:
				if (outMessager != null) {
					outMessager.send(Message.obtain(null, MSG_LOCATION_UPDATE_CHECKIN, payload));
				}
				break;
			case MSG_LOCATION_UPDATE:
				if(isRecording){
					if(outMessager != null){
						//convert to yu-hsiang styled check-in json string before sending
						if(payload instanceof JSONObject){
							outMessager.send(Message.obtain(null, MSG_LOCATION_UPDATE, ((JSONObject)payload).toString()));
						} else if(payload instanceof Location){
							outMessager.send(Message.obtain(null, MSG_LOCATION_UPDATE, CheckinJSONConverter.fromLocationtoCheckinJSON((Location)payload).toString()));
						} else{
							//should not happen
						}
					} else{
						//save all the location update during check-in as-is
						if(locationQueue != null){
							locationQueue.offer((Location)payload);
						}
					}
				} else{
					if(outMessager != null){
						if(payload instanceof Location){
							outMessager.send(Message.obtain(null, MSG_LOCATION_UPDATE, payload));
						} else{
							//what is going on
						}
					}
				}
				break;
			default:
				break;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		currentUid = pref.getString("sid", null);
		
		currentTid = null;
		
		skyhookLocation = null;
		
		dh = null;
		
		stats = null;
		
		locationQueue = new LinkedBlockingQueue<Location>();
		
		stopRecordingActionUnfinished = false;
		
		isRunning = true;
		isRecording = false;
		isStarted = false;
	}
	
	public static boolean isRunning(){
		return isRunning;
	}
	
	public static boolean isStarted(){
		return isStarted;
	}
	
	public static boolean isRecording(){
		return isRecording;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//this function might be called when Android system kills service for fun
		//when calling from activity, need to pass an action string, so service can tell who's the caller
		//only set isstarted=true when activity called startservice with action string
		if(intent != null && intent.getAction() != null){
			if(intent.getAction().equalsIgnoreCase("START_RECORDING")){
				if(dh != null){
					//dh is not null, a trip is probably recording now...
					Log.e("AntripService", "IncomingHandler: start recording called when dh is NOT null");
				} else if(!isStarted){
					dh = new DBHelper128(getApplicationContext());
					currentTid = intent.getLongExtra("tid", -1);
					stats = new TripStats();
					isStarted = true;
					isRecording = true;
					//should start foreground here, with a notification of course
					
				} else{
					//is already started, why is this being called again?
				}
			} else{
				//not sure what case this is, called by Android system?
			}
		} else{
			//called by the Android system maybe? killed and resurrected
		}
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		currentTid = null;
		
		locationQueue = null;
		
		dh = null;
		
		skyhookLocation = null;
		
		isRunning = false;
	}

	@Override
	public void newLocationUpdate(Location location) {
		Log.w("AntripService", "newLocationUpdate: new location received");
		//not recording, repost only valid and accurate opints to UI
		if(LocationFilter.validityFilter(location)){
			if(LocationFilter.accuracyFilter(location)){
				//filter tests can be stacked, a location update have to pass all the filters to be reported to activity
				sendMessageToUI(MSG_LOCATION_UPDATE, location);
			} else{
				//inaccurate points
			}
		} else{
			//invalid points
		}
		//if 3 consecutive location updates failed the filter, show a warning banner on screen
		
		//if recording=true, save location update to DB
		if(isRecording){
			if(dh != null){
				dh.insert(location, currentUid, currentTid.toString());
				stats.addOnePoint(location, false);
			} else{
				//this should not happen
			}
		}
	}
}
