package tw.plash.antrip.offline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.OverScroller;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emilsjolander.components.StickyListHeaders.StickyListHeadersListView;

public class TripListActivity4 extends Activity implements TripListReloader{
	
	private TripListAdapter2 adapter = null;
	private Context mContext;
	private SharedPreferences pref;
	private String userid;
	
	private boolean stillLoading;
	
	private int JVM_HEAP_POINT_COUNT_LIMIT;
	
	final private int REQUEST_CODE_SETTINGS = 1379;
	
	private StickyListHeadersListView stickyList;
	
	private ImageButton dropdownList;
//	private ImageButton refreshBtn;
	
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
		
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		userid = pref.getString("userid", "-1");
		
		JVM_HEAP_POINT_COUNT_LIMIT = ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() * 60;
		
		initTripList();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_CODE_SETTINGS:
			userid = pref.getString("userid", "-1");
			switch(resultCode){
			case RESULT_CANCELED:
				Log.e("triplistact4", "onactresult, setting no good");
			case RESULT_OK:
				adapter = null;
				loadTripList(-1);
				Log.e("triplistact4", "onactresult, setting ok");
				break;
			case RESULT_FIRST_USER:
			default:
				break;
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}
	
	private void initTripList() {
		
		((TextView) findViewById(R.id.actionbar_activity_title)).setText(R.string.dropdown_triplist);
		
//		refreshBtn = (ImageButton) findViewById(R.id.actionbar_action_button_right);
//		refreshBtn.setVisibility(View.VISIBLE);
//		refreshBtn.setImageResource(0);
//		refreshBtn.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if(!stillLoading){
//					adapter = null;
//					loadTripList(-1);
//				}
//			}
//		});
		
		dropdownList = (ImageButton) findViewById(R.id.actionbar_activity_icon);
		dropdownList.setImageResource(R.drawable.dropdown_triplist);
		dropdownList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new DropdownFunctionList(v).showLikePopDownMenu();
			}
		});
		
		stickyList = (StickyListHeadersListView) findViewById(R.id.triplist2);
		
		stickyList.setEmptyView(findViewById(android.R.id.empty));
		
