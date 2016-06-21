package com.thespeakers_studio.thespeakersstudioapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
public class ListItemContactinfoPromptView extends ListItemPromptView implements TextWatcher {

    public ListItemContactinfoPromptView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    @Override
    void inflateView() {
        super.inflateView();
    }

    @Override
    protected void inflateEditContents(LayoutInflater inflater, LinearLayout editWrapper) {
        inflater.inflate(R.layout.presentation_prompt_card_contactinfo, editWrapper, true);
    }

    @Override
    void renderViews() {
        super.renderViews();

        EditText nameInput = (EditText) findViewById(R.id.name_input);
        EditText compInput = (EditText) findViewById(R.id.company_input);
        EditText emalInput = (EditText) findViewById(R.id.email_input);
        EditText phonInput = (EditText) findViewById(R.id.phone_input);

        nameInput.setText(mPrompt.getAnswerByKey("name").getValue());
        compInput.setText(mPrompt.getAnswerByKey("company").getValue());
        emalInput.setText(mPrompt.getAnswerByKey("email").getValue());
        phonInput.setText(mPrompt.getAnswerByKey("phone").getValue());

        nameInput.addTextChangedListener(this);
        compInput.addTextChangedListener(this);
        emalInput.addTextChangedListener(this);
        phonInput.addTextChangedListener(this);
    }

    @Override
    public ArrayList<PromptAnswer> getUserInput() {
        EditText nameInput = (EditText) findViewById(R.id.name_input);
        EditText compInput = (EditText) findViewById(R.id.company_input);
        EditText emalInput = (EditText) findViewById(R.id.email_input);
        EditText phonInput = (EditText) findViewById(R.id.phone_input);

        ArrayList<PromptAnswer> answers = new ArrayList<>();
        if (!nameInput.getText().toString().isEmpty()) {
            answers.add(new PromptAnswer("name", nameInput.getText().toString(), mPrompt.getId()));
        }
        if (!compInput.getText().toString().isEmpty()) {
            answers.add(new PromptAnswer("company", compInput.getText().toString(), mPrompt.getId()));
        }
        if (!emalInput.getText().toString().isEmpty()) {
            answers.add(new PromptAnswer("email", emalInput.getText().toString(), mPrompt.getId()));
        }
        if (!phonInput.getText().toString().isEmpty()) {
            answers.add(new PromptAnswer("phone", phonInput.getText().toString(), mPrompt.getId()));
        }

        return answers;
    }

    @Override
    protected String processAnswer() {
        PromptAnswer name = mPrompt.getAnswerByKey("name");
        PromptAnswer company = mPrompt.getAnswerByKey("company");
        if (name == null && company == null) {
            return "";
        } else if (name == null) {
            return company.getValue();
        } else {
            return name.getValue();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setSaveButtonIcon();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
