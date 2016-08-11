package com.thespeakers_studio.thespeakersstudioapp.ui;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.thebluealliance.spectrum.SpectrumDialog;
import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.utils.PresentationUtils;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

/**
 * Created by smcgi_000 on 7/1/2016.
 */
public class PresentationListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {
    private PresentationData mPresentation;
    private CardView mCard;
    private OnPresentationCardClickedListener mListener;

    public PresentationListViewHolder(View v) {
        super(v);
        if (v instanceof CardView) {
            mCard = (CardView) v;
            mCard.findViewById(R.id.presentation_submenu).setOnClickListener(this);
        }
        mListener = null;

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
        if (mCard != null) {
            if (mPresentation.getIsSelected()) {
                mCard.setCardBackgroundColor(
                        ContextCompat.getColor(mCard.getContext(), R.color.presentation_selected_bg));
            } else {
                mCard.setCardBackgroundColor(mPresentation.getColor());
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.presentation_submenu) {
            PopupMenu popup = new PopupMenu(v.getContext(),
                    mCard.findViewById(R.id.presentation_submenu));
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_in_presentation, popup.getMenu());
            popup.setOnMenuItemClickListener(this);

            if (mPresentation.getCompletionPercentage() < 1) {
                popup.getMenu().findItem(R.id.menu_action_practice).setVisible(false);
            } else {
                popup.getMenu().findItem(R.id.menu_action_practice).setVisible(true);
            }

            popup.show();
        } else {
            if (mListener != null) {
                boolean done = mListener.onPresentationOpened(mPresentation);
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
                mListener.onPresentationDeselected(mPresentation);
            }
        } else {
            mPresentation.select();
            if (mListener != null) {
                mListener.onPresentationSelected(mPresentation);
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
                    mListener.onPresentationPracticeSelected(mPresentation);
                }
                return true;
            case R.id.menu_action_delete:
                if (mListener != null) {
                    mListener.onPresentationDeleteSelected(mPresentation);
                }
                return true;
            case R.id.menu_action_color:
                PresentationUtils.showColorDialog((AppCompatActivity) mCard.getContext(), mPresentation,
                    new SpectrumDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                            mPresentation.setColor(color);
                            setBackground();
                            if (mListener != null) {
                                mListener.onPresentationColorChange(mPresentation, color);
                            }
                        }
                    }
                );
                return true;
            case R.id.menu_action_reset:
                PresentationUtils.resetPresentation(mCard.getContext(),
                        mPresentation, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mListener != null) {
                                    mListener.onPresentationReset(mPresentation);
                                    setPresentation(mPresentation);
                                }
                            }
                        });
            default:
                return false;
        }
    }

    public interface OnPresentationCardClickedListener {
        public void onPresentationSelected(PresentationData presentation);
        public void onPresentationDeselected(PresentationData presentation);
        public boolean onPresentationOpened(PresentationData presentation);
        public void onPresentationPracticeSelected(PresentationData presentation);
        public void onPresentationDeleteSelected(PresentationData presentation);
        public void onPresentationColorChange(PresentationData presentation, int color);
        public void onPresentationReset(PresentationData presentation);
    }
}
