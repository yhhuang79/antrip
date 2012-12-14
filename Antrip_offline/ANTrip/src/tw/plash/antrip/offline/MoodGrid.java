package tw.plash.antrip.offline;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MoodGrid extends BetterPopupWindow implements OnClickListener {
	
	final private MoodCallback mc;
	
	public MoodGrid(View anchor, MoodCallback mc) {
		super(anchor);
		this.mc = mc;
	}
	
	@Override
	protected void onCreate() {
		// inflate layout
		LayoutInflater inflater = (LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.popup_moodgrid_layout, null);
		
		// setup button events
		for (int i = 0, icount = root.getChildCount(); i < icount; i++) {
			View v = root.getChildAt(i);
			
			if (v instanceof LinearLayout) {
				LinearLayout col = (LinearLayout) v;
				
				for (int j = 0, jcount = col.getChildCount(); j < jcount; j++) {
					
					View item = col.getChildAt(j);
					if (item instanceof ImageView) {
						ImageView b = (ImageView) item;
						b.setOnClickListener(this);
					}
				}
			}
		}
		
		// set the inflated view as what we want to display
		this.setContentView(root);
	}
	
	@Override
	public void onClick(View v) {
		// we'll just display a simple toast on a button click
		ImageView b = (ImageView) v;
		Log.w("mood grid", "clicky: " + (String) b.getTag());
		mc.setMood(Integer.parseInt((String) b.getTag()));
		this.dismiss();
	}
}
