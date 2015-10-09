package com.taylorandtucker.jot.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.LineDataProvider;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.taylorandtucker.jot.R;
import com.taylorandtucker.jot.localdb.DBContract.EntryContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by tuckerkirven on 10/1/15.
 */
public class SentimentGraphFragment extends LineChart implements OnChartGestureListener, OnChartValueSelectedListener, IFragmentCard {

    private int SECONDS = 1000;
    private int MINUTES = SECONDS*60;
    private int HOURS = MINUTES*60;
    private int DAYS = HOURS*24;
    private int MONTHS = DAYS*30;
    private int YEARS = MONTHS*12;

    private final int layoutId = R.layout.fragment_sentiment_graph;

    //private Cursor dataCursor;
    @Override
    public int getLayoutResourceId()
    {
        return layoutId;
    }

    public SentimentGraphFragment(Context context) {
        super(context);
        //this.dataCursor = dataCursor;
        onCreate();
    }

    public SentimentGraphFragment(Context context, AttributeSet attrs, Cursor dataCursor) {
        super(context, attrs);
        onCreate();
    }

    protected void onCreate() {
/*
        this.setOnChartGestureListener(this);
        this.setOnChartValueSelectedListener(this);
        this.setDrawGridBackground(false);

        // no description text
        this.setDescription("");
        this.setNoDataTextDescription("After entering entries they will appear here");

        // enable value highlighting
        this.setHighlightEnabled(true);

        // enable touch gestures
        this.setTouchEnabled(true);

        // enable scaling and dragging
        this.setDragEnabled(true);
        this.setScaleEnabled(true);
        // this.setScaleXEnabled(true);
        // this.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        this.setPinchZoom(true);

        // set an alternative background color
        // this.setBackgroundColor(Color.GRAY);
        this.setBackgroundColor(Color.DKGRAY);
        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it



        XAxis xAxis = this.getXAxis();
        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        // add x-axis limit line

        YAxis leftAxis = this.getAxisLeft();


        // x-axis limit line
        LimitLine llYAxis = new LimitLine(0f, "");
        llYAxis.setLineWidth(1f);
        llYAxis.enableDashedLine(10f, 10f, 0f);
        llYAxis.setLineColor(Color.LTGRAY);
        llYAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llYAxis.setTextSize(10f);
        leftAxis.addLimitLine(llYAxis);

        xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        xAxis.setSpaceBetweenLabels(-5);
        xAxis.setTextColor(Color.WHITE);

        leftAxis.setAxisMaxValue(2.1f);
        leftAxis.setAxisMinValue(-2.1f);
        leftAxis.setStartAtZero(false);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        this.getAxisRight().setEnabled(false);
        this.getViewPortHandler().setMaximumScaleY(1);
        //this.getViewPortHandler().setMaximumScaleY(2f);
        //this.getViewPortHandler().setMaximumScaleX(2f);

//        this.setVisibleXRange(20);
//        this.setVisibleYRange(20f, AxisDependency.LEFT);
//        this.centerViewTo(20, 50, AxisDependency.LEFT);

        //this.animateX(2500, Easing.EasingOption.EaseInOutQuart);

        //this.addData(dataCursor);
        // get the legend (only possible after setting data)
        //Legend l = this.getLegend();


        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);

        // // dont forget to refresh the drawing
         this.invalidate();
         */
    }

    private int rand(int Min, int Max){
        return Min + (int)(Math.random() * ((Max - Min) + 1));
    }

