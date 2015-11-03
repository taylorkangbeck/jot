package com.taylorandtucker.jot.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;

import com.taylorandtucker.jot.Entry;
import com.taylorandtucker.jot.NLP.DemoHelper;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

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

    private ImageButton fab;
    private FrameLayout invisFrame;

    private InfoExtractor ie;

    private long minTimeChart = 0;
    private long maxTimeChart = Long.MAX_VALUE;

    private String POS_EMOJI = new String(Character.toChars(0x1F601));
    private String NEG_EMOJI = new String(Character.toChars(0x1F620));

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

        ie = new InfoExtractor(getActivity());
        View rootView;
        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
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
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View rootView;

        //merging adapters for the entries feed
        cardMergeAdapter = new CardMergeAdapter();
        cardFragmentAdapter = new CardFragmentAdapter(getContext());

        cardMergeAdapter.addAdapter(cardFragmentAdapter);


        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
            case 1:

                getLoaderManager().initLoader(LOADER_ID, null, this);

                final ListView entriesFeed = (ListView) getActivity().findViewById(R.id.entriesFeed);

                mChart = (SentimentGraphFragment) getActivity().findViewById(R.id.chart);

                if (ie.getAllEntries().size() <= 2) {
                    DemoHelper dh = new DemoHelper(60, 4 * 24 * 60 * 60, getActivity(), mChart);
                    System.out.println("DEMO HELPER FINISHED MAKING CALLS");
                }
                mChart.updateData(ie.getAllEntries());

                mChart.addVPListener(new SentimentGraphFragment.GraphVPListener() {
                    @Override
                    public void onNodeSelected(long startOfDay, long endOfDay) {

                        for (int i = 0; i < cardMergeAdapter.getCount(); i++) {
                            Cursor c = (Cursor) cardMergeAdapter.getItem(i);

                            long entryTime = 1000 * c.getLong(c.getColumnIndexOrThrow(EntryContract.COLUMN_DATE));

                            final int index = i;
                            if (entryTime <= endOfDay && entryTime >= startOfDay) {
                                entriesFeed.setSelectionFromTop(i, 0);
                            }
                        }
                    }

                    @Override
                    public void onVPRangeChange(long startDate, long endDate) {
                        minTimeChart = startDate;
                        maxTimeChart = endDate;
                        getLoaderManager().restartLoader(LOADER_ID, null, MainFragment.this);
                        entriesFeed.smoothScrollToPosition(0);
                    }
                });

                cardCursorAdapter = new CardCursorAdapter(getContext(), null);
                cardMergeAdapter.addAdapter(cardCursorAdapter);
                entriesFeed.setAdapter(cardMergeAdapter);

                setupEmojiButtons();
                setupFAB();
                break;
            case 2:

                getLoaderManager().initLoader(LOADER_ID, null, this);

                final ListView entitiesFeed = (ListView) getActivity().findViewById(R.id.entitiesList);

                entityCardCursorAdapter = new EntityCardCursorAdapter(getContext(), null);
                cardMergeAdapter.addAdapter(entityCardCursorAdapter);
                entitiesFeed.setAdapter(cardMergeAdapter);

                setupFAB();

                break;
            case 3:

                break;
            default:
        }
    }

    private void setupEmojiButtons(){
        Button pos = (Button) getActivity().findViewById(R.id.addPosEmoji);
        Button neg = (Button) getActivity().findViewById(R.id.addNegEmoji);

        pos.setText(POS_EMOJI);
        neg.setText(NEG_EMOJI);
        pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeEmoji(POS_EMOJI);
            }
        });
        neg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeEmoji(NEG_EMOJI);
            }
        });

    }
    private void writeEmoji(String emojiChars){
        EditText textEntry = (EditText) getActivity().findViewById(R.id.textEntry);
        textEntry.getText().insert(textEntry.getSelectionStart(), emojiChars);
    }
    private void setupFAB() {
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
                //fabAnim();
                textEntryReveal(); //instead
                fab.setVisibility(View.INVISIBLE); //instead
                invisFrame.setVisibility(View.VISIBLE);

                EditText textEntry = (EditText) getActivity().findViewById(R.id.textEntry);
                textEntry.setFocusableInTouchMode(true);
                textEntry.requestFocus();

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(textEntry, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        //Set up invis framelayout, onclick hides textentry if it's visible
        invisFrame = (FrameLayout) getActivity().findViewById(R.id.invisFrame);
        invisFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent me) {
                View textEntryLayout = getActivity().findViewById(R.id.textEntryLayout);

                if (textEntryLayout.getVisibility() == View.VISIBLE) {
                    textEntryHide();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return false;
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

    @Override
    public void onResume() {
        super.onResume();
        if (mChart != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChart.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mChart.updateData(ie.getAllEntries());
                            mChart.setGradient();
                            System.out.println("2 delay resume");
                        }
                    }, 30);
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mChart != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChart.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mChart.updateData(ie.getAllEntries());
                            mChart.setGradient();
                            System.out.println("2 delay start");
                        }
                    }, 30);
                }
            });
        }

    }

    private void textEntryReveal() {
        //setting up circular reveal

        View textEntryLayout = getActivity().findViewById(R.id.textEntryLayout);

        View submitButton = getActivity().findViewById(R.id.submitButton);
        int cx = (submitButton.getLeft() + submitButton.getRight()) / 2;
        int cy = (submitButton.getTop() + submitButton.getBottom()) / 2;

        int startRadius = fab.getWidth() / 2;
        int finalRadius = Math.max(textEntryLayout.getWidth(), textEntryLayout.getHeight());
        Animator anim =
                ViewAnimationUtils.createCircularReveal(textEntryLayout, cx, cy, startRadius, finalRadius);
        textEntryLayout.setVisibility(View.VISIBLE);
        anim.start();
    }

    private void textEntryHide() {
        final View myView = getActivity().findViewById(R.id.textEntryLayout);

        View submitButton = getActivity().findViewById(R.id.submitButton);
        int cx = (submitButton.getLeft() + submitButton.getRight()) / 2;
        int cy = (submitButton.getTop() + submitButton.getBottom()) / 2;

        // get the initial radius for the clipping circle
        int initialRadius = myView.getWidth() / 2;

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                myView.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.VISIBLE);
                invisFrame.setVisibility(View.INVISIBLE);
            }
        });

        // start the animation
        anim.start();
    }

    private void fabAnim() {
        //Moving the fab
        int[] fabLoc = {0, 0};
        fab.getLocationOnScreen(fabLoc);
        int[] subLoc = {0, 0};
        View submitButton = getActivity().findViewById(R.id.submitButton);
        submitButton.getLocationOnScreen(subLoc);
        int subMidx = (submitButton.getLeft() + submitButton.getRight()) / 2;
        int subMidy = (submitButton.getTop() + submitButton.getBottom()) / 2;
        int dx = subMidx - (fab.getLeft() + fab.getRight()) / 2;
        int dy = subMidy - (fab.getTop() + fab.getBottom()) / 2;

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

            Uri uri = ie.putEntry(entry);
            String[] segments = uri.getPath().split("/");
            String idStr = segments[segments.length - 1];


            RetrieveNLPdata nlp = new RetrieveNLPdata(idStr, entry.getBody());
            nlp.execute();
        }
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            entryText.setText("");
            textEntryHide();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection;
        String sortOrder;
        String selection;
        String[] selectionArgs;
        Uri uri;


        View rootView;
        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
            case 1:
                projection = DBUtils.entryProjection;
                sortOrder = EntryContract.COLUMN_DATE + " DESC";
                uri = DBContentProvider.ENTRY_URI;
                selection = EntryContract.COLUMN_DATE + " BETWEEN ? and ? ";
                selectionArgs = new String[] {Long.toString(minTimeChart / 1000), Long.toString(maxTimeChart/1000)};

                break;
            case 2:
                projection = DBUtils.entityProjection;
                sortOrder = EntityContract.COLUMN_IMPORTANCE + " DESC";
                uri = DBContentProvider.ENTITY_URI;
                selection = null;
                selectionArgs = null;
                break;
            case 3:
                projection = DBUtils.entityProjection;
                sortOrder = EntityContract.COLUMN_IMPORTANCE + " DESC";
                uri = DBContentProvider.ENTITY_URI;
                selection = null;
                selectionArgs = null;
                break;
            default:
                projection = DBUtils.entityProjection;
                sortOrder = EntityContract.COLUMN_IMPORTANCE + " DESC";
                uri = DBContentProvider.ENTITY_URI;
                selection = null;
                selectionArgs = null;

        }

        CursorLoader cursorLoader = new CursorLoader(getContext(), uri, projection, selection, selectionArgs, sortOrder);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
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
        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
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
            String line = "";
            String xml = "";

            try {
                HttpEntity entity = new ByteArrayEntity(entry.getBytes("UTF-8"));
                httppost.setEntity(entity);

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                while ((line = rd.readLine()) != null) {
                    xml += line;
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
            final ProcessedEntry ent = new ProcessedEntry(xml, entry);


            InfoExtractor ie = new InfoExtractor(getActivity());

            ie.processNewEntryData(Long.parseLong(entryID), ent);

            final List entries = ie.getAllEntries();


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mChart != null) {

                        mChart.updateData(entries);
                    }
                }
            });
            System.out.println("===================== ENTITIES =======================");

            Map<String, Integer> entityMap = ent.personSentiment();


            for (Map.Entry<String, Integer> a : entityMap.entrySet()) {
                System.out.println(a.getKey() + " : " + a.getValue());
            }
            System.out.println("entity count : " + ie.getAllEntitiesByImportance().size());


            System.out.println("===================== ENTRIES =======================");

                /*
                    DBUtils utils = DBUtils.getInstance(getContext());
                    cardCursorAdapter.swapCursor(utils.getAllEntriesQuery());
                */

            return null;

        }

    }


    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

}
