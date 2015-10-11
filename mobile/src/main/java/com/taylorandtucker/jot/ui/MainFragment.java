package com.taylorandtucker.jot.ui;

import android.animation.Animator;
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
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.taylorandtucker.jot.Entry;
import com.taylorandtucker.jot.NLP.InfoExtractor;
import com.taylorandtucker.jot.NLP.ProcessedEntry;
import com.taylorandtucker.jot.R;
import com.taylorandtucker.jot.localdb.DBContentProvider;
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
    private int LOADER_ID = 1;
    private Context context;
    private SentimentGraphFragment mChart;

    private ImageButton fab;

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

    public MainFragment() {}

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


        //InfoExtractor ie = new InfoExtractor(getActivity());
        //mChart = new SentimentGraphFragment(getContext());
mChart = (SentimentGraphFragment) getActivity().findViewById(R.id.chart);
        //cardFragmentAdapter.add(mChart);
        cardMergeAdapter.addAdapter(cardFragmentAdapter);


        cardCursorAdapter = CardCursorAdapter.getInstance(getContext(), null);
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


        // Set up FAB
        fab = (ImageButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabAnim();
                EditText textEntry = (EditText) getActivity().findViewById(R.id.textEntry);
                textEntry.setFocusableInTouchMode(true);
                textEntry.requestFocus();
            }
        });
//
//        View feed = getActivity().findViewById(R.id.entriesFeed);
//        feed.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                View feed = getActivity().findViewById(R.id.entriesFeed);
//                feed.setFocusableInTouchMode(true);
//                feed.requestFocus();
//            }
//        });

        EditText textEntry = (EditText) getActivity().findViewById(R.id.textEntry);
        textEntry.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        });
    }

    private void textEntryReveal() {
        //setting up circular reveal

        View textEntryLayout = getActivity().findViewById(R.id.textEntryLayout);

        View submitButton = getActivity().findViewById(R.id.submitButton);
        int cx = (submitButton.getLeft() + submitButton.getRight())  / 2;
        int cy = (submitButton.getTop() + submitButton.getBottom())  / 2;

        int startRadius = fab.getWidth()/2;
        int finalRadius = Math.max(textEntryLayout.getWidth(), textEntryLayout.getHeight());
        Animator anim =
                ViewAnimationUtils.createCircularReveal(textEntryLayout, cx, cy, startRadius, finalRadius);
        textEntryLayout.setVisibility(View.VISIBLE);
        anim.start();
    }

    private void fabAnim() {
        //Moving the fab
        int[] fabLoc = {0,0};
        fab.getLocationOnScreen(fabLoc);
        int[] subLoc = {0,0};
        View submitButton = getActivity().findViewById(R.id.submitButton);
        submitButton.getLocationOnScreen(subLoc);
        int subMidx = (submitButton.getLeft() + submitButton.getRight())  / 2;
        int subMidy = (submitButton.getTop() + submitButton.getBottom())  / 2;
        int dx = subMidx - (fab.getLeft() + fab.getRight())/2;
        int dy = subMidy - (fab.getTop() + fab.getBottom())/2;

        TranslateAnimation animation = new TranslateAnimation(0, dx, 0, dy);
        animation.setDuration(150);
        animation.setFillAfter(false);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                textEntryReveal();
                fab.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });

        fab.startAnimation(animation);
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
            DBUtils dbUtils = DBUtils.getInstance(getActivity());
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
        String[] projection = {
                EntryContract._ID,
                EntryContract.COLUMN_DATE,
                EntryContract.COLUMN_BODY,
                EntryContract.COLUMN_SENTIMENT
        };
        String sortOrder = EntryContract._ID + " DESC"; //ordering by descending id (couldn't get date to work)

        CursorLoader cursorLoader = new CursorLoader(getContext(),
                DBContentProvider.ENTRY_URI, projection, null, null, sortOrder);
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
                try {
                    wait(3000);
                }catch (Exception e){

                }

                final List entries = ie.getAllEntries();

                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                //cardFragmentAdapter.remove(0);
                                //mChart = null;
                                //mChart = new SentimentGraphFragment(getActivity());
                                //cardFragmentAdapter.add(mChart);
                                mChart.updateData(entries);
                                //mChart.invalidate();
                                //cardFragmentAdapter.notifyDataSetChanged();
                                //cardMergeAdapter.notifyDataSetChanged();
                                //cardMergeAdapter.notifyDataSetInvalidated();

                                //cardFragmentAdapter.getView(0,null,null).invalidate();
                                //System.out.println("hereeeee");

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

}
