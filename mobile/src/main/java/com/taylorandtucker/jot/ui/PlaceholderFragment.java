package com.taylorandtucker.jot.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.taylorandtucker.jot.Entry;
import com.taylorandtucker.jot.R;
import com.taylorandtucker.jot.localdb.DBContentProvider;
import com.taylorandtucker.jot.localdb.EntriesContract.Contract;

/**
 * Created by Taylor on 9/16/2015.
 */
public class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private CardCursorAdapter cardCursorAdapter;
    private int LOADER_ID = 1;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceholderFragment() {
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

        //entries feed adapter
        getLoaderManager().initLoader(LOADER_ID, null, this);
        final ListView entriesFeed = (ListView) getActivity().findViewById(R.id.listView);
        cardCursorAdapter = new CardCursorAdapter(getContext(), null);
        entriesFeed.setAdapter(cardCursorAdapter);

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
        final Entry entry = new Entry(entryText.getText().toString());

        //putEntry
        ContentValues values = new ContentValues();
        values.put(Contract._ID, entry.getId());
        values.put(Contract.COLUMN_DATE, entry.getCreatedOn().toString());
        values.put(Contract.COLUMN_BODY, entry.getBody());
        getActivity().getContentResolver().insert(DBContentProvider.CONTENT_URI, values);

        
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                Contract._ID,
                Contract.COLUMN_DATE,
                Contract.COLUMN_BODY
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


}
