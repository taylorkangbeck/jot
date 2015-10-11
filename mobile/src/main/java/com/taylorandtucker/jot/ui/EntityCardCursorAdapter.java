package com.taylorandtucker.jot.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.taylorandtucker.jot.R;

/**
 * Created by Taylor on 9/16/2015.
 */
public class EntityCardCursorAdapter extends CursorAdapter {
    //use .changeCursor for specific queries

    public EntityCardCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.entity_card, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView entityNameTextView = (TextView) view.findViewById(R.id.entityName);
        TextView entitySentTextView = (TextView) view.findViewById(R.id.entitySentiment);


        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String sent = cursor.getString(cursor.getColumnIndexOrThrow("sentiment"));


        // Populate fields with extracted properties
        entityNameTextView.setText(name);
        entitySentTextView.setText(sent);
    }

}
