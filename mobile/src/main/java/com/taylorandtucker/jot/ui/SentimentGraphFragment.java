package com.taylorandtucker.jot.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by tuckerkirven on 10/1/15.
 */
public class SentimentGraphFragment extends LineChart implements OnChartGestureListener, OnChartValueSelectedListener, IFragmentCard {

    private long SECONDS = 1000;
    private long MINUTES = SECONDS*60;
    private long HOURS = MINUTES*60;
    private long DAYS = HOURS*24;
    private long WEEKS = DAYS*7;
    private long MONTHS = DAYS*30;
    private long YEARS = DAYS*365;

    private long startTime;
    private long range;
    private long visibleRange;
    private int nodeCount;

    private int prevHighX;
    private int prevLowX;
    private Context context;

    private GraphVPListener graphVPListener;
    private final int layoutId = R.layout.fragment_sentiment_graph;

    public interface GraphVPListener{
        void onVPRangeChange(long startDate, long endDate);
        void onNodeSelected(long startOfDay, long endOfDay);
    }
    //private Cursor dataCursor;
    @Override
    public int getLayoutResourceId()
    {
        return layoutId;
    }

    public SentimentGraphFragment(Context context) {
        super(context);
        this.context = context;
        setupChart();
    }

    public SentimentGraphFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setupChart();
    }

    public int dateMilliToGraphIndex(long dateMilli) {
        return (int) (dateMilli/DAYS - startTime);
    }

    public void addVPListener(GraphVPListener vpl){
        this.graphVPListener = vpl;
    }
    public void setupChart() {
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

        this.getAxisLeft().setEnabled(false);
        this.getAxisRight().setEnabled(false);
        this.getViewPortHandler().setMaximumScaleY(1);
        //this.getViewPortHandler().setMaximumScaleY(2f);


        xAxis.setLabelsToSkip(60 * 60 * 24);

        // // dont forget to refresh the drawing
         this.invalidate();


    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        setGradient();
    }

    public void setGradient(){

        Paint paintRenderer =  this.getRenderer().getPaintRender();

        int[] gradColors = {Color.GREEN, Color.LTGRAY, Color.RED};

        float startY = this.getViewPortHandler().contentTop();
        float endY = this.getViewPortHandler().contentBottom();

        paintRenderer.setShader(new LinearGradient(0, startY, 0, endY, gradColors, null, Shader.TileMode.MIRROR));
    }

    private List<DayEntry> dataToDays(List<com.taylorandtucker.jot.Entry> origEntries){
        List averagedList = new ArrayList();


        int curDay = origEntries.get(0).createdDaysAfterEpoch();
        double runSum = 0;
        int entryPerDay = 0;
        for(int i = 0; i<origEntries.size(); ++i){
            int entDay = origEntries.get(i).createdDaysAfterEpoch();
            double curSent = origEntries.get(i).getSentiment();

            if(entDay == curDay ) {
                runSum += curSent;
                ++entryPerDay;
            }
            else{

                averagedList.add(new DayEntry(curDay, runSum / entryPerDay));
                runSum = curSent;
                entryPerDay = 1;
                curDay = entDay;

            }
        }

        averagedList.add(new DayEntry(curDay, runSum / entryPerDay));
        return averagedList;
    }
    private int rand(int Min, int Max){
        return Min + (int)(Math.random() * ((Max - Min) + 1));
    }

    class DayEntry{
        public DayEntry(int day, double sent){
            this.day = day;
            this.sent = sent;
        }
        public int day;
        public double sent;
    }
    public void updateData(List<com.taylorandtucker.jot.Entry> entries) {

        //formatData(new ArrayList(), new ArrayList());

        ArrayList<Entry> yVals = new ArrayList<Entry>();

        if (!entries.isEmpty()) {

            startTime = entries.get(0).createdDaysAfterEpoch();
            List<DayEntry> dayEntries = dataToDays(entries);
            nodeCount = dayEntries.size();
            for (DayEntry diaryEntry : dayEntries) {

                double sent = diaryEntry.sent;
                int day = diaryEntry.day;


                yVals.add(new Entry((float) sent, (int) (day - startTime)));
            }

            int startX = yVals.get(0).getXIndex();
            int endX = yVals.get(yVals.size() - 1).getXIndex();

            range = endX - startX;

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = startX ; i <= endX; i++) {

                xVals.add(i + "");
            }

            formatData(xVals, yVals);

        }

        //setting the max scale and range min does something dumb andscrolls to the end of the chart
        //when max scale is reached. dumb
        //this.getViewPortHandler().setMaximumScaleX((float) (2));
        //this.setVisibleXRangeMinimum(7f);


    }
    public void formatData(List xVals, List yVals) {

        // create a dataset and give it a type
        LineDataSet set1 = createSet(yVals);
        //set1.setFillAlpha(410);


        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets


        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data

        this.setData(data);
        this.invalidate();

    }
    private LineDataSet createSet(List<Entry> data) {

        LineDataSet set1 = new LineDataSet(data, "");
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

        set1.setColor(Color.DKGRAY);
        return set1;
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

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        chartVisibleRangeChange(getMinViewX(), getMaxViewX(), false);
        visibleRange = getMaxViewX()-getMinViewX();
        //TODO for some reason sometimes list does not update
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

        long min = getMinViewX();
        long max = getMaxViewX();

               //on chartTranslate gets called on touch even when view is at maximum or minimum
        if(getLowestVisibleXIndex() == 0){
                        max = min+ visibleRange;
        }

        if (range == getHighestVisibleXIndex()){
            min = max-visibleRange;
        }
        chartVisibleRangeChange(min, max, false);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (graphVPListener !=null) {
            Log.i("Entry selected", e.toString());
            setGradient();

            long dayTimeMilli = startTime * DAYS + e.getXIndex() * DAYS;
            DateTime startOfDay = new DateTime(dayTimeMilli).withTimeAtStartOfDay();
            DateTime endOfDay = new DateTime(dayTimeMilli).plusDays(1).withTimeAtStartOfDay();

            this.graphVPListener.onNodeSelected(startOfDay.getMillis(), endOfDay.getMillis());
        }
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    public void chartVisibleRangeChange(long firstDay, long lastDay, boolean force){
       if (firstLastVisibleNodesChanged() || force) {
           if (this.graphVPListener != null) {
               DateTime startOfStartDay = new DateTime(firstDay).withTimeAtStartOfDay();
               DateTime endOfEndDay = new DateTime(lastDay).plusDays(1).withTimeAtStartOfDay();

               this.graphVPListener.onVPRangeChange(startOfStartDay.getMillis(), endOfEndDay.getMillis());
           }

       }
    }
    public boolean firstLastVisibleNodesChanged(){
        int curFirst = getLowestVisibleXIndex();
        int curLast  = getHighestVisibleXIndex();

        if(curFirst != prevLowX || curLast != prevHighX){
            prevHighX = curLast;
            prevLowX = curFirst;
            return false;
        }
        return true;

    }
    public class MyCustomXAxisValueFormatter implements XAxisValueFormatter {


        public MyCustomXAxisValueFormatter() {
            // maybe do something here or provide parameters in constructor
        }

        @Override
        public String getXValue(String original, int index, ViewPortHandler viewPortHandler) {

            Calendar calendar = Calendar.getInstance();


            calendar.setTimeInMillis((Long.parseLong(original) * DAYS) + startTime*DAYS);

            //System.out.println("Center: " + viewPortHandler.);

            Date leftDate = new Date(getMinViewX());
            Date rightDate = new Date(getMaxViewX());
            int mYear = calendar.get(Calendar.YEAR);

            int nMonth = calendar.get(Calendar.MONTH);
            String mMonth = new SimpleDateFormat("MMM").format(calendar.getTime());
            String mMonthDay = new SimpleDateFormat("MM/dd/yyy").format(calendar.getTime());
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);

            String xStr = "idk";
            String leftLabel = "";
            String rightLabel="";
            long skipLabels = DAYS;

            long vpRange = getVPRange();
            // e.g. adjust the x-axis values depending on scale / zoom level

            if (vpRange > YEARS) {

                xStr = new SimpleDateFormat("yyyy").format(calendar.getTime());
                skipLabels = YEARS;
            }
            else if (vpRange <= YEARS && vpRange > YEARS / 2) {
                leftLabel = new SimpleDateFormat("yyyy").format(leftDate);
                rightLabel = new SimpleDateFormat("yyyy").format(rightDate);
                xStr = new SimpleDateFormat("MMM").format(calendar.getTime());
                if(!new SimpleDateFormat("d").format(calendar.getTime()).equals("01")) {
                xStr="";
                }
                skipLabels = 0;//MONTHS*2;
            }
            else if (vpRange <= YEARS / 2 && vpRange > MONTHS) {
                leftLabel = new SimpleDateFormat("yyyy").format(leftDate);
                rightLabel = new SimpleDateFormat("yyyy").format(rightDate);
                xStr = new SimpleDateFormat("MMM").format(calendar.getTime());

                if(!new SimpleDateFormat("dd").format(calendar.getTime()).equals("01")) {
                    xStr = "";
                }
                skipLabels = 0;//MONTHS;
            }
            else if (vpRange <= MONTHS && vpRange > MONTHS/2) {
                leftLabel = new SimpleDateFormat("MMMM").format(leftDate);
                rightLabel = new SimpleDateFormat("MMMM").format(rightDate);
                xStr = new SimpleDateFormat("dd").format(calendar.getTime());
                skipLabels = DAYS*2;
            }
            else if (vpRange <= MONTHS/2 && vpRange > WEEKS){
                leftLabel = new SimpleDateFormat("MMMM").format(leftDate);
                rightLabel = new SimpleDateFormat("MMMM").format(rightDate);
                xStr = new SimpleDateFormat("dd").format(calendar.getTime());
                skipLabels = DAYS;
            }
            else {
                //todo find closest monday?
                leftLabel = new SimpleDateFormat("MMMM").format(leftDate);
                rightLabel = new SimpleDateFormat("MMMM").format(rightDate);
                xStr = new SimpleDateFormat("E MM/d").format(calendar.getTime());
                skipLabels = 0;
            }

            SentimentGraphFragment.this.getXAxis().setLabelsToSkip((int) (skipLabels/DAYS));
            String centerLabel ="";
            if(rightLabel.equals(leftLabel)) {
                rightLabel="";
                centerLabel=leftLabel;
                leftLabel="";
            }

            setleftLabel(leftLabel);
            setRightLabel(rightLabel);
            setCenterLabel(centerLabel);
            return xStr;

        }

    }
    class MyCustomFillFormatter implements FillFormatter {

        // return the fill position
        public float getFillLinePosition(LineDataSet dataSet, LineDataProvider lineDataProvider) {
            return 0;
            //return dataSet.getYValForXIndex(dataSet.getEntryCount());
        }
    }
    private long getVPRange(){

        return (long) ((DAYS*range)/getViewPortHandler().getScaleX());
    }
    private long getMidViewPoint(){
        return getMinViewX()*DAYS + range/2;
    }
    private long getMinViewX(){
        return startTime*DAYS + getLowestVisibleXIndex()*DAYS;
    }
    private long getMaxViewX(){
        return startTime*DAYS + getHighestVisibleXIndex()*DAYS;
    }
    private void setleftLabel(String text){




    }
    private void setRightLabel(String text){


    }
    private void setCenterLabel(String text){

        
    }
    public void showAll(){

        fitScreen();
this.invalidate();
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                long min = getMinViewX() - DAYS * 100;
                long max = getMaxViewX() + DAYS * 100;

                visibleRange = max - min;
                SentimentGraphFragment.this.chartVisibleRangeChange(min, max, true);

            }
        }, 300);

    }
}
