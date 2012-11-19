package tw.plash.antrip.offline;

import android.location.Location;

public class LocationFilter {
	/**
	 * test if input location object is within ACCURACY_THRESHOLD
	 * @param inputLocation
	 * @return true/false
	 */
	static public boolean accuracyFilter(Location inputLocation){
		final int ACCURACY_THRESHOLD = 1500;
		//make sure input parameter is not null
		if(inputLocation != null){
			//make sure provider column is not null nor "null" provider, as seen in null location case
			if(!inputLocation.getProvider().isEmpty() && !inputLocation.getProvider().equalsIgnoreCase("null")){
				//test if accuracy of input location object is within accuracy threshold
				if(inputLocation.getAccuracy() < ACCURACY_THRESHOLD){
					return true;
				}
			}
		}
		return false;
	}
	
	static public boolean validityFilter(Location inputLocation){
		final int VALIDITY_THRESHOLD = -999;
		if(inputLocation != null){
			//at least input is not null
			if(!inputLocation.getProvider().isEmpty() && !inputLocation.getProvider().equalsIgnoreCase("null")){
				//location provider value should not be empty or "null"
				if(inputLocation.getLatitude() > VALIDITY_THRESHOLD){
					//latitude value is larger then -999, meaning -998~infinity, thus assume valid
					//we mark invalid location with latitude value of -999
					return true;
				}
			}
		}
		return false;
	}
	
	static public void something(){
		
	}
}
