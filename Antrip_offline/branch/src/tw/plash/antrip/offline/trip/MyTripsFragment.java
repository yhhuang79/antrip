package tw.plash.antrip.offline.trip;

import java.util.List;

import tw.plash.antrip.offline.R;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MyTripsFragment extends ListFragment implements LoaderCallbacks<List<String>> {
	
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
		return new MyTripListLoader(getActivity());
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
					getLoaderManager().restartLoader(0, null, MyTripsFragment.this);
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
}
