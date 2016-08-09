package com.thespeakers_studio.thespeakersstudioapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.design.widget.FloatingActionButton;
import android.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.fragment.PresentationPracticeDialog;
import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.utils.AnalyticsHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.OutlineHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 8/5/2016.
 */
public class PracticeSetupActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = makeLogTag(OutlineActivity.class);
    private static final String SCREEN_LABEL = "Presentation Outline";

    private PresentationData mPresentation;

    private View mContentWrapper;

    private FloatingActionButton mStartButton;

    private boolean isPractice;

    private OutlineHelper mHelper;
    private Outline mOutline;

    private boolean mHeaderSetup;

    private float mMaxFABElevation;

    private int mDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_practice_setup);

        AnalyticsHelper.sendScreenView(SCREEN_LABEL);

        mMaxFABElevation = getResources().getDimensionPixelSize(R.dimen.max_fab_elevation);

        mContentWrapper = findViewById(R.id.content_wrapper);
        mStartButton = (FloatingActionButton) findViewById(R.id.fab_practice);

        mDuration = SettingsUtils.getDefaultTimerDuration(this);

        setPresentationId(getIntent().getStringExtra(Utils.INTENT_PRESENTATION_ID));
    }

    private void setPresentationId(String id) {
        if (id != null) {
            mPresentation = mDbHelper.loadPresentationById(id);
            mOutline = Outline.fromPresentation(this, mPresentation);
        }
        isPractice = mOutline != null;

        setup();
    }

    private void setup() {
        TextView presentationName = (TextView) findViewById(R.id.presentation_name);
        TextView presentationDuration = (TextView) findViewById(R.id.presentation_duration);
        EditText timerDurationInput = (EditText) findViewById(R.id.timer_duration_input);
        View fab = findViewById(R.id.fab_practice);

        final PracticeSettingsFragment fragment = (PracticeSettingsFragment) getFragmentManager().findFragmentById(R.id.settings_fragment);
        fragment.setPractice(isPractice);

        if (isPractice) {
            presentationName.setText(mOutline.getTitle());
            presentationDuration.setText(mOutline.getDuration());
        } else {
            //presentationName.setVisibility(View.GONE);
            presentationDuration.setVisibility(View.GONE);
            //durationSpinner.setVisibility(View.VISIBLE);

            timerDurationInput.setVisibility(View.VISIBLE);
            timerDurationInput.setText(String.valueOf(mDuration));
            presentationName.setText(R.string.minutes);
            presentationName.setOnClickListener(this);
            /*
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.presentation_duration_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            durationSpinner.setAdapter(adapter);
            durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch(position) {
                        case 0:
                            mDuration = 5;
                            break;
                        case 1:
                            mDuration = 10;
                            break;
                        case 2:
                            mDuration = 20;
                            break;
                        case 3:
                            mDuration = 30;
                            break;
                        case 4:
                            mDuration = -1;
                            showCustomDuration();
                            return;
                        default:
                            mDuration = 0;
                            break;
                    }
                    hideCustomDuration();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    mDuration = 0;
                    hideCustomDuration();
                }
            });
            */

            setTitle(R.string.timer);
            ((TextView) findViewById(R.id.no_results)).setText(R.string.no_saved_timers);
        }

        fab.setOnClickListener(this);

        /* WIP - use the scroll magic to collapse the settings section
        DO NOT REMOVE

        final View settingsView = findViewById(R.id.practice_settings);
        final View contentView = findViewById(R.id.saved_timer_list);
        final View settingsLabel = findViewById(R.id.settings_label);

        ViewTreeObserver vto = settingsView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (!mHeaderSetup) {
                        int height = settingsView.getHeight();
                        int layoutHeight = contentView.getHeight();

                        int h = height; // - settingsLabel.getHeight();
                        if (layoutHeight < h) {
                            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) contentView.getLayoutParams();
                            layoutParams.height = h;
                            contentView.setLayoutParams(layoutParams);
                        }

                        LOGD(TAG, "h " + h);

                        findViewById(R.id.headerbar_scroll).scrollBy(0, h);
                        mHeaderSetup = true;
                    }
                }
            });
        }
        */
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
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mContentWrapper.getLayoutParams();
        if (mlp.topMargin != actionBarSize) {
            mlp.topMargin = actionBarSize;
            mContentWrapper.setLayoutParams(mlp);
        }
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        super.onScrollChanged(deltaX, deltaY);

        int scrollY = findViewById(R.id.headerbar_scroll).getScrollY();

        int fabHeight = getResources().getDimensionPixelSize(R.dimen.fab_button) +
                (getResources().getDimensionPixelSize(R.dimen.fab_margin) * 2);
        int headerHeight = getSupportActionBar() != null ? getSupportActionBar().getHeight() : 0;
        int baseHeight = mHeaderDetailsHeightPixels + headerHeight;

        float newHeight = Math.min(baseHeight, (float)scrollY); // (scrollY * 0.4));

        //float ratio = (float) headerDetailsHeight / (float) mHeaderDetailsHeightPixels;
        //mStartWrapper.setTranslationY(endPos + ((startPos - endPos) * ratio));
        mStartButton.setTranslationY(baseHeight - newHeight - (fabHeight / 2));

        float gapFillProgress;
        gapFillProgress = Math.min(
                Math.max(Utils.getProgress(scrollY, 0, mHeaderHeightPixels), 0),
                1);

        ViewCompat.setElevation(mStartButton, (mMaxFABElevation / 2) + (gapFillProgress * (mMaxFABElevation / 2)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.presentation_name:
                EditText timerDurationInput = (EditText) findViewById(R.id.timer_duration_input);
                timerDurationInput.requestFocus();
                break;
            case R.id.fab_practice:
                startPractice();
            break;
        }
    }

    private void startPractice() {
        PresentationPracticeDialog dialog = new PresentationPracticeDialog();
        if (isPractice) {
            dialog.setup(mOutline);
        } else {
            mDuration = Integer.parseInt(((TextView) findViewById(R.id.timer_duration_input)).getText().toString());
            if (mDuration <= 0) {
                return;
            }
            SettingsUtils.setDefaultTimerDuration(this, mDuration);
            dialog.setup(mDuration);
        }
        dialog.show(getSupportFragmentManager(), PresentationPracticeDialog.TAG);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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

        public PracticeSettingsFragment () {
        }

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
