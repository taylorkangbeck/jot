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
        View entrySentimentGradient = (View) view.findViewById(R.id.sentGrad);

        // Extract properties from cursor
        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        double sent = cursor.getDouble(cursor.getColumnIndexOrThrow("sentiment"));

        // Populate fields with extracted properties
        entryDateTextView.setText(date);
        entryBodyTextView.setText(body);

        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.BL_TR,
                new int[] {Color.WHITE,getColorFromGradient(sent)});
        d.setCornerRadius(0f);

        view.setBackgroundDrawable(d);
        //entrySentimentGradient.setBackground(d);

    }
private int getColorFromGradient(double sent){
    /*double resultRed = color1.red + percent * (color2.red - color1.red);
    double resultGreen = color1.green + percent * (color2.green - color1.green);
    double resultBlue = color1.blue + percent * (color2.blue - color1.blue);
    */

    int red = Color.RED;
    int redR = Color.red(red);
    int redG = Color.green(red);
    int redB = Color.blue(red);

    int yellow = Color.YELLOW;
    int yellowR = Color.red(yellow);
    int yellowG = Color.green(yellow);
    int yellowB = Color.blue(yellow);

    int green = Color.GREEN;
    int greenR = Color.red(green);
    int greenG = Color.green(green);
    int greenB = Color.blue(green);

    double resultR;
    double resultG;
    double resultB;

    if(sent>=0){
        double percent = sent/2;
        resultR = yellowR + percent*(greenR-yellowR);
        resultG = yellowG + percent*(greenG-yellowG);
        resultB = yellowB + percent*(greenB-yellowB);

    }else{
        double percent = -1*sent/2;
        resultR = redR + percent*(yellowR-redR);
        resultG = redG + percent*(yellowG-redG);
        resultB = redB + percent*(yellowB-redB);
    }


    return Color.rgb((int) resultR, (int) resultG, (int)resultB);
}
}
