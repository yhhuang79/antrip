package tw.plash.antrip.offline;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class AntripService2 extends Service implements LocationPublisher{
	
	final static int CONNECTION_TIMEOUT = 5000; //5 seconds connection timeout
	
	final private int timeout_FirstLocationFix = 60000; //60 seconds timeout for first location fix
	final private int timeout_locationFixInterval = 30000;//30 seconds timeout between locations...
	
	private static boolean isRecording;
	
	private static String currentTid;
	
//	private SharedPreferences pref;
	private TripStats stats;
	
	private Timer mTimer;
	
	private Context mContext;
	
	private SkyhookLocation skyhook;
	
	private boolean isUsingSkyhook;
	private boolean shouldSkiptimertask;
	
	private DBHelper2 dh2;
	private boolean isValidTripToKeep;
	
	static final int MSG_REGISTER_CLIENT = 0;
	static final int MSG_UNREGISTER_CLIENT = 1;
	static final int MSG_INIT_LOCATION_THREAD = 2;
	static final int MSG_STOP_LOCATION_THREAD = 3;
	static final int MSG_START_RECORDING = 4;
	static final int MSG_PAUSE_RECORDING = 5;
	static final int MSG_STOP_RECORDING = 6;
	static final int MSG_STOP_RECORDING_CONFIRMED = 7;
	static final int MSG_SAVE_TRIPNAME = 8;
	static final int MSG_SAVE_CCO = 11;
	static final int MSG_AUTO_STOP_RECORDING_CONFIRMED = 12;
	
	private final int priMSG_SWITCH_LOCATING_METHOD = 1000;
	private final int priMSG_TOO_MANY_INVALID_POINTS = 1001;
	private final int priMSG_TOO_MANY_INACCURATE_POINTS = 1002;
	private final int priMSG_LOW_BATTERY_AUTO_STOP = 1003;
	
	static final int MSG_SYNC_LOCATION = 9;
	
	static final int MSG_LOCATION_UPDATE = 10;
	
	private BroadcastReceiver br = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent != null && intent.getAction() != null){
				if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
					if(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 999) < 25){
						if(isRecording){
							//don't care if you're not recording...
							Notification notification = new Notification(R.drawable.antrip_status_icon, mContext.getString(R.string.lowbatteryautostopnotification_title), System.currentTimeMillis());
							notification.setLatestEventInfo(mContext, mContext.getString(R.string.lowbatteryautostopnotification_title), mContext.getString(R.string.lowbatteryautostopnotification_message), PendingIntent.getActivity(mContext, 0, new Intent(), 0));
							((NotificationManager)mContext.getSystemService(NOTIFICATION_SERVICE)).notify(1384, notification);
							
							try {
								inMessenger.send(Message.obtain(null, priMSG_LOW_BATTERY_AUTO_STOP));
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
					}
				} else if(intent.getAction().equals(Intent.ACTION_SHUTDOWN)){
					//shutdown event, stop recording and save all the stuff
					try {
						inMessenger.send(Message.obtain(null, priMSG_LOW_BATTERY_AUTO_STOP));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				} 
			}
		}
	};
	
	private LocationManager locManager;
	private LocationListener locListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			if(provider.equals(LocationManager.GPS_PROVIDER)){
				//XXX
				//wtf? asks user to re-enable GPS
			}
		}
		
		@Override
		public void onLocationChanged(Location location) {
			location.setTime(new Date().getTime());
			newLocationUpdate(location);
		}
	};
	
	private Messenger outMessenger;
	private Messenger inMessenger = new Messenger(new IncomingHandler());
	
	private class IncomingHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case priMSG_SWITCH_LOCATING_METHOD:
				Log.w("priMSG_SWITCH_LOCATING_METHOD", "called");
				stopLocationUpdate();
				if(isNetworkAvailable(mContext)){
					//internet available, try to use skyhook
					skyhook = new SkyhookLocation(mContext, AntripService2.this);
					skyhook.run(10);
				} else{
					//no internet, uses GPS
					locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 10, locListener);
				}
				break;
			case priMSG_LOW_BATTERY_AUTO_STOP:
				Log.w("priMSG_LOW_BATTERY_AUTO_STOP", "called");
				shouldSkiptimertask = true;
				stopLocationUpdate();
				//finalize tripstats, closes db
				isRecording = false;
				stopForeground(true);
				if(isValidTripToKeep){
					//save trip stats right now!!!!
					if(dh2 != null){
						stats.setButtonEndTime(new Date().getTime());
						dh2.saveTripStats(currentTid, stats);
						dh2.closeDB();
						dh2 = null;
					} else{
						Log.e("antrip service2", "MSG_STOP_RECORDING, dh2 is null, cannot save trip stats");
					}
					//trip has at least one valid point
					sendMessageToUI(MSG_AUTO_STOP_RECORDING_CONFIRMED, null);
				} else{
					//invalid trip, delete trip from db and show warning
					if(dh2 != null){
						dh2.deleteLocalTrip(currentTid);
					} else{
						Log.e("antrip service2", "MSG_STOP_RECORDING, dh2 is null, cannot delete trip");
					}
					currentTid = null;
					//try to not allow user to input trip name
					sendMessageToUI(MSG_AUTO_STOP_RECORDING_CONFIRMED, null);
				}
				isValidTripToKeep = false;
				if(dh2 != null){
					dh2.closeDB();
					dh2 = null;
				}
				break;
			case MSG_REGISTER_CLIENT:
				Log.w("MSG_REGISTER_CLIENT", "called");
				outMessenger = msg.replyTo;
				//if isRecording = true, sync location
				if(isRecording){
					if(dh2 != null){
						ArrayList<JSONObject> data = dh2.getCurrentTripData(currentTid);
//						Log.e("sync location", "data= " + data);
						sendMessageToUI(MSG_SYNC_LOCATION, data);
						//send the last point as location update
						Location loc = dh2.getLatestGoodPointFromCurrentTripData(currentTid);
						sendMessageToUI(MSG_LOCATION_UPDATE, loc);
					}
				}
				break;
			case MSG_UNREGISTER_CLIENT:
				Log.w("MSG_UNREGISTER_CLIENT", "called");
				if(outMessenger == msg.replyTo){
					outMessenger = null;
				}
				if(!isRecording){
					if(mTimer != null){
						mTimer.cancel();
						mTimer.purge();
						mTimer = null;
					}
					stopLocationUpdate();
				}
				break;
			case MSG_INIT_LOCATION_THREAD:
				Log.w("MSG_INIT_LOCATION_THREAD", "called");
				if (isRecording) {
					// skyhook is already running
				} else {
					mTimer = new Timer();
					mTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							if(!shouldSkiptimertask){
								//if a new location update is not received in 1 minute, switch location service
								try {
									inMessenger.send(Message.obtain(null, priMSG_SWITCH_LOCATING_METHOD));
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
						}
					}, timeout_FirstLocationFix);
					if(isNetworkAvailable(mContext)){
						if (skyhook != null) {
							skyhook.cancel();
						}
						skyhook = new SkyhookLocation(mContext, AntripService2.this);
						skyhook.run(10);
						isUsingSkyhook = true;
					} else{
						locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 10, locListener);
						isUsingSkyhook = false;
					}
				}
				break;
			case MSG_STOP_LOCATION_THREAD:
				Log.w("MSG_STOP_LOCATION_THREAD", "called");
				if (isRecording) {
					
				} else {
					stopLocationUpdate();
				}
				break;
			case MSG_START_RECORDING:
				Log.w("MSG_START_RECORDING", "called");
				//init dbhelper and start saving stuff to DB
				if(dh2 != null){
					//should be null at this point
				} else{
					//generate a new tripid
					//show notification
					dh2 = new DBHelper2(mContext);
					isValidTripToKeep = false;
					stats = new TripStats();
					stats.setButtonStartTime(new Date().getTime());
					currentTid = String.valueOf(new Date().getTime());
					
					//create a new entry in db
					Log.e("new recording!", "tripid= " + currentTid);
					isRecording = true;
					showNotification();
				}
				break;
			case MSG_STOP_RECORDING:
				Log.w("MSG_STOP_RECORDING", "called");
				//finalize tripstats, closes db
				isRecording = false;
				stopForeground(true);
				if(isValidTripToKeep){
					//save trip stats right now!!!!
					if(dh2 != null){
						stats.setButtonEndTime(new Date().getTime());
						dh2.saveTripStats(currentTid, stats);
						dh2.closeDB();
						dh2 = null;
					} else{
						Log.e("antrip service2", "MSG_STOP_RECORDING, dh2 is null, cannot save trip stats");
					}
					//trip has at least one valid point
					sendMessageToUI(MSG_STOP_RECORDING_CONFIRMED, true);
				} else{
					//invalid trip, delete trip from db and show warning
					if(dh2 != null){
						dh2.deleteLocalTrip(currentTid);
					} else{
						Log.e("antrip service2", "MSG_STOP_RECORDING, dh2 is null, cannot delete trip");
					}
					currentTid = null;
					//try to not allow user to input trip name
					sendMessageToUI(MSG_STOP_RECORDING_CONFIRMED, false);
				}
				isValidTripToKeep = false;
				if(dh2 != null){
					dh2.closeDB();
					dh2 = null;
				}
				break;
			case MSG_SAVE_TRIPNAME:
				Log.w("MSG_SAVE_TRIPNAME", "called");
				//init a new dbhelper and save the trip name
				if(msg.obj instanceof String){
					if(dh2 == null){
						dh2 = new DBHelper2(mContext);
					}
					if(currentTid != null){
						dh2.setTripName(currentTid, String.valueOf(msg.obj));
					} else{
						throw new NullPointerException();
					}
					dh2.closeDB();
					dh2 = null;
				} else{
					Log.e("antrip service2", "MSG_SAVE_TRIPNAME: tripname obj not STRING!!!");
				}
				break;
			case MSG_SAVE_CCO:
				Log.w("MSG_SAVE_CCO", "called");
				if(msg.obj instanceof CandidateCheckinObject){
					if(dh2 != null){
						dh2.insert((CandidateCheckinObject) msg.obj, currentTid);
						stats.addOnePoint(((CandidateCheckinObject) msg.obj).getLocation(), true);
					}
				}
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	}
	
	private void sendMessageToUI(int msgType, Object payload) {
		try {
			if (outMessenger != null) {
				if(payload != null){
					outMessenger.send(Message.obtain(null, msgType, payload));
				} else{
					outMessenger.send(Message.obtain(null, msgType));
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void stopLocationUpdate(){
		if (skyhook != null) {
			skyhook.asyncCancel();
//			skyhook.cancel();
			skyhook = null;
		}
		if(locManager != null){
			if(locListener != null){
				locManager.removeUpdates(locListener);
			}
		}
	}
	
	private void showNotification(){
		Notification notification = new Notification(R.drawable.antrip_status_icon, mContext.getString(R.string.notification_new_trip_started), System.currentTimeMillis());
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, GMapRecorderActivity3.class), PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(mContext, getResources().getString(R.string.app_name), getResources().getString(R.string.notification_is_recording_a_trip), pendingIntent);
		startForeground(1377, notification);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mContext = this;
		
//		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		skyhook = null;
		
		isUsingSkyhook = false;
		
		locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		dh2 = null;
		
		isValidTripToKeep = false;
		
		currentTid = null;
		
		isRecording = false;
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_SHUTDOWN);
		registerReceiver(br, filter);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null && intent.getAction() != null){
			if(intent.getAction().equals("startcalledfromgmaprecorder")){
				//start service but do nothing
				//ignore this call if already running
			} else {
				stopSelf();
			}
		} else{
			stopSelf();
		}
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		shouldSkiptimertask = true;
		if(mTimer != null){
			mTimer.cancel();
			mTimer.purge();
		}
		mTimer = null;
		
		isRecording = false;
		
		inMessenger = null;
		outMessenger = null;
		
		unregisterReceiver(br);
		
		stopForeground(true);
		
		stopLocationUpdate();
		
		locManager = null;
		
		currentTid = null;
		if(dh2 != null){
			dh2.closeDB();
		}
		dh2 = null;
	}
	
	//need a timer to check the time between the latest two location update
	private Long previousLocationTime = null;
	
	@Override
	public void newLocationUpdate(Location location) {
		//need to stop timer
		if(mTimer != null){
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
		// should setup a new timeout timer
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(!shouldSkiptimertask){
					try {
						inMessenger.send(Message.obtain(null, priMSG_SWITCH_LOCATING_METHOD));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}, timeout_locationFixInterval);
		//XXX
		//compare 10 consecutive location updates, if they are close-by
		//prompt the "you haven't moved in XX:XX" message
		
		if(LocationFilter.isValid(location)){
			if(LocationFilter.isAccurate(location)){
				if(previousLocationTime == null){
					previousLocationTime = location.getTime();
					Log.d("antripservice2", "first locationupdate, previous time=" + previousLocationTime + ", location update time=" + location.getTime());
					sendMessageToUI(MSG_LOCATION_UPDATE, location);
				} else if((location.getTime() - previousLocationTime) > 8500){
					Log.e("antripservice2", "locationupdate published, previous time=" + previousLocationTime + ", location update time=" + location.getTime() + ", difference=" + (location.getTime()-previousLocationTime));
					previousLocationTime = location.getTime();
					sendMessageToUI(MSG_LOCATION_UPDATE, location);
				} else{
					Log.d("antripservice2", "locationupdate not published, previous time=" + previousLocationTime + ", location update time=" + location.getTime() + ", difference=" + (location.getTime()-previousLocationTime));
				}
			} else{
				//XXX
				//show warning about moving to a place with better GPS signal...
				//if this section if reached like 5 consecutive times
				
			}
		} else{
			//XXX
			//show warning about no location fix can be obtained...maybe try to record later?
			//if this section if reached like 5 consecutive times
			
		}
		if(isRecording){
			//assume invalid trip(trip with only invalid points)
			//if at least one valid point in the trip, keep the trip
			//otherwise delete it when done recording
			if(LocationFilter.isValid(location)){
				isValidTripToKeep = true;
			}
			//save location update to DB
			if(dh2 != null){
				dh2.insert(location, currentTid);
				stats.addOnePoint(location, false);
			} else{
				Log.e("antrip service2", "newLocationUpdate: dh2 is null whilst recording...wtf?");
			}
		}
	}
	
	private boolean isNetworkAvailable(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if(ni != null){ //in airplane mode, there are no "Active Network", networkInfo will be null
			return ni.isConnected();
		} else{
			return false;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return inMessenger.getBinder();
	}
	
	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		return true;
	}
	
	static public String getCurrentTripid(){
		return currentTid;
	}
	
	static public boolean isRecording(){
		return isRecording;
	}
}
