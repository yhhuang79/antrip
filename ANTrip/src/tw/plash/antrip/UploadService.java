package tw.plash.antrip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class UploadService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		// check the internet status
		if (!isNetworkAvailable()) {
			// no internet, don't proceed
			Log.e("uploadService", "internet not available");
			stopSelf();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		if (action == null) {
			stopSelf();
		} else if (action.equals("ACTION_UPLOAD_TRIP")) {
			//get tripid
			String tid = intent.getExtras().getString("tripid");
			String sid = intent.getExtras().getString("userid");
			Log.e("uploadService", "old tripid= " + tid);
			//give the task a dh to handle its own business with DB
			new uploadThread().execute(tid, sid);
		} else if (action.equals("")) {

		} else {
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	private class uploadThread extends AsyncTask<String, Integer, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			
		}

		@Override
		protected Void doInBackground(String... params) {
			String oldTripid = params[0];
			String newTripid = null;
			String userid = params[1];
			DBHelper128 dh = new DBHelper128(getApplicationContext());
			try {
				// the client to handle sending/receiving requests
				HttpClient httpsClient = getHttpClient();
				//first, update trip info and data with the correct tripid
				String newTripidUrl = "https://plash.iis.sinica.edu.tw:8080/GetNewTripId?userid=" + userid;
				Log.e("newTripidUrl", newTripidUrl);
				HttpGet getRequest = new HttpGet(newTripidUrl);
				HttpResponse response = httpsClient.execute(getRequest);
				Integer statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200){
					BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
					in.close();
					newTripid = result.getString("newTripId");
					if(newTripid != null){
						int num = dh.updateTripid(oldTripid, newTripid);
						Log.e("update tripid", "rows= " + num);
					} else{
						//did not received a new tripid, abort
						return null;
					}
				} else{
					//connection failed, abort
					return null;
				}
				//to be reused later
				getRequest = null;
				response = null;
				statusCode = null;
				//second, fetch start and end address from google
				HttpClient httpClient = new DefaultHttpClient();
				
				CachedPoints first = dh.getOnePoint(userid, newTripid, true);
				String reGeoFirst = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + first.latitude + "," + first.longitude + "&sensor=true";
				Log.e("reGeoFirst", reGeoFirst);
				getRequest = new HttpGet(reGeoFirst);
				response = httpClient.execute(getRequest);
				statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200){
					BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
					in.close();
					if(result.getString("status").equals("OK")){
						JSONArray addr = ((JSONObject)result.getJSONArray("result").get(0)).getJSONArray("address_components");
						dh.insertStartaddr(
								userid, 
								newTripid, 
								((JSONObject)addr.get(4)).getString("long_name"), 
								((JSONObject)addr.get(3)).getString("long_name"), 
								((JSONObject)addr.get(2)).getString("long_name"), 
								((JSONObject)addr.get(1)).getString("long_name"), 
								((JSONObject)addr.get(0)).getString("long_name"));
					} else{
						//error with google geocoding service, put something in DB first
						dh.insertStartaddr(userid, newTripid, "", "", "", "", "Address not available");
					}
				} else{
					dh.insertStartaddr(userid, newTripid, "", "", "", "", "Address not available");
					return null;
				}
				//to be reused later
				getRequest = null;
				response = null;
				statusCode = null;
				CachedPoints last = dh.getOnePoint(userid, newTripid, false);
				String reGeoLast = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + last.latitude + "," + last.longitude + "&sensor=true";
				Log.e("reGeoLast", reGeoLast);
				getRequest = new HttpGet(reGeoFirst);
				response = httpClient.execute(getRequest);
				statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200){
					BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
					in.close();
					if(result.getString("status").equals("OK")){
						JSONArray addr = ((JSONObject)result.getJSONArray("result").get(0)).getJSONArray("address_components");
						dh.insertEndaddr(
								userid, 
								newTripid, 
								((JSONObject)addr.get(4)).getString("long_name"), 
								((JSONObject)addr.get(3)).getString("long_name"), 
								((JSONObject)addr.get(2)).getString("long_name"), 
								((JSONObject)addr.get(1)).getString("long_name"), 
								((JSONObject)addr.get(0)).getString("long_name"));
					} else{
						//error with google geocoding service, put something in DB first
						dh.insertEndaddr(userid, newTripid, "", "", "", "", "Address not available");
					}
				} else{
					dh.insertEndaddr(userid, newTripid, "", "", "", "", "Address not available");
					return null;
				}
				//to be reused later
				getRequest = null;
				response = null;
				statusCode = null;
				//thirdly, upload trip info with eugene's component
				JSONObject tripinfo = dh.getOneTripInfo(userid, newTripid);
				String inputtripinfo = "https://plash.iis.sinica.edu.tw:8080/InputTripInfoComponent?update_status=2&trip_name="
				+ tripinfo.getString("trip_name")
				+ "&trip_st=" + tripinfo.getString("trip_st")
				+ "&trip_et=" + tripinfo.getString("trip_et")
				+ "&trip_length=" + tripinfo.getString("trip_length")
				+ "&num_of_pts=" + tripinfo.getString("num_of_pts")
				+ "&st_addr_prt1=" + tripinfo.getString("st_addr_prt1")
				+ "&st_addr_prt2=" + tripinfo.getString("st_addr_prt2")
				+ "&st_addr_prt3=" + tripinfo.getString("st_addr_prt3")
				+ "&st_addr_prt4=" + tripinfo.getString("st_addr_prt4")
				+ "&st_addr_prt5=" + tripinfo.getString("st_addr_prt5")
				+ "&et_addr_prt1=" + tripinfo.getString("et_addr_prt1")
				+ "&et_addr_prt2=" + tripinfo.getString("et_addr_prt2")
				+ "&et_addr_prt3=" + tripinfo.getString("et_addr_prt3")
				+ "&et_addr_prt4=" + tripinfo.getString("et_addr_prt4")
				+ "&et_addr_prt5=" + tripinfo.getString("et_addr_prt5");
				getRequest = new HttpGet(inputtripinfo);
				response = httpsClient.execute(getRequest);
				statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200){
					//not sure what will be returned
				} else{
					return null;
				}
				//release this
				httpsClient = null;
				//to be reused later
				getRequest = null;
				response = null;
				statusCode = null;
				//finally upload the updated trip data to yu-hsiang's php component
				String uploadUrl = "http://plash2.iis.sinica.edu.tw/api/UploadTrip.php";
				Log.e("uploadUrl", uploadUrl);
