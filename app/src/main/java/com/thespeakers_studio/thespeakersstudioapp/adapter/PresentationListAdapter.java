package com.thespeakers_studio.thespeakersstudioapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thespeakers_studio.thespeakersstudioapp.PresentationListViewHolder;
import com.thespeakers_studio.thespeakersstudioapp.R;

import java.util.ArrayList;

import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;

/**
 * Created by smcgi_000 on 7/1/2016.
 */
public class PresentationListAdapter extends RecyclerView.Adapter<PresentationListViewHolder>
    implements PresentationListViewHolder.OnPresentationCardClickedListener {

    private PresentationListViewHolder.OnPresentationCardClickedListener mHandler;
    private ArrayList<PresentationData> mPresies;

    private boolean mIsTwoColumn;
    private int mSelectionCount;

    public PresentationListAdapter(ArrayList<PresentationData> presies,
                                   PresentationListViewHolder.OnPresentationCardClickedListener h) {
        mPresies = presies;
        mHandler = h;

        mIsTwoColumn = false;
        mSelectionCount = 0;
    }

    public void setIsTwoColumn(boolean is) {
        mIsTwoColumn = is;
    }

    @Override
    public PresentationListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (mIsTwoColumn) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.presentation_list_item_two_column, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.presentation_list_item_one_column, parent, false);
        }
        PresentationListViewHolder holder = new PresentationListViewHolder(itemView);
        holder.setCardClickListener(this);

        return holder;
    }

    @Override
    public void onBindViewHolder(PresentationListViewHolder holder, int position) {
        holder.setPresentation(mPresies.get(position));
    }

    public int getSelectedCount() {
        return mSelectionCount;
    }
    public void deselectAll() {
        mSelectionCount = 0;
    }

    @Override
    public int getItemCount() {
        return mPresies.size();
    }

    @Override
    public void onPresentationSelected(String presentationId) {
        mSelectionCount++;
        mHandler.onPresentationSelected(presentationId);
    }

    @Override
    public void onPresentationDeselected(String presentationId) {
        mSelectionCount--;
        mHandler.onPresentationDeselected(presentationId);
    }

    @Override
    public boolean onPresentationOpened(String presentationId) {
        if (mSelectionCount == 0) {
            return mHandler.onPresentationOpened(presentationId);
        } else {
            return false;
        }
    }

    @Override
    public void onPresentationPracticeSelected(String presentationId) {
        mHandler.onPresentationPracticeSelected(presentationId);
    }

    @Override
    public void onPresentationDeleteSelected(String presentationId) {
        mHandler.onPresentationDeleteSelected(presentationId);
    }
}
