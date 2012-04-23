package me.samael.android.calllocation;

import me.samael.android.calllocation.CallLocationService.LocalBinder;
import android.app.Activity;
import android.util.Log;
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
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

public class TempTestActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "TempTestActivity";
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
		setContentView(R.layout.temptestlayout);
		
		buttonStart = (Button) findViewById(R.id.buttonStart);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		
		buttonStart.setOnClickListener(this);
		buttonStop.setOnClickListener(this);
		
		tempfeedback = (TextView) findViewById(R.id.tempfeedback);
		
		callLocationServiceIntent = new Intent(this, CallLocationService.class);
	}

	//@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.buttonStart:
			if (!serviceBound) {
				Log.d(TAG, "onClick: starting service");
				tempfeedback.setText("starting service");
				bindService(callLocationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
			} else {
				Location loc = callLocationService.getLocation();
				tempfeedback.setText(loc.getLatitude() + " " + loc.getLongitude());
			}
			//Log.d(TAG, "onClick: starting service");
			//tempfeedback.setText("starting service");
			//startService(service);
			//service.addFlags(1);
			//bindService(service, mConnection, Context.BIND_AUTO_CREATE);
			break;
		case R.id.buttonStop:
			// Unbind from the service
	        if (serviceBound) {
	        	Log.d(TAG, "onClick: stopping service");
				tempfeedback.setText("stopping service");
	            unbindService(serviceConnection);
	            serviceBound = false;
	        }
			//Log.d(TAG, "onClick: stopping service");
			//tempfeedback.setText("stopping service");
			//stopService(service);
			break;
		}
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
