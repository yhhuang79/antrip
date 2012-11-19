package tw.plash.antrip.offline;

import android.location.Location;

public interface LocationPublisher {
	/**
	 * 
	 * @param location
	 */
	public void newLocationUpdate(Location location);
}
