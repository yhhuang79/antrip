package tw.plash.antrip;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class GetTripListAsync extends AsyncTask<Void, Void, Void> {
	
	private DBHelper128 dh;
	private ProgressDialog diag;
	
	public GetTripListAsync(Context mContext) {
		dh = new DBHelper128(mContext);
		diag = new ProgressDialog(mContext);
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		
	}
	
	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		
	}
}
