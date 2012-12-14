package tw.plash.antrip.offline;

import java.sql.Timestamp;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class GMapRecorderActivity2 extends FragmentActivity implements InfoWindowAdapter{
	
	private GoogleMap gmap;
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.gmaprecorder);
		
		mContext = this;
		
		((Button) findViewById(R.id.btn_checkin)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(mContext, CheckinWindow.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), 100);
			}
		});
		
		isMapAvailable();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case 100:
			switch (resultCode) {
			case RESULT_OK:
				if (data.getSerializableExtra("cco") != null) {
					Log.e("antrip activity", "received CCO!!!");
					
					CandidateCheckinObject cco = (CandidateCheckinObject) data.getSerializableExtra("cco");
					
//					gmap.addMarker(new MarkerOptions().position(new LatLng(gmap.getCameraPosition().target.latitude, gmap.getCameraPosition().target.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker)).title("check-in").snippet(cco.getCheckinText()));
					gmap.addMarker(new MarkerOptions().position(new LatLng(gmap.getCameraPosition().target.latitude, gmap.getCameraPosition().target.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.placemarker_48)).snippet(cco.getCheckinText()));
				}
				break;
			case RESULT_CANCELED:
				Log.e("antrip activity", "received CCO: canceled");
				break;
			default:
				Log.e("antrip activity", "received CCO: default");
				break;
			}
			break;
		}
	}
	
	private void isMapAvailable() {
		if (gmap == null) {
			gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gmapview)).getMap();
			if (gmap != null) {
				setUpMap();
			}
		}
	}
	
	private void setUpMap() {
		
		gmap.setInfoWindowAdapter(GMapRecorderActivity2.this);
		
		gmap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				if(marker.getTitle() != null){
					//start or end point
				} else{
					//show full size image via gallery
					Toast.makeText(mContext, "snippet= " + marker.getSnippet(), Toast.LENGTH_LONG).show();
				}
			}
		});
		
		gmap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Start"));
		
		PolylineOptions po = new PolylineOptions();
		po.add(new LatLng(0, 0))
		.add(new LatLng(1, 1))
		.add(new LatLng(1, 2))
		.add(new LatLng(1, 3))
		.add(new LatLng(2, 4))
		.add(new LatLng(2, 5))
		.add(new LatLng(3, 3))
		.add(new LatLng(4, 2))
		.add(new LatLng(5, 1))
		.add(new LatLng(6, 0))
		.add(new LatLng(7, -1))
		.color(0xffdd0000);
		
		gmap.addPolyline(po);
		
		gmap.addMarker(new MarkerOptions().position(new LatLng(7, -1)).title("End"));
	}

	@Override
	public View getInfoContents(Marker marker) {
		if(marker.getTitle() != null){
			Log.e("gmap recorder", "getinfocontent: title not null");
			return null;
		} else{
			Log.e("gmap recorder", "getinfocontent: title null");
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ViewGroup root = (ViewGroup) inflater.inflate(R.layout.checkin_test, null);
			((TextView) root.findViewById(R.id.checkin_test_text)).setText(new Timestamp(new Date().getTime()).toString() + "\n" + marker.getSnippet());
			return root;
		}
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}
}