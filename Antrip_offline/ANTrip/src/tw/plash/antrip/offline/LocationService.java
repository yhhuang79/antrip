package tw.plash.antrip.offline;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class LocationService extends Service {
	
	private static boolean isRecording = false;
	
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_START_RECORDING = 3;
	static final int MSG_STOP_RECORDING = 4;
	static final int MSG_SAVE_TRIP_NAME = 5;
	
	private Messenger outMessager = null;
	final private Messenger inMessenger = new Messenger(new IncomingHandler());
	private class IncomingHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_REGISTER_CLIENT:
				outMessager = msg.replyTo;
				break;
			case MSG_UNREGISTER_CLIENT:
				if(outMessager == msg.replyTo){
					outMessager = null;
				}
				break;
			case MSG_START_RECORDING:
				isRecording = true;
				break;
			case MSG_STOP_RECORDING:
				isRecording = false;
				break;
			case MSG_SAVE_TRIP_NAME:
				
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	public void sendMessageToUI(int msgType, Object payload){
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return inMessenger.getBinder();
	}
	
	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		return true;
	}
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		isRecording = false;
		
		
	}
	
	
	public static boolean isRecording(){
		return isRecording;
	}
}
