package tw.plash.antrip.offline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class GMapRecorderActivity3 extends FragmentActivity implements InfoWindowAdapter, CheckinQuickActionCallback{
	
	private GoogleMap gmap;
	private Context mContext;
	
	private Button checkinBtn;
	private ImageButton recordBtn;
	private ImageButton dropdownList;
	
	private TextView warningPanel;
	
	private CheckinDialog checkinDiag;
	
	private boolean firstLoactionFixAnimate;
	private Location latestLocation;
	private CandidateCheckinObject cco;
	private Uri imageUri;
	final private int REQUEST_CODE_TAKE_PICTURE = 1377;
	private Polyline trajectory;
	
	private boolean mIsBound;
	private Messenger outMessenger;
	private Messenger inMessenger;
	private IncomingHandler ih;
	private antripLocationSource als;
	
	private interface ListenerCallback{
		public void activateListener(OnLocationChangedListener olcl);
		public void deactiveListener();
	}
	
	/**
	 * customized location source to be used by google map android api's mylocation service
	 * activate/deactivate will be called when google map start/stop showing current location
	 * 
	 * pass the OnLocationChangedListener to IncomingHandler through listenerCallback
	 * let IncomingHandler pass the location update to OnLocationChangedListener by itself
	 * @author CSZU
	 *
	 */
	private class antripLocationSource implements LocationSource{
		
		final private ListenerCallback lc;
		
		public antripLocationSource(ListenerCallback lc) {
			this.lc = lc;
		}
		
		@Override
		public void activate(OnLocationChangedListener listener) {
			lc.activateListener(listener);
		}
		
		@Override
		public void deactivate() {
			lc.deactiveListener();
		}
		
	}
	
	/**
	 * implements ListnerCallback to pass new location update directly to 
	 * google map android api for updating current location marker
	 * @author CSZU
	 *
	 */
	private class IncomingHandler extends Handler implements ListenerCallback{
		
		private OnLocationChangedListener olcl = null;
		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case AntripService2.MSG_LOCATION_UPDATE:
				if(msg.obj instanceof Location){
					latestLocation = (Location) msg.obj;
					if(olcl != null){
						//only valid and accurate location update will be 
						//received, we can safely update current location
						olcl.onLocationChanged(latestLocation);
						if(firstLoactionFixAnimate){
							gmap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()), 17, 0, 0)));
							firstLoactionFixAnimate = false;
						}
						
					}
					if(AntripService2.isRecording()){
						if(checkinBtn.getVisibility() == View.GONE){
							checkinBtn.setVisibility(View.VISIBLE);
						}
						//draw polyline on map
						if(trajectory == null){
							//first time setup
							trajectory = gmap.addPolyline(new PolylineOptions().add(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude())));
							gmap.addMarker(new MarkerOptions().position(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude())).title("start"));
						} else{
							//update the piont list, no need to clear the entire map to redraw
							//this way, the first point marker will be kept
							List<LatLng> list = trajectory.getPoints();
							list.add(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()));
							trajectory.setPoints(list);
						}
					}
				}
				break;
			case AntripService2.MSG_SYNC_LOCATION:
				if(msg.obj instanceof ArrayList<?>){
					//clear the map for redrawing
					ArrayList<JSONObject> data = (ArrayList<JSONObject>) msg.obj;
					if(data != null && data.size() > 0){
						//at least one point exists
						gmap.clear();
						try {
							PolylineOptions po = new PolylineOptions();
							for (JSONObject item : data) {
								po.add(new LatLng(item.getDouble("latitude"), item.getDouble("longitude")));
								if(item.has("picture") || item.has("emotion") || item.has("note")){
									CandidateCheckinObject tmpcco = new CandidateCheckinObject();
									if(item.has("picture")){
										tmpcco.setPicturePath(item.getString("picture"));
									}
									if(item.has("emotion")){
										tmpcco.setEmotionID(Integer.parseInt(item.getString("emotion")));
									}
									if(item.has("note")){
										tmpcco.setCheckinText(item.getString("note"));
									}
									gmap.addMarker(new MarkerOptions().position(new LatLng(item.getDouble("latitude"), item.getDouble("longitude"))).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_48)).snippet(tmpcco.toString()));
								}
							}
							trajectory = gmap.addPolyline(po);
							Log.w("gmaprecorder activity3", "MSG_SYNC_LOCATION: po=" + po.toString());
							gmap.addMarker(new MarkerOptions().position(new LatLng(data.get(0).getDouble("latitude"), data.get(0).getDouble("longitude"))).title("start"));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
				break;
			case AntripService2.MSG_STOP_RECORDING_CONFIRMED:
				if(msg.obj instanceof Boolean){
					if((Boolean) msg.obj){
						//trip is confirmed as VALID, show input trip name dialog
						final Dialog dialog = new Dialog(mContext);
						dialog.setContentView(R.layout.edittripname);
						dialog.setTitle(R.string.input_tripname);
						dialog.setCancelable(true);
						dialog.setCanceledOnTouchOutside(true);
						final EditText et = (EditText) dialog.findViewById(R.id.edittripname);
						final String hint = getResources().getString(R.string.untitled_trip);
						et.setHint(hint);
						((Button) dialog.findViewById(R.id.edittripnamebtn)).setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								String tripname = null;
								if (et.getText().length() > 0) {
									tripname = et.getText().toString();
								} else {
									tripname = hint;
								}
								sendMessageToService(AntripService2.MSG_SAVE_TRIPNAME, tripname);
								dialog.dismiss();
							}
						});
						dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								sendMessageToService(AntripService2.MSG_SAVE_TRIPNAME, hint);
							}
						});
						dialog.show();
					} else{
						//trip is confirmed as INVALID, show warning
						new AlertDialog.Builder(mContext)
							.setTitle("Invalid Trip")
							.setMessage("Your trip contains no valid point, it has been discarded")
							.setPositiveButton("I see...", null)
							.show();
					}
				} else{
					Log.e("gmaprecorderactivity3", "MSG_STOP_RECORDING_CONFIRMED: msg.obj is not BOOLEAN");
				}
				break;
			case AntripService2.MSG_AUTO_STOP_RECORDING_CONFIRMED:
				checkinBtn.setVisibility(View.GONE);
				recordBtn.setImageResource(R.drawable.actionbar_record);
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
		
		@Override
		public void activateListener(OnLocationChangedListener olcl) {
			this.olcl = olcl;
		}
		
		@Override
		public void deactiveListener() {
			this.olcl = null;
		}
	}
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			outMessenger = new Messenger(service);
			try{
				Message msg = Message.obtain(null, AntripService2.MSG_REGISTER_CLIENT);
				msg.replyTo = inMessenger;
				outMessenger.send(msg);
			} catch(RemoteException e){
				e.printStackTrace();
			}
			//
			sendMessageToService(AntripService2.MSG_INIT_LOCATION_THREAD, null);
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			outMessenger = null;
		}
		
	};
	
	private void sendMessageToService(int msgType, Object payload){
		if(mIsBound){
			if(outMessenger != null){
				try{
					if(payload != null){
						outMessenger.send(Message.obtain(null, msgType, payload));
					} else{
						outMessenger.send(Message.obtain(null, msgType));
					}
				} catch(RemoteException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
	}
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.gmaprecorder);
		
		mContext = this;
		
		latestLocation = null;
		firstLoactionFixAnimate = true;
		trajectory = null;
		
		ih = new IncomingHandler();
		inMessenger = new Messenger(ih);
		als = new antripLocationSource(ih);
		
		checkinBtn = (Button) findViewById(R.id.btn_checkin);
		checkinBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//make sure 
				cco = new CandidateCheckinObject();
				cco.setLocation(latestLocation);
				//pass the latest location object to checkin window to be saved with cco
				checkinDiag = new CheckinDialog(mContext, R.layout.checkin_layout2, GMapRecorderActivity3.this);
				checkinDiag.show();
			}
		});
		
		recordBtn = (ImageButton) findViewById(R.id.actionbar_action_button_right);
		if(AntripService2.isRecording()){
			recordBtn.setImageResource(R.drawable.actionbar_record_pressed);
		} else{
			recordBtn.setImageResource(R.drawable.actionbar_record);
		}
		recordBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(AntripService2.isRecording()){
					//was recording, STOP recording now
					checkinBtn.setVisibility(View.GONE);
					recordBtn.setImageResource(R.drawable.actionbar_record);
					sendMessageToService(AntripService2.MSG_STOP_RECORDING, null);
					//wait for the stop recording confirmation
					//if trip is valid, show input tripname dialog
					//else show warning
				} else{
					//was NOT recording, START recording now
					gmap.clear();
					latestLocation = null;
					firstLoactionFixAnimate = true;
					trajectory = null;
					sendMessageToService(AntripService2.MSG_START_RECORDING, null);
					recordBtn.setImageResource(R.drawable.actionbar_record_pressed);
				}
			}
		});
		
		dropdownList = (ImageButton) findViewById(R.id.actionbar_activity_icon);
		dropdownList.setImageResource(R.drawable.dropdown_recorder_pressed);
		dropdownList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DropdownFunctionList(v).showLikePopDownMenu();
			}
		});
		
		warningPanel = (TextView) findViewById(R.id.recorder_viewstub);
		warningPanel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		});
		
		isMapAvailable();
	}
	
	
	
	private void doBindService(){
		bindService(new Intent(mContext, AntripService2.class), mServiceConnection, BIND_AUTO_CREATE);
		mIsBound = true;
	}
	
	private void doUnbindService(){
		if(mIsBound){
			if(outMessenger != null){
				try {
					Message msg = Message.obtain(null, AntripService2.MSG_UNREGISTER_CLIENT);
					msg.replyTo = inMessenger;
					outMessenger.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			unbindService(mServiceConnection);
			mIsBound = false;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(!((LocationManager)getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)){
			warningPanel.setVisibility(View.VISIBLE);
		} else{
			warningPanel.setVisibility(View.GONE);
		}
		
		Log.w("gmaprecorderactivity3", "onresume");
		
		startService(new Intent(mContext, AntripService2.class).setAction("startcalledfromgmaprecorder"));
		
		doBindService();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		Log.w("gmaprecorderactivity3", "onpause");
		
		doUnbindService();
		
		if(!AntripService2.isRecording()){
			//if not recording, stop the service
			stopService(new Intent(mContext, AntripService2.class));
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_CODE_TAKE_PICTURE:
			Log.e("gmaprecorderactivity3", "onactivityresult: take picture done");
			if(imageUri != null && imageUri.getPath() != null && !imageUri.getPath().isEmpty()){
				ExifEditor.GeoTagPicture(imageUri.getPath(), cco.getLocation().getLatitude(), cco.getLocation().getLongitude(), String.valueOf(cco.getLocation().getTime()/1000));
				checkinDiag.setPicture(imageUri.getPath());
				cco.setPicturePath(imageUri.getPath());
			}
			break;
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
		
		gmap.setLocationSource(als);
		
		gmap.setMyLocationEnabled(true);
//		gmap.getUiSettings().setAllGesturesEnabled(true);
//		gmap.getUiSettings().setCompassEnabled(true);
		
		gmap.setInfoWindowAdapter(GMapRecorderActivity3.this);
		
		gmap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				if(marker.getTitle() != null){
					//start or end point
				} else{
					//show full size image via gallery
					Toast.makeText(mContext, "snippet= " + marker.getSnippet(), Toast.LENGTH_LONG).show();
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
			if(tmp != null && tmp.length() == 1){
				switch(Integer.parseInt(tmp)){
				case 1:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.mood_excited);
					break;
				case 2:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.mood_happy);
					break;
				case 3:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.mood_pleased);
					break;
				case 4:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.mood_relaxed);
					break;
				case 5:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.mood_peaceful);
					break;
				case 6:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.mood_sleepy);
					break;
				case 7:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.mood_sad);
					break;
				case 8:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.mood_bored);
					break;
				case 9:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.mood_nervous);
					break;
				case 10:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.mood_angry);
					break;
				case 11:
					((ImageView)root.findViewById(R.id.checkin_marker_mood)).setImageResource(R.drawable.mood_calm);
					break;
				}
			} else{
				((ImageView)root.findViewById(R.id.checkin_marker_mood)).setVisibility(View.GONE);
			}
			tmp = cco.substring(cco.indexOf(";text:") + 6, cco.indexOf(";pic:"));
			if(tmp != null && tmp.length() > 0){
				((TextView) root.findViewById(R.id.checkin_marker_text)).setText(tmp);
			}
			if(cco.indexOf(";pic:") + 5 < cco.length()){
				tmp = cco.substring(cco.indexOf(";pic:") + 5);
				((ImageView) root.findViewById(R.id.checkin_marker_picture)).setImageBitmap(BitmapUtility.getPreview(tmp, 200));
			}
			return root;
		}
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}
	
	
	
	@Override
	public void setCheckinText(String text) {
		Log.w("gmaprecorderactivity3", "setCheckinText: " + text);
		if(cco != null){
			cco.setCheckinText(text);
		} else{
			//error
		}
	}

	@Override
	public void startCamera() {
		Log.w("gmaprecorderactivity3", "start camera called");
		
		File imagefile = null;
		
		{
			String imagename = String.format("%1$d.jpg", System.currentTimeMillis());
			String path = "/mnt/sdcard/";
			Log.e("gmaprecorderactivity3", "path= " + path);
			Log.e("gmaprecorderactivity3w", "system path= " + Environment.getExternalStorageDirectory().getAbsolutePath());
			File tester = new File(path);
			if (tester.exists() && tester.isDirectory()) {
				Log.w("sdcard/dcim", "exists");
				File dir = new File(path + "antrip");
				dir.mkdir();
				if (dir.exists() && dir.isDirectory()) {
					Log.w("sdcard/dcim", "antrip dir created");
					imagefile = new File(path + "antrip", imagename);
				} else {
					Log.w("sdcard/dcim", "cannot create antrip/not a dir");
					imagefile = null;
				}
				
			} else {
				path = "/mnt/emmc/";
				tester = new File(path);
				if (tester.exists() && tester.isDirectory()) {
					Log.w("emmc/dcim", "exists");
					File dir = new File(path + "antrip");
					dir.mkdirs();
					if (dir.exists() && dir.isDirectory()) {
						Log.w("emmc/dcim", "antrip dir created");
						imagefile = new File(path + "antrip", imagename);
					} else {
						Log.w("emmc/dcim", "cannot create antrip/not a dir");
						imagefile = null;
					}
				}
			}
		}
		if(imagefile != null){
			imageUri = Uri.fromFile(imagefile);
			Log.w("startcamera", "imageUri= " + imageUri.getPath());
			// intent to launch Android camera app to take pictures
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// input the desired filepath + filename
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			// launch the intent with code
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
		}
	}

	@Override
	public void setMood(int mood) {
		Log.w("gmaprecorderactivity3", "setMood: " + mood);
		if(cco != null){
			cco.setEmotionID(mood);
		} else{
			//cco should not be null
		}
	}

	@Override
	public void commit() {
		Log.w("gmaprecorderactivity3", "commit called");
		if(cco != null && cco.isValid()){
			//save cco and draw marker
			sendMessageToService(AntripService2.MSG_SAVE_CCO, cco);
			gmap.addMarker(new MarkerOptions().position(new LatLng(cco.getLocation().getLatitude(), cco.getLocation().getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_48)).snippet(cco.toString()));
		} else{
			//show warning
		}
	}

	@Override
	public void cancel() {
		Log.w("gmaprecorderactivity3", "cancel called");
		cco = null;
	}
}