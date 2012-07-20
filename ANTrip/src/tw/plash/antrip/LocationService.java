package tw.plash.antrip;

import java.util.Timer;
import java.util.TimerTask;

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
import com.skyhookwireless.wps.WPSPeriodicLocationCallback;
import com.skyhookwireless.wps.WPSReturnCode;
import com.skyhookwireless.wps.XPS;

public class LocationService extends Service {

	private XPS _xps;
	private WPSAuthentication auth;

	private boolean isRecording;

//	private String PERIOD_UPDATE = "PLASH_PERIOD_UPDATE";

	private Location mLocationBuffer = null;
	private Location nullLocation;

	private SharedPreferences pref;

	private DBHelper128 dh;
	
	private Timer mTimer;

	private Integer period;
	private Integer oldPeriod;
	
	private String error_no_uid = "NO UID IN PREFERENCE, ABORT RECORDING";
	private String error_no_action = "NO ACTION SPECIFIED, TERMINATE SERVICE";
	private String error_unknown_action = "UNKNOWN ACTION GIVEN, TERMINATE SERVICE";
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		isRecording = false;

		_xps = new XPS(this);
		auth = new WPSAuthentication("plash", "iis.sinica.edu.tw");

		nullLocation = new Location("");
		nullLocation.setLatitude(-999.0);
		nullLocation.setLongitude(-999.0);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("LocationService128", "onStartCommand called");

		pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		//period will have value after this line
		oldPeriod = period;
		period = Integer.valueOf(pref.getString("timeInterval", "30"));
		
		Log.e("locationService128", "period= " + period);
		
		String action = intent.getAction();
		if (action == null) {
			// does not accept startService call without any given ACTION
			errorStopService(error_no_action);
		} else if (action.equals("ACTION_START_SERVICE")) {
			// if equal, that means two consecutive call to startService without interval change
			if(period != oldPeriod){
				_xps.getXPSLocation(auth, period, XPS.EXACT_ACCURACY, _callback);
				//if recording, update the timer
				if(isRecording){
					stopTimer();
					setTimer();
				}
			}
		} else if (action.equals("ACTION_START_RECORDING")) {
			// start saving location updates into DB
			// create a unique key as local trip id
			Long tid = System.nanoTime();
			pref.edit().putLong("tid", tid).commit();
			// save the unique key to preference so mapview can access it
			
			// start a runnable in handler to fetch location every interval and
			// save it to DB
			setTimer(tid);
			
			isRecording = true;
			
		} else if (action.equals("ACTION_STOP_RECORDING")) {
			
			isRecording = false;
			
			// stop saving location updates into DB
			// kill the runnable that's been saving location to DB
			stopTimer();
			// remove all the related preference entries
			pref.edit().remove("tid").commit();
		} else {
			// unknown ACTION given, stop service
			errorStopService(error_unknown_action);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Cancel all tasks and let Dalvik GC timer
	 */
	void stopTimer(){
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
	}
	
	/**
	 * Set timer task with the saved user id, terminate process if there is no valid user id
	 */
	void setTimer(){
		final String userid = pref.getString("uid", "");
		if(userid != null){
			mTimer = new Timer();
			dh = new DBHelper128(getApplicationContext());
			mTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					// save to DB
					if(mLocationBuffer == null){
						//don't want to input null stuff into database
						mLocationBuffer = nullLocation;
					}
					dh.insert(mLocationBuffer, userid, pref.getLong("tid", -1));
				}
			}, 0, period);
		} else{
			errorStopService(error_no_uid);
		}
	}
	
	/**
	 * For first time setting up timer, also create new trip info entry
	 * @param tid
	 */
	void setTimer(Long tid){
		final String userid = pref.getString("uid", "");
		if(userid != null){
			mTimer = new Timer();
			dh = new DBHelper128(getApplicationContext());
			dh.createNewTripInfo(tid, userid);
			mTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					// save to DB
					if(mLocationBuffer == null){
						//don't want to input null stuff into database
						mLocationBuffer = nullLocation;
					}
					dh.insert(mLocationBuffer, userid, pref.getLong("tid", -1));
				}
			}, 0, period);
		} else{
			errorStopService(error_no_uid);
		}
	}
	
	void errorStopService(String errorEvent){
		//more actions can be done here, e.g. show a pop up with error message
		Log.e("LocationSerice128", "ERROR: " + errorEvent);
		stopSelf();
	}
	
	@Override
	public void onDestroy() {
		
		_xps.abort();
		
		pref.edit().remove("tid").commit();
		
		stopTimer();
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	// *** For Skyhook callback
	/**
	 * A single callback class that will be used to handle all notifications
	 * sent by WPS to our app.
	 */
	public class MyLocationCallback implements WPSPeriodicLocationCallback {

		public void done() {
			// tell the UI thread to re-enable the buttons
		}

		public WPSContinuation handleError(WPSReturnCode error) {
			if (isRecording) {
				// save to location buffer when recording
				mLocationBuffer = nullLocation;
			} else {
				// broadcast when not recording
				// actually no need to broadcast error when not recording
			}

			Log.d("Yu-Hsiang: Skyhook GPSLocation@LocationService = ",
					"error code = " + error.toString());
			return null;
		}

		public WPSContinuation handleWPSPeriodicLocation(WPSLocation wpslocation) {
			Location location = WPS2Location(wpslocation);

			if (isRecording) {
				// save to location buffer when recording
				mLocationBuffer = location;
			} else {
				// Broadcasts when not recording
				Intent intent = new Intent();
//				intent.setAction(PLASHConst.LOCATIONSERVICE_UPDATE);
				intent.putExtra("location", location);
				sendBroadcast(intent);
			}

			Log.d("Yu-Hsiang: Skyhook periodic location@LocationService = ", location.toString());
			return null;
		}
	}

	private final MyLocationCallback _callback = new MyLocationCallback();

	public Location WPS2Location(WPSLocation l) {
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
