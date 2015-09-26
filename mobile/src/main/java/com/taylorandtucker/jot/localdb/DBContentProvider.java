package com.taylorandtucker.jot.localdb;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.taylorandtucker.jot.localdb.EntriesContract2.Contract;

/**
 * Created by Taylor on 9/17/2015.
 */
public class DBContentProvider extends ContentProvider {
    private EntryDBHelper2 dbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int ENTRIES = 1;
    private static final int ENTRY_ID = 2;
    private static final String AUTHORITY = "com.taylorandtucker.jot.provider";
    private static final String BASE_PATH = "entry";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    static {
        sUriMatcher.addURI(AUTHORITY, BASE_PATH, ENTRIES);
        sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ENTRY_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new EntryDBHelper2(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        //checkColumns(projection); TODO

        // Set the table
        queryBuilder.setTables(Contract.TABLE_NAME);

        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ENTRIES:
                break;
            case ENTRY_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(Contract._ID + "="
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
                id = sqlDB.insert(Contract.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
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
        return 0; //TODO
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0; //TODO
    }
}
