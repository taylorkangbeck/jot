package com.taylorandtucker.jot.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.taylorandtucker.jot.R;
import com.taylorandtucker.jot.localdb.DBContract.EntryContract;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Taylor on 9/16/2015.
 */
public class CardCursorAdapter extends CursorAdapter {
    //use .changeCursor for specific queries

    //TODO maybe change from being singleton

    public CardCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    private static CardCursorAdapter instance;
    public static CardCursorAdapter getInstance(Context context, Cursor cursor) {
        if (instance == null)
            instance = new CardCursorAdapter(context, cursor);
        else if (cursor != null)
            instance.changeCursor(cursor);
        return instance;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.entry_card, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView entryDateTextView = (TextView) view.findViewById(R.id.entryDate);
        TextView entryBodyTextView = (TextView) view.findViewById(R.id.entryBody);
        //GradientView entrySentimentGradient = (GradientView) view.findViewById(R.id.sentGrad);
        View background = (View) view.findViewById((R.id.cardContent));

        // Extract properties from cursor
        long dateMil = cursor.getLong(cursor.getColumnIndexOrThrow(EntryContract.COLUMN_DATE));
        String body = cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.COLUMN_BODY));
        double sent = cursor.getDouble(cursor.getColumnIndexOrThrow(EntryContract.COLUMN_SENTIMENT));

        // Populate fields with extracted properties
        Date date = new Date(dateMil*1000);
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE MMM dd, yyyy    h:mm a");
        String formattedDate = formatter.format(date);
        entryDateTextView.setText(formattedDate);
        entryBodyTextView.setText(body);

        //view.setBackgroundDrawable(d);
        int a = Color.WHITE;
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.BL_TR,
                new int[] {a,a,a,a,a,a,a,a,GradientView.getColorFromGradient(sent)});
        d.setCornerRadius(0f);
        background.setBackground(d);
       // entrySentimentGradient.setgradient(sent);

    }

}
