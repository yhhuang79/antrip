package tw.plash.antrip;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.skyhookwireless.wps.WPSAuthentication;
import com.skyhookwireless.wps.WPSContinuation;
import com.skyhookwireless.wps.WPSLocation;
import com.skyhookwireless.wps.WPSLocationCallback;
import com.skyhookwireless.wps.WPSPeriodicLocationCallback;
import com.skyhookwireless.wps.WPSReturnCode;
import com.skyhookwireless.wps.XPS;

public class LocationService extends Service {
	/**
	 * check-in related stuff
	 */
	private boolean checkinCallbackCanCallAgain;
	private Location checkinLocationBuffer = null;
	private CandidateCheckinObject cco;
	/**
	 * recording related
	 */
	private boolean isRecording;
	private Location recorderLocationBuffer = null;
	private Location nullLocation;
	private DBHelper128 dh;
	private Timer mTimer;
	/**
	 * generic
	 */
	
	private XPS _xps;
	private WPSAuthentication auth;
	private SharedPreferences pref;
	private Integer periodSEC;
	private Long periodMS;
	private Integer oldPeriodSEC;
	/**
	 * possible error situations
	 */
	private String error_no_uid = "NO UID IN PREFERENCE, ABORT RECORDING";
	private String error_no_action = "NO ACTION SPECIFIED, TERMINATE SERVICE";
	private String error_unknown_action = "UNKNOWN ACTION GIVEN, TERMINATE SERVICE";

