package com.thespeakers_studio.thespeakersstudioapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;
import com.thespeakers_studio.thespeakersstudioapp.ui.PromptListHeaderView;
import com.thespeakers_studio.thespeakersstudioapp.ui.SmartScrollView;
import com.thespeakers_studio.thespeakersstudioapp.ui.PromptListView;
import com.thespeakers_studio.thespeakersstudioapp.utils.AnalyticsHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGE;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 8/1/2016.
 */
public class PresentationPromptListActivity extends BaseActivity implements
            PromptListView.PromptListListener {

    public final static String INTENT_STEP = "intent_presentation_open_step";

    public final static int REQUEST_CODE = 1;

    private static final String TAG = makeLogTag(PresentationPromptListActivity.class);
    private static final String SCREEN_LABEL = "Presentation Step List";

    private String mPresentationId;
    private int mStep;

    private PresentationData mPresentation;
    private ArrayList<Prompt> mPromptData;

    private PromptListView mPromptList;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_presentation_prompt_list);

        AnalyticsHelper.sendScreenView(SCREEN_LABEL);

        mPromptList = (PromptListView) findViewById(R.id.prompt_list);
        if (mPromptList != null) {
            mPromptList.setPromptListListener(this);
        } else {
            LOGE(TAG, "Prompt List View is null!");
        }

        //TODO: registerHideableHeaderView(findViewById(R.id.headerbar));

        mPresentationId = getIntent().getStringExtra(Utils.INTENT_PRESENTATION_ID);
        mPresentation = mDbHelper.loadPresentationById(mPresentationId);
        mStep = getIntent().getIntExtra(INTENT_STEP, 1);

        setStep();
    }

    /*
    @Override
    public void onBackPressed() {
        navigateUpOrBack(PresentationPromptListActivity.this,
                EditPresentationActivity.class);
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_in_presentation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.menu_action_reset:
                resetPresentation();
                break;
            case android.R.id.home:
                onNextStep(-1);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onNextStep(int step) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_STEP, step + 1);
        intent.putExtra(Utils.INTENT_PRESENTATION_ID, mPresentationId);
        // TODO: add progress amount?
        setResult(step + 1, intent);
        finish();
    }

    @Override
    protected void setLayoutPadding(int actionBarSize) {
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mPromptList.getLayoutParams();
        if (mlp.topMargin != actionBarSize) {
            mlp.topMargin = actionBarSize;
            mPromptList.setLayoutParams(mlp);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // TODO: enableActionBarAutoHide((CollectionView) findViewById());
    }

    private void resetPresentation() {

    }

    public void setStep () {
        if (mStep > 0) {
            mPromptData = mPresentation.getPromptsForStep(mStep);
            LOGD(TAG, "Step count: " + mPromptData.size());
            mPromptList.setData(mPromptData);

            setupHeaderDetails();
        }
    }

    @Override
    public void onSaveItem(Prompt prompt) {
        mDbHelper.savePrompt(mPresentationId, prompt);
        animateProgress();
        setTitle(mPresentation.getTopic());
    }

    @Override
    protected int getMinHeaderHeight() {
        return ((PromptListHeaderView) mHeaderDetails).getMinHeight();
    }

    private void setupHeaderDetails() {
        String headerText;
        String label;
        switch(mStep) {
            case 1:
                headerText = getString(R.string.details);
                label = getString(R.string.step_1);
                break;
            case 2:
                headerText = getString(R.string.landscape);
                label = getString(R.string.step_2);
                break;
            case 3:
                headerText = getString(R.string.specifics);
                label = getString(R.string.step_3);
                break;
            case 4:
                headerText = getString(R.string.content);
                label = getString(R.string.step_4);
                break;
            default:
                headerText = "";
                label = "";
                break;
        }

        ((TextView) findViewById(R.id.step_label)).setText(label);
        ((TextView) findViewById(R.id.step_name)).setText(headerText);

        animateProgress();
    }

    private void animateProgress() {
        ((PromptListHeaderView) mHeaderDetails).animateFillFactor(mPresentation.getCompletionPercentage(mStep));
    }
}
