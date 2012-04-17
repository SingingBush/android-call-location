package me.samael.android.calllocation;

import me.samael.android.calllocation.data.SharedPrefs;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	private static final String TAG = SettingsActivity.class.getSimpleName();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.settings);
        getPreferenceManager().setSharedPreferencesName(SharedPrefs.PREFS_NAME);
        addPreferencesFromResource(R.xml.prefs);
	}

}
