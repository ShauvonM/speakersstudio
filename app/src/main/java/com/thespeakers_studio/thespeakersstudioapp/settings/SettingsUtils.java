package com.thespeakers_studio.thespeakersstudioapp.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by smcgi_000 on 8/1/2016.
 */
public class SettingsUtils {

    public static final String PREF_WELCOME_DONE = "pref_welcome_done";
    public static final String PREF_PRESENTATION_LIST_TWO_COLUMN = "presentation_list_view_two_col";
    public static final String PREF_OPEN_COMPLETE_GOTO = "pref_completed_presentations_goto";

    public static final String PREF_TIMER_WAIT = "pref_timer_wait";
    public static final String PREF_TIMER_SHOW = "pref_timer_show";
    public static final String PREF_TIMER_WARNING = "pref_timer_warning";
    public static final String PREF_TIMER_VIBRATE = "pref_timer_vibrate";
    public static final String PREF_TIMER_DISABLE = "pref_timer_disable";
    public static final String PREF_TIMER_RECORD = "pref_timer_record";
    public static final String PREF_TIMER_TRACK = "pref_timer_track";

    public static final String PREF_TIMER_DEFAULT_DURATION = "pref_timer_default_duration";
    public static final int DEFAULT_TIMER_DURATION = 5;

    public static final int NAVDRAWER_LAUNCH_DELAY = 150;
    public static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    public static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    public static final int SELECTION_TOOLBAR_FADE_DURATION = 300;
    public static final int PROMPT_HEADER_SLIDE_DURATION = 500;
    public static final int PROMPT_PROGRESS_ANIMATION_DURATION = 300;
    public static final int OUTLINE_BUTTON_TRANSITION_DURATION = 1000;

    public static void putBoolean(final Context context, final String key, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(key, newValue).apply();
    }

    public static boolean getBoolean(final Context context, final String key, boolean def) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(key, def);
    }

    public static boolean isFirstRunProcessComplete(final Context context) {
        return getBoolean(context, PREF_WELCOME_DONE, false);
    }

    public static void markFirstRunProcessesDone(final Context context, boolean newValue) {
        putBoolean(context, PREF_WELCOME_DONE, newValue);
    }

    public static boolean isPresentationListTwoColumns(final Context context) {
        return getBoolean(context, PREF_PRESENTATION_LIST_TWO_COLUMN, false);
    }

    public static void setPresentationListTwoColumns(final Context context, boolean newValue) {
        putBoolean(context, PREF_PRESENTATION_LIST_TWO_COLUMN, newValue);
    }

    public static boolean getOpenCompleteGoto(final Context context) {
        return getBoolean(context, PREF_OPEN_COMPLETE_GOTO, true);
    }

    public static boolean getTimerWait(final Context context) {
        return getBoolean(context, PREF_TIMER_WAIT, true);
    }

    public static boolean getTimerShow(final Context context) {
        return getBoolean(context, PREF_TIMER_SHOW, true);
    }

    public static boolean getTimerWarning(final Context context) {
        return getBoolean(context, PREF_TIMER_WARNING, true);
    }

    public static boolean getTimerVibrate(final Context context) {
        return getBoolean(context, PREF_TIMER_VIBRATE, true);
    }

    public static boolean getTimerDisable(final Context context) {
        return getBoolean(context, PREF_TIMER_DISABLE, true);
    }

    public static boolean getTimerRecord(final Context context) {
        return getBoolean(context, PREF_TIMER_RECORD, true);
    }

    public static boolean getTimerTrack(final Context context) {
        return getBoolean(context, PREF_TIMER_TRACK, true);
    }

    public static int getDefaultTimerDuration(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_TIMER_DEFAULT_DURATION, DEFAULT_TIMER_DURATION);
    }

    public static void setDefaultTimerDuration(final Context context, int duration) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_TIMER_DEFAULT_DURATION, duration).apply();
    }

    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
