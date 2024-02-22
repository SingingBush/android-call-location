package me.samael.android.calllocation;

import java.util.Timer; 
import java.util.TimerTask; 

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener; 
import android.location.LocationManager; 
import android.os.Bundle; 
import androidx.core.app.ActivityCompat;

/**
 * 
 * @author Fedor
 * see http://stackoverflow.com/questions/3145089/what-is-the-simplest-and-most-robust-way-to-get-the-users-current-location-in-a/3145655#3145655
 *
 */
public class RecentLocation {
	Timer timer1; 
	LocationManager lm; 
	LocationResult locationResult; 
	boolean gps_enabled=false; 
	boolean network_enabled=false; 

	public boolean getLocation(Context ctx, LocationResult result) {
		//I use LocationResult callback class to pass location value from MyLocation to user code. 
		locationResult=result; 
		if(lm==null) 
			lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

//		//exceptions will be thrown if provider is not permitted.
//		try{gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
//		try{network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}
//
//		//don't start listeners if no provider is enabled
//		if(!gps_enabled && !network_enabled)
//			return false;
//
//		if(gps_enabled)
//			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
//		if(network_enabled)
//			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);

		if(ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
			}
		} else if(ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
			}
		} else {
			return false;
		}

		timer1=new Timer(); 
		timer1.schedule(new GetLastLocation(), 20000); 
		return true; 
	} 

	LocationListener locationListenerGps = new LocationListener() { 
		public void onLocationChanged(Location location) { 
			timer1.cancel(); 
			locationResult.onLocationUpdated(location); 
			lm.removeUpdates(this); 
			lm.removeUpdates(locationListenerNetwork); 
		} 
		public void onProviderDisabled(String provider) {} 
		public void onProviderEnabled(String provider) {} 
		public void onStatusChanged(String provider, int status, Bundle extras) {} 
	}; 

	LocationListener locationListenerNetwork = new LocationListener() { 
		public void onLocationChanged(Location location) { 
			timer1.cancel(); 
			locationResult.onLocationUpdated(location); 
			lm.removeUpdates(this); 
			lm.removeUpdates(locationListenerGps); 
		} 
		public void onProviderDisabled(String provider) {} 
		public void onProviderEnabled(String provider) {} 
		public void onStatusChanged(String provider, int status, Bundle extras) {} 
	}; 

	class GetLastLocation extends TimerTask { 
		@Override 
		public void run() { 
			lm.removeUpdates(locationListenerGps); 
			lm.removeUpdates(locationListenerNetwork); 

//			Location net_loc=null, gps_loc=null;
//
//			if(gps_enabled)
//				gps_loc=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//			if(network_enabled)
//				net_loc=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//
//			//if there are both values use the latest one
//			if(gps_loc!=null && net_loc!=null){
//				if(gps_loc.getTime()>net_loc.getTime())
//					locationResult.onLocationUpdated(gps_loc);
//				else
//					locationResult.onLocationUpdated(net_loc);
//				return;
//			}
//
//			if(gps_loc!=null){
//				locationResult.onLocationUpdated(gps_loc);
//				return;
//			}
//			if(net_loc!=null){
//				locationResult.onLocationUpdated(net_loc);
//				return;
//			}
//			locationResult.onLocationUpdated(null);

			Location loc = null;

			try {
				if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				} else if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				}
			} catch (final SecurityException e) {
				return;
			}

			locationResult.onLocationUpdated(loc);
		} 
	} 

	public static abstract class LocationResult {
		public abstract void onLocationUpdated(Location location); 
	} 

}
