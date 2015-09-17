package com.taylorandtucker.jot.localdb;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.taylorandtucker.jot.localdb.EntriesContract.Contract;

/**
 * Created by Taylor on 9/16/2015.
 */
public class EntryDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1; //update this if schema/contract changes
    public static final String DATABASE_NAME = "JotEntries.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + Contract.TABLE_NAME + " (" +
                Contract._ID + " INTEGER PRIMARY KEY," +
                Contract.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                Contract.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                Contract.COLUMN_NAME_BODY + TEXT_TYPE +
        " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Contract.TABLE_NAME;

    public EntryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}