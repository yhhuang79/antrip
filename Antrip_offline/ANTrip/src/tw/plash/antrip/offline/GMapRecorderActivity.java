package tw.plash.antrip.offline;

import java.sql.Timestamp;
import java.util.Date;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class GMapRecorderActivity extends MapActivity {
	
	private boolean mIsRecordBound;
	final private Messenger inMessenger = new Messenger(new IncomingHandler());
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BasicLocationService.MSG_LOCATION_UPDATE:
				if(msg.obj instanceof Location){
					Location loc = (Location) msg.obj;
					//add location filter here
					gmapview.getController().animateTo(new GeoPoint((int)(loc.getLatitude() * 1E6), (int)(loc.getLongitude() * 1E6)));
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	private Messenger recordOutMessenger = null;
	private ServiceConnection recordConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.w("GMapRecorderActivity", "recordConnection: onServiceConnected");
			recordOutMessenger = new Messenger(service);
			try {
				Message msg = Message.obtain(null, AntripService.MSG_REGISTER_CLIENT);
				msg.replyTo = inMessenger;
				recordOutMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.w("GMapRecorderActivity", "recordConnection: onServiceDisconnected");
			recordOutMessenger = null;
		}
	};
	
	private void sendMessageToRecordService(int msgType, Object payload) {
		if (mIsRecordBound) {
			if (recordOutMessenger != null) {
				try {
					if (payload != null) {
						recordOutMessenger.send(Message.obtain(null, msgType, payload));
					} else {
						recordOutMessenger.send(Message.obtain(null, msgType));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void doBindRecordService() {
		bindService(new Intent(mContext, RecordLocationService.class), recordConnection, Context.BIND_AUTO_CREATE);
		mIsRecordBound = true;
	}
	
	private void doUnbindRecordService() {
		if (mIsRecordBound) {
			if (recordOutMessenger != null) {
				
				try {
					Message msg = Message.obtain(null, RecordLocationService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = inMessenger;
					recordOutMessenger.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			unbindService(recordConnection);
			mIsRecordBound = false;
		}
	}
	
	private boolean mIsBasicBound;
	private Messenger basicOutMessenger = null;
	private ServiceConnection basicConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.w("GMapRecorderActivity", "basicConnection: onServiceConnected");
			basicOutMessenger = new Messenger(service);
			try {
				Message msg = Message.obtain(null, AntripService.MSG_REGISTER_CLIENT);
				msg.replyTo = inMessenger;
				basicOutMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.w("GMapRecorderActivity", "basicConnection: onServiceDisconnected");
			basicOutMessenger = null;
		}
	};
	
	private void sendMessageToBasicService(int msgType, Object payload) {
		if (mIsBasicBound) {
			if (basicOutMessenger != null) {
				try {
					if (payload != null) {
						basicOutMessenger.send(Message.obtain(null, msgType, payload));
					} else {
						basicOutMessenger.send(Message.obtain(null, msgType));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void doBindBasicService() {
		bindService(new Intent(mContext, BasicLocationService.class), basicConnection, Context.BIND_AUTO_CREATE);
		mIsBasicBound = true;
	}
	
	private void doUnbindBasicService() {
		if (mIsBasicBound) {
			if (basicOutMessenger != null) {
				
				try {
					Message msg = Message.obtain(null, BasicLocationService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = inMessenger;
					basicOutMessenger.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			unbindService(basicConnection);
			mIsBasicBound = false;
		}
	}
	
	private MapView gmapview;
	private Context mContext;
	private ImageButton recordBtn;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.gmaprecorder);
		
		mContext = this;
		
		gmapview = (MapView) findViewById(R.id.gmapview);
		
		gmapview.setBuiltInZoomControls(true);
		gmapview.setClickable(true);
		
		recordBtn = (ImageButton) findViewById(R.id.actionbar_action_button2);
		if (RecordLocationService.isRecording()) {
			recordBtn.setImageResource(R.drawable.actionbar_record_pressed);
		} else {
			recordBtn.setImageResource(R.drawable.actionbar_record);
		}
		recordBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (RecordLocationService.isRecording()) {
					// stop recording
					((ImageButton) v).setImageResource(R.drawable.actionbar_record);
					sendMessageToRecordService(RecordLocationService.MSG_STOP_RECORDING, null);
					final Dialog dialog = new Dialog(mContext);
					dialog.setContentView(R.layout.edittripname);
					dialog.setCancelable(false);
					final EditText et = (EditText) dialog.findViewById(R.id.edittripname);
					final String hint = new Timestamp(new Date().getTime()).toString();
					et.setHint(hint);
					((Button) dialog.findViewById(R.id.edittripnamebtn)).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							String tripname = null;
							if (et.getText().length() > 0) {
								Toast.makeText(mContext, et.getText().toString(), Toast.LENGTH_LONG).show();
								tripname = et.getText().toString();
							} else {
								Toast.makeText(mContext, "no input", Toast.LENGTH_LONG).show();
								tripname = hint;
							}
							sendMessageToRecordService(RecordLocationService.MSG_SAVE_TRIP_NAME, tripname);
							stopService(new Intent(mContext, RecordLocationService.class));
							doUnbindRecordService(); //unbind from record location service
							doBindBasicService(); //bind to basic location service
							dialog.dismiss();
						}
					});
					dialog.show();
				} else {
					// start recording
					((ImageButton) v).setImageResource(R.drawable.actionbar_record_pressed);
					doUnbindBasicService(); //unbind from basic location service
					startService(new Intent(mContext, RecordLocationService.class).setAction("record"));
					doBindRecordService(); //bind to record location service 
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(RecordLocationService.isRecording()){
			doBindRecordService();
		} else{
			doBindBasicService();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(RecordLocationService.isRecording()){
			doUnbindRecordService();
		} else{
			doUnbindBasicService();
		}
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}