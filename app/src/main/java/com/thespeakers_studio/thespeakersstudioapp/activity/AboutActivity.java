package com.thespeakers_studio.thespeakersstudioapp.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.utils.AnalyticsHelper;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGE;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 8/8/2016.
 */
public class AboutActivity extends BaseActivity {

    private static final String TAG = makeLogTag(AboutActivity.class);
    private static final String SCREEN_LABEL = "About";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        AnalyticsHelper.sendScreenView(SCREEN_LABEL);

        try {
            TextView ver = (TextView) findViewById(R.id.app_version);
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            String verString = String.format(getResources().getString(R.string.app_version),
                    pi.versionName);

            ver.setText(verString);
        } catch (PackageManager.NameNotFoundException e) {
            LOGE(TAG, "Something horrible happened.");
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_ABOUT;
    }

    @Override
    protected void setLayoutPadding(int actionBarSize) {
        View v = findViewById(R.id.margin_content);
        v.setPadding(v.getPaddingLeft(), actionBarSize, v.getPaddingRight(), v.getPaddingBottom());
    }
}
