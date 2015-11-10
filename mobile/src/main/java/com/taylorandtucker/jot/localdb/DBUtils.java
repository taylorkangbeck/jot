package com.taylorandtucker.jot.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.taylorandtucker.jot.Entry;
import com.taylorandtucker.jot.localdb.DBContract.EntryContract;
import com.taylorandtucker.jot.localdb.DBContract.EntityContract;

/**
 * Created by Taylor on 9/16/2015.
 *
 * Don't access the DB directly from the UI thread!!!
 */
public class DBUtils {
    private static DBUtils instance = null;
    private jotDBHelper dbHelper = null;
    public static String[] entryProjection = {
            EntryContract._ID,
            EntryContract.COLUMN_DATE,
            EntryContract.COLUMN_BODY,
            EntryContract.COLUMN_SENTIMENT,
            EntryContract.COLUMN_ENTRY_NUM
    };
    public static String[] entityProjection = {
            EntityContract._ID,
            EntityContract.COLUMN_NAME,
            EntityContract.COLUMN_IMPORTANCE,
            EntityContract.COLUMN_SENTIMENT
    };

    protected DBUtils() {}

    //passing in getActivity() as the context allows for automatic updates
    public static DBUtils getInstance(Context context) {
        if (instance == null) {
            instance = new DBUtils();
            instance.dbHelper = new jotDBHelper(context);
        }
        instance.dbHelper = new jotDBHelper(context);
        return instance;
    }

    public long putEntry(Entry entry)
    {
        long newRowId;
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(EntryContract._ID, entry.getId());
        values.put(EntryContract.COLUMN_DATE, entry.getCreatedOn().toString());
        values.put(EntryContract.COLUMN_BODY, entry.getBody());
        values.put(EntryContract.COLUMN_SENTIMENT, entry.getSentiment());
        values.put(EntryContract.COLUMN_ENTRY_NUM, entry.getEntryNumber());

        // Insert the new row, returning the primary key value of the new row
         newRowId = dbHelper.getWritableDatabase().insert(EntryContract.TABLE_NAME ,null, values);
        return newRowId;
    }

    private Entry getEntryById(String id)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // WHERE column
        String selection = EntryContract._ID;

        // WHERE values
        String[] selectionArgs = {
            id
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = EntryContract.COLUMN_DATE + " DESC";

        Cursor c = db.query(
                EntryContract.TABLE_NAME,                      // The table to query
                entryProjection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        return new Entry("test"); //TODO
    }

    public Cursor getAllEntriesQuery() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + EntryContract.TABLE_NAME, null);
        return c;
    }

    public Cursor getAllEntitiesQuery() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + EntityContract.TABLE_NAME, null);
        return c;
    }

/** not currently used, TODO delete
    private class getAllEntriesTask extends AsyncTask<Void, Void, Cursor> {
        private Runnable callBack;

        public getAllEntriesTask(Runnable runnable)
        {
            this.callBack = runnable;
        }

        protected Cursor doInBackground(Void... params) {
            return getAllEntriesQuery();
        }

        protected void onPostExecute(Cursor result) {
            callBack.run();
        }
    }
 **/

    //TODO: UPDATE, DELETE
}
