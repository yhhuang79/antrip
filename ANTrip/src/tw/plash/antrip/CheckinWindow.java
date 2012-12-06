package tw.plash.antrip;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;

public class CheckinWindow extends Activity implements MoodCallback{
	
	private Context mContext;
	private Uri imageUri;
	
	private final int REQUEST_CODE_TAKE_PICTURE = 100;
	
	private EditText checkinText;
	
	private ImageView moodBtn;
	private ImageView moodIcon;
	
	private ImageView pictureBtn;
	private ImageView pictureThumbnail;
	
	private ImageView checkinCommitBtn;
	private ImageView checkinCancelBtn;
	
	private CandidateCheckinObject cco;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.checkin_layout);
		
		mContext = this;
		
		cco = new CandidateCheckinObject();
		
		checkinText = (EditText) findViewById(R.id.checkin_text);
		checkinText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				Log.w("test checkin window", "edittext.aftertextchanged: " + s.toString());
				cco.setCheckinText(s.toString());
			}
		});
		
		moodBtn = (ImageView) findViewById(R.id.checkin_mood_btn);
		moodBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//show mood grid popup window
				//need to implement a "mood grid selector interface" and pass it to the popup window
				//as a callback function
				new MoodGrid(v, CheckinWindow.this).showLikeQuickAction(5);
			}
		});
		
		moodIcon = (ImageView) findViewById(R.id.checkin_mood_icon);
		
		pictureBtn = (ImageView) findViewById(R.id.checkin_picture_btn);
		pictureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startCamera();
			}
		});
		
		pictureThumbnail = (ImageView) findViewById(R.id.checkin_picture_thumbnail);
		
		checkinCommitBtn = (ImageView) findViewById(R.id.checkin_commit);
		checkinCommitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//check if there's any input in cco, don't save an empty cco to DB 
				if(cco.isValid()){
					//set the result and finish
					setResult(RESULT_OK, new Intent().putExtra("cco", cco));
				} else{
					setResult(RESULT_CANCELED);
					//show a warning that nothing has been "checked-in"
				}
				finish();
			}
		});
		
		checkinCancelBtn = (ImageView) findViewById(R.id.checkin_cancel);
		checkinCancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cco = null;
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}
	
	private void startCamera() {
		// file name for pictures
		File imagefile = null;
		
		{
			String imagename = String.format("%1$d.jpg", System.currentTimeMillis());
			String path = "/mnt/sdcard/";
			Log.e("checkin window", "path= " + path);
			Log.e("checkin window", "system path= " + Environment.getExternalStorageDirectory().getAbsolutePath());
			File tester = new File(path);
			if (tester.exists() && tester.isDirectory()) {
				Log.w("sdcard/dcim", "exists");
				File dir = new File(path + "antrip");
				dir.mkdir();
				if (dir.exists() && dir.isDirectory()) {
					Log.w("sdcard/dcim", "antrip dir created");
					imagefile = new File(path + "antrip", imagename);
				} else {
					Log.w("sdcard/dcim", "cannot create antrip/not a dir");
					imagefile = null;
				}
				
			} else {
				path = "/mnt/emmc/";
				tester = new File(path);
				if (tester.exists() && tester.isDirectory()) {
					Log.w("emmc/dcim", "exists");
					File dir = new File(path + "antrip");
					dir.mkdirs();
					if (dir.exists() && dir.isDirectory()) {
						Log.w("emmc/dcim", "antrip dir created");
						imagefile = new File(path + "antrip", imagename);
					} else {
						Log.w("emmc/dcim", "cannot create antrip/not a dir");
						imagefile = null;
					}
				}
			}
		}
		if (imagefile != null) {
			imageUri = Uri.fromFile(imagefile);
//			pref.edit().putString("imguri", imageUri.getPath()).commit();
			Log.w("startcamera", "imageUri= " + imageUri.getPath());
			// intent to launch Android camera app to take pictures
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// input the desired filepath + filename
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			// launch the intent with code
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
		} else{
			Log.e("checkin window", "storage not ready/error, imagefile cannot be created!");
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_CODE_TAKE_PICTURE:
			switch(resultCode){
			case RESULT_OK:
				if(imageUri != null){
					SafeBitmapResizer.resize(imageUri.getPath());
					pictureThumbnail.setImageBitmap(getPreview(imageUri.getPath()));
					cco.setPicturePath(imageUri.getPath());
				} else{
					
				}
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}
	
	private Bitmap getPreview(String uri) {
		
		int THUMBNAIL_SIZE = pictureBtn.getHeight() - 20;
		
		File image = new File(uri);
		
		BitmapFactory.Options bounds = new BitmapFactory.Options();
		bounds.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(image.getPath(), bounds);
		if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
			return null;
		
		int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight : bounds.outWidth;
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = originalSize / THUMBNAIL_SIZE;
		return BitmapFactory.decodeFile(image.getPath(), opts);
	}

	@Override
	public void setMood(int mood) {
		switch(mood){
		case 0:
			cco.setEmotionID(10);
			moodIcon.setImageResource(R.drawable.mood_angry);
			break;
		case 1:
			cco.setEmotionID(8);
			moodIcon.setImageResource(R.drawable.mood_bored);
			break;
		case 2:
			cco.setEmotionID(11);
			moodIcon.setImageResource(R.drawable.mood_calm);
			break;
		case 3:
			cco.setEmotionID(1);
			moodIcon.setImageResource(R.drawable.mood_excited);
			break;
		case 4:
			cco.setEmotionID(2);
			moodIcon.setImageResource(R.drawable.mood_happy);
			break;
		case 5:
			cco.setEmotionID(9);
			moodIcon.setImageResource(R.drawable.mood_nervous);
			break;
		case 6:
			cco.setEmotionID(5);
			moodIcon.setImageResource(R.drawable.mood_peaceful);
			break;
		case 7:
			cco.setEmotionID(3);
			moodIcon.setImageResource(R.drawable.mood_pleased);
			break;
		case 8:
			cco.setEmotionID(4);
			moodIcon.setImageResource(R.drawable.mood_relaxed);
			break;
		case 9:
			cco.setEmotionID(7);
			moodIcon.setImageResource(R.drawable.mood_sad);
			break;
		case 10:
			cco.setEmotionID(6);
			moodIcon.setImageResource(R.drawable.mood_sleepy);
			break;
		default:
			cco.setEmotionID(3);
			moodIcon.setImageResource(R.drawable.mood_pleased);
			break;
		}
	}
}
