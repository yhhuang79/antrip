package tw.plash.antrip.offline;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
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

import tw.plash.antrip.offline.PLASHUrlEncodedFormEntity.ProgressListener;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class UploadThread extends AsyncTask<Void, Integer, Integer> {
	
	final private Context mContext;
	private DBHelper2 dh2;
	final private String userid;
	final private String tripid;
	private int position;
	
	private ProgressDialog diag;
	
	private TripListReloader reloader;
	
	private final int STAGE_0_PREPARE = 0;
	private final int STAGE_1_ADDRESS = 1;
	private final int STAGE_2_INFODATA = 2;
	private final int STAGE_3_PICTURE = 3;
	private final int STAGE_4_CONFIRM = 4;
	private final int STAGE_5_ALLDONE = 5;
	
	private final int largeTripThreshold;
	
	private int totalPointCount;
	private int totalpictures = 0;
	
	public UploadThread(Context c, String userid, String tripid, TripListReloader tlr, int position) {
		this.mContext = c;
		dh2 = new DBHelper2(mContext);
		this.largeTripThreshold = dh2.getLargeTripThreshold();
		this.userid = userid;
		this.tripid = tripid;
		reloader = tlr;
		this.position = position;
//		Log.e("uploadthread", "constructor: userid=" + userid + ", tripid=" + tripid + ", position=" + position + ", largetripthreshold=" + largeTripThreshold);
//		Toast.makeText(c, this.userid + ", " + this.tripid + ", " + this.position, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (userid == null || userid.equals("-1") || tripid == null) {
			Toast.makeText(mContext, R.string.toast_canceled, Toast.LENGTH_LONG).show();
			cancel(true);
		} else if(!InternetUtility.isNetworkAvailable(mContext)){
			new AlertDialog.Builder(mContext)
			.setTitle(R.string.error)
			.setMessage(R.string.alertdialog_nointernet_nologin)
			.setNeutralButton(R.string.okay, null)
			.show();
			cancel(true);
		} else{
			diag = new ProgressDialog(mContext);
			diag.setTitle(R.string.progressdialog_uploading);
			diag.setMessage(mContext.getString(R.string.progressdialog_uploading));
			diag.setCancelable(false);
			diag.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			diag.setMax(100);
			diag.setProgress(0);
			diag.show();
			System.gc(); //just trying to be safe
		}
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		Log.i("uploadthread", "onprogresupdate values=" + values.toString());
		switch(values[0]){
		case STAGE_0_PREPARE: //preparing upload information
//			diag.setMessage("Preparing data for upload...(part 1 of 4)");
			diag.setProgress(values[1]); //total progress 1%
			break;
		case STAGE_1_ADDRESS: //doing address
			switch(values[1]){
			case 5:
//				diag.setMessage("Preparing data for upload...(part 2 of 4)");
				diag.setProgress(values[1]); //total progress 5%
				break;
			case 10:
//				diag.setMessage("Preparing data for upload...(part 3 of 4)");
				diag.setProgress(values[1]); //total progress 10%
				break;
			case 15:
//				diag.setMessage("Preparing data for upload...(part 4 of 4)");
				diag.setProgress(values[1]); //total progress 15%
				break;
			}
			break;
		case STAGE_2_INFODATA: //uploading tripinfo/data
			System.gc(); //just trying to be safe
			switch(values[1]){
			case 20:
//				diag.setMessage("Preparing data for upload...done");
				diag.setProgress(values[1]);
				break;
			default:
//				diag.setMessage("Uploading data..." + values[1] + "%");
				diag.setProgress(20 + values[1]/100*30);
				break;
			}
			break;
		case STAGE_3_PICTURE: //uploading pictures
			System.gc(); //just trying to be safe
			switch(values[1]){
			case 50:
//				diag.setMessage("Uploading data...done!");
				diag.setProgress(values[1]);
				break;
			case 0:
				totalpictures = values[2];
//				diag.setMessage("Preparing to upload " + totalpictures + " pictures...");
				diag.setProgress(51);
				break;
			default:
				if(totalpictures > 0){ //XXX don't think it will have value of 0, just to be safe...
//					diag.setMessage("Uploading picture + " + values[1] + " of " + totalpictures + "..." + values[2] + "%");
					diag.setProgress(51 + (values[2]/100*(values[1]/totalpictures)*44));
				}
				break;
			}
			break;
		case STAGE_4_CONFIRM: //confirming with server
			switch(values[1]){
			case 95:
//				diag.setMessage("Upload pictures...done!");
				diag.setProgress(values[1]);
				break;
			default:
//				diag.setMessage("Confirming upload...");
				diag.setProgress(values[1]);
				break;
			}
			
			break;
		case STAGE_5_ALLDONE: //done
			diag.setMessage(mContext.getString(R.string.progressdialog_upload_done));
			diag.setProgress(100);
			break;
		}
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		Log.e("uploadthread", "upload starting");
		int uploadStage = dh2.getUploadStage(tripid);
		publishProgress(STAGE_0_PREPARE, 1);
		Log.e("uploadthread", "2) get uploadstatus: " + uploadStage);
		switch (uploadStage) {
		case 0:
			publishProgress(STAGE_1_ADDRESS, 5);
			// no upload attempted yet
			List<Address> startAddr = getaddress(tripid, true);
			Log.e("uploadthread", "3) get start address done");
			publishProgress(STAGE_1_ADDRESS, 10);
			List<Address> endAddr = getaddress(tripid, false);
			Log.e("uploadthread", "4) get end address done");
			publishProgress(STAGE_1_ADDRESS, 15);
			// all good, commit everything to DB, and save new upload stage
			commitAlltoDB(startAddr, endAddr);
			dh2.setUploadStage(tripid, 1); //set stage to 1, meaning stage 0 is already done
			Log.e("uploadthread", "5) save addresses to DB done");
		case 1:
			publishProgress(STAGE_2_INFODATA, 20);
			// addresses parsed
			if (uploadtripinfodata()) {
				dh2.setUploadStage(tripid, 2);
				Log.e("uploadthread", "6) upload trip info/data done");
			} else {
				Log.e("uploadthread", "6) <ERROR> upload trip info/data failed");
				return 2;
			}
		case 2:
			publishProgress(STAGE_3_PICTURE, 50);
			// tripinfo/data uploaded
			ArrayList<String> pics = dh2.getOneTripPicturePaths(tripid);
			// show the number of pictures pending upload
			if(pics != null && pics.size() > 0){
				//at least one picture to upload
				int picresult = uploadpictures(pics);
				if (picresult == pics.size()) {
					// if the number of pictures pending upload equals the number of
					// the pictures uploaded
					dh2.setUploadStage(tripid, 3);
					Log.e("uploadthread", "7) upload pictures done");
				} else {
					Log.e("uploadthread", "7) <ERROR> upload pictures failed");
					// show the number of pictures failed to upload
					return 3;
				}
			} else{
				//no picture to upload
				dh2.setUploadStage(tripid, 3);
			}
		case 3:
			publishProgress(STAGE_4_CONFIRM, 95);
			// just need to confirm all done
			if (confirmAllDone()) {
				dh2.setUploadStage(tripid, 4);
				Log.e("uploadthread", "8) confirm with server done");
			} else {
				Log.e("uploadthread", "8) <ERROR> confirm with server failed");
				return 4;
			}
			// break;
		case 4:
			// this trip is already uploaded, delete it from local DB
			publishProgress(STAGE_5_ALLDONE);
			return 1;
		default:
			return -2;
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
			new AlertDialog.Builder(mContext)
				.setTitle(R.string.error)
				.setMessage(R.string.upload_failed_warning)
				.setNeutralButton(R.string.okay, null)
				.show();
			cleanup(false);
			break;
		}
	}
	
	private List<Address> getaddress(String tid, boolean start) {
		try {
			//XXX think about it, it is impossible that trip data has already obtained server tripid at this point
			// so get one point operation with local tripid will guarantee to work
			Location point = dh2.getOnePoint(tid, start, false); //XXX think about using accurate or not
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			List<Address> addr = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
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
		dh2.setStartaddr(tripid, startaddr);
		dh2.setEndaddr(tripid, endaddr);
	}
	
	private boolean uploadtripinfodata() {
		
		String actualTripid = dh2.getTripid(tripid);
		if(actualTripid == null || actualTripid.equals("-1")){
			//null or -1 means no server tripid received yet, use the local tripid
			actualTripid = tripid;
		}
		
		int count = (int) dh2.getNumberofPoints(actualTripid);
		Log.i("upload thread", "trip point count= " + count);
		if (count < largeTripThreshold) {
			// small trip, normal upload
			return uploadSmallTrip(actualTripid);
		} else {
			return uploadLargeTrip(actualTripid);
		}
	}
	
	//XXX
	//XXX Taiwan average 3G upload speed is 0.17 Mbps ~ 21.76 KBps ~ 22282.24 bytes per second
	//XXX
	
	private boolean uploadSmallTrip(String actualTripid){
		
		String uploadUrl = "http://plash2.iis.sinica.edu.tw/api/UploadTrip.php";
		HttpPost postRequest = new HttpPost(uploadUrl);
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
		// the timeout for waiting for data.
		//XXX server will only response when upload is complete, so the socket timeout should depend on the size of 
		//the data pending upload and the internet connection speed
		//XXX according to calculation, timeout should be 30 seconds...but that's just stupid short
		//XXX we'll use 3 min as timeout for now...
		int timeoutSocket = 180000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		
		JSONObject data = dh2.getSmallTripData(userid, actualTripid);
		if(data != null){
			try {
				List<NameValuePair> param = new ArrayList<NameValuePair>();
				param.add(new BasicNameValuePair("trip", data.toString()));
				
				// tripinfo table contains both local and server tripid, is it
				// safe to query with local tripid
				JSONObject tripinfo = dh2.getOneTripInfo(userid, tripid);
				if(tripinfo != null){
					param.add(new BasicNameValuePair("tripinfo", tripinfo.toString()));
					
					//XXX comment out this part when release
//					for (NameValuePair item : param) {
//						Log.e("UploadThread", "uploadtripdata2: param.item= " + item.toString());
//					}
					
					//custom entity wieh ability to count the bytes uploaded, and display it in progress bar
					final PLASHUrlEncodedFormEntity entity = new PLASHUrlEncodedFormEntity(param, HTTP.UTF_8, new ProgressListener() {
						@Override
						public void transferred(long num, long total) {
							Log.i("uploadthread", "smalltripupload: " + num + "/" + total);
							//publish the the ratio of total data uploaded
							//times 100 to prevent integer clipping when cast
							if(total > 0){ //XXX just to be safe...
								publishProgress(STAGE_2_INFODATA, (int) ((num/(float)total)*100));
							}
						}
					});
					// encode our data with UTF8
					// put our data in the method object
					postRequest.setEntity(entity);
					//XXX entity.getcontentlength... also use custom entity with listener to monitor upload progress
					
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
							Log.w("upload service", "uploadtripdata result: good");
							JSONObject tmp = new JSONObject(new JSONTokener(msg));
							//if this operation failed, upload will be treated as incomplete
							String servertripid = tmp.getString("trip_id");
//							Log.e("UploadThread", "small trip server tripid= " + servertripid);
							// might have already been set before, but it's fine 
							// to overwrite it with the same value
							dh2.setServerTripid(tripid, servertripid);
							
							return true;
						} else {
							Log.e("upload service", "uplaodtripdata small trip error: upload failed, messsage=" + msg);
						}
					} else {
						// connection error
						Log.e("upload service", "uplaodtripdata small trip error: connection failed, status code=" + statusCode);
					}
				} else{
					Log.e("upload thread", "upload small trip, trip info is null");
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} finally{
				httpClient.getConnectionManager().shutdown();
			}
		} else{
			Log.e("upload thread", "upload small trip, trip data is null");
		}
		return false;
	}
	
	private boolean uploadLargeTrip(String actualTripid){
		
		String uploadUrl = "http://plash2.iis.sinica.edu.tw/api/UploadTrip.php";
		HttpPost postRequest = new HttpPost(uploadUrl);
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
		// the timeout for waiting for data.
		//XXX server will only response when upload is complete, so the socket timeout should depend on the size of 
		//the data pending upload and the internet connection speed
		int timeoutSocket = 180000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		
		boolean allgood = true;
		
		Log.e("UPLOAD THREAD", "WARNING: LARGE TRIP UPLOAD");
		
		String tmpurl = "http://plash2.iis.sinica.edu.tw/api/GetNewTripId.php?userid=" + userid;
		HttpGet getRequest = new HttpGet(tmpurl);
		HttpParams tmphttpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		HttpConnectionParams.setConnectionTimeout(tmphttpParameters, AntripService2.CONNECTION_TIMEOUT);
		// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
		HttpConnectionParams.setSoTimeout(tmphttpParameters, 10000);
		DefaultHttpClient tmphttpClient = new DefaultHttpClient(tmphttpParameters);
		
		String newtripid = dh2.getTripid(tripid);
		
		//retry "get new tripid" up to 5 times
		for(int i = 0; (newtripid == null) || (newtripid.equals("-1")) && (i < 5); i++){
			try {
				HttpResponse tmpresponse = tmphttpClient.execute(getRequest);
				if (tmpresponse.getStatusLine().getStatusCode() == 200) {
					BufferedReader in = new BufferedReader(new InputStreamReader(tmpresponse.getEntity().getContent()));
					JSONObject msg = new JSONObject(new JSONTokener(in.readLine()));
					in.close();
					if(msg.has("newTripId")){
						newtripid = msg.getString("newTripId");
					}
				}
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
//		tmphttpClient.getConnectionManager().shutdown();
		tmphttpClient = null;
		tmphttpParameters = null;
		getRequest = null;
		tmpurl = null;
		
		if(newtripid == null || newtripid.equals("-1")){
			//unable to retrieve a new tripid, rage quit
			allgood = false;
		} else if(dh2.setServerTripid(tripid, newtripid)){
			//there are at least 2 parts of tripinfo/data to upload
			JSONObject data = dh2.getLargeTripData(userid, newtripid);
			
			for(int i = 1; data != null; i++){
				try {
					
					Log.e("upload thread", "large trip upload: " + i);
					
					final int lastPointId = data.getInt("lastpointid");
					
					List<NameValuePair> param = new ArrayList<NameValuePair>();
					
					if(i == 1){
						JSONObject tripinfo = dh2.getOneTripInfo(userid, tripid);
						totalPointCount = tripinfo.getInt("num_of_pts");
						Log.e("upload thread", "large trip point count= " + totalPointCount);
						param.add(new BasicNameValuePair("tripinfo", tripinfo.put("trip_id", newtripid).toString()));
					}
					
					param.add(new BasicNameValuePair("trip", data.toString()));
					
					final int currentcount = i;
					
					PLASHUrlEncodedFormEntity entity = new PLASHUrlEncodedFormEntity(param, HTTP.UTF_8, new ProgressListener() {
						@Override
						public void transferred(long num, long total) {
							if(totalPointCount < 1){
								if(total > 0){
									publishProgress(STAGE_2_INFODATA, (int) ((num/total) * (currentcount/(currentcount + 1)))*100);
								} else{
									Log.e("upload thread", "upload large, progress update total is zero");
								}
							} else{
								if(total > 0){
									if(totalPointCount > 0){
										if(largeTripThreshold > 0){
											publishProgress(STAGE_2_INFODATA, (int) ((num/total) * (currentcount/(totalPointCount/largeTripThreshold + 1)))*100);
										} else{
											Log.e("upload thread", "upload large, progress update largetripthreshold is zero");
										}
									} else{
										Log.e("upload thread", "upload large, progress update totalpointcount is zero");
									}
								} else{
									Log.e("upload thread", "upload large, progress update total is zero");
								}
							}
							
						}
					});
					
					postRequest.setEntity(entity);
					
					HttpResponse response = httpClient.execute(postRequest);
					
					// if received 200 ok status
					Integer statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == 200) {
						// extrace the return message
						BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						String msg = in.readLine();
						in.close();
						if (msg != null && msg.contains("Ok")) {
							Log.w("upload service", "uplaodtripdata large trip result: good");
							//XXX check for return value?
							dh2.setUploadStatus(newtripid, lastPointId);
						} else {
							Log.e("upload service", "uplaodtripdata large trip error: upload failed, messsage=" + msg);
							allgood = false;
						}
					} else {
						// connection error
						Log.e("upload service", "uplaodtripdata large trip error: connection failed, status code=" + statusCode);
						allgood = false;
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					allgood = false;
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					allgood = false;
				} catch (IOException e) {
					e.printStackTrace();
					allgood = false;
				} catch (JSONException e) {
					e.printStackTrace();
					allgood = false;
				}
				//if there are no more data to be upload, data will be null
				data = dh2.getLargeTripData(userid, newtripid);
			}
			httpClient.getConnectionManager().shutdown();
		} else{
			allgood = false;
		}
		
		return allgood;
	}
	
	private Integer uploadpictures(ArrayList<String> picPaths) {
		// XXX update the progress bar with how many picure to upload
		publishProgress(STAGE_3_PICTURE, 0, picPaths.size());
		
		int numberofUploadedPictures = 0;
		String uploadPicUrl = "http://plash2.iis.sinica.edu.tw/picture/UploadPicture.php";
		HttpPost postRequest = new HttpPost(uploadPicUrl);
		// get a list of all picture paths in the given tripid
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is
		// established.
		// The default value is zero, that means the timeout is not used.
		HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
		// Set the default socket timeout (SO_TIMEOUT) in milliseconds which
		// is the timeout for waiting for data.
		//XXX timeout for each picture should be proportional to the picture size
		//XXX 300 seconds ~ 5 min for each picture should be more than enough...
		int timeoutSocket = 300000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		for (int i = 0; i < picPaths.size(); i++) {
			try {
				//XXX don't want to start from zero, which is a special case in upload picture
				final int currentpicturenum = i + 1; 
				// get the picture path
				// uploaded pictures will not be in the picPath list, already filtered out by DBhelper
				String path = picPaths.get(i);
				
				Log.w("upload service", "image path=" + path);
				File file = new File(path);
				// XXX update the progress bar showing how many bytes is
				// pending upload
				if(file.exists()){
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
					byte[] ba = new byte[(int) file.length()];
					bis.read(ba);
					bis.close();
					ByteArrayBody bab = new ByteArrayBody(ba, path.substring(path.lastIndexOf("/") + 1));
					ba = null;
					
					final PLASHMultipartEntity mentity = new PLASHMultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,
						new ProgressListener() {
							@Override
							public void transferred(long num, long total) {
								if(total > 0){
									publishProgress(STAGE_3_PICTURE, currentpicturenum, (int) ((num / total)*100));
								} else{
									Log.e("uploadthread", "upload picture, total is zero");
								}
							}
						}
					);
					
					mentity.addPart("file", bab);
					mentity.addPart("userid", new StringBody(userid));
					
					postRequest.setEntity(mentity);
					
					HttpResponse response = httpClient.execute(postRequest);
					Integer statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == 200) {
						BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						String msg = in.readLine();
						in.close();
						if (msg.contains("OK")) {
							// mark uploaded pictures by renaming the picture
							File f = new File(path);
							if (f.exists()) {
								f.renameTo(new File(path.substring(0, path.lastIndexOf("/") + 1) + "u" + path.substring(path.lastIndexOf("/") + 1)));
								//XXX the filename change is not sync with DB
								//so any upload photo will have different name in DB and in filesystem
								
							}
							f = null;
							numberofUploadedPictures += 1;
						}
					}
				} else{
					//file not found
//					continue;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		httpClient.getConnectionManager().shutdown();
		return numberofUploadedPictures;
	}
	
	private boolean confirmAllDone(){
		publishProgress(STAGE_4_CONFIRM, 97);
		String servertripid = dh2.getTripid(tripid);
		String url = "http://plash2.iis.sinica.edu.tw/api/UploadTripComplete.php?userid=" + userid + "&trip_id=" + servertripid;
		HttpGet getRequest = new HttpGet(url);
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
		// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
		HttpConnectionParams.setSoTimeout(httpParameters, 10000);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		try {
			HttpResponse response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				JSONObject msg = new JSONObject(new JSONTokener(in.readLine()));
				in.close();
				if(msg.has("code") && msg.getInt("code") == 200){
					//all good
					return true;
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally{
			httpClient.getConnectionManager().shutdown();
		}
		
		return false;
	}
	
//	public static HttpClient getHttpClient() {
//		try {
//			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//			trustStore.load(null, null);
//			SSLSocketFactory sf = new AndroidSSLSocketFactory(trustStore);
//			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//			HttpParams params = new BasicHttpParams();
//			// set connection timeout
//			int timeoutConnection = 5000;
//			HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
//			// set socket timeout
//			int timeoutSocket = 5000;
//			HttpConnectionParams.setSoTimeout(params, timeoutSocket);
//			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
//			SchemeRegistry registry = new SchemeRegistry();
//			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//			registry.register(new Scheme("https", sf, 443));
//			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
//			return new DefaultHttpClient(ccm, params);
//		} catch (Exception e) {
//			return new DefaultHttpClient();
//		}
//	}
	
	private void cleanup(boolean shouldDeleteTrip) {
		Log.e("uploadthread", "cleanup~");
		diag.dismiss();
		if (shouldDeleteTrip) {
			dh2.deleteLocalTrip(tripid);
		} else{
			position = -1;
		}
		if (dh2 != null) {
			dh2.closeDB();
			dh2 = null;
		}
		//XXX trip list reloader should be able to reload remote trip also...
		reloader.shouldReloadTripList(position);
	}
}
