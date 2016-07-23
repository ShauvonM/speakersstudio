package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * Created by smcgi_000 on 7/1/2016.
 */
public class PresentationListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {
    private PresentationData mPresentation;
    private CardView mCard;
    private OnPresentationCardClickedListener mListener;

    public PresentationListViewHolder(View v) {
        super(v);
        mCard = (CardView) v;
        mListener = null;

        mCard.findViewById(R.id.presentation_submenu).setOnClickListener(this);
    }

    public void setCardClickListener (OnPresentationCardClickedListener l) {
        mListener = l;
    }

    public void setPresentation(PresentationData pres) {
        mPresentation = pres;
        Resources res = mCard.getResources();

        ((TextView) mCard.findViewById(R.id.presentation_name)).setText(mPresentation.getTopic());

        ((TextView) mCard.findViewById(R.id.presentation_modified)).setText(
                String.format(res.getString(R.string.modified_output),
                        mPresentation.getModifiedDate())
        );

        ((TextView) mCard.findViewById(R.id.presentation_progress)).setText(
                String.format(res.getString(R.string.progress_output),
                        (int)(mPresentation.getCompletionPercentage() * 100))
        );

        setBackground();

        mCard.setOnClickListener(this);
        mCard.setOnLongClickListener(this);
    }

    private void setBackground() {
        if (mPresentation.getIsSelected()) {
            mCard.setCardBackgroundColor(ContextCompat.getColor(mCard.getContext(), R.color.colorAccent));
        } else {
            mCard.setCardBackgroundColor(ContextCompat.getColor(mCard.getContext(), R.color.colorPrimary));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.presentation_submenu) {
            PopupMenu popup = new PopupMenu(v.getContext(),
                    mCard.findViewById(R.id.presentation_submenu));
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_presentation_oncard, popup.getMenu());
            popup.setOnMenuItemClickListener(this);

            if (mPresentation.getCompletionPercentage() < 1) {
                popup.getMenu().findItem(R.id.menu_action_practice).setVisible(false);
            } else {
                popup.getMenu().findItem(R.id.menu_action_practice).setVisible(true);
            }

            popup.show();
        } else {
            if (mListener != null) {
                boolean done = mListener.onPresentationOpened(mPresentation.getId());
                if (!done) {
                    onLongClick(v);
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mPresentation.getIsSelected()) {
            mPresentation.deselect();
            if (mListener != null) {
                mListener.onPresentationDeselected(mPresentation.getId());
            }
        } else {
            mPresentation.select();
            if (mListener != null) {
                mListener.onPresentationSelected(mPresentation.getId());
            }
        }
        setBackground();
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_action_practice:
                if (mListener != null) {
                    mListener.onPresentationPracticeSelected(mPresentation.getId());
                }
                return true;
            case R.id.menu_action_delete:
                if (mListener != null) {
                    mListener.onPresentationDeleteSelected(mPresentation.getId());
                }
                return true;
            default:
                return false;
        }
    }

    public interface OnPresentationCardClickedListener {
        public void onPresentationSelected(String presentationId);
        public void onPresentationDeselected(String presentationId);
        public boolean onPresentationOpened(String presentationId);
        public void onPresentationPracticeSelected(String presentationId);
        public void onPresentationDeleteSelected(String presentationId);
    }
}
