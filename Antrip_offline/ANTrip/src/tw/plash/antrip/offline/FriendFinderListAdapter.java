package tw.plash.antrip.offline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendFinderListAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	
	final private ArrayList<JSONObject> data;
	
	final private SparseArray<String> clickedd;
	
	final private InvalidateViewsCallback ivc;
	
	final private String userid;
	
	private String relationship;
	
	final private Context mContext;
	
	public FriendFinderListAdapter(Context context, JSONArray data, InvalidateViewsCallback ivc, String userid) {
		
		this.mContext = context;
		
		this.ivc = ivc;
		
		if(data != null && data.length() > 0){
			this.data = (ArrayList<JSONObject>) JSONUtility.asList(data);
		} else{
			this.data = new ArrayList<JSONObject>();
		}
		
		clickedd = new SparseArray<String>();
		
		this.userid = userid;
		
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return data.size();
	}
	
	@Override
	public Object getItem(int position) {
		return data.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ListHolder holder = null;
		
		if(convertView == null){
			holder = new ListHolder();
			convertView = mInflater.inflate(R.layout.friendfinderlistitem, null);
			holder.image = (ImageView) convertView.findViewById(R.id.friendfinder_image);
			holder.name = (TextView) convertView.findViewById(R.id.friendfinder_name);
			holder.button = (ImageView) convertView.findViewById(R.id.friendfinder_button);
			convertView.setTag(holder);
		} else{
			holder = (ListHolder) convertView.getTag();
		}
		
		holder.button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				Log.e("friendfinderadapter", "button clicked: " + position);
				if (clickedd.get(position) == null || clickedd.get(position).equals("failed")) {
					//button either had not been pressed, or request failed
					try {
						if (data.get(position).getString("isFriend").equals("true")) {
							// already friends, show warning toast
							Toast.makeText(mContext, R.string.toast_alreadyfriends, Toast.LENGTH_SHORT).show();
						} else if (data.get(position).getString("isFriend").equals("pending")) {
							// pending response, show be patient toast
							Toast.makeText(mContext, R.string.toast_friendrequestpending, Toast.LENGTH_SHORT).show();
						} else if (data.get(position).getString("isFriend").equals("false")) {
							// if not already working
							
							// not friends yet, send request
							new AsyncTask<Void, Void, Boolean>() {
								String name = null;
								
								protected void onPreExecute() {
									clickedd.put(position, "working");
									// show indefinite progress bar XXX
									((ImageView) v).setImageResource(R.drawable.friendfinder_working);
									try {
										name = data.get(position).getString("name");
									} catch (JSONException e) {
										e.printStackTrace();
										name = null;
									}
									Log.e("friendfinderadapter", "request " + name + " to be friend");
									if (name == null) {
										cancel(true);
									}
								};
								
								@Override
								protected Boolean doInBackground(Void... params) {
									
									// start connection to send add friend
									// request
									// need userid and friendname
									if (userid != null && !userid.equals("-1") && name != null && name.length() > 0) {
										
										String url = "http://plash2.iis.sinica.edu.tw/api/FriendRequest.php?user_id="
												+ userid + "&friend_name=" + InternetUtility.encode(name);
										
										HttpGet getRequest = new HttpGet(url);
										
										HttpParams httpParameters = new BasicHttpParams();
										// Set the timeout in milliseconds until
										// a connection is
										// established.
										// The default value is zero, that means
										// the timeout is not used.
										HttpConnectionParams.setConnectionTimeout(httpParameters,
												AntripService2.CONNECTION_TIMEOUT);
										// Set the default socket timeout
										// (SO_TIMEOUT)
										// in milliseconds which is the timeout
										// for waiting for
										// data.
										int timeoutSocket = 10000;
										HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
										
										DefaultHttpClient client = new DefaultHttpClient(httpParameters);
										try {
											HttpResponse response = client.execute(getRequest);
											
											Integer statusCode = response.getStatusLine().getStatusCode();
											if (statusCode == 200) {
												
												BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
												JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
												in.close();
												
												if(result.getInt("code") == 200){
													//correct parameters, no exception, no connection error, successful operation
													return true;
												}
											}
										} catch (IOException e) {
											e.printStackTrace();
										} catch (JSONException e) {
											e.printStackTrace();
										} finally {
											client.getConnectionManager().shutdown();
										}
									}
									// incorrect parameters, exceptions,
									// connection error, or failed operation
									return false;
								}
								
								protected void onPostExecute(Boolean result) {
									if (result) {
										clickedd.put(position, "good");
										Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_friendrequest_success) + " " + name, Toast.LENGTH_SHORT).show();
									} else {
										clickedd.put(position, "failed");
										Toast.makeText(mContext, R.string.toast_friendrequest_fail, Toast.LENGTH_SHORT).show();
									}
									ivc.requestViewsInvalidation();
								};
							}.execute();
							
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else if(clickedd.get(position).equals("working")){
//					Toast.makeText(mContext, "sending request...", Toast.LENGTH_SHORT).show();
				} else if(clickedd.get(position).equals("good")){
//					Toast.makeText(mContext, "request already sent", Toast.LENGTH_SHORT).show();
				} else {
					//no more other cases here, i think
				}
			}
		});
		
		if(clickedd.get(position) != null){
			if(clickedd.get(position).equals("working")){
				//still working XXX
				holder.button.setImageResource(R.drawable.friendfinder_working);
			} else if(clickedd.get(position).equals("good")){
				//now wait for reponse XXX
				holder.button.setImageResource(R.drawable.friendfinder_pending);
			} else{
				//failed, try again later XXX
				holder.button.setImageResource(R.color.button_state_friendfinderadd);
			}
		} else{
			try {
				relationship = data.get(position).getString("isFriend");
			} catch (JSONException e1) {
				e1.printStackTrace();
				//default to "not friend"
				relationship = "false";
			}
			//not clicked, show image according to relationship
			if(relationship.equals("true")){
				//already friend XXX
				holder.button.setImageResource(R.drawable.friendfinder_alreadyfriends);
			} else if(relationship.equals("pending")){
				//response pending XXX
				holder.button.setImageResource(R.drawable.friendfinder_pending);
			} else if(relationship.equals("false")){
				//not friend XXX
				//can send friend request
				holder.button.setImageResource(R.color.button_state_friendfinderadd);
			} else{
				//should not happen
				Log.e("friendfinderadapter", "unknown friednd relationship");
			}
		}
		
		//should display user image...
		holder.image.setImageResource(R.drawable.antrip_logo);
		try {
			holder.name.setText(data.get(position).getString("name"));
		} catch (JSONException e) {
			e.printStackTrace();
			holder.name.setText("name");
		}
		return convertView;
	}

	class ListHolder{
		ImageView image; //XXX asks ivan how empty image can be more attractive...
		TextView name;
		ImageView button; //XXX might need to change this into imagebutton
	}
}
