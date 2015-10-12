package com.taylorandtucker.jot.localdb;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import com.taylorandtucker.jot.localdb.DBContract.*;

/**
 * Created by tuckerkirven on 10/11/15.
 */
public class EntityCursorLoader extends CursorLoader {
    jotDBHelper db;
    long id;

    public EntityCursorLoader(Context context, jotDBHelper db, long id) {
        super(context);
        this.db = db;
        this.id = id;
    }

    @Override
    public Cursor loadInBackground() {
        Cursor cursor = null;

        String query = "SELECT * ";

        String entries = EntryContract.TABLE_NAME;
        String entities = EntityContract.TABLE_NAME;
        String eTOe = EtoEContract.TABLE_NAME;
        String entityID = EtoEContract.COLUMN_ENTITY_ID;
        String entryID = EtoEContract.COLUMN_ENTRY_ID;
        query +=  "FROM ("+entities+" Inner Join "+eTOe+" on "+entities+"._id = "+eTOe+"."+entityID+") ";
        query += "Inner Join "+entries+" on "+entries+"._id = " + eTOe+"."+entryID;
        query += " WHERE "+ entities+"."+EntityContract._ID + " = " + id;
        query += " ORDER BY "+EntryContract.COLUMN_DATE+" DESC";

        cursor = db.getReadableDatabase().rawQuery(query, null);

        return cursor;
    }

}
