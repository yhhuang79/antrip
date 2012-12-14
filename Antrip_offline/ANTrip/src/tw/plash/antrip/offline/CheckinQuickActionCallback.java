package tw.plash.antrip.offline;

import android.view.View;

public interface CheckinQuickActionCallback {
	public void setCheckinText(String text);
	
	public void startCamera();
	
	public void setMood(int mood);
	
	public void commit();
	
	public void cancel();
}
