package tw.plash.antrip.offline;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	private Context con;

	
	private EditText checkin_text;
	
	public CheckinDialog(Context context, int cv, CheckinQuickActionCallback c) {
		super(context);
		
		this.cqac = c;
		this.con = context;
		
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
				registerForContextMenu(v);
				openContextMenu(v);
				unregisterForContextMenu(v);
		
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
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = new MenuInflater(con);
		inflater.inflate(R.menu.recorder_menu, menu);
		
	}
	
	public boolean onMenuItemSelected(int aFeatureId, MenuItem aMenuItem)	{
		if (aFeatureId==Window.FEATURE_CONTEXT_MENU)
			return onContextItemSelected(aMenuItem);
		else
			return super.onMenuItemSelected(aFeatureId, aMenuItem);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		switch (item.getItemId())	{
		case R.id.Picture:
			cqac.startCamera();
			return true;
		case R.id.Video:
			cqac.startVideo();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	
	public void setPicture(String imagepath){
		Log.e("checkindialog", "setpicture received: " + imagepath);
		Bitmap bitmap = BitmapUtility.getPreview(imagepath, picture_btn.getHeight());
		if(bitmap != null){
			picture_btn.setScaleType(ScaleType.CENTER_CROP);
			picture_btn.setImageBitmap(bitmap);
		}
	}
	
	public void setVideoThumbnail(String mediapath){
		Log.e("checkindialog", "setvideoThumbnail received: " + mediapath);
		Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(new File(mediapath).getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
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

