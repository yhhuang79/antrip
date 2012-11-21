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
import android.content.Context;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.emilsjolander.components.StickyListHeaders.StickyListHeadersListView;

public class TripListActivity2 extends Activity {
	
	private TripListAdapter2 adapter = null;
	private Context mContext;
	private SharedPreferences pref;
	
	private StickyListHeadersListView stickyList;
	
	private String userid;
	
	private ImageButton btn_activity_icon;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		
		Log.w("triplistactivity", "on config change");
		
		super.onConfigurationChanged(newConfig);
		
		setContentView(R.layout.triplist2);
		
		loadTripLists();
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
		
		loadTripLists();
	}
	
	private void loadTripLists() {
		
		stickyList = (StickyListHeadersListView) findViewById(R.id.triplist2);
		
		new AsyncTask<Void, Void, Integer>() {
			
			JSONArray localtripinfo = null;
			JSONArray remotetripinfo = null;
			
			@Override
			protected Integer doInBackground(Void... params) {
				if (adapter == null) {
					
					if(userid != null){
						DBHelper128 dh = new DBHelper128(mContext);
						//XXX get current tid from service
						if(AntripService.getCurrentTid() != null){
							localtripinfo = dh.getAllTripInfoForHTML2(userid, AntripService.getCurrentTid().toString());
						} else{
							localtripinfo = dh.getAllTripInfoForHTML2(userid, null);
						}
						
						dh.closeDB();
						dh = null;
					}
					if(isNetworkAvailable(mContext)){
						try {
							String url = "http://plash2.iis.sinica.edu.tw/api/GetTripInfoComponent.php?userid=" + userid;
							
							HttpGet getRequest = new HttpGet(url);
							
							HttpParams httpParameters = new BasicHttpParams();
							// Set the timeout in milliseconds until a connection is established.
							// The default value is zero, that means the timeout is not used. 
							int timeoutConnection = 3000;
							HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
							// Set the default socket timeout (SO_TIMEOUT) 
							// in milliseconds which is the timeout for waiting for data.
							int timeoutSocket = 5000;
							HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
							
							DefaultHttpClient client = new DefaultHttpClient(httpParameters);
							
							HttpResponse response = client.execute(getRequest);
							
							Integer statusCode = response.getStatusLine().getStatusCode();
							if (statusCode == 200) {
								
								BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity()
										.getContent()));
								JSONObject result = new JSONObject(new JSONTokener(in.readLine().replace("({", "{")
										.replace("});", "}")));
								in.close();
								
								remotetripinfo = result.getJSONArray("tripInfoList");
								
								Log.e("triplistactivity", "name: " + remotetripinfo.getJSONObject(0).getString("trip_name")
										+ "; st: " + remotetripinfo.getJSONObject(0).getString("trip_st"));
								
								client.getConnectionManager().shutdown();
								//supposedly all good
								return 0;
							} else{
								
								client.getConnectionManager().shutdown();
								//connection error
								return 1;
							}
							
						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						//exceptions
						return 4;
					} else{
						//no internet
						return 2;
					}
				} else{
					//adapter not null
					return 3;
				}
			}
			
			protected void onPostExecute(Integer result) {
				switch(result){
				case 0:
					//all good
					if(remotetripinfo != null || localtripinfo != null){
						adapter = new TripListAdapter2(mContext, remotetripinfo, localtripinfo);
					} else{
						((TextView) findViewById(android.R.id.empty)).setText("Nothing to see here...");
						break;
					}
				case 3:
					//adapter not null
					stickyList.setAdapter(adapter);
					//don't think i need to dismiss empty view myself
					((TextView) findViewById(android.R.id.empty)).setVisibility(View.GONE);
					break;	
				case 1:
					//connection error
					Toast.makeText(mContext, "connection error", Toast.LENGTH_LONG).show();
					break;
				case 2:
					//no internet
					Toast.makeText(mContext, "no internet", Toast.LENGTH_LONG).show();
					break;
				case 4:
					//exceptions
					Toast.makeText(mContext, "exception", Toast.LENGTH_LONG).show();
					break;
				default:
					//huh?
					Toast.makeText(mContext, "huh?", Toast.LENGTH_LONG).show();
					break;
				}
			};
		}.execute();
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
