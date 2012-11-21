package tw.plash.antrip.offline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.emilsjolander.components.StickyListHeaders.StickyListHeadersBaseAdapter;

public class TripListAdapter2 extends StickyListHeadersBaseAdapter {
	
	private LayoutInflater mInflater;
	
	private JSONArray data = null;
	
	public TripListAdapter2(Context context, JSONArray remotedata, JSONArray localdata) {
		super(context);
		
		String local = null;
		String remote = null;
		String combine = null;
		
		if(localdata != null && localdata.length() > 0){
			local = localdata.toString();
		}
		if(remotedata != null && remotedata.length() > 0){
			remote = remotedata.toString();
		}
		
		if(local != null){
			if(remote != null){
				combine = local.substring(0, local.length() - 2) + "," + remote.substring(1);
			} else{
				combine = local;
			}
		} else{
			if(remote != null){
				combine = remote;
			} else{
				//local and remote can't both be null, otherwise this constructor should not even be called upon
				Log.e("triplistadapter2", "local and remote both null, impossibru la");
			}
		}
		
		try {
			data = new JSONArray(new JSONTokener(combine));
//			Log.i("triplistadapter2", "data= " + data.toString());
		} catch (JSONException e) {
			e.printStackTrace();
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
			if(data.getJSONObject(position).has("triplistheader")){
				holder.htv.setText("pending upload");
			} else{
				holder.htv.setText(data.getJSONObject(position).getString("trip_st").substring(0, 7).replace("-", "/"));
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
			if(data.getJSONObject(position).has("triplistheader")){
				id = data.getJSONObject(position).getInt("triplistheader");
			} else{
				id = Long.valueOf(data.getJSONObject(position).getString("trip_st").substring(0, 7).replace("-", ""));
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
			holder.tv1.setText((String) data.getJSONObject(position).getString("trip_name"));
			holder.tv2.setText((String) data.getJSONObject(position).getString("trip_st"));
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
	
	@Override
	public int getCount() {
		return data.length();
	}
	
	@Override
	public Object getItem(int position) {
		Object obj = null;
		try {
			obj = data.get(position);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}