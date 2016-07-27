package com.thespeakers_studio.thespeakersstudioapp.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;
import com.thespeakers_studio.thespeakersstudioapp.fragment.PromptDatepickerFragment;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;
import com.thespeakers_studio.thespeakersstudioapp.model.PromptAnswer;

/**
 * Created by smcgi_000 on 6/8/2016.
 */
public class ListItemDateTimePromptView extends ListItemPromptView implements PromptDatepickerFragment.OnPromptDateTimeListener {

    private PromptDatepickerFragment mDateTimePickerFragment;

    public ListItemDateTimePromptView(Context context, Prompt prompt) {
        super(context, prompt);
    }

    @Override
    void inflateView() {
        super.inflateView();
    }

    @Override
    protected void inflateEditContents(LayoutInflater inflater, LinearLayout editWrapper) {
        inflater.inflate(R.layout.presentation_prompt_card_datetime, editWrapper, true);
    }

    @Override
    void renderViews() {
        Calendar c = Calendar.getInstance();

        if (!mPrompt.getAnswer().isEmpty()) {
            String timestamp = mPrompt.getAnswerByKey("timestamp").getValue();
            c.setTimeInMillis(Long.parseLong(timestamp));
        }

        // prepare the date/time picker dialogs
        mDateTimePickerFragment = new PromptDatepickerFragment();
        mDateTimePickerFragment.setOnPromptDateTimeListener(this);
        mDateTimePickerFragment.setCalendar(c);

        super.renderViews();

        setDateTimeStrings();
    }

    @Override
    public ArrayList<PromptAnswer> getUserInput() {
        Calendar c = mDateTimePickerFragment.getCalendar();
        String timestamp = String.valueOf(c.getTimeInMillis());
        ArrayList<PromptAnswer> answers = new ArrayList<>();
        answers.add(new PromptAnswer("timestamp", timestamp, mPrompt.getId()));
        return answers;
    }

    private void setDateTimeStrings () {
        Calendar c = mDateTimePickerFragment.getCalendar();
        if (findViewById(R.id.prompt_datepicker_button) != null) {
            TextView dateField = (TextView) findViewById(R.id.prompt_datepicker_button);
            dateField.setOnClickListener(this);

            // set the current date in the date area
            dateField.setText(Utils.getDateString(c));
        }
        if (findViewById(R.id.prompt_timepicker_button) != null) {
            TextView timeField = (TextView) findViewById(R.id.prompt_timepicker_button);
            timeField.setOnClickListener(this);

            // set the current date in the date area
            timeField.setText(Utils.getTimeString(c));
        }
    }

    @Override
    protected String processAnswer() {
        String timestamp = mPrompt.getAnswerByKey("timestamp").getValue();
        if (!timestamp.isEmpty()) {
            return Utils.getDateTimeString(timestamp, getResources());
        } else {
            return "";
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prompt_datepicker_button:
                mDateTimePickerFragment.setDatePicker();
                mDateTimePickerFragment.show(((Activity) getContext()).getFragmentManager(), "datetime");
                break;
            case R.id.prompt_timepicker_button:
                mDateTimePickerFragment.setTimePicker();
                mDateTimePickerFragment.show(((Activity) getContext()).getFragmentManager(), "datetime");
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public void onDateTimeSet(Calendar cal) {
        setSaveButtonIcon();
        setDateTimeStrings();
    }

}
