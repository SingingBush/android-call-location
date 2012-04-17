package me.samael.android.calllocation;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class MainActivity extends TabActivity {
	private static final String TAG = MainActivity.class.getSimpleName();
	
	//private static
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Amputa Bangiz.ttf");
        TextView tv = (TextView) findViewById(R.id.HelloText);
        tv.setTypeface(tf);
        
        TabHost tabHost = getTabHost();
        
        // Tab for Call History
        TabSpec callHistorySpec = tabHost.newTabSpec("Call History");
        // setting Title and Icon for the Tab
        callHistorySpec.setIndicator("Call History", getResources().getDrawable(android.R.drawable.ic_menu_recent_history));
        Intent callHistoryIntent = new Intent(this, CallHistoryActivity.class);
        callHistorySpec.setContent(callHistoryIntent);
        
        // Tab for Call Map
        TabSpec callMapSpec = tabHost.newTabSpec("Call Map");
        // setting Title and Icon for the Tab
        callMapSpec.setIndicator("Call Map", getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
        Intent callMapIntent = new Intent(this, CallMapActivity.class);
        callMapSpec.setContent(callMapIntent);
        
        // Tab for Call Map
        TabSpec settingsSpec = tabHost.newTabSpec("Settings");
        // setting Title and Icon for the Tab
        settingsSpec.setIndicator("Settings", getResources().getDrawable(android.R.drawable.ic_menu_preferences));
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        settingsSpec.setContent(settingsIntent);
        
        // Adding all TabSpec to TabHost
        tabHost.addTab(callHistorySpec);
        tabHost.addTab(callMapSpec);
        tabHost.addTab(settingsSpec);
    }
}