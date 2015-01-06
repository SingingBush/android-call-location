package me.samael.android.calllocation.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {
	
	private static final String TAG = DataBaseHelper.class.getSimpleName();
	
	private static final String DATABASE_NAME = "calllocation.db";
	private static final int DATABASE_VERSION = 2;
	
	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

//	public DataBaseHelper(Context context, String name, CursorFactory factory, int version) {
//		super(context, DATABASE_NAME, null, DATABASE_VERSION);
//	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			Log.v(TAG, "Attempting to create table using SQL: " + CallDbAdapter.CREATE_TABLE);
			db.execSQL(CallDbAdapter.CREATE_TABLE);
		} catch (SQLException e) {
			Log.e(TAG, "Error creating table: " + e.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from " + oldVersion + " to " + newVersion + ". All data will be destroyed");
		
		db.execSQL("DROP TABLE IF EXISTS " + CallDbAdapter.getTableName());
	}

}
