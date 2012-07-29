package tw.plash.antrip;

import android.location.Location;

public class TripStats {
	//in meters
	private final Double radius = 6371008.7714;
	//in meters
	private Double totalLength;
	private Integer totalPointCount;
	//holder
	private Location previousLoc;
	
	public TripStats() {
		totalLength = 0.0;
		totalPointCount = 0;
		previousLoc = null;
	}
	
	public void addOnePoint(Location loc){
		//don't proceed if null was inputted
		if(loc != null){
			//if previous location was not null, it is not the first time adding point
			if(previousLoc != null){
				//lat & lon should both be greater than the null location value
				if(loc.getLatitude() > -999.0 && loc.getLongitude() > -999.0){
					//input location is valid, calculate the distance
					totalLength += greatCircleDistance(previousLoc, loc);
					//replace the location in holder with current location
					previousLoc = loc;
				}
			} else{
				//previous location was null, first adding point
				previousLoc = loc;
			}
			//input not null, add one count
			totalPointCount += 1;
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
	
	public Double getLength(){
		return totalLength;
	}
	
	public Integer getCount(){
		return totalPointCount;
	}
}