//		stickyList.setOnScrollListener(new AbsListView.OnScrollListener(){
//			
//			@Override
//			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//				Log.e("triplist", "(first,visible,total,last)=" + firstVisibleItem + ", " + visibleItemCount + ", " + totalItemCount + ", " + ((StickyListHeadersListView) view).getLastVisiblePosition());
//			}
//			
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//			}
//			
//		});
		
		stickyList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				final JSONObject obj = (JSONObject) parent.getItemAtPosition(position);
				//only local tripinfo has this key
				if(obj.has("triplistheader")){
					//is a local tripinfo entry
					new AlertDialog.Builder(mContext).setItems(R.array.triplist_contextmenu_local,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case 0:
										String tripid = null;
										try {
											tripid = obj.getString("trip_id");
										} catch (JSONException e1) {
											e1.printStackTrace();
										}
										//don't start upload thread if tripid or userid is invalid or null
										if(userid != null && !userid.equals("-1")){
											if(tripid != null){
												new UploadThread(mContext, userid, tripid, TripListActivity4.this, position).execute();
											} else{
												//prompt error message
												Toast.makeText(mContext, R.string.error, Toast.LENGTH_SHORT).show();
											}
										} else{
											//prompt user to login
											new AlertDialog.Builder(mContext)
											.setTitle(R.string.error)
											.setMessage(R.string.alertdialog_pleaseloginfirst)
											.setPositiveButton(R.string.alertdialog_loginnow, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													startActivityForResult(new Intent(mContext, Settings.class), REQUEST_CODE_SETTINGS);
												}
											})
											.setNeutralButton(R.string.alertdialog_maybelater, null)
											.show();
										}
										break;
									case 1: 
										{	
											//XXX show alertdialog confirming deletion
											
											// delete
											// this is a deletion from local DB
											DBHelper2 dh2 = new DBHelper2(mContext);
											try {
												if (dh2.deleteLocalTrip(obj.getString("trip_id")) > 0) {
													Toast.makeText(mContext, "\"" + obj.getString("trip_name") + "\" " + mContext.getResources().getString(R.string.toast_trip_deleted), Toast.LENGTH_LONG).show();
													loadTripList(position);
												} else {
													Toast.makeText(mContext, "\"" + obj.getString("trip_name") + "\" " + mContext.getResources().getString(R.string.toast_trip_cannotdelete), Toast.LENGTH_LONG).show();
												}
												dh2.closeDB();
												dh2 = null;
											} catch (JSONException e) {
												e.printStackTrace();
											} finally {
												if (dh2 != null) {
													dh2.closeDB();
													dh2 = null;
												}
											}
										}
										break;
									default:
										break;
									}
								}
							}
						)
						.show();
				} else{
					//is a remote tripinfo entry
					new AlertDialog.Builder(mContext).setItems(R.array.triplist_contextmenu_remote,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case 0:
											new AsyncTask<Void, Void, Integer>(){
												
												private ArrayList<SharedUser> sharedUserList;
												
												private ProgressDialog diag;
												
												private String tripid = null;
												
												@Override
												protected void onPreExecute() {
													
													try {
														tripid = obj.getString("trip_id");
													} catch (JSONException e1) {
														e1.printStackTrace();
													}
													
													if(tripid == null){
														cancel(true);
													} else{
														
														sharedUserList = new ArrayList<SharedUser>();
														
														diag = new ProgressDialog(mContext);
														diag.setMessage(mContext.getString(R.string.universal_loading));
														diag.setCancelable(false);
														diag.setIndeterminate(true);
														diag.show();
													}
												}
												
												@Override
												protected Integer doInBackground(Void... params) {
													
													//first get friendlist
													String url1 = "http://plash2.iis.sinica.edu.tw/api/GetFriendList.php?sid=" + userid;
													//consider saving a local copy of friend list, with timeout limit 30min 
													
													HttpGet getRequest = new HttpGet(url1);
													
													HttpParams httpParameters = new BasicHttpParams();
													// Set the timeout in milliseconds until a connection is
													// established.
													// The default value is zero, that means the timeout is not
													// used.
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
															
															BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//															response.getEntity().consumeContent();
															//entity of the response is now cleared
															JSONObject result = new JSONObject(new JSONTokener(in.readLine().replace("({", "{").replace("});", "}")));
															in.close();
															
															ArrayList<JSONObject> info = (ArrayList<JSONObject>) JSONUtility.asList(result.getJSONArray("friend_list"));
//															Log.i("info", "info=" + info.toString());
															if(info != null){
																if(info.isEmpty()){
																	//no friend
																	return 1;
																} else{
																	
																	//save all friend names into userlist
																	for(JSONObject item : info){
																		sharedUserList.add(new SharedUser(item.getInt("id"), item.getString("name")));
																	}
																	
																	//first get the shared user list of this trip
																	String url2 = "http://plash2.iis.sinica.edu.tw/api/GetTripShareUser.php?userid=" + userid + "&trip_id=" + tripid;
																	
																	getRequest = new HttpGet(url2);
																	
																	client = new DefaultHttpClient(httpParameters);
																	
																	response = client.execute(getRequest);
																	
																	statusCode = response.getStatusLine().getStatusCode();
																	
																	if(statusCode == 200){
																		//TripShareUser
																		BufferedReader in2 = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
																		JSONObject result2 = new JSONObject(new JSONTokener(in2.readLine()));
																		in2.close();
																		
																		ArrayList<JSONObject> list = (ArrayList<JSONObject>) JSONUtility.asList(result2.getJSONArray("TripShareUser"));
																		if(list != null){
																			if(list.isEmpty()){
																				//have not shared with anyone yet
																			} else{
																				//might have shared with someone
																				for(JSONObject item : list){
																					for(int j = 0; j < sharedUserList.size(); j++){
																						if(sharedUserList.get(j).getName().equals(item.getString("name"))){
																							//it is default to all false, set checked to true if a trip has been shared with this friend
																							sharedUserList.get(j).setChecked(true);
																						}
																					}
																				}
																			}
																			return 0;
																		}
																	}
																}
															}
															
														}
													} catch (ClientProtocolException e) {
														e.printStackTrace();
													} catch (IOException e) {
														e.printStackTrace();
													} catch (JSONException e) {
														e.printStackTrace();
													} finally{
														client.getConnectionManager().shutdown();
													}
													return 2;
												}
												
												@Override
												protected void onCancelled() {
													Log.e("share", "no tripid");
												}
												
												@Override
												protected void onPostExecute(Integer result) {
													diag.dismiss();
													switch(result){
													case 0:
														final String[] names = new String[sharedUserList.size()];
														final boolean[] checked = new boolean[sharedUserList.size()];
														for(int i = 0; i < sharedUserList.size(); i++){
															if(sharedUserList.get(i).getId() == 0){
																//public trip guy
																names[i] = mContext.getString(R.string.alertdialog_setpublictripname);
															} else{
																names[i] = sharedUserList.get(i).getName();
															}
															checked[i] = sharedUserList.get(i).getChecked();
														}
														
														new AlertDialog.Builder(mContext)
														.setTitle(R.string.alertdialog_sharetrip_title)
														.setMultiChoiceItems(names, checked, new DialogInterface.OnMultiChoiceClickListener(){
															@Override
															public void onClick(DialogInterface dialog, int which, boolean isChecked) {
																sharedUserList.get(which).setChecked(isChecked);
															}
															
														})
														.setNegativeButton(R.string.dialog_button_cancel, null)
														.setPositiveButton(R.string.alertdialog_sharetrip_commit, new DialogInterface.OnClickListener(){
															@Override
															public void onClick(final DialogInterface dialog, int which) {
																
																new AsyncTask<Void, Void, Boolean>(){
																	
																	private String friendids = null;
																	
																	private ProgressDialog diag;
																	
																	@Override
																	protected void onPreExecute() {
																		
																		for(SharedUser item : sharedUserList){
																			if(item.getChecked()){
																				if(friendids == null){
																					friendids = String.valueOf(item.getId());
																				} else{
																					friendids = friendids + "," + String.valueOf(item.getId());
																				}
																			}
																		}
																		
																		if(friendids == null){
																			cancel(true);
																		} else{
																			
																			diag = new ProgressDialog(mContext);
																			diag.setMessage(mContext.getString(R.string.alertdialog_sharetrip_commiting));
																			diag.setCancelable(false);
																			diag.setIndeterminate(true);
																			diag.show();
																		}
																	}
																	
																	@Override
																	protected Boolean doInBackground(Void... params) {
																		
																			//save the newly assigned shared user list
																			String url = "http://plash2.iis.sinica.edu.tw/api/SetAuthFriendComponent.php?userid=" + userid + "&trip_id=" + tripid + "&friend_id=" + friendids;
																			
//																			Log.i("setauthfriend", "url=" + url);
																			
																			HttpGet getRequest = new HttpGet(url);
																			
																			HttpParams httpParameters = new BasicHttpParams();
																			// Set the timeout in milliseconds until a connection is
																			// established.
																			// The default value is zero, that means the timeout is not
																			// used.
																			HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
																			// Set the default socket timeout (SO_TIMEOUT)
																			// in milliseconds which is the timeout for waiting for
																			// data.
																			int timeoutSocket = 30000;
																			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
																			
																			DefaultHttpClient client = new DefaultHttpClient(httpParameters);
																			try {
																			HttpResponse response = client.execute(getRequest);
																			
																			
																			Integer statusCode = response.getStatusLine().getStatusCode();
																			if (statusCode == 200) {
																				
																				BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
																				JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
																				in.close();
																				if(result != null){
																					if(result.has("code") && result.getInt("code")==200){
																						//successful operation
																						return true;
																					}
																				}
																			}
																		} catch (IOException e) {
																			e.printStackTrace();
																		} catch (JSONException e) {
																			e.printStackTrace();
																		} finally{
																			client.getConnectionManager().shutdown();
																		}
																		return false;
																	}
																	
																	@Override
																	protected void onPostExecute(Boolean result) {
																		diag.dismiss();
																		if(result){
																			dialog.dismiss();
																			Toast.makeText(mContext, R.string.toast_triplist_shared_success, Toast.LENGTH_SHORT).show();
																		} else{
																			Toast.makeText(mContext, R.string.toast_triplist_shared_failed, Toast.LENGTH_LONG).show();
																		}
																	}
																	
																}.execute();
															}
														})
														.show();
														break;
													case 1:
														//no friend to share with
														Toast.makeText(mContext, R.string.toast_nofriendtosharetripwith, Toast.LENGTH_LONG).show();
														break;
													case 2:
													default:
														//any other error
														//XXX show something about failed to load shared user list
														Toast.makeText(mContext, R.string.toast_unabletofetchtripdata, Toast.LENGTH_LONG).show();
														break;
													}
												}
											}.execute();
										break;
									case 1:
										try {
											final String pendingDeletionTripname = obj.getString("trip_name");
											final String pendingDeletionTripid = obj.getString("trip_id");
											new AlertDialog.Builder(mContext)
											.setTitle(R.string.alertdialog_delete_title)
											.setMessage(mContext.getResources().getString(R.string.alertdialog_delete_message) + " \"" + pendingDeletionTripname + "\" ?")
											.setNegativeButton(R.string.nope, null)
											.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener(){
												@Override
												public void onClick(DialogInterface dialog, int which) {
													new AsyncTask<Void, Void, Boolean>(){
														ProgressDialog diag;
														@Override
														protected void onPreExecute() {
															diag = new ProgressDialog(mContext);
															diag.setCancelable(false);
															diag.setIndeterminate(true);
															diag.setMessage(mContext.getResources().getString(R.string.progressdialog_deleting) + " " + pendingDeletionTripname + "...");
															diag.show();
														}
														@Override
														protected Boolean doInBackground(Void... params) {
															if(isNetworkAvailable(mContext)){
																
																	String url = "http://plash2.iis.sinica.edu.tw/api/DelTripComponent.php?userid=" + userid + "&trip_id=" + pendingDeletionTripid;
																	
																	HttpGet getRequest = new HttpGet(url);
																	
																	HttpParams httpParameters = new BasicHttpParams();
																	// Set the timeout in milliseconds until a connection is established. 
																	// The default value is zero, that means the timeout is not used.
																	HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
																	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
																	int timeoutSocket = 5000;
																	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
																	
																	DefaultHttpClient client = new DefaultHttpClient(httpParameters);
																	try{
																	HttpResponse response = client.execute(getRequest);
																	
																	Integer statusCode = response.getStatusLine().getStatusCode();
																	if (statusCode == 200) {
																		
																		BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//																		JSONObject result = new JSONObject(new JSONTokener(in.readLine().replace("({", "{").replace("});", "}")));
																		in.close();
																		
																		
																		// supposedly all good
																		return true;
																	} 
																		
																} catch(ClientProtocolException e){
																	e.printStackTrace();
																} catch (IOException e) {
																	e.printStackTrace();
																} finally{
																	client.getConnectionManager().shutdown();
																}
																return false;
															} else{
																return false;
															}
														}
														@Override
														protected void onPostExecute(Boolean result) {
															diag.dismiss();
															if(result){
																Toast.makeText(mContext, "\"" + pendingDeletionTripname + "\" " + mContext.getResources().getString(R.string.toast_trip_deleted), Toast.LENGTH_LONG).show();
																loadTripList(position);
															} else{
																Toast.makeText(mContext, "\"" + pendingDeletionTripname + "\" " + mContext.getResources().getString(R.string.toast_trip_cannotdelete), Toast.LENGTH_LONG).show();
															}
														}
													}.execute();
												}
											}).show();
										} catch (JSONException e) {
											e.printStackTrace();
											Toast.makeText(mContext, R.string.toast_trip_deletion_failed, Toast.LENGTH_LONG).show();
										}
										break;
									default:
										break;
									}
								}
							}
						)
						.show();
				}
				return false;
			}
		});
		
		stickyList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final JSONObject obj = (JSONObject) parent.getItemAtPosition(position);
				if(obj.has("triplistheader")){
					//it's a local trip, get the local tripid and pass it to mapviewer
					try {
						String name = obj.getString("trip_name");
						String tripid = obj.getString("trip_id");
						startActivity(new Intent(mContext, GMapViewer.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("islocal", true).putExtra("tripname", name).putExtra("tripid", tripid));
					} catch (JSONException e) {
						e.printStackTrace();
						Toast.makeText(mContext, R.string.toast_cannotviewtrip, Toast.LENGTH_LONG).show();
					}
				} else{
					//it's a remote trip, pass both userid and remote tripid to mapviewer
					try {
						String name = obj.getString("trip_name");
						String hash = obj.getString("hash");
						String count = obj.getString("num_of_pts");
						if(Integer.valueOf(count) > JVM_HEAP_POINT_COUNT_LIMIT){
							//trip is too long for this device to display
							new AlertDialog.Builder(mContext)
								.setTitle(R.string.toast_cannotviewtrip)
								.setMessage(R.string.tripistoolargetodisplay)
								.setNeutralButton(R.string.alertdialog_iseebutton, null)
								.show();
						} else{
							startActivity(new Intent(mContext, GMapViewer.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("islocal", false).putExtra("tripname", name).putExtra("hash", hash));
						}
					} catch (JSONException e) {
						e.printStackTrace();
						Toast.makeText(mContext, R.string.toast_cannotviewtrip, Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		
		loadTripList(-1);
	}
	
	private class SharedUser{
		private Integer id;
		private String name;
		private Boolean checked;
		
		public SharedUser(Integer id, String name, Boolean checked) {
			this.id = id;
			this.name = name;
			this.checked = checked;
		}
		
		public SharedUser(Integer id, String name) {
			this(id, name, false);
		}
		
		public void setId(int id){
			this.id = id;
		}
		
		public Integer getId(){
			return this.id;
		}
		
		public void setName(String name){
			this.name = name;
		}
		
		public String getName(){
			return this.name;
		}
		
		public void setChecked(Boolean checked){
			this.checked = checked;
		}
		
		public Boolean getChecked(){
			return this.checked;
		}
	}
	
	private void loadTripList(int position){
		Log.i("triplist", "loadtriplist, position= " + position);
		if(!stillLoading){
			Log.i("trip list", "loadTripList, stillloading false, proceed");
			tlal = new TripListAsyncLoader(position);
			tlal.execute();
		}
		Log.i("trip list", "loadTripList, stillloading false, proceed");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("trip list", "ondestroy");
		if(stillLoading){
			tlal.cancel(true);
		}
	}
	
	private TripListAsyncLoader tlal;
	
	private class TripListAsyncLoader extends AsyncTask<Void, Void, Integer>{
		
		final private int ADAPTER_NOT_NULL = 0;
		final private int ADAPTER_NOT_NULL_STUFF_TO_REMOVE = 1;
		
		final private int ADAPTER_IS_NULL_NO_USERID_LOCAL_GOOD = 2;
		final private int ADAPTER_IS_NULL_NO_USERID_LOCAL_NO_GOOD = 3;
		
		final private int ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET_LOCAL_GOOD = 4;
		final private int ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET_LOCAL_NO_GOOD = 5;
		
		final private int ADAPTER_IS_NULL_REMOTE_GOOD_LOCAL_GOOD = 6;
		final private int ADAPTER_IS_NULL_REMOTE_GOOD_LOCAL_NO_GOOD = 7;
		
		final private int ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_GOOD = 8;
		final private int ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_NO_GOOD = 9;
		
		private JSONArray localtripinfo;
		private JSONArray remotetripinfo;
		
		final int pendingRemovePosition;
		
		public TripListAsyncLoader(int position) {
			pendingRemovePosition = position;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			((TextView) stickyList.getEmptyView().findViewById(R.id.emptytext)).setText(R.string.universal_loading);
			
			stillLoading = true;
			
			localtripinfo = null;
			
			remotetripinfo = null;
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
				//first trip to get local tripinfo
				DBHelper2 dh2 = new DBHelper2(mContext);
				// get current tid from service
				if (AntripService2.isRecording()) {
					localtripinfo = dh2.getAllTripInfoForHTML2(AntripService2.getCurrentTripid());
				} else {
					localtripinfo = dh2.getAllTripInfoForHTML2(null);
				}
				dh2.closeDB();
				dh2 = null;
				//now try to get remote tripinfo
				//but first check if user is logged in or not
				if(userid.equals("-1")){
					//user is not logged in yet
					if(localtripinfo != null){
						//user is not logged in, but has local tripinfo
						return ADAPTER_IS_NULL_NO_USERID_LOCAL_GOOD;
					} else{
						//user is not logged in and has no local tripinfo
						return ADAPTER_IS_NULL_NO_USERID_LOCAL_NO_GOOD;
					}
				} else{
					//user id logged in, can try to get remote tripinfo
					//first check if internet is available
					if (isNetworkAvailable(mContext)) {
						// internet is available, get remote tripinfo
						
						String url = "http://plash2.iis.sinica.edu.tw/api/GetTripInfoComponent.php?userid=" + userid + "&sort=3" + "&MaxResult=200";
						
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
								JSONObject result = new JSONObject(new JSONTokener(in.readLine().replace("({", "{")
										.replace("});", "}")));
								in.close();
								
								remotetripinfo = result.getJSONArray("tripInfoList");
								
								if (remotetripinfo != null && remotetripinfo.length() > 0) {
									// remote not null, local unknown
									if (localtripinfo != null) {
										// has remote tripinfo and local
										// tripinfo
										return ADAPTER_IS_NULL_REMOTE_GOOD_LOCAL_GOOD;
									} else {
										// has remote tripinfo, but no local
										// tripinfo
										return ADAPTER_IS_NULL_REMOTE_GOOD_LOCAL_NO_GOOD;
									}
								} else {
									// remote null, local unknown
									if (localtripinfo != null) {
										// no remote tripinfo, has local
										// tripinfo
										return ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_GOOD;
									} else {
										// no remote tripinfo, no local tripinfo
										return ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_NO_GOOD;
									}
								}
							} else {
								// connection error, local unknown
								if (localtripinfo != null) {
									// connection error, no remote tripinfo, but
									// has local tripinfo
									return ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_GOOD;
								} else {
									// connection error, no remote tripinfo, no
									// local tripinfo
									return ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_NO_GOOD;
								}
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
						// exceptions
						if (localtripinfo != null) {
							// exception, no remote tripinfo, but has local
							// tripinfo
							return ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_GOOD;
						} else {
							// exception, no remote tripinfo, no local tripinfo
							return ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_NO_GOOD;
						}
					} else {
						// user id logged in, but internet is not available
						if (localtripinfo != null) {
							// has local tripinfo
							return ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET_LOCAL_GOOD;
						} else {
							// nop local tripinfo
							return ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET_LOCAL_NO_GOOD;
						}
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
			case ADAPTER_IS_NULL_NO_USERID_LOCAL_GOOD:
			case ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET_LOCAL_GOOD:
			case ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_GOOD:
				//no remote, yes local
				adapter = new TripListAdapter2(mContext, null, localtripinfo);
				stickyList.setAdapter(adapter);
				break;
				
			case ADAPTER_IS_NULL_NO_USERID_LOCAL_NO_GOOD:
			case ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET_LOCAL_NO_GOOD:
			case ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_NO_GOOD:
				//no remote, no local
				adapter = new TripListAdapter2(mContext, null, null);;
				stickyList.setAdapter(adapter);
				((TextView) stickyList.getEmptyView().findViewById(R.id.emptytext)).setText(R.string.trip_list_empty_actual);
				((ProgressBar) stickyList.getEmptyView().findViewById(R.id.emptyprogressbar)).setVisibility(View.GONE);
				break;
				
			case ADAPTER_IS_NULL_REMOTE_GOOD_LOCAL_NO_GOOD:
				//yes remote, no local
				adapter = new TripListAdapter2(mContext, remotetripinfo, null);
				stickyList.setAdapter(adapter);
				break;
				
			case ADAPTER_IS_NULL_REMOTE_GOOD_LOCAL_GOOD:
				//yes remote, yes local
				adapter = new TripListAdapter2(mContext, remotetripinfo, localtripinfo);
				stickyList.setAdapter(adapter);
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
				((TextView) stickyList.getEmptyView().findViewById(R.id.emptytext)).setText(R.string.trip_list_empty_actual);
				((ProgressBar) stickyList.getEmptyView().findViewById(R.id.emptyprogressbar)).setVisibility(View.GONE);
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
	public void shouldReloadTripList(int position) {
		loadTripList(position);
		//XXX need to also reload remote trip list, in stead of just removing local trip...
	}
}
