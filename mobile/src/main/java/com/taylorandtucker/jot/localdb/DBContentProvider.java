package com.taylorandtucker.jot.localdb;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.taylorandtucker.jot.localdb.DBContract.EntryContract;
import com.taylorandtucker.jot.localdb.DBContract.EntityContract;
import com.taylorandtucker.jot.localdb.DBContract.EtoEContract;

/**
 * Created by Taylor on 9/17/2015.
 */
public class DBContentProvider extends ContentProvider {
    private jotDBHelper dbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int ENTRIES = 1;
    private static final int ENTRY_ID = 2;
    private static final int ENTITIES= 3;
    private static final int ENTITY_ID = 4;
    private static final int EtoE = 5;
    private static final int EtoE_ID = 6;
    private static final String AUTHORITY = "com.taylorandtucker.jot.provider";
    private static final String ENTRY_PATH = "entry";
    private static final String ENTITY_PATH = "entity";
    private static final String EtoE_PATH = "entitiesToEntries";

    public static final Uri ENTRY_URI = Uri.parse("content://" + AUTHORITY
            + "/" + ENTRY_PATH);
    public static final Uri ENTITY_URI = Uri.parse("content://" + AUTHORITY
            + "/" + ENTITY_PATH);
    public static final Uri EtoE_URI = Uri.parse("content://" + AUTHORITY
            + "/" + EtoE_PATH);

    static {
        sUriMatcher.addURI(AUTHORITY, ENTRY_PATH, ENTRIES);
        sUriMatcher.addURI(AUTHORITY, ENTRY_PATH + "/#", ENTRY_ID);
        sUriMatcher.addURI(AUTHORITY, ENTITY_PATH, ENTITIES);
        sUriMatcher.addURI(AUTHORITY, ENTITY_PATH + "/#", ENTITY_ID);
        sUriMatcher.addURI(AUTHORITY, EtoE_PATH, EtoE);
        sUriMatcher.addURI(AUTHORITY, EtoE_PATH + "/#", EtoE_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new jotDBHelper(getContext());
        return false;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        //checkColumns(projection); TODO

        int uriType = sUriMatcher.match(uri);
        System.out.println("URI TYPE + " + uriType + " " + uri.toString());
        switch (uriType) {
            case ENTRIES:
                queryBuilder.setTables(EntryContract.TABLE_NAME);
                break;
            case ENTRY_ID:
                // adding the ID to the original query
                queryBuilder.setTables(EntryContract.TABLE_NAME);
                queryBuilder.appendWhere(EntryContract._ID + "="
                        + uri.getLastPathSegment());
                break;
            case ENTITIES:
                queryBuilder.setTables(EntityContract.TABLE_NAME);
                break;
            case ENTITY_ID:
                // adding the ID to the original query
                queryBuilder.setTables(EntityContract.TABLE_NAME);
                queryBuilder.appendWhere(EntityContract._ID + "="
                        + uri.getLastPathSegment());
                break;
            case EtoE:
                queryBuilder.setTables(EtoEContract.TABLE_NAME);
                break;
            case EtoE_ID:
                // adding the ID to the original query
                queryBuilder.setTables(EtoEContract.TABLE_NAME);
                queryBuilder.appendWhere(EtoEContract._ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase(); //readable?
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        long id = 0;
        switch (uriType) {
            case ENTRIES:
                id = sqlDB.insert(EntryContract.TABLE_NAME, null, values);
                break;
            case ENTITIES:
                id = sqlDB.insert(EntityContract.TABLE_NAME, null, values);
                break;
            case EtoE:
                id = sqlDB.insert(EtoEContract.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(ENTRY_PATH + "/" + id);
    }

    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case ENTRIES:
                return AUTHORITY;
            default:
                return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        int retVal = 0;
        switch (uriType) {
            case ENTRIES:
                sqlDB.beginTransaction();

                retVal = sqlDB.update(EntryContract.TABLE_NAME, values, selection, selectionArgs);
                sqlDB.setTransactionSuccessful();
                sqlDB.endTransaction();
                break;
            case ENTITIES:
                sqlDB.beginTransaction();
                retVal = sqlDB.update(EntityContract.TABLE_NAME, values, selection, selectionArgs);
                sqlDB.setTransactionSuccessful();
                sqlDB.endTransaction();
                break;
            case EtoE:
                sqlDB.beginTransaction();
                retVal = sqlDB.update(EtoEContract.TABLE_NAME, values, selection, selectionArgs);
                sqlDB.setTransactionSuccessful();
                sqlDB.endTransaction();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        //return Uri.parse(ENTRY_PATH + "/" + id);
        return retVal; //TODO
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0; //TODO
    }
}
