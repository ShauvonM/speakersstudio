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
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.Layout;
import android.view.LayoutInflater;
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
import com.thespeakers_studio.thespeakersstudioapp.adapter.PracticeListAdapter;
import com.thespeakers_studio.thespeakersstudioapp.data.OutlineDbHelper;
import com.thespeakers_studio.thespeakersstudioapp.fragment.PresentationPracticeDialog;
import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.model.Practice;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
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

    private static final String TAG = makeLogTag(OutlineActivity.class);
    private static final String SCREEN_LABEL = "Presentation Outline";

    private PresentationData mPresentation;

    private View mContentWrapper;

    private FloatingActionButton mStartButton;

    private boolean isPractice;

    private OutlineHelper mHelper;
    private Outline mOutline;

    private RecyclerView mRecyclerView;

    private boolean mHeaderSetup;

    private float mMaxFABElevation;

    private int mDuration;

    private OutlineDbHelper mOutlineDbHelper;
    private ArrayList<Practice> mPractices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_practice_setup);

        AnalyticsHelper.sendScreenView(SCREEN_LABEL);

        mMaxFABElevation = getResources().getDimensionPixelSize(R.dimen.max_fab_elevation);

        mContentWrapper = findViewById(R.id.content_wrapper);
        mStartButton = (FloatingActionButton) findViewById(R.id.fab_practice);

        mRecyclerView = (RecyclerView) findViewById(R.id.saved_practice_list);
        mRecyclerView.setHasFixedSize(true);

        mDuration = SettingsUtils.getDefaultTimerDuration(this);

        setPresentationId(getIntent().getStringExtra(Utils.INTENT_PRESENTATION_ID));
    }

    private void setPresentationId(String id) {
        if (id != null) {
            mPresentation = mDbHelper.loadPresentationById(id);
            mOutline = Outline.fromPresentation(this, mPresentation);

            if (mPresentation != null) {
                // load the saved practices for this presentation
                mOutlineDbHelper = new OutlineDbHelper(this);

                mPractices = mOutlineDbHelper.getPractices(mPresentation.getId());
                PracticeListAdapter adapter = new PracticeListAdapter(mPractices);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL, false));
                mRecyclerView.setAdapter(adapter);

                showMessageIfEmpty();
            }
        }
        isPractice = mOutline != null;

        setup();
    }

    private void showMessageIfEmpty() {
        if (mRecyclerView.getAdapter().getItemCount() == 0) {
            findViewById(R.id.no_results).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.no_results).setVisibility(View.GONE);
        }
    }

    private void setup() {
        TextView presentationName = (TextView) findViewById(R.id.presentation_name);
        TextView presentationDuration = (TextView) findViewById(R.id.presentation_duration);
        EditText timerDurationInput = (EditText) findViewById(R.id.timer_duration_input);
        View fab = findViewById(R.id.fab_practice);

        final PracticeSettingsFragment fragment = (PracticeSettingsFragment) getFragmentManager()
                .findFragmentById(R.id.settings_fragment);
        fragment.setPractice(isPractice);

        if (isPractice) {
            timerDurationInput.setVisibility(View.GONE);
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
                //EditText timerDurationInput = (EditText) findViewById(R.id.timer_duration_input);
                //timerDurationInput.requestFocus();
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
            dialog.setInterface(this);
        } else {
            mDuration = Integer.parseInt(((TextView) findViewById(R.id.timer_duration_input))
                    .getText().toString());
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
    public void onDialogDismissed(Outline outline, boolean finished) {
        if (SettingsUtils.getTimerTrack(getApplicationContext()) && finished) {
            // we will save any recorded timings to the database here
            for (OutlineItem item : outline.getItems()) {
                if (item.getTimedDuration() > 0) {
                    OutlineItem durationItem = OutlineItem.createDurationItem(item);
                    mOutlineDbHelper.saveOutlineItem(durationItem);
                }
            }
        }

        if (finished) {
            // save user's thoughts on this practice
            AlertDialog.Builder thoughtsDialogBuilder = new AlertDialog.Builder(this);

            View practiceResponseDialogContents =
                    getLayoutInflater().inflate(R.layout.practice_response_dialog, null);

            thoughtsDialogBuilder.setView(practiceResponseDialogContents);

            final TextInputEditText messageView = (TextInputEditText) practiceResponseDialogContents
                    .findViewById(R.id.practice_response);
            final AppCompatRatingBar ratingBar = (AppCompatRatingBar) practiceResponseDialogContents
                    .findViewById(R.id.practice_rating);

            thoughtsDialogBuilder
                    .setCancelable(false)
                    .setTitle(R.string.practice_response_title)
                    .setPositiveButton(R.string.save,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String message = messageView.getText().toString();
                                    float rating = ratingBar.getRating();
                                    mOutlineDbHelper.savePracticeResponse(
                                            mPresentation.getId(), rating, message);

                                    mPractices = mOutlineDbHelper.getPractices(mPresentation.getId());
                                    mRecyclerView.getAdapter().notifyDataSetChanged();
                                }
                            }
                    )
                    .setNegativeButton(R.string.practice_response_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }
                    );

            AlertDialog dialog = thoughtsDialogBuilder.create();
            dialog.show();
        }
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
