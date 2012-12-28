package tw.plash.antrip.offline;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class CheckinDialog extends Dialog implements MoodCallback{
	
	final CheckinQuickActionCallback cqac;
	
	private ImageView commit_btn;
	private ImageView cancel_btn;
	private ImageView mood_btn;
	private ImageView picture_btn;
	
	private EditText checkin_text;
	
	public CheckinDialog(Context context, int cv, CheckinQuickActionCallback c) {
		super(context);
		
		this.cqac = c;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCancelable(true);
		setCanceledOnTouchOutside(true);
		setContentView(cv);
		
		checkin_text = (EditText) findViewById(R.id.checkin_text);
		
		mood_btn = (ImageView) findViewById(R.id.checkin_mood_btn);
		mood_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new MoodGrid(v, CheckinDialog.this).showLikePopDownMenu(1);
			}
		});
		
		picture_btn = (ImageView) findViewById(R.id.checkin_picture_btn);
		picture_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cqac.startCamera();
			}
		});
		
		commit_btn = (ImageView) findViewById(R.id.checkin_commit);
		commit_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(checkin_text.getEditableText().length() > 0){
					cqac.setCheckinText(checkin_text.getEditableText().toString());
				}
				cqac.commit();
				dismiss();
			}
		});
		
		cancel_btn = (ImageView) findViewById(R.id.checkin_cancel);
		cancel_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cqac.cancel();
				dismiss();
			}
		});
	}
	
	public void setPicture(String imagepath){
		Log.e("checkindialog", "setpicture received: " + imagepath);
		Bitmap bitmap = BitmapUtility.getPreview(imagepath, picture_btn.getHeight());
		if(bitmap != null){
			picture_btn.setScaleType(ScaleType.CENTER_CROP);
			picture_btn.setImageBitmap(bitmap);
		}
	}

	@Override
	public void setMood(int mood) {
		if(cqac != null){
			cqac.setMood(mood);
			switch(mood){
			case 1:
				mood_btn.setImageResource(R.drawable.emotion_excited);
				break;
			case 2:
				mood_btn.setImageResource(R.drawable.emotion_happy);
				break;
			case 3:
				mood_btn.setImageResource(R.drawable.emotion_pleased);
				break;
			case 4:
				mood_btn.setImageResource(R.drawable.emotion_relaxed);
				break;
			case 5:
				mood_btn.setImageResource(R.drawable.emotion_peaceful);
				break;
			case 6:
				mood_btn.setImageResource(R.drawable.emotion_sleepy);
				break;
			case 7:
				mood_btn.setImageResource(R.drawable.emotion_sad);
				break;
			case 8:
				mood_btn.setImageResource(R.drawable.emotion_bored);
				break;
			case 9:
				mood_btn.setImageResource(R.drawable.emotion_nervous);
				break;
			case 10:
				mood_btn.setImageResource(R.drawable.emotion_angry);
				break;
			case 11:
				mood_btn.setImageResource(R.drawable.emotion_calm);
				break;
			default:
				mood_btn.setImageResource(R.drawable.emotion_pleased);
				break;
			}
		}
	}
}