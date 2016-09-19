package com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
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

import java.util.ArrayList;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.ui.ListItemView;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;
import com.thespeakers_studio.thespeakersstudioapp.model.PromptAnswer;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
@SuppressLint("ViewConstructor")
public class ListItemPromptView extends ListItemView {
    private CardView mCardView;
    private boolean mCardBGEmpty;

    public ListItemPromptView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    @Override
    protected void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        inflater.inflate(R.layout.presentation_prompt_card, this, true);
        LinearLayout editWrapper = (LinearLayout) findViewById(R.id.prompt_edit_layout);

        inflateEditContents(inflater, editWrapper);
    }

    protected void inflateEditContents(LayoutInflater inflater, LinearLayout wrapper) {

    }

    public boolean isCardEmpty() {
        return mCardBGEmpty;
    }

    @Override
    protected void renderViews() {
        String text = mPrompt.getProcessedText();
        String answer = processAnswer();

        if (mPrompt.getType() != PresentationData.NONE) {
            setOnClickListener(this);
        }

        mCardView = (CardView) findViewById(R.id.card_view);

        // this fills in the actual card details

        // the main label is what shows when editing
        if (findViewById(R.id.card_label) != null) {
            ((TextView) findViewById(R.id.card_label)).setText(text.replace("\n", " "));
        }
        // all cards have a save button
        if (findViewById(R.id.prompt_save_button) != null) {
            findViewById(R.id.prompt_save_button).setOnClickListener(this);
        }

        // some other setup things now

        setSaveButtonIcon();

        if (mPrompt.getIsOpen()) {
            findViewById(R.id.card_text).setAlpha(0);
            if (findViewById(R.id.prompt_edit_layout) != null) {
                findViewById(R.id.prompt_edit_layout).setVisibility(VISIBLE);
            }
            setOpenLayoutParams();
        }
        mCardView.setOnClickListener(this);

        mCardBGEmpty = answer.isEmpty();

        setCardText();

        setCardBackground(1);
    }

    private void setCardText() {
        // fill out the text on this card, based on if this prompt has an answer or not
        TextView textView = ((TextView) findViewById(R.id.card_text));
        String answer = processAnswer();
        String text = answer.isEmpty() ? mPrompt.getProcessedText() : answer;
        textView.setText(text);

        if (answer.isEmpty()) {
            text = mPrompt.getProcessedText();
            Utils.setTextAppearance(getContext(), textView, R.style.PromptText_Inverted);
        } else {
            text = answer;
            Utils.setTextAppearance(getContext(), textView, R.style.PromptText);
        }

        if (text.length() > 125) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.prompt_small_font_size));
        } else {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.prompt_font_size));
        }
    }

    protected String processAnswer() {
        return mPrompt.getAnswerByKey("text").getValue();
    }

    private void setCardBackground(int time, boolean force) {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.card_view_contents);
        TransitionDrawable transition = (TransitionDrawable) layout.getBackground();
        boolean empty = mPrompt.getAnswer().isEmpty();

        if (empty || force) {
            if (mCardBGEmpty) {
                // it's already empty!
                transition.resetTransition();
            } else {
                // it isn't already empty!
                transition.startTransition(0);
                transition.reverseTransition(time);
            }
            mCardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.prompt_default_background));
            mCardBGEmpty = true;
        } else {
            if (mCardBGEmpty) {
                // it's currently empty, so transition to not empty
                transition.startTransition(time);
            } else {
                // it's currently not empty, so do nothing basically
                transition.startTransition(0);
            }
            mCardView.setCardBackgroundColor(Utils.fetchPrimaryColor(getContext()));
            mCardBGEmpty = false;
        }
    }
    private void setCardBackground(int time) {
        setCardBackground(time, false);
    }

    protected ArrayList<PromptAnswer> getUserInput() {
        return new ArrayList<PromptAnswer>();
    }

    private boolean hasUserInput() {
        return !getUserInput().isEmpty();
    }

    protected void setSaveButtonIcon() {
        final FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.prompt_save_button);

        // set the icon on the save button
        if (!hasUserInput() && mPrompt.getRequired()) {
            saveButton.setImageResource(R.drawable.ic_not_interested_white_24dp);
            saveButton.setEnabled(false);
        } else if (!hasUserInput() && !mPrompt.getRequired()) {
            saveButton.setImageResource(R.drawable.ic_skip_white_24dp);
            saveButton.setEnabled(true);
        } else {
            saveButton.setImageResource(R.drawable.ic_done_white_24dp);
            saveButton.setEnabled(true);
        }
    }

    private void savePrompt() {
        ArrayList<PromptAnswer> answers = getUserInput();
        mPrompt.setAnswers(answers);

        setCardBackground(400);

        if (mOpenListener != null) {
            mOpenListener.onSaveItem(mPrompt);
        }
        promptSaved();
    }

    protected void promptSaved() { }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prompt_save_button:
                boolean req = mPrompt.getRequired();

                if (!hasUserInput() && req) {
                    Log.d("SS", "Save clicked with no text entered. Nothing happens.");
                } else if (!hasUserInput() && !req) {
                    closePrompt();
                    goToNext();
                } else {
                    savePrompt();
                    closePrompt();
                    goToNext();
                }
                break;
            case R.id.card_view:
                open();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    public void open() {
        if (!mPrompt.getIsOpen()) {
            openPrompt();
        }
    }

    // transition to the edit state
    private void openPrompt() {
        mPrompt.toggleOpen();

        fireItemOpen();

        setCardBackground(400, true);

        //collect up the views
        final TextView textView = (TextView) findViewById(R.id.card_text);
        final CardView cardView = (CardView) findViewById(R.id.card_view);
        final LinearLayout editLayout = (LinearLayout) findViewById(R.id.prompt_edit_layout);
        final FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.prompt_save_button);

        //animate the text
        Animation textViewAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.prompt_card_text_edit);
        textViewAnimation.reset();
        textView.clearAnimation();
        textView.startAnimation(textViewAnimation);

        setOpenLayoutParams();

        // animate in the edit views
        if (null != editLayout) {
            editLayout.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            final int targetHeight = editLayout.getMeasuredHeight() - textView.getMeasuredHeight();
            final int startHeight = textView.getMeasuredHeight();

            editLayout.getLayoutParams().height = startHeight;
            editLayout.setVisibility(VISIBLE);
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
                        textView.setVisibility(GONE);
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
        FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.prompt_save_button);
        CardView cardView = (CardView) findViewById(R.id.card_view);

        saveButton.setVisibility(VISIBLE);

        RelativeLayout.LayoutParams cardParams = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
        cardParams.addRule(LEFT_OF, R.id.prompt_save_button);
        //cardParams.addRule(RelativeLayout.RIGHT_OF, R.id.prompt_help_button);
        cardParams.addRule(ALIGN_PARENT_LEFT);
        cardView.setLayoutParams(cardParams);
    }

    public void close() {
        if (mPrompt.getIsOpen()) {
            closePrompt();
        }
    }

    private void closePrompt() {
        mPrompt.toggleOpen();

        Utils.hideKeyboard(getContext(), this);

        fireItemClosed();

        setCardBackground(400);

        //collect up the views
        final TextView textView = (TextView) findViewById(R.id.card_text);
        final CardView cardView = (CardView) findViewById(R.id.card_view);
        final LinearLayout editLayout = (LinearLayout) findViewById(R.id.prompt_edit_layout);
        final FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.prompt_save_button);

        // reset the text so that we can get the proper size that we want the thing to be
        textView.clearAnimation();
        textView.setVisibility(INVISIBLE);

        setCardText();

        final int gotoHeight = textView.getHeight();

        //animate the text
        Animation textViewAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.prompt_card_text_edit_close);
        textViewAnimation.reset();
        textView.startAnimation(textViewAnimation);

        // animate out the edit views
        if (null != editLayout) {
            editLayout.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            final int startHeight = editLayout.getMeasuredHeight();
            final int targetHeight = gotoHeight - startHeight;

            editLayout.getLayoutParams().height = startHeight;
            editLayout.setVisibility(VISIBLE);
            editLayout.setAlpha(1);
            Animation editLayoutAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (interpolatedTime < 1) {
                        editLayout.getLayoutParams().height = (int) (startHeight + (targetHeight * interpolatedTime));
                        textView.getLayoutParams().height = (int) (startHeight + (targetHeight * interpolatedTime));

                        editLayout.setAlpha(1 - interpolatedTime);
                        editLayout.requestLayout();

                        saveButton.setTranslationX((saveButton.getMeasuredWidth() + 50) * interpolatedTime);
                    } else {
                        textView.setVisibility(VISIBLE);
                        editLayout.setVisibility(GONE);
                        saveButton.setVisibility(GONE);
                        editLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        textView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        cardView.requestLayout();
                    }
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            editLayoutAnimation.setDuration(500);
            cardView.startAnimation(editLayoutAnimation);
        }
    }
}
