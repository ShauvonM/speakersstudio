package com.thespeakers_studio.thespeakersstudioapp.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.thebluealliance.spectrum.SpectrumDialog;
import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.fragment.PresentationPromptListFragment;
import com.thespeakers_studio.thespeakersstudioapp.fragment.PresentationStepListFragment;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.ui.PromptListHeaderView;
import com.thespeakers_studio.thespeakersstudioapp.ui.StepListView;
import com.thespeakers_studio.thespeakersstudioapp.utils.AnalyticsHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.PresentationUtils;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGE;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 8/1/2016.
 */
public class EditPresentationActivity extends BaseActivity implements
        FragmentManager.OnBackStackChangedListener,
        PresentationStepListFragment.OnStepSelectedListener,
        PresentationPromptListFragment.PromptSaveListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = makeLogTag(EditPresentationActivity.class);
    private static final String SCREEN_LABEL = "Presentation Step List";

    private static final String STATE_PRESENTATION_ID = "presentation_id";
    private static final String STATE_STEP = "step";

    private PresentationData mPresentation;
    private int mCurrentStep;

    private View mFragmentContainer;

    private PresentationStepListFragment mStepListFragment;
    private PresentationPromptListFragment mPromptListFragment;

    private ArrayList<LocationSelectedListener> mLocationListeners = new ArrayList<>();

    public interface LocationSelectedListener {
        public void onLocationSelected(Place p);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_presentation);

        AnalyticsHelper.sendScreenView(SCREEN_LABEL);

        mFragmentContainer = findViewById(R.id.fragment_container);

        FragmentManager fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(this);

        mStepListFragment =
                (PresentationStepListFragment)
                        fm.findFragmentByTag(PresentationStepListFragment.TAG);

        mPromptListFragment =
                (PresentationPromptListFragment)
                        fm.findFragmentByTag(PresentationPromptListFragment.TAG);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (mStepListFragment == null) {
            mStepListFragment = new PresentationStepListFragment();
            ft.add(R.id.fragment_container, mStepListFragment,
                    PresentationStepListFragment.TAG);
            ft.hide(mStepListFragment);
        }

        if (mPromptListFragment == null) {
            mPromptListFragment = new PresentationPromptListFragment();
            ft.add(R.id.fragment_container, mPromptListFragment,
                    PresentationPromptListFragment.TAG);
            ft.hide(mPromptListFragment);
        }

        ft.commit();

        if (savedInstanceState != null) {
            mCurrentStep = savedInstanceState.getInt(STATE_STEP);
            setPresentationId(savedInstanceState.getString(STATE_PRESENTATION_ID));
        } else {
            mCurrentStep = 0;
            setPresentationId(getIntent().getStringExtra(Utils.INTENT_PRESENTATION_ID));
        }
    }

    private void setPresentationId(String id) {
        if (id != null) {
            LOGD(TAG, "Opening presentation " + id);

            mPresentation = mDbHelper.loadPresentationById(id);
            setTitle(mPresentation.getTopic());

            if (mPresentation != null) {
                setup();
            } else {
                LOGE(TAG, "No Presentation was found");
            }
        }
    }

    private void setup() {
        mStepListFragment.setPresentation(mPresentation);
        mPromptListFragment.setPresentation(mPresentation);
        onStepSelected(mCurrentStep);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (title.length() == 0) {
            super.setTitle(R.string.new_presentation);
        } else {
            super.setTitle(title);
        }
    }

    @Override
    protected void setLayoutPadding(int actionBarSize) {
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mFragmentContainer.getLayoutParams();
        if (mlp.topMargin != actionBarSize) {
            mlp.topMargin = actionBarSize;
            mFragmentContainer.setLayoutParams(mlp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        MenuInflater inflater = getMenuInflater();
        int menuId = R.menu.menu_in_presentation;
        inflater.inflate(menuId, menu);

        if (mPresentation == null || mPresentation.getCompletionPercentage() < 1) {
            menu.findItem(R.id.menu_action_practice).setVisible(false);
        } else {
            menu.findItem(R.id.menu_action_practice).setVisible(true);
        }

        onBackStackChanged();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.menu_action_color:
                PresentationUtils.showColorDialog(this, mPresentation,
                    new SpectrumDialog.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                            mDbHelper.savePresentationColor(mPresentation.getId(), color);
                            getIntent().putExtra(Utils.INTENT_THEME_ID,
                                    PresentationUtils.getThemeForColor(getApplicationContext(), color));
                            recreate();
                        }
                    }
                );

                break;
            case R.id.menu_action_reset:
                PresentationUtils.resetPresentation(this, mPresentation,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mPresentation == null) {
                                    return;
                                }
                                mDbHelper.resetPresentation(mPresentation);
                            }
                        }
                );
                break;
            case R.id.menu_action_practice:
                Intent intent = new Intent(getApplicationContext(), PracticeSetupActivity.class);
                intent.putExtra(Utils.INTENT_PRESENTATION_ID, mPresentation.getId());
                startActivityForResult(intent, Utils.REQUEST_CODE_PRACTICE);
                break;
            case R.id.menu_action_delete:
                delete();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void delete() {
        String message = getResources().getQuantityString(R.plurals.confirm_delete_message,
                    1);

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doDelete();
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
    private void doDelete() {
        mDbHelper.deletePresentation(mPresentation);
        navigateUpOrBack(this, null, PresentationMainActivity.class);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else if (getCallingActivity() != null) {
            returnActivityResult();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        Utils.hideKeyboard(this, findViewById(R.id.fragment_container));
        if (mStepListFragment != null && mStepListFragment.isVisible()) {
            hideHeaderDetails();
            // TODO: use a handler to delay this?
            mStepListFragment.animateProgressHeight();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_PRESENTATION_ID, mPresentation.getId());
        // we don't want to store that we are on step 5
        outState.putInt(STATE_STEP, mCurrentStep < 5 ? mCurrentStep : 0);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case Utils.REQUEST_CODE_OUTLINE:
                setPresentationId(data.getStringExtra(Utils.INTENT_PRESENTATION_ID));
                break;
            case Utils.REQUEST_CODE_LOCATION_SELECTED:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    for(LocationSelectedListener l : mLocationListeners) {
                        l.onLocationSelected(place);
                    }
                    Log.i("SS", "Place: " + place.getName());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    // TODO: handle this
                    Log.i("SS", status.getStatusMessage());
                } else if (resultCode == RESULT_CANCELED) {
                    // the user canceled the thing
                }
                break;
        }
    }

    private void returnActivityResult() {
        Intent intent = new Intent();
        intent.putExtra(Utils.INTENT_PRESENTATION_ID, mPresentation.getId());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void addLocationCallback(LocationSelectedListener listener) {
        if (!mLocationListeners.contains(listener)) {
            mLocationListeners.add(listener);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO: handle google api connection errors
    }

    @Override
    public void onStepSelected(int step) {
        mCurrentStep = step;
        if (step == 0) {
            showStepList();
        } else {
            openStep(step);
        }
    }

    @Override
    protected int getMinHeaderHeight() {
        if (mStepListFragment == null || mStepListFragment.isVisible()) {
            return 0;
        } else {
            return ((PromptListHeaderView) mHeaderDetails).getMinHeight();
        }
    }

    @Override
    public void onPromptSave(Prompt prompt) {
        mDbHelper.savePrompt(mPresentation.getId(), prompt);
        animateHeaderProgress();
        setTitle(mPresentation.getTopic());
    }

    @Override
    public void onNextStep(int step) {
        final int nextStep = step + 1;
        mStepListFragment.setOnProgressAnimationListener(new StepListView.OnProgressAnimationListener() {
            @Override
            public void onProgressAnimationFinished() {
                onStepSelected(nextStep);
                mStepListFragment.clearOnProgressAnimationListener();
            }
        });
        getSupportFragmentManager().popBackStack();
    }

    private void showStepList() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.show(mStepListFragment);
        ft.commitAllowingStateLoss();
    }

    private void openStep(int step) {
        if (step == 5) {
            // set mCurrentStep to 0 in case we go back to here
            // this will make sure it shows the step list, and it won't just keep going to the outline
            mCurrentStep = 0;

            // open the outline activity
            Intent intent = new Intent(getApplicationContext(), OutlineActivity.class);
            //int code = OutlineActivity.REQUEST_CODE;
            intent.putExtra(Utils.INTENT_PRESENTATION_ID, mPresentation.getId());
            startActivityForResult(intent, Utils.REQUEST_CODE_OUTLINE);
            //createBackStack(intent);
        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

            ft.hide(mStepListFragment);

            mPromptListFragment.setStep(step);

            ft.show(mPromptListFragment);

            ft.addToBackStack(PresentationPromptListFragment.TAG);
            ft.commit();

            setupHeaderDetails(step);
        }
    }

    private void setupHeaderDetails(int step) {
        String headerText;
        String label;
        switch(step) {
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
                return;
        }

        ((TextView) findViewById(R.id.step_label)).setText(label);
        ((TextView) findViewById(R.id.step_name)).setText(headerText);

        showHeaderDetails();
        animateHeaderProgress();
    }

    private void animateHeaderProgress() {
        ((PromptListHeaderView) mHeaderDetails)
                .animateFillFactor(mPresentation.getCompletionPercentage(mCurrentStep));
    }

    // animates the header opening, which shows the selected step
    private void showHeaderDetails() {
        ((PromptListHeaderView) mHeaderDetails).reset();
        mHeaderDetails.measure(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        final int detailsHeight = mHeaderDetails.getMeasuredHeight();
        final int actionBarSize = Utils.calculateActionBarSize(this);
        final int fullHeaderHeight = actionBarSize + detailsHeight;

        Animation showHeaderAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int h = (int) (detailsHeight * interpolatedTime);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                        mHeaderDetails.getLayoutParams();
                params.height = h;
                mHeaderDetails.setLayoutParams(params);

                /// /(actionBarSize + ((int) (detailsHeight * interpolatedTime)));
                mPromptListFragment.setMargin(h);
            }
        };
        showHeaderAnimation.setDuration(SettingsUtils.PROMPT_HEADER_SLIDE_DURATION);
        showHeaderAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mHeaderDetailsHeightPixels = detailsHeight;
                mHeaderHeightPixels = fullHeaderHeight;
                mHeaderDetails.clearAnimation();
                onScrollChanged(0,0);
            }
        });

        mHeaderDetails.startAnimation(showHeaderAnimation);
    }

    private void hideHeaderDetails() {
        final int actionBarSize = Utils.calculateActionBarSize(this);
        final int detailsHeight = mHeaderDetails.getMeasuredHeight();

        Animation hideHeaderAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int h = detailsHeight - ((int) (detailsHeight * interpolatedTime));

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                        mHeaderDetails.getLayoutParams();
                params.height = h;
                mHeaderDetails.setLayoutParams(params);
            }
        };
        hideHeaderAnimation.setDuration(SettingsUtils.PROMPT_PROGRESS_ANIMATION_DURATION);
        hideHeaderAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mHeaderDetailsHeightPixels = 0;
                mHeaderHeightPixels = actionBarSize;
                mHeaderDetails.clearAnimation();
                onScrollChanged(0,0);
            }
        });

        mHeaderDetails.startAnimation(hideHeaderAnimation);
    }
}
