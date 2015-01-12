package me.samael.android.calllocation.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {
	
	private static final String TAG = DataBaseHelper.class.getSimpleName();
	
	private static final String DATABASE_NAME = "calllocation.db";
	private static final int DATABASE_VERSION = 1;
	
	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override // SQL needed to create database on fresh installation
    public void onCreate(SQLiteDatabase db) {
        // create the "run" table
        final String sql = String.format("create table %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, phone TEXT NOT NULL, longitude REAL, latitude REAL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)", CallDbAdapter.TABLE_NAME, CallDbAdapter.KEY_ID);

        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            Log.e(TAG, "Error creating database table: ", e);
        }
    }

    @Override // SQL needed to update existing database if schema changed (if db gets to v3, then should consider oldVersion)
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, String.format("Upgrading database from %d to %d. All data will be destroyed", oldVersion, newVersion));
        // no needed as still v1
        // could either alter tables or drop existing
    }

}
