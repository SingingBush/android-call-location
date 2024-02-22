package me.samael.android.calllocation;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import me.samael.android.calllocation.RecentLocation.LocationResult;
import me.samael.android.calllocation.data.CallDbAdapter;
import me.samael.android.calllocation.data.SharedPrefs;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class CallLocationService extends Service {
	
	private static final String TAG = CallLocationService.class.getSimpleName();
	private static final String CHANNEL_ID = "3984275";
	private static final int LOCATION_REQUEST_CODE = 45367452;
	private static final boolean DEBUG_MODE = true;
	
	private static LocationManager locationManager = null;
	
	SharedPrefs settings;
	
	// Binder given to clients
    private final IBinder serviceBinder = new LocalBinder();
	
	private boolean gps_enabled = false;
	private boolean network_enabled = false;
	
	InnerLocationListener locationListener;
	
	RecentLocation lastKnownLocation;
	
	TelephonyManager telephonyManager;
	MyPhoneStateListener phoneStateListener;
	private NotificationManager notificationManager;
	private Location location;
	
	private CallDbAdapter dbAdapter;
	
	private static AtomicBoolean serviceActive = new AtomicBoolean(false);
	
	public static boolean isActive() {
		return serviceActive.get();
	}
	
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
    
//    private synchronized void setState(int state) {
//        if(DEBUG_MODE) Log.d(TAG, "setState() " + this.state + " -> " + state);
//        this.state = state;
//    }
//    
//    public synchronized int getState() {
//        return state;
//    }

    @Override
    public IBinder onBind(Intent intent) {
    	if(DEBUG_MODE) {
			Log.d(TAG, "[SERVICE] onBind");
		}
        return serviceBinder;
    }
    
    @Override
	public boolean onUnbind(Intent intent) {
    	return serviceBinder.isBinderAlive();
    }
	
	@Override
	public void onCreate() {
		if(DEBUG_MODE) Log.d(TAG, "onCreate");
		
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		showNotification();
		
		lastKnownLocation = new RecentLocation();
		lastKnownLocation.getLocation(this, locationResult);
		
		dbAdapter = new CallDbAdapter(this);
		if(DEBUG_MODE) Log.v(TAG, "opening database connection");
		dbAdapter.open();
		
		if(DEBUG_MODE) Log.d(TAG, "getting location service...");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if(DEBUG_MODE) Log.d(TAG, "Location service: " + locationManager.toString());
		
		if(DEBUG_MODE) Log.d(TAG, "getting best provider...");
		Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);     
        if(DEBUG_MODE) Log.d(TAG, "Best Provider: " + provider);
        
        locationListener = new InnerLocationListener();
        if(DEBUG_MODE) Log.d(TAG, "Inner Location Listener instantiated");    
		
        settings = SharedPrefs.getCallLocationPrefs(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, settings.getGpsTimeInterval(), settings.getGpsDistance(), locationListener);

			//ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 111);
        	// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
        locationManager.requestLocationUpdates(provider, settings.getGpsTimeInterval(), settings.getGpsDistance(), locationListener);
		
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
        
        if(DEBUG_MODE) Log.d(TAG, "getting last known location...");
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
        	if(DEBUG_MODE) Log.d(TAG, "Location:\n latitude  = " + location.getLatitude() + "\n longitude = " + location.getLatitude());
        } else {
        	if(DEBUG_MODE) Log.d(TAG, "Location is null - generating new location");
        	location = new Location(provider);
        }
        
        if(DEBUG_MODE) Log.d(TAG, "Instantiating phone state listener...");
        phoneStateListener = new MyPhoneStateListener();
        if(DEBUG_MODE) Log.d(TAG, "getting telephony service...");
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if(DEBUG_MODE) Log.d(TAG, "listen for phone state...");
        telephonyManager.listen(phoneStateListener, MyPhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public void onDestroy() {
		if(DEBUG_MODE) Log.d(TAG, "onDestroy");
		notificationManager.cancel(R.string.app_name); // this removes icon from notification area
		
		if(DEBUG_MODE) Log.v(TAG, "Stop listening for location updates");
		locationManager.removeUpdates(locationListener);
		
		if(DEBUG_MODE) Log.v(TAG, "Stop listening to phone state");
		telephonyManager.listen(phoneStateListener, MyPhoneStateListener.LISTEN_NONE);
		
		serviceActive.set(false);
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		if(DEBUG_MODE) Log.d(TAG, "onStart");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(DEBUG_MODE) Log.d(TAG, "onStartCommand");
		serviceActive.set(true);
		
		return Service.START_STICKY;
	}
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		if(DEBUG_MODE) logLocation(location);
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
			if (location != null) {
				setLocation(location);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(), provider + " Disabled",Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(), provider + " Enabled",Toast.LENGTH_SHORT).show();	
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.v(TAG, "Status changed : " + extras.toString());		
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
					Log.d("PhoneStateListener", "Phone Call from: " + incomingNumber);
					addCallToDatabase(incomingNumber, getCurrentLocation());
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

		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
        	return new Location(provider); // todo: fix  this
		}
        Location location = locationManager.getLastKnownLocation(provider);
        return (location != null) ? location : new Location(provider);
	}
	
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
		final Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
				.setSmallIcon(R.drawable.icon_launcher)
				.setTicker(getText(R.string.app_name))
				.setSubText(getText(R.string.service_subtext))
				.setWhen(System.currentTimeMillis())
				.build();

        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        
        //Context context = getApplicationContext();       
//        Intent mainActivityIntent = new Intent(this, MainActivity.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0);

        // todo: fix this:
        //notification.setLatestEventInfo(this, appName, notificationSubText, contentIntent);
        
        notificationManager.notify(R.string.app_name, notification);
    }
}
