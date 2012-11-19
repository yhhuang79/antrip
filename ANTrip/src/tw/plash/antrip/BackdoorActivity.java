package tw.plash.antrip;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class BackdoorActivity extends Activity {
	
	private TextView tvfile;
	private TextView tvpictures;
	private Button uploadbtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main2);
		
		tvfile = (TextView) findViewById(R.id.backdoor_tv_file);
		tvpictures = (TextView) findViewById(R.id.backdoor_tv_picture);
		uploadbtn = (Button) findViewById(R.id.backdoor_upload_btn);
		
		Intent intent = getIntent();
		if(intent != null){
			String path = intent.getStringExtra("path");
			if(path != null){
				tvfile.setText(path);
				if(path.contains("sdcard")){
					File file = new File("/mnt/sdcard/DCIM/antrip/");
					if(file.exists()){
						String[] tmp = file.list();
						String pictures = null;
						for(String item : tmp){
							if(pictures == null){
								pictures = item;
							} else{
								pictures = pictures + "\n" + item;
							}
						}
						tvpictures.setText(pictures);
					} else{
						Log.e("backdoor activity", "/mnt/sdcard/DCIM/antrip/ does not exist");
					}
				} else if(path.contains("emmc")){
					File file = new File("/mnt/emmc/DCIM/antrip/");
					if(file.exists()){
						String[] tmp = file.list();
						String pictures = null;
						for(String item : tmp){
							if(pictures == null){
								pictures = item;
							} else{
								pictures = pictures + "\n" + item;
							}
						}
						tvpictures.setText(pictures);
					} else{
						Log.e("backdoor activity", "/mnt/emmc/DCIM/antrip/ does not exist");
					}
				} else{
					Log.e("backdoor activity", "neither sdcard or emmc is contained in path");
					finish();
				}
			}
		}
		
		uploadbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
