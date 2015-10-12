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
public class CardCursorAdapter extends CursorAdapter {
    //use .changeCursor for specific queries

    public CardCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.entry_card, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView entryDateTextView = (TextView) view.findViewById(R.id.entryDate);
        TextView entryBodyTextView = (TextView) view.findViewById(R.id.entryBody);
        GradientView entrySentimentGradient = (GradientView) view.findViewById(R.id.sentGrad);

        // Extract properties from cursor
        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        double sent = cursor.getDouble(cursor.getColumnIndexOrThrow("sentiment"));

        // Populate fields with extracted properties
        entryDateTextView.setText(date);
        entryBodyTextView.setText(body);

        //view.setBackgroundDrawable(d);

        entrySentimentGradient.setgradient(sent);

    }

}
