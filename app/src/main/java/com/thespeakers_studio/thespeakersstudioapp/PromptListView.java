package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
public class PromptListView extends LinearLayout implements ListItemView.ListItemListener {
    private ArrayList<Prompt> mPromptData;
    private ListItemView mOpenView;
    private PromptListListener mListener;

    private ListItemHeaderView mHeader;

    public PromptListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mListener = null;
    }

    public void setPromptListListener (PromptListListener l) {
        mListener = l;
    }

    private float getCompletionPercentage() {
        if (getContext() instanceof PresentationMainActivity) {
            PresentationData pres = ((PresentationMainActivity) getContext()).getSelectedPresentation();
            if (pres != null) {
                return pres.getCompletionPercentage(mPromptData.get(0).getStep());
            }
        }
        return 0;
    }

    public void clearData() {
        mPromptData = null;
        removeAllViews();
    }

    public void setData(ArrayList<Prompt> data) {
        mPromptData = data;
        int count = mPromptData.size();
        boolean contiguous = true;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,
                -getResources().getDimensionPixelOffset(R.dimen.prompt_spacing_bottom),
                0, 0);

        // for each prompt, generate an appropriate view
        for (Prompt thisPrompt : mPromptData) {
            int type = thisPrompt.getType();
            ListItemView view;
            switch(type) {
                case PresentationData.HEADER:
                    view = new ListItemHeaderView(getContext(), thisPrompt);
                    mHeader = (ListItemHeaderView) view;
                    mHeader.animateFillFactor(getCompletionPercentage());
                    break;
                case PresentationData.NEXT:
                    view = new ListItemNextView(getContext(), thisPrompt);
                    break;
                case PresentationData.TEXT:
                case PresentationData.PARAGRAPH:
                    view = new ListItemTextPromptView(getContext(), thisPrompt);
                    break;
                case PresentationData.DATETIME:
                    view = new ListItemDateTimePromptView(getContext(), thisPrompt);
                    break;
                case PresentationData.LOCATION:
                    view = new ListItemLocationPromptView(getContext(), thisPrompt);
                    break;
                case PresentationData.CONTACTINFO:
                    view = new ListItemContactinfoPromptView(getContext(), thisPrompt);
                    break;
                case PresentationData.DURATION:
                    view = new ListItemDurationPromptView(getContext(), thisPrompt);
                    break;
                case PresentationData.LIST:
                    view = new ListItemListPromptView(getContext(), thisPrompt);
                    break;
                default:
                    view = new ListItemPromptView(getContext(), thisPrompt);
                    break;
            }

            if (contiguous && (thisPrompt.getAnswer().size() > 0 || !thisPrompt.getIsPrompt())) {
                view.setContiguous();
            } else {
                contiguous = false;
            }

            view.setLayoutParams(params);
            view.setListItemOpenListener(this);
            try {
                view.setZ(count--);
            } catch (NoSuchMethodError e) {
                // oh well, don't worry about it
            }
            addView(view);
        }

        requestLayout();
    }

    private void closeOpenItem () {
        if (mOpenView != null && mOpenView instanceof ListItemPromptView) {
            ((ListItemPromptView) mOpenView).close();
        }
    }

    public void onListItemOpen(ListItemView item) {
        closeOpenItem();
        mOpenView = item;
    }

    public void onListItemClosed(ListItemView item) {
        mOpenView = null;
    }

    public void onListItemDefaultClick(ListItemView item) {
        closeOpenItem();
    }

    @Override
    public void onGoToNext(int position) {
        int posto = position + 1;

        if (posto > getChildCount()) {
            // next was clicked, so we should go to the next step
            if (mListener!= null) {
                mListener.onNextStep(mPromptData.get(1).getStep());
            }
        } else {
            ListItemView view = (ListItemView) getChildAt(posto);

            if (view != null && view instanceof ListItemPromptView) {
                //smoothScrollTo(0, view.getTop() - 50);
                ((ListItemPromptView) view).open();
            }
        }
    }

    @Override
    public void onSaveItem(Prompt prompt) {
        int thisOrder = prompt.getOrder();
        ListItemView view = (ListItemView) getChildAt(thisOrder);
        if (thisOrder == 1) {
            view.animateLineTop();
        } else if (thisOrder > 1) {
            if (((ListItemView) getChildAt(thisOrder - 1)).isFinishShown()) {
                view.animateLineTop();
                // size() - 2 is the Next button, because the order is 0 based
                if (thisOrder == mPromptData.size() - 2) {
                    // if it's the last one, we can animate it AND the next button!
                    ((ListItemView) getChildAt(thisOrder + 1)).animateLineTop(Utils.PROMPT_PROGRESS_ANIMATION_DURATION);
                }
            }
        }

        mHeader.animateFillFactor(getCompletionPercentage());

        if (mListener != null) {
            mListener.onSaveItem(prompt);
        }
    }

    public interface PromptListListener {
        public void onSaveItem(Prompt prompt);
        public void onNextStep(int step);
    }
}
