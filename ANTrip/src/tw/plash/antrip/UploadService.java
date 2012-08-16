package tw.plash.antrip;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
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

import android.app.Notification;
import android.app.PendingIntent;
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
//	private NotificationManager nm;
	private Notification nnn;
	private final int notification_tag = 1338;
	
	private boolean selfCheckAndUploadIsRunning;
	
	// for keeping track of every thread's status
	private HashMap<Long, Integer> threadStatus;
	
	@Override
	public void onCreate() {
		super.onCreate();
		// check the internet status
		if (!isNetworkAvailable()) {
			// no internet, don't proceed
			Log.e("uploadService", "internet not available");
			stopSelf();
		} else{
			threadStatus = new HashMap<Long, Integer>();
			
			selfCheckAndUploadIsRunning = false;
			
			pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		if (action == null) {
			stopSelf();
		} else if (action.equals("ACTION_UPLOAD_TRIP")) {
			// get unique id
			String id = intent.getExtras().getString("id");
			String sid = pref.getString("sid", null);
			Log.e("uploadService", "old tripid= " + id);
			// give the task a dh to handle its own business with DB
			if (id != null && sid != null) {
				Long tag = System.currentTimeMillis();
				updateThreadStatus(tag, 0);
				new uploadThread(tag, sid, id).execute();
				startNotification(0);
			} else {
				Log.e("upload service", "null id error: id= " + id + ", sid= " + sid);
				//don't stop the entire service, there might be other threads working
				//stopSelf();
			}
		} else if (action.equals("ACTION_SELF_CHECK_AND_UPLOAD")) {
			//if self check is in progress, don't start again
			if(!selfCheckAndUploadIsRunning){
				selfCheckAndUploadIsRunning = true;
				String sid = pref.getString("sid", null);
				if(sid != null){
					DBHelper128 dh = new DBHelper128(getApplicationContext());
					HashMap<String, Integer> tmp = dh.getAllUnfinishedUploads(sid);
					dh.closeDB();
					if(tmp != null){
						startNotification(1);
						for(String key : tmp.keySet()){
							Long tag = System.currentTimeMillis();
							updateThreadStatus(tag, 0);
							new uploadThread(tag, sid).execute(key, tmp.get(key).toString());
						}
					}
				} else{
					//not logged in yet, does not know whose records to look for in the DB
					stopSelf();
				}
			}
		} else {
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void startNotification(int which){
		switch(which){
		case 0:
			PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_NO_CREATE);
//			PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, null, PendingIntent.FLAG_NO_CREATE);
			nnn = new Notification(R.drawable.ant_24, " upload has started~", System.currentTimeMillis());
			nnn.flags = Notification.FLAG_ONGOING_EVENT;
			nnn.setLatestEventInfo(getApplicationContext(), "antrip", "upload in progress...", pIntent);
			startForeground(notification_tag, nnn);
			break;
		case 1:
			nnn = new Notification();
			startForeground(notification_tag, nnn);
			break;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
	}
	
	synchronized private void updateThreadStatus(Long tag, int status){
		threadStatus.put(tag, status);
		
		boolean allDone = true;
		for(Integer item : threadStatus.values()){
			//init status for a thread is 0
			//if there's any thread with init status we need to keep running
			if(item < 1){
				allDone = false;
			}
		}
		//if all thread is done, terminate upload service
		if(allDone){
			stopSelf();
		}
	}
	
	/**
	 * 
	 * @author CSZU
	 *
	 */
	private class uploadThread extends AsyncTask<String, Void, boolean[]> {
		
		private Long threadTag = null;
		private String userid = null;
		private String uniqueid = null;
//		private PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), PendingIntent.FLAG_NO_CREATE);;
		
		public uploadThread(Long tag, String sid) {
			threadTag = tag;
			userid = sid;
		}
		
		public uploadThread(Long tag, String sid, String id) {
			threadTag = tag;
			userid = sid;
			uniqueid = id;
		}
		
		/**
		 * encode only the parameter part after ? mark, not the ENTIRE url...
		 * @param inURL, the url to be encoded
		 * @return url, complete and correctly encoded
		 */
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
		protected boolean[] doInBackground(String... params) {
			boolean[] checkList = { false, false, false, false, false, false, false};
			
			DBHelper128 dh = new DBHelper128(getApplicationContext());
			
			String newTripid = null;
			int job = -1;
			
			if(uniqueid != null){
				//this is a manually selected trip to be uploaded
				job = 0;
			} else if(params.length > 0){
				job = Integer.parseInt(params[0]);
				newTripid = params[1];
			}
			
			switch(job){
			case 0:
				
				// XXX step 1: get the old tripid using the unique id value
				String oldTripid = dh.getTripid(uniqueid);
				if (oldTripid != null) {
					checkList[0] = true;
				} else {
					dh.closeDB();
					return checkList;
				}
				
				// XXX step 2: get new trip id from server
				newTripid = getnewtripid(oldTripid, userid, dh);
				if (newTripid != null) {
					checkList[1] = true;
					dh.markUploaded(userid, newTripid, 0, 2, null);
				} else {
					dh.closeDB();
					return checkList;
				}
			case 1:
				sendBroadcast(new Intent(getApplicationContext(), ANTripActivity.class).setAction("ACTION_RELOAD_TRIPLIST"));
			case 2:
			case 3:
				// XXX step 3: reverse geocode start point
				if (getstartaddress(userid, newTripid, dh)) {
					checkList[2] = true;
					dh.markUploaded(userid, newTripid, 0, 3, null);
				} else{
					// it's okay if we can't do reverse geocoding, let our server handle it
				}
			case 4:
				// XXX step 4: reverse geocode end point
				if (getendaddress(userid, newTripid, dh)) {
					checkList[3] = true;
					dh.markUploaded(userid, newTripid, 0, 4, null);
				} else{
					// it's okay if we can't do reverse geocoding, let our server handle it
				}
			case 5:
				// XXX step 5: upload trip info
				if (uploadtripinfo(userid, newTripid, dh, checkList)) {
					checkList[4] = true;
					dh.markUploaded(userid, newTripid, 0, 5, null);
				} else {
					// it's okay to fail here, just try again later
				}
			case 6:
				// XXX step 6: upload trip data
				if (uploadtripdata(userid, newTripid, dh)) {
					checkList[5] = true;
					dh.markUploaded(userid, newTripid, 0, 6, null);
				} else {
					// it's okay to fail here, just try again later
				}
			case 7:
				// XXX step 7: upload pictures(if present)
				if(uploadpictures(userid, newTripid, dh)){
					checkList[6] = true;
					dh.markUploaded(userid, newTripid, 0, 7, null);
				} else{
					// it's okay to fail here, just try again later
				}
			default:
				break;
			}
			
			boolean allgood = true;
			for(boolean item : checkList){
				if(!item){
					allgood = false;
				}
			}
			if(allgood){
				dh.deleteTrip(userid, newTripid);
			}
			
			if (dh.DBIsOpen()) {
				dh.closeDB();
			}
			return checkList;
		}
		
		private String getnewtripid(String oldtripid, String userid, DBHelper128 dh){
			try{
				// the client to handle sending/receiving requests
				HttpClient httpsClient = getHttpClient();
				//first, update trip info and data with the correct tripid
				String newTripidUrl = "https://plash.iis.sinica.edu.tw:8080/GetNewTripId?userid=" + userid;
				Log.w("newTripidUrl", newTripidUrl);
				HttpGet getRequest = new HttpGet();
				getRequest.setURI(new URI(correctURLEncoder(newTripidUrl)));
				HttpResponse response = httpsClient.execute(getRequest);
				
				Integer statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200){
					BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					JSONObject result = new JSONObject(new JSONTokener(in.readLine()));
					in.close();
					String newTripid = result.getString("newTripId");
					if(newTripid != null){
						int num = dh.updateTripid(oldtripid, newTripid);
						Log.w("update tripid", "rows= " + num);
						httpsClient.getConnectionManager().shutdown();
						return newTripid;
					} else{
						//did not received a new tripid, abort
						Log.e("upload service", "getnewrtipid error: new trip id = null");
						httpsClient.getConnectionManager().shutdown();
						return null;
					}
				} else{
					//connection failed, abort
					Log.e("upload service", "getnewrtipid error: status code=" + statusCode);
					httpsClient.getConnectionManager().shutdown();
					return null;
				}
			} catch(IOException e){
				e.printStackTrace();
			} catch(URISyntaxException e){
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.e("upload service", "getnewrtipid error: exception");
			return null;
		}
		
		private boolean getstartaddress(String uid, String tid, DBHelper128 dh){
			try{
				CachedPoints first = dh.getOnePoint(uid, tid, true);
				Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
				List<Address> firstAddr = geocoder.getFromLocation(first.latitude, first.longitude, 1);
				if(firstAddr != null && !firstAddr.isEmpty()){
					Log.w("upload service", "first address: \nadmin:" + firstAddr.get(0).getAdminArea() + "\ncountry code:" + firstAddr.get(0).getCountryCode() + "\ncountru name:" + firstAddr.get(0).getCountryName() + "\nfeature name:" + firstAddr.get(0).getFeatureName() + "\nlocale:" + firstAddr.get(0).getLocale() + "\nlocality:" + firstAddr.get(0).getLocality() + "\npostal code:" + firstAddr.get(0).getPostalCode() + "\npremises:" + firstAddr.get(0).getPremises() + "\nsubadmin:" + firstAddr.get(0).getSubAdminArea() + "\nsublocality:" + firstAddr.get(0).getSubLocality() + "\nsubthroughfare:" + firstAddr.get(0).getSubThoroughfare() + "\nthroughfare:" + firstAddr.get(0).getThoroughfare());
					dh.insertStartaddr(
							uid, 
							tid, 
							(firstAddr.get(0).getCountryName() != null?firstAddr.get(0).getCountryName():"NULL"), 
							(firstAddr.get(0).getAdminArea() != null?firstAddr.get(0).getAdminArea():"NULL"), 
							(firstAddr.get(0).getLocality() != null?firstAddr.get(0).getLocality():"NULL"), 
							(firstAddr.get(0).getSubLocality() != null?firstAddr.get(0).getSubLocality():"NULL"), 
							(firstAddr.get(0).getThoroughfare() != null?firstAddr.get(0).getThoroughfare():"NULL"));
					Log.w("upload service", "getstartaddress result: good");
					return true;
				} else{
					dh.insertStartaddr(uid, tid, "", "", "", "", "Address not available");
					Log.e("upload service", "getstartaddress error: reverse geocode failed");
					return false;
				}
			} catch (IOException e) {
				Log.e("upload service", "getstartaddress error: exception");
				e.printStackTrace();
			}
			return false;
		}
		
		private boolean getendaddress(String uid, String tid, DBHelper128 dh){
			try{	
				CachedPoints last = dh.getOnePoint(uid, tid, false);
				Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
				List<Address> lastAddr = geocoder.getFromLocation(last.latitude, last.longitude, 1);
				if(lastAddr != null && !lastAddr.isEmpty()){
					Log.w("upload service", "last address: \nadmin:" + lastAddr.get(0).getAdminArea() + "\ncountry code:" + lastAddr.get(0).getCountryCode() + "\ncountru name:" + lastAddr.get(0).getCountryName() + "\nfeature name:" + lastAddr.get(0).getFeatureName() + "\nlocale:" + lastAddr.get(0).getLocale() + "\nlocality:" + lastAddr.get(0).getLocality() + "\npostal code:" + lastAddr.get(0).getPostalCode() + "\npremises:" + lastAddr.get(0).getPremises() + "\nsubadmin:" + lastAddr.get(0).getSubAdminArea() + "\nsublocality:" + lastAddr.get(0).getSubLocality() + "\nsubthroughfare:" + lastAddr.get(0).getSubThoroughfare() + "\nthroughfare:" + lastAddr.get(0).getThoroughfare());
					dh.insertEndaddr(
							uid, 
							tid, 
							(lastAddr.get(0).getCountryName() != null?lastAddr.get(0).getCountryName():"NULL"), 
							(lastAddr.get(0).getAdminArea() != null?lastAddr.get(0).getAdminArea():"NULL"), 
							(lastAddr.get(0).getLocality() != null?lastAddr.get(0).getLocality():"NULL"), 
							(lastAddr.get(0).getSubLocality() != null?lastAddr.get(0).getSubLocality():"NULL"), 
							(lastAddr.get(0).getThoroughfare() != null?lastAddr.get(0).getThoroughfare():"NULL"));
					Log.w("upload service", "getendaddress result: good");
					return true;
				} else{
					dh.insertEndaddr(uid, tid, "", "", "", "", "Address not available");
					Log.e("upload service", "getendaddress error: reverse geocode failed");
					return false;
				}
			} catch(IOException e){
				e.printStackTrace();
			}
			Log.e("upload service", "getendaddress error: exception");
			return false;
		}
		
		private boolean uploadtripinfo(String uid, String tid, DBHelper128 dh, boolean[] check){
			try{
				JSONObject tripinfo = dh.getOneTripInfo(uid, tid);
				
				String inputtripinfo = "https://plash.iis.sinica.edu.tw:8080/InputTripInfoComponent?update_status=" + ((check[2] && check[3])?"2":"1") + "&trip_name="
				+ tripinfo.getString("trip_name")
				+ "&trip_st=" + tripinfo.getString("trip_st")
				+ "&trip_et=" + tripinfo.getString("trip_et")
				//convert length from double to integer, cuz eugene likes integer...
				+ "&trip_length=" + (int)Double.parseDouble(tripinfo.getString("trip_length"))
				+ "&num_of_pts=" + tripinfo.getString("num_of_pts");
				
				//if reverse geociding failed, don't upload empty addresses
				String stAddr = check[2]?
				"&st_addr_prt1=" + tripinfo.getString("st_addr_prt1")
				+ "&st_addr_prt2=" + tripinfo.getString("st_addr_prt2")
				+ "&st_addr_prt3=" + tripinfo.getString("st_addr_prt3")
				+ "&st_addr_prt4=" + tripinfo.getString("st_addr_prt4")
				+ "&st_addr_prt5=" + tripinfo.getString("st_addr_prt5"):"";
				
				//if reverse geociding failed, don't upload empty addresses
				String etAddr = check[3]?
				"&et_addr_prt1=" + tripinfo.getString("et_addr_prt1")
				+ "&et_addr_prt2=" + tripinfo.getString("et_addr_prt2")
				+ "&et_addr_prt3=" + tripinfo.getString("et_addr_prt3")
				+ "&et_addr_prt4=" + tripinfo.getString("et_addr_prt4")
				+ "&et_addr_prt5=" + tripinfo.getString("et_addr_prt5"):"";
				
				HttpClient httpsClient = getHttpClient();
				HttpGet getRequest = new HttpGet(correctURLEncoder(inputtripinfo + stAddr + etAddr));
				HttpResponse response = httpsClient.execute(getRequest);
				
				Integer statusCode = response.getStatusLine().getStatusCode();
				if(statusCode == 200){
					//not sure what will be returned
					Log.w("upload service", "uploadtripinfo result: good");
					httpsClient.getConnectionManager().shutdown();
					return true;
				} else{
					Log.e("upload service", "uploadtripinfo error: connection failed, status code=" + statusCode);
					httpsClient.getConnectionManager().shutdown();
					return false;
				}
			} catch(JSONException e){
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.e("upload service", "uploadtripinfo error: exception");
			return false;
		}
		
		private boolean uploadtripdata(String uid, String tid, DBHelper128 dh){
			try{
				String uploadUrl = "http://plash2.iis.sinica.edu.tw/api/UploadTrip.php";
				Log.w("uploadUrl", uploadUrl);
				// the method to be used, with url
				HttpPost postRequest = new HttpPost(uploadUrl);
				//GET DATA FROM DB
				JSONObject data = dh.getOneTripData(uid, tid, true);
				// pair our data with corresponding name
				List<NameValuePair> param = new ArrayList<NameValuePair>();
				param.add(new BasicNameValuePair("trip", data.toString()));
				// encode our data with UTF8
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(param, HTTP.UTF_8);
				// put our data in the method object
				postRequest.setEntity(entity);
				HttpClient httpClient = new DefaultHttpClient();
				// execute the request and catch the response
				HttpResponse response = httpClient.execute(postRequest);
				
				// if received 200 ok status
				Integer statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					// extrace the return message
					BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					String msg = in.readLine();
					in.close();
					if(msg.contains("Ok")){
						Log.w("upload service", "uplaodtripdata result: good");
						dh.markUploaded(uid, tid, 1, 1, null);
						httpClient.getConnectionManager().shutdown();
						return true;
					} else{
						Log.e("upload service", "uplaodtripdata error: upload failed, messsage=" + msg);
						httpClient.getConnectionManager().shutdown();
						return false;
					}
				} else {
					// connection error
					Log.e("upload service", "uplaodtripdata error: connection failed, status code=" + statusCode);
					httpClient.getConnectionManager().shutdown();
					return false;
				}
			} catch(IOException e){
				e.printStackTrace();
			}
			Log.e("upload service", "uplaodtripdata error: exception");
			return false;
		}
		private boolean uploadpictures(String uid, String tid, DBHelper128 dh){
			try{
				String uploadPicUrl = "http://plash2.iis.sinica.edu.tw/picture/UploadPicture.php";
				HttpPost postRequest = new HttpPost(uploadPicUrl);
				//get a list of all picture paths in the given tripid
				ArrayList<String> picPaths = dh.getOneTripPicturePaths(uid, tid, false);
				if(picPaths != null){
					//go through the list and upload every picture
					for(int i = 0; i < picPaths.size(); i++){
						//get the picture path
						String path = picPaths.get(i);
						Log.w("upload service", "image path=" + path);
						BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(path)));
						byte[] ba = new byte[bis.available()];
						bis.read(ba);
						bis.close();
						ByteArrayBody bab = new ByteArrayBody(ba, path.substring(path.lastIndexOf("/") + 1));
						ba = null;
						MultipartEntity mentity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
						mentity.addPart("file", bab);
						mentity.addPart("userid", new StringBody(uid));
						mentity.addPart("trip_id", new StringBody(tid));
						
						postRequest.setEntity(mentity);
						
						HttpClient httpClient = new DefaultHttpClient();
						HttpResponse response = httpClient.execute(postRequest);
						Integer statusCode = response.getStatusLine().getStatusCode();
						if (statusCode == 200) {
							BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity()
									.getContent()));
							String msg = in.readLine();
							in.close();
							if (msg.contains("OK")) {
								Log.w("upload service", i + ") upload picture result: good");
								dh.markUploaded(uid, tid, 1, 2, path);
							} else {
								Log.e("upload service", i + ") upload pictures error: upload failed, messsage=" + msg);
							}
						} else {
							Log.e("upload service", i + ") upload picture error: connection error, status code="
									+ statusCode);
							dh.markUploaded(uid, tid, 1, 1, path);
						}
						httpClient.getConnectionManager().shutdown();
					}
					return true;
				} else{
					//no picture la
					Log.e("upload service", "null image path, no pictures, no need to upload");
					return true;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.e("upload service", "upload picture error: exception");
			return false;
		}
		
		@Override
		protected void onPostExecute(boolean[] result) {
			boolean allgood = true;
			for(boolean item : result){
				if(!item){
					allgood = false;
				}
			}
			updateThreadStatus(threadTag, allgood?2:1);
			Log.e("upload service", "upload result:\n" 
			+ "get local tripid: " + (result[0]?"good":"bad")
			+ "\nget new trip id: " + (result[1]?"good":"bad")
			+ "\nget start address: " + (result[2]?"good":"bad")
			+ "\nget end address: " + (result[3]?"good":"bad")
			+ "\nupload trip info: " + (result[4]?"good":"bad")
			+ "\nupload trip data: " + (result[5]?"good":"bad")
			+ "\nupload pictures: " + (result[6]?"good":"bad"));
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
