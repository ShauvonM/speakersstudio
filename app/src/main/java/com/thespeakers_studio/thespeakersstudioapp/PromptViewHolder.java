package com.thespeakers_studio.thespeakersstudioapp;

import android.app.ActionBar;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.zip.Inflater;

/**
 * Created by smcgi_000 on 5/10/2016.
 */
public class PromptViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, TextWatcher {
    public View mLayout;
    public PromptListRecyclerView mParent;
    private Prompt mPrompt;
    private int mClosedHeight;

    public PromptViewHolder(View v, View parent) {
        super(v);
        mLayout = v;
        mParent = (PromptListRecyclerView) parent;

        v.setOnClickListener(this);
    }

    public void setData(Prompt data) {
        mPrompt = data;
        mLayout.setTag(data);

        String text = data.getProcessedText();
        String answer = data.getAnswer();
        int type = data.getType();

        if (type == PresentationData.NEXT) {
            mLayout.findViewById(R.id.next_action_button).setOnClickListener(this);
            return;
        }

        if (type == PresentationData.HEADER) {
            // the header shows the step name
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
            mLayout.findViewById(R.id.header_card).setOnClickListener(this);
            ((TextView) mLayout.findViewById(R.id.card_text)).setText(text);
        } else {
            // this renders the actual card
            if (mLayout.findViewById(R.id.card_label) != null) {
                ((TextView) mLayout.findViewById(R.id.card_label)).setText(text);
            }
            if (mLayout.findViewById(R.id.prompt_char_count) != null) {
                ((TextView) mLayout.findViewById(R.id.prompt_char_count)).setText(String.valueOf(mPrompt.getAnswer().length()));
            }
            if (mLayout.findViewById(R.id.prompt_char_max) != null) {
                ((TextView) mLayout.findViewById(R.id.prompt_char_max)).setText(String.valueOf(mPrompt.getCharLimit()));
            }
            if (mLayout.findViewById(R.id.prompt_save_button) != null) {
                mLayout.findViewById(R.id.prompt_save_button).setOnClickListener(this);
            }
            if (mLayout.findViewById(R.id.prompt_input) != null) {
                EditText input = (EditText) mLayout.findViewById(R.id.prompt_input);
                input.setText(answer);
                input.addTextChangedListener(this);

                if (mPrompt.getCharLimit() > 0) {
                    InputFilter[] fa = new InputFilter[1];
                    fa[0] = new InputFilter.LengthFilter(mPrompt.getCharLimit());
                    input.setFilters(fa);
                }

                setSaveButtonIcon();
            }

            if (data.getIsOpen()) {
                mLayout.findViewById(R.id.card_text).setAlpha(0);
                if (mLayout.findViewById(R.id.prompt_edit_layout) != null) {
                    mLayout.findViewById(R.id.prompt_edit_layout).setVisibility(View.VISIBLE);
                }
                setOpenLayoutParams();
            }
            mLayout.findViewById(R.id.card_view).setOnClickListener(this);

            ((TextView) mLayout.findViewById(R.id.card_text)).setText(answer.isEmpty() ? text : answer);
        }

    }

    public boolean getIsOpen() {
        return mPrompt.getIsOpen();
    }

    private String getUserInput() {
        switch(mPrompt.getType()) {
            case PresentationData.TEXT:
                return ((EditText) mLayout.findViewById(R.id.prompt_input)).getText().toString().trim();
            default:
                return "";
        }
    }

    private boolean hasUserInput() {
        return !getUserInput().isEmpty();
    }

    private void setSaveButtonIcon() {
        final FloatingActionButton saveButton = (FloatingActionButton) mLayout.findViewById(R.id.prompt_save_button);

        // set the icon on the save button
        if (!hasUserInput() && mPrompt.getRequired()) {
            saveButton.setImageResource(0);
            saveButton.setEnabled(false);
        } else if (!hasUserInput() && !mPrompt.getRequired()) {
            saveButton.setImageResource(R.mipmap.skip_prompt);
            saveButton.setEnabled(true);
        } else {
            saveButton.setImageResource(R.mipmap.ic_done);
            saveButton.setEnabled(true);
        }
    }

