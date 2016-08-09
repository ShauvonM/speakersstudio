package com.thespeakers_studio.thespeakersstudioapp.activity;

import android.os.Bundle;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.utils.AnalyticsHelper;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 8/8/2016.
 */
public class StoriesMainActivity extends BaseActivity {

    private static final String TAG = makeLogTag(StoriesMainActivity.class);
    private static final String SCREEN_LABEL = "Story List";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stories_main);

        AnalyticsHelper.sendScreenView(SCREEN_LABEL);

    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_STORIES;
    }

    @Override
    protected void setLayoutPadding(int actionBarSize) {

    }
}
