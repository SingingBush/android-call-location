package me.samael.android.calllocation;

import android.app.Activity;
import android.os.Bundle;

public class CallHistoryActivity extends Activity {
	private static final String TAG = CallHistoryActivity.class.getSimpleName();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callhistory);
	}

}
