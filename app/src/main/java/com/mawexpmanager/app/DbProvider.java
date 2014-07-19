package com.mawexpmanager.app;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.concurrent.CancellationException;

/**
 * Created by maww on 2014/5/20.
 */
public class DbProvider extends ContentProvider{
    private Context context;
    private DatabaseHelper dbHelper;
    public SQLiteDatabase db;

    private static final String AUTHORITY="com.mawexpmanager.app.DbProvider";
    private static final String PATH_BILL="bills";
    private static final String PATH_CATEGORY="category";
    private static final String PATH_CATEGORY_SUM="categorySum";

    public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/"+PATH_BILL);
    public static final Uri CATEGORY_URI=Uri.parse("content://"+AUTHORITY+"/"+PATH_CATEGORY);
    //public static final Uri CATEGORY_SUM_URI=Uri.parse("content://"+AUTHORITY+"/"+PATH_CATEGORY_SUM);

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+ "/bill";
    
    private static final int URI_TYPE_BILLS=1;
    private static final int URI_TYPE_BILL=2;
    private static final int URI_TYPE_CATEGORY=3;
    private static final int URI_TYPE_CATEGORY_SUM=4;
    private static UriMatcher uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY,PATH_BILL,URI_TYPE_BILLS);
        uriMatcher.addURI(AUTHORITY,PATH_BILL+"/#",URI_TYPE_BILL);
        uriMatcher.addURI(AUTHORITY,PATH_CATEGORY,URI_TYPE_CATEGORY);
        uriMatcher.addURI(AUTHORITY,PATH_BILL+"/categorySum",URI_TYPE_CATEGORY_SUM);
    }

    private static final String DATABASE_NAME = "expense.db";
    private static final int DATABASE_VESION = 1;
    public static final String TABLE_BILL = "bill";
    public static final String TABLE_CATEGORY = "category";

    public static final String KEY_COST = "cost";
    public static final String KEY_CATEGORY = "cat_id";
    public static final String KEY_CATEGORY_NAME = "cat_name";
    public static final String KEY_CATEGORY_COLOR = "cat_color";
    public static final String KEY_DATE = "date";
    public static final String KEY_NOTE = "note";
    public static final String KEY_SUM = "sum";
    public static final String KEY_ID = "_id";

    private static final String DATABASE_CREATE_BILL = "create table "+TABLE_BILL+"("+KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_COST + " INTEGER NOT NULL," + KEY_CATEGORY + " INTEGER NOT NULL," + KEY_DATE + " INTEGER NOT NULL," + KEY_NOTE + " TEXT" + ");";
    private static final String DATABASE_CREATE_CATEGORY = "create table "+TABLE_CATEGORY+"("+KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_CATEGORY_NAME + " TEXT NOT NULL," + KEY_CATEGORY_COLOR + " TEXT NOT NULL" + ");";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VESION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_BILL);
            db.execSQL(DATABASE_CREATE_CATEGORY);
            db.execSQL("INSERT INTO "+TABLE_CATEGORY+" VALUES(null," + " \"其他\" ," + " \"#77969A\" " + ");");
            db.execSQL("INSERT INTO "+TABLE_CATEGORY+" VALUES(null," + " \"早午晚餐\" ," + " \"#DC9FB4\" " + ");");
            db.execSQL("INSERT INTO "+TABLE_CATEGORY+" VALUES(null," + " \"運輸\" ," + " \"#0089A7\" " + ");");
            db.execSQL("INSERT INTO "+TABLE_CATEGORY+" VALUES(null," + " \"娛樂\" ," + " \"#F6C555\" " + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS" + TABLE_BILL);
            db.execSQL("DROP TABLE IF EXISTS" + TABLE_CATEGORY);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate(){
        this.context=getContext();
        dbHelper=new DatabaseHelper(this.context);
        return true;
    }

    @Override
    public Cursor query(Uri uri,String[] projection,String selection, String[] selectionArgs, String sortOrder){
        open();
        Cursor c;
        switch (uriMatcher.match(uri)){
            case URI_TYPE_BILLS:
                c= db.query(TABLE_BILL,projection,selection,selectionArgs,null,null,sortOrder);
                c.setNotificationUri(getContext().getContentResolver(),uri);
                return c;
            case URI_TYPE_BILL:
                c= db.query(TABLE_BILL,projection,KEY_ID+"=?",new String[]{uri.getLastPathSegment()},null,null,sortOrder);
                c.setNotificationUri(getContext().getContentResolver(),uri);
                return c;
            case URI_TYPE_CATEGORY:
                c= db.query(TABLE_CATEGORY,new String[]{"*"},null,null,null,null,KEY_ID+" ASC");
                c.setNotificationUri(getContext().getContentResolver(),uri);
                return c;
            case URI_TYPE_CATEGORY_SUM:
                /*c= db.rawQuery(
                        "SELECT bill.cat_id AS _id, sum(cost) AS sum, cat_name, cat_color " +
                                "FROM bill " +
                                "JOIN category ON bill.cat_id=category._id " +
                                "WHERE strftime('%Y-%m',date/1000,'unixepoch')=\"" + selectionArgs[0] + "\" " +
                                "GROUP BY cat_id " +
                                "ORDER BY sum DESC;", null
                );*/
                c=db.query("bill JOIN category ON bill.cat_id=category._id",projection,selection,selectionArgs,KEY_CATEGORY,null,sortOrder);
                c.setNotificationUri(getContext().getContentResolver(),uri);
                return c;
            default:
                throw new IllegalArgumentException("unknown URI:"+uri);
        }
    }

    @Override
    public Uri insert(Uri uri,ContentValues contentValues){
        open();
        long id=0;
        switch (uriMatcher.match(uri)){
            case URI_TYPE_BILLS:
                id=db.insert(TABLE_BILL,null,contentValues);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return Uri.parse(CONTENT_URI+"/"+id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        int rowupdated=0;
        open();
        switch (uriMatcher.match(uri)){
            case URI_TYPE_BILL:
                String id=uri.getLastPathSegment();
                if(selection==null ) {
                    rowupdated=db.delete(TABLE_BILL,KEY_ID+" = "+ id,null);
                }else{
                    rowupdated=db.delete(TABLE_BILL,KEY_ID+" = "+ id+" AND "+selection,selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("unknown URI:"+uri);
        }
        getContext().getContentResolver().notifyChange(CONTENT_URI,null);
        return rowupdated;
    }

    @Override
    public int update(Uri uri,ContentValues contentValues,String selection, String[] selectionArgs){
        int rowupdated=0;
        open();
        switch (uriMatcher.match(uri)){
            case URI_TYPE_BILL:
                String id=uri.getLastPathSegment();
                if(selection==null ) {
                    rowupdated=db.update(TABLE_BILL,contentValues,KEY_ID+"="+id,null);
                }else{
                    rowupdated=db.update(TABLE_BILL,contentValues,KEY_ID+"="+id+" and "+selection,selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("unknown URI:"+uri);
        }
        getContext().getContentResolver().notifyChange(CONTENT_URI,null);

        return rowupdated;
    }

    @Override
    public String getType(Uri uri){return null;}

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }
}
