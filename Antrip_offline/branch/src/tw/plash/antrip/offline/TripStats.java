package tw.plash.antrip.offline;

import java.sql.Timestamp;

import tw.plash.antrip.offline.utility.LocationFilter;

import android.location.Location;

public class TripStats {
	//time when start record button is pushed
	private Long buttonStartTime;
	//time of the LAST inaccurate point
	private Long lastInaccuratePointTime;
	//time of the GOOD good point
	private Long lastGoodPointTime;
	//time when end record button is pushed
	private Long buttonEndTime;
	//in meters
	private final Double radius = 6371008.7714;
	//in meters
	private Double totalValidLength;
	//total points
	private Integer totalPointCount;
	// exclude -999 points
	private Integer totalValidPointCount;
	// exclude accuracy > threshold
	private Integer totalAccuratePointCount;
	// regardless of accuracy
	private Integer totalCheckinPointCount;
	//holder
	private Location previousLoc;
	
	public TripStats() {
		buttonStartTime = null;
		lastInaccuratePointTime = null;
		lastGoodPointTime = null;
		buttonEndTime = null;
		
		totalValidLength = 0.0;
		totalPointCount = 0;
		totalValidPointCount = 0;
		totalAccuratePointCount = 0;
		totalCheckinPointCount = 0;
		
		previousLoc = null;
	}
	
	synchronized public void addOnePoint(Location loc, boolean checkin){
		//don't proceed if null was inputted
		if(loc != null){
			//input not null, add one count
			totalPointCount += 1;
			//if previous location was not null, it is not the first time adding point
			if(previousLoc != null){
				if(checkin){
					//is a check-in point, skip all validity check
					totalCheckinPointCount += 1;
					//regardless of accuracy, check-in points are to be kept in DB
					totalValidLength += greatCircleDistance(previousLoc, loc);
					//replace the location in holder with current location
					previousLoc = loc;
				} else{
					// not a check-in point, check for validity and accuracy
					if(LocationFilter.isValid(loc)){
						totalValidPointCount += 1;
						lastInaccuratePointTime = loc.getTime();
						if(LocationFilter.isAccurate(loc)){
							totalAccuratePointCount += 1;
							lastGoodPointTime = loc.getTime();
							//input location is valid, calculate the distance
							totalValidLength += greatCircleDistance(previousLoc, loc);
							//replace the location in holder with current location
							previousLoc = loc;
						} else{
							//inaccurate points
						}
					} else{
						//invalid points
					}
				}
			} else{
				//previous location was null, first time adding point
				previousLoc = loc;
			}
		}
	}
	
	private Double greatCircleDistance(Location one, Location two){
		Double dlat = toRad(two.getLatitude() - one.getLatitude());
		Double dlon = toRad(two.getLongitude() - one.getLongitude());
		Double latone = toRad(one.getLatitude());
		Double lattwo = toRad(two.getLatitude());
		Double a = Math.sin(dlat/2) * Math.sin(dlat/2) + Math.sin(dlon/2) * Math.sin(dlon/2) * Math.cos(latone) * Math.cos(lattwo);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return radius * c;
	}
	
	private Double toRad(Double degree){
		return degree/180*Math.PI;
	}

	public Long getNonNullEndTime(){
		if(buttonEndTime != null){
			return buttonEndTime;
		} else if(lastInaccuratePointTime != null){
			return lastInaccuratePointTime;
		} else{
			//no further value available, must return a value
			return lastGoodPointTime;
		}
	}
	
	public Long getButtonStartTime() {
		return buttonStartTime;
	}

	public void setButtonStartTime(Long buttonStartTime) {
		this.buttonStartTime = buttonStartTime;
	}

	public Long getLastInaccuratePointTime() {
		return lastInaccuratePointTime;
	}

	public Long getLastGoodPointTime() {
		return lastGoodPointTime;
	}

	public Long getButtonEndTime() {
		return buttonEndTime;
	}

	public void setButtonEndTime(Long buttonEndTime) {
		this.buttonEndTime = buttonEndTime;
	}

	public Double getTotalValidLength() {
		return totalValidLength;
	}

	public Integer getTotalPointCount() {
		return totalPointCount;
	}

	public Integer getTotalValidPointCount() {
		return totalValidPointCount;
	}

	public Integer getTotalAccuratePointCount() {
		return totalAccuratePointCount;
	}

	public Location getPreviousLoc() {
		return previousLoc;
	}
}
