package tw.plash.antrip.offline;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.emilsjolander.components.StickyListHeaders.StickyListHeadersBaseAdapter;

public class FriendListAdapterTEMP extends BaseExpandableListAdapter {
	
	private LayoutInflater mInflater;
	
	private ArrayList<JSONObject> groupData = null;
	
	private SparseArray<JSONArray> childData = null;
	
	public FriendListAdapterTEMP(Context context, JSONArray groupData) {
		
		if(groupData != null && groupData.length() > 0){
			this.groupData = (ArrayList<JSONObject>) JSONUtility.asList(groupData);
		} else{
			this.groupData = new ArrayList<JSONObject>();
		}
		
		mInflater = LayoutInflater.from(context);
	}
	
	public void addChildData(long id, JSONArray childdata) {
		Log.e("addchilddata", "id:" + id);
		if(childData == null){
			childData = new SparseArray<JSONArray>();
		}
		if((int)id == 0){
			try {
				groupData.get(0).put("shareTripNum", childdata.length());
			} catch (JSONException e) {
				e.printStackTrace();
				//well...don't update it then...
			}
		}
		childData.put((int) id, childdata);
	}
	
	public boolean isChildDataEntryExist(long id){
		if(childData != null){
			if(childData.get((int) id) != null){
				return true;
			} else{
				return false;
			}
		}
		return false;
	}
	
	@Override
	public JSONObject getChild(int groupPosition, int childPosition) {
		JSONObject child = null;
		if(childData != null){
			try {
				child = childData.get((int) getGroupId(groupPosition)).getJSONObject(childPosition);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return child;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
//		if(childData != null){
//			try {
//				return childData.get((int) getGroupId(groupPosition)).getJSONObject(childPosition).getInt("trip_id");
//			} catch (JSONException e) {
//				e.printStackTrace();
//				return childPosition;
//			}
//		} else{
			return childPosition;
//		}
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ChildViewHolder holder = null;
		Log.e("getchildview", groupPosition + ", " + childPosition);
		if(convertView == null){
			holder = new ChildViewHolder();
			convertView = mInflater.inflate(R.layout.friendtriplistitem, null);
			//XXX find all the holder views by id
			holder.name = (TextView) convertView.findViewById(R.id.friendtriplistitem_name);
			holder.time = (TextView) convertView.findViewById(R.id.friendtriplistitem_starttime);
			holder.sharer = (TextView) convertView.findViewById(R.id.friendtriplistitem_sharer);
			convertView.setTag(holder);
		} else{
			holder = (ChildViewHolder) convertView.getTag();
		}
		
		//set view content by getting stuff from data array
		
		try {
			if(childData != null){
				holder.name.setText(childData.get((int) getGroupId(groupPosition)).getJSONObject(childPosition).getString("trip_name"));
				holder.time.setText(childData.get((int) getGroupId(groupPosition)).getJSONObject(childPosition).getString("trip_st"));
				if((int) getGroupId(groupPosition) == 0 && childData.get((int) getGroupId(groupPosition)).getJSONObject(childPosition).has("username")){
					holder.sharer.setText(convertView.getResources().getString(R.string.puclivtripsharer) + " " + childData.get((int) getGroupId(groupPosition)).getJSONObject(childPosition).getString("username"));
					holder.sharer.setVisibility(View.VISIBLE);
				} else{
					holder.sharer.setVisibility(View.GONE);
				}
			} else{
				holder.name.setText("-");
				holder.time.setText("-");
				holder.sharer.setVisibility(View.GONE);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			holder.name.setText("-");
			holder.time.setText("-");
			holder.sharer.setVisibility(View.GONE);
		}
		return convertView;
	}
	
	class ChildViewHolder{
		TextView name, time, sharer;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		int count = 0;
		try {
			if(childData != null){
//				if(groupPosition == 0){
//					count = groupData.get(groupPosition).getInt("shareTripNum") - 1;
//				} else{
					count = groupData.get(groupPosition).getInt("shareTripNum");
//				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return count;
	}

	@Override
	public JSONObject getGroup(int groupPosition) {
		return groupData.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groupData.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		long id = -1;
		try {
			id = groupData.get(groupPosition).getInt("id");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return id;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		GroupViewHolder holder = null;
		if(convertView == null){
			holder = new GroupViewHolder();
			convertView = mInflater.inflate(R.layout.friendlistitemgroup, null);
			//XXX set the view 
			holder.name = (TextView) convertView.findViewById(R.id.friendlistitem_name);
			holder.shared = (TextView) convertView.findViewById(R.id.friendlistitem_numberofsharedtrips);
			holder.image = (ImageView) convertView.findViewById(R.id.friendlist_picture);
			convertView.setTag(holder);
		} else{
			holder = (GroupViewHolder) convertView.getTag();
		}
		//XXX set the textview, imageview, etc. from data
		if(getGroupId(groupPosition) == 0){
			holder.image.setImageResource(R.drawable.antrip_logo);
			holder.name.setText(R.string.public_trip);
			try {
				
				holder.shared.setText(convertView.getResources().getString(R.string.public_trip_available) + " " + String.valueOf(groupData.get(groupPosition).getInt("shareTripNum")));
			} catch (JSONException e) {
				e.printStackTrace();
				holder.shared.setText(convertView.getResources().getString(R.string.public_trip_available) + " 0");
			}
		} else{
			holder.image.setImageResource(R.drawable.antrip_logo); //XXX should use user image
			try {
				holder.name.setText(groupData.get(groupPosition).getString("name"));
				holder.shared.setText(convertView.getResources().getString(R.string.friendlist_sharedtrips) + " " + String.valueOf(groupData.get(groupPosition).getInt("shareTripNum")));
			} catch (JSONException e) {
				e.printStackTrace();
				holder.name.setText("name");
				holder.shared.setText(convertView.getResources().getString(R.string.friendlist_sharedtrips) + " 0");
			}
		}
		
		return convertView;
	}
	
	class GroupViewHolder{
		ImageView image;
		TextView name, shared;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}