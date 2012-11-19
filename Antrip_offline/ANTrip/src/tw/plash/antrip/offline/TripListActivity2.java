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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.emilsjolander.components.StickyListHeaders.StickyListHeadersListView;

public class TripListActivity2 extends Activity {
	
	private TripListAdapter2 adapter = null;
	private Context mContext;
	private SharedPreferences pref;
	
	private StickyListHeadersListView stickyList;
	
	private String userid;
	
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
		
		Crittercism.init(getApplicationContext(), "50a0662801ed852ced000002");
		
		setContentView(R.layout.triplist2);
		
		mContext = this;
		
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		userid = pref.getString("userid", "154");
		
		loadTripLists();
	}
	
	private void loadTripLists() {
		
		stickyList = (StickyListHeadersListView) findViewById(R.id.triplist2);
		
		new AsyncTask<Void, Void, Void>() {
			
			JSONArray localtripinfo = null;
			JSONArray remotetripinfo = null;
			
			@Override
			protected Void doInBackground(Void... params) {
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
							
						}
						client.getConnectionManager().shutdown();
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
			
			protected void onPostExecute(Void result) {
				if (adapter == null) {
					Log.e("triplistactivity2", "onpostexecute: adapter is null");
					if(remotetripinfo != null || localtripinfo != null){
						adapter = new TripListAdapter2(mContext, remotetripinfo, localtripinfo);
						Log.e("triplistactivity2", "onpostexecute: new adapter");
					} else{
						((TextView) findViewById(android.R.id.empty)).setText("Nothing to see here...");
						Log.e("triplistactivity2", "onpostexecute: no data");
						return;
					}
				}
				stickyList.setAdapter(adapter);
				((TextView) findViewById(android.R.id.empty)).setVisibility(View.GONE);
				Log.e("triplistactivity2", "onpostexecute: set adapter");
			};
		}.execute();
	}
}
