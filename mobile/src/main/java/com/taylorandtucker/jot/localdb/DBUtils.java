package com.taylorandtucker.jot.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.taylorandtucker.jot.Entry;
import com.taylorandtucker.jot.localdb.EntriesContract.Contract;

import java.util.Date;

/**
 * Created by Taylor on 9/16/2015.
 *
 * Don't access the DB directly from the UI thread!!!
 */
public class DBUtils {
    private static DBUtils instance = null;
    private EntryDBHelper dbHelper = null;
    private String[] projection = {
            Contract._ID,
            Contract.COLUMN_NAME_ENTRY_ID,
            Contract.COLUMN_NAME_DATE,
            Contract.COLUMN_NAME_BODY
    };

    protected DBUtils() {}

    public static DBUtils getInstance(Context context) {
        if (instance == null) {
            instance = new DBUtils();
            instance.dbHelper = new EntryDBHelper(context);
        }
        return instance;
    }

    public long putEntry(Entry entry)
    {
        long newRowId;
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(Contract.COLUMN_NAME_ENTRY_ID, entry.getId());
        values.put(Contract.COLUMN_NAME_DATE, entry.getCreatedOn().toString());
        values.put(Contract.COLUMN_NAME_BODY, entry.getBody());

        // Insert the new row, returning the primary key value of the new row
        newRowId = dbHelper.getWritableDatabase().insert(Contract.TABLE_NAME ,null, values);
        return newRowId;
    }

    private Entry getEntryById(String id)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // WHERE column
        String selection = Contract._ID;

        // WHERE values
        String[] selectionArgs = {
            id
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = Contract.COLUMN_NAME_DATE + " DESC";

        Cursor c = db.query(
                Contract.TABLE_NAME,                      // The table to query
                projection,                               // The columns to return
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

        Cursor c = db.rawQuery("select * from " + Contract.TABLE_NAME, null);

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
