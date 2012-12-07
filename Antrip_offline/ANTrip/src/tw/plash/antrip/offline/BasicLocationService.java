package tw.plash.antrip.offline;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BasicLocationService extends Service implements LocationPublisher{
	
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_LOCATION_UPDATE = 3;
	
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
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	public void sendMessageToUI(int msgType, Location loc){
		try {
			if(outMessager != null){
				outMessager.send(Message.obtain(null, MSG_LOCATION_UPDATE, loc));
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.w("BasicLocationService", "onBind");
		return inMessenger.getBinder();
	}
	
	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		Log.w("BasicLocationService", "onRebind");
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.w("BasicLocationService", "onUnbind");
		return true;
	}
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.w("BasicLocationService", "onCreate");
		
	}

	@Override
	public void newLocationUpdate(Location location) {
		
	}
}