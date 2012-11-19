package tw.plash.antrip.offline;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import android.os.AsyncTask;

public class ConnectionManager implements PLASHConnectionInterface{
	
	String url;
	HttpPost postRequest;
	HttpGet getRequest;
	HttpClient httpClient;
	HttpResponse serverResponse;
	
	public ConnectionManager() {
		
	}
	
	private void cleanUp(){
		url = null;
		postRequest = null;
		getRequest = null;
		if(httpClient != null){
			httpClient.getConnectionManager().shutdown();
			httpClient = null;
		}
		serverResponse = null;
	}
	
	@Override
	public void login() {
		
	}

	@Override
	public void getTripList() {
		
	}

	@Override
	public void getFriendList() {
		
	}
	
	private class asyncConnection extends AsyncTask<Void, Void, Void>{
		
		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}
		
	}
}
