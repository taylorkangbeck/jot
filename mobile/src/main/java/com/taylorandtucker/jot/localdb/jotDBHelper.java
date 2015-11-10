package com.taylorandtucker.jot.localdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.taylorandtucker.jot.localdb.DBContract.EntryContract;
import com.taylorandtucker.jot.localdb.DBContract.EntityContract;
import com.taylorandtucker.jot.localdb.DBContract.EtoEContract;

/**
 * Created by Taylor on 9/16/2015.
 */
public class jotDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2; //update this if schema/Contract changes
    public static final String DATABASE_NAME = "Jot.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + EntryContract.TABLE_NAME + " (" +
                EntryContract._ID + " INTEGER PRIMARY KEY," +
                EntryContract.COLUMN_DATE + TEXT_TYPE + COMMA_SEP +
                EntryContract.COLUMN_BODY + TEXT_TYPE + COMMA_SEP +
                EntryContract.COLUMN_SENTIMENT + TEXT_TYPE + COMMA_SEP +
                EntryContract.COLUMN_ENTRY_NUM + TEXT_TYPE +
        " )";
    private static final String SQL_CREATE_ENTITIES =
            "CREATE TABLE " + EntityContract.TABLE_NAME + " (" +
                    EntityContract._ID + " INTEGER PRIMARY KEY," +
                    EntityContract.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    EntityContract.COLUMN_IMPORTANCE + TEXT_TYPE + COMMA_SEP +
                    EntityContract.COLUMN_SENTIMENT + TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_EtoE =
            "CREATE TABLE " + EtoEContract.TABLE_NAME + " (" +
                    EtoEContract._ID + " INTEGER PRIMARY KEY," +
                    EtoEContract.COLUMN_ENTITY_ID + TEXT_TYPE + COMMA_SEP +
                    EtoEContract.COLUMN_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    EtoEContract.COLUMN_SENTIMENT + TEXT_TYPE +
                    " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + EntryContract.TABLE_NAME;
    private static final String SQL_DELETE_ENTITIES =
            "DROP TABLE IF EXISTS " + EntityContract.TABLE_NAME;
    private static final String SQL_DELETE_EtoE =
            "DROP TABLE IF EXISTS " + EtoEContract.TABLE_NAME;

    public jotDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTITIES);
        db.execSQL(SQL_CREATE_EtoE);

    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_ENTITIES);
        db.execSQL(SQL_DELETE_EtoE);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}