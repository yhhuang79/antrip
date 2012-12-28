package tw.plash.antrip.offline;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DropdownFunctionList extends BetterPopupWindow implements OnClickListener {
	
	public DropdownFunctionList(View anchor) {
		super(anchor);
	}
	
	@Override
	protected void onCreate() {
		// inflate layout
		LayoutInflater inflater = (LayoutInflater) this.anchor.getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.dropdown_activitylist, null);
		
		// setup button events
		for (int i = 0, icount = root.getChildCount(); i < icount; i++) {
			View v = root.getChildAt(i);
			
			if (v instanceof LinearLayout) {
				((LinearLayout) v).setOnClickListener(this);
			} else {
				Log.e("dropdown list", "huh2");
			}
		}
		
		// set the inflated view as what we want to display
		this.setContentView(root);
	}
	
	@Override
	public void onClick(View v) {
		// we'll just display a simple toast on a button click
		Context mContext = this.anchor.getContext();
		
		if (v instanceof LinearLayout) {
			LinearLayout b = (LinearLayout) v;
			String tag = (String) b.getTag();
			if (tag.equals("recorder")) {
				mContext.startActivity(new Intent(mContext, GMapRecorderActivity3.class));
			} else if (tag.equals("triplist")) {
				mContext.startActivity(new Intent(mContext, TripListActivity4.class));
			} else if (tag.equals("friendlist")) {
				mContext.startActivity(new Intent(mContext, FriendListActivityTEMP.class));
			}
		}
		
		this.dismiss();
	}
}
