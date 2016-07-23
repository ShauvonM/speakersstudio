package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.renderscript.ScriptGroup;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Created by smcgi_000 on 5/10/2016.
 */
public class Utils {

    public static final int PROMPT_PROGRESS_ANIMATION_DURATION = 300;
    public static final int FRAMES_PER_SECOND = 60;

    public static float dpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static int getShadowY(int level, Context context) {
        int shadowY = -1;

        switch(level) {
            case 1:
                shadowY = 1;
                break;
            case 2:
                shadowY = 3;
                break;
            case 3:
                shadowY = 10;
                break;
            case 4:
                shadowY = 14;
                break;
            case 5:
                shadowY = 19;
                break;
        }
        return (int) dpToPixel(shadowY, context);
    }

    public static int getShadowBlur(int level, Context context) {
        int shadowBlur = 1;

        switch(level) {
            case 1:
                shadowBlur = 3;
                break;
            case 2:
                shadowBlur = 6;
                break;
            case 3:
                shadowBlur = 20;
                break;
            case 4:
                shadowBlur = 28;
                break;
            case 5:
                shadowBlur = 38;
                break;
        }
        return (int) dpToPixel(shadowBlur, context);
    }

    public static Paint setShadowLayer(Paint paint, int level, Context context) {
        int shadowY = getShadowY(level, context);
        int shadowBlur = getShadowBlur(level, context);

        paint.setShadowLayer(shadowBlur, 0, shadowY, ContextCompat.getColor(context, R.color.shadow));

        return paint;
    }

    public static void showKeyboard(Context context, View container) {
        InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(container, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideKeyboard(Context context, View container) {
        InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(container.getWindowToken(), 0);
    }

    public static String getIso8601Format() {
        return "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    }

    public static String getDateTimeStamp() {
        SimpleDateFormat iso8601Format =
                new SimpleDateFormat(getIso8601Format(), Locale.getDefault());

        String datetime = iso8601Format.format(Calendar.getInstance().getTime());

        return datetime;
    }

    public static String formatDateTime(Context context, String timeToFormat) {
        String finalDateTime = "";

        SimpleDateFormat iso8601Format =
                new SimpleDateFormat(getIso8601Format(), Locale.getDefault());

        Date date = null;
        if (timeToFormat != null) {
            try {
                date = iso8601Format.parse(timeToFormat);
            } catch (ParseException e) {
                date = null;
            }

            if (date != null) {
                long when = date.getTime();
                int flags = 0;
                flags |= DateUtils.FORMAT_SHOW_TIME;
                flags |= DateUtils.FORMAT_SHOW_DATE;
                flags |= DateUtils.FORMAT_ABBREV_MONTH;
                flags |= DateUtils.FORMAT_SHOW_YEAR;

                finalDateTime = DateUtils.formatDateTime(context,
                        when + TimeZone.getDefault().getOffset(when), flags);
            }
        }
        return finalDateTime;
    }

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

    public static String getDateTimeString (String timestamp, Resources r) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Long.parseLong(timestamp));
        return Utils.getDateString(c) + "\n" + r.getString(R.string.datetime_at) + " " + Utils.getTimeString(c);
    }

    public static String getDurationString (String duration, Resources r) {
        String text;
        if (duration.isEmpty()) {
            return "";
        }

        switch (duration) {
            case "5":
                text = r.getString(R.string.five_minutes);
                break;
            case "10":
                text = r.getString(R.string.ten_minutes);
                break;
            case "20":
                text = r.getString(R.string.twenty_minutes);
                break;
            case "30":
                text = r.getString(R.string.thirty_minutes);
                break;
            default:
                text = duration + " " + r.getString(R.string.minutes);
                break;
        }
        return text;
    }
    public static String getDurationString (int duration, Resources r) {
        return getDurationString(String.valueOf(duration), r);
    }

    public static void sortOutlineList (ArrayList<OutlineItem> items) {
        Collections.sort(items, new Comparator<OutlineItem>() {
            @Override
            public int compare(OutlineItem lhs, OutlineItem rhs) {
                return lhs.getOrder() - rhs.getOrder();
            }
        });
    }

    public static String processAnswerList (ArrayList<PromptAnswer> answers, Resources r) {
        String text = "";
        for(int cnt = 0; cnt < answers.size(); cnt++) {
            PromptAnswer answer = answers.get(cnt);

            if (!answer.getValue().isEmpty()) {
                if (cnt > 0) {
                    text += ", ";
                    if (cnt == answers.size() - 1) {
                        text += r.getString(R.string.list_and) + " ";
                    }
                }
                text += answer.getValue();
            }
        }
        return text;
    }

    public static String getTimeStringFromMillis (long millis, Resources r) {
        int secs = (int) (millis / 1000);
        if (millis % 1000 > 1) {
            secs += 1;
        }
        int mins = secs / 60;
        secs = secs % 60;

        return String.format(r.getString(R.string.timer_output), mins, secs);
    }

    public static String secondsFromMillis (long millis) {
        int secs = (int) (millis / 1000);
        if (millis % 1000 > 1) {
            secs += 1;
        }
        return "" + secs;
    }

    public static long roundToThousand (long num) {
        return Math.round((float)(num / 1000)) * 1000;
    }
}