    public void addData(Cursor dbCursor) {


        ArrayList<Entry> yVals = new ArrayList<Entry>();

        try {

            while (dbCursor.moveToNext()) {

                double sent = dbCursor.getDouble(dbCursor.getColumnIndex(EntryContract.COLUMN_SENTIMENT));
                long seconds = dbCursor.getLong(dbCursor.getColumnIndex(EntryContract.COLUMN_DATE));

                yVals.add(new Entry((float) sent, (int) seconds));
            }
        } catch (Exception e) {
            System.out.println("Failed to get curso data: " + e);
        }

        try {
            if (!yVals.isEmpty()) {
                int startX = yVals.get(0).getXIndex();
                int endX = yVals.get(yVals.size() - 1).getXIndex();

                System.out.println(yVals.size());
                System.out.println(startX + " " + endX);
                ArrayList<String> xVals = new ArrayList<String>();
                for (int i = startX - 1; i <= endX + 1; i++) {
                    xVals.add(i + "");
                }

                formatData(xVals, yVals);
            }
        } catch (Exception e) {
            System.out.println("Failed to do x or format data: " + e);
        }
    }
    public void formatData(List xVals, List yVals) {

        int now = 0;
        xVals = new ArrayList<String>();
               for (int i = 0; i < 20; i++) {
                   xVals.add((now) + "");
                   now += 24*60*60*1000;
               }

        yVals = new ArrayList<Entry>();
        for (int i = 0; i < 20; i++) {
            float val = (float) (Math.random() * 4) - 2;// + (float)
            // ((mult *
            // 0.1) / 10);
            if (Math.random() < .233) {
                yVals.add(new Entry(val, i));
            }
        }
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "");
        //set1.setFillAlpha(410);
        set1.setFillColor(Color.BLUE);
        //set1.setDrawCubic(true);
        set1.setDrawValues(false);
        // set the line to be drawn like this "- - - - - -"
        //set1.setColor(ColorTemplate.getHoloBlue());
        set1.setCircleColor(Color.WHITE);

        set1.setLineWidth(2f);
        set1.setCircleSize(3f);
        set1.setCircleSize(3f);
        //set1.setFillAlpha(200);

        //set1.setFillColor(ColorTemplate.getHoloBlue());
        //set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);
        set1.setDrawFilled(true);
        set1.setFillFormatter(new MyCustomFillFormatter());

        Paint paintRenderer =  this.getRenderer().getPaintRender();

        float height = mViewPortHandler.getContentCenter().y;
        int[] gradColors = {Color.GREEN, Color.YELLOW, Color.RED};
        paintRenderer.setShader(new LinearGradient(0, 20, 0, 600, gradColors,null, Shader.TileMode.MIRROR));
        set1.setColor(Color.BLACK);

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets


        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        this.setData(data);

        System.out.println("HEEEEEEEEEEEEEERRRRRRRRRRRREEEEEEEEeE");
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
        System.out.println("SCALEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        Log.i("", "low: " + this.getLowestVisibleXIndex() + ", high: " + this.getHighestVisibleXIndex());
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
        Log.i("", "lowwwwwwwwwwwwwww: " + this.getLowestVisibleXIndex() + ", high: " + this.getHighestVisibleXIndex());
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());

    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    public class MyCustomXAxisValueFormatter implements XAxisValueFormatter {


        public MyCustomXAxisValueFormatter() {
            // maybe do something here or provide parameters in constructor
        }

        @Override
        public String getXValue(String original, int index, ViewPortHandler viewPortHandler) {

            Calendar calendar = Calendar.getInstance();

            calendar.setTimeInMillis(Long.parseLong(original));


            int mYear = calendar.get(Calendar.YEAR);

            int nMonth = calendar.get(Calendar.MONTH);
            String mMonth = new SimpleDateFormat("MMM").format(calendar.getTime());
            String mMonthDay = new SimpleDateFormat("MM/dd").format(calendar.getTime());
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);

            // e.g. adjust the x-axis values depending on scale / zoom level
            if (viewPortHandler.getScaleX() > YEARS/DAYS)
                return mMonthDay+"";
            else if (viewPortHandler.getScaleX() > YEARS/MONTHS)
                return mMonth;
            else if (viewPortHandler.getScaleX() > YEARS/YEARS)
                return mYear+"";
            else
                return original;
        }

    }
    class MyCustomFillFormatter implements FillFormatter {

        // return the fill position
        public float getFillLinePosition(LineDataSet dataSet, LineDataProvider lineDataProvider) {

            return 0;
            //return dataSet.getYValForXIndex(dataSet.getEntryCount());
        }
    }
}
