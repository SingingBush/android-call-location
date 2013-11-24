package me.samael.android.calllocation;

import java.util.List;

import me.samael.android.calllocation.data.CallDbAdapter;
import me.samael.android.calllocation.data.SharedPrefs;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.support.v4.app.Fragment;

public class CallMapActivity extends Fragment {
	private static final String TAG = CallMapActivity.class.getSimpleName();

	private MapView mapView;
	private GoogleMap map;
	Long callId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.callmap, container, false);

        if (v != null) {
            mapView = (MapView) v.findViewById(R.id.map_view);
        } else {
            Log.e(TAG, "MapView was null");
        }
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
        map.animateCamera(cameraUpdate);

        return v;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

//	@Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.callmap);
//
//        mapView = (MapView) findViewById(R.id.mapview);
//        mapView.setBuiltInZoomControls(true);
//
//        mapView = (MapView)findViewById(R.id.mapview);
//
//        mapController = mapView.getController();
//
//        callId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(CallDbAdapter.KEY_ID);
//        Bundle extras = getIntent().getExtras();
//        callId = extras != null ? extras.getLong(CallDbAdapter.KEY_ID) : null;
//	}
	
//	@Override
//    protected void onStart() {
//        super.onStart();
//        Log.d(TAG, "onStart. callId: " + callId);
//
//        if(callId > 0) {
//        	try {
//        		CallDbAdapter db = new CallDbAdapter(this);
//        		db.open();
//        		Cursor cursor = db.selectAllFromCallHistoryWhereIdEquals(callId);
//        		Log.v(TAG, "Cursor size: " + cursor.getCount());
//                startManagingCursor(cursor);
//
//                double dlong = cursor.getDouble(cursor.getColumnIndexOrThrow(CallDbAdapter.KEY_LONGITUTE));
//                double dlat = cursor.getDouble(cursor.getColumnIndexOrThrow(CallDbAdapter.KEY_LATITUDE));
//
//                Log.v(TAG, "Creating GeoPoint from lat: " + dlat + " and long: " + dlong);
//
//                GeoPoint geoPoint = new GeoPoint( (int) (dlat*1E6), (int) (dlong*1E6) );
//                mapController.setZoom(SharedPrefs.getCallLocationPrefs(this).getMapZoomLevel()); // between 12 and 18 should be good - should be a preference
//                //mapController.setCenter(geoPoint);
//                mapController.animateTo(geoPoint);
//
//                placeOverLay(geoPoint, null, null);
//
//                db.close();
//            } catch (IllegalArgumentException e) {
//            	Log.e(TAG, e.getMessage());
//            }
//        }
//	}
//
//	private void placeOverLay(GeoPoint geoPoint, String phoneNumber, String timestamp) {
//		List<Overlay> mapOverlays = mapView.getOverlays();
//        Drawable drawable = this.getResources().getDrawable(R.drawable.map_marker);
//        MyMapOverlay overlay = new MyMapOverlay(drawable, this);
//        OverlayItem overlayitem = new OverlayItem(geoPoint, phoneNumber, timestamp);
//        overlay.addOverlay(overlayitem);
//        mapOverlays.add(overlay);
//	}
//
//	@Override
//	protected boolean isRouteDisplayed() {
//		// TODO Auto-generated method stub
//		return false;
//	}

}
