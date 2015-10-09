package com.taylorandtucker.jot.NLP;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.taylorandtucker.jot.Entry;
import com.taylorandtucker.jot.localdb.DBContentProvider;
import com.taylorandtucker.jot.localdb.DBContract;
import com.taylorandtucker.jot.localdb.DBContract.EntityContract;
import com.taylorandtucker.jot.localdb.DBUtils;

import java.util.Map;

/**
 * Created by tuckerkirven on 10/7/15.
 */
public class InfoExtractor {

    private Context context;

    //passing in getActivity() as the context allows UI listeners to update
    public InfoExtractor(Context context){
            this.context = context;
    }
    public Uri putEntry(Entry entry)
    {
        Uri newRowId;
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DBContract.EntryContract._ID, entry.getId());
        values.put(DBContract.EntryContract.COLUMN_DATE, entry.getCreatedOn().toString());
        values.put(DBContract.EntryContract.COLUMN_BODY, entry.getBody());
        values.put(DBContract.EntryContract.COLUMN_SENTIMENT, entry.getSentiment());

        // Insert the new row, returning the primary key value of the new row
        newRowId = context.getContentResolver().insert(DBContentProvider.ENTRY_URI, values);
        return newRowId;
    }
    public void updateSentimentForEntry(String entryID, double sentSum){
        ContentValues values = new ContentValues();
        values.put(DBContract.EntryContract.COLUMN_SENTIMENT, sentSum);
        String[] Values = new String[1];
        Values[0] = entryID;
        context.getContentResolver().update(DBContentProvider.ENTRY_URI, values, "_id" + "= ?", Values);

    }

    public void insertEntityData(Map<String, Integer> entSentMap){


    }

    public void insertEntity(String name, int sentVal){
        ContentValues values = new ContentValues();
        values.put(EntityContract.COLUMN_NAME, name);
        values.put(EntityContract.COLUMN_SENTIMENT, sentVal);
        context.getContentResolver().insert(DBContentProvider.ENTITY_URI , values);

    }
    public Cursor getEntityByName(String name){
        Cursor c = context.getContentResolver().query(DBContentProvider.ENTITY_URI, DBUtils.entityProjection, "name = ?", null, null);

        System.out.println(c.getColumnName(1));
        System.out.println(c.getColumnIndex(EntityContract.COLUMN_NAME));
        System.out.println(c.getColumnIndex(EntityContract.COLUMN_IMPORTANCE));
        System.out.println(c.getColumnIndex(EntityContract.COLUMN_SENTIMENT));
        return c;
    }
    private void updateEntityVals(String entityID, double importance, double sentiment){

    }
}
