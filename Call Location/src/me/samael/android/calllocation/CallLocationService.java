package me.samael.android.calllocation;

import java.util.Date;

import me.samael.android.calllocation.RecentLocation.LocationResult;
import me.samael.android.calllocation.data.CallDbAdapter;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallLocationService extends Service {
	
	private static final String TAG = CallLocationService.class.getSimpleName();
	
	// Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    
    private static final long FIVE_SECONDS = 5000L;
	private static final float FIVE_METERS = 5.0f; // 0 is lowest value can use
	
	private int state;
	public static final int STATE_NONE = 0;
	public static final int STATE_LISTENING = 1;
	private boolean gps_enabled = false;
	private boolean network_enabled = false;
	
	LocationManager locationManager;
	InnerLocationListener locationListener;
	
	RecentLocation lastKnownLocation;
	
	TelephonyManager telephonyManager;
	MyPhoneStateListener phoneStateListener;
	private NotificationManager notificationManager;
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
    
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + this.state + " -> " + state);
        this.state = state;
    }
    
    public synchronized int getState() {
        return state;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		showNotification();
		
		lastKnownLocation = new RecentLocation();
		lastKnownLocation.getLocation(this, locationResult);
		
		dbAdapter = new CallDbAdapter(this);
		Log.v(TAG, "opening database connection");
		dbAdapter.open();
		
		Log.d(TAG, "getting location service...");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Log.d(TAG, "Location service: " + locationManager.toString());
		
		Log.d(TAG, "getting best provider...");
		Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);     
        Log.d(TAG, "Best Provider: " + provider);
        
        locationListener = new InnerLocationListener();
		Log.d(TAG, "Inner Location Listener instantiated");    
		
        locationManager.requestLocationUpdates(provider, FIVE_SECONDS, FIVE_METERS, locationListener);
		
        // exceptions will be thrown if provider is not permitted.
        try {
        	gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        	Log.e(TAG, "gps_enabled = " + gps_enabled + "\n Exception" + ex.getMessage());
        }
        try {
        	network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        	Log.e(TAG, "network_enabled = " + network_enabled + "\n Exception" + ex.getMessage());
        }
        
        Log.d(TAG, "getting last known location...");
        try {
        	location = locationManager.getLastKnownLocation(provider); // try get best provider
        } catch (Exception e) {
        	Log.e(TAG, "Unable to get last known location with " + provider);
        	if (gps_enabled) {
        		location.set(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)); // default to GPS
        	} else if (network_enabled) {
        		location.set(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)); // default to Network
        	}
        }
        
        if (location != null) {
        	Log.d(TAG, "Location:\n latitude  = " + location.getLatitude() + "\n longitude = " + location.getLatitude());
        } else {
        	Log.d(TAG, "Location is null - generating new location");
        	location = new Location(provider);
        }
        
        
        
        Log.d(TAG, "Instantiating phone state listener...");
        phoneStateListener = new MyPhoneStateListener();
        Log.d(TAG, "getting telephony service...");
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        Log.d(TAG, "listen for phone state...");
        telephonyManager.listen(phoneStateListener, MyPhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		notificationManager.cancel(R.string.app_name); // this removes icon from notification area
		
		Log.v(TAG, "Stop listening for location updates");
		locationManager.removeUpdates(locationListener);
		
		Log.v(TAG, "Stop listening to phone state");
		telephonyManager.listen(phoneStateListener, MyPhoneStateListener.LISTEN_NONE);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(TAG, "Call Location Service started");
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
		Log.d(TAG, "Adding entry to database:\nCall From " + phonenumber 
				+ "\nlatitude "+ location.getLatitude() + "\nlongitude " + location.getLongitude());
		dbAdapter.addCall(phonenumber, location.getLatitude(), location.getLongitude()); // todo - potential problem here if lat and long are null
	}
	
	class InnerLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			logLocation(location);	
			setLocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub			
		}
	}
	
	class MyPhoneStateListener extends PhoneStateListener {
		
		@Override
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
					addCallToDatabase(incomingNumber, getCurrentLocation()); //getLocation());
					break;
				default:
					break;
			}
		}
	}
	
	private Location getCurrentLocation() {
		Log.d(TAG, "getting best provider...");
		Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);     
        Log.d(TAG, "Best Provider: " + provider);
        
        Location loc = locationManager.getLastKnownLocation(provider);
        return (loc != null) ? loc : new Location(provider);
	}
	
	// double currentLatitude = currentLocation.getLatitude();
	// double currentLongitude = currentLocation.getLongitude();
	
	LocationResult locationResult = new LocationResult() {
		@Override
		public void onLocationUpdated(Location location) {
			//Got the location! 
		}
	};
	
	
	
	
	
	
	
	
	
	
	/**
     * Shows a notification in the status bar while service is running.
     */
    private void showNotification() {
        CharSequence appName = getText(R.string.app_name);
        CharSequence notificationSubText = getText(R.string.service_subtext);
        
        Notification notification = new Notification(R.drawable.icon_launcher, appName, System.currentTimeMillis());
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        
        //Context context = getApplicationContext();       
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0);
        notification.setLatestEventInfo(this, appName, notificationSubText, contentIntent);
        
        notificationManager.notify(R.string.app_name, notification);
    }
}
