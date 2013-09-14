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
	public final String timestamp;
	
	public CachedPoints(double latitude, double longitude, String timestamp) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.timestamp = timestamp;
	}
	
	public CachedPoints(double latitude, double longitude) {
		this(latitude, longitude, null);
	}
}
