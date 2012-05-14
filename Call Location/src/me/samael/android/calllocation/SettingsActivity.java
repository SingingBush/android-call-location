package me.samael.android.calllocation;

import me.samael.android.calllocation.data.SharedPrefs;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {
	private static final String TAG = SettingsActivity.class.getSimpleName();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate for " + TAG);
        
        getPreferenceManager().setSharedPreferencesName(SharedPrefs.SHARED_PREF_FILENAME);
        
        addPreferencesFromResource(R.xml.prefs);
	}

}
