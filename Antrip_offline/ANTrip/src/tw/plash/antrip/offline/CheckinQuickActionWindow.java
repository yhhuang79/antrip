package tw.plash.antrip.offline;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;

public class CheckinQuickActionWindow extends BetterPopupWindow {
	
	private EditText checkinText = null;
	
	private ImageView moodBtn = null;
	
	private ImageView pictureBtn = null;
	
	private ImageView checkinCommitBtn = null;
	private ImageView checkinCancelBtn = null;
	
	private CheckinQuickActionCallback cqac = null;
	
	//make sure check-in window is dismissed by either commit or cancel button
	private boolean properDismiss;
	
	public CheckinQuickActionWindow(View anchor, CheckinQuickActionCallback cqac) {
		super(anchor);
		this.cqac = cqac;
		properDismiss = false;
	}
	
	@Override
	protected void onCreate() {
		Log.e("checkinquickactionwindow", "oncreate");
		// inflate layout
		LayoutInflater inflater = (LayoutInflater) this.anchor.getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.checkin_layout2, null);
		
		checkinText = (EditText) root.findViewById(R.id.checkin_text);
		checkinText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				Log.w("test checkin window", "edittext.aftertextchanged: " + s.toString());
				// XXX
				// set check-in text via callback
				if (cqac != null) {
					cqac.setCheckinText(s.toString());
				}
			}
		});
		
		moodBtn = (ImageView) root.findViewById(R.id.checkin_mood_btn);
		moodBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int[] loc = {0, 0};
				
				v.getLocationOnScreen(loc);
				Log.e("checkinquicklactionwindow", "loc=" + loc[0] + ", " + loc[1]);
				cqac.showMoodGrid(loc);
			}
		});
		
		pictureBtn = (ImageView) root.findViewById(R.id.checkin_picture_btn);
		pictureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// start camera via callback
				if (cqac != null) {
					cqac.startCamera();
				}
			}
		});
		
		checkinCommitBtn = (ImageView) root.findViewById(R.id.checkin_commit);
		checkinCommitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// call commit via callback
				if (cqac != null) {
					cqac.commit();
					//show warning if committing empty check-in
				}
				properDismiss = true;
				CheckinQuickActionWindow.this.dismiss();
			}
		});
		
		checkinCancelBtn = (ImageView) root.findViewById(R.id.checkin_cancel);
		checkinCancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// call cancel via callback
				if (cqac != null) {
					cqac.cancel();
				}
				properDismiss = true;
				CheckinQuickActionWindow.this.dismiss();
			}
		});
		
		// set the inflated view as what we want to display
		this.setContentView(root);
	}
	
	@Override
	public void setOnDismissListener(OnDismissListener listener) {
		if(!properDismiss){
			if (cqac != null) {
				cqac.cancel();
			}
		}
	};
	
	private void setPicture(String imagepath) {
		Bitmap bitmap = BitmapUtility.getPreview(imagepath, pictureBtn.getHeight());
		if (bitmap != null) {
			if(pictureBtn != null){
				pictureBtn.setImageBitmap(bitmap);
			}
		}
	}
}
