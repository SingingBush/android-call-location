package me.samael.android.calllocation.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPrefs {
	static final SharedPrefs INSTANCE = new SharedPrefs();
	private static SharedPreferences settings;
	
	public final static String SHARED_PREF_FILENAME = "calllocationPrefsFile";
	
	public final static String GPS_TIME_INTERVAL_PREF = "gps_time_interval_pref";
	public final static String GPS_DISTANCE_PREF = "gps_distance_pref";
	public final static String ZOOM_PREF = "zoom_pref";
	
	public final static String DEFAULT_GPS_TIME_INTERVAL_PREF = "5000";
	public final static String DEFAULT_GPS_DISTANCE_PREF = "5";
	public final static String DEFAULT_ZOOM_PREF = "16";
	
	public static SharedPrefs getCallLocationPrefs(Context context) {
		SharedPrefs.settings = context.getSharedPreferences(SharedPrefs.SHARED_PREF_FILENAME, Context.MODE_PRIVATE); 
		return INSTANCE;
	}
	
	private SharedPrefs() {
	}
	
	public int getMapZoomLevel() {
		return Integer.valueOf(settings.getString(SharedPrefs.ZOOM_PREF, SharedPrefs.DEFAULT_ZOOM_PREF));
	}
	
	public void setMapZoomLevel(int zoomLevel) {
		Editor prefsEditor = settings.edit();
		prefsEditor.putInt(SharedPrefs.ZOOM_PREF, zoomLevel);
		prefsEditor.commit();
	}
	
	public long getGpsTimeInterval() {
		return Long.valueOf(settings.getString(SharedPrefs.GPS_TIME_INTERVAL_PREF, SharedPrefs.DEFAULT_GPS_TIME_INTERVAL_PREF));
	}
	
	public void setGpsTimeInterval(long milliseconds) {
		Editor prefsEditor = settings.edit();
		prefsEditor.putLong(SharedPrefs.GPS_TIME_INTERVAL_PREF, milliseconds);
		prefsEditor.commit();
	}
	
	public float getGpsDistance() {
		return Float.valueOf(settings.getString(SharedPrefs.GPS_DISTANCE_PREF, SharedPrefs.DEFAULT_GPS_DISTANCE_PREF));
	}
}
