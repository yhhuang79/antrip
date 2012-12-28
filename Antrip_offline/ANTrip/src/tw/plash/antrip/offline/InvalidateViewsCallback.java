package tw.plash.antrip.offline;

public interface InvalidateViewsCallback {
	public void requestViewsInvalidation();
	public void removePosition(int position);
}
