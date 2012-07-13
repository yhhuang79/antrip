package tw.plash.antrip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class test21 extends Activity {

	private PLASHCameraPreview mPreview;
	private Camera mCamera;

	private Button btn3;

	private Context mContext;

//	private FocusRectangle mFocusRectangle;
	private FrameLayout mCameraPreviewPanel;
	
	private RotateImageView riv, riv2;

	private int requestCode;
	
	private SensorManager sm;

	private SensorEventListener sml = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {

			float x = event.values[0];
			float y = event.values[1];

			if (Math.abs(x) > Math.abs(y)) {
				if (x > 0) {
					// 90 degrees counter-clockwise
					riv.setDegree(0);
				} else {
					// 90 degrees clockwise
					riv.setDegree(180);
				}
			} else {
				if (y > 0) {
					// normal position
					riv.setDegree(90);
				} else {
					// upside down
					riv.setDegree(270);
				}
			}

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestCode = getIntent().getExtras().getInt("requestCode");
		
		mContext = this;

		mPreview = new PLASHCameraPreview(mContext);

		setContentView(R.layout.main2);

		btn3 = (Button) findViewById(R.id.btn3);

		riv = new RotateImageView(mContext, null);
		riv.setImageResource(R.drawable.ic_launcher);

		riv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// new RotateImageView(mContext, null).addTouchables(new
				// ArrayList<View>().add(new TextView(mContext)));
				Toast.makeText(mContext, "lalala", Toast.LENGTH_SHORT).show();
			}
		});

		((FrameLayout) findViewById(R.id.testFrame)).addView(riv);

		riv2 = new RotateImageView(mContext, null);
		riv2.setImageResource(R.drawable.ic_launcher);

		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		sm.registerListener(sml, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);

		mCameraPreviewPanel = ((FrameLayout) findViewById(R.id.cameraPreviewPanel));
		mCameraPreviewPanel.addView(mPreview);

//		mFocusRectangle = new FocusRectangle(mContext, null);
//		mFocusRectangle.setLayoutParams(new LayoutParams(100, 100));
//		mCameraPreviewPanel.addView(mFocusRectangle);

		mCameraPreviewPanel.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					Log.e("ontouch", "start");

					int x = (int) event.getX();
					int y = (int) event.getY();

					Rect r = new Rect();
//					mCameraPreviewPanel.getLocalVisibleRect(r);
					mCameraPreviewPanel.getDrawingRect(r);
					Log.e("preview rect", "left= " + r.left + ", top= " + r.top + ", right= "
							+ r.right + ", bottom= " + r.bottom);

					if (r.contains(x, y)) {
//						mFocusRectangle.clear();
						Log.e("event location", "x= " + x + ", y= " + y);
//						mFocusRectangle.setPosition(x, y);
//						mFocusRectangle.showStart();
						mCamera.autoFocus(mAutoFocusCallback);
					}

					Log.e("ontouch", "done");
				}
				return true;
			}
		});

		btn3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// mCamera.takePicture(mShutterCallback, null,
				// mPitcureCallback);
				// mCamera.takePicture(null, null, mPitcureCallback);
				// riv.setDegree((int) (System.currentTimeMillis() % 360));
				// mCamera.autoFocus(mAutoFocusCallback);
			}
		});

	}

	Camera.AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			Log.e("onautofocus", "start");
			if (success) {
//				mFocusRectangle.showSuccess();

				camera.takePicture(mShutterCallback, null, mPitcureCallback);
			} else {
//				mFocusRectangle.showFail();

				camera.cancelAutoFocus();
			}
			Log.e("onautofocus", "done");
		}
	};

	Camera.ShutterCallback mShutterCallback = new ShutterCallback() {

		@Override
		public void onShutter() {
			Toast.makeText(mContext, "aaa", Toast.LENGTH_SHORT).show();
		}
	};

	Camera.PictureCallback mPitcureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			Log.e("onpicturetaken", "start");
			FileOutputStream outStream = null;
			String filename = String.format("%1$d.jpg", System.currentTimeMillis());
			String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
			try {
				File file = new File(filepath, filename);
				outStream = new FileOutputStream(file);
				outStream.write(data);
				outStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(requestCode == 1){
					//activity started requesting for one photo only
					Log.e("photo location", "at " + filepath + "/" + filename);
					setResult(1, new Intent().putExtra("photoLocation", filepath + "/" + filename));
					finish();
				} else{
					mCamera.startPreview();
				}
			}
			Log.e("onpicturetaken", "done");
		}
	};

	public int getScreenOrientation() {
		Display getOrient = getWindowManager().getDefaultDisplay();
		int orientation = Configuration.ORIENTATION_UNDEFINED;
		if (getOrient.getWidth() == getOrient.getHeight()) {
			orientation = Configuration.ORIENTATION_SQUARE;
		} else {
			if (getOrient.getWidth() < getOrient.getHeight()) {
				orientation = Configuration.ORIENTATION_PORTRAIT;
			} else {
				orientation = Configuration.ORIENTATION_LANDSCAPE;
			}
		}
		return orientation;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mCamera = Camera.open();

		mPreview.setCamera(mCamera);
	}

	@Override
	protected void onPause() {

		sm.unregisterListener(sml);

		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
//		if(requestCode == 1){
//			setResult(0);
//		}
		
		super.onPause();
	}
}
