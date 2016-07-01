package com.thespeakers_studio.thespeakersstudioapp;

import android.app.Presentation;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 7/1/2016.
 */
public class PresentationListAdapter extends RecyclerView.Adapter<PresentationListViewHolder>
    implements PresentationListViewHolder.OnPresentationCardClickedListener {

    private PresentationListViewHolder.OnPresentationCardClickedListener mHandler;
    private ArrayList<PresentationData> mPresies;

    public PresentationListAdapter(ArrayList<PresentationData> presies,
                                   PresentationListViewHolder.OnPresentationCardClickedListener h) {
        mPresies = presies;
        mHandler = h;
    }

    @Override
    public PresentationListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.presentation_list_item, parent, false);
        PresentationListViewHolder holder = new PresentationListViewHolder(itemView);
        holder.setCardClickListener(this);

        return holder;
    }

    @Override
    public void onBindViewHolder(PresentationListViewHolder holder, int position) {
        holder.setPresentation(mPresies.get(position));
    }

    @Override
    public int getItemCount() {
        return mPresies.size();
    }

    @Override
    public void onPresentationSelected(String presentationId) {
        mHandler.onPresentationSelected(presentationId);
    }
}
