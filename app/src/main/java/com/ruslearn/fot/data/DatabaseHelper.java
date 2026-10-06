package com.ruslearn.fot.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ruslearn.fot.model.Paper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Local app database (ADR-0002): everything student-owned lives only on the device.
 * Tables:
 *   users          — Google profiles that signed in on this device (FR01)
 *   favourites     — saved papers, keyed by Drive file id (FR06)
 *   offline_papers — downloaded PDF metadata, keyed by Drive file id (FR14)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RUSLearn.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USERS_EMAIL = "email";
    public static final String COLUMN_USERS_NAME = "name";
    public static final String COLUMN_USERS_LAST_LOGIN = "last_login";

    public static final String TABLE_FAVOURITES = "favourites";
    public static final String COLUMN_KEY_DRIVE_ID = "drive_id";
    public static final String COLUMN_PAPER_JSON = "paper_json";
    public static final String COLUMN_ADDED_AT = "added_at";

    public static final String TABLE_OFFLINE = "offline_papers";
    public static final String COLUMN_FILE_PATH = "file_path";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USERS_EMAIL + " TEXT PRIMARY KEY, "
                + COLUMN_USERS_NAME + " TEXT, "
                + COLUMN_USERS_LAST_LOGIN + " INTEGER)");
        db.execSQL("CREATE TABLE " + TABLE_FAVOURITES + " ("
                + COLUMN_KEY_DRIVE_ID + " TEXT PRIMARY KEY, "
                + COLUMN_PAPER_JSON + " TEXT NOT NULL, "
                + COLUMN_ADDED_AT + " INTEGER)");
        db.execSQL("CREATE TABLE " + TABLE_OFFLINE + " ("
                + COLUMN_KEY_DRIVE_ID + " TEXT PRIMARY KEY, "
                + COLUMN_PAPER_JSON + " TEXT NOT NULL, "
                + COLUMN_FILE_PATH + " TEXT NOT NULL, "
                + COLUMN_ADDED_AT + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FAVOURITES + " ("
                    + COLUMN_KEY_DRIVE_ID + " TEXT PRIMARY KEY, "
                    + COLUMN_PAPER_JSON + " TEXT NOT NULL, "
                    + COLUMN_ADDED_AT + " INTEGER)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_OFFLINE + " ("
                    + COLUMN_KEY_DRIVE_ID + " TEXT PRIMARY KEY, "
                    + COLUMN_PAPER_JSON + " TEXT NOT NULL, "
                    + COLUMN_FILE_PATH + " TEXT NOT NULL, "
                    + COLUMN_ADDED_AT + " INTEGER)");
        }
    }

    // ---------- Users (FR01) ----------

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

    // ---------- Favourites (FR06) ----------

    public boolean addFavourite(Paper paper) {
        return upsert(TABLE_FAVOURITES, paper, null);
    }

    public boolean removeFavourite(String driveId) {
        return deleteRow(TABLE_FAVOURITES, driveId);
    }

    public boolean isFavourite(String driveId) {
        return rowExists(TABLE_FAVOURITES, driveId);
    }

    /** Favourite papers, newest first. */
    public List<Paper> getFavourites() {
        return readPapers(TABLE_FAVOURITES, COLUMN_ADDED_AT + " DESC");
    }

    // ---------- Offline papers (FR14) ----------

    public boolean addOfflinePaper(Paper paper, String filePath) {
        return upsert(TABLE_OFFLINE, paper, filePath);
    }

    public boolean removeOfflinePaper(String driveId) {
        return deleteRow(TABLE_OFFLINE, driveId);
    }

    public boolean isOffline(String driveId) {
        return rowExists(TABLE_OFFLINE, driveId);
    }

    /** Offline-paper metadata rows, newest first. */
    public List<OfflinePaper> getOfflinePapers() {
        List<OfflinePaper> result = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_OFFLINE, null, null, null,
                null, null, COLUMN_ADDED_AT + " DESC");
        if (cursor.moveToFirst()) {
            do {
                Paper paper = paperFrom(cursor);
                String filePath = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));
                if (paper != null) {
                    result.add(new OfflinePaper(paper, filePath));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    /**
     * Value holder for an offline paper: the original paper record plus the local
     * PDF file path.
     */
    public static final class OfflinePaper {
        public final Paper paper;
        public final String filePath;

        OfflinePaper(Paper paper, String filePath) {
            this.paper = paper;
            this.filePath = filePath;
        }
    }

    // ---------- Internal helpers ----------

    /** Insert or replace a keyed row carrying a paper snapshot (-1 = conflict failure). */
    private boolean upsert(String table, Paper paper, String filePath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY_DRIVE_ID, paper.getDriveFileId());
        values.put(COLUMN_PAPER_JSON, paper.toJson().toString());
        if (filePath != null) {
            values.put(COLUMN_FILE_PATH, filePath);
        }
        values.put(COLUMN_ADDED_AT, System.currentTimeMillis());
        long rowId = db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return rowId != -1;
    }

    private boolean deleteRow(String table, String driveId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(table, COLUMN_KEY_DRIVE_ID + "=?", new String[]{driveId});
        db.close();
        return rows > 0;
    }

    private boolean rowExists(String table, String driveId) {
        Cursor cursor = getReadableDatabase().query(table, new String[]{COLUMN_KEY_DRIVE_ID},
                COLUMN_KEY_DRIVE_ID + "=?", new String[]{driveId}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private List<Paper> readPapers(String table, String orderBy) {
        List<Paper> papers = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(table, null, null, null, null, null, orderBy);
        if (cursor.moveToFirst()) {
            do {
                Paper paper = paperFrom(cursor);
                if (paper != null) {
                    papers.add(paper);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return papers;
    }

    /** Rebuilds a Paper from the stored snapshot; skips rows whose JSON is unreadable. */
    private Paper paperFrom(Cursor cursor) {
        try {
            String json = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAPER_JSON));
            return Paper.fromJson(new JSONObject(json));
        } catch (org.json.JSONException e) {
            return null;
        }
    }
}
