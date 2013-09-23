package tw.plash.antrip.offline.user;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class UserListPagerAdapter extends FragmentPagerAdapter {
	
	public UserListPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		switch(position){
		case 1: //friends
			return new FriendListFragment();
		case 0: //my info
		default: //not sure when will this happen
			return new MyInfoFragment();
		}
	}
	
	@Override
	public int getCount() {
		return 2;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		switch(position){
		case 0:
		default:
			return "My Info";
		case 1:
			return "My Friends";
		}
	}
}
