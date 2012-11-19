package tw.plash.antrip.offline;

/**
 * Simple data class for cached points(drawing cache)
 * 
 * @author CSZU
 * 
 */
public class CachedPoints {
	public final double latitude;
	public final double longitude;
	public final int transportMode;
	public final boolean marker;
	
	public CachedPoints(double latitude, double longitude, int transportMode, boolean marker) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.transportMode = transportMode;
		this.marker = marker;
	}

	public CachedPoints(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.transportMode = -2;
		this.marker = false;
	}
}
