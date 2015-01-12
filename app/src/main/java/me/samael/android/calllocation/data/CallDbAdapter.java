package me.samael.android.calllocation.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

public class CallDbAdapter { // DAO
	
	private static final String TAG = CallDbAdapter.class.getSimpleName();

	public static final String KEY_ID = "_id";
	public static final String KEY_PHONE_NUMBER = "phone";
	public static final String KEY_LONGITUTE = "longitude";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_DATETIME = "timestamp";

    public static final String TABLE_NAME = "callhistory";
	
	private DataBaseHelper _db;
	private final Context context;
	
	public CallDbAdapter(Context context) {
		this.context = context;
	}
	
	public CallDbAdapter open() throws SQLException {
		_db = new DataBaseHelper(context);
		return this;
	}
	
	public void close() {
		_db.close();
	}
	
	public boolean addCall(String phonenumber, double latitude, double longitude) {
		Log.d(TAG, String.format("Attempting to add new entry to %s.\nPhone Number: %s\nLocation: %s by %s", TABLE_NAME, phonenumber, String.valueOf(latitude), String.valueOf(longitude)));

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_PHONE_NUMBER, phonenumber);
		initialValues.put(KEY_LATITUDE, latitude);
		initialValues.put(KEY_LONGITUTE, longitude);
		try {
            _db.getWritableDatabase().insertOrThrow(TABLE_NAME, null, initialValues);
			Log.v(TAG, "Call added to database");
			return true;
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
	}
	
	public Cursor selectAllFromCallHistory() {
		Cursor cursor = _db.getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, KEY_DATETIME);
		if(cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public Cursor selectAllFromCallHistoryWhereIdEquals(Long id) {
		Cursor cursor = _db.getReadableDatabase().query(TABLE_NAME, null, "_id = ?", new String[]{String.valueOf(id)}, null, null, null);
		
		if (cursor.moveToNext()) {
			try {
				cursor.moveToFirst();
				return cursor;
			} catch (SQLException e) {
				Log.e(TAG, e.getMessage());
				return null;
			}
		} else
			return null;
	}
	
	public boolean deleteFromCallHistoryWhereIdEquals(long id) {
		try {
			return _db.getWritableDatabase().delete(TABLE_NAME, "_id = ?", new String[]{String.valueOf(id)}) > 0; // delete returns no of rows affected
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
			return false;
		}		
	}
}
