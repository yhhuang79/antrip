package tw.plash.antrip.offline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.emilsjolander.components.StickyListHeaders.StickyListHeadersListView;

public class TripListActivity4 extends Activity implements TripListReloader{
	
	private TripListAdapter2 adapter = null;
	private Context mContext;
//	private SharedPreferences pref;
	
	private boolean stillLoading;
	
	private StickyListHeadersListView stickyList;
	
	private ImageButton dropdownList;
	
	private int positionHolder = -1;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		
		Log.w("triplistactivity", "on config change");
		
		super.onConfigurationChanged(newConfig);
		
		setContentView(R.layout.triplist2);
		
		initTripList();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.triplist2);
		
		mContext = this;
		
//		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		initTripList();
	}
	
	private void initTripList() {
		
		dropdownList = (ImageButton) findViewById(R.id.actionbar_activity_icon);
		dropdownList.setImageResource(R.drawable.dropdown_triplist_pressed);
		dropdownList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new DropdownFunctionList(v).showLikePopDownMenu();
			}
		});
		
		stickyList = (StickyListHeadersListView) findViewById(R.id.triplist2);
		
		stickyList.setEmptyView(findViewById(android.R.id.empty));
		
		stickyList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				final JSONObject obj = (JSONObject) parent.getItemAtPosition(position);
				new AlertDialog.Builder(mContext).setItems(new String[] { "upload", "delete" },
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							int pendingPosition = -1;
							switch (which) {
							case 0:
								// upload
								// start upload thread, can try to lock this
								// entry, overlapped with progress bar
								// should maybe run as background
								// service...?
								// or maybe there should be a persistent
								// background service, and run upload thread
								// XXX
								// pass position to upload thread for
								// reference
								
								//show number of rows...
							{
								DBHelper2 dh2 = new DBHelper2(mContext);
								try {
									new AlertDialog.Builder(mContext)
										.setTitle("hiyooooo")
										.setMessage("upload function coming soon...\nhere's the total number of records in this trip: " + dh2.getNumberofPoints(obj.getString("trip_id")))
										.setNeutralButton("I see...", null)
										.show();
								} catch (JSONException e) {
									e.printStackTrace();
								}
								dh2.closeDB();
								dh2 = null;
							}
								break;
							case 1:
							{
								// delete
								// this is a deletion from local DB
								DBHelper2 dh2 = new DBHelper2(mContext);
								try {
									if (dh2.deleteLocalTrip(obj.getString("trip_id")) > 0) {
										pendingPosition = position;
										Toast.makeText(mContext, "\"" + obj.getString("trip_name") + "\" deleted!", Toast.LENGTH_LONG).show();
									} else{
										pendingPosition = -1;
										Toast.makeText(mContext, "\"" + obj.getString("trip_name") + "\" cannot be deleted!", Toast.LENGTH_LONG).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
								dh2.closeDB();
								dh2 = null;
								loadTripList(pendingPosition);
							}
								break;
							default:
								break;
							}
						}
					}
				)
				.show();
				return false;
			}
		});
		
		stickyList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//XXX
				//open the trip
				Toast.makeText(mContext, "function coming soon...", Toast.LENGTH_SHORT).show();
			}
		});
		
		loadTripList(-1);
	}
	
	private void loadTripList(int position){
		tlal = new TripListAsyncLoader(position);
		tlal.execute();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(stillLoading){
			tlal.cancel(true);
		}
	}
	
	private TripListAsyncLoader tlal;
	
	private class TripListAsyncLoader extends AsyncTask<Void, Void, Integer>{
		
		final private int ADAPTER_NOT_NULL = 0;
		final private int ADAPTER_NOT_NULL_STUFF_TO_REMOVE = 1;
		
		final private int ADAPTER_IS_NULL = 2;
		
		private JSONArray localtripinfo;
		
		final int pendingRemovePosition;
		
		public TripListAsyncLoader(int position) {
			pendingRemovePosition = position;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			((TextView) stickyList.getEmptyView()).setText("Loading...");
			
			stillLoading = true;
			
			localtripinfo = null;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			if(adapter != null){
				if(pendingRemovePosition > -1){
					return ADAPTER_NOT_NULL_STUFF_TO_REMOVE;
				} else{
					return ADAPTER_NOT_NULL;
				}
			} else {
				DBHelper2 dh2 = new DBHelper2(mContext);
				// get current tid from service
				if (AntripService2.isRecording()) {
					localtripinfo = dh2.getAllTripInfoForHTML2(AntripService2.getCurrentTripid());
				} else {
					localtripinfo = dh2.getAllTripInfoForHTML2(null);
				}
				
				dh2.closeDB();
				dh2 = null;
				return ADAPTER_IS_NULL;
			}
		}

		@Override
		protected void onCancelled() {
			//not sure what needs to be done here
		}
		
		protected void onPostExecute(Integer result) {
			switch(result){
			case 2:
				if(localtripinfo != null){
					adapter = new TripListAdapter2(mContext, null, localtripinfo);
					stickyList.setAdapter(adapter);
				}else{
					((TextView) stickyList.getEmptyView()).setText("Nothing to see here...");
				}
				break;
			case ADAPTER_NOT_NULL_STUFF_TO_REMOVE:
				adapter.remove(pendingRemovePosition);
				
			case ADAPTER_NOT_NULL:
				//adapter not null
				stickyList.setAdapter(adapter);
				if(!adapter.isEmpty()){
					break;
				}
			default:
				((TextView) stickyList.getEmptyView()).setText("Nothing to see here...");
				break;
			}
			stillLoading = false;
		};
	}
	
	private boolean isNetworkAvailable(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if(ni != null){ //in airplane mode, there are no "Active Network", networkInfo will be null
			return ni.isConnected();
		} else{
			return false;
		}
	}

	@Override
	public void shouldReloadTripList(int pos) {
		loadTripList(pos);
	}
}
