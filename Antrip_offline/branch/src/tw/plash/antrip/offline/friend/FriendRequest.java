package tw.plash.antrip.offline.friend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import tw.plash.antrip.offline.InvalidateViewsCallback;
import tw.plash.antrip.offline.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendRequest extends Activity implements InvalidateViewsCallback{
	
	private Context mContext;
	
	private FriendRequestListAdapter adapter;
	private ListView listview;
	
	private SharedPreferences pref;
	private String userid;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		setContentView(R.layout.friendrequest);
		
		initRequestList();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.friendrequest);
		
		mContext = this;
		
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		userid = pref.getString("userid", "-1"); //XXX
		
		adapter = null;
		
		initRequestList();
	}
	
	private void initRequestList(){
		
		((TextView) findViewById(R.id.actionbar_activity_title)).setText(R.string.title_friendrequest);
		
		ImageButton btn = (ImageButton) findViewById(R.id.actionbar_activity_icon);
		btn.setImageResource(R.color.button_state_goback);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		listview = (ListView) findViewById(R.id.friendfinder_listview);
		
		if(getIntent() != null){
			if(getIntent().getExtras() != null){
				String jsonarraystring = getIntent().getExtras().getString("friendrequest");
				if(jsonarraystring != null){
					JSONArray array = null;
					try {
						array = new JSONArray(new JSONTokener(jsonarraystring));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if(array != null){
						adapter = new FriendRequestListAdapter(mContext, array, FriendRequest.this, userid);
						listview.setAdapter(adapter);
					}
				}
			}
		}
		//show empty if anyhting goes wrong...
		listview.setEmptyView(findViewById(android.R.id.empty));
	}

	@Override
	public void requestViewsInvalidation() {
		if(listview != null){
			listview.invalidateViews();
		}
	}
	
	@Override
	public void removePosition(int position) {
		if(listview != null && listview.getChildCount() > position){
			adapter.removeItem(position);
			listview.setAdapter(adapter);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		setResult(RESULT_OK);
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.nonactivity_menu, menu);
//		return true;
//	}
//	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch(item.getItemId()){
//		case R.id.menu_help:
//			Toast.makeText(mContext, "there's no help...", Toast.LENGTH_SHORT).show();
//			return true;
//		default:
//			return super.onOptionsItemSelected(item);
//		}
//	}
}
