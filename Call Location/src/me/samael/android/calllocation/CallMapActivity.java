package me.samael.android.calllocation;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.os.Bundle;

public class CallMapActivity extends MapActivity {
	private static final String TAG = CallMapActivity.class.getSimpleName();
	
	private MapView mapView;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callmap);
        
        mapView = (MapView) findViewById(R.id.mapview);       
        mapView.setBuiltInZoomControls(true);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
