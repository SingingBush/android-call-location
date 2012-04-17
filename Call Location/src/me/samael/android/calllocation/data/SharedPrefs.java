package me.samael.android.calllocation.data;

import me.samael.android.calllocation.R;
import me.samael.android.calllocation.R.string;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPrefs {
	public final static String PREFS_NAME = "call_location_prefs";

    public static boolean getBackgroundUpdateFlag(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(context.getString(R.string.pref_key), false);
    }

    public static void setBackgroundUpdateFlag(Context context, boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(context.getString(R.string.pref_key), newValue);
        prefsEditor.commit();
    }
}
