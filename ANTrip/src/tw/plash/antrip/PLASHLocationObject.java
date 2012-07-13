package tw.plash.antrip;

import java.io.Serializable;

public class PLASHLocationObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4143774512396343448L;

	private final int id;
	private final double latitude;
	private final double longitude;
	private final String time;
	private final String altitude;
	private final String speed;
	private final String bearing;
	private final String accuracy;
	private final String userID;
	private final String tripID;

	public PLASHLocationObject(int id, double latitude, double longitude, String time,
			String altitude, String speed, String bearing, String accuracy, String userID,
			String tripID) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.time = time;
		this.altitude = altitude;
		this.speed = speed;
		this.bearing = bearing;
		this.accuracy = accuracy;
		this.userID = userID;
		this.tripID = tripID;
	}

	public int getId() {
		return id;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getAltitude() {
		return altitude;
	}

	public String getAccuracy() {
		return accuracy;
	}

	public String getSpeed() {
		return speed;
	}

	public String getBearing() {
		return bearing;
	}

	public String getUserID() {
		return userID;
	}

	public String getTime() {
		return time;
	}

	public String getTripID() {
		return tripID;
	}
}
