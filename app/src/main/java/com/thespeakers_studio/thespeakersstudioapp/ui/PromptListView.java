package com.thespeakers_studio.thespeakersstudioapp.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.ArrayList;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses.ListItemContactinfoPromptView;
import com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses.ListItemDateTimePromptView;
import com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses.ListItemDurationPromptView;
import com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses.ListItemListPromptView;
import com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses.ListItemLocationPromptView;
import com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses.ListItemNextView;
import com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses.ListItemPromptView;
import com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses.ListItemTextPromptView;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;
import com.thespeakers_studio.thespeakersstudioapp.activity.PresentationMainActivity;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
public class PromptListView extends LinearLayout implements ListItemView.ListItemListener {
    private ArrayList<Prompt> mPromptData;
    private ListItemView mOpenView;
    private PromptListListener mListener;

    //private ListItemHeaderView mHeader;

    public PromptListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mListener = null;
    }

    public void setPromptListListener (PromptListListener l) {
        mListener = l;
    }

    /*
    private float getCompletionPercentage() {
        if (getContext() instanceof PresentationMainActivity) {
            PresentationData pres = null; //((PresentationMainActivity) getContext()).getSelectedPresentation();
            if (pres != null) {
                return pres.getCompletionPercentage(mPromptData.get(0).getStep());
            }
        }
        return 0;
    }
    */

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

        // for each prompt, generate an appropriate com.thespeakers_studio.thespeakersstudioapp.view
        for (Prompt thisPrompt : mPromptData) {
            int type = thisPrompt.getType();
            ListItemView view;
            switch(type) {
                case PresentationData.HEADER:
                    /* Skip the header, because it's in the actionbar now
                    view = new ListItemHeaderView(getContext(), thisPrompt);
                    mHeader = (ListItemHeaderView) view;
                    mHeader.animateFillFactor(getCompletionPercentage());
                    */
                    view = null;
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

            if (view != null) {
                if (contiguous && (thisPrompt.getAnswer().size() > 0 || !thisPrompt.getIsPrompt())) {
                    view.setContiguous();
                } else {
                    contiguous = false;
                }

                view.setLayoutParams(params);
                view.setListItemOpenListener(this);

                if (Utils.versionGreaterThan(21)) {
                    view.setZ(count--);
                }

                addView(view);
            }
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
    public void openPromptAt(int position) {
        if (position > getChildCount()) {
            // next was clicked, so we should go to the next step
            if (mListener!= null) {
                mListener.onNextStep(mPromptData.get(1).getStep());
            }
        } else {
            ListItemView view = (ListItemView) getChildAt(position);

            if (view != null && view instanceof ListItemPromptView) {
                //smoothScrollTo(0, com.thespeakers_studio.thespeakersstudioapp.view.getTop() - 50);
                ((ListItemPromptView) view).open();
            }
        }
    }

    @Override
    public void onSaveItem(Prompt prompt) {
        int thisOrder = prompt.getOrder();
        ListItemView view = (ListItemView) getChildAt(thisOrder - 1);

        if (thisOrder == 1) {
            // the first item should just show progress
            view.animateLineTop();
        } else if (thisOrder > 1) {
            if (((ListItemView) getChildAt(thisOrder - 2)).isFinishShown()) {
                view.animateLineTop();
                if (thisOrder == mPromptData.size() - 2) {
                    // if it's the last one, we can animate it AND the next button!
                    // thisOrder is not 0 based, so thisOrder would point to the next view
                    ((ListItemView) getChildAt(thisOrder)).animateLineTop(SettingsUtils.PROMPT_PROGRESS_ANIMATION_DURATION);
                }
            }
        }

        //mHeader.animateFillFactor(getCompletionPercentage());

        if (mListener != null) {
            mListener.onSaveItem(prompt);
        }
    }

    public interface PromptListListener {
        public void onSaveItem(Prompt prompt);
        public void onNextStep(int step);
    }
}
