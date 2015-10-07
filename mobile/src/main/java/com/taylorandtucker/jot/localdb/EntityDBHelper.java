package com.taylorandtucker.jot.localdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.taylorandtucker.jot.localdb.EntitiesContract.ContractEntities;

/**
 * Created by Taylor on 9/16/2015.
 */
public class EntityDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1; //update this if schema/contract changes
    public static final String DATABASE_NAME = "JotEntries.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + ContractEntities.TABLE_NAME + " (" +
                ContractEntities._ID + " INTEGER PRIMARY KEY," +
                ContractEntities.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                ContractEntities.COLUMN_IMPORTANCE + TEXT_TYPE + COMMA_SEP +
                ContractEntities.COLUMN_SENTIMENT + TEXT_TYPE +
        " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ContractEntities.TABLE_NAME;

    public EntityDBHelper(Context context) {
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