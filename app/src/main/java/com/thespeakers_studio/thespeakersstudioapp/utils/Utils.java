package com.thespeakers_studio.thespeakersstudioapp.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Build;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.model.PromptAnswer;

/**
 * Created by smcgi_000 on 5/10/2016.
 */
public class Utils {

    public static final int REQUEST_CODE_EDIT_PRESENTATION = 1;
    public static final int REQUEST_CODE_OUTLINE = 2;
    public static final int REQUEST_CODE_LOCATION_SELECTED = 3;
    public static final int REQUEST_CODE_PRACTICE = 4;

    public static final String INTENT_PRESENTATION_ID = "presentation_id";
    public static final String INTENT_THEME_ID = "theme_id";
    public static final String INTENT_OUTLINE = "outline";
    public static final String INTENT_DURATION = "duration";

    public static final String BUNDLE_TIMER = "timer";

    public static final int VIBRATE_PULSE = 200;
    public static final int VIBRATE_LONG = 500;
    public static final int VIBRATE_PULSE_GAP = 60;
    public static final int VIBRATE_BUMP =40;

    public static final long[] VIBRATE_PATTERN_DOUBLE =
            new long[] {0, VIBRATE_PULSE, VIBRATE_PULSE_GAP, VIBRATE_PULSE};
    public static final long[] VIBRATE_PATTERN_TRIPLE =
            new long[] {0, VIBRATE_PULSE, VIBRATE_PULSE_GAP, VIBRATE_PULSE,
                    VIBRATE_PULSE_GAP, VIBRATE_LONG};

    public static void vibrate(Context context, long[] pattern) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, -1);
    }
    public static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATE_BUMP);
    }
    public static void vibratePulse(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATE_PULSE);
    }
    public static void vibrateLongRepeat(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[] {0, VIBRATE_LONG, VIBRATE_LONG}, 0);
    }

    public static void vibrateCancel(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
    }

    public static boolean versionGreaterThan(int version) {
        return Build.VERSION.SDK_INT >= version;
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

    public static long now() {
        return SystemClock.uptimeMillis();
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
                        when, flags);
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
                if (cnt > 0 && text.length() > 0) {
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

    public static String getTimeStringFromMillis (int millis, Resources r) {
        int use = Math.abs(millis);
        int secs = (int) (use / 1000);
        if (use % 1000 > 1) {
            secs += 1;
        }
        int mins = secs / 60;
        secs = secs % 60;

        int formatString = millis >= 0 ? R.string.timer_output : R.string.timer_output_negative;
        return String.format(r.getString(formatString), mins, secs);
    }

    public static String getTimeStringFromMillisInText (int millis, Resources r) {
        int use = Math.abs(millis);
        int secs = (int) (use / 1000);
        if (use % 1000 > 1) {
            secs += 1;
        }
        int mins = secs / 60;
        secs = secs % 60;

        if (mins > 0 && secs > 0) {
            return String.format(r.getString(R.string.text_time_display_minutes_and_seconds),
                    mins, secs);
        } else if (mins > 0) {
            return String.format(r.getString(R.string.text_time_display_minutes), mins);
        } else {
            return String.format(r.getString(R.string.text_time_display_seconds), secs);
        }
    }

    public static String secondsFromMillis (int millis) {
        int secs = millis / 1000;
        if (millis % 1000 > 1) {
            secs += 1;
        }
        return "" + secs;
    }

    public static int roundToThousand (int num) {
        return Math.round((float)(num / 1000)) * 1000;
    }

    public static int roundDownToThousand (int num) {
        return (int) Math.floor((float)(num / 1000)) * 1000;
    }

    public static int getScreenWidthInPx(@NonNull final Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getThemeAttributeDimensionSize(@NonNull final Context context, final int attr) {
        TypedArray typedArray = null;

        try {
            typedArray = context.getTheme().obtainStyledAttributes(new int[] { attr });
            return typedArray.getDimensionPixelSize(0, 0);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    private static final int[] RES_IDS_ACTION_BAR_SIZE = { R.attr.actionBarSize };

    public static int calculateActionBarSize(Context context) {
        if (context == null) {
            return 0;
        }

        Resources.Theme curTheme = context.getTheme();
        if (curTheme == null) {
            return 0;
        }

        TypedArray att = curTheme.obtainStyledAttributes(RES_IDS_ACTION_BAR_SIZE);
        if (att == null) {
            return 0;
        }

        float size = att.getDimension(0, 0);
        att.recycle();
        return (int) size;
    }

    public static float getProgress(int value, int min, int max) {
        if (min == max) {
            throw new IllegalArgumentException("Max (" + max + ") cannot equal min (" + min + ")");
        }

        return (value - min) / (float) (max - min);
    }

    public static int fetchPrimaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data,
                new int[] {R.attr.colorPrimary});
        int primaryColor = a.getColor(0, 0);
        a.recycle();
        return primaryColor;
    }

    public static String getUUID() {
        return java.util.UUID.randomUUID().toString();
    }
}
