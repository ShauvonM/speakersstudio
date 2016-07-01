package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by smcgi_000 on 7/1/2016.
 */
public class PresentationListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private PresentationData mPresentation;
    private CardView mCard;
    private OnPresentationCardClickedListener mListener;

    public PresentationListViewHolder(View v) {
        super(v);
        mCard = (CardView) v;
        mListener = null;
    }

    public void setCardClickListener (OnPresentationCardClickedListener l) {
        mListener = l;
    }

    public void setPresentation(PresentationData pres) {
        mPresentation = pres;

        ((TextView) mCard.findViewById(R.id.presentation_name)).setText(mPresentation.getTopic());
        ((TextView) mCard.findViewById(R.id.presentation_modified)).setText(mPresentation.getModifiedDate());

        mCard.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onPresentationSelected(mPresentation.getId());
        }
    }

    public interface OnPresentationCardClickedListener {
        public void onPresentationSelected(String presentationId);
    }
}
