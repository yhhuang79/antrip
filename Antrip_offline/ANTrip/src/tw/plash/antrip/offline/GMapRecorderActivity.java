package tw.plash.antrip.offline;

import java.sql.Timestamp;
import java.util.Date;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class GMapRecorderActivity extends MapActivity {
	
	
	
	
	private boolean mIsBound;
	private Messenger outMessenger = null;
	final private Messenger inMessenger = new Messenger(new IncomingHandler());
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.w("Activity", "mConnection: onServiceConnected");
			outMessenger = new Messenger(service);
			try {
				Message msg = Message.obtain(null, AntripService.MSG_REGISTER_CLIENT);
				msg.replyTo = inMessenger;
				outMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.w("Activity", "mConnection: onServiceDisconnected");
			outMessenger = null;
		}
	};
	
	private void sendMessageToService(int msgType, Object payload) {
		if (mIsBound) {
			if (outMessenger != null) {
				try {
					if (payload != null) {
						outMessenger.send(Message.obtain(null, msgType, payload));
					} else {
						outMessenger.send(Message.obtain(null, msgType));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void doBindService() {
		bindService(new Intent(mContext, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
		
	}
	
	private void doUnbindService() {
		if(mIsBound){
			if(outMessenger != null){
				
				try {
					Message msg = Message.obtain(null, LocationService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = inMessenger;
					outMessenger.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			unbindService(mConnection);
			mIsBound = false;
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
		// gmapview.getController().animateTo(new GeoPoint((int)(25.035839 *
		// 1E6), (int)(121.538589 * 1E6)));
		
		recordBtn = (ImageButton) findViewById(R.id.actionbar_action_button2);
		if(LocationService.isRecording()){
			recordBtn.setImageResource(R.drawable.actionbar_record_pressed);
		} else{
			recordBtn.setImageResource(R.drawable.actionbar_record);
		}
		recordBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(LocationService.isRecording()){
					//stop recording
					((ImageButton)v).setImageResource(R.drawable.actionbar_record);
					sendMessageToService(LocationService.MSG_STOP_RECORDING, null);
					{
						final Dialog dialog = new Dialog(mContext);
						dialog.setContentView(R.layout.edittripname);
						dialog.setCancelable(false);
						final EditText et = (EditText) dialog.findViewById(R.id.edittripname);
						final String hint = new Timestamp(new Date().getTime()).toString();
						et.setHint(hint);
						((Button) dialog.findViewById(R.id.edittripnamebtn)).setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if(et.getText().length() > 0){
									Toast.makeText(mContext, et.getText().toString(), Toast.LENGTH_LONG).show();
								} else{
									Toast.makeText(mContext, hint, Toast.LENGTH_LONG).show();
								}
								dialog.dismiss();
							}
						});
						dialog.show();
					}
				} else{
					//start recording
					((ImageButton)v).setImageResource(R.drawable.actionbar_record_pressed);
					sendMessageToService(LocationService.MSG_START_RECORDING, null);
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		doBindService();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		doUnbindService();
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}