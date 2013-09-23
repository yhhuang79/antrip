package tw.plash.antrip.offline.trip;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TripListPagerAdapter extends FragmentPagerAdapter {
	
	public TripListPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		switch(position){
		case 1: //friends' trips
			return new ShareTripsFragment(); //XXX
		case 2: //public trips
			return new PublicTripsFragment(); //XXX
		case 0: //my trips
		default: //not sure when will this happen
			return new MyTripsFragment();
		}
	}
	
	@Override
	public int getCount() {
		return 3;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		switch(position){
		case 0:
		default:
			return "My Trips";
		case 1:
			return "Friends' Trips";
		case 2:
			return "Public Trips";
		}
	}
}
