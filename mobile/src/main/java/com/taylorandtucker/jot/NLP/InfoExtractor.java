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

    //does everything that needs to be done with an entry and a processed entry
    public void processNewEntryData(String entryID, ProcessedEntry processedEntry){

        updateSentimentForEntry(entryID, processedEntry.getEntrySentiment());

        Map<String, Integer> pMap = processedEntry.personSentiment();

        for (Map.Entry<String, Integer> ent: pMap.entrySet()) {
            updateEntityWithNewSent(entryID, ent.getKey(), ent.getValue());
        }
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

    public void insertEntity(String name, int sentVal){
        ContentValues values = new ContentValues();
        values.put(EntityContract.COLUMN_NAME, name);
        values.put(EntityContract.COLUMN_SENTIMENT, sentVal);
        values.put(EntityContract.COLUMN_IMPORTANCE, getInitialImportance());
        System.out.println("insterting entity to: ");
        System.out.println(context.getContentResolver().insert(DBContentProvider.ENTITY_URI, values));

    }
    public Cursor getEntityByName(String name){
        String[] Values = new String[1];
        Values[0] = name;
        Cursor c = context.getContentResolver().query(DBContentProvider.ENTITY_URI, DBUtils.entityProjection, "name = ?", Values, null);

        return c;
    }

    public void updateEntityByName(String name, int importance, double sentiment){
        updateEntityByName(name, name, importance, sentiment);
    }
    public void updateEntityByName(String name, String newName, int importance, double sentiment){
        ContentValues values = new ContentValues();
        values.put(EntityContract.COLUMN_SENTIMENT, sentiment);
        values.put(EntityContract.COLUMN_NAME, newName);
        values.put(EntityContract.COLUMN_IMPORTANCE, importance);

        String[] Values = new String[1];
        Values[0] = name;
        context.getContentResolver().update(DBContentProvider.ENTITY_URI, values, "name = ?", Values);

    }
    private void updateEntityWithNewSent(String entryID, String name, int newSentiment){
        Cursor c = getEntityByName(name);

        if (c.moveToFirst()) {
            double sent = c.getDouble(c.getColumnIndex(EntityContract.COLUMN_SENTIMENT));
            int imp = c.getInt(c.getColumnIndex(EntityContract.COLUMN_IMPORTANCE));
            sent = calcSentForEntity(newSentiment, sent, imp);
            imp = recalculateImportance(name, imp);
            updateEntityByName(name, imp, sent);
        }else{
            insertEntity(name, newSentiment);
        }

        //do this last to ensure an entity has been created already
        addEtoEdata(entryID, name, newSentiment);
    }

    private void addEtoEdata(String entryID, String name, int newSentiment){
        
        Cursor entityInfo = getEntityByName(name);
        //some reason doesnt start on the first item - this will be false if no items are in the cursor
        if (entityInfo.moveToFirst()) {
            long entityId = entityInfo.getLong(entityInfo.getColumnIndex(EntityContract._ID));
            ContentValues values = new ContentValues();
            values.put(DBContract.EtoEContract.COLUMN_ENTRY_ID, entryID);
            values.put(DBContract.EtoEContract.COLUMN_ENTITY_ID, entityId);
            values.put(DBContract.EtoEContract.COLUMN_SENTIMENT, newSentiment);
            context.getContentResolver().insert(DBContentProvider.EtoE_URI, values);
        }
    }

    public Cursor getAllEntitiesByImportance(){
        Cursor c = context.getContentResolver().query(DBContentProvider.ENTITY_URI,
                DBUtils.entityProjection,
                null,
                null,
                EntityContract.COLUMN_IMPORTANCE + " DESC");

        return c;
    }
    private double calcSentForEntity(int newSent, double oldSent, int importance){
        double sum = oldSent*importance;
        return (sum+newSent)/(importance+1);
    }
    private int recalculateImportance(String name, int importance){
        return importance+1;
    }
    private int getInitialImportance(){
        return 1;
    }
}
