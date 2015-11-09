package com.taylorandtucker.jot.ui;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.taylorandtucker.jot.R;

/**
 * Created by tuckerkirven on 11/8/15.
 */
class TestCover {
    public static TestCover instance = null;
    private static ListView entriesFeed;
    private static SentimentGraphFragment chart;
    private static CardCursorAdapter cardCursorAdapter;
    private static View coverView;

    public static TestCover getInstance(){

            return instance;

    }
    public TestCover(final ListView entriesFeed, SentimentGraphFragment chart, CardCursorAdapter cca, Activity activity) {
        this.entriesFeed = entriesFeed;
        this.chart = chart;
        this.cardCursorAdapter = cca;

        coverView = activity.findViewById(R.id.listCover);
        Button startTimerButton = (Button) activity.findViewById(R.id.startTimerButton);

        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                coverView.setVisibility(View.INVISIBLE);
                cardCursorAdapter.setStartTime();
            }
        });
        instance = this;
    }

    public void coverAll() {

        coverView.setVisibility(View.VISIBLE);

        entriesFeed.postDelayed(new Runnable() {
            @Override
            public void run() {
                entriesFeed.smoothScrollToPosition(0);
            }
        }, 500);

    }
}
