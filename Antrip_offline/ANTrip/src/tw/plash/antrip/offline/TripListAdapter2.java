package tw.plash.antrip.offline;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.emilsjolander.components.StickyListHeaders.StickyListHeadersBaseAdapter;

public class TripListAdapter2 extends StickyListHeadersBaseAdapter {
	
	private LayoutInflater mInflater;
	
//	private JSONArray data = null;
	private ArrayList<JSONObject> data = null;
	
	public TripListAdapter2(Context context, JSONArray remotedata, JSONArray localdata) {
		super(context);
		
		ArrayList<JSONObject> local = null;
		ArrayList<JSONObject> remote = null;
		
		if(localdata != null && localdata.length() > 0){
			local = (ArrayList<JSONObject>) JSONUtility.asList(localdata);
			Log.i("triplistadapter2", "local= " + local.toString());
		}
		if(remotedata != null && remotedata.length() > 0){
			remote = (ArrayList<JSONObject>) JSONUtility.asList(remotedata);
		}
		
		if(local != null){
			//local not null
			data = new ArrayList<JSONObject>(local);
			if(remote != null){
				//remote also notn ull
				data.addAll(remote);
			}
		} else{
			//local is null
			if(remote != null){
				//remote not null
				data = new ArrayList<JSONObject>(remote);
			} else{
				Log.e("triplistadapter2", "local and remote both null, impossibru la");
				throw new NullPointerException();
			}
		}
		
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getHeaderView(int position, View convertView) {
		HeaderViewHolder holder = null;
		
		if(convertView == null){
			holder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.triplistheader, null);
			holder.htv = (TextView) convertView.findViewById(R.id.triplistheader_text);
			convertView.setTag(holder);
		} else{
			holder = (HeaderViewHolder) convertView.getTag();
		}
		
		try {
			//only local tripinfo have this column
			if(data.get(position).has("triplistheader")){
				holder.htv.setText("pending upload");
			} else{
				holder.htv.setText(data.get(position).getString("trip_st").substring(0, 7).replace("-", "/"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			holder.htv.setText("error");
		}
		
		return convertView;
	}
	
	class HeaderViewHolder{
		TextView htv;
	}
	
	@Override
	public long getHeaderId(int position) {
		long id = 0;
		
		try {
			if(data.get(position).has("triplistheader")){
				id = data.get(position).getInt("triplistheader");
			} else{
				id = Long.valueOf(data.get(position).getString("trip_st").substring(0, 7).replace("-", ""));
			}
			Log.w("header id", "position=" + position + "; id= " + id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return id;
	}

	@Override
	protected View getView(int position, View convertView) {
		ViewHolder holder;
		
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.triplistitem, null);
			
			holder = new ViewHolder();
			holder.tv1 = (TextView) convertView.findViewById(R.id.triplistitem_name);
			holder.tv2 = (TextView) convertView.findViewById(R.id.triplistitem_starttime);
			
			convertView.setTag(holder);
		} else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		try {
			holder.tv1.setText((String) data.get(position).getString("trip_name"));
			holder.tv2.setText((String) data.get(position).getString("trip_st"));
		} catch (JSONException e) {
			e.printStackTrace();
			holder.tv1.setText("");
			holder.tv2.setText("");
		}
		
		return convertView;
	}
	
	class ViewHolder{
		TextView tv1, tv2;
	}
	
	public boolean remove(int position){
		if(position < 0 || position > data.size()){
			return false;
		} else{
			data.remove(position);
			return true;
		}
	}
	
	@Override
	public int getCount() {
		return data.size();
	}
	
	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}