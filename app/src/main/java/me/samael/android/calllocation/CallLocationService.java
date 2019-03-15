package me.samael.android.calllocation;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import me.samael.android.calllocation.RecentLocation.LocationResult;
import me.samael.android.calllocation.data.CallDbAdapter;
import me.samael.android.calllocation.data.SharedPrefs;

import android.Manifest;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class CallLocationService extends Service {

    private static final String TAG = CallLocationService.class.getSimpleName();
    private static final String CHANNEL_ID = "3984275";
    private static final int LOCATION_REQUEST_CODE = 45367452;

    private static LocationManager locationManager = null;

    SharedPrefs settings;

    // Binder given to clients
    private final IBinder serviceBinder = new LocalBinder();

    private FusedLocationProviderClient _fusedLocationClient;

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
    class LocalBinder extends Binder {
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
        Log.d(TAG, "[SERVICE] onBind");
        return serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return serviceBinder.isBinderAlive();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //_fusedLocationClient.requestLocationUpdates();
        //_fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

//        startForeground(234243, (new Notification.Builder(getApplicationContext()).build())); // potential hack? see: https://hackernoon.com/android-location-tracking-with-a-service-80940218f561

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
//        String provider = locationManager.getBestProvider(criteria, true);
//        Log.d(TAG, "Best Provider: " + provider);

        locationListener = new InnerLocationListener();
        Log.d(TAG, "Inner Location Listener instantiated");

        settings = SharedPrefs.getCallLocationPrefs(this);

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, settings.getGpsTimeInterval(), settings.getGpsDistance(), locationListener);
            
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //location.set(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            }
        } else {
            // trigger a request for Location Permission...
//            ActivityCompat.requestPermissions(, new String[] {
//                    android.Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//            }, LOCATION_REQUEST_CODE);
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, settings.getGpsTimeInterval(), settings.getGpsDistance(), locationListener);

            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                //location.set(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
            }
        }

        if (location != null) {
            Log.d(TAG, "Location:\n latitude  = " + location.getLatitude() + "\n longitude = " + location.getLatitude());
        } else {
            Log.w(TAG, "Location is null");
        }

        Log.d(TAG, "Instantiating phone state listener...");
        phoneStateListener = new MyPhoneStateListener();
        
        Log.d(TAG, "getting telephony service...");
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        
        if(telephonyManager != null) {
            Log.d(TAG, "listen for phone state...");

            telephonyManager.listen(phoneStateListener, MyPhoneStateListener.LISTEN_CALL_STATE);
        } else {
            Log.w(TAG, "TelephonyManager was null");
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        notificationManager.cancel(R.string.app_name); // this removes icon from notification area

        Log.v(TAG, "Stop listening for location updates");
        locationManager.removeUpdates(locationListener);

        Log.v(TAG, "Stop listening to phone state");
        telephonyManager.listen(phoneStateListener, MyPhoneStateListener.LISTEN_NONE);

        serviceActive.set(false);
    }

//    @Override
//    public void onStart(Intent intent, int startid) {
//        Log.d(TAG, "onStart");
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        serviceActive.set(true);

        return Service.START_STICKY; // or START_NOT_STICKY ?
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        logLocation(location);
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
                + "\nlatitude " + location.getLatitude() + "\nlongitude " + location.getLongitude());
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
            Toast.makeText(getApplicationContext(), provider + " Disabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), provider + " Enabled", Toast.LENGTH_SHORT).show();
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

        if (
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
//            ActivityCompat.requestPermissions(this, new String[] {
//                    android.Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//            }, 29387456);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return new Location(provider);
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
        CharSequence appName = getText(R.string.app_name);
        CharSequence notificationSubText = getText(R.string.service_subtext);
        
        //Context context = getApplicationContext();       
        //Intent mainActivityIntent = new Intent(this, MainActivity.class);
        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID);

        //Notification notification = new Notification(R.drawable.icon_launcher, appName, System.currentTimeMillis());
        final Notification notification = builder
                .setSmallIcon(R.drawable.icon_launcher)
                .setTicker(appName)
                //.setWhen(System.currentTimeMillis())
                .build();

        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        //notification.setLatestEventInfo(this, appName, notificationSubText, contentIntent);
        
        notificationManager.notify(R.string.app_name, notification);
    }
}
