package com.taylorandtucker.jot.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.taylorandtucker.jot.Entity;
import com.taylorandtucker.jot.Entry;
import com.taylorandtucker.jot.localdb.EntriesContract.ContractEntries;
import com.taylorandtucker.jot.localdb.EntitiesContract.ContractEntities;

/**
 * Created by Taylor on 9/16/2015.
 *
 * Don't access the DB directly from the UI thread!!!
 */
public class DBUtils {
    private static DBUtils entryInstance = null;
    private static DBUtils entityInstance = null;
    private EntryDBHelper dbEntryHelper = null;
    private EntryDBHelper dbEntityHelper = null;

    private String[] entryProjection = {
            ContractEntries._ID,
            ContractEntries.COLUMN_DATE,
            ContractEntries.COLUMN_BODY,
            ContractEntries.COLUMN_SENTIMENT
    };

    private String[] entityProjection = {
            ContractEntities._ID,
            ContractEntities.COLUMN_NAME,
            ContractEntities.COLUMN_IMPORTANCE,
            ContractEntities.COLUMN_SENTIMENT
    };

    protected DBUtils() {}

    public static DBUtils getEntryInstance(Context context) {
        if (entryInstance == null) {
            entryInstance = new DBUtils();
            entryInstance.dbEntryHelper = new EntryDBHelper(context);
        }
        return entryInstance;
    }

    public static DBUtils getEntityInstance(Context context) {
        if (entityInstance == null) {
            entityInstance = new DBUtils();
            entityInstance.dbEntryHelper = new EntryDBHelper(context);
        }
        return entityInstance;
    }
    public long putEntry(Entry entry)
    {
        long newRowId;
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ContractEntries._ID, entry.getId());
        values.put(ContractEntries.COLUMN_DATE, entry.getCreatedOn().toString());
        values.put(ContractEntries.COLUMN_BODY, entry.getBody());
        values.put(ContractEntries.COLUMN_SENTIMENT, entry.getSentiment());

        // Insert the new row, returning the primary key value of the new row
        newRowId = dbEntryHelper.getWritableDatabase().insert(ContractEntries.TABLE_NAME ,null, values);
        return newRowId;
    }

    public long putEntity(Entity entity)
    {
        long newRowId;
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ContractEntities._ID, entity.getId());
        values.put(ContractEntities.COLUMN_NAME, entity.getName().toString());
        values.put(ContractEntities.COLUMN_IMPORTANCE, entity.getImportance());
        values.put(ContractEntities.COLUMN_SENTIMENT, entity.getSentiment());

        // Insert the new row, returning the primary key value of the new row
        newRowId = dbEntityHelper.getWritableDatabase().insert(ContractEntities.TABLE_NAME ,null, values);
        return newRowId;
    }

    private Entry getEntryById(String id)
    {
        SQLiteDatabase db = dbEntityHelper.getReadableDatabase();

        // WHERE column
        String selection = ContractEntries._ID;

        // WHERE values
        String[] selectionArgs = {
            id
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = ContractEntries.COLUMN_DATE + " DESC";

        Cursor c = db.query(
                ContractEntries.TABLE_NAME,                      // The table to query
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
        SQLiteDatabase db = dbEntryHelper.getReadableDatabase();

        Cursor c = db.rawQuery("select * from " + ContractEntries.TABLE_NAME, null);

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
