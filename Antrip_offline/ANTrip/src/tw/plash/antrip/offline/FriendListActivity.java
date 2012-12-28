package tw.plash.antrip.offline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
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

import tw.plash.antrip.offline.AntripService2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class FriendListActivity extends Activity{
	
	private TripListAdapter2 adapter = null;
	private Context mContext;
	private SharedPreferences pref;
	private String userid;
	
	private boolean stillLoading;
	
	private StickyListHeadersListView stickyList;
	
	private ImageButton dropdownList;
	
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
		
		userid = pref.getString("userid", "154"); //XXX
		
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
				//only local tripinfo has this key
				if(obj.has("triplistheader")){
					//is a local tripinfo entry
					new AlertDialog.Builder(mContext).setItems(new String[] { "upload", "delete" },
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
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
												.setMessage("upload function coming soon...\nhere's the total number of points in this trip: " + dh2.getNumberofPoints(obj.getString("trip_id")))
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
													Toast.makeText(mContext, "\"" + obj.getString("trip_name") + "\" deleted!", Toast.LENGTH_LONG).show();
													loadTripList(position);
												} else {
													Toast.makeText(mContext, "\"" + obj.getString("trip_name") + "\" cannot be deleted!", Toast.LENGTH_LONG).show();
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
					new AlertDialog.Builder(mContext).setItems(new String[] { "share", "delete" },
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case 0:
										//share
										//make sure user is logged in
										//if user have no friend, show empty list, maybe empty view like empty lifeview
										//show popup dialog/window with checkbox and friend list
										new AlertDialog.Builder(mContext)
										.setTitle("hold your horses!")
										.setMessage("sharing function is coming soon...\nin the meamtime, here's your lucky number of today: " + new Random(new Date().getTime()).nextInt(10000))
										.setNeutralButton("okie dokie", null)
										.show();
										break;
									case 1:
										try {
											final String pendingDeletionTripname = obj.getString("trip_name");
											final String pendingDeletionTripid = obj.getString("trip_id");
											new AlertDialog.Builder(mContext)
											.setTitle("delete")
											.setMessage("are you sure you want to delete \"" + pendingDeletionTripname + "\" ?")
											.setNegativeButton("no", null)
											.setPositiveButton("yeah", new DialogInterface.OnClickListener(){
												@Override
												public void onClick(DialogInterface dialog, int which) {
													new AsyncTask<Void, Void, Boolean>(){
														ProgressDialog diag;
														@Override
														protected void onPreExecute() {
															diag = new ProgressDialog(mContext);
															diag.setCancelable(false);
															diag.setIndeterminate(true);
															diag.setMessage("deleting " + pendingDeletionTripname + "...");
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
																Toast.makeText(mContext, "\"" + pendingDeletionTripname + "\" deleted!", Toast.LENGTH_LONG).show();
																loadTripList(position);
															} else{
																Toast.makeText(mContext, "\"" + pendingDeletionTripname + "\" cannot be deleted!", Toast.LENGTH_LONG).show();
															}
														}
													}.execute();
												}
											}).show();
										} catch (JSONException e) {
											e.printStackTrace();
											Toast.makeText(mContext, "Deletion failed! Try again later!", Toast.LENGTH_LONG).show();
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
				//XXX
				//open the trip
				Toast.makeText(mContext, "viewing function coming soon...", Toast.LENGTH_SHORT).show();
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
		
		final private int ADAPTER_IS_NULL_NO_USERID_LOCAL_GOOD = 2;
		final private int ADAPTER_IS_NULL_NO_USERID_LOCAL_NO_GOOD = 3;
		
		final private int ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET_LOCAL_GOOD = 4;
		final private int ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET_LOCAL_NO_GOOD = 5;
		
		final private int ADAPTER_IS_NULL_REMOTE_GOOD_LOCAL_GOOD = 6;
		final private int ADAPTER_IS_NULL_REMOTE_GOOD_LOCAL_NO_GOOD = 7;
		
		final private int ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_GOOD = 8;
		final private int ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_NO_GOOD = 9;
		
		private ProgressDialog diag;
		
		private JSONArray localtripinfo;
		private JSONArray remotetripinfo;
		
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
			
			remotetripinfo = null;
			
			diag = new ProgressDialog(mContext);
			diag.setCancelable(false);
			diag.setIndeterminate(true);
			diag.setMessage("loading...");
			diag.show();
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
						//internet is available, get remote tripinfo
						 
						
							String url = "http://plash2.iis.sinica.edu.tw/api/GetTripInfoComponent.php?userid=" + userid;
							
							HttpGet getRequest = new HttpGet(url);
							
							HttpParams httpParameters = new BasicHttpParams();
							// Set the timeout in milliseconds until a connection is
							// established.
							// The default value is zero, that means the timeout is not used.
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
								JSONObject result = new JSONObject(new JSONTokener(in.readLine().replace("({", "{").replace("});", "}")));
								in.close();
								
								remotetripinfo = result.getJSONArray("tripInfoList");
								
								
								
								if(remotetripinfo != null && remotetripinfo.length() > 0){
									//remote not null, local unknown
									if(localtripinfo != null){
										//has remote tripinfo and local tripinfo
										return ADAPTER_IS_NULL_REMOTE_GOOD_LOCAL_GOOD;
									} else{
										//has remote tripinfo, but no local tripinfo
										return ADAPTER_IS_NULL_REMOTE_GOOD_LOCAL_NO_GOOD;
									}
								} else{
									//remote null, local unknown
									if(localtripinfo != null){
										//no remote tripinfo, has local tripinfo
										return ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_GOOD;
									} else{
										//no remote tripinfo, no local tripinfo
										return ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_NO_GOOD;
									}
								}
							} else {
								// connection error, local unknown
								if(localtripinfo != null){
									//connection error, no remote tripinfo, but has local tripinfo
									return ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_GOOD;
								} else{
									//connection error, no remote tripinfo, no local tripinfo
									return ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_NO_GOOD;
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
						// exceptions
						if(localtripinfo != null){
							//exception, no remote tripinfo, but has local tripinfo
							return ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_GOOD;
						} else{
							//exception, no remote tripinfo, no local tripinfo
							return ADAPTER_IS_NULL_REMOTE_NO_GOOD_LOCAL_NO_GOOD;
						}
					} else {
						//user id logged in, but internet is not available
						if(localtripinfo != null){
							//has local tripinfo
							return ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET_LOCAL_GOOD;
						} else{
							//nop local tripinfo
							return ADAPTER_IS_NULL_HAS_USERID_NO_INTERNET_LOCAL_NO_GOOD;
						}
					}
				}
			}
		}

		@Override
		protected void onCancelled() {
			//not sure what needs to be done here
			diag.dismiss();
		}
		
		protected void onPostExecute(Integer result) {
			diag.dismiss();
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
				((TextView) stickyList.getEmptyView()).setText("Nothing to see here...");
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
}
