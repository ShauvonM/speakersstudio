package com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;
import com.thespeakers_studio.thespeakersstudioapp.model.PromptAnswer;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
@SuppressLint("ViewConstructor")
public class ListItemDurationPromptView extends ListItemPromptView implements AdapterView.OnItemSelectedListener, TextWatcher {

    private int mDuration;

    public ListItemDurationPromptView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    @Override
    protected void inflateView() {
        super.inflateView();
    }

    @Override
    protected void inflateEditContents(LayoutInflater inflater, LinearLayout editWrapper) {
        inflater.inflate(R.layout.presentation_prompt_card_duration, editWrapper, true);
    }

    @Override
    protected void renderViews() {
        super.renderViews();

        // the dropdown to specify duration
        Spinner durationInput = (Spinner) findViewById(R.id.duration_input);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.presentation_duration_array, R.layout.spinner_item_duration);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationInput.setAdapter(adapter);

        // the input element for custom duration
        EditText customInput = (EditText) findViewById(R.id.custom_duration_input);
        durationInput.setOnItemSelectedListener(this);

        customInput.addTextChangedListener(this);

        PromptAnswer answer = mPrompt.getAnswerByKey("duration");
        if (!answer.getValue().isEmpty()) {
            String duration = answer.getValue();

            int position;
            String processedAnswer = processAnswer();
            // if the processed answer contains the number, it's a custom value
            if (processedAnswer.contains(String.valueOf(duration))) {
                durationInput.setSelection(adapter.getCount() - 1);
                customInput.setText(answer.getValue());
                customInput.setVisibility(VISIBLE);
            } else {
                durationInput.setSelection(adapter.getPosition(processedAnswer));
                customInput.setVisibility(GONE);
            }
        }
    }

    @Override
    protected String processAnswer() {
        String duration = mPrompt.getAnswerByKey("duration").getValue();
        return Utils.getDurationString(duration, getResources());
    }

    @Override
    public ArrayList<PromptAnswer> getUserInput() {
        ArrayList<PromptAnswer> answers = new ArrayList<>();

        if (mDuration > 0) {
            answers.add(new PromptAnswer("duration", String.valueOf(mDuration), mPrompt.getId()));
        } else if (mDuration == 0) {
            String input = ((EditText) findViewById(R.id.custom_duration_input)).getText().toString();
            if (!input.isEmpty()) {
                answers.add(new PromptAnswer("duration", input, mPrompt.getId()));
            }
        }

        return answers;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        findViewById(R.id.custom_duration_input_layout).setVisibility(GONE);
        switch(position) {
            case 0:
                mDuration = 5;
                break;
            case 1:
                mDuration = 10;
                break;
            case 2:
                mDuration = 20;
                break;
            case 3:
                mDuration = 30;
                break;
            case 4:
                mDuration = 0;
                // show the custom option
                findViewById(R.id.custom_duration_input_layout).setVisibility(VISIBLE);
                break;
            default:
                mDuration = -1;
                break;
        }
        setSaveButtonIcon();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mDuration = 0;
        setSaveButtonIcon();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        setSaveButtonIcon();
    }
}
