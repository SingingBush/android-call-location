package me.samael.android.calllocation;

import me.samael.android.calllocation.data.CallDbAdapter;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class CallHistoryActivity extends ListActivity {
	private static final String TAG = CallHistoryActivity.class.getSimpleName();
	
	private CallDbAdapter callDbAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callhistory);
        
        callDbAdapter = new CallDbAdapter(this);
	}
	
	@Override
    protected void onStart() {
		super.onStart();
		Log.v(TAG, "onStart");
        
        try {
        	fillListView();
        } catch (Exception e) {
			Log.v(TAG, "Exception: " + e.getMessage());
		}
	}
	
	@Override
    protected void onStop() {
		Log.v(TAG, "onStop");
		super.onStop();
	}
	
	@Override
    public void onDestroy() {
    	Log.v(TAG, "exiting " + TAG);
    	super.onDestroy();
    }
	
	@Override
    protected void onListItemClick(ListView listview, View view, int position, long rowId) {
        super.onListItemClick(listview, view, position, rowId);
        Log.v(TAG, "view call at position '" + position + "' with rowId: " + rowId);
        
        viewCallOnMap(rowId);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillListView();
    }
	
	private void fillListView() {
		Log.v(TAG, "fillListView");
		
        Log.v(TAG, "opening database connection");
        callDbAdapter.open();
        
//        Cursor productCursor = productDbAdapter.selectAllFromProducts();
//        Log.v(TAG, "Cursor size: " + productCursor.getCount());
//		startManagingCursor(productCursor);
//		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.productrow, productCursor, FROM_PRODUCTS_TABLE, TO_PRODUCT_ROW_VIEW_IDS);
//	    setListAdapter(adapter);
		//productCursor.close(); // DO NOT CLOSE THE CURSOR HERE, IT PREVENTS THE LIST BEING POPULATED!
        
        String[] fromDatabase = { CallDbAdapter.KEY_PHONE_NUMBER };
        int[] toRowIds = { R.id.callHistoryPhoneNumber };
        
		try {
			Cursor productCursor = callDbAdapter.selectAllFromCallHistory();
			Log.v(TAG, "Cursor size: " + productCursor.getCount());
			startManagingCursor(productCursor);
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.callhistoryrow, productCursor, fromDatabase, toRowIds);
	        setListAdapter(adapter);
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
		} // DO NOT CLOSE THE CURSOR HERE, IT PREVENTS THE LIST BEING POPULATED!
        
        Log.v(TAG, "closing database connection");
        callDbAdapter.close();
	}
	
	private void viewCallOnMap(long rowId) {
		Intent viewCallLocation = new Intent(this, CallMapActivity.class);
		viewCallLocation.putExtra(CallDbAdapter.KEY_ID, rowId);
		startActivity(viewCallLocation);
	}
}
