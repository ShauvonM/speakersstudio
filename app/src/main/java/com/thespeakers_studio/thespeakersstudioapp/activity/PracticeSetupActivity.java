package com.thespeakers_studio.thespeakersstudioapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.adapter.PracticeListAdapter;
import com.thespeakers_studio.thespeakersstudioapp.data.OutlineDbHelper;
import com.thespeakers_studio.thespeakersstudioapp.fragment.PresentationPracticeDialog;
import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.model.Practice;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.service.TimerService;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.utils.AnalyticsHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.OutlineHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 8/5/2016.
 */
public class PracticeSetupActivity extends BaseActivity implements
        View.OnClickListener, PresentationPracticeDialog.PresentationPracticeDialogInterface {

    private static final String TAG = PracticeSetupActivity.class.getSimpleName();
    private static final String SCREEN_LABEL = "Presentation Outline";

    private PresentationData mPresentation;

    private View mContentWrapper;

    private int mScreenHeight;

    private FloatingActionButton mStartButton;
    private ImageButton mSettingsExpand;

    private boolean isPractice;

    private OutlineHelper mHelper;
    private Outline mOutline;

    private PracticeSettingsFragment mSettingsFragment;

    PresentationPracticeDialog mTimerDialog;

    private RecyclerView mRecyclerView;
    private int mScrollPos;
    private int mSettingsHeight;

    private float mMaxFABElevation;

    private int mDuration;

    private OutlineDbHelper mOutlineDbHelper;
    private ArrayList<Practice> mPractices;

    private boolean mIsDialogOpen;

    private static final String BUNDLE_DURATION = "duration";
    private static final String BUNDLE_OUTLINE = "outline";
    private static final String BUNDLE_SCROLL_POS = "scroll_pos";
    public static final String BUNDLE_DIALOG_OPEN = "dialog_open";

    // service stuff
    private boolean mServiceBound = false;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_practice_setup);

        AnalyticsHelper.sendScreenView(SCREEN_LABEL);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenHeight = size.y;

        mMaxFABElevation = getResources().getDimensionPixelSize(R.dimen.max_fab_elevation);

        mContentWrapper = findViewById(R.id.content_wrapper);
        mStartButton = (FloatingActionButton) findViewById(R.id.fab_practice);

        mSettingsExpand = (ImageButton) findViewById(R.id.settings_expand);
        mSettingsExpand.setOnClickListener(this);

        mSettingsFragment = (PracticeSettingsFragment) getFragmentManager()
                .findFragmentById(R.id.settings_fragment);

        mRecyclerView = (RecyclerView) findViewById(R.id.saved_practice_list);
        mRecyclerView.setHasFixedSize(true);

        mScrollPos = 0;
        mSettingsHeight = -1;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                onScrollChanged(dy);
            }
        });

        mDuration = SettingsUtils.getDefaultTimerDuration(this);
        String presentationId = getIntent().getStringExtra(Utils.INTENT_PRESENTATION_ID);

        setPresentationId(presentationId, null);

        mTimerDialog = new PresentationPracticeDialog();
        mTimerDialog.setInterface(this);

        // see if there's anything to bind to yet
        // if there is, the dialog will automagically show, otherwise nothing will happen
        bindTimerService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOGD(TAG, "On resume");
    }

    private void setPresentationId(String id, Outline outline) {
        if (id != null) {
            mPresentation = mDbHelper.loadPresentationById(id);
            mOutline = outline == null ? Outline.fromPresentation(this, mPresentation)
                    : outline;

            if (mPresentation != null) {
                // load the saved practices for this presentation
                mOutlineDbHelper = new OutlineDbHelper(this);

                mPractices = mOutlineDbHelper.getPractices(mPresentation.getId());
                PracticeListAdapter adapter = new PracticeListAdapter(mPractices, mOutline);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL, false));
                mRecyclerView.setAdapter(adapter);
            }
        }
        isPractice = mOutline != null;

        setup();
    }

    private void showMessageIfEmpty() {
        if (!isPractice) {
            findViewById(R.id.no_results).setVisibility(View.VISIBLE);
            mSettingsExpand.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            if (mRecyclerView.getAdapter().getItemCount() == 2) {
                findViewById(R.id.no_results).setVisibility(View.VISIBLE);
                mSettingsExpand.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);

                onScrollChanged(-mScrollPos);
            } else {
                findViewById(R.id.no_results).setVisibility(View.GONE);
                mSettingsExpand.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setup() {
        TextView presentationName = (TextView) findViewById(R.id.presentation_name);
        TextView presentationDuration = (TextView) findViewById(R.id.presentation_duration);
        EditText timerDurationInput = (EditText) findViewById(R.id.timer_duration_input);
        View fab = findViewById(R.id.fab_practice);

        mSettingsFragment.setPractice(isPractice);

        if (isPractice) {
            timerDurationInput.setVisibility(View.GONE);
            presentationName.setText(mOutline.getTitle());
            presentationDuration.setText(mOutline.getDuration());
        } else {
            presentationDuration.setVisibility(View.GONE);

            timerDurationInput.setVisibility(View.VISIBLE);
            timerDurationInput.setText(String.valueOf(mDuration));
            presentationName.setText(R.string.minutes);
            presentationName.setOnClickListener(this);

            setTitle(R.string.timer);
            ((TextView) findViewById(R.id.no_results)).setText(R.string.no_saved_timers);
        }

        fab.setOnClickListener(this);

        ViewTreeObserver vto = mSettingsFragment.getView().getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // only do this once
                    if (mSettingsHeight == -1) {
                        mSettingsHeight = mSettingsFragment.getView().getHeight();
                        mRecyclerView.scrollBy(0, mSettingsHeight - mScrollPos);

                        showMessageIfEmpty();
                    }
                }
            });
        }
    }

    @Override
    protected int getMinHeaderHeight() {
        return mHeaderDetailsHeightPixels - mSettingsHeight;
    }

    @Override
    protected int getSelfNavDrawerItem() {
        if (mOutline != null) {
            return NAVDRAWER_ITEM_INVALID;
        } else {
            return NAVDRAWER_ITEM_TIMER;
        }
    }

    @Override
    protected void setLayoutPadding(int actionBarSize) {
        findViewById(R.id.no_results).setPadding(0, actionBarSize, 0, 0);

        if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
            ((PracticeListAdapter) mRecyclerView.getAdapter()).setTopMargin(actionBarSize);
        }
    }

    public void onScrollChanged(int dy) {
        mScrollPos = Math.max(mScrollPos + dy, 0);

        int fabHeight = getResources().getDimensionPixelSize(R.dimen.fab_button) +
                (getResources().getDimensionPixelSize(R.dimen.fab_margin) * 2);
        int headerHeight = getSupportActionBar() != null ? getSupportActionBar().getHeight() : 0;

        int fabPosition;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fabPosition = headerHeight - getResources().getDimensionPixelSize(R.dimen.fab_margin);
        } else {
            int baseHeight = mHeaderDetailsHeightPixels + headerHeight;
            fabPosition = Math.max((baseHeight - (fabHeight / 2)) - mScrollPos, 0);
        }
        mStartButton.setTranslationY(fabPosition);

        ViewGroup.LayoutParams params = mHeaderDetails.getLayoutParams();
        params.height = getNewHeaderDetailsHeight(mScrollPos, false);
        mHeaderDetails.setLayoutParams(params);

        float gapFillProgress = setHeaderElevation(mScrollPos);
        // set the start button elevation, too
        ViewCompat.setElevation(mStartButton, (mMaxFABElevation / 2) + (gapFillProgress * (mMaxFABElevation / 2)));

        setSettingsExpandIcon();
    }

    private void setSettingsExpandIcon() {
        if (isSettingsPanelOpen()) {
            mSettingsExpand.setImageResource(R.drawable.ic_expand_less_white_24dp);
        } else {
            mSettingsExpand.setImageResource(R.drawable.ic_expand_more_white_24dp);
        }
    }

    private boolean isSettingsPanelOpen() {
        return mScrollPos <= 50;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.presentation_name:
                EditText timerDurationInput = (EditText) findViewById(R.id.timer_duration_input);
                timerDurationInput.requestFocus();
                break;
            case R.id.fab_practice:
                if (!isPractice) {
                    mDuration = Integer.parseInt(((TextView) findViewById(R.id.timer_duration_input))
                            .getText().toString());
                    if (mDuration <= 0) {
                        return;
                    }
                    SettingsUtils.setDefaultTimerDuration(this, mDuration);
                }
                startPractice();
                break;
            case R.id.settings_expand:
                if (isSettingsPanelOpen()) {
                    collapseSettings();
                } else {
                    expandSettings();
                }
                break;
        }
    }

    private void expandSettings() {
        mRecyclerView.smoothScrollBy(0, -mScrollPos);
    }
    private void collapseSettings() {
        mRecyclerView.smoothScrollBy(0, mSettingsHeight - mScrollPos);
    }

    @Override
    protected void onPause() {
        if (isChangingConfigurations()) {
            mRecyclerView.scrollBy(0, -mScrollPos);
        }
        unbindTimerService();

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindTimerService();

        if (!isChangingConfigurations()) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindTimerService();
    }

    private void startPractice() {
        startTimerService();
        bindTimerService();
    }

    private void showDialog() {
        mTimerDialog.show(getSupportFragmentManager(), PresentationPracticeDialog.TAG);
        mIsDialogOpen = true;
    }

    private void startTimerService() {
        Intent intent = new Intent(this, TimerService.class);
        if (isPractice) {
            intent.putExtra(Utils.INTENT_OUTLINE, mOutline.toBundle());
        } else {
            intent.putExtra(Utils.INTENT_DURATION, mDuration * 60 * 1000);
        }
        startService(intent);
    }

    private void bindTimerService() {
        Intent bindIntent = new Intent(this, TimerService.class);
        bindService(bindIntent, mServiceConnection, 0);
    }

    private void unbindTimerService() {
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
            mTimerDialog.unregister();
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mServiceBound = true;
            mTimerDialog.setBinder(binder);
            LOGD(TAG, "Service bound!");

            showDialog();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onDialogDismissed() {
        mIsDialogOpen = false;
        // TODO: show an indicator that we are waiting for the service to stop?
    }

    @Override
    public void onServiceKilled(final Outline outline, boolean finished) {
        unbindTimerService();

        if (finished) {
            // save user's thoughts on this practice
            AlertDialog.Builder thoughtsDialogBuilder = new AlertDialog.Builder(this);

            View practiceResponseDialogContents =
                    getLayoutInflater().inflate(R.layout.dialog_practice_response, null);

            thoughtsDialogBuilder.setView(practiceResponseDialogContents);

            final TextInputEditText messageView = (TextInputEditText) practiceResponseDialogContents
                    .findViewById(R.id.practice_response);
            final AppCompatRatingBar ratingBar = (AppCompatRatingBar) practiceResponseDialogContents
                    .findViewById(R.id.practice_rating);

            thoughtsDialogBuilder
                .setCancelable(true)
                .setIcon(R.drawable.ic_record_voice_over_white_24dp)
                .setTitle(R.string.practice_response_title)
                .setPositiveButton(R.string.save,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String message = messageView.getText().toString();
                                float rating = ratingBar.getRating();
                                String practiceId = Utils.getUUID();

                                mOutlineDbHelper.savePractice(
                                        mPresentation.getId(), rating, message, practiceId);

                                ArrayList<OutlineItem> savedItems =
                                        savePracticeTrackedInfo(outline, practiceId);

                                mPractices.add(0, new Practice(practiceId, mPresentation.getId(),
                                        rating, message, Utils.getDateTimeStamp(), savedItems));
                                mRecyclerView.getAdapter().notifyDataSetChanged();

                                showMessageIfEmpty();
                            }
                        }
                )
                .setNegativeButton(R.string.practice_response_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                savePracticeTrackedInfo(outline, "");
                            }
                        }
                );

            AlertDialog dialog = thoughtsDialogBuilder.create();
            dialog.show();
        }
    }

    private ArrayList<OutlineItem> savePracticeTrackedInfo(Outline outline, String practiceId) {
        ArrayList<OutlineItem> savedItems = new ArrayList<>();
        if (SettingsUtils.getTimerTrack(getApplicationContext())) {
            // we will save any recorded timings to the database here
            for (OutlineItem item : outline.getItems()) {
                if (item.getTimedDuration() > 0) {
                    OutlineItem durationItem = OutlineItem.createDurationItem(item);
                    durationItem.setPracticeId(practiceId);
                    mOutlineDbHelper.saveOutlineItem(durationItem);
                    savedItems.add(durationItem);
                }
            }
        }
        return savedItems;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getCallingActivity() != null) {
            returnActivityResult();
        } else {
            navigateUpOrBack(this, getIntent().getExtras(), PresentationMainActivity.class);
        }
    }

    private void returnActivityResult() {
        Intent intent = new Intent();
        if (mPresentation != null) {
            intent.putExtra(Utils.INTENT_PRESENTATION_ID, mPresentation.getId());
        }
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public static class PracticeSettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        private boolean isPractice;

        public PracticeSettingsFragment () {}

        public void setPractice(boolean isp) {
            isPractice = isp;
            if (isPractice) {
                addPreferencesFromResource(R.xml.settings_practice);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_timer);

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
            switch(key) {
                case SettingsUtils.PREF_TIMER_RECORD:
                    Toast.makeText(getActivity(), "Recording isn't implemented yet", Toast.LENGTH_SHORT).show();
                    break;
                case SettingsUtils.PREF_TIMER_DISABLE:
                    Toast.makeText(getActivity(), "Disabling phone isn't implemented yet", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }

}
