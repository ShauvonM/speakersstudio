package com.thespeakers_studio.thespeakersstudioapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by smcgi_000 on 6/10/2016.
 */
public class PromptDatepickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private Calendar mCalendar;
    private boolean mType;
    private OnPromptDateTimeListener mListener;

    public void setOnPromptDateTimeListener (OnPromptDateTimeListener l) {
        mListener = l;
    }

    public void setCalendar (Calendar c) {
        mCalendar = c;
    }

    public Calendar getCalendar () {
        return mCalendar;
    }

    public void setDatePicker () {
        mType = false;
    }
    public void setTimePicker () {
        mType = true;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mType) {
            int hour = mCalendar.get(Calendar.HOUR);
            int minute = mCalendar.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute, false);
        } else {
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, monthOfYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        if (mListener != null) {
            mListener.onDateTimeSet(mCalendar);
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mCalendar.set(Calendar.HOUR, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);

        if (mListener != null) {
            mListener.onDateTimeSet(mCalendar);
        }
    }

    public interface OnPromptDateTimeListener {
        public void onDateTimeSet (Calendar cal);
    }
}
