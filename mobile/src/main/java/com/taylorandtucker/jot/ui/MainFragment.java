package com.taylorandtucker.jot.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.taylorandtucker.jot.Entry;
import com.taylorandtucker.jot.NLP.ProcessedEntry;
import com.taylorandtucker.jot.R;
import com.taylorandtucker.jot.localdb.DBContentProvider;
import com.taylorandtucker.jot.localdb.EntriesContract.Contract;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Taylor on 9/16/-015.
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private CardMergeAdapter cardMergeAdapter;
    private CardFragmentAdapter cardFragmentAdapter;
    private CardCursorAdapter cardCursorAdapter;
    private int LOADER_ID = 1;
    private Context context;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainFragment newInstance(int sectionNumber) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        context = getContext();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        final ListView entriesFeed = (ListView) getActivity().findViewById(R.id.entriesFeed);

        //merging adapters for the entries feed
        cardMergeAdapter = new CardMergeAdapter();
        cardFragmentAdapter = new CardFragmentAdapter(getContext());
        //cardFragmentAdapter.add(new CalendarReviewFragment());
        cardMergeAdapter.addAdapter(cardFragmentAdapter);

        cardCursorAdapter = new CardCursorAdapter(getContext(), null);
        cardMergeAdapter.addAdapter(cardCursorAdapter);
        entriesFeed.setAdapter(cardMergeAdapter);

        // submit listener
        Button submitButton = (Button) getActivity().findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });

        final GraphView graph = (GraphView) getActivity().findViewById(R.id.graph);

        // generate Dates
        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();
        System.out.println(d1.getTime());
        Date date1 = new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime();
        Date date2 = new GregorianCalendar(2014, Calendar.FEBRUARY, 12).getTime();
        Date date3 = new GregorianCalendar(2014, Calendar.FEBRUARY, 27).getTime();
        Date date4 = new GregorianCalendar(2014, Calendar.APRIL, 11).getTime();
        Date date5 = new GregorianCalendar(2015, Calendar.FEBRUARY, 11).getTime();
        Date date6 = new GregorianCalendar(2017, Calendar.FEBRUARY, 11).getTime();


        DataPoint[] entryList = new DataPoint[]{};
        List<DataPoint> l1 = new ArrayList();
        List l2 = new ArrayList();

        long now = calendar.getTimeInMillis();
        for (int i = 0; i < 365; i++){
            l1.add(new DataPoint(new Date(now), rand(-2, 2)));
            now += rand(1000*60*60*12, 1000*60*60*48);
        }


// you can directly pass Date objects to DataPoint-Constructor
// this will convert the Date to double via Date#getTime()git
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(l1.toArray(new DataPoint[l1.size()]));
        series.setDataPointsRadius(10);
        series.setColor(Color.GREEN);
        graph.addSeries(series);

        PointsGraphSeries<DataPoint> series2 = new PointsGraphSeries<DataPoint>(l1.toArray(new DataPoint[l1.size()]));
        series2.setShape(PointsGraphSeries.Shape.POINT);

        series2.setColor(Color.BLUE);

        graph.addSeries(series2);
// set manual x bounds to have nice steps


    }
private int rand(int Min, int Max){
    return Min + (int)(Math.random() * ((Max - Min) + 1));
}
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private void onSubmit()
    {

        EditText entryText = (EditText) getActivity().findViewById(R.id.textEntry);
        System.out.println(":"+entryText.getText()+":");
        if (entryText.getText().length() >=1) {
            final Entry entry = new Entry(entryText.getText().toString());

            //putEntry
            ContentValues values = new ContentValues();
            values.put(Contract.COLUMN_DATE, entry.getCreatedOn().toString());
            values.put(Contract.COLUMN_BODY, entry.getBody());
            values.put(Contract.COLUMN_SENTIMENT, 0);
            Uri uri  = getActivity().getContentResolver().insert(DBContentProvider.CONTENT_URI, values);

            String[] segments = uri.getPath().split("/");
            String idStr = segments[segments.length-1];


            RetrieveNLPdata nlp = new RetrieveNLPdata(idStr, entry.getBody());
            nlp.execute();

        }
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            entryText.setText("");
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                Contract._ID,
                Contract.COLUMN_DATE,
                Contract.COLUMN_BODY,
                Contract.COLUMN_SENTIMENT
        };
        String sortOrder = Contract._ID + " DESC"; //ordering by descending id (couldn't get date to work)

        CursorLoader cursorLoader = new CursorLoader(getContext(),
                DBContentProvider.CONTENT_URI, projection, null, null, sortOrder);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cardCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cardCursorAdapter.swapCursor(null);
    }

    class RetrieveNLPdata extends AsyncTask<Void, Void, Void> {

        private String entryID;
        private String entry;
        public RetrieveNLPdata(String entryID, String entry){
            this.entryID = entryID;
            this.entry = entry;
        }
        protected Void doInBackground(Void... param) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://10.66.235.118:8000/entry");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                HttpEntity entity = new ByteArrayEntity(entry.getBytes("UTF-8"));
                httppost.setEntity(entity);

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                String xml = "";


                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder;
                InputSource is;

                while ((line = rd.readLine()) != null) {
                    xml += line;
                }

                ProcessedEntry ent = new ProcessedEntry(xml);
                double sentSum = ent.getEntrySentiment();
                ContentValues values = new ContentValues();

                values.put(Contract.COLUMN_SENTIMENT, sentSum);
                String[] Values = new String[1];
                Values[0] = entryID;

                getActivity().getContentResolver().update(DBContentProvider.CONTENT_URI, values, "_id" + "= ?", Values);
                System.out.println("Sent Sum " + Double.toString(sentSum));

                System.out.println("{ Person: Sentiment }Map --> " + ent.personSentiment().toString());
                System.out.println("{ Location: Sentiment }Map --> " + ent.locationSentiment().toString());

                String weHate = "";
                String weLike = "";
                Map<String, Integer> psMap = ent.personSentiment();
                for(Map.Entry<String, Integer> entry: psMap.entrySet()){
                    if(entry.getValue() >= 1)
                        weLike = entry.getKey();
                    if(entry.getValue() <= -1)
                        weHate = entry.getKey();
                }

                final String like = weLike;
                final String hate = weHate;
                if(weLike != "") {
                    Handler handler = new Handler(context.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "mmm. We like " + like , Toast.LENGTH_LONG).show();
                        }
                    });
                }
                if(weHate != "") {
                    Handler handler = new Handler(context.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "WE HATE " + hate + "!", Toast.LENGTH_LONG).show();
                        }
                    });
                }


            } catch (ClientProtocolException e) {
                System.out.println(e);
                return null;
                // TODO Auto-generated catch block
            } catch (IOException e) {
                System.out.println(e);
                return null;
            // TODO Auto-generated catch block
            }
            return null;
        }

    }

}
