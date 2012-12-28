package tw.plash.antrip.offline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class GMapViewer extends FragmentActivity implements InfoWindowAdapter{
	
	private Context mContext;
	private GoogleMap gmap;
	
	private Boolean isLocal;
	
	private String hash;
	private String tripname;
	private String tripid;
	
	private Polyline trajectory;
	private ArrayList<MarkerOptions> checkinmarkerlist;
	
	private ArrayList<JSONObject> tripdata;
	
	private TextView statusBanner;
	
	private ImageView movetofirst;
	private ImageView zoomtoextent;
	private ImageView movetolast;
	
	private MarkerOptions firstPoint;
	private MarkerOptions lastPoint;
	private CameraUpdate zoomtoextentcameraupdate;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.gmapviewer);
		
		mContext = this;
		
		statusBanner = (TextView) findViewById(R.id.gmapviewer_banner);
		
		hash = null;
		tripname = null;
		tripid = null;
		
		tripdata = null;
		
		isLocal = false;
		
		firstPoint = null;
		lastPoint = null;
		zoomtoextentcameraupdate = null;
		
		Intent intent = getIntent();
		if(intent != null && intent.getExtras() != null){
			Bundle bundle = intent.getExtras();
			if(bundle.containsKey("islocal")){ //must be called from own trip list
				tripname = bundle.getString("tripname");
				isLocal = bundle.getBoolean("islocal");
				if(isLocal){
					//local trip, use tripname and tripid
					tripid = bundle.getString("tripid");
				} else{
					//remote trip, use tripname and hash
					hash = bundle.getString("hash");
				}
			} else{ //probably called from friend's shared trip list
				isLocal = false; //friend's trip data should be retrieved with remote loader
				tripname = bundle.getString("tripname");
				hash = bundle.getString("hash");
			}
			
		} else{
			Toast.makeText(mContext, R.string.toast_unabletofetchtripdata, Toast.LENGTH_LONG).show();
			finish();
		}
		
		((TextView) findViewById(R.id.actionbar_activity_title)).setText(tripname);
		
		//set title as trip name
		ImageButton goback = (ImageButton) findViewById(R.id.actionbar_activity_icon);
		goback.setImageResource(R.color.button_state_goback);
		goback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isLocal){
					if(localloader != null){
						localloader.cancel(true);
					}
				} else{
					if(remoteloader != null){
						remoteloader.cancel(true);
					}
				}
				finish();
			}
		});
		
		movetofirst = (ImageView) findViewById(R.id.gmapviewer_movetofirst);
		movetofirst.setEnabled(false);
		movetofirst.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstPoint != null){
					//move the camera to this point
					gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstPoint.getPosition(), 16));
				}
			}
		});
		
		zoomtoextent = (ImageView) findViewById(R.id.gmapviewer_zoomtoextent);
		zoomtoextent.setEnabled(false);
		zoomtoextent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(zoomtoextentcameraupdate != null){
					gmap.animateCamera(zoomtoextentcameraupdate);
				}
			}
		});
		
		movetolast = (ImageView) findViewById(R.id.gmapviewer_movetolast);
		movetolast.setEnabled(false);
		movetolast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(lastPoint != null){
					//move the camera to this point
					gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPoint.getPosition(), 16));
				}
			}
		});
		
		isMapAvailable();
		
		if(isLocal){
			localloader = new asyncloadlocal();
			localloader.execute();
		} else{
			remoteloader = new asyncloadremote();
			remoteloader.execute();
		}
	}
	
	private void isMapAvailable() {
		if (gmap == null) {
			gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gmapview)).getMap();
			if (gmap != null) {
				setUpMap();
			}
		}
	}
	
	private void setUpMap() {
		
		gmap.setMyLocationEnabled(false);
		
		gmap.setInfoWindowAdapter(GMapViewer.this);
		
		gmap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				if(marker.getTitle() != null){
					//start or end point
				} else{
					//show full size image via gallery XXX
//					Toast.makeText(mContext, "snippet= " + marker.getSnippet(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public View getInfoContents(Marker marker) {
		if(marker.getTitle() != null){
			Log.e("gmap recorder", "getinfocontent: title not null");
			return null;
		} else{
			Log.e("gmap recorder", "getinfocontent: title null");
			String cco = marker.getSnippet();
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ViewGroup root = (ViewGroup) inflater.inflate(R.layout.checkin_test, null);
			String tmp = cco.substring(0, cco.indexOf(";text:")).replace("mood:", "").replace(";", "");
//			Log.e("tmp", "mood:" + tmp);
			if(tmp != null && tmp.length() == 1){
				switch(Integer.parseInt(tmp)){
				case 1:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.emotion_excited);
					break;
				case 2:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.emotion_happy);
					break;
				case 3:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.emotion_pleased);
					break;
				case 4:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.emotion_relaxed);
					break;
				case 5:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.emotion_peaceful);
					break;
				case 6:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.emotion_sleepy);
					break;
				case 7:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.emotion_sad);
					break;
				case 8:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.emotion_bored);
					break;
				case 9:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.emotion_nervous);
					break;
				case 10:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.emotion_angry);
					break;
				case 11:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.emotion_calm);
					break;
				}
			} else{
				((ImageView)root.findViewById(R.id.checkin_marker_mood)).setVisibility(View.GONE);
			}
			tmp = cco.substring(cco.indexOf(";text:") + 6, cco.indexOf(";pic:"));
