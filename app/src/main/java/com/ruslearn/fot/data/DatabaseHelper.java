package com.ruslearn.fot.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Local app database (ADR-0002): everything student-owned lives only on the device.
 * Created with the users table in M2; other student tables are added in later
 * milestones.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RUSLearn.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USERS_EMAIL = "email";
    public static final String COLUMN_USERS_NAME = "name";
    public static final String COLUMN_USERS_LAST_LOGIN = "last_login";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USERS_EMAIL + " TEXT PRIMARY KEY, "
                + COLUMN_USERS_NAME + " TEXT, "
                + COLUMN_USERS_LAST_LOGIN + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Saves the signed-in student's profile; the first insert performs registration
     * (FR01), later sign-ins just refresh it.
     */
    public void saveUser(String email, String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERS_EMAIL, email);
        values.put(COLUMN_USERS_NAME, name);
        values.put(COLUMN_USERS_LAST_LOGIN, System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
}
