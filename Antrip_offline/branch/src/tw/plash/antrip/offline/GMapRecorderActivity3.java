package tw.plash.antrip.offline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import tw.plash.antrip.offline.utility.BitmapUtility;
import tw.plash.antrip.offline.utility.ExifEditor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class GMapRecorderActivity3 extends FragmentActivity implements InfoWindowAdapter, CheckinQuickActionCallback{
	
	protected static Object audio_ss;
	private GoogleMap gmap;
	private Context mContext;
	
	private Button checkinBtn;
	private ImageButton recordBtn;
	private ImageButton dropdownList;
	
	private ImageView movetofirst;
	private ImageView zoomtoextent;
	private MarkerOptions firstPoint;
	private CameraUpdate zoomtoextentcameraupdate;
	private LatLngBounds.Builder boundBuilder;
	
	private TextView warningPanel;
	
	private CheckinDialog checkinDiag;
	
	private boolean canStartRecord;
	private boolean firstLoactionFixAnimate;
	private Location latestLocation;
	private CandidateCheckinObject cco;
	private Uri imageUri;
	final private int REQUEST_CODE_SETTINGS = 1375;
	final private int REQUEST_CODE_TAKE_PICTURE = 1376;	
	final private int REQUEST_CODE_TAKE_VIDEO = 1377;
	private Polyline trajectory;
	
	private boolean mIsBound;
	private Messenger outMessenger;
	private Messenger inMessenger;
	private IncomingHandler ih;
	private antripLocationSource als;
	
	
//	private Timer mTimer;
//	private boolean shouldSkipTimertask;
	
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
				Log.w("activity", "MSG_LOCATION_UPDATE");
				if(msg.obj instanceof Location){
//					if(mTimer != null){
//						mTimer.cancel();
//						mTimer.purge();
//						mTimer = null;
//					} else{
//						mTimer = new Timer();
//						mTimer.schedule(new TimerTask() {
//							@Override
//							public void run() {
//								//show warning banner, about not receiving location update for a while
//								if(!shouldSkipTimertask){
//									
//								}
//							}
//						}, 000);
//					}
					latestLocation = (Location) msg.obj;
					//user is only allowed to start recording when a location fix is received
					canStartRecord = true;
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
						if(firstPoint == null){
							firstPoint = new MarkerOptions().position(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude())).title(mContext.getString(R.string.gmapviewer_marker_start)).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_startpoint));
							movetofirst.setVisibility(View.VISIBLE);
						}
						if(checkinBtn.getVisibility() == View.GONE){
							checkinBtn.setVisibility(View.VISIBLE);
						}
						if(boundBuilder != null){
							boundBuilder.include(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()));
							zoomtoextentcameraupdate = CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 30);
							zoomtoextent.setVisibility(View.VISIBLE);
						}
						//draw polyline on map
						if(trajectory == null){
							//first time setup
							trajectory = gmap.addPolyline(new PolylineOptions().add(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude())).color(0xffff0000));
							if(firstPoint != null){
								gmap.addMarker(firstPoint);
							}
						} else{
							//update the piont list, no need to clear the entire map to redraw
							//this way, the first point marker will be kept
							List<LatLng> list = trajectory.getPoints();
//							Log.i("location update", "trajectory: " + list);
//							Log.i("location update", "latest location: " + latestLocation);
							list.add(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()));
							trajectory.setPoints(list);
						}
					}
				}
				break;
			case AntripService2.MSG_SYNC_LOCATION:
				Log.w("activity", "MSG_SYNC_LOCATION");
				if(msg.obj instanceof ArrayList<?>){
					zoomtoextent.setVisibility(View.GONE);
					//clear the map for redrawing
					ArrayList<JSONObject> data = (ArrayList<JSONObject>) msg.obj;
//					Log.i("gmaprecorder activity", "sync location data=" + data);
					if(data != null && data.size() > 0){
						//at least one point exists
						gmap.clear();
						boundBuilder = new LatLngBounds.Builder();
						try {
							PolylineOptions po = new PolylineOptions();
							for (JSONObject item : data) {
								if(firstPoint == null){
									firstPoint = new MarkerOptions().position(new LatLng(item.getDouble("latitude"), item.getDouble("longitude"))).title(mContext.getString(R.string.gmapviewer_marker_start)).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_startpoint));
									movetofirst.setVisibility(View.VISIBLE);
								}
//								Log.e("recorder", "syncloc: " + item.toString());
								boundBuilder.include(new LatLng(item.getDouble("latitude"), item.getDouble("longitude")));
								po.add(new LatLng(item.getDouble("latitude"), item.getDouble("longitude")));
								if(item.has("checkin")){
									CandidateCheckinObject tmpcco = new CandidateCheckinObject();
									JSONObject tmpitem = item.getJSONObject("checkin");
									if(tmpitem.has("picture")){
										tmpcco.setPicturePath(tmpitem.getString("picture"));
									}
									if(tmpitem.has("emotion")){
										tmpcco.setEmotionID(Integer.parseInt(tmpitem.getString("emotion")));
									}
									if(tmpitem.has("note")){
										tmpcco.setCheckinText(tmpitem.getString("note"));
									}
									gmap.addMarker(new MarkerOptions().position(new LatLng(item.getDouble("latitude"), item.getDouble("longitude"))).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_48)).snippet(tmpcco.toString()).draggable(true));
								}
							}
							trajectory = gmap.addPolyline(po.color(0xffff0000));
							gmap.addMarker(firstPoint);
							zoomtoextentcameraupdate = CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 30);
							zoomtoextent.setVisibility(View.VISIBLE);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
				break;
			case AntripService2.MSG_STOP_RECORDING_CONFIRMED:
				Log.w("activity", "MSG_STOP_RECORDING_CONFIRMED");
				if(msg.obj instanceof Boolean){
					if((Boolean) msg.obj){
						//trip is confirmed as VALID, show input trip name dialog
						final Dialog dialog = new Dialog(mContext);
						dialog.setContentView(R.layout.edittripname);
						dialog.setTitle(R.string.input_tripname);
						dialog.setCancelable(true);
						dialog.setCanceledOnTouchOutside(true);
						final EditText et = (EditText) dialog.findViewById(R.id.edittripname);
						final String hint = mContext.getString(R.string.untitled_trip);
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
							.setTitle(R.string.alertdialog_invalid_trip_title)
							.setMessage(R.string.alertdialog_invalid_trip_message)
							.setPositiveButton(R.string.alertdialog_iseebutton, null)
							.show();
					}
				} else{
					Log.e("gmaprecorderactivity3", "MSG_STOP_RECORDING_CONFIRMED: msg.obj is not BOOLEAN");
				}
				break;
			case AntripService2.MSG_AUTO_STOP_RECORDING_CONFIRMED:
				Log.w("activity", "MSG_AUTO_STOP_RECORDING_CONFIRMED");
				firstPoint = null;
				zoomtoextentcameraupdate = null;
				zoomtoextent.setVisibility(View.GONE);
				movetofirst.setVisibility(View.GONE);
				checkinBtn.setVisibility(View.GONE);
				recordBtn.setImageResource(R.color.button_state_startrecord);
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
		
		firstPoint = null;
		zoomtoextentcameraupdate = null;
		boundBuilder = null;
		
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
		recordBtn.setVisibility(View.VISIBLE);
		if(AntripService2.isRecording()){
			recordBtn.setImageResource(R.color.button_state_stoprecord);
		} else{
			recordBtn.setImageResource(R.color.button_state_startrecord);
		}
		recordBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AntripService2.isRecording()) {
					// was recording, STOP recording now
					firstPoint = null;
					zoomtoextentcameraupdate = null;
					zoomtoextent.setVisibility(View.GONE);
					movetofirst.setVisibility(View.GONE);
					checkinBtn.setVisibility(View.GONE);
					recordBtn.setImageResource(R.color.button_state_startrecord);
					sendMessageToService(AntripService2.MSG_STOP_RECORDING, null);
					// wait for the stop recording confirmation
					// if trip is valid, show input tripname dialog
					// else show warning
				} else {
					// was NOT recording, START recording now
					if(canStartRecord){
						gmap.clear();
						latestLocation = null;
						firstLoactionFixAnimate = true;
						trajectory = null;
						boundBuilder = new LatLngBounds.Builder();
						sendMessageToService(AntripService2.MSG_START_RECORDING, null);
						recordBtn.setImageResource(R.color.button_state_stoprecord);
					} else{
						Toast.makeText(mContext, R.string.toast_waitingforfirstlocationfix, Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		((TextView) findViewById(R.id.actionbar_activity_title)).setText(R.string.dropdown_recorder);
		
		dropdownList = (ImageButton) findViewById(R.id.actionbar_activity_icon);
		dropdownList.setImageResource(R.drawable.dropdown_recorder);
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
		
		movetofirst = (ImageView) findViewById(R.id.gmaprecorder_movetofirst);
		movetofirst.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(firstPoint != null){
					gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstPoint.getPosition(), 16));
				}
			}
		});
		
		zoomtoextent = (ImageView) findViewById(R.id.gmaprecorder_zoomtoextent);
		zoomtoextent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(zoomtoextentcameraupdate != null){
					gmap.animateCamera(zoomtoextentcameraupdate);
				}
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
			Log.e("activity", "stopping service");
			//if not recording, stop the service
			stopService(new Intent(mContext, AntripService2.class));
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		int duration = Toast.LENGTH_SHORT;

		switch(requestCode){
		case REQUEST_CODE_TAKE_PICTURE:
			Log.e("gmaprecorderactivity3", "onactivityresult: take picture done");
			if(imageUri != null && imageUri.getPath() != null && !(imageUri.getPath().length() == 0)){
				ExifEditor.GeoTagPicture(imageUri.getPath(), cco.getLocation().getLatitude(), cco.getLocation().getLongitude(), String.valueOf(cco.getLocation().getTime()/1000));
				checkinDiag.setPicture(imageUri.getPath());
				cco.setPicturePath(imageUri.getPath());
				
				CharSequence text = "Picture saved to " + imageUri.getPath();
				Toast toast = Toast.makeText(mContext, text, duration);
				toast.show();
			}
			break;
		case REQUEST_CODE_TAKE_VIDEO:
			Log.e("gmaprecorderactivity3", "onactivityresult: take video done"); 
			File videoFile = getFile(REQUEST_CODE_TAKE_VIDEO);
			AssetFileDescriptor videoAsset;
			if (data != null)	{
				try {
					videoAsset = getContentResolver().openAssetFileDescriptor(data.getData(), "r");
					
					FileInputStream fis = videoAsset.createInputStream(); 
					FileOutputStream fos = new FileOutputStream(videoFile);
					
					byte[] buffer = new byte[1024];
					int length;
					while ((length = fis.read(buffer)) > 0) {
						fos.write(buffer, 0, length);
					}       
					fis.close();
					
						fos.close();
				} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		
				checkinDiag.setVideoThumbnail(videoFile.getPath());
				cco.setPicturePath(videoFile.getPath());
				
				CharSequence text = "Video saved to " + videoFile.getPath();
				Toast toast = Toast.makeText(mContext, text, duration);
				toast.show();
			}
			
			break;
		case REQUEST_CODE_SETTINGS:
			switch(resultCode){
			case RESULT_OK:
				//it's fine if you're logged out in recorder activity...
				
				break;
			case RESULT_CANCELED:
			case RESULT_FIRST_USER:
			default:
				//whatever...
				break;
			}
			break;
		default:
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
				if (tmp.substring(tmp.length()-4, tmp.length()).equals(".jpg"))
					((ImageView) root.findViewById(R.id.checkin_marker_picture)).setImageBitmap(BitmapUtility.getPreview(tmp, 200));
				else if (tmp.substring(tmp.length()-4, tmp.length()).equals(".mp4"))
					((ImageView) root.findViewById(R.id.checkin_marker_picture)).setImageBitmap(Bitmap.createScaledBitmap(ThumbnailUtils.createVideoThumbnail(tmp, MediaStore.Video.Thumbnails.MICRO_KIND), 200, 200, true));	
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
//		Log.w("gmaprecorderactivity3", "setCheckinText: " + text);
		if(cco != null){
			cco.setCheckinText(text);
		} else{
			//error
		}
	}
	
	
	private File getFile(int requestCode){
		File imagefile = null;
		String imagename = "";
		
		switch (requestCode)	{
			case REQUEST_CODE_TAKE_PICTURE:
				imagename = String.format("%1$d.jpg", System.currentTimeMillis());
				break;
			case REQUEST_CODE_TAKE_VIDEO:
				imagename = String.format("%1$d.mp4", System.currentTimeMillis());
				break;
		}
			
		
		String path = "/mnt/sdcard/";
//		Log.e("gmaprecorderactivity3", "path= " + path);
//		Log.e("gmaprecorderactivity3w", "system path= " + Environment.getExternalStorageDirectory().getAbsolutePath());
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
	return imagefile;
		
	}


	@Override
	public void startCamera() {
		Log.w("gmaprecorderactivity3", "start camera called");
		
		File imagefile = getFile(REQUEST_CODE_TAKE_PICTURE);	
		
		if(imagefile != null){
			imageUri = Uri.fromFile(imagefile);
//			Log.w("startcamera", "imageUri= " + imageUri.getPath());
			// intent to launch Android camera app to take pictures
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// input the desired filepath + filename
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			// launch the intent with code
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
		}
	}

	@Override
	public void startVideo() {
		Log.w("gmaprecorderactivity3", "start video called");
		
		
//			Log.w("startcamera", "imageUri= " + imageUri.getPath());
			// intent to launch Android camera app to take pictures
			Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			// input the desired filepath + filename
			intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
			// launch the intent with code
			startActivityForResult(intent, REQUEST_CODE_TAKE_VIDEO);
		
	}
	/*
	public void startAudio()	{
		Log.w("gmaprecorderactivity3", "start audio called");
		isRecording = false;
		MediaRecorder mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(getFile(REQUEST_CODE_TAKE_AUDIO).getPath());
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try	{
			mRecorder.prepare();
		}
		catch (IOException e)	{
			Log.e("Audio Record", "prepare() failed");
		}

		LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		View popupView = inflater.inflate(R.layout.audio_ui, null);
		final PopupWindow popup = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		final Button audio_ss = (Button)findViewById(R.id.audio_ss);
		Button audio_final = (Button)findViewById(R.id.audio_final);
		
		audio_ss.setOnClickListener(new Button.OnClickListener(){

			   @Override
			   public void onClick(View arg0) {
				  if (isRecording == true){
					  ((TextView) GMapRecorderActivity3.audio_ss).setText("Record");
					  
				  }
				  
			   }
		});
	}*/
	
	@Override
	public void setMood(int mood) {
//		Log.w("gmaprecorderactivity3", "setMood: " + mood);
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
			gmap.addMarker(new MarkerOptions().position(new LatLng(cco.getLocation().getLatitude(), cco.getLocation().getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_48)).snippet(cco.toString()).draggable(true));
		} else{
			//show warning
		}
	}

	@Override
	public void cancel() {
		Log.w("gmaprecorderactivity3", "cancel called");
		cco = null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_settings:
			startActivityForResult(new Intent(mContext, tw.plash.antrip.offline.Settings.class), REQUEST_CODE_SETTINGS);
			return true;
		case R.id.menu_help:
			Toast.makeText(mContext, R.string.toast_theres_no_help, Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}