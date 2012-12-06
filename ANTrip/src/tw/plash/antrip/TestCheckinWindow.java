package tw.plash.antrip;

import java.io.File;
import java.net.URI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class TestCheckinWindow extends BetterPopupWindow{
	
	private Context mContext;
	
	private ActivityCallback ac;
	
	private CandidateCheckinObject cco;
	
	private ImageView thumbnail;
	
	public TestCheckinWindow(View anchor, Context c, ActivityCallback ac) {
		super(anchor);
		this.mContext = c;
		this.ac = ac;
		this.cco = new CandidateCheckinObject();
	}
	
	@Override
	protected void onCreate() {
		// inflate layout
		LayoutInflater inflater = (LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.test_checkin_layout, null);
		
		final EditText et = (EditText) root.findViewById(R.id.checkin_text);
		et.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				Log.w("test checkin window", "edittext.ontextchanged: " + s);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//				Log.w("test checkin window", "edittext.beforetextchanged: " + s);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				Log.w("test checkin window", "edittext.aftertextchanged: " + s.toString());
				cco.setCheckinText(s.toString());
			}
		});
		
		
		
		((ImageView) root.findViewById(R.id.checkin_mood_btn)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
		((ImageView) root.findViewById(R.id.checkin_picture_btn)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ac.startCamera();
			}
		});
		
		thumbnail = (ImageView) root.findViewById(R.id.checkin_picture_thumbnail);
		
		((Button) root.findViewById(R.id.checkin_commit_btn)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(et.getText().length() > 0){
					cco.setCheckinText(et.getText().toString());
				}
				TestCheckinWindow.this.dismiss();
			}
		});
		
		((Button) root.findViewById(R.id.checkin_cancel_btn)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cco = null;
				TestCheckinWindow.this.dismiss();
			}
		});
		
		// set the inflated view as what we want to display
		this.setContentView(root);
	}
	
	public void setThumbnail(String filename) {
		thumbnail.setImageBitmap(getPreview(filename));
	}
	
	private Bitmap getPreview(String uri) {
		
		final int THUMBNAIL_SIZE = 156;
		
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
}
