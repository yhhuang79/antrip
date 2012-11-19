package tw.plash.antrip;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
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
import android.content.DialogInterface.OnClickListener;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class UploadThread extends AsyncTask<Void, Void, Integer> {
	
	private Context mContext;
	private DBHelper128 dh;
	private String uniqueid;
	private String userid;
	private String localTripid;
	
	private ProgressDialog diag;
	
	private TripListReloader reloader;
	
	public UploadThread(Context c, String id, TripListReloader tlr) {
		mContext = c;
		dh = new DBHelper128(mContext);
		uniqueid = id;
		userid = PreferenceManager.getDefaultSharedPreferences(mContext).getString("sid", null);
		reloader = tlr;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (uniqueid == null || userid == null || !isNetworkAvailable()) {
			cancel(false);
		} else {
			diag = new ProgressDialog(mContext);
			diag.setTitle("Uploading...");
			diag.setCancelable(false);
			diag.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			diag.setMax(100);
			diag.setProgress(0);
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
		// XXX unique id -> old trip id
		localTripid = getLocalTripId();
		Log.e("uploadthread", "1: get local tripid done, tripid= " + localTripid);
		if (localTripid != null) {
			// cannot proceed with null tripid
			publishProgress();
			int uploadStatus = dh.getUploadStage(userid, localTripid);
			Log.e("uploadthread", "1.5: get uploadstatus done, uploadstatus= " + uploadStatus);
			switch (uploadStatus) {
			case -1:
				return -2;
			case 0:
				// no upload attempted yet
				List<Address> startAddr = getaddress(userid, localTripid, true);
				Log.e("uploadthread", "2: get start address done");
				publishProgress();
				publishProgress();
				List<Address> endAddr = getaddress(userid, localTripid, false);
				Log.e("uploadthread", "3: get end address done");
				publishProgress();
				// all good, commit everything to DB, and save new upload stage
				commitAlltoDB(startAddr, endAddr);
				dh.updateUploadStage(userid, localTripid, 1);
				Log.e("uploadthread", "4: save addresses to DB done");
				publishProgress();
			case 1:
				// addresses parsed
				if (uploadtripdata2()) {
					dh.updateUploadStage(userid, localTripid, 3);
					Log.e("uploadthread", "5: upload trip info/data done");
					publishProgress();
				} else {
					Log.e("uploadthread", "5 ERROR: upload trip info/data failed");
					return 2;
				}
			case 2:
				// no such case
			case 3:
				// tripinfo/data uploaded
				if (uploadpictures()) {
					dh.updateUploadStage(userid, localTripid, 4);
					Log.e("uploadthread", "6: upload pictures done");
					publishProgress();
					publishProgress();
					publishProgress();
				} else {
					Log.e("uploadthread", "6 ERROR: upload pictures failed");
					return 3;
				}
			case 4:
				// just need to confirm all done
				if(confirmAllDone()){
					dh.updateUploadStage(userid, localTripid, 5);
					Log.e("uploadthread", "7: confirm with server done");
					publishProgress();
				} else{
					Log.e("uploadthread", "7 ERROR: confirm with server failed");
					return 4;
				}
			default:
				break;
			}
			return 1;
		} else {
			Log.e("uploadthread", "ERROR: null tripid");
			return -1;
		}
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		switch (result) {
		case 1:
			Toast.makeText(mContext, R.string.upload_complete, Toast.LENGTH_LONG).show();
			cleanup(true);
			break;
		case -1:
		case -2:
		case 0:
		case 2:
		case 3:
		default:
			new AlertDialog.Builder(mContext).setTitle(R.string.error).setMessage(R.string.upload_failed_warning)
					.setNeutralButton(R.string.okay, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
			cleanup(false);
			break;
		}
	}
	
	private String getLocalTripId() {
		return dh.getTripid(uniqueid);
	}
	
	private List<Address> getaddress(String uid, String tid, boolean start) {
		try {
			CachedPoints point = dh.getOnePoint(uid, tid, start);
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			List<Address> addr = geocoder.getFromLocation(point.latitude, point.longitude, 1);
			if (addr != null && !addr.isEmpty()) {
				// Log.w("upload service", "first address: \nadmin:" +
				// firstAddr.get(0).getAdminArea() + "\ncountry code:" +
				// firstAddr.get(0).getCountryCode() + "\ncountru name:" +
				// firstAddr.get(0).getCountryName() + "\nfeature name:" +
				// firstAddr.get(0).getFeatureName() + "\nlocale:" +
				// firstAddr.get(0).getLocale() + "\nlocality:" +
				// firstAddr.get(0).getLocality() + "\npostal code:" +
				// firstAddr.get(0).getPostalCode() + "\npremises:" +
				// firstAddr.get(0).getPremises() + "\nsubadmin:" +
				// firstAddr.get(0).getSubAdminArea() + "\nsublocality:" +
				// firstAddr.get(0).getSubLocality() + "\nsubthroughfare:" +
				// firstAddr.get(0).getSubThoroughfare() + "\nthroughfare:" +
				// firstAddr.get(0).getThoroughfare());
				// Log.w("upload service", "getstartaddress result: good");
				return addr;
			}
		} catch (IOException e) {
			// Log.e("upload service", "getstartaddress error: exception");
			e.printStackTrace();
		}
		return null;
	}
	
	private void commitAlltoDB(List<Address> startaddr, List<Address> endaddr) {
		dh.setStartaddr(userid, localTripid, startaddr);
		dh.setEndaddr(userid, localTripid, endaddr);
	}
	
	private boolean uploadtripdata2() {
		try {
			String uploadUrl = "http://plash2.iis.sinica.edu.tw/api/UploadTrip.php";
			// Log.w("uploadUrl", uploadUrl);
			// the method to be used, with url
			HttpPost postRequest = new HttpPost(uploadUrl);
			// GET DATA FROM DB
			JSONObject data = dh.getOneTripData(userid, localTripid, true);
			// pair our data with corresponding name
//			List<NameValuePair> param = new ArrayList<NameValuePair>();
//			param.add(new BasicNameValuePair("trip", data.toString()));
			
			JSONObject tripinfo = dh.getOneTripInfo(userid, localTripid);
			// Log.e("UploadThread", "uploadtripdata2: tripinfo= " +
			// tripinfo.toString());
//			param.add(new BasicNameValuePair("tripinfo", tripinfo.toString()));
			
//			for (NameValuePair item : param) {
//				Log.e("UploadThread", "uploadtripdata2: param.item= " + item.toString());
//			}
			
			// encode our data with UTF8
//			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(param, HTTP.UTF_8);
			
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			entity.addPart("trip", new StringBody(data.toString()));
			entity.addPart("tripinfo", new StringBody(tripinfo.toString()));
			
			// put our data in the method object
			postRequest.setEntity(entity);
			
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used. 
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			
			HttpClient httpClient = new DefaultHttpClient(httpParameters);
			// execute the request and catch the response
			HttpResponse response = httpClient.execute(postRequest);
			
			// if received 200 ok status
			Integer statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				// extrace the return message
				BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String msg = in.readLine();
				in.close();
				if (msg != null && msg.contains("Ok")) {
					Log.w("upload service", "uplaodtripdata result: good");
					dh.markUploaded(userid, localTripid, 1, 1, null);
					try {
						JSONObject tmp = new JSONObject(new JSONTokener(msg));
						String servertripid = tmp.getString("trip_id");
						Log.e("UploadThread", "server tripid= " + servertripid);
						dh.setServerTripid(userid, localTripid, servertripid);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					httpClient.getConnectionManager().shutdown();
					return true;
				} else {
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
		} catch (IOException e) {
			e.printStackTrace();
			new AlertDialog.Builder(mContext)
				.setTitle("error")
				.setMessage("internet problem, try again later")
				.setNeutralButton("okay", null)
				.show();
		}
		
		Log.e("upload service", "uplaodtripdata error: exception");
		return false;
	}
	
	private boolean uploadpictures() {
		try {
			String uploadPicUrl = "http://plash2.iis.sinica.edu.tw/picture/UploadPicture.php";
			HttpPost postRequest = new HttpPost(uploadPicUrl);
			// get a list of all picture paths in the given tripid
			ArrayList<String> picPaths = dh.getOneTripPicturePaths(userid, localTripid, false);
			if (picPaths != null) {
				// go through the list and upload every picture
				for (int i = 0; i < picPaths.size(); i++) {
					// get the picture path
					String path = picPaths.get(i);
					// Log.w("upload service", "image path=" + path);
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(path)));
					byte[] ba = new byte[bis.available()];
					bis.read(ba);
					bis.close();
					ByteArrayBody bab = new ByteArrayBody(ba, path.substring(path.lastIndexOf("/") + 1));
					ba = null;
					MultipartEntity mentity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
					mentity.addPart("file", bab);
					mentity.addPart("userid", new StringBody(userid));
//					mentity.addPart("trip_id", new StringBody(localTripid));
					
					postRequest.setEntity(mentity);
					
					HttpParams httpParameters = new BasicHttpParams();
					// Set the timeout in milliseconds until a connection is established.
					// The default value is zero, that means the timeout is not used. 
					int timeoutConnection = 3000;
					HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
					// Set the default socket timeout (SO_TIMEOUT) 
					// in milliseconds which is the timeout for waiting for data.
					int timeoutSocket = 5000;
					HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
					
					HttpClient httpClient = new DefaultHttpClient(httpParameters);
					HttpResponse response = httpClient.execute(postRequest);
					Integer statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == 200) {
						BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						String msg = in.readLine();
						in.close();
						if (msg != null && msg.contains("OK")) {
							 Log.w("upload service", i + ") upload picture result: good");
							dh.markUploaded(userid, localTripid, 1, 2, path);
						} else {
							 Log.e("upload service", i + ") upload pictures error: upload failed, messsage=" + msg);
						}
					} else {
						 Log.e("upload service", i + ") upload picture error: connection error, status code=" + statusCode);
						dh.markUploaded(userid, localTripid, 1, 1, path);
					}
					httpClient.getConnectionManager().shutdown();
				}
				return true;
			} else {
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
		// Log.e("upload service", "upload picture error: exception");
		return false;
	}
	
	private boolean confirmAllDone(){
		
		return true;
	}
	
	/**
	 * encode only the parameter part after ? mark, not the ENTIRE url...
	 * 
	 * @param inURL
	 *            , the url to be encoded
	 * @return url, complete and correctly encoded
	 */
//	private String correctURLEncoder(String inURL) {
//		String inParameter = inURL.substring(inURL.lastIndexOf("?") + 1);
//		// Log.e("correct url encoder", "inParam=" + inParameter);
//		String outParameter = null;
//		try {
//			outParameter = URLEncoder.encode(inParameter, "UTF-8");
//			// Log.e("correct url encoder", "outParam=" + outParameter);
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		String result = inURL.replace(inParameter, outParameter);
//		// Log.e("correct url encoder", "result=" + result);
//		return result;
//	}
	
	public static HttpClient getHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
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
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}
	
	private void cleanup(boolean shouldDeleteTrip) {
		Log.e("uploadthread", "cleanup~");
		diag.dismiss();
		if (shouldDeleteTrip) {
			dh.deleteLocalTrip(userid, localTripid);
		}
		if (dh != null) {
			dh.closeDB();
			dh = null;
		}
		reloader.shouldReloadTripList();
	}
	
	private boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null) { // in airplane mode, there are no "Active Network",
							// networkInfo will be null
			return ni.isConnected();
		} else {
			return false;
		}
	}
}
