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

    public static final int NAVDRAWER_LAUNCH_DELAY = 150;
    public static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    public static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    public static final int SELECTION_TOOLBAR_FADE_DURATION = 300;
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
}
