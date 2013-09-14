package tw.plash.antrip.offline;

import tw.plash.antrip.offline.friend.FriendFinder;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class aaa extends Activity {
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		Log.e("aaa", "onConfigurationChanged");
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		DBHelper2 dh2 = new DBHelper2(this);
//		dh2.exportEverything();
//		dh2.setUploadStatus(null, 0);
//		dh2.closeDB();
//		dh2 = null;
		TextView tv = new TextView(this);
		tv.setText("done");
		tv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(aaa.this, FriendFinder.class), 1234);
			}
		});
		setContentView(tv);
		
		Log.e("aaa", "onCreate");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Log.e("aaa", "onResume");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		Log.e("aaa", "onStart");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		Log.e("aaa", "onPause");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		Log.e("aaa", "onStop");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Log.e("aaa", "onDestroy");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e("aaa", "onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
	}
}
