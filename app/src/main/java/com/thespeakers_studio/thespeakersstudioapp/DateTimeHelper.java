package com.thespeakers_studio.thespeakersstudioapp;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by smcgi_000 on 6/14/2016.
 */
public class DateTimeHelper {
    public static String getDateString (Calendar c) {
        String weekDay = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
        String month = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        int year = c.get(Calendar.YEAR);

        return weekDay + ", " + month + " " + dayOfMonth + ", " + year;
    }

    public static String getTimeString (Calendar c) {
        int hours = c.get(Calendar.HOUR);
        int mins = c.get(Calendar.MINUTE);
        String ampm = c.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US);

        return hours + ":" + String.format("%02d", mins) + " " + ampm;
    }
}
