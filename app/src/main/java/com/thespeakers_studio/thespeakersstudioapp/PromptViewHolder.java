package com.thespeakers_studio.thespeakersstudioapp;

import android.app.ActionBar;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.zip.Inflater;

/**
 * Created by smcgi_000 on 5/10/2016.
 */
public class PromptViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public View mLayout;
    public View mParent;
    private Prompt mPrompt;

    public PromptViewHolder(View v, View parent) {
        super(v);
        mLayout = v;
        mParent = parent;
    }

    public void setData(Prompt data) {
        mPrompt = data;
        String text = data.getProcessedText();
        int type = data.getType();

        if (type == PresentationData.NEXT) {
            mLayout.findViewById(R.id.next_action_button).setOnClickListener(this);
            return;
        }

        if (type == PresentationData.HEADER) {
            String label;
            switch(data.getStep()) {
                case 1:
                    label = mLayout.getContext().getString(R.string.step_1);
                    break;
                case 2:
                    label = mLayout.getContext().getString(R.string.step_2);
                    break;
                case 3:
                    label = mLayout.getContext().getString(R.string.step_3);
                    break;
                case 4:
                    label = mLayout.getContext().getString(R.string.step_4);
                    break;
                default:
                    label = "";
                    break;
            }
            ((TextView) mLayout.findViewById(R.id.card_label)).setText(label);
        } else {
            if (mLayout.findViewById(R.id.card_label) != null) {
                ((TextView) mLayout.findViewById(R.id.card_label)).setText(text);
            }
            if (mLayout.findViewById(R.id.prompt_char_count) != null) {
                ((TextView) mLayout.findViewById(R.id.prompt_char_count)).setText(String.valueOf(mPrompt.getAnswer().length()));
            }
            if (mLayout.findViewById(R.id.prompt_char_max) != null) {
                ((TextView) mLayout.findViewById(R.id.prompt_char_max)).setText(String.valueOf(mPrompt.getCharLimit()));
            }

            if (data.getIsOpen()) {
                mLayout.findViewById(R.id.card_text).setAlpha(0);
                mLayout.findViewById(R.id.prompt_edit_layout).setVisibility(View.VISIBLE);
                setOpenLayoutParams();
            }
        }

        ((TextView) mLayout.findViewById(R.id.card_text)).setText(text);
        mLayout.findViewById(R.id.card_view).setOnClickListener(this);
    }

    private void openPrompt() {
        mPrompt.toggleOpen();

        // transition to the edit state

        final TextView textView = (TextView) mLayout.findViewById(R.id.card_text);
        final CardView cardView = (CardView) mLayout.findViewById(R.id.card_view);
        final LinearLayout editLayout = (LinearLayout) mLayout.findViewById(R.id.prompt_edit_layout);
        final FloatingActionButton saveButton = (FloatingActionButton) mLayout.findViewById(R.id.prompt_save_button);

        //animate the text
        Animation textViewAnimation = AnimationUtils.loadAnimation(mLayout.getContext(), R.anim.prompt_card_text_edit);
        textViewAnimation.reset();
        textView.clearAnimation();
        textView.startAnimation(textViewAnimation);

        // insert the buttons
        /*
        FloatingActionButton saveButton = new FloatingActionButton(mLayout.getContext(), null, R.style.PromptButton);
        saveButton.setId(R.id.prompt_save_button);
        ((RelativeLayout) mLayout).addView(saveButton);

        RelativeLayout.LayoutParams saveButtonParams = (RelativeLayout.LayoutParams) saveButton.getLayoutParams();
        saveButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        saveButtonParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.card_view);
        saveButton.setLayoutParams(saveButtonParams);

        FloatingActionButton helpButton = new FloatingActionButton(mLayout.getContext(), null, R.style.PromptButton);
        helpButton.setId(R.id.prompt_help_button);
        ((RelativeLayout) mLayout).addView(helpButton);

        RelativeLayout.LayoutParams helpButtonParams = (RelativeLayout.LayoutParams) saveButton.getLayoutParams();
        helpButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        helpButtonParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.card_view);
        helpButton.setLayoutParams(helpButtonParams);
        */

        setOpenLayoutParams();

        // end of button insertion

        // animate in the edit views
        if (null != editLayout) {
            editLayout.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            final int targetHeight = editLayout.getMeasuredHeight() - textView.getMeasuredHeight();
            final int startHeight = textView.getMeasuredHeight();

            editLayout.getLayoutParams().height = startHeight;
            editLayout.setVisibility(View.VISIBLE);
            editLayout.setAlpha(0);
            Animation editLayoutAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (interpolatedTime < 1) {
                        if (targetHeight > 0) {
                            editLayout.getLayoutParams().height = (int) (startHeight + (targetHeight * interpolatedTime));
                        } else {
                            textView.getLayoutParams().height = (int) (startHeight + (targetHeight * interpolatedTime));
                        }
                        editLayout.setAlpha(interpolatedTime * 2);
                        editLayout.requestLayout();

                        saveButton.setTranslationX(saveButton.getMeasuredWidth() - (saveButton.getMeasuredWidth() * interpolatedTime));
                    } else {
                        textView.setVisibility(View.GONE);
                        editLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        cardView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        editLayout.setAlpha(1);
                        editLayout.requestLayout();

                        saveButton.setTranslationX(0);

                    }
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            editLayoutAnimation.setDuration(750);
            cardView.startAnimation(editLayoutAnimation);
        }
    }

    private void setOpenLayoutParams() {
        FloatingActionButton saveButton = (FloatingActionButton) mLayout.findViewById(R.id.prompt_save_button);
        CardView cardView = (CardView) mLayout.findViewById(R.id.card_view);

        saveButton.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams cardParams = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
        cardParams.addRule(RelativeLayout.LEFT_OF, R.id.prompt_save_button);
        //cardParams.addRule(RelativeLayout.RIGHT_OF, R.id.prompt_help_button);
        cardParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        cardView.setLayoutParams(cardParams);
    }

    @Override
    public void onClick (View v) {
        int type = mPrompt.getType();
        switch (type) {
            case PresentationData.NEXT:
                Log.d("SS", "Next step clicked");
                break;
            case PresentationData.HEADER:
                Log.d("SS", "Header clicked");
                break;
            default:
                openPrompt();
                break;
        }
    }
}
