package tw.plash.antrip;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class LocationServiceGPS extends Service {
	/**
	 * check-in related stuff
	 */
	private Location checkinLocationBuffer = null;
	private CandidateCheckinObject cco;
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
//	private XPS _xps;
//	private WPSAuthentication auth;
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
	
	private LocationManager lm;
	private LocationListener listener = new LocationListener() {
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		
		@Override
		public void onProviderEnabled(String provider) {}
		
		@Override
		public void onProviderDisabled(String provider) {}
		
		@Override
		public void onLocationChanged(Location location) {
			if(isBetterLocation(location, recorderLocationBuffer)){
				recorderLocationBuffer = location;
			}
			if (!isRecording) {
				// Broadcasts when not recording
				Intent intent = new Intent();
				intent.setAction("ACTION_LOCATION_SERVICE_SET_POSITION");
				intent.putExtra("location", recorderLocationBuffer);
				if (activityCanBroadcast) {
					sendBroadcast(intent);
				}
			}
			Log.d("location service", "periodic location= " + recorderLocationBuffer.toString());
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		isRecording = false;
		activityCanBroadcast = false;
		
		pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		currentSid = pref.getString("sid", "-1");
		
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		
//		_xps = new XPS(getApplicationContext());
//		auth = new WPSAuthentication("plash", "iis.sinica.edu.tw");
		
		nullLocation = new Location("");
		nullLocation.setLatitude(-999.0);
		nullLocation.setLongitude(-999.0);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.e("LocationService", "onStartCommand called");
		
		// period will have value after this line
		oldPeriodSEC = periodSEC;
		periodSEC = Integer.valueOf(pref.getString("timeInterval", "10"));
		periodMS = (long) (periodSEC * 1000);
		//Log.e("locationService", "period= " + periodSEC + " seconds");
		//Log.e("locationService", "period= " + periodMS + " milliseconds");
		
		String action = intent.getAction();
		//Log.e("location service", "action= " + action);
		if (action == null) {
			// does not accept startService call without any given ACTION
			errorStopService(error_no_action);
			
		} else if (action.equals("ACTION_START_SERVICE")) {
			// if equal, that means two consecutive call to startService without
			// interval change
			if (periodSEC != oldPeriodSEC) {
				//Log.e("getXPSLocation", "period= " + periodSEC);
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, listener);
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
//				_xps.getXPSLocation(auth, periodSEC, XPS.EXACT_ACCURACY, recorderCallback);
				// if recording, update the timer
				if (isRecording) {
					//Log.e("location service", "should not see this");
					stopTimer();
					setTimer();
				}
			}
		} else if (action.equals("ACTION_START_RECORDING")) {
			Intent noIntent = new Intent();
			PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, noIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			Notification noti;
			if(Locale.getDefault().getLanguage().contains("zh")){
				noti = new Notification(R.drawable.ant_24, "一個新的旅程開始了", System.currentTimeMillis());
				noti.setLatestEventInfo(getApplicationContext(), "雲水途誌", "正在紀錄一個旅程...", pIntent);
			} else{
				noti = new Notification(R.drawable.ant_24, "A NEW RECORDING HAS BEGUN", System.currentTimeMillis());
				noti.setLatestEventInfo(getApplicationContext(), "Antrip", "is recording a trip...", pIntent);
			}
			noti.flags = Notification.FLAG_ONGOING_EVENT;
			startForeground(1337, noti);
			// start saving location updates into DB
			// create a unique key as local trip id
			currentTid = intent.getExtras().getString("tid");
			// the insert action is already done by html using setcookie
			// pref.edit().putLong("tid", tid).commit();
			// save the unique key to preference so mapview can access it
			//Log.e("LocationService", "start recording, tid= " + currentTid);
			// start a runnable in handler to fetch location every interval and
			// save it to DB
			startNewTrip = true;
			stats = new TripStats();
			
			isRecording = true;
			
			setTimer();
			
		} else if (action.equals("ACTION_STOP_RECORDING")) {
			
			isRecording = false;
			
			//Log.e("LocationService", "stop recording");
			
			// finalize trip stats and save to DB
			String name = intent.getExtras().getString("tripname");
			//Log.e("locationService", "tripname= " + name);
			dh.insertEndInfo(currentSid, currentTid, name, new Timestamp(new Date().getTime()).toString(),
					stats.getLength());
			
			// stop saving location updates into DB
			// kill the runnable that's been saving location to DB
			stopTimer();
			
			// remove all the related preference entries
			stats = null;
			currentTid = null;
			pref.edit().remove("trip_id").commit();
			
			stopForeground(true);
			
		} else if (action.equals("ACTION_GET_CHECKIN_LOCATION")) {
			//Log.e("LocationService", "get check-in location");
			checkinLocationBuffer = recorderLocationBuffer;
			
		} else if (action.equals("ACTION_SAVE_CCO")) {
			//Log.e("LocationService", "save cco");
			// save check-in data to DB
			cco = (CandidateCheckinObject) intent.getExtras().getSerializable("cco");
			// only save check-in to DB when lcheckinlocationbuffer is not null
			String picPath = cco.getPicturePath();
			// replace null checkin location buffer with recorder location
			// buffer
			if (picPath != null) {
				GeoTagPicture(picPath, checkinLocationBuffer.getLatitude(), checkinLocationBuffer.getLongitude());
			}
			dh.insert(checkinLocationBuffer, currentSid, currentTid, cco);
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
			if (activityCanBroadcast) {
				sendBroadcast(ccoIntent);
			}
			
		} else if (action.equals("ACTION_CANCEL_CHECKIN")) {
			//Log.e("LocationService", "cancel check-in");
			// clear temp check-in location object
			checkinLocationBuffer = null;
			cco = null;
		} else if (action.equals("ACTION_LOCATION_SERVICE_SYNC_POSITION")) {
			//Log.e("location service", "sync position");
			if (dh != null && dh.DBIsOpen()) {
				JSONObject syncPos = dh.getOneTripData(currentSid, currentTid, false);
				Intent syncIntent = new Intent("ACTION_LOCATION_SERVICE_SYNC_POSITION");
				syncIntent.putExtra("location", syncPos.toString());
				if (activityCanBroadcast) {
					sendBroadcast(syncIntent);
				}
			} else {
				errorStopService("dh is null or db is closed");
			}
		} else if (action.equals("ACTION_ACTIVITY_NOT_READY_FOR_BROADCAST")) {
			activityCanBroadcast = false;
		} else if (action.equals("ACTION_ACTIVITY_IS_READY_FOR_BROADCAST")) {
			activityCanBroadcast = true;
		} else {
			// unknown ACTION given, stop service
			errorStopService(error_unknown_action);
		}
		return super.onStartCommand(intent, flags, startId);
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
					stats.addOnePoint(recorderLocationBuffer);
					dh.insert(recorderLocationBuffer, currentSid, currentTid);
					//Log.e("locationService", "actual DB insert2");
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
					if (activityCanBroadcast) {
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
		//Log.e("LocationSerice128", "ERROR: " + errorEvent);
		stopSelf();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//Log.e("locationService", "onDestroy called");
		
		lm.removeUpdates(listener);
		
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
	
//	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int THIRTY_SECONDS = 1000 * 30;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
//	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
//	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isSignificantlyNewer = timeDelta > THIRTY_SECONDS;
	    boolean isSignificantlyOlder = timeDelta < -THIRTY_SECONDS;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
}
