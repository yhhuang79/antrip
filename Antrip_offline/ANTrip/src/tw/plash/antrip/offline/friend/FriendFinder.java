package tw.plash.antrip.offline.friend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.http.HttpResponse;
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
import tw.plash.antrip.offline.InvalidateViewsCallback;
import tw.plash.antrip.offline.R;
import tw.plash.antrip.offline.connection.InternetUtility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FriendFinder extends Activity implements InvalidateViewsCallback{
	
	private Context mContext;
	
	private FriendFinderListAdapter adapter;
	private ListView listview;
	
	private EditText edittext;
	
	private HashSet<String> queriedPrefix;
	
	private SharedPreferences pref;
	private String userid;
	
	/*
	 * to cached the queried user list with their corresponding prefix
	 */
	private HashMap<String, JSONArray> cachedQuery;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		setContentView(R.layout.friendfinder);
		
		initCandidateList();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.friendfinder);
		
		mContext = this;
		
		pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		userid = pref.getString("userid", "-1"); //XXX
		
		queriedPrefix = new HashSet<String>();
		cachedQuery = new HashMap<String, JSONArray>();
		adapter = null;
		
		initCandidateList();
	}
	
	private void initCandidateList(){
		
		((TextView) findViewById(R.id.actionbar_activity_title)).setText(R.string.title_friendfinder);
		
		ImageButton btn = (ImageButton) findViewById(R.id.actionbar_activity_icon);
		btn.setImageResource(R.color.button_state_goback);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		listview = (ListView) findViewById(R.id.friendfinder_listview);
		listview.setEmptyView(findViewById(android.R.id.empty));
		((ProgressBar) listview.getEmptyView().findViewById(R.id.emptyprogressbar)).setVisibility(View.GONE);
		
		edittext = (EditText) findViewById(R.id.friendfinder_edittext);
		edittext.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Log.w("friendfinder", "textchanged: " + s.toString());
				if(s != null && s.length() == 1){
					Log.w("friendfinder", "textchanged if cycle: " + s.toString());
					final String prefix = s.toString();
					if(!queriedPrefix.contains(prefix)){
						//send request to server, cached data and set adapter
						new AsyncTask<Void, Void, Boolean>() {
							protected void onPreExecute() {
								adapter = new FriendFinderListAdapter(mContext, null, FriendFinder.this, userid);
								listview.setAdapter(adapter);
								((TextView) listview.getEmptyView().findViewById(R.id.emptytext)).setText(R.string.universal_loading);
								((ProgressBar) listview.getEmptyView().findViewById(R.id.emptyprogressbar)).setVisibility(View.VISIBLE);
							};
							@Override
							protected Boolean doInBackground(Void... params) {
								if (isNetworkAvailable(mContext)) {
									
									String url = "http://plash2.iis.sinica.edu.tw/api/GetAllUserList.php?userid="
											+ userid + "&name=" + InternetUtility.encode(prefix);
									
									HttpGet getRequest = new HttpGet(url);
									
									HttpParams httpParameters = new BasicHttpParams();
									// Set the timeout in milliseconds until a
									// connection is
									// established.
									// The default value is zero, that means the
									// timeout is not used.
									HttpConnectionParams.setConnectionTimeout(httpParameters,
											AntripService2.CONNECTION_TIMEOUT);
									// Set the default socket timeout
									// (SO_TIMEOUT)
									// in milliseconds which is the timeout for
									// waiting for
									// data.
									int timeoutSocket = 10000;
									HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
									
									DefaultHttpClient client = new DefaultHttpClient(httpParameters);
									try {
										HttpResponse response = client.execute(getRequest);
										
										Integer statusCode = response.getStatusLine().getStatusCode();
										if (statusCode == 200) {
											
											BufferedReader in = new BufferedReader(new InputStreamReader(response
													.getEntity().getContent()));
											JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
											in.close();
											
											JSONArray array = result.getJSONArray("user_list");
											cachedQuery.put(prefix, array);
											
											return true;
										}
									} catch (IOException e) {
										e.printStackTrace();
									} catch (JSONException e) {
										e.printStackTrace();
									} finally {
										client.getConnectionManager().shutdown();
									}
									return false;
								} else {
									return false;
								}
							}
							protected void onPostExecute(Boolean result) {
								((TextView) listview.getEmptyView().findViewById(R.id.emptytext)).setText(R.string.friendfinder_emptylist);
								((ProgressBar) listview.getEmptyView().findViewById(R.id.emptyprogressbar)).setVisibility(View.GONE);
								if(result){
									//operation success, add
									queriedPrefix.add(prefix);
								} else{
									
								}
								//cachedquery will return null if no mapping exists
								adapter = new FriendFinderListAdapter(mContext, cachedQuery.get(prefix), FriendFinder.this, userid);
								listview.setAdapter(adapter);
							};
						}.execute();
					} else{
						//already queried, get the cached data and set to adapter
						adapter = new FriendFinderListAdapter(mContext, cachedQuery.get(prefix), FriendFinder.this, userid);
						listview.setAdapter(adapter);
					}
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
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
	public void requestViewsInvalidation() {
		if(listview != null){
			listview.invalidateViews();
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

	@Override
	public void removePosition(int position) {
		//do nothing...
	}
}