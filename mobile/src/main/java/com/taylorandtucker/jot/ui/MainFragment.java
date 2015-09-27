package com.taylorandtucker.jot.ui;

import android.app.Activity;
import android.content.ContentValues;
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
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.taylorandtucker.jot.Entry;
import com.taylorandtucker.jot.R;
import com.taylorandtucker.jot.localdb.DBContentProvider;
import com.taylorandtucker.jot.localdb.DBUtils;
import com.taylorandtucker.jot.localdb.EntriesContract.Contract;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
        cardFragmentAdapter.add(new CalendarReviewFragment());
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
        if (!entryText.equals("")) {
            final Entry entry = new Entry(entryText.getText().toString());

            //putEntry
            ContentValues values = new ContentValues();
            values.put(Contract.COLUMN_DATE, entry.getCreatedOn().toString());
            values.put(Contract.COLUMN_BODY, entry.getBody());
            values.put(Contract.COLUMN_SENTIMENT, 0);
            Uri uri  = getActivity().getContentResolver().insert(DBContentProvider.CONTENT_URI, values);

            String[] segments = uri.getPath().split("/");
            String idStr = segments[segments.length-1];

            System.out.println(idStr);
            RetrieveNLPdata nlp = new RetrieveNLPdata(idStr, entry.getBody());
            nlp.execute();


            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                entryText.setText("");
            }
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

                try {
                    builder = factory.newDocumentBuilder();
                    is = new InputSource(new StringReader(xml));
                    Document doc = builder.parse(is);


                    NodeList list = doc.getElementsByTagName("sentence");

                    int sentSum = 0;

                    for(int i = 0; i < list.getLength()/2; i++){
                        int val = Integer.valueOf(list.item(0).getAttributes().getNamedItem("sentimentValue").getNodeValue());
                        System.out.println(Integer.toString(i) +": " + Integer.toString(val));
                        sentSum +=(val-2);
                    }

                    ContentValues values = new ContentValues();
                    values.put(Contract.COLUMN_SENTIMENT, sentSum);

                    String[] Values = new String[1];
                    Values[0] = entryID;
                    System.out.println(entryID);
                    getActivity().getContentResolver().update(DBContentProvider.CONTENT_URI, values, "_id" + "= ?", Values);
                    System.out.println("Sent Sum" + Integer.toString(sentSum));

                } catch (ParserConfigurationException e) {
                } catch (SAXException e) {
                } catch (IOException e) {
                }
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
