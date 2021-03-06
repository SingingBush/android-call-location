package me.samael.android.calllocation;

import me.samael.android.calllocation.data.CallDbAdapter;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class CallHistoryActivity extends ListActivity {

	private static final String TAG = CallHistoryActivity.class.getSimpleName();

	private CallDbAdapter callDbAdapter;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callhistory);
        
        registerForContextMenu(getListView());

        callDbAdapter = new CallDbAdapter(this);
        callDbAdapter.open();

        String[] fromDatabase = { CallDbAdapter.KEY_PHONE_NUMBER, CallDbAdapter.KEY_DATETIME };
        int[] toRowIds = { R.id.callHistoryPhoneNumber, R.id.callHistoryDateTime };

        try {
            Cursor cursor = callDbAdapter.selectAllFromCallHistory();
            Log.v(TAG, "Cursor size: " + cursor.getCount());
            startManagingCursor(cursor); // todo: use the new android.content.CursorLoader
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.callhistoryrow, cursor, fromDatabase, toRowIds);
            setListAdapter(adapter);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		
		if (view == getListView()) {
			getMenuInflater().inflate(R.menu.callhistory_contextmenu, menu);
			
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			
			long id = getListAdapter().getItemId(info.position);
			
			menu.setHeaderTitle("Call " + id);
			menu.setHeaderIcon(R.drawable.phone_icon);		
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		long rowId = info.id;
		
		switch (item.getItemId()) {
		case R.id.contextmenu_returncall:
			startCallActionForGivenRow(rowId);
			return true;
		case R.id.contextmenu_delete:
			confirmDeletionOfRow(rowId);
			return true;
		}
		return false;		
	}

    @Override
    protected void onResume() {
        callDbAdapter.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        callDbAdapter.close();
        super.onPause();
    }
	
	@Override
    protected void onListItemClick(ListView listview, View view, int position, long rowId) {
        super.onListItemClick(listview, view, position, rowId);
        Log.v(TAG, "view call at position '" + position + "' with rowId: " + rowId);
        
        viewCallOnMap(rowId);
	}
	
	@Override // gets called when selecting option from context menu
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        final SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
        adapter.notifyDataSetChanged();
    }
	
	private void viewCallOnMap(long rowId) {
		Intent viewCallLocation = new Intent(this, CallMapActivity.class);
		viewCallLocation.putExtra(CallDbAdapter.KEY_ID, rowId);
		startActivity(viewCallLocation);
	}
	
	private void startCallActionForGivenRow(final long rowId) {

		Cursor cursor = callDbAdapter.selectAllFromCallHistoryWhereIdEquals(rowId);
		String phoneNumber = cursor.getString(cursor.getColumnIndex(CallDbAdapter.KEY_PHONE_NUMBER));

        if (phoneNumber.length() > 0) {
        	try {
    			Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
    			callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			startActivity(callIntent);
    		} catch (ActivityNotFoundException e) {
    	        Log.e(TAG, "Unable to make call: " + e.getMessage());
    	    }
        } else
        	Log.e(TAG, "Unable to phone: " + phoneNumber);
	}
	
	private void confirmDeletionOfRow(final long rowId) {
		if (rowId > 0) {
			new AlertDialog.Builder(this).setTitle(R.string.callhistory_contextmenu_confirm_deletion)
			.setPositiveButton("ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleteRowFromDatabase(rowId);					
				}
			}).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// do nothing				
				}
			}).show();
		}
	}
	
	private void deleteRowFromDatabase(final long rowId) {

		Log.v(TAG, String.format("delete row %d from database", rowId));

        if (callDbAdapter.deleteFromCallHistoryWhereIdEquals(rowId)) {
            final SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();

            try {
                Cursor cursor = callDbAdapter.selectAllFromCallHistory();
                adapter.swapCursor(cursor);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to get call history", e);
            }

        	Log.v(TAG, "Table row deleted: " + rowId);
        } else {
            Log.e(TAG, "Unable to delete row: " + rowId);
        }
	}
}
