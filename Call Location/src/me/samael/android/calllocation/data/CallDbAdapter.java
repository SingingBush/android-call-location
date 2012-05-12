package me.samael.android.calllocation.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CallDbAdapter {
	
	private static final String TAG = CallDbAdapter.class.getSimpleName();
	
	public static final String KEY_ID = "_id";
	public static final String KEY_PHONE_NUMBER = "phone";
	public static final String KEY_LONGITUTE = "longitude";
	public static final String KEY_LATITUDE = "latitude";
	
	private static final String DATABASE_NAME = "calllocation";
	private static final int DATABASE_VERSION = 2;
	
	private static final String TABLE_NAME = "callhistory";
	
	// Table Creation SQL statement:
	protected static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
			+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ KEY_PHONE_NUMBER + " TEXT NOT NULL, "
			+ KEY_LONGITUTE + " INTEGER, "
			+ KEY_LATITUDE + " INTEGER);";
	
	private DataBaseHelper databaseHelper;
	private SQLiteDatabase database;
	private final Context context;
	
	public CallDbAdapter(Context context) {
		this.context = context;
	}

	public static String getTableName() {
		return TABLE_NAME;
	}
	
	public static String getDatabaseName() {
		return DATABASE_NAME;
	}
	
	public static int getDatabaseVersion() {
		return DATABASE_VERSION;
	}
	
	public CallDbAdapter open() throws SQLException {
		databaseHelper = new DataBaseHelper(context);
		database = databaseHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		databaseHelper.close();
	}
	
	public boolean addCall(String phonenumber, double latitude, double longitude) {
		Log.d(TAG, "Attempting to add new entry to " + TABLE_NAME + ".\nPhone Number: " 
				+ phonenumber + "\nLocation: " + String.valueOf(latitude) + " by " + String.valueOf(longitude));
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_PHONE_NUMBER, phonenumber);
		initialValues.put(KEY_LATITUDE, latitude);
		initialValues.put(KEY_LONGITUTE, longitude);
		try {
			database.insertOrThrow(TABLE_NAME, null, initialValues);
			Log.v(TAG, "Call added to database");
			return true;
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
	}
	
	public Cursor selectAllFromCallHistory() {
		Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null);
		if(cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public Cursor selectAllFromCallHistoryWhereIdEquals(Long id) {
		String sqlQuery = KEY_ID + "=" + id;
		Cursor cursor = database.query(TABLE_NAME, null, sqlQuery, null, null, null, null);
		
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
}
