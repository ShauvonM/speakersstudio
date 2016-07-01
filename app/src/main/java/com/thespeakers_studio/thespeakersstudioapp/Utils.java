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

import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

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

    public static String getDateTimeStamp() {
        SimpleDateFormat iso8601Format =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        String datetime = iso8601Format.format(Calendar.getInstance().getTime());

        return datetime;
    }

    public static String formatDateTime(Context context, String timeToFormat) {
        String finalDateTime = "";

        SimpleDateFormat iso8601Format =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

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
}
