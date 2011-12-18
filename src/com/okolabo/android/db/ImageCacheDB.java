package com.okolabo.android.db;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ImageCacheDB extends SQLiteOpenHelper {

    private static final String DB_FILE = "imagecache.db";
    public static final String TBL_CACHE = "ImageCache";
    private static final int DB_VERSION = 1;
    
    public interface CacheColumn {
        public static final String ID = "_id";
        public static final String NAME = "fileName";
        public static final String REGIST_DATE = "registDate";
        public static final String URL = "url";
        public static final String TYPE = "type";
    }
    
    private SQLiteDatabase mDB;
    
    public static ImageCacheDB instance;
    
    private ImageCacheDB(Context context) {
        super(context, DB_FILE, null, DB_VERSION);
    }
    
    public static ImageCacheDB getInstance(Context context) {
        if (instance == null) {
            instance = new ImageCacheDB(context);
        }
        return instance;
    }
    
    synchronized private SQLiteDatabase getDB() {
        if (mDB == null) {
            mDB = getWritableDatabase();
        }
        return mDB;
    }
    
    public void close() {
        if (mDB != null) {
            getDB().close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TBL_CACHE + " ("
                + CacheColumn.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CacheColumn.REGIST_DATE + " INTEGER,"
                + CacheColumn.URL + " VARCHAR(300),"
                + CacheColumn.TYPE + " VARCHAR(100),"
                + CacheColumn.NAME + " VARCHAR(20))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    
    public long insert(String url) {
        ContentValues values = new ContentValues();
        values.put(CacheColumn.REGIST_DATE, new Date().getTime());
        values.put(CacheColumn.URL, url);
        long id = getDB().insertOrThrow(TBL_CACHE, null, values);
        return id;
    }

    public int update(long id, String filename, String type) {
        ContentValues values = new ContentValues();
        values.put(CacheColumn.TYPE, type);
        values.put(CacheColumn.NAME, filename);
        return getDB().update(TBL_CACHE, values, CacheColumn.ID + " = ?", new String[]{String.valueOf(id)});
    }
    
    public int delete(long id) {
        return getDB().delete(TBL_CACHE, CacheColumn.ID + " = ?", new String[]{String.valueOf(id)});
    }
    
    public Cursor exists(String url) {
        return getDB().query(TBL_CACHE, null, CacheColumn.URL + " = ?", new String[]{url}, null, null, null);
    }
    public Cursor existsFile(String url) {
        return getDB().query(TBL_CACHE, null, CacheColumn.URL + " = ? AND " + CacheColumn.NAME + " IS NOT NULL", new String[]{url}, null, null, null);
    }
    
    public Cursor findOlderCache() {
        return getDB().query(TBL_CACHE,
                null,
                CacheColumn.REGIST_DATE + " < ?",
                new String[]{String.valueOf(new Date().getTime() - 604800)}, // 7*24*60*60
                null, null, null);
    }
    
    public Cursor findAll() {
        return getDB().query(TBL_CACHE,
                null,
                null,
                null,
                null, null, null);
    }
    
    @Override
    protected void finalize() throws Throwable {
        if (mDB != null) {
            mDB.close();
        }
        this.close();
        super.finalize();
    }
}
