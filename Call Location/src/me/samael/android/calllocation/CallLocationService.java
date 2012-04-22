package me.samael.android.calllocation;

import java.util.Date;

import me.samael.android.calllocation.data.CallDbAdapter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallLocationService extends Service implements LocationListener {
	
	private static final String TAG = CallLocationService.class.getSimpleName();
	
	// Binder given to clients
    private final IBinder mBinder = new LocalBinder();
	
	private static final int ONE_SECOND = 1000;
	private static final long MIN_TIME = 35000L; // 0 is lowest value can use
	private static final float DISTANCE_IN_METERS = 1.0f; // 0 is lowest value can use
	
	LocationManager locationManager;
	TelephonyManager telephonyManager;
	
	Location location;
	
	private CallDbAdapter dbAdapter;
	
	/**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        CallLocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CallLocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		//location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // probably inaccurate but need to initialise
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        locationManager.requestLocationUpdates(
        		LocationManager.GPS_PROVIDER, MIN_TIME, DISTANCE_IN_METERS, this); 
		
        // register receiver here
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(
        		new MyPhoneStateListener(), MyPhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		
		locationManager.removeUpdates(this);
		
		// unregister receiver here:
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		//Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");
	}
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	void logLocation(Location location) {
    	Log.d(TAG, "location changed via " + location.getProvider() + ".\nTime: "
				+ new Date(location.getTime()).toString() 
				+ "\nLatitude: " + location.getLatitude()
				+ "\nLongitude: " + location.getLongitude());
    }
	
	private void addCallToDatabase(String phonenumber, Location location) {
		dbAdapter.addCall(phonenumber, location.getLatitude(), location.getLongitude()); // problem here
	}

	//@Override
	public void onLocationChanged(Location location) {
		logLocation(location);	
		setLocation(location);
	}

	//@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	class MyPhoneStateListener extends PhoneStateListener {
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);

			switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					//phone in idle state
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					//phone in offhook state
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					//phone is ringing
					Log.d("CallLocationService PhoneStateListener", "Phone Call from: " + incomingNumber);
					addCallToDatabase(incomingNumber, getLocation());
					break;
				default:
					break;
			}
		}
	}
}
