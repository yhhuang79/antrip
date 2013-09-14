package tw.plash.antrip.offline;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MarkerOptionsCreator;

public class AntripMarker extends MarkerOptionsCreator {
	
	private MarkerOptions mo;
	
	public AntripMarker(MarkerOptions mo) {
		this.mo = mo;
	}
	
	
}
