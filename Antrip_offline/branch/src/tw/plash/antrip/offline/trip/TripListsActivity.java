package tw.plash.antrip.offline.trip;

import tw.plash.antrip.offline.R;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TripListsActivity extends ActionBarActivity implements TabListener {
	
	private ViewPager viewPager;
	private TripListPagerAdapter pagerAdapter;
	private ActionBarDrawerToggle toggle;
	private DrawerLayout drawerlayout;
	private ListView drawerlist;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.triplistactivity);
		
		pagerAdapter = new TripListPagerAdapter(getSupportFragmentManager());
		
		drawerlayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerlist = (ListView) findViewById(R.id.left_drawer);
		
		drawerlayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawerlist.setAdapter(new ArrayAdapter<String>(
				this, 
				R.layout.drawer_list_item, 
				new String[] { "Trips", "Map", "Friends", "Settings", "About" }));
		drawerlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Toast.makeText(getApplicationContext(), "clicked: " + drawerlist.getItemAtPosition(arg2), Toast.LENGTH_SHORT).show();
				drawerlist.setItemChecked(arg2, true);
				drawerlayout.closeDrawer(drawerlist);
			}
		});
		toggle = new ActionBarDrawerToggle(
				this, 
				drawerlayout, 
				R.drawable.ic_drawer, 
				R.string.drawer_open,
				R.string.drawer_close);
		drawerlayout.setDrawerListener(toggle);
		
		final ActionBar actionBar = getSupportActionBar();
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		for(int i = 0; i < pagerAdapter.getCount(); i++){
			actionBar.addTab(actionBar.newTab().setText(pagerAdapter.getPageTitle(i)).setTabListener(this));
		}
		if (savedInstanceState == null) {
			drawerlist.setItemChecked(0, true);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (toggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		toggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		toggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
	}
	
	@Override
	public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
		viewPager.setCurrentItem(arg0.getPosition());
	}
	
	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
	}
}