//				HttpClient client = getHttpClient();
				// the method to be used, with url
				HttpPost postRequest = new HttpPost(uploadUrl);
				//GET DATA FROM DB
				
				
				
				// pair our data with corresponding name
				List<NameValuePair> param = new ArrayList<NameValuePair>();
				param.add(new BasicNameValuePair("trip", "data"));
				// encode our data with UTF8
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(param,
						HTTP.UTF_8);
				// put our data in the method object
				postRequest.setEntity(entity);
				// execute the request and catch the response
				response = httpClient.execute(postRequest);
				// if received 200 ok status
				statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					// extrace the return message
					BufferedReader in = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					String line;
					while ((line = in.readLine()) != null) {
						Log.e("IT RETURNS:", line);
					}
					in.close();
				} else {
					// connection error
					Log.e("connection error", "status code= " + statusCode);
				}
				// close the connection
				httpClient.getConnectionManager().shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			} catch(JSONException e){
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

		}
	}

	public static HttpClient getHttpClient() {

		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);
			SSLSocketFactory sf = new AndroidSSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			HttpParams params = new BasicHttpParams();
			// set connection timeout
			int timeoutConnection = 5000;
			HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
			// set socket timeout
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(params, timeoutSocket);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);
			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null) { // in airplane mode, there are no "Active Network",
							// networkInfo will be null
			return ni.isConnected();
		} else {
			return false;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
