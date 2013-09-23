package tw.plash.antrip.offline.trip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tw.plash.antrip.offline.R;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class PublicTripsFragment extends ListFragment implements LoaderCallbacks<List<String>> {
	
	private ArrayAdapter<String> adapter;
	private View pending;
	private View retry;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootview = inflater.inflate(R.layout.mytriplist, container, false);
		pending = rootview.findViewById(R.id.pending); 
		retry = rootview.findViewById(R.id.retry); 
		return rootview;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setHasOptionsMenu(false);
		
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Toast.makeText(getActivity(), "clicked: " + adapter.getItem(position), Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public Loader<List<String>> onCreateLoader(int arg0, Bundle arg1) {
		return new PublicTripListLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<String>> arg0, List<String> arg1) {
		adapter.clear();
		if(arg1 != null){
			for(String item : arg1){
				adapter.add(item);
			}
		} else{
			retry.setVisibility(View.VISIBLE);
			retry.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getLoaderManager().restartLoader(0, null, PublicTripsFragment.this);
					retry.setVisibility(View.GONE);
					pending.setVisibility(View.VISIBLE);
				}
			});
		}
		pending.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<List<String>> arg0) {
		
	}
	
	private static class TripListLoader extends AsyncTaskLoader<List<String>>{
		
		List<String> trips;
		
		public TripListLoader(Context context) {
			super(context);
			Log.e("loader", "constructor");
		}

		@Override
		public List<String> loadInBackground() {
			Log.e("loader", "start loading");
			trips = new ArrayList<String>();
			
			HttpURLConnection conn = null;
			
			try{
				
				URL url = new URL("http://192.168.56.102/getTrips.php?userid=123");
				
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setReadTimeout(5000);
				
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String readInputLine = null;
				readInputLine = in.readLine();
				
				JSONObject result = new JSONObject(new JSONTokener(readInputLine));
				in.close();
				
				int code = result.getInt("status code");
				if(code == 200){
					JSONArray data = result.getJSONArray("query result");
					for(int i = 0; i < data.length(); i++){
						JSONObject obj = data.getJSONObject(i);
						String name = obj.getString("tripname");
						String time = obj.getString("starttime");
						trips.add(name + "\n" + time);
					}
					Log.e("loader", "got data");
					return trips;
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				if(conn != null){
					conn.disconnect();
					conn = null;
				}
			}
			
			Log.e("loader", "error");
			return null;
		}
		
		@Override
		public void deliverResult(List<String> data) {
			if(isStarted()){
				super.deliverResult(data);
			}
		}
		
		@Override
		protected void onStartLoading() {
			if(trips != null){
				deliverResult(trips);
			}
			if(takeContentChanged() || trips == null){
				forceLoad();
			}
		}
		
		@Override
		protected void onStopLoading() {
			cancelLoad();
		}
		
		@Override
		protected void onReset() {
			super.onReset();
			
			onStopLoading();
			
			if(trips != null){
				trips = null;
			}
		}
	}
}
