package com.taylorandtucker.jot.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.taylorandtucker.jot.Entry;
import com.taylorandtucker.jot.NLP.InfoExtractor;
import com.taylorandtucker.jot.NLP.ProcessedEntry;
import com.taylorandtucker.jot.R;
import com.taylorandtucker.jot.localdb.DBContentProvider;
import com.taylorandtucker.jot.localdb.DBContract.EntityContract;
import com.taylorandtucker.jot.localdb.DBContract.EntryContract;
import com.taylorandtucker.jot.localdb.DBUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

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
    private EntityCardCursorAdapter entityCardCursorAdapter;
    private int LOADER_ID = 1;
    private Context context;
    private SentimentGraphFragment mChart;

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

        View rootView;
        switch (getArguments().getInt(ARG_SECTION_NUMBER)){
            case 1:
                System.out.println("CASE1");
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
                break;
            case 2:
                System.out.println("CASE2");
                rootView = inflater.inflate(R.layout.fragment_entities_list, container, false);
                break;
            case 3:
                System.out.println("CASE3");
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
                break;
            default:
                rootView = inflater.inflate(R.layout.fragment_main, container, false);

        }

        context = getContext();
        setupUIKeyboardDisapear(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        InfoExtractor ie = new InfoExtractor(getActivity());
        View rootView;

        //merging adapters for the entries feed
        cardMergeAdapter = new CardMergeAdapter();
        cardFragmentAdapter = new CardFragmentAdapter(getContext());

        cardMergeAdapter.addAdapter(cardFragmentAdapter);


        switch (getArguments().getInt(ARG_SECTION_NUMBER)){
            case 1:

                getLoaderManager().initLoader(LOADER_ID, null, this);

                final ListView entriesFeed = (ListView) getActivity().findViewById(R.id.entriesFeed);

                mChart = (SentimentGraphFragment) getActivity().findViewById(R.id.chart);
                mChart.updateData(ie.getAllEntries());

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
                break;
            case 2:

                getLoaderManager().initLoader(LOADER_ID, null, this);

                final ListView entitiesFeed = (ListView) getActivity().findViewById(R.id.entitiesList);

                entityCardCursorAdapter = new EntityCardCursorAdapter(getContext(), null);
                cardMergeAdapter.addAdapter(entityCardCursorAdapter);
                entitiesFeed.setAdapter(cardMergeAdapter);
                break;
            case 3:

                break;
            default:
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private void onSubmit() {
        EditText entryText = (EditText) getActivity().findViewById(R.id.textEntry);
        if (!entryText.toString().equals("")) {

            final Entry entry = new Entry(entryText.getText().toString());

            InfoExtractor ie = new InfoExtractor(getActivity());

            Uri uri = ie.putEntry(entry);
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
        String[] projection;
        String sortOrder;
        Uri uri;

        View rootView;
        switch (getArguments().getInt(ARG_SECTION_NUMBER)){
            case 1:
                projection = DBUtils.entryProjection;
                sortOrder = EntryContract._ID + " DESC";
                uri = DBContentProvider.ENTRY_URI;
                break;
            case 2:
               projection = DBUtils.entityProjection;
                sortOrder = EntityContract.COLUMN_IMPORTANCE + " DESC";
                uri = DBContentProvider.ENTITY_URI;
                break;
            case 3:
                projection = DBUtils.entityProjection;
                sortOrder = EntityContract.COLUMN_IMPORTANCE + " DESC";
                uri = DBContentProvider.ENTITY_URI;
                break;
            default:
                projection = DBUtils.entityProjection;
                sortOrder = EntityContract.COLUMN_IMPORTANCE + " DESC";
                uri = DBContentProvider.ENTITY_URI;

        }

        CursorLoader cursorLoader = new CursorLoader(getContext(), uri, projection, null, null, sortOrder);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (getArguments().getInt(ARG_SECTION_NUMBER)){
            case 1:
                cardCursorAdapter.swapCursor(data);
                break;
            case 2:
                entityCardCursorAdapter.swapCursor(data);
                break;
            case 3:
                cardCursorAdapter.swapCursor(data);
                break;
            default:
                cardCursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (getArguments().getInt(ARG_SECTION_NUMBER)){
            case 1:
                cardCursorAdapter.swapCursor(null);
                break;
            case 2:
                entityCardCursorAdapter.swapCursor(null);
                break;
            case 3:
                cardCursorAdapter.swapCursor(null);
                break;
            default:
                cardCursorAdapter.swapCursor(null);
        }

    }

    class RetrieveNLPdata extends AsyncTask<Void, Void, Void> {

        private String entryID;
        private String entry;

        public RetrieveNLPdata(String entryID, String entry) {
            this.entryID = entryID;
            this.entry = entry;
        }

        protected Void doInBackground(Void... param) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://54.173.123.6:8000/entry");

            try {
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

                final ProcessedEntry ent = new ProcessedEntry(xml);


                 InfoExtractor ie = new InfoExtractor(getActivity());
                ie.processNewEntryData(Long.parseLong(entryID), ent);

                final List entries = ie.getAllEntries();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChart.updateData(entries);
                    }
                });

                System.out.println("===================== ENTITIES =======================");



                System.out.println("===================== ENTRIES =======================");

                /*
                    DBUtils utils = DBUtils.getInstance(getContext());
                    cardCursorAdapter.swapCursor(utils.getAllEntriesQuery());
                */

                return null;

            } catch (ClientProtocolException e) {
                System.out.println(e);
                return null;
                // TODO Auto-generated catch block
            } catch (IOException e) {
                System.out.println(e);
                return null;
            // TODO Auto-generated catch block
            }

        }

    }
    public void setupUIKeyboardDisapear(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUIKeyboardDisapear(innerView);
            }
        }
    }
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
