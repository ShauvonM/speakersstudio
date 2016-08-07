package com.thespeakers_studio.thespeakersstudioapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.fragment.PresentationPracticeDialog;
import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.utils.AnalyticsHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.OutlineHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 8/5/2016.
 */
public class PracticeSetupActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = makeLogTag(OutlineActivity.class);
    private static final String SCREEN_LABEL = "Presentation Outline";

    public static final int REQUEST_CODE = 4;

    private PresentationData mPresentation;

    private View mContentWrapper;

    private View mStartWrapper;
    private FloatingActionButton mStartButton;

    private OutlineHelper mHelper;
    private Outline mOutline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_practice_setup);

        AnalyticsHelper.sendScreenView(SCREEN_LABEL);

        setPresentationId(getIntent().getStringExtra(Utils.INTENT_PRESENTATION_ID));

        mContentWrapper = findViewById(R.id.content_wrapper);

        mStartWrapper = findViewById(R.id.fab_practice_wrapper);

        TextView presentationName = (TextView) findViewById(R.id.presentation_name);
        TextView presentationDuration = (TextView) findViewById(R.id.presentation_duration);
        View fab = findViewById(R.id.fab_practice);

        if (presentationName != null && presentationDuration != null) {
            if (mOutline != null) {
                presentationName.setText(mOutline.getTitle());
                presentationDuration.setText(mOutline.getDuration());
            } else {
                presentationName.setVisibility(View.GONE);
                presentationDuration.setVisibility(View.GONE);
            }
        }

        if (mOutline == null) {
            setTitle(R.string.timer);
        }

        if (fab != null) {
            fab.setOnClickListener(this);
        }

        findViewById(R.id.checkbox_record).setOnClickListener(this);
        findViewById(R.id.checkbox_track).setOnClickListener(this);
        findViewById(R.id.checkbox_disable_phone).setOnClickListener(this);
    }

    private void setPresentationId(String id) {
        if (id != null) {
            mPresentation = mDbHelper.loadPresentationById(id);
            mOutline = Outline.fromPresentation(this, mPresentation);
        }
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

        int wrapperHeight = mStartWrapper.getHeight();
        int headerHeight = getSupportActionBar() != null ? getSupportActionBar().getHeight()
                : 0;
        int headerDetailsHeight = getNewHeaderDetailsHeight(scrollY);
        int startPos = headerDetailsHeight + headerHeight - (wrapperHeight / 2);
        int endPos = 0;

        float ratio = (float) headerDetailsHeight / (float) mHeaderDetailsHeightPixels;

        mStartWrapper.setTranslationY(endPos + ((startPos - endPos) * ratio));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_practice:
                boolean delay = ((CheckBox) findViewById(R.id.checkbox_delay)).isChecked();
                boolean displayTimer = ((CheckBox) findViewById(R.id.checkbox_show_timer)).isChecked();
                boolean showWarning = ((CheckBox) findViewById(R.id.checkbox_show_warning)).isChecked();
                boolean track = ((CheckBox) findViewById(R.id.checkbox_track)).isChecked();
                boolean vibrate = ((CheckBox) findViewById(R.id.checkbox_vibrate)).isChecked();
                boolean recordVideo = ((CheckBox) findViewById(R.id.checkbox_record)).isChecked();
                boolean disablePhone = ((CheckBox) findViewById(R.id.checkbox_disable_phone)).isChecked();

                startPractice(delay, displayTimer, showWarning, track, vibrate, recordVideo, disablePhone);
            break;
            case R.id.checkbox_record:
                Toast.makeText(PracticeSetupActivity.this, "Recording isn't implemented yet", Toast.LENGTH_SHORT).show();
                ((CheckBox) findViewById(R.id.checkbox_record)).setChecked(false);
                break;
            case R.id.checkbox_track:
                Toast.makeText(PracticeSetupActivity.this, "Tracking isn't implemented yet", Toast.LENGTH_SHORT).show();
                ((CheckBox) findViewById(R.id.checkbox_track)).setChecked(false);
                break;
            case R.id.checkbox_disable_phone:
                Toast.makeText(PracticeSetupActivity.this, "Disabling phone isn't implemented yet", Toast.LENGTH_SHORT).show();
                ((CheckBox) findViewById(R.id.checkbox_disable_phone)).setChecked(false);
                break;
        }
    }

    private void startPractice(boolean delay, boolean displayTimer, boolean showWarning, boolean track, boolean vibrate, boolean recordVideo, boolean disablePhone) {
        if (mOutline != null) {
            PresentationPracticeDialog dialog = new PresentationPracticeDialog();
            dialog.setup(mOutline, delay, displayTimer, showWarning, track, vibrate);

            dialog.show(getSupportFragmentManager(), PresentationPracticeDialog.TAG);
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            Toast.makeText(this, "Custom timers aren't working yet. I know, I'm sorry.", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case android.R.id.home:
                if (mOutline != null) {
                    onBackPressed();
                } else {
                    super.onOptionsItemSelected(item);
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mOutline != null) {
            navigateUpOrBack(this, getIntent().getExtras(), EditPresentationActivity.class);
        } else {
            super.onBackPressed();
        }
    }

    private void returnActivityResult() {
        Intent intent = new Intent();
        if (mPresentation != null) {
            intent.putExtra(Utils.INTENT_PRESENTATION_ID, mPresentation.getId());
        }
        setResult(0, intent);
        finish();
    }
}
