package com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;
import com.thespeakers_studio.thespeakersstudioapp.model.PromptAnswer;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
@SuppressLint("ViewConstructor")
public class ListItemTextPromptView extends ListItemPromptView implements TextWatcher {

    public ListItemTextPromptView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    @Override
    protected void inflateView() {
        super.inflateView();
    }

    @Override
    protected void inflateEditContents(LayoutInflater inflater, LinearLayout wrapper) {
        inflater.inflate(R.layout.presentation_prompt_card_text, wrapper, true);
    }

    @Override
    protected void renderViews() {
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

            TextInputLayout layout = (TextInputLayout) findViewById(R.id.prompt_input_layout);
            layout.setHint(mPrompt.getProcessedText());

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
