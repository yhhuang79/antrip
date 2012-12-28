package tw.plash.antrip.offline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FriendListActivityTEMP extends ExpandableListActivity{
	
	private FriendListAdapterTEMP adapter = null;
	private JSONArray friendrequestlist = null;
	private Context mContext;
	private SharedPreferences pref;
	private String userid;
	
	private ExpandableListView expListView;
	
	final private int REQUEST_CODE_SETTINGS = 1376;
	final private int REQUEST_CODE_FRIEND_FINDER = 1380;
	final private int REQUEST_CODE_FRIEND_REQUEST = 1381;
	
	private boolean stillLoading;
	
	private ImageButton dropdownList;
	private ImageButton addFriendBtn;
	private ImageButton pendingFriendRequestBtn;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		
		Log.w("triplistactivity", "on config change");
		
		super.onConfigurationChanged(newConfig);
		
		setContentView(R.layout.friendlisttemp);
		
		initFriendList();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.friendlisttemp);
		
		mContext = this;
		
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		userid = pref.getString("userid", "-1");
		
		initFriendList();
	}
	
	private void initFriendList() {
		
		pendingFriendRequestBtn = (ImageButton) findViewById(R.id.actionbar_action_button_left);
		
		addFriendBtn = (ImageButton) findViewById(R.id.actionbar_action_button_right);
		addFriendBtn.setVisibility(View.VISIBLE);
		addFriendBtn.setImageResource(R.color.button_state_addfriend);
		addFriendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(userid.equals("-1")){
					Toast.makeText(mContext, R.string.toast_addfriendnotloginyet, Toast.LENGTH_SHORT).show();
				} else{
					//new add friends activity
					startActivityForResult(new Intent(mContext, FriendFinder.class), REQUEST_CODE_FRIEND_FINDER);
				}
			}
		});
		
		((TextView) findViewById(R.id.actionbar_activity_title)).setText(R.string.dropdown_friendlist);
		
		dropdownList = (ImageButton) findViewById(R.id.actionbar_activity_icon);
		dropdownList.setImageResource(R.drawable.dropdown_friendlist);
		dropdownList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new DropdownFunctionList(v).showLikePopDownMenu();
			}
		});
		
		expListView = (ExpandableListView) findViewById(android.R.id.list);
		
		expListView.setEmptyView(findViewById(android.R.id.empty));
		
		expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				
				//should load the trip data from server and show it in trip viewer
				JSONObject obj = (JSONObject) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
				if(obj != null){
					try {
						startActivity(new Intent(mContext, GMapViewer.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("tripname", obj.getString("trip_name")).putExtra("hash", obj.getString("hash")));
					} catch (JSONException e) {
						e.printStackTrace();
						Toast.makeText(mContext, R.string.toast_cannotviewtrip, Toast.LENGTH_SHORT).show();
					}
				} else{
					Toast.makeText(mContext, R.string.toast_cannotviewtrip, Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
		
		expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				if(!parent.isGroupExpanded(groupPosition)){
					//group is collapsed at this click
					//get userid and friend id and load from getauthdatacomp
					FriendTripInfoAsyncLoader loader = new FriendTripInfoAsyncLoader(parent, groupPosition, id);
					loader.execute();
					return true;
				}
				return false;
			}
		});
		
		loadFriendList();
	}
	
	private class FriendTripInfoAsyncLoader extends AsyncTask<Void, Void, Boolean>{
		
		private ProgressDialog diag;
		final private ExpandableListView elv;
		final private int position;
		final private long gid;
		String fid = "-1";
		
		public FriendTripInfoAsyncLoader(ExpandableListView elv, int position, long gid) {
			this.elv = elv;
			this.position = position;
			this.gid = gid;
			try {
				this.fid = ((JSONObject)elv.getExpandableListAdapter().getGroup(position)).getString("id");
			} catch (JSONException e) {
				e.printStackTrace();
			}
//			Log.i("constructor", "position= " + position + ", gid= " + gid + ", fid= " + fid);
		}
		
		@Override
		protected void onPreExecute() {
			if(fid.equals("-1")){
				cancel(true);
			} else{
				diag = new ProgressDialog(mContext);
				diag.setCancelable(false);
				diag.setMessage(mContext.getResources().getString(R.string.universal_loading)); 
				diag.setIndeterminate(true);
				diag.show();
			}
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			if(((FriendListAdapterTEMP) elv.getExpandableListAdapter()).isChildDataEntryExist(gid)){
				//already have data
				return true;
			} else if (isNetworkAvailable(mContext)) {
				if (fid.equals("0")) {
					String url = "http://plash2.iis.sinica.edu.tw/api/GetPublicTripInfo.php";
					
					HttpGet getRequest = new HttpGet(url);
					
					HttpParams httpParameters = new BasicHttpParams();
					// Set the timeout in milliseconds until a connection is
					// established.
					// The default value is zero, that means the timeout is
					// not
					// used.
					HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
					// Set the default socket timeout (SO_TIMEOUT)
					// in milliseconds which is the timeout for waiting for
					// data.
					int timeoutSocket = 10000;
					HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
					
					DefaultHttpClient client = new DefaultHttpClient(httpParameters);
					try {
						HttpResponse response;
						
						if (!isCancelled()) {
							response = client.execute(getRequest);
						} else {
							return false;
						}
						
						Integer statusCode = response.getStatusLine().getStatusCode();
						if (statusCode == 200) {
							
							BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity()
									.getContent()));
							JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
							in.close();
							
							JSONArray info = result.getJSONArray("tripInfoList");
//							Log.i("info", "info=" + info.toString());
							if (info != null) {
								((FriendListAdapterTEMP) elv.getExpandableListAdapter()).addChildData(gid, info);
							}
							
							return true;
						}
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					} finally {
						client.getConnectionManager().shutdown();
					}
				} else {
					String url = "http://plash2.iis.sinica.edu.tw/api/GetTripAuthData.php?userid=" + fid
							+ "&friend_id=" + userid;
//					Log.i("url", "url= " + url);
					HttpGet getRequest = new HttpGet(url);
					
					HttpParams httpParameters = new BasicHttpParams();
					// Set the timeout in milliseconds until a connection is
					// established.
					// The default value is zero, that means the timeout is
					// not
					// used.
					HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
					// Set the default socket timeout (SO_TIMEOUT)
					// in milliseconds which is the timeout for waiting for
					// data.
					int timeoutSocket = 10000;
					HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
					
					DefaultHttpClient client = new DefaultHttpClient(httpParameters);
					try {
						HttpResponse response;
						
						if (!isCancelled()) {
							response = client.execute(getRequest);
						} else {
							return false;
						}
						
						Integer statusCode = response.getStatusLine().getStatusCode();
						if (statusCode == 200) {
							
							BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity()
									.getContent()));
							JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
							in.close();
							
							JSONArray info = result.getJSONArray("tripInfo");
//							Log.i("info", "info=" + info.toString());
							if (info != null) {
								((FriendListAdapterTEMP) elv.getExpandableListAdapter()).addChildData(gid, info);
							}
							
							return true;
						}
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					} finally {
						client.getConnectionManager().shutdown();
					}
				}
			}
			return false;
		}
		
		@Override
		protected void onCancelled() {
			diag.dismiss();
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			diag.dismiss();
			if(result){
				elv.expandGroup(position);
			}
		}
	}
	
	private void loadFriendList(){
		Log.i("friend list", "loadFriendList");
		if(!stillLoading){
			Log.i("friend list", "loadFriendList, stillloading false, proceed");
			flal = new FriendListAsyncLoader();
			flal.execute();
		} else{
			Log.i("friend list", "loadFriendList, stillloading true, do not proceed");
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("friend list", "ondestroy");
		if(stillLoading){
			flal.cancel(true);
		}
	}
	
	private FriendListAsyncLoader flal;
	
	private class FriendListAsyncLoader extends AsyncTask<Void, Void, Integer>{
		
		final private int ADAPTER_NOT_NULL = 0;
		final private int ADAPTER_IS_NULL_NO_USERID = 1;
		final private int ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET = 2;
		final private int ADAPTER_IS_NULL_HAS_USERID_HAS_INTERNET_EXCEPTION = 3;
		final private int ADAPTER_IS_NULL_HAS_USERID_HAS_INTERNET_CONNECTION_ERROR = 4;
		final private int ADAPTER_IS_NULL_HAS_USERID_HAS_INTERNET_CONNECTION_GOOD_NO_DATA = 5;
		final private int ADAPTER_IS_NULL_HAS_USERID_HAS_INTERNET_CONNECTION_GOOD_HAS_DATA = 6;
		
		private JSONArray friendlist;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			((TextView) expListView.getEmptyView().findViewById(R.id.emptytext)).setText(R.string.universal_loading);
			
			stillLoading = true;
			
			friendlist = null;
			friendrequestlist = null;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			if (adapter != null) {
				return ADAPTER_NOT_NULL;
			} else {
				// now try to get remote tripinfo
				// but first check if user is logged in or not
				if (userid.equals("-1")) {
					// prompt user to login
					return ADAPTER_IS_NULL_NO_USERID;
				} else {
					// user id logged in, can try to get remote tripinfo
					// first check if internet is available
					if (isNetworkAvailable(mContext)) {
						// internet is available, get remote tripinfo
						
						// try to get friend request list
						
						String url = "http://plash2.iis.sinica.edu.tw/api/GetFriendRequestList.php?sid=" + userid;
						
//						Log.i("getfriendrequest", "url=" + url);
						
						HttpGet getRequest = new HttpGet(url);
						
						HttpParams httpParameters = new BasicHttpParams();
						// Set the timeout in milliseconds until a connection is
						// established.
						// The default value is zero, that means the timeout is
						// not used.
						HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
						// Set the default socket timeout (SO_TIMEOUT)
						// in milliseconds which is the timeout for waiting for
						// data.
						int timeoutSocket = 10000;
						HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
						
						DefaultHttpClient client = new DefaultHttpClient(httpParameters);
						try {
							HttpResponse response = client.execute(getRequest);
							
							Integer statusCode = response.getStatusLine().getStatusCode();
							if (statusCode == 200) {
								
								BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity()
										.getContent()));
								JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
								in.close();
								
								friendrequestlist = result.getJSONArray("friendrequest_list");
								
							}
						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
						url = "http://plash2.iis.sinica.edu.tw/api/GetFriendList.php?sid=" + userid;
						
						getRequest = new HttpGet(url);
						try {
							HttpResponse response = client.execute(getRequest);
							
							Integer statusCode = response.getStatusLine().getStatusCode();
							if (statusCode == 200) {
								
								BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity()
										.getContent()));
								JSONObject result = new JSONObject(new JSONTokener(in.readLine().replace("({", "{")
										.replace("});", "}")));
								in.close();
								
								friendlist = result.getJSONArray("friend_list");
								
								if (friendlist != null && friendlist.length() > 0) {
									return ADAPTER_IS_NULL_HAS_USERID_HAS_INTERNET_CONNECTION_GOOD_HAS_DATA;
								} else {
									return ADAPTER_IS_NULL_HAS_USERID_HAS_INTERNET_CONNECTION_GOOD_NO_DATA;
								}
							} else {
								return ADAPTER_IS_NULL_HAS_USERID_HAS_INTERNET_CONNECTION_ERROR;
							}
							
						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						} finally {
							client.getConnectionManager().shutdown();
						}
						return ADAPTER_IS_NULL_HAS_USERID_HAS_INTERNET_EXCEPTION;
					} else {
						return ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET;
					}
				}
			}
		}

		@Override
		protected void onCancelled() {
			stillLoading = false;
		}
		
		protected void onPostExecute(Integer result) {
			switch(result){
			case ADAPTER_IS_NULL_NO_USERID:
				pendingFriendRequestBtn.setVisibility(View.GONE);
				adapter = new FriendListAdapterTEMP(mContext, null);
				expListView.setAdapter(adapter);
				((TextView) expListView.getEmptyView().findViewById(R.id.emptytext)).setText(R.string.friendlist_emptyview);
				((ProgressBar) expListView.getEmptyView().findViewById(R.id.emptyprogressbar)).setVisibility(View.GONE);
				break;
			case ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET:
			case ADAPTER_IS_NULL_HAS_USERID_HAS_INTERNET_EXCEPTION:
			case ADAPTER_IS_NULL_HAS_USERID_HAS_INTERNET_CONNECTION_ERROR:
			case ADAPTER_IS_NULL_HAS_USERID_HAS_INTERNET_CONNECTION_GOOD_NO_DATA:
				if(friendrequestlist != null && friendrequestlist.length() > 0){
					pendingFriendRequestBtn.setImageResource(R.color.button_state_friendrequestnotification_notempty);
					pendingFriendRequestBtn.setVisibility(View.VISIBLE);
					pendingFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(friendrequestlist != null && friendrequestlist.length() > 0){
								//you have friend request pending
								startActivityForResult(new Intent(mContext, FriendRequest.class).putExtra("friendrequest", friendrequestlist.toString()), REQUEST_CODE_FRIEND_REQUEST);
							} else{
								Toast.makeText(mContext, R.string.toast_no_pending_friendreqiest, Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
				//no data
				adapter = new FriendListAdapterTEMP(mContext, null);
				expListView.setAdapter(adapter);
				((TextView) expListView.getEmptyView().findViewById(R.id.emptytext)).setText(R.string.trip_list_empty_actual);
				((ProgressBar) expListView.getEmptyView().findViewById(R.id.emptyprogressbar)).setVisibility(View.GONE);
				break;
			case ADAPTER_IS_NULL_HAS_USERID_HAS_INTERNET_CONNECTION_GOOD_HAS_DATA:
				//has data
				adapter = new FriendListAdapterTEMP(mContext, friendlist);
				//don't break, need to set adapter
			case ADAPTER_NOT_NULL:
				//adapter not null
				if(friendrequestlist != null && friendrequestlist.length() > 0){
					pendingFriendRequestBtn.setImageResource(R.color.button_state_friendrequestnotification_notempty);
					pendingFriendRequestBtn.setVisibility(View.VISIBLE);
					pendingFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if(friendrequestlist != null && friendrequestlist.length() > 0){
								//you have friend request pending
								startActivityForResult(new Intent(mContext, FriendRequest.class).putExtra("friendrequest", friendrequestlist.toString()), REQUEST_CODE_FRIEND_REQUEST);
							} else{
								Toast.makeText(mContext, R.string.toast_no_pending_friendreqiest, Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
				expListView.setAdapter(adapter);
				if(!adapter.isEmpty()){
					break;
				}
			default:
				((TextView) expListView.getEmptyView().findViewById(R.id.emptytext)).setText(R.string.trip_list_empty_actual);
				((ProgressBar) expListView.getEmptyView().findViewById(R.id.emptyprogressbar)).setVisibility(View.GONE);
				break;
			}
			stillLoading = false;
//			flal = null;
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_settings:
			startActivityForResult(new Intent(mContext, Settings.class), REQUEST_CODE_SETTINGS);
			return true;
//		case R.id.menu_help:
//			Toast.makeText(mContext, R.string.toast_theres_no_help, Toast.LENGTH_SHORT).show();
//			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_CODE_SETTINGS:
		case REQUEST_CODE_FRIEND_FINDER:
		case REQUEST_CODE_FRIEND_REQUEST:
			userid = pref.getString("userid", "-1");
			Log.i("friend list", "on act result, userid=" + userid);
			switch(resultCode){
			case RESULT_CANCELED:
				Log.e("friend list", "on act result, cancel/first/default");
			case RESULT_OK:
				Log.e("friend list", "on act result, ok");
				if(!stillLoading){
					adapter = null;
					pendingFriendRequestBtn.setVisibility(View.GONE);
					loadFriendList();
				}
				break;
			case RESULT_FIRST_USER:
			default:
				//do nothing
				break;
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}
}
