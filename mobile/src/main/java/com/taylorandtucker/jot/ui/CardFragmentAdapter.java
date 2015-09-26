package com.taylorandtucker.jot.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by Taylor on 9/26/2015.
 */
public class CardFragmentAdapter extends BaseAdapter {

    private ArrayList<IFragmentCard> fragmentCards;
    private Context context;

    public CardFragmentAdapter(Context context) {
        this.context = context;
        fragmentCards = new ArrayList<IFragmentCard>();
    };

    public void add(IFragmentCard fragmentCard) {
        fragmentCards.add(fragmentCard);
    }

    public IFragmentCard getItem(int position) {
        return fragmentCards.get(position);
    }

    public long getItemId(int position) {
        return fragmentCards.get(position).getLayoutResourceId(); //TODO idkwtf
    }

    public int getCount() {
        return fragmentCards.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        IFragmentCard p = getItem(position);
        if (p != null) {
            if (convertView == null) {

                return LayoutInflater.from(this.context).inflate(p.getLayoutResourceId(), null);
            }
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

}
