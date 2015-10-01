package com.taylorandtucker.jot.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by tuckerkirven on 10/1/15.
 */
public class SentimentGraphView extends GraphView {
    private Viewport vp;
    private double windowMinX;
    private double windowMaxX;
    private int SECONDS = 1000;
    private int MINUTES = SECONDS*60;
    private int HOURS = MINUTES*60;
    private int DAYS = HOURS*24;
    private int MONTHS = DAYS*30;
    private int YEARS = MONTHS*12;

    public SentimentGraphView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        this.getViewport().setScalable(true);
        this.getViewport().setScrollable(true);
        vp = this.getViewport();

        this.getGridLabelRenderer().setLabelFormatter(new CustomDateLabelFormatter());
        this.getGridLabelRenderer().setNumHorizontalLabels(2); // only 4 because of the space

// set manual x bounds to have nice steps


        this.setBackgroundColor(Color.argb(0, 105, 105, 200));
        this.getViewport().setXAxisBoundsManual(true);
        this.setOnTouchListener(new GraphTouchListener());
    }

    private class CustomDateLabelFormatter implements LabelFormatter{
        @Override
        public String formatLabel(double value, boolean isValueX) {
            if(isValueX){

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Double.doubleToLongBits(value));


                int mYear = calendar.get(Calendar.YEAR);

                int nMonth = calendar.get(Calendar.MONTH);
                String mMonth = new SimpleDateFormat("MMM").format(calendar.getTime());
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);

                String type = "";
                double range = windowMaxX - windowMinX;
                System.out.println(range);
                if(range > DAYS*1.3 && range < MONTHS*1.3){
                    type = Integer.toString(mDay);
                }
                else if(range > MONTHS*1.3 && range < YEARS*1.3)
                    type = mMonth;
                else if( range > YEARS*1.3)

                    type = Integer.toString(mYear);
                    if (nMonth != 1)
                        return null;
                else
                    type = "hour";

                return type;
            }
            return null;
        }

        @Override
        public void setViewport(Viewport viewport) {

        }
    }
    private class GraphTouchListener implements View.OnTouchListener {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_POINTER_UP || event.getAction() == MotionEvent.ACTION_UP) {

                float screenX = event.getX();
                float screenY = event.getY();
                float width_x = v.getWidth();
                float viewX = screenX - v.getLeft();
                float viewY = screenY - v.getTop();
                float percent_x = (viewX / width_x);

                windowMinX = vp.getMinX(false);
                windowMaxX = vp.getMaxX(false);

                //System.out.println("Xmin : " + vp.getMinX(false) + " Xmax: " + vp.getMaxX(false) + " Percent = " + percent_x);


                return true;
            }
            return false;
        }
    }

}
