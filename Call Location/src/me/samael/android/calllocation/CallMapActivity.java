package me.samael.android.calllocation;

import java.util.List;

import me.samael.android.calllocation.data.CallDbAdapter;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.database.Cursor;
import android.database.SQLException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class CallMapActivity extends MapActivity {
	private static final String TAG = CallMapActivity.class.getSimpleName();
	
	private MapView mapView;
	private MapController mapController;
	Long callId;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callmap);
        
        mapView = (MapView) findViewById(R.id.mapview);       
        mapView.setBuiltInZoomControls(true);
        
        mapView = (MapView)findViewById(R.id.mapview);
        
        mapController = mapView.getController();
        
        callId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(CallDbAdapter.KEY_ID);
        Bundle extras = getIntent().getExtras();
        callId = extras != null ? extras.getLong(CallDbAdapter.KEY_ID) : null;
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart. callId: " + callId);
        
        if(callId > 0) {
        	try {
        		CallDbAdapter db = new CallDbAdapter(this);
        		db.open();
        		Cursor cursor = db.selectAllFromCallHistoryWhereIdEquals(callId);
        		Log.v(TAG, "Cursor size: " + cursor.getCount());
                startManagingCursor(cursor);
                
                double dlong = cursor.getDouble(cursor.getColumnIndexOrThrow(CallDbAdapter.KEY_LONGITUTE));
                double dlat = cursor.getDouble(cursor.getColumnIndexOrThrow(CallDbAdapter.KEY_LATITUDE));
                
                Log.v(TAG, "Creating GeoPoint from lat: " + dlat + " and long: " + dlong);
                
                GeoPoint geoPoint = new GeoPoint( (int) (dlat*1E6), (int) (dlong*1E6) );
                mapController.setZoom(20); //Fixed Zoom Level
                //mapController.setCenter(geoPoint);
                mapController.animateTo(geoPoint);
                
                placeOverLay(geoPoint, null, null);
                
                db.close();
            } catch (IllegalArgumentException e) {
            	Log.e(TAG, e.getMessage());
            }
        }
	}
	
	private void placeOverLay(GeoPoint geoPoint, String phoneNumber, String timestamp) {
		List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.map_marker);
        MyMapOverlay overlay = new MyMapOverlay(drawable, this);
        OverlayItem overlayitem = new OverlayItem(geoPoint, phoneNumber, timestamp);     
        overlay.addOverlay(overlayitem);
        mapOverlays.add(overlay);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
