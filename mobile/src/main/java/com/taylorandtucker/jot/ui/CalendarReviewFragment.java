package com.taylorandtucker.jot.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.taylorandtucker.jot.R;

/**
 * Created by Taylor on 9/26/2015.
 */
public class CalendarReviewFragment extends Fragment implements IFragmentCard {
    private final int layoutId = R.layout.fragment_calendar_review;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(layoutId, container, false);


        return rootView;
    }

    @Override
    public int getLayoutResourceId()
    {
        return layoutId;
    }

}
