package tw.plash.antrip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class UploadService extends Service {
	
	private SharedPreferences pref;
	
	@Override
	public void onCreate() {
		super.onCreate();
		// check the internet status
		if (!isNetworkAvailable()) {
			// no internet, don't proceed
			Log.e("uploadService", "internet not available");
			stopSelf();
		} else{
			pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		if (action == null) {
			stopSelf();
		} else if (action.equals("ACTION_UPLOAD_TRIP")) {
			//get unique id
			String id = intent.getExtras().getString("id");
			String sid = pref.getString("sid", null);
			Log.e("uploadService", "old tripid= " + id);
			//give the task a dh to handle its own business with DB
			if(id != null && sid != null){
				new uploadThread().execute(id, sid);
			} else{
				Log.e("upload service", "null id error: id= " + id + ", sid= " + sid);
				stopSelf();
			}
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
		
		/**
		 * 0. find tripid from id
		 * 1. get new tripid
		 * 2. reverse geocode start point
		 * 3. reverse geocode end point
		 * 4. upload trip info
		 * 5. upload trip data
		 * 6. upload pictures
		 */
		private boolean[] checkList = {false, false, false, false, false, false, false};
		
		private String correctURLEncoder(String inURL){
			String inParameter = inURL.substring(inURL.lastIndexOf("?") + 1);
			Log.e("correct url encoder", "inParam=" + inParameter);
			String outParameter= null;
			try {
				outParameter = URLEncoder.encode(inParameter, "UTF-8");
				Log.e("correct url encoder", "outParam=" + outParameter);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			String result = inURL.replace(inParameter, outParameter);
			Log.e("correct url encoder", "result=" + result);
			return result;
		}
		
		@Override
		protected Void doInBackground(String... params) {
			String uniqueid = params[0];
			String oldTripid = null;
			String newTripid = null;
			String userid = params[1];
			DBHelper128 dh = new DBHelper128(getApplicationContext());
			try {
				//first we get the old tripid using the unique id value
				oldTripid = dh.getTripid(uniqueid);
				if(oldTripid != null){
					checkList[0] = true;
				} else{
					return null;
				}
				
				// the client to handle sending/receiving requests
				HttpClient httpsClient = getHttpClient();
				//first, update trip info and data with the correct tripid
				String newTripidUrl = "https://plash.iis.sinica.edu.tw:8080/GetNewTripId?userid=" + userid;
				Log.e("newTripidUrl", newTripidUrl);
				HttpGet getRequest = new HttpGet();
				getRequest.setURI(new URI(correctURLEncoder(newTripidUrl)));
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
				checkList[1] = true;
				//XXX get new trip id done
				
				//to be reused later
				getRequest = null;
				response = null;
				statusCode = null;
				//second, fetch start and end address from google
				HttpClient httpClient = new DefaultHttpClient();
				
				CachedPoints first = dh.getOnePoint(userid, newTripid, true);
				
				Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
				List<Address> firstAddr = geocoder.getFromLocation(first.latitude, first.longitude, 1);
				if(firstAddr != null && !firstAddr.isEmpty()){
					Log.e("upload service", "first address: \nadmin:" + firstAddr.get(0).getAdminArea() + "\ncountry code:" + firstAddr.get(0).getCountryCode() + "\ncountru name:" + firstAddr.get(0).getCountryName() + "\nfeature name:" + firstAddr.get(0).getFeatureName() + "\nlocale:" + firstAddr.get(0).getLocale() + "\nlocality:" + firstAddr.get(0).getLocality() + "\npostal code:" + firstAddr.get(0).getPostalCode() + "\npremises:" + firstAddr.get(0).getPremises() + "\nsubadmin:" + firstAddr.get(0).getSubAdminArea() + "\nsublocality:" + firstAddr.get(0).getSubLocality() + "\nsubthroughfare:" + firstAddr.get(0).getSubThoroughfare() + "\nthroughfare:" + firstAddr.get(0).getThoroughfare());
					dh.insertStartaddr(
							userid, 
							newTripid, 
							(firstAddr.get(0).getCountryName() != null?firstAddr.get(0).getCountryName():"NULL"), 
							(firstAddr.get(0).getAdminArea() != null?firstAddr.get(0).getAdminArea():"NULL"), 
							(firstAddr.get(0).getLocality() != null?firstAddr.get(0).getLocality():"NULL"), 
							(firstAddr.get(0).getSubLocality() != null?firstAddr.get(0).getSubLocality():"NULL"), 
							(firstAddr.get(0).getThoroughfare() != null?firstAddr.get(0).getThoroughfare():"NULL"));
					checkList[2] = true;
				} else{
					dh.insertStartaddr(userid, newTripid, "", "", "", "", "Address not available");
					checkList[2] = false;
//					return null;
				}
				
				//XXX reverse-geocoding of starting address done
				
				//to be reused later
				geocoder = null;
				getRequest = null;
				response = null;
				statusCode = null;
				
				CachedPoints last = dh.getOnePoint(userid, newTripid, false);
				geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
				List<Address> lastAddr = geocoder.getFromLocation(last.latitude, last.longitude, 1);
				if(lastAddr != null && !lastAddr.isEmpty()){
					Log.e("upload service", "last address: \nadmin:" + lastAddr.get(0).getAdminArea() + "\ncountry code:" + lastAddr.get(0).getCountryCode() + "\ncountru name:" + lastAddr.get(0).getCountryName() + "\nfeature name:" + lastAddr.get(0).getFeatureName() + "\nlocale:" + lastAddr.get(0).getLocale() + "\nlocality:" + lastAddr.get(0).getLocality() + "\npostal code:" + lastAddr.get(0).getPostalCode() + "\npremises:" + lastAddr.get(0).getPremises() + "\nsubadmin:" + lastAddr.get(0).getSubAdminArea() + "\nsublocality:" + lastAddr.get(0).getSubLocality() + "\nsubthroughfare:" + lastAddr.get(0).getSubThoroughfare() + "\nthroughfare:" + lastAddr.get(0).getThoroughfare());
					dh.insertEndaddr(
							userid, 
							newTripid, 
							(lastAddr.get(0).getCountryName() != null?lastAddr.get(0).getCountryName():"NULL"), 
							(lastAddr.get(0).getAdminArea() != null?lastAddr.get(0).getAdminArea():"NULL"), 
							(lastAddr.get(0).getLocality() != null?lastAddr.get(0).getLocality():"NULL"), 
							(lastAddr.get(0).getSubLocality() != null?lastAddr.get(0).getSubLocality():"NULL"), 
							(lastAddr.get(0).getThoroughfare() != null?lastAddr.get(0).getThoroughfare():"NULL"));
					checkList[3] = true;
				} else{
					dh.insertEndaddr(userid, newTripid, "", "", "", "", "Address not available");
					checkList[3] = false;
//					return null;
				}
				
				//XXX reverse-geocoding of ending address done
				
				//done with geocoder
				geocoder = null;
				
				//to be reused later
				getRequest = null;
				response = null;
				statusCode = null;
				//thirdly, upload trip info with eugene's component
				JSONObject tripinfo = dh.getOneTripInfo(userid, newTripid);
				
				String inputtripinfo = "https://plash.iis.sinica.edu.tw:8080/InputTripInfoComponent?update_status=" + ((checkList[2] && checkList[3])?"2":"1") + "&trip_name="
				+ tripinfo.getString("trip_name")
				+ "&trip_st=" + tripinfo.getString("trip_st")
				+ "&trip_et=" + tripinfo.getString("trip_et")
				+ "&trip_length=" + (int)Double.parseDouble(tripinfo.getString("trip_length"))
				+ "&num_of_pts=" + tripinfo.getString("num_of_pts");
				
				//if reverse geociding failed, don't upload empty addresses
				String stAddr = checkList[2]?
				"&st_addr_prt1=" + tripinfo.getString("st_addr_prt1")
				+ "&st_addr_prt2=" + tripinfo.getString("st_addr_prt2")
				+ "&st_addr_prt3=" + tripinfo.getString("st_addr_prt3")
				+ "&st_addr_prt4=" + tripinfo.getString("st_addr_prt4")
				+ "&st_addr_prt5=" + tripinfo.getString("st_addr_prt5"):"";
				
				//if reverse geociding failed, don't upload empty addresses
				String etAddr = checkList[3]?
				"&et_addr_prt1=" + tripinfo.getString("et_addr_prt1")
				+ "&et_addr_prt2=" + tripinfo.getString("et_addr_prt2")
				+ "&et_addr_prt3=" + tripinfo.getString("et_addr_prt3")
				+ "&et_addr_prt4=" + tripinfo.getString("et_addr_prt4")
				+ "&et_addr_prt5=" + tripinfo.getString("et_addr_prt5"):"";
				
				getRequest = new HttpGet(correctURLEncoder(inputtripinfo + stAddr + etAddr));
				response = httpsClient.execute(getRequest);
				statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200){
					//not sure what will be returned
				} else{
					
					return null;
				}
				checkList[4] = true;
				//XXX input trip info done
				
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
				JSONObject data = dh.getOneTripData(userid, newTripid, true);
				// pair our data with corresponding name
				List<NameValuePair> param = new ArrayList<NameValuePair>();
				param.add(new BasicNameValuePair("trip", data.toString()));
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
				checkList[5] = true;
				//XXX input trip data done
				
				
				
				
				
				// close the connection
				httpClient.getConnectionManager().shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			} catch(JSONException e){
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			if(dh.DBIsOpen()){
				dh.closeDB();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			int i = 0;
			for(boolean item : checkList){
				Log.e("checkList", "part " + i + ": " + (item?"good":"no good"));
				i++;
			}
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
