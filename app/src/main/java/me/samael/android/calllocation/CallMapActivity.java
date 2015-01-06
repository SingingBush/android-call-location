package me.samael.android.calllocation;

import java.util.List;

import me.samael.android.calllocation.data.CallDbAdapter;
import me.samael.android.calllocation.data.SharedPrefs;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.Fragment;
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

public class CallMapActivity extends Activity implements OnMapReadyCallback {

	private static final String TAG = CallMapActivity.class.getSimpleName();

    //private GoogleMap map;
    Long callId;

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng sydney = new LatLng(-33.867, 151.206);

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

        map.addMarker(new MarkerOptions()
                .title("Sydney")
                .snippet("The most populous city in Australia.")
                .position(sydney));
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callmap);

//        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_view)).getMap();
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

        // a little something for debugging::
//        if (map!=null) {
//            Marker hamburg = map.addMarker(new MarkerOptions().position(new LatLng(53.558, 9.927))
//                    .title("Hamburg"));
//            Marker kiel = map.addMarker(new MarkerOptions()
//                    .position(new LatLng(53.551, 9.993))
//                    .title("Kiel")
//                    .snippet("Kiel is cool")
//                    .icon(BitmapDescriptorFactory
//                            .fromResource(R.drawable.map_marker)));
//    }

//        mapView = (MapView) findViewById(R.id.mapview);
//        mapView.setBuiltInZoomControls(true);
//
//        mapView = (MapView)findViewById(R.id.mapview);
//
//        mapController = mapView.getController();

            callId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(CallDbAdapter.KEY_ID);
            Bundle extras = getIntent().getExtras();
            callId = extras != null ? extras.getLong(CallDbAdapter.KEY_ID) : null;
	}
	
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

    // not sure if fragment needed, check docs:
    // https://developers.google.com/maps/documentation/android/
    public static class CallMap extends Fragment {

        private MapView mapView;
        private GoogleMap map;

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
//        try {
            MapsInitializer.initialize(getActivity());
//        } catch (GooglePlayServicesNotAvailableException e) {
//            e.printStackTrace();
//        }

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
    }

}