    // transition to the edit state
    private void openPrompt() {
        mPrompt.toggleOpen();

        mParent.onChildOpened(this);
        this.setIsRecyclable(false);

        //collect up the views
        final TextView textView = (TextView) mLayout.findViewById(R.id.card_text);
        final CardView cardView = (CardView) mLayout.findViewById(R.id.card_view);
        final LinearLayout editLayout = (LinearLayout) mLayout.findViewById(R.id.prompt_edit_layout);
        final FloatingActionButton saveButton = (FloatingActionButton) mLayout.findViewById(R.id.prompt_save_button);

        //animate the text
        Animation textViewAnimation = AnimationUtils.loadAnimation(mLayout.getContext(), R.anim.prompt_card_text_edit);
        textViewAnimation.reset();
        textView.clearAnimation();
        textView.startAnimation(textViewAnimation);

        setOpenLayoutParams();

        // animate in the edit views
        if (null != editLayout) {
            editLayout.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            final int targetHeight = editLayout.getMeasuredHeight() - textView.getMeasuredHeight();
            final int startHeight = textView.getMeasuredHeight();
            mClosedHeight = startHeight;

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

    public void closePrompt() {
        mPrompt.toggleOpen();

        mParent.onChildClosed();

        //collect up the views
        final TextView textView = (TextView) mLayout.findViewById(R.id.card_text);
        final CardView cardView = (CardView) mLayout.findViewById(R.id.card_view);
        final LinearLayout editLayout = (LinearLayout) mLayout.findViewById(R.id.prompt_edit_layout);
        final FloatingActionButton saveButton = (FloatingActionButton) mLayout.findViewById(R.id.prompt_save_button);

        if (hasUserInput()) {
            textView.setText(getUserInput());
        } else {
            textView.setText(mPrompt.getProcessedText());
        }

        //animate the text
        Animation textViewAnimation = AnimationUtils.loadAnimation(mLayout.getContext(), R.anim.prompt_card_text_edit_close);
        textViewAnimation.reset();
        textView.clearAnimation();
        textView.startAnimation(textViewAnimation);

        setOpenLayoutParams();

        // animate out the edit views
        if (null != editLayout) {
            editLayout.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            final int targetHeight = mClosedHeight - editLayout.getMeasuredHeight();
            final int startHeight = editLayout.getMeasuredHeight();

            editLayout.getLayoutParams().height = startHeight;
            editLayout.setVisibility(View.VISIBLE);
            editLayout.setAlpha(1);
            Animation editLayoutAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (interpolatedTime < 1) {
                        if (targetHeight < 0) {
                            editLayout.getLayoutParams().height = (int) (startHeight + (targetHeight * interpolatedTime));
                        } else {
                            textView.getLayoutParams().height = (int) (startHeight + (targetHeight * interpolatedTime));
                        }
                        editLayout.setAlpha(1 - interpolatedTime);
                        editLayout.requestLayout();

                        saveButton.setTranslationX((saveButton.getMeasuredWidth() + 50) * interpolatedTime);
                    } else {
                        textView.setVisibility(View.VISIBLE);
                        editLayout.setVisibility(View.GONE);
                        saveButton.setVisibility(View.GONE);
                        editLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        cardView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        cardView.requestLayout();

                        setIsRecyclable(true);
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

        Log.d("SS", "Close prompt at " + getAdapterPosition());
    }

    private void setClosedLayoutParams() {
        FloatingActionButton saveButton = (FloatingActionButton) mLayout.findViewById(R.id.prompt_save_button);
        CardView cardView = (CardView) mLayout.findViewById(R.id.card_view);

        saveButton.setVisibility(View.GONE);

        RelativeLayout.LayoutParams cardParams = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
        cardParams.addRule(RelativeLayout.LEFT_OF, 0);
        cardParams.addRule(RelativeLayout.RIGHT_OF, 0);
        cardParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
        cardView.setLayoutParams(cardParams);
    }

    private void clickBubble () {
        if (!mPrompt.getIsOpen()) {
            mParent.onChildClicked();
        }
    }

    private void goToNext() {
        mParent.openNextChild(getLayoutPosition());
    }

    @Override
    public void onClick (View v) {

        clickBubble();

        switch (v.getId()) {
            case R.id.prompt_save_button:
                String text = getUserInput();
                boolean req = mPrompt.getRequired();

                if (text.isEmpty() && req) {
                    Log.d("SS", "Save clicked with no text entered. Nothing happens.");
                } else if (text.isEmpty() && !req) {
                    closePrompt();
                    goToNext();
                } else {
                    mPrompt.setAnswer(text);
                    closePrompt();
                    goToNext();
                }
                break;
            case R.id.card_view:
                openPrompt();
                break;
            case R.id.next_action_button:
                Log.d("SS", "Next step clicked");
                break;
            case R.id.header_card:
                Log.d("SS", "Header clicked");
                break;
        }
    }

    @Override
    public void afterTextChanged (Editable s) { }

    @Override
    public void beforeTextChanged (CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setSaveButtonIcon();

        if (mLayout.findViewById(R.id.prompt_char_count) != null) {
            ((TextView) mLayout.findViewById(R.id.prompt_char_count)).setText(String.valueOf(s.length()));
        }
    }
}
