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
import java.util.Random;

/**
 * Created by Taylor on 9/16/2015.
 */
public class CardCursorAdapter extends CursorAdapter {
    //use .changeCursor for specific queries

    //TODO maybe change from being singleton

    static long clickDate;
    static boolean needsClick = false;
    public long dateMil;
    private static long startTimeMil=0;
    private static int entryCount = 0;
    public static String testType;
    private static int testID =-1;


    public CardCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    public static void setStartTime(){
        startTimeMil = new Date().getTime();
        if(testID==-1){
            Random rand = new Random();
            testID = rand.nextInt(100);
        }
        entryCount++;
    }

    private static CardCursorAdapter instance;
    public static CardCursorAdapter getInstance(Context context, Cursor cursor) {
        if (instance == null)
            instance = new CardCursorAdapter(context, cursor);
        else
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
        long dateSec = cursor.getLong(cursor.getColumnIndexOrThrow(EntryContract.COLUMN_DATE));
        String body = cursor.getString(cursor.getColumnIndexOrThrow(EntryContract.COLUMN_BODY));
        double sent = cursor.getDouble(cursor.getColumnIndexOrThrow(EntryContract.COLUMN_SENTIMENT));
        final int entryNumInList = cursor.getInt(cursor.getColumnIndexOrThrow(EntryContract.COLUMN_ENTRY_NUM));

        dateMil = dateSec*1000;
        // Populate fields with extracted properties
        Date date = new Date(dateSec*1000);
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


        /*
        * onclick blue outline was moved to MainFragment onCreateView
        */


        view.setLongClickable(true);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final View view = v;

                System.out.println("card clicked2");
                long timeTakenMil = new Date().getTime() - startTimeMil;
                String testDataString = "";
                testDataString += "Test ID:  "+testID;
                testDataString += ", Entry " + entryCount;
                testDataString += ", Time: "+ timeTakenMil+" ms ";
                testDataString += ", Test Type: " + testType;
                testDataString += ", Position in list From bottom: " + entryNumInList;

                FeedBackAsync fa = new FeedBackAsync(testDataString);
                fa.execute();

                return true;
            }
        });

        if(needsClick && dateMil > clickDate && dateMil < clickDate+24*60*60*1000){
            MainFragment.flashBlueCardOutline(view);
            needsClick = false;
        }

    }


}
