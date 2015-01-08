package me.samael.android.calllocation;


import me.samael.android.calllocation.data.CallDbAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

public class CallMapActivity extends Activity implements OnMapReadyCallback {

	private static final String TAG = CallMapActivity.class.getSimpleName();

    String phoneNumber;
    LatLng location;
    Long callId;

    @Override
    public void onMapReady(GoogleMap map) {
        //map.setMyLocationEnabled(true);
        //map.getUiSettings().setMyLocationButtonEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        //map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        map.addMarker(new MarkerOptions()
                .title(phoneNumber)
                //.snippet("") // put timestamp here
                .position(location)); // .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker)));
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callmap);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        callId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(CallDbAdapter.KEY_ID);
        Bundle extras = getIntent().getExtras();
        callId = extras != null ? extras.getLong(CallDbAdapter.KEY_ID) : null;
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, String.format("onStart. callId: %d", callId));

        if(callId > 0) {
        	try {
        		CallDbAdapter db = new CallDbAdapter(this);
        		db.open();
        		Cursor cursor = db.selectAllFromCallHistoryWhereIdEquals(callId);
        		Log.v(TAG, "Cursor size: " + cursor.getCount());
                startManagingCursor(cursor);

                double dlong = cursor.getDouble(cursor.getColumnIndexOrThrow(CallDbAdapter.KEY_LONGITUTE));
                double dlat = cursor.getDouble(cursor.getColumnIndexOrThrow(CallDbAdapter.KEY_LATITUDE));

                Log.v(TAG, String.format("Call location coordinates Lat: %s, Lng: %s", dlat, dlong));

                phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(CallDbAdapter.KEY_PHONE_NUMBER));

                location = new LatLng(dlat, dlong);

                db.close();
            } catch (IllegalArgumentException e) {
            	Log.e(TAG, e.getMessage());
            }
        }
	}

}
