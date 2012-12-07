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

import com.crittercism.app.Crittercism;
import com.emilsjolander.components.StickyListHeaders.StickyListHeadersListView;

public class TripListActivity3 extends Activity implements TripListReloader{
	
	private TripListAdapter2 adapter = null;
	private Context mContext;
	private SharedPreferences pref;
	
	private boolean stillLoading;
	
	private StickyListHeadersListView stickyList;
	
	private String userid;
	
	private ImageButton btn_activity_icon;
	
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
		
		Crittercism.init(getApplicationContext(), "50aafd4a4f633a2e1a000002");
		
		setContentView(R.layout.triplist2);
		
		mContext = this;
		
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		userid = pref.getString("userid", "154");
		btn_activity_icon = (ImageButton) findViewById(R.id.actionbar_activity_icon);
		btn_activity_icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new DropdownFunctionList(v).showLikePopDownMenu();
			}
		});
		
		initTripList();
	}
	
	private void initTripList() {
		
		stickyList = (StickyListHeadersListView) findViewById(R.id.triplist2);
		
		stickyList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				//check if a column only exists in local trips exists
				final JSONObject obj = (JSONObject) parent.getItemAtPosition(position);
				if (obj.has("triplistheader")) {
					new AlertDialog.Builder(mContext)
						.setItems(new String[] { "upload", "delete" },
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Toast.makeText(mContext, "position: " + position + ", item: " + which + " clicked!", Toast.LENGTH_LONG).show();
									switch(which){
									case 0:
										//upload
										//start upload thread, can try to lock this entry, overlapped with progress bar
										//should maybe run as background service...?
										//or maybe there should be a persistent background service, and run upload thread
										break;
									case 1:
										// delete
										// this is a deletion from local DB
										int pos = -1;
										DBHelper dh = new DBHelper(mContext);
										try {
											if(dh.deleteLocalTrip(userid, obj.getString("trip_id")) > 0){
												pos = position;
												Toast.makeText(mContext, "\"" + obj.getString("trip_name") + "\" deleted!", Toast.LENGTH_LONG).show();
											}
										} catch (JSONException e) {
											e.printStackTrace();
										}
										dh.closeDB();
										dh = null;
										loadTripList(pos);
										break;
									default:
										break;
									}
								}
							}).show();
				} else {
					try {
						final String tripid = ((JSONObject) parent.getItemAtPosition(position)).getString("trip_id");
						new AlertDialog.Builder(mContext)
							.setItems(new String[] { "delete" },
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Toast.makeText(mContext,
												"position: " + position + ", item: " + which + " clicked!",
												Toast.LENGTH_LONG).show();
										// need to send a delete request to
										// PLASH server, and must wait for
										// response
										// http://plash2.iis.sinica.edu.tw/api/DelTripComponent.php?userid=154&trip_id=1050
										new AsyncTask<Void, Void, Boolean>() {
											ProgressDialog diag;
											
											@Override
											protected void onPreExecute() {
												if(!isNetworkAvailable(mContext)){
													//no internet
													cancel(true);
												} else{
													//internet is good
													diag = new ProgressDialog(mContext);
													diag.setIndeterminate(true);
													diag.setCancelable(false);
													diag.setMessage("deleting...");
													diag.show();
												}
												
											}
											
											@Override
											protected Boolean doInBackground(Void... params) {
												try {
													String url = "http://plash2.iis.sinica.edu.tw/api/DelTripComponent.php?userid=" + userid + "&trip_id=" + tripid;
													
													HttpGet getRequest = new HttpGet(url);
													
													HttpParams httpParameters = new BasicHttpParams();
													// Set the timeout in milliseconds until a connection is established. 
													// The default value is zero, that means the timeout is not used.
													int timeoutConnection = 3000;
													HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
													// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
													int timeoutSocket = 5000;
													HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
													
													DefaultHttpClient client = new DefaultHttpClient(httpParameters);
													
													HttpResponse response = client.execute(getRequest);
													
													Integer statusCode = response.getStatusLine().getStatusCode();
													if (statusCode == 200) {
														
														BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//														JSONObject result = new JSONObject(new JSONTokener(in.readLine().replace("({", "{").replace("});", "}")));
														in.close();
														
														client.getConnectionManager().shutdown();
														// supposedly all good
														return true;
													} else {
														
														client.getConnectionManager().shutdown();
														// connection error
														return false;
													}
													
												} catch (ClientProtocolException e) {
													e.printStackTrace();
												} catch (IOException e) {
													e.printStackTrace();
												}
												return false;
											}
											
											@Override
											protected void onPostExecute(Boolean result) {
												diag.dismiss();
												if (result) {
													//XXX
													//remove the entry from trip list
													Toast.makeText(mContext, "DELETED!", Toast.LENGTH_LONG).show();
												} else {
													//show deletion error message
													Toast.makeText(mContext, "DELETION FAILED, TRY AGAIN LATER", Toast.LENGTH_LONG).show();
												}
											}
											
											@Override
											protected void onCancelled() {
												//show internet error warning
												Toast.makeText(mContext, "NO INTERNET, CANNOT PERFORM DELETE OPERATION", Toast.LENGTH_LONG).show();
											}
										}.execute();
									}
								}).show();
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
					
				}
				return false;
			}
		});
		
		stickyList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//XXX
				//open the trip
				
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
		
		final private int REMOTE_NOT_NULL_LOCAL_UNKNOWN = 2;
		final private int REMOTE_NULL_LOCAL_UNKNOWN = 3;
		final private int CONNECTION_ERROR_LOCAL_UNKNOWN = 4;
		final private int REMOTE_EXCEPTION_LOCAL_UNKNOWN = 5;
		
		final private int NO_INTERNET_LOCAL_UNKNOWN = 6;
		
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
		}

		@Override
		protected Integer doInBackground(Void... params) {
			if(adapter != null){
				if(pendingRemovePosition != -1){
					return ADAPTER_NOT_NULL_STUFF_TO_REMOVE;
				} else{
					return ADAPTER_NOT_NULL;
				}
			} else{
				if (userid != null) {
					DBHelper dh = new DBHelper(mContext);
					// get current tid from service
					if (AntripService.getCurrentTid() != null) {
						localtripinfo = dh.getAllTripInfoForHTML2(userid, AntripService.getCurrentTid().toString());
					} else {
						localtripinfo = dh.getAllTripInfoForHTML2(userid, null);
					}
					
					dh.closeDB();
					dh = null;
				}
				if (isNetworkAvailable(mContext)) {
					try {
						String url = "http://plash2.iis.sinica.edu.tw/api/GetTripInfoComponent.php?userid=" + userid;
						
						HttpGet getRequest = new HttpGet(url);
						
						HttpParams httpParameters = new BasicHttpParams();
						// Set the timeout in milliseconds until a connection is
						// established.
						// The default value is zero, that means the timeout is not used.
						int timeoutConnection = 3000;
						HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
						// Set the default socket timeout (SO_TIMEOUT)
						// in milliseconds which is the timeout for waiting for
						// data.
						int timeoutSocket = 5000;
						HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
						
						DefaultHttpClient client = new DefaultHttpClient(httpParameters);
						
						HttpResponse response = client.execute(getRequest);
						
						Integer statusCode = response.getStatusLine().getStatusCode();
						if (statusCode == 200) {
							
							BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
							JSONObject result = new JSONObject(new JSONTokener(in.readLine().replace("({", "{")
									.replace("});", "}")));
							in.close();
							
							remotetripinfo = result.getJSONArray("tripInfoList");
							
							client.getConnectionManager().shutdown();
							
							if(remotetripinfo != null && remotetripinfo.length() > 0){
								//remote not null, local unknown
								return REMOTE_NOT_NULL_LOCAL_UNKNOWN;
							} else{
								//remote null, local unknown
								return REMOTE_NULL_LOCAL_UNKNOWN;
							}
						} else {
							client.getConnectionManager().shutdown();
							// connection error, local unknown
							return CONNECTION_ERROR_LOCAL_UNKNOWN;
						}
						
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					// exceptions
					return REMOTE_EXCEPTION_LOCAL_UNKNOWN;
				} else {
					return NO_INTERNET_LOCAL_UNKNOWN;
				}
			}
		}
		
		@Override
		protected void onCancelled() {
			
		}
		
		protected void onPostExecute(Integer result) {
			switch(result){
			case REMOTE_NOT_NULL_LOCAL_UNKNOWN:
			case REMOTE_NULL_LOCAL_UNKNOWN:
			case CONNECTION_ERROR_LOCAL_UNKNOWN:
			case REMOTE_EXCEPTION_LOCAL_UNKNOWN:
			case NO_INTERNET_LOCAL_UNKNOWN:
				//all local unknown, set adapter anyway
				adapter = new TripListAdapter2(mContext, remotetripinfo, localtripinfo);
				
			case ADAPTER_NOT_NULL_STUFF_TO_REMOVE:
				adapter.remove(pendingRemovePosition);
				
			case ADAPTER_NOT_NULL:
				//adapter not null
				stickyList.setAdapter(adapter);
				//don't think i need to dismiss empty view myself
//				((TextView) stickyList.getEmptyView()).setVisibility(View.GONE);
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
