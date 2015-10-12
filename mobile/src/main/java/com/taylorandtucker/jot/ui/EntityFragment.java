package com.taylorandtucker.jot.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.taylorandtucker.jot.NLP.InfoExtractor;
import com.taylorandtucker.jot.R;
import com.taylorandtucker.jot.localdb.EntityCursorLoader;
import com.taylorandtucker.jot.localdb.jotDBHelper;

/**
 * Created by Taylor on 9/16/-015.
 */
public class EntityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
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

    static private long entityId;
    private String entityName;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */

    public EntityFragment() {

    }
    public EntityFragment(Bundle data){
        entityId = data.getLong("entityId");
        entityName = data.getString("entityName");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;

        rootView = inflater.inflate(R.layout.fragment_entity, container, false);

        context = getContext();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        InfoExtractor ie = new InfoExtractor(getActivity());

        //merging adapters for the entries feed
        cardMergeAdapter = new CardMergeAdapter();
        cardFragmentAdapter = new CardFragmentAdapter(getContext());

        cardMergeAdapter.addAdapter(cardFragmentAdapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        final ListView entriesFeed = (ListView) getActivity().findViewById(R.id.entriesFeed);

        TextView entityNameView = (TextView) getActivity().findViewById(R.id.entityNameView);
        entityNameView.setText(entityName);

        mChart = (SentimentGraphFragment) getActivity().findViewById(R.id.chartE);
        mChart.updateData(ie.getEntriesForEntity(entityId));

        cardCursorAdapter = new CardCursorAdapter(getContext(), null);
        cardMergeAdapter.addAdapter(cardCursorAdapter);
        entriesFeed.setAdapter(cardMergeAdapter);

    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        EntityCursorLoader entityCursorLoader = new EntityCursorLoader(getContext(), new jotDBHelper(getContext()), entityId);

        return entityCursorLoader;
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
