package com.taylorandtucker.jot.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
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
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.entity_card, parent, false);

        final String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        final long entityId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fm = ((FragmentActivity)context).getSupportFragmentManager();
                Bundle data = new Bundle();
                data.putLong("entityId", entityId);
                data.putString("entityName", name);
                Fragment ef = new EntityFragment(data);
                fm.beginTransaction().replace(R.id.container, ef, "entityFrag1").commit();
            }
        });

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView entityNameTextView = (TextView) view.findViewById(R.id.entityName);
        TextView entityMentionTextView = (TextView) view.findViewById(R.id.entityMentions);
        View background = (View) view.findViewById((R.id.cardContent));

        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        double sent = cursor.getDouble(cursor.getColumnIndexOrThrow("sentiment"));
        int mentions = cursor.getInt(cursor.getColumnIndex("importance"));

        // Populate fields with extracted properties
        entityNameTextView.setText(name);

        String eString = "entries";

        if (mentions == 1)
            eString = "entry";

        entityMentionTextView.setText(mentions + " " + eString);

        int a = Color.WHITE;
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.BL_TR,
                new int[] {a,a,a,a,a,a,a,a,GradientView.getColorFromGradient(sent)});
        d.setCornerRadius(0f);
        background.setBackground(d);
    }
}