//			Log.e("tmp", "text:" + tmp);
			if(tmp != null && tmp.length() > 0){
				((TextView) root.findViewById(R.id.checkin_marker_text)).setText(tmp);
			}
			if(cco.indexOf(";pic:") + 5 < cco.length()){
				tmp = cco.substring(cco.indexOf(";pic:") + 5);
//				Log.e("tmp", "pic:" + tmp);
				((ImageView) root.findViewById(R.id.checkin_marker_picture)).setImageBitmap(BitmapUtility.getPreview(tmp, 200));
			}
			return root;
		}
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}
	
	private asyncloadlocal localloader;
	private class asyncloadlocal extends AsyncTask<Void, Void, Void> {
		private PolylineOptions po;
		
		protected void onPreExecute() {
			// check if trip data needs to be loaded
			if (tripdata != null) {
				cancel(true);
			} else {
				statusBanner.setVisibility(View.VISIBLE);
				statusBanner.setText(R.string.universal_loading);
				po = new PolylineOptions();
				checkinmarkerlist = new ArrayList<MarkerOptions>();
			}
		};
		
		@Override
		protected Void doInBackground(Void... params) {
			DBHelper2 dh2 = new DBHelper2(mContext);
			
			tripdata = dh2.getCurrentTripData(tripid);
			
			dh2.closeDB();
			dh2 = null;
			
			// should start building polyline option and markers here
			// remember don't manipulate UI object from this method...
			
			LatLngBounds.Builder boundbuilder = new LatLngBounds.Builder();
			
			for (JSONObject item : tripdata) {
				try {
					LatLng latlng = new LatLng(item.getDouble("latitude"), item.getDouble("longitude"));
					if(LocationFilter.isValid(latlng)){
						po.add(latlng);
						boundbuilder.include(latlng);
					}
					if (item.has("checkin")) {
						CandidateCheckinObject tmpcco = new CandidateCheckinObject();
						JSONObject tmpitem = item.getJSONObject("checkin");
						if (tmpitem.has("picture")) {
							tmpcco.setPicturePath(tmpitem.getString("picture"));
						}
						if (tmpitem.has("emotion")) {
							tmpcco.setEmotionID(Integer.parseInt(tmpitem.getString("emotion")));
						}
						if (tmpitem.has("note")) {
							tmpcco.setCheckinText(tmpitem.getString("note"));
						}
						checkinmarkerlist.add(new MarkerOptions()
							.position(latlng)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_48))
							.snippet(tmpcco.toString())
							.draggable(true));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			zoomtoextentcameraupdate = CameraUpdateFactory.newLatLngBounds(boundbuilder.build(), 30);
			
			try {
				firstPoint = new MarkerOptions().position(new LatLng(tripdata.get(0).getDouble("latitude"), tripdata.get(0).getDouble("longitude"))).title(mContext.getString(R.string.gmapviewer_marker_start)).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_startpoint));
				checkinmarkerlist.add(firstPoint);
				lastPoint = new MarkerOptions().position(new LatLng(tripdata.get(tripdata.size() - 1).getDouble("latitude"), tripdata.get(tripdata.size() - 1).getDouble("longitude"))).title(mContext.getString(R.string.gmapviewer_marker_end)).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_endpoint));
				checkinmarkerlist.add(lastPoint);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// now po and checkinmarkerlist are ready to be drawn
			
			return null;
		}
		
		protected void onCancelled() {
			if(statusBanner != null){
				statusBanner.setVisibility(View.GONE);
			}
		};
		
		protected void onPostExecute(Void result) {
			statusBanner.setVisibility(View.GONE);
			trajectory = gmap.addPolyline(po.color(0xffff0000));
			for (MarkerOptions item : checkinmarkerlist) {
				gmap.addMarker(item);
			}
			movetofirst.setEnabled(true);
			zoomtoextent.setEnabled(true);
			movetolast.setEnabled(true);
			
			gmap.animateCamera(zoomtoextentcameraupdate);
		};
	}
	
	private asyncloadremote remoteloader;
	private class asyncloadremote extends AsyncTask<Void, Void, Boolean> {
		
		private PolylineOptions po;
		
		protected void onPreExecute() {
			// check if trip data needs to be loaded
			if (tripdata != null) {
				cancel(true);
			} else if (!InternetUtility.isNetworkAvailable(mContext)) {
				Toast.makeText(mContext, R.string.toast_nointernet_nofetch, Toast.LENGTH_LONG)
						.show();
				cancel(true);
			} else {
				statusBanner.setVisibility(View.VISIBLE);
				statusBanner.setText(R.string.universal_loading);
				po = new PolylineOptions();
				checkinmarkerlist = new ArrayList<MarkerOptions>();
			}
		};
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			
				String url = "http://plash2.iis.sinica.edu.tw/api/GetCheckInData.php?hash=" + hash + "&field_mask=1100001100000001";
				
				HttpGet getRequest = new HttpGet(url);
				
				HttpParams httpParameters = new BasicHttpParams();
				// Set the timeout in milliseconds until a connection is
				// established.
				// The default value is zero, that means the timeout is not
				// used.
				HttpConnectionParams.setConnectionTimeout(httpParameters, AntripService2.CONNECTION_TIMEOUT);
				// Set the default socket timeout (SO_TIMEOUT) in milliseconds
				// which is the timeout for waiting for data.
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
					
					
					tripdata = (ArrayList<JSONObject>) JSONUtility.asList(result.getJSONArray("CheckInDataList"));
					
					LatLngBounds.Builder boundbuilder = new LatLngBounds.Builder();
					for (JSONObject item : tripdata) {
						try {
							
							LatLng latlng = new LatLng(item.getDouble("lat")/1E6, item.getDouble("lng")/1E6);
							if(item.getDouble("lat") > -998 && item.getInt("accu") < 1499){
//								Log.e("latlng", "lat,lng: " + latlng.latitude + ", " + latlng.longitude);
								if(firstPoint == null){
									firstPoint = new MarkerOptions().position(latlng).title(mContext.getString(R.string.gmapviewer_marker_start)).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_startpoint));
								}
								lastPoint = new MarkerOptions().position(latlng).title(mContext.getString(R.string.gmapviewer_marker_end)).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_endpoint));
								po.add(latlng);
								boundbuilder.include(latlng);
							}
							
							if (item.has("CheckIn")) {
								CandidateCheckinObject tmpcco = new CandidateCheckinObject();
								JSONObject tmpitem = item.getJSONObject("CheckIn");
								if (tmpitem.has("picture_uri") && !tmpitem.getString("picture_uri").equals("null")) {
									tmpcco.setPicturePath(tmpitem.getString("picture_uri"));
								}
								if (tmpitem.has("emotion") && !tmpitem.getString("emotion").equals("null")) {
									tmpcco.setEmotionID(Integer.parseInt(tmpitem.getString("emotion")));
								}
								if (tmpitem.has("message") && !tmpitem.getString("message").equals("null")) {
									tmpcco.setCheckinText(tmpitem.getString("message"));
								}
								checkinmarkerlist.add(new MarkerOptions()
									.position(latlng)
									.icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_48))
									.snippet(tmpcco.toString())
									.draggable(true));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					
					zoomtoextentcameraupdate = CameraUpdateFactory.newLatLngBounds(boundbuilder.build(), 30);
					checkinmarkerlist.add(firstPoint);
					checkinmarkerlist.add(lastPoint);
					
					// supposedly all good
					return true;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} finally{
				client.getConnectionManager().shutdown();
			}
			
			return false;
		}
		
		protected void onCancelled() {
			statusBanner.setVisibility(View.GONE);
		};
		
		protected void onPostExecute(Boolean result) {
			if (result) {
				// load trip data success
				statusBanner.setVisibility(View.GONE);
				trajectory = gmap.addPolyline(po.color(0xffff0000));
				for (MarkerOptions item : checkinmarkerlist) {
					gmap.addMarker(item);
				}
				movetofirst.setEnabled(true);
				zoomtoextent.setEnabled(true);
				movetolast.setEnabled(true);
				
				gmap.animateCamera(zoomtoextentcameraupdate);
			} else {
				// fail to laod trip data
				statusBanner.setText(R.string.gmapviewer_error);
			}
		};
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(isLocal){
			if(localloader != null){
				localloader.cancel(true);
			}
		} else{
			if(remoteloader != null){
				remoteloader.cancel(true);
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gmapviewer_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_maptype:
			new AlertDialog.Builder(mContext)
				.setTitle(R.string.menu_maptype)
				.setSingleChoiceItems(R.array.maptype_array, gmap.getMapType()-1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch(which){
						case 0:
							gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
							break;
						case 1:
							gmap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
							break;
						case 2:
							gmap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
							break;
						case 3:
							gmap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
							break;
						default:
							gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
							break;
						}
					}
				})
				.show();
			return true;
		case R.id.menu_help:
			Toast.makeText(mContext, R.string.toast_theres_no_help, Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}