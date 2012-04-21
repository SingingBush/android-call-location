package me.samael.android.calllocation;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class TempTestActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "TempTestActivity";
	Button buttonStart, buttonStop;
	TextView tempfeedback;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.temptestlayout);
		
		buttonStart = (Button) findViewById(R.id.buttonStart);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		
		buttonStart.setOnClickListener(this);
		buttonStop.setOnClickListener(this);
		
		tempfeedback = (TextView) findViewById(R.id.tempfeedback);
	}


	//@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.buttonStart:
			Log.d(TAG, "onClick: starting service");
			tempfeedback.setText("starting service");
			startService(new Intent(this, CallLocationService.class));
			break;
		case R.id.buttonStop:
			Log.d(TAG, "onClick: stopping service");
			tempfeedback.setText("stopping service");
			stopService(new Intent(this, CallLocationService.class));
			break;
		}
	}

}
