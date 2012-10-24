package tw.plash.antrip;

import java.util.Date;

import android.content.Context;
import android.location.Location;

import com.skyhookwireless.wps.WPSAuthentication;
import com.skyhookwireless.wps.WPSContinuation;
import com.skyhookwireless.wps.WPSLocation;
import com.skyhookwireless.wps.WPSPeriodicLocationCallback;
import com.skyhookwireless.wps.WPSReturnCode;
import com.skyhookwireless.wps.XPS;

public class SkyhookLocation{
	
	private XPS xps;
	private WPSAuthentication auth;
	private boolean isRunning;
	
	private LocationPublisher locPublisher;
	
	private Location lastNonNullLocation;
	
	public SkyhookLocation(Context mContext, LocationPublisher lp) {
		xps = new XPS(mContext);
		auth = new WPSAuthentication("plash", "iis.sinica.edu.tw");
		isRunning = false;
		lastNonNullLocation = null;
		locPublisher = lp;
	}
	
	/**
	 * start retrieving skyhook xps periodic location with the given time interval
	 * will send fixed location value to AntripService(both valid and null(-999) location)
	 * if this method is called when xps is already running, will update the time interval with the new value and restart
	 * @param timeIntervalInSeconds
	 */
	public void run(int timeIntervalInSeconds) {
		if(xps!= null){
			//XXX
			//should check the value of input time interval, see if it lies in reasonable range
			if(isRunning){
				//is already running, update the time interval and restart
				xps.abort();
				xps.getXPSLocation(auth, timeIntervalInSeconds, XPS.EXACT_ACCURACY, locationCallback);
			} else{
				//first time running, set the time interval and start
				xps.getXPSLocation(auth, timeIntervalInSeconds, XPS.EXACT_ACCURACY, locationCallback);
				isRunning = true;
			}
		} else{
			//xps is null, this method might be called after cancel, or something else has gone south
		}
	}
	
	/**
	 * stop reporting skyhook location and nullify xps object to prevent further function calls
	 */
	public void cancel() {
		xps.abort();
		xps = null;
		isRunning = false;
	}
	
	/**
	 * Get the latest non-null location object, mostly for check-in purpose, I think...
	 * @return latest non-null location object, or null if no location fix has been acquired yet
	 */
	public Location getLastNonNullLocation(){
		return lastNonNullLocation;
	}
	
	private final skyhookPeriodicLocationCallback locationCallback = new skyhookPeriodicLocationCallback();
	
	public class skyhookPeriodicLocationCallback implements WPSPeriodicLocationCallback{
		
		private Location nullLocation;
		
		public skyhookPeriodicLocationCallback() {
			nullLocation = new Location("null");
			nullLocation.setLatitude(-999.0);
			nullLocation.setLongitude(-999.0);
			nullLocation.setAccuracy(-999f);
		}
		
		@Override
		public void done() {
			//not useful right now
		}
		
		@Override
		public WPSContinuation handleError(WPSReturnCode arg0) {
			//XXX
			nullLocation.setTime(new Date().getTime());
			//need to use interface instead of static methods
			locPublisher.newLocationUpdate(nullLocation);
			return WPSContinuation.WPS_CONTINUE;
		}
		
		@Override
		public WPSContinuation handleWPSPeriodicLocation(WPSLocation arg0) {
			//save the latest non null location object for future(check-in) references
			lastNonNullLocation = WPS2Location(arg0);
			//XXX
			//need to use interface instead of static methods
			locPublisher.newLocationUpdate(lastNonNullLocation);
			return WPSContinuation.WPS_CONTINUE;
		}
		
		private Location WPS2Location(WPSLocation loc){
			Location location = new Location("skyhook");
			location.setLatitude(loc.getLatitude());
			location.setLongitude(loc.getLongitude());
			//use cell phone time system for consistency purpose
			location.setTime(new Date().getTime());
			location.setAltitude(loc.getAltitude());
			location.setSpeed((float)loc.getSpeed());
			location.setBearing((float)loc.getBearing());
			location.setAccuracy(loc.getHPE());
			return location;
		}
	}
}
