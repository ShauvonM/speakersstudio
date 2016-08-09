package com.thespeakers_studio.thespeakersstudioapp.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.activity.BaseActivity;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;

/**
 * Created by smcgi_000 on 8/8/2016.
 */
public class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_SETTINGS;
    }

    @Override
    protected void setLayoutPadding(int actionBarSize) {
        View view = findViewById(R.id.settings_fragment);
        if (view == null) {
            return;
        }
        view.setPadding(view.getPaddingLeft(), actionBarSize, view.getPaddingRight(), view.getPaddingBottom());
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private int mPrefsId;

        public SettingsFragment() {
            mPrefsId = R.xml.settings_prefs;
        }

        public void setPrefsId(int prefsId) {
            mPrefsId = prefsId;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(mPrefsId);

            SettingsUtils.registerOnSharedPreferenceChangeListener(getActivity(), this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            SettingsUtils.unregisterOnSharedPreferenceChangeListener(getActivity(), this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // put handlers for specific settings here, as needed
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }
}
