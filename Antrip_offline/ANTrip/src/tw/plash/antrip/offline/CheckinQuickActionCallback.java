package tw.plash.antrip.offline;

public interface CheckinQuickActionCallback {
	public void setCheckinText(String text);
	
	public void startCamera();
	
	public void startVideo();
	
	public void setMood(int mood);
	
	public void commit();
	
	public void cancel();
}
