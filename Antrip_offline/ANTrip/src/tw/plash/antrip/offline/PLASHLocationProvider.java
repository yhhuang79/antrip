package tw.plash.antrip.offline;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class PLASHLocationProvider {
	
	static final int LOCATE_MODE_ACCURATE = 0;
	static final int LOCATE_MODE_EFFICIENT = 1;
	static final int LOCATE_MODE_AUTO = 2;
	
	private Context mContext;
	private LocationPublisher locationPublisher;
	private SharedPreferences pref;
	private LocationManager locationManager;
	private SensorManager sensorManager;
	
	public PLASHLocationProvider(Context c, LocationPublisher lp) {
		mContext = c;
		locationPublisher = lp;
		
		pref = PreferenceManager.getDefaultSharedPreferences(c);
		
		locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
		
		sensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
		
	}
	
	public void run(){
		if(pref != null){
			switch(pref.getInt("LOCATE_MODE", LOCATE_MODE_AUTO)){
			case LOCATE_MODE_ACCURATE:
				break;
			case LOCATE_MODE_EFFICIENT:
				break;
			case LOCATE_MODE_AUTO:
				break;
			default:
				Log.e("", "");
				break;
			}
		}
	}
}
