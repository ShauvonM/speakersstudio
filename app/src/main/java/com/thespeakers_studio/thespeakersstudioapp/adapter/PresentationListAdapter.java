package com.thespeakers_studio.thespeakersstudioapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thespeakers_studio.thespeakersstudioapp.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.ui.PresentationListViewHolder;

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
        for (PresentationData pres : mPresies) {
            pres.deselect();
        }
        mSelectionCount = 0;
        notifyDataSetChanged();
    }

    public ArrayList<PresentationData> getSelectedPresentations () {
        ArrayList<PresentationData> presies = new ArrayList<>();
        for (PresentationData pres : mPresies) {
            if (pres.getIsSelected()) {
                presies.add(pres);
            }
        }
        return presies;
    }

    public void setPresentations(ArrayList<PresentationData> presentations) {
        mPresies = presentations;
        mSelectionCount = 0;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mPresies.size();
    }

    @Override
    public void onPresentationSelected(PresentationData presentation) {
        mSelectionCount++;
        presentation.select();
        mHandler.onPresentationSelected(presentation);
    }

    @Override
    public void onPresentationDeselected(PresentationData presentation) {
        mSelectionCount--;
        presentation.deselect();
        mHandler.onPresentationDeselected(presentation);
    }

    @Override
    public boolean onPresentationOpened(PresentationData presentation) {
        if (mSelectionCount == 0) {
            return mHandler.onPresentationOpened(presentation);
        } else {
            return false;
        }
    }

    @Override
    public void onPresentationPracticeSelected(PresentationData presentation) {
        mHandler.onPresentationPracticeSelected(presentation);
    }

    @Override
    public void onPresentationDeleteSelected(PresentationData presentation) {
        mHandler.onPresentationDeleteSelected(presentation);
    }
}
