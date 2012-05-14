package me.samael.android.calllocation;

import me.samael.android.calllocation.CallLocationService.LocalBinder;
import me.samael.android.calllocation.data.SharedPrefs;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	
	private static final String TAG = MainActivity.class.getSimpleName();
	Button buttonStart, buttonStop;
	TextView tempfeedback;
	Intent callLocationServiceIntent;
	
	CallLocationService callLocationService;
	boolean serviceBound = false;
	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/AmputaBangiz.ttf");
        TextView tv = (TextView) findViewById(R.id.application_title);
        tv.setTypeface(tf);
		
		buttonStart = (Button) findViewById(R.id.buttonStart);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		
		buttonStart.setOnClickListener(this);
		buttonStop.setOnClickListener(this);
		
		tempfeedback = (TextView) findViewById(R.id.tempfeedback);
		
		callLocationServiceIntent = new Intent(this, CallLocationService.class);
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart - " + TAG);
	}
	
	@Override
    public void onResume() {
    	super.onResume();
    	Log.d(TAG, "onResume - " + TAG);
    	
    	Log.d(TAG, " *** make sure relevant button is clickable...");
    	buttonStart.setClickable(!CallLocationService.isActive());
    	buttonStop.setClickable(CallLocationService.isActive());
    	
    	Log.d(TAG, "getting preferences...");
    	// if preference activity is started whilst on Main Screen, onResume is called so updated settings should be applied if applicable
    	SharedPrefs settings = SharedPrefs.getCallLocationPrefs(this);  
		Log.v(TAG, " *** zoom preference is: " + settings.getMapZoomLevel());
		Log.v(TAG, " *** time interval preference is: " + settings.getGpsTimeInterval());
		Log.v(TAG, " *** distance preference is: " + settings.getGpsDistance());
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");	
	}
    
    @Override
    protected void onStop() {
    	Log.d(TAG, "onStop");	
        super.onStop();        
    }
        
    @Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	//@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.buttonStart:
			if (!serviceBound) {
				Log.d(TAG, "onClick: starting service");
				tempfeedback.setText("starting service");
				startService(callLocationServiceIntent);
				bindService(callLocationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE); // todo - problem here if exit app with service running
			} else {
				Location loc = callLocationService.getLocation();
				tempfeedback.setText(loc.getLatitude() + " " + loc.getLongitude());
			}
			buttonStart.setClickable(false);
			buttonStop.setClickable(true);
			break;
		case R.id.buttonStop:
			// Unbind from the service
	        if (serviceBound) {
	        	Log.d(TAG, "onClick: stopping service");
				tempfeedback.setText("stopping service");
	            unbindService(serviceConnection);
	            serviceBound = false;
	        }
	        buttonStart.setClickable(true);
	    	buttonStop.setClickable(false);
			stopService(callLocationServiceIntent);
			break;
		}
	}
	
	@Override /** Called when menu instantiated */
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.mainactivity_optionsmenu, menu);
    	return true;
    }

    @Override /** Called when menu accessed */
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.menu_Callhistory:
    		startActivity(new Intent(this, CallHistoryActivity.class));
    		return true;
    	case R.id.menu_Newsfeed:
    		startActivity(new Intent(this, NewsFeedActivity.class));
    		return true;
    	case R.id.menu_Preferences:
    		startActivity(new Intent(this, SettingsActivity.class));
    		return true;
    	case R.id.menu_About:
    		startActivity(new Intent(this, AboutActivity.class));
    		return true;
    	}
    	return false;
    }
    
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            callLocationService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };
    
    // The BroadcastReceiver that listens for updates from the
    // CallLocationService
    private final BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			Log.d(TAG, "receiver has speed: " + callLocationService.getLocation().getSpeed());
			
		}
    };
}
