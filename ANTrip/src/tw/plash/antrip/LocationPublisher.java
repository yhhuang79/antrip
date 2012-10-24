package tw.plash.antrip;

import android.location.Location;

public interface LocationPublisher {
	/**
	 * 
	 * @param location
	 */
	public void newLocationUpdate(Location location);
}
