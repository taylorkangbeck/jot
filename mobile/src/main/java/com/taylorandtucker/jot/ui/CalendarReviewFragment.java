package com.taylorandtucker.jot.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
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

        GraphView graph = (GraphView) getActivity().findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);
        return rootView;
    }

    @Override
    public int getLayoutResourceId()
    {
        return layoutId;
    }

}
