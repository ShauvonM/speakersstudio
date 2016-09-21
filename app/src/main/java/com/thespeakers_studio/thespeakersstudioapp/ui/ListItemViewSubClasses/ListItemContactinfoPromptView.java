package com.thespeakers_studio.thespeakersstudioapp.ui.ListItemViewSubClasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.data.PresentationDbHelper;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;
import com.thespeakers_studio.thespeakersstudioapp.model.PromptAnswer;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
@SuppressLint("ViewConstructor")
public class ListItemContactinfoPromptView extends ListItemPromptView implements TextWatcher {
    private static final String TAG = ListItemContactinfoPromptView.class.getSimpleName();

    private EditText mPhonInput;
    private EditText mNameInput;
    private EditText mCompInput;
    private EditText mWebyInput;
    private EditText mEmalInput;

    private String mDefaultCompany;
    private String mDefaultWebsite;

    public ListItemContactinfoPromptView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    @Override
    protected void inflateView() {
        super.inflateView();
    }

    @Override
    protected void inflateEditContents(LayoutInflater inflater, LinearLayout editWrapper) {
        inflater.inflate(R.layout.presentation_prompt_card_contactinfo, editWrapper, true);
    }

    @Override
    protected void renderViews() {
        mNameInput = (EditText) findViewById(R.id.name_input);
        mCompInput = (EditText) findViewById(R.id.company_input);
        mWebyInput = (EditText) findViewById(R.id.company_website_input);
        mEmalInput = (EditText) findViewById(R.id.email_input);
        mPhonInput = (EditText) findViewById(R.id.phone_input);

        mNameInput.addTextChangedListener(this);
        mWebyInput.addTextChangedListener(this);
        mCompInput.addTextChangedListener(this);
        mEmalInput.addTextChangedListener(this);
        mPhonInput.addTextChangedListener(this);

        super.renderViews();
    }

    @Override
    protected void onOpen() {
        String company = mPrompt.getAnswerByKey("company").getValue();
        String companyWebsite = mPrompt.getAnswerByKey("company_website").getValue();

        if (company.isEmpty() || companyWebsite.isEmpty()) {
            PresentationData presentation = mPrompt.getPresentation();
            Prompt locationPrompt = presentation.getPromptById(PresentationData.PRESENTATION_LOCATION);
            if (locationPrompt.getAnswer().size() > 0) {
                if (company.isEmpty()) {
                    company = locationPrompt.getAnswerByKey("name").getValue();
                    mDefaultCompany = company;
                }
                if (companyWebsite.isEmpty()) {
                    companyWebsite = locationPrompt.getAnswerByKey("website").getValue();
                    mDefaultWebsite = companyWebsite;
                }
            }
        }

        mNameInput.setText(mPrompt.getAnswerByKey("name").getValue());
        mCompInput.setText(company);
        mWebyInput.setText(companyWebsite);
        mEmalInput.setText(mPrompt.getAnswerByKey("email").getValue());
        mPhonInput.setText(mPrompt.getAnswerByKey("phone").getValue());
    }

    @Override
    public ArrayList<PromptAnswer> getUserInput() {
        ArrayList<PromptAnswer> answers = new ArrayList<>();

        String company = mCompInput.getText().toString();
        if (!company.isEmpty()) { // && !company.equals(mDefaultCompany)) {
            answers.add(new PromptAnswer("company", company, mPrompt.getId()));
        }

        String website = mWebyInput.getText().toString();
        if (!website.isEmpty()) { // && !website.equals(mDefaultWebsite)) {
            answers.add(new PromptAnswer("company_website", website, mPrompt.getId()));
        }

        if (!mNameInput.getText().toString().isEmpty()) {
            answers.add(new PromptAnswer("name", mNameInput.getText().toString(), mPrompt.getId()));
        }
        if (!mEmalInput.getText().toString().isEmpty()) {
            answers.add(new PromptAnswer("email", mEmalInput.getText().toString(), mPrompt.getId()));
        }
        if (!mPhonInput.getText().toString().isEmpty()) {
            answers.add(new PromptAnswer("phone", mPhonInput.getText().toString(), mPrompt.getId()));
        }

        return answers;
    }

    @Override
    protected String processAnswer() {
        onOpen();

        PromptAnswer name = mPrompt.getAnswerByKey("name");
        PromptAnswer company = mPrompt.getAnswerByKey("company");

        String ret = "";
        if (!name.getValue().isEmpty()) {
            ret += name.getValue();
        }
        if (!name.getValue().isEmpty() && !company.getValue().isEmpty()) {
            ret += ", ";
        }
        ret += company.getValue();

        return ret;
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
