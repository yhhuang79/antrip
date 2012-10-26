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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class UploadThread extends AsyncTask<Void, Void, Integer>{
	
	private Context mContext;
	private DBHelper128 dh;
	private String uniqueid;
	private String userid;
	private String oldTripid;
	private String newTripid;
	
	private ProgressDialog diag;
	
	private TripListReloader reloader;
	
	final Address nullAddr = new Address(Locale.getDefault());
	final ArrayList<Address> nulladdrlist = new ArrayList<Address>();
	
	public UploadThread(Context c, String id, TripListReloader tlr) {
		mContext = c;
		dh = new DBHelper128(mContext);
		uniqueid = id;
		userid = PreferenceManager.getDefaultSharedPreferences(mContext).getString("sid", null);
		reloader = tlr;
		nulladdrlist.add(nullAddr);
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if(uniqueid == null || userid == null || !isNetworkAvailable()){
			cancel(false);
		} else{
			diag = new ProgressDialog(mContext);
			diag.setTitle("Uploading...");
			diag.setCancelable(true);
			diag.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			diag.setMax(100);
			diag.setProgress(0);
			diag.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					Log.e("uploadthread", "progressdiag: canceled");
				}
			});
			diag.show();
		}
	}
	
	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		diag.setProgress((diag.getProgress() + 10));
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		//XXX unique id -> old trip id
		oldTripid = getOldTripId();
		Log.e("uploadthread", "doinbg: 1");
		publishProgress();
		//XXX old trip id -> new trip id
		newTripid = getNewTripId();
		Log.e("uploadthread", "doinbg: 2");
		publishProgress();
		//XXX get start/end address
		List<Address> startAddr = getaddress(userid, oldTripid, true);
		Log.e("uploadthread", "doinbg: 3");
		publishProgress();
		List<Address> endAddr = getaddress(userid, oldTripid, false);
		Log.e("uploadthread", "doinbg: 4");
		publishProgress();
		//XXX commit all to DB
		if(newTripid != null){
			//all good, commit everything to DB, and  save new upload stage
			if(startAddr == null){
				startAddr = nulladdrlist;
			}
			if(endAddr == null){
				endAddr = nulladdrlist;
			}
			commitAlltoDB(startAddr, endAddr);
			Log.e("uploadthread", "doinbg: 5~~~~");
			publishProgress();
			//XXX upload info
			if(uploadtripinfo()){
				dh.updateUploadStage(userid, newTripid, 2);
				Log.e("uploadthread", "doinbg: 6");
				publishProgress();
				if(uploadtripdata()){
					//XXX upload data
					dh.updateUploadStage(userid, newTripid, 3);
					Log.e("uploadthread", "doinbg: 7");
					publishProgress();
					if(uploadpictures()){
						//XXX upload pictures
						dh.updateUploadStage(userid, newTripid, 4);
						Log.e("uploadthread", "doinbg: 8");
						publishProgress();
						publishProgress();
						publishProgress();
						
					} else{
						return null;
					}
				} else{
					return null;
				}
			} else{
				return null;
			}
		}else{
			return null;
		}
		return 1;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if(result == null){
			new AlertDialog.Builder(mContext)
			.setTitle(R.string.error)
			.setMessage(R.string.upload_failed_warning)
			.setNeutralButton(R.string.okay, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			})
			.show();
		} else{
			Toast.makeText(mContext, "upload comeplete~~", Toast.LENGTH_LONG).show();
		}
		cleanup();
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
		cleanup();
	}
	
	private String getOldTripId(){
		return dh.getTripid(uniqueid);
	}
	
	private String getNewTripId(){
		try{
			// the client to handle sending/receiving requests
			HttpClient httpsClient = getHttpClient();
			//first, update trip info and data with the correct tripid
			String newTripidUrl = "https://plash.iis.sinica.edu.tw:8080/GetNewTripId?userid=" + userid;
			//Log.w("newTripidUrl", newTripidUrl);
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
					//Log.w("update tripid", "rows= " + num);
					httpsClient.getConnectionManager().shutdown();
					return newTripid;
				} else{
					//did not received a new tripid, abort
					//Log.e("upload service", "getnewrtipid error: new trip id = null");
					httpsClient.getConnectionManager().shutdown();
					return null;
				}
			} else{
				//connection failed, abort
				//Log.e("upload service", "getnewrtipid error: status code=" + statusCode);
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
		//Log.e("upload service", "getnewrtipid error: exception");
		return null;
	}
	
	private List<Address> getaddress(String uid, String tid, boolean start){
		try{
			CachedPoints point = dh.getOnePoint(uid, tid, start);
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			List<Address> addr = geocoder.getFromLocation(point.latitude, point.longitude, 1);
			if(addr != null && !addr.isEmpty()){
				//Log.w("upload service", "first address: \nadmin:" + firstAddr.get(0).getAdminArea() + "\ncountry code:" + firstAddr.get(0).getCountryCode() + "\ncountru name:" + firstAddr.get(0).getCountryName() + "\nfeature name:" + firstAddr.get(0).getFeatureName() + "\nlocale:" + firstAddr.get(0).getLocale() + "\nlocality:" + firstAddr.get(0).getLocality() + "\npostal code:" + firstAddr.get(0).getPostalCode() + "\npremises:" + firstAddr.get(0).getPremises() + "\nsubadmin:" + firstAddr.get(0).getSubAdminArea() + "\nsublocality:" + firstAddr.get(0).getSubLocality() + "\nsubthroughfare:" + firstAddr.get(0).getSubThoroughfare() + "\nthroughfare:" + firstAddr.get(0).getThoroughfare());
//				dh.setStartaddr(
//						uid, 
//						tid, 
//						(addr.get(0).getCountryName() != null?addr.get(0).getCountryName():"NULL"), 
//						(addr.get(0).getAdminArea() != null?addr.get(0).getAdminArea():"NULL"), 
//						(addr.get(0).getLocality() != null?addr.get(0).getLocality():"NULL"), 
//						(addr.get(0).getSubLocality() != null?addr.get(0).getSubLocality():"NULL"), 
//						(addr.get(0).getThoroughfare() != null?addr.get(0).getThoroughfare():"NULL"));
				//Log.w("upload service", "getstartaddress result: good");
				return addr;
			}
		} catch (IOException e) {
			//Log.e("upload service", "getstartaddress error: exception");
			e.printStackTrace();
		}
		return null;
	}
	
	private void commitAlltoDB(List<Address> startaddr, List<Address> endaddr){
		dh.updateTripid(oldTripid, newTripid);
		dh.setStartaddr(userid, newTripid, startaddr);
		dh.setEndaddr(userid, newTripid, endaddr);
		dh.updateUploadStage(userid, newTripid, 1);
	}
	
	private boolean uploadtripinfo(){
		try{
			JSONObject tripinfo = dh.getOneTripInfo(userid, newTripid);
			
			String inputtripinfo = "https://plash.iis.sinica.edu.tw:8080/InputTripInfoComponent?userid=" + userid + "&trip_id=" + newTripid + "&update_status=3&trip_name="
			+ tripinfo.getString("trip_name")
			+ "&trip_st=" + tripinfo.getString("trip_st")
			+ "&trip_et=" + tripinfo.getString("trip_et")
			//convert length from double to integer, cuz eugene likes integer...
			+ "&trip_length=" + (int)Double.parseDouble(tripinfo.getString("trip_length"))
			+ "&num_of_pts=" + tripinfo.getString("num_of_pts");
			
			//if reverse geocoding failed, don't upload empty addresses
			String stAddr = 
			"&st_addr_prt1=" + tripinfo.getString("st_addr_prt1")
			+ "&st_addr_prt2=" + tripinfo.getString("st_addr_prt2")
			+ "&st_addr_prt3=" + tripinfo.getString("st_addr_prt3")
			+ "&st_addr_prt4=" + tripinfo.getString("st_addr_prt4")
			+ "&st_addr_prt5=" + tripinfo.getString("st_addr_prt5");
			
			//if reverse geocoding failed, don't upload empty addresses
			String etAddr = 
			"&et_addr_prt1=" + tripinfo.getString("et_addr_prt1")
			+ "&et_addr_prt2=" + tripinfo.getString("et_addr_prt2")
			+ "&et_addr_prt3=" + tripinfo.getString("et_addr_prt3")
			+ "&et_addr_prt4=" + tripinfo.getString("et_addr_prt4")
			+ "&et_addr_prt5=" + tripinfo.getString("et_addr_prt5");
			
			HttpClient httpsClient = getHttpClient();
			HttpGet getRequest = new HttpGet(correctURLEncoder(inputtripinfo + stAddr + etAddr));
			HttpResponse response = httpsClient.execute(getRequest);
			
			Integer statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 200){
				//not sure what will be returned
//				Log.w("upload service", "uploadtripinfo result: good");
				httpsClient.getConnectionManager().shutdown();
				return true;
			} else{
//				Log.e("upload service", "uploadtripinfo error: connection failed, status code=" + statusCode);
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
//		Log.e("upload service", "uploadtripinfo error: exception");
		return false;
	}
	
	private boolean uploadtripdata(){
		try{
			String uploadUrl = "http://plash2.iis.sinica.edu.tw/api/UploadTrip.php";
			//Log.w("uploadUrl", uploadUrl);
			// the method to be used, with url
			HttpPost postRequest = new HttpPost(uploadUrl);
			//GET DATA FROM DB
			JSONObject data = dh.getOneTripData(userid, newTripid, true);
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
					//Log.w("upload service", "uplaodtripdata result: good");
					dh.markUploaded(userid, newTripid, 1, 1, null);
					httpClient.getConnectionManager().shutdown();
					return true;
				} else{
					//Log.e("upload service", "uplaodtripdata error: upload failed, messsage=" + msg);
					httpClient.getConnectionManager().shutdown();
					return false;
				}
			} else {
				// connection error
				//Log.e("upload service", "uplaodtripdata error: connection failed, status code=" + statusCode);
				httpClient.getConnectionManager().shutdown();
				return false;
			}
		} catch(IOException e){
			e.printStackTrace();
		}
		//Log.e("upload service", "uplaodtripdata error: exception");
		return false;
	}
	
	private boolean uploadpictures(){
		try{
			String uploadPicUrl = "http://plash2.iis.sinica.edu.tw/picture/UploadPicture.php";
			HttpPost postRequest = new HttpPost(uploadPicUrl);
			//get a list of all picture paths in the given tripid
			ArrayList<String> picPaths = dh.getOneTripPicturePaths(userid, newTripid, false);
			if(picPaths != null){
				//go through the list and upload every picture
				for(int i = 0; i < picPaths.size(); i++){
					//get the picture path
					String path = picPaths.get(i);
					//Log.w("upload service", "image path=" + path);
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(path)));
					byte[] ba = new byte[bis.available()];
					bis.read(ba);
					bis.close();
					ByteArrayBody bab = new ByteArrayBody(ba, path.substring(path.lastIndexOf("/") + 1));
					ba = null;
					MultipartEntity mentity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
					mentity.addPart("file", bab);
					mentity.addPart("userid", new StringBody(userid));
					mentity.addPart("trip_id", new StringBody(newTripid));
					
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
							//Log.w("upload service", i + ") upload picture result: good");
							dh.markUploaded(userid, newTripid, 1, 2, path);
						} else {
							//Log.e("upload service", i + ") upload pictures error: upload failed, messsage=" + msg);
						}
					} else {
						//Log.e("upload service", i + ") upload picture error: connection error, status code="
//								+ statusCode);
						dh.markUploaded(userid, newTripid, 1, 1, path);
					}
					httpClient.getConnectionManager().shutdown();
				}
				return true;
			} else{
				//no picture la
				//Log.e("upload service", "null image path, no pictures, no need to upload");
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Log.e("upload service", "upload picture error: exception");
		return false;
	}
	
	/**
	 * encode only the parameter part after ? mark, not the ENTIRE url...
	 * @param inURL, the url to be encoded
	 * @return url, complete and correctly encoded
	 */
	private String correctURLEncoder(String inURL){
		String inParameter = inURL.substring(inURL.lastIndexOf("?") + 1);
//		Log.e("correct url encoder", "inParam=" + inParameter);
		String outParameter= null;
		try {
			outParameter = URLEncoder.encode(inParameter, "UTF-8");
//			Log.e("correct url encoder", "outParam=" + outParameter);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String result = inURL.replace(inParameter, outParameter);
//		Log.e("correct url encoder", "result=" + result);
		return result;
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
	
	private void cleanup(){
		Log.e("uploadthread", "cleanup~");
		diag.dismiss();
		if(dh != null){
			dh.closeDB();
			dh = null;
		}
		reloader.shouldReloadTripList();
	}
	
	private boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null) { // in airplane mode, there are no "Active Network", networkInfo will be null
			return ni.isConnected();
		} else {
			return false;
		}
	}
}
