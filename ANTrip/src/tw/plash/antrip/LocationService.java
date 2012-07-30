package tw.plash.antrip;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.ExifInterface;
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
	private Location checkinLocationBuffer = null;
	private CandidateCheckinObject cco;
	private final int MAX_RETRY = 5;
	private int checkinTryCounter;
	/**
	 * recording related
	 */
	private boolean isRecording;
	private boolean startNewTrip = false;
	private Location recorderLocationBuffer = null;
	private Location nullLocation;
	private DBHelper128 dh;
	private Timer mTimer;
	private String currentTid = null;
	private String currentSid = null;
	private TripStats stats = null;
	/**
	 * generic
	 */
	private boolean activityCanBroadcast;
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
		activityCanBroadcast = false;
		
		pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		currentSid = pref.getString("sid", "-1");
		
		_xps = new XPS(getApplicationContext());
		auth = new WPSAuthentication("plash", "iis.sinica.edu.tw");
		
		nullLocation = new Location("");
		nullLocation.setLatitude(-999.0);
		nullLocation.setLongitude(-999.0);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("LocationService", "onStartCommand called");
		
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
			currentTid = intent.getExtras().getString("tid");
			// the insert action is already done by html using setcookie
			// pref.edit().putLong("tid", tid).commit();
			// save the unique key to preference so mapview can access it
			Log.e("LocationService", "start recording, tid= " + currentTid);
			// start a runnable in handler to fetch location every interval and
			// save it to DB
			startNewTrip = true;
			stats = new TripStats();
			
			isRecording = true;
			
			setTimer();
			
		} else if (action.equals("ACTION_STOP_RECORDING")) {
			
			isRecording = false;
			
			Log.e("LocationService", "stop recording");
			
			// finalize trip stats and save to DB
			String name = intent.getExtras().getString("tripname");
			Log.e("locationService", "tripname= " + name);
			dh.insertEndInfo(currentSid, currentTid, name, new Timestamp(new Date().getTime()).toString(),
					stats.getLength());
			
			// stop saving location updates into DB
			// kill the runnable that's been saving location to DB
			stopTimer();
			
			// remove all the related preference entries
			stats = null;
			currentTid = null;
			pref.edit().remove("trip_id").commit();
			
		} else if (action.equals("ACTION_GET_CHECKIN_LOCATION")) {
			Log.e("LocationService", "get check-in location");
			// try to get a location for check-in purpose, maximum retry is 5
			// times
			// request one location for picture geotagging
			checkinTryCounter = 0;
			getCheckinLocation();
		} else if (action.equals("ACTION_SAVE_CCO")) {
			Log.e("LocationService", "save cco");
			// save check-in data to DB
			cco = (CandidateCheckinObject) intent.getExtras().getSerializable("cco");
			// only save check-in to DB when lcheckinlocationbuffer is not null
			String picPath = cco.getPicturePath();
			if (checkinLocationBuffer != null) {
				if (picPath != null) {
					GeoTagPicture(picPath, checkinLocationBuffer.getLatitude(), checkinLocationBuffer.getLongitude());
				}
				dh.insert(checkinLocationBuffer, currentSid, currentTid, cco);
			} else {
				// otherwise use the nearest recorderlocationbuffer to replace it
				// XXX this is a last resort kind of method, saving multiple
				// check-in to the same location will compromise the user experience
				if (picPath != null) {
					//need to geotag the picture before saving it to DB
					GeoTagPicture(picPath, recorderLocationBuffer.getLatitude(), recorderLocationBuffer.getLongitude());
				}
				dh.insert(recorderLocationBuffer, currentSid, currentTid, cco);
			}
			// the object to be returned
			JSONObject addpos = new JSONObject();
			try {
				// the ckeckInDataList jsonarray
				JSONArray array = new JSONArray();
				// the object within jsonarray
				JSONObject tmp = new JSONObject();
				tmp.put("lat", checkinLocationBuffer.getLatitude());
				tmp.put("lng", checkinLocationBuffer.getLongitude());
				tmp.put("timestamp", new Timestamp(checkinLocationBuffer.getTime()).toString());
				// the object within one entry of data
				JSONObject checkin = new JSONObject();
				if (cco.getPicturePath() != null) {
					checkin.put("picture_uri", cco.getPicturePath());
				}
				if (cco.getEmotionID() != null) {
					checkin.put("emotion", cco.getEmotionID());
				}
				if (cco.getCheckinText() != null) {
					checkin.put("message", cco.getCheckinText());
				}
				tmp.put("CheckIn", checkin);
				array.put(tmp);
				addpos.put("CheckInDataList", array);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Intent ccoIntent = new Intent("ACTION_LOCATION_SERVICE_ADD_POSITION");
			ccoIntent.putExtra("location", addpos.toString());
			if(activityCanBroadcast){
				sendBroadcast(ccoIntent);
			}
			
		} else if (action.equals("ACTION_CANCEL_CHECKIN")) {
			Log.e("LocationService", "cancel check-in");
			// clear temp check-in location object
			checkinLocationBuffer = null;
			cco = null;
		} else if (action.equals("ACTION_LOCATION_SERVICE_SYNC_POSITION")) {
			Log.e("location service", "sync position");
			if (dh != null && dh.DBIsOpen()) {
				JSONObject syncPos = dh.getOneTripData(currentSid, currentTid, false);
				Intent syncIntent = new Intent("ACTION_LOCATION_SERVICE_SYNC_POSITION");
				syncIntent.putExtra("location", syncPos.toString());
				if(activityCanBroadcast){
					sendBroadcast(syncIntent);
				}
			} else {
				errorStopService("dh is null or db is closed");
			}
		} else if(action.equals("ACTION_ACTIVITY_NOT_READY_FOR_BROADCAST")){
			activityCanBroadcast = false;
		}else if(action.equals("ACTION_ACTIVITY_IS_READY_FOR_BROADCAST")){
			activityCanBroadcast = true;
		}else{
			// unknown ACTION given, stop service
			errorStopService(error_unknown_action);
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void getCheckinLocation() {
		_xps.getLocation(auth, null, checkinCallback);
	}
	
	/**
	 * Grab the picture at the given path, and geotagged it with the latitude
	 * and longitude value in the given cachedpoint object
	 * 
	 * @param path
	 * @param point
	 */
	private void GeoTagPicture(String path, double latitude, double longitude) {
		ExifInterface exif;
		
		try {
			exif = new ExifInterface(path);
			int num1Lat = (int) Math.floor(latitude);
			int num2Lat = (int) Math.floor((latitude - num1Lat) * 60);
			double num3Lat = (latitude - ((double) num1Lat + ((double) num2Lat / 60))) * 3600000;
			
			int num1Lon = (int) Math.floor(longitude);
			int num2Lon = (int) Math.floor((longitude - num1Lon) * 60);
			double num3Lon = (longitude - ((double) num1Lon + ((double) num2Lon / 60))) * 3600000;
			
			exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, num1Lat + "/1," + num2Lat + "/1," + num3Lat + "/1000");
			exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, num1Lon + "/1," + num2Lon + "/1," + num3Lon + "/1000");
			
			if (latitude > 0) {
				exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
			} else {
				exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
			}
			
			if (longitude > 0) {
				exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
			} else {
				exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
			}
			
			exif.saveAttributes();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		if (dh != null) {
			dh.closeDB();
			dh = null;
		}
	}
	
	/**
	 * For first time setting up timer, also create new trip info entry
	 * 
	 * @param tid
	 */
	void setTimer() {
		// final String userid = pref.getString("sid", null);
		if (currentSid != null) {
			mTimer = new Timer();
			dh = new DBHelper128(getApplicationContext());
			// only create new trip info if we're starting a new trip
			if (startNewTrip) {
				dh.createNewTripInfo(currentSid, currentTid, new Timestamp(new Date().getTime()).toString());
				startNewTrip = false;
			}
			mTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					// save to DB
					if (recorderLocationBuffer == null) {
						// don't want to input null stuff into database
						recorderLocationBuffer = getCurrentNullLocation();
					}
					dh.insert(recorderLocationBuffer, currentSid, currentTid);
					Log.e("locationService", "actual DB insert2");
					/**
					 * if activity is running in foreground broadcast location
					 * from recorderLocationBuffer
					 */
					JSONObject addpos = new JSONObject();
					try {
						JSONArray array = new JSONArray();
						JSONObject tmp = new JSONObject();
						tmp.put("lat", recorderLocationBuffer.getLatitude());
						tmp.put("lng", recorderLocationBuffer.getLongitude());
						tmp.put("timestamp", new Timestamp(recorderLocationBuffer.getTime()).toString());
						array.put(tmp);
						addpos.put("CheckInDataList", array);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Intent intent = new Intent();
					intent.setAction("ACTION_LOCATION_SERVICE_ADD_POSITION");
					intent.putExtra("location", addpos.toString());
					if(activityCanBroadcast){
						sendBroadcast(intent);
					}
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
	
	private Location getCurrentNullLocation() {
		nullLocation.setTime(System.currentTimeMillis());
		return nullLocation;
	}
	
	private final RecorderLocationCallback recorderCallback = new RecorderLocationCallback();
	private final CheckinLocationCallback checkinCallback = new CheckinLocationCallback();
	
	public class CheckinLocationCallback implements WPSLocationCallback {
		
		@Override
		public void done() {
			checkinTryCounter += 1;
			if (checkinLocationBuffer != null) {
				// we're good
				
			} else {
				// if < maximum retry, try again
				if (checkinTryCounter < MAX_RETRY) {
					getCheckinLocation();
				}
			}
		}
		
		@Override
		public WPSContinuation handleError(WPSReturnCode arg0) {
			checkinLocationBuffer = null;
			
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
			Log.d("location service", "check-in location= " + checkinLocationBuffer.toString());
		}
	}
	
	public class RecorderLocationCallback implements WPSPeriodicLocationCallback {
		
		public void done() {
			
		}
		
		public WPSContinuation handleError(WPSReturnCode error) {
			if (isRecording) {
				// save to location buffer when recording
				// set the time so we can better analysis the situation
				recorderLocationBuffer = getCurrentNullLocation();
			} else {
				// broadcast when not recording
				// actually no need to broadcast error when not recording
			}
			
			Log.d("location service", "error code = " + error.toString());
			return WPSContinuation.WPS_CONTINUE;
		}
		
		public WPSContinuation handleWPSPeriodicLocation(WPSLocation wpslocation) {
			recorderLocationBuffer = WPS2Location(wpslocation);
			
			if (!isRecording) {
				// Broadcasts when not recording
				Intent intent = new Intent();
				intent.setAction("ACTION_LOCATION_SERVICE_SET_POSITION");
				intent.putExtra("location", recorderLocationBuffer);
				if(activityCanBroadcast){
					sendBroadcast(intent);
				}
			}
			Log.d("location service", "periodic location= " + recorderLocationBuffer.toString());
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