	@Override
	public void onCreate() {
		super.onCreate();

		isRecording = false;
		checkinCallbackCanCallAgain = true;
		
		_xps = new XPS(getApplicationContext());
		auth = new WPSAuthentication("plash", "iis.sinica.edu.tw");

		nullLocation = new Location("");
		nullLocation.setLatitude(-999.0);
		nullLocation.setLongitude(-999.0);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("LocationService", "onStartCommand called");

		pref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		// period will have value after this line
		oldPeriodSEC = periodSEC;
		periodSEC = Integer.valueOf(pref.getString("timeInterval", "30"));
		periodMS = (long) (periodSEC * 1000);
		Log.e("locationService", "period= " + periodSEC + " seconds");
		Log.e("locationService", "period= " + periodMS + " milliseconds");
		
		String action = intent.getAction();
		if (action == null) {
			// does not accept startService call without any given ACTION
			errorStopService(error_no_action);
		} else if (action.equals("ACTION_START_SERVICE")) {
			// if equal, that means two consecutive call to startService without
			// interval change
			if (periodSEC != oldPeriodSEC) {
				Log.e("getXPSLocation", "period= " + periodSEC);
				_xps.getXPSLocation(auth, periodSEC, XPS.EXACT_ACCURACY, recorderCallback);
				// if recording, update the timer
				if (isRecording) {
					stopTimer();
					setTimer();
				}
			}
		} else if (action.equals("ACTION_START_RECORDING")) {
			// start saving location updates into DB
			// create a unique key as local trip id
			Long tid = intent.getExtras().getLong("tid");
			//the insert action is already done by html using setcookie
//			pref.edit().putLong("tid", tid).commit();
			// save the unique key to preference so mapview can access it
			Log.e("LocationService", "start recording, tid= " + tid);
			// start a runnable in handler to fetch location every interval and
			// save it to DB
			setTimer(tid);

			isRecording = true;

		} else if (action.equals("ACTION_STOP_RECORDING")) {
			
			isRecording = false;
			
			Log.e("LocationService", "stop recording");
			// stop saving location updates into DB
			// kill the runnable that's been saving location to DB
			stopTimer();
			// remove all the related preference entries
			pref.edit().remove("trip_id").commit();
			
		} else if (action.equals("ACTION_GET_CHECKIN_LOCATION")) {
			
			Log.e("LocationService", "get check-in location");
			// request one location for picture geotagging
			if(checkinCallbackCanCallAgain){
				_xps.getLocation(auth, null, checkinCallback);
				checkinCallbackCanCallAgain = false;
			} else{
				//try again later
			}
			
		} else if (action.equals("ACTION_SAVE_CCO")) {
			Log.e("LocationService", "save cco");
			// save check-in data to DB
			cco = (CandidateCheckinObject) intent.getExtras().getSerializable("cco");
			
		} else if (action.equals("ACTION_CANCEL_CHECKIN")){
			Log.e("LocationService", "cancel check-in");
			// clear temp check-in location object
			checkinLocationBuffer = null;
			cco = null;
		} else {
			// unknown ACTION given, stop service
			errorStopService(error_unknown_action);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Cancel all tasks and let Dalvik GC timer
	 */
	void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
		if(dh != null){
			dh.closeDB();
			dh = null;
		}
	}

	/**
	 * Set timer task with the saved user id, terminate process if there is no
	 * valid user id
	 */
	void setTimer() {
		final String userid = pref.getString("uid", null);
		if (userid != null) {
			mTimer = new Timer();
			dh = new DBHelper128(getApplicationContext());
			mTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					// save to DB
					if (recorderLocationBuffer == null) {
						// don't want to input null stuff into database
						recorderLocationBuffer = getCurrentNullLocation();
					}
//					dh.insert(recorderLocationBuffer, userid, Long.valueOf(pref.getString("trip_id", "-1")));
					Log.e("locationService", "mock DB insert");
				}
			}, 0, periodMS);
		} else {
			errorStopService(error_no_uid);
		}
	}

	/**
	 * For first time setting up timer, also create new trip info entry
	 * 
	 * @param tid
	 */
	void setTimer(Long tid) {
		final String userid = pref.getString("sid", null);
		if (userid != null) {
			mTimer = new Timer();
			dh = new DBHelper128(getApplicationContext());
			dh.createNewTripInfo(userid, tid, new Timestamp(new Date().getTime()).toString());
			mTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					// save to DB
					if (recorderLocationBuffer == null) {
						// don't want to input null stuff into database
						recorderLocationBuffer = getCurrentNullLocation();
					}
//					dh.insert(recorderLocationBuffer, userid, Long.valueOf(pref.getString("trip_id", "-1")));
					Log.e("locationService", "mock DB insert2");
					/**
					 * if activity if running in foreground
					 * broadcast location from recorderLocationBuffer
					 */
				}
			}, 0, periodMS);
		} else {
			errorStopService(error_no_uid);
		}
	}

	void errorStopService(String errorEvent) {
		// more actions can be done here, e.g. show a pop up with error message
		Log.e("LocationSerice128", "ERROR: " + errorEvent);
		stopSelf();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.e("locationService", "onDestroy called");
		
		_xps.abort();

		pref.edit().remove("trip_id").commit();

		stopTimer();

		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	
	private Location getCurrentNullLocation(){
		nullLocation.setTime(System.currentTimeMillis());
		return nullLocation;
	}
	
	private final RecorderLocationCallback recorderCallback = new RecorderLocationCallback();
	private final CheckinLocationCallback checkinCallback = new CheckinLocationCallback();
	
	public class CheckinLocationCallback implements WPSLocationCallback{
		
		@Override
		public void done() {
			checkinCallbackCanCallAgain = true;
		}

		@Override
		public WPSContinuation handleError(WPSReturnCode arg0) {
			checkinLocationBuffer = getCurrentNullLocation();
			Log.d("location service", "error code = " + arg0.toString());
			return WPSContinuation.WPS_CONTINUE;
		}

		/**
		 * This will be the location callback for check in points. Recorder will
		 * be default at a lower frequency, so a check-in point might not be
		 * able to tie with a nearby point, thus check-in points need to request
		 * their own location
		 */
		@Override
		public void handleWPSLocation(WPSLocation arg0) {
			checkinLocationBuffer = WPS2Location(arg0);
			Log.d("location service","check-in location= " + checkinLocationBuffer.toString());
		}
		
	}
	
	public class RecorderLocationCallback implements WPSPeriodicLocationCallback{

		public void done() {
			
		}

		public WPSContinuation handleError(WPSReturnCode error) {
			if (isRecording) {
				// save to location buffer when recording
				//set the time so we can better analysis the situation
				recorderLocationBuffer = getCurrentNullLocation();
			} else {
				// broadcast when not recording
				// actually no need to broadcast error when not recording
			}

			Log.d("location service", "error code = " + error.toString());
			return WPSContinuation.WPS_CONTINUE;
		}

		public WPSContinuation handleWPSPeriodicLocation(WPSLocation wpslocation) {
			Location location = WPS2Location(wpslocation);

			if (isRecording) {
				// save to location buffer when recording
				recorderLocationBuffer = location;
			} else {
				// Broadcasts when not recording
				Intent intent = new Intent();
				intent.setAction("ACTION_LOCATION_SERVICE_UPDATE");
				intent.putExtra("location", location);
				sendBroadcast(intent);
			}

			Log.d("location service","periodic location= " + location.toString());
			return null;
		}
	}

	
	
	private Location WPS2Location(WPSLocation l) {
		Location location = new Location("skyhook");
		location.setLatitude(l.getLatitude());
		location.setLongitude(l.getLongitude());
		location.setTime(l.getTime());
		location.setAltitude(l.getAltitude());
		location.setSpeed((float) l.getSpeed());
		location.setBearing((float) l.getBearing());
		location.setAccuracy((float) l.getHPE());
		return location;
	}
}
