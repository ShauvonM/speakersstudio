package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
public class ListItemTextPromptView extends ListItemPromptView implements TextWatcher {

    public ListItemTextPromptView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    @Override
    void inflateView() {
        super.inflateView();
    }

    @Override
    protected void inflateEditContents(LayoutInflater inflater, LinearLayout wrapper) {
        inflater.inflate(R.layout.presentation_prompt_card_text, wrapper, true);
    }

    @Override
    void renderViews() {
        super.renderViews();

        String answer = processAnswer();

        // for text cards, we have a character count
        if (findViewById(R.id.prompt_char_count) != null) {
            ((TextView) findViewById(R.id.prompt_char_count)).setText(String.valueOf(mPrompt.getAnswerByKey("text").getValue().length()));
        }
        // for text cards, we have a max number of characters
        if (findViewById(R.id.prompt_char_max) != null) {
            ((TextView) findViewById(R.id.prompt_char_max)).setText(String.valueOf(mPrompt.getCharLimit()));
        }
        // text cards have an input element
        if (findViewById(R.id.prompt_input) != null) {
            EditText input = (EditText) findViewById(R.id.prompt_input);
            input.setText(answer);
            input.addTextChangedListener(this);

            // impose the character limit
            if (mPrompt.getCharLimit() > 0) {
                InputFilter[] fa = new InputFilter[1];
                fa[0] = new InputFilter.LengthFilter(mPrompt.getCharLimit());
                input.setFilters(fa);
            }
        }
    }

    @Override
    protected ArrayList<PromptAnswer> getUserInput() {
        String text = ((EditText) findViewById(R.id.prompt_input)).getText().toString().trim();
        if (text.isEmpty()) {
            return mPrompt.getAnswer();
        } else {
            ArrayList<PromptAnswer> answers = new ArrayList<>();
            answers.add(new PromptAnswer("text", text, mPrompt.getId()));
            return answers;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        setSaveButtonIcon();

        if (findViewById(R.id.prompt_char_count) != null) {
            ((TextView) findViewById(R.id.prompt_char_count)).setText(String.valueOf(s.length()));
        }
    }

}
