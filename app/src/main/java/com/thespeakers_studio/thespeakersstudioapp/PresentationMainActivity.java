package com.thespeakers_studio.thespeakersstudioapp;

import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class PresentationMainActivity extends AppCompatActivity implements
        PresentationListFragment.PresentationListFragmentHandler,
        PresentationStepListFragment.OnStepSelectedListener,
        PresentationPromptListFragment.PromptSaveListener,
        GoogleApiClient.OnConnectionFailedListener,
        FragmentManager.OnBackStackChangedListener {

    private PresentationDbHelper mDbHelper;
    private ArrayList<PresentationData> mPresentations;
    private String mPresentationId;

    private GoogleApiClient mGoogleApiClient;
    private LocationSelectedListener mLocationListener;

    private PresentationListFragment mPresentationListFragment;
    private PresentationStepListFragment mStepListFragment;
    private PresentationPromptListFragment mPromptListFragment;
    private PresentationOutlineFragment mOutlineFragment;

    private Menu mMenu;
    private String mCurrentFragment;

    static final String TAG_PRESENTATION_LIST = "presentation_list";
    static final String TAG_STEP_LIST = "step_list";
    static final String TAG_PROMPT_LIST = "prompt_list";
    static final String TAG_OUTLINE = "presentation_outline";

    static final String STATE_PRESENTATION_ID = "presentationId";
    static final String STATE_FRAGMENT = "openFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("SS", "On Create!");
        setContentView(R.layout.activity_presentation_main);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

        mDbHelper = new PresentationDbHelper(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle();

        if (savedInstanceState == null) {
            mPresentationId = null;
        } else {
            mPresentationId = savedInstanceState.getString(STATE_PRESENTATION_ID);
        }

        FragmentManager fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(this);

        mPresentationListFragment = (PresentationListFragment) fm.findFragmentByTag(TAG_PRESENTATION_LIST);
        mStepListFragment = (PresentationStepListFragment) fm.findFragmentByTag(TAG_STEP_LIST);
        mPromptListFragment = (PresentationPromptListFragment) fm.findFragmentByTag(TAG_PROMPT_LIST);
        mOutlineFragment = (PresentationOutlineFragment) fm.findFragmentByTag(TAG_OUTLINE);

        FragmentTransaction ft = fm.beginTransaction();

        mPresentations = loadPresentations();

        if (mPresentationListFragment == null) {
            mPresentationListFragment = new PresentationListFragment();
            mPresentationListFragment.setPresentationData(mPresentations);
            ft.add(R.id.fragment_container, mPresentationListFragment, TAG_PRESENTATION_LIST);
        }

        if (mStepListFragment == null) {
            mStepListFragment = new PresentationStepListFragment();
            ft.add(R.id.fragment_container, mStepListFragment, TAG_STEP_LIST);
        }

        if (mPromptListFragment == null) {
            mPromptListFragment = new PresentationPromptListFragment();
            ft.add(R.id.fragment_container, mPromptListFragment, TAG_PROMPT_LIST);
        }

        if (mOutlineFragment == null) {
            mOutlineFragment = new PresentationOutlineFragment();
            ft.add(R.id.fragment_container, mOutlineFragment, TAG_OUTLINE);
        }

        ft.commit();

        if (savedInstanceState == null) {
            showPresentationList();
        } else {
            switch(savedInstanceState.getString(STATE_FRAGMENT)) {
                case TAG_PROMPT_LIST:
                    onPromptListShown();
                    break;
                case TAG_STEP_LIST:
                    onStepListShown();
                    break;
                case TAG_OUTLINE:
                    onOutlineShown();
                default:
                    onPresentationListShown();
                    break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_PRESENTATION_ID, mPresentationId);
        outState.putString(STATE_FRAGMENT, mCurrentFragment);

        super.onSaveInstanceState(outState);
    }

    private void toggleCardViewType() {
        if (mPresentationListFragment.isVisible()) {
            boolean twoCol = mPresentationListFragment.toggleView();

            SharedPreferences.Editor editor = getSharedPreferences("presentation_list", 0).edit();
            editor.putBoolean("presentation_list_view_type", twoCol);
            editor.commit();

            if (twoCol) {
                mMenu.findItem(R.id.menu_action_view).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_view_agenda_white_24dp));
            } else {
                mMenu.findItem(R.id.menu_action_view).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_view_quilt_white_24dp));
            }
        } else {
            return;
        }
    }

    @Override
    public void onBackPressed() {
        if (mPresentationListFragment.getSelectedCount() > 0) {
            mPresentationListFragment.deselectAll();
            deselect();
        } else {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onBackStackChanged() {
        Utils.hideKeyboard(this, findViewById(R.id.fragment_container));
        if (mPresentationListFragment.isVisible()) {
            onPresentationListShown();
        } else if (mStepListFragment.isVisible()) {
            onStepListShown();
        } else if (mPromptListFragment.isVisible()) {
            onPromptListShown();
        } else if (mOutlineFragment.isVisible()) {
            onOutlineShown();
        }
    }

    private void showBackButton() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }
    private void hideBackButton() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(false);
        }
    }

    public void showMenuGroup(int id) {
        if (mMenu == null) {
            return;
        }
        mMenu.setGroupVisible(R.id.menu_group_main, false);
        mMenu.setGroupVisible(R.id.menu_group_presentation, false);
        mMenu.setGroupVisible(R.id.menu_group_selection, false);

        mMenu.setGroupVisible(id, true);
    }

    public void onPresentationListShown() {
        mCurrentFragment = TAG_PRESENTATION_LIST;
        mPresentationListFragment.refresh();
        mPresentationId = null;
        setTitle();
        hideBackButton();

        // reset the progress indication on the step list
        mStepListFragment.resetProgress();

        showMenuGroup(R.id.menu_group_main);
    }

    public void onStepListShown() {
        mCurrentFragment = TAG_STEP_LIST;
        // we are on the step list
        mStepListFragment.animateProgressHeight();
        mPromptListFragment.clearStep();
        setTitle();
        showBackButton();
        showMenuGroup(R.id.menu_group_presentation);
    }

    public void onPromptListShown() {
        mCurrentFragment = TAG_PROMPT_LIST;
        setTitle();
        showBackButton();
        showMenuGroup(R.id.menu_group_presentation);
    }

    public void onOutlineShown() {
        mCurrentFragment = TAG_OUTLINE;
        setTitle();
        showBackButton();
        showMenuGroup(R.id.menu_outline);
    }

    public PresentationData getSelectedPresentation() {
        if (mPresentations == null) {
            return null;
        }
        for (PresentationData pres : mPresentations) {
            if (pres.getId().equals(mPresentationId)) {
                return pres;
            }
        }
        return null;
    }

    public void setTitle() {
        PresentationData pres = getSelectedPresentation();
        ActionBar bar = getSupportActionBar();
        if (bar == null) {
            return;
        }

        Log.d("SS", "current fragment: " + mCurrentFragment);

        if (mCurrentFragment != null && mCurrentFragment.equals(TAG_OUTLINE)) {
            bar.setTitle(getResources().getString(R.string.outline));
        } else {
            if (pres != null) {
                bar.setTitle(pres.getTopic());
            } else {
                bar.setTitle(getResources().getString(R.string.saved_presentations));
            }
        }
    }

    public GoogleApiClient getGoogleApi() {
        return mGoogleApiClient;
    }

    private void showPresentationList() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(mPromptListFragment);
        ft.hide(mStepListFragment);
        ft.hide(mOutlineFragment);
        ft.show(mPresentationListFragment);
        ft.commit();

        onPresentationListShown();
    }

    @Override
    public void onOpenPresentation(String id) {
        Log.d("SS", "Presentation " + id + " opened");
        mPresentationId = id;
        mStepListFragment.setPresentation(getSelectedPresentation());
        mPromptListFragment.setPresentation(getSelectedPresentation());
        showStepList();
    }

    @Override
    public void onSelectPresentation(String presentationId) {
        Log.d("SS", "Presentation " + presentationId + " selected");
        ActionBar bar = getSupportActionBar();
        if (bar == null) {
            return;
        }
        bar.setTitle(String.valueOf(mPresentationListFragment.getSelectedCount()));
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorAccent)));

        showMenuGroup(R.id.menu_group_selection);
    }

    @Override
    public void onDeselectPresentation(String presentationId) {
        Log.d("SS", "Presentation " + presentationId + " deselected");

        ActionBar bar = getSupportActionBar();
        if (bar == null) {
            return;
        }
        bar.setTitle(String.valueOf(mPresentationListFragment.getSelectedCount()));

        if (mPresentationListFragment.getSelectedCount() <= 0) {
            deselect();
        }
    }

    private void deselect() {
        ActionBar bar = getSupportActionBar();
        if (bar == null) {
            return;
        }
        setTitle();
        bar.setDisplayHomeAsUpEnabled(false);
        showMenuGroup(R.id.menu_group_main);
        bar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
    }

    @Override
    public void onCreateNewPresentation() {
        PresentationData newPres = createNewPresentation();
        mPresentations.add(newPres);
        mPresentationId = newPres.getId();
        mStepListFragment.setPresentation(newPres);
        mPromptListFragment.setPresentation(newPres);
        showStepList();
    }

    private void showStepList() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

        ft.hide(mPresentationListFragment);
        ft.show(mStepListFragment);

        ft.addToBackStack("presentation_" + mPresentationId);
        ft.commit();
    }

    public void onStepSelected(int step) {
        Log.d("SS", "Step " + step + " selected");
        if (step < 5) {
            showStep(step);
        } else {
            showOutline();
        }
    }

    private void showStep(int step) {
        mPromptListFragment.setStep(step);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

        ft.hide(mStepListFragment);
        ft.show(mPromptListFragment);

        ft.addToBackStack("step_" + step);
        ft.commit();
    }

    private void showOutline() {
        mOutlineFragment.setOutline(Outline.fromPresentation(this, getSelectedPresentation()));

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

        ft.hide(mStepListFragment);
        ft.show(mOutlineFragment);

        mOutlineFragment.render();

        ft.addToBackStack("outline");
        ft.commit();
    }

    @Override
    public void onPromptSave(Prompt prompt) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String id;
        ContentValues values = new ContentValues();

        String datetime = Utils.getDateTimeStamp();

        ArrayList<PromptAnswer> answers = prompt.getAnswer();

        for (PromptAnswer answer : answers) {
            //
            values.put(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_KEY, answer.getKey());
            values.put(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_VALUE, answer.getValue());
            values.put(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_DATE_MODIFIED, datetime);
            values.put(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_LINK_ID, answer.getAnswerLinkId());

            answer.setModifiedDate(datetime);

            if (answer.getId().isEmpty()) {
                // if this is a new answer, we will add it to the database
                id = java.util.UUID.randomUUID().toString();

                values.put(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_ID, id);
                values.put(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PRESENTATION_ID, mPresentationId);
                values.put(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PROMPT_ID, prompt.getId());
                values.put(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_DATE_CREATED, datetime);

                // TODO: created by and modified by fields

                db.insert(PresentationDataContract.PresentationAnswerEntry.TABLE_NAME, null, values);

                // update the local record, so we know how to handle it from now on
                answer.setId(id);
                answer.setCreatedDate(datetime);
            } else {
                // if this is an existing answer, we'll update it
                id = answer.getId();

                if (answer.getValue().isEmpty()) {
                    db.delete(PresentationDataContract.PresentationAnswerEntry.TABLE_NAME,
                            PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_ID + " = ?",
                            new String[]{id});
                } else {
                    db.update(PresentationDataContract.PresentationAnswerEntry.TABLE_NAME,
                            values,
                            PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_ID + " = ?",
                            new String[]{id});
                }
            }
        }

        // update the modified date property on the presentation
        ContentValues presUpdate = new ContentValues();
        presUpdate.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_MODIFIED, datetime);
        db.update(PresentationDataContract.PresentationEntry.TABLE_NAME,
                presUpdate,
                PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID + " = ?",
                new String[]{mPresentationId});
        getSelectedPresentation().setModifiedDate(datetime);

        db.close();

        if (prompt.getId() == PresentationData.PRESENTATION_TOPIC) {
            setTitle();
        }
    }

    @Override
    public void onNextStep(int step) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

        final int nextStep = step + 1;
        if (nextStep < 5) {
            mStepListFragment.setOnProgressAnimationListener(new StepListView.OnProgressAnimationListener() {
                @Override
                public void onProgressAnimationFinished() {
                    showStep(nextStep);
                    mStepListFragment.clearOnProgressAnimationListener();
                }
            });
        }

        ft.hide(mPromptListFragment);
        ft.show(mStepListFragment);

        ft.addToBackStack("step_list");

        ft.commit();
    }

    private ArrayList<PresentationData> loadPresentations() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        ArrayList<PresentationData> presies = new ArrayList<>();

        // pick what columns to load
        String[] presProjection = {
                PresentationDataContract.PresentationEntry._ID,
                PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID,
                PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_MODIFIED
        };
        String[] ansProjection = {
                PresentationDataContract.PresentationAnswerEntry._ID,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PROMPT_ID,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PRESENTATION_ID,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_KEY,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_VALUE,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_ID,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_LINK_ID,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_DATE_CREATED,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_DATE_MODIFIED,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_CREATED_BY,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_MODIFIED_BY
        };

        // set up the clause to use on the answer db
        String answerSelection = PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PRESENTATION_ID + "=?";

        // set up sort clause
        String sortOrder = PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_MODIFIED + " DESC";

        // fetch the presentation
        Cursor presCursor = db.query(PresentationDataContract.PresentationEntry.TABLE_NAME, presProjection, null, null, null, null, sortOrder);

        presCursor.moveToFirst();
        try {
            while (!presCursor.isAfterLast()) {
                String presentationId = presCursor.getString(
                        presCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID)
                );
                String modifiedDate = presCursor.getString(
                        presCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_MODIFIED)
                );

                PresentationData pres = new PresentationData(getApplicationContext(), presentationId, modifiedDate);

                // set up the answer clause
                String[] answerSelectionValues = new String[] { String.valueOf(presentationId) };

                // fetch the answers
                Cursor answerCursor = db.query(PresentationDataContract.PresentationAnswerEntry.TABLE_NAME, ansProjection, answerSelection, answerSelectionValues, null, null, PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_KEY, null);

                pres.setAnswers(answerCursor);

                presies.add(pres);

                presCursor.moveToNext();
            }
        } finally {
            presCursor.close();
        }
        return presies;
    }

    private PresentationData createNewPresentation() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String id = UUID.randomUUID().toString();

        String datetime = Utils.getDateTimeStamp();

        int color = R.color.colorPrimary;

        ContentValues values = new ContentValues();
        values.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID, id);
        values.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_CREATED, datetime);
        values.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_MODIFIED, datetime);
        values.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_COLOR, color); // TODO: custom colors

        db.insert(PresentationDataContract.PresentationEntry.TABLE_NAME, null, values);
        return new PresentationData(getApplicationContext(), id, datetime);
    }

    private void resetPresentation() {
        if (mPresentationId == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.action_reset_presentation))
                .setMessage(getString(R.string.confirm_reset_message))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doResetPresentation();
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void doResetPresentation() {
        if (mPresentationId == null) {
            return;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(PresentationDataContract.PresentationAnswerEntry.TABLE_NAME, PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PRESENTATION_ID + "=?", new String[]{mPresentationId});
        db.close();

        getSelectedPresentation().resetAnswers();

        showStepList();
    }

    private void deleteSelectedPresentations() {
        int count = mPresentationListFragment.getSelectedCount();
        if (count == 0) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.action_reset_presentation))
                .setMessage(getResources().getQuantityString(R.plurals.confirm_delete_message,
                        count, count))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doDeleteSelectedPresentations();
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void doDeleteSelectedPresentations() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int[] remove = new int[mPresentationListFragment.getSelectedCount()];
        int ri = 0;
        int pi = 0;

        for (PresentationData pres : mPresentations) {
            if (pres.getIsSelected()) {
                String id = pres.getId();
                db.delete(PresentationDataContract.PresentationAnswerEntry.TABLE_NAME, PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PRESENTATION_ID + "=?", new String[]{id});
                db.delete(PresentationDataContract.PresentationEntry.TABLE_NAME, PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID + "=?", new String[]{id});
                remove[ri] = pi;
                ri++;
            }
            pi++;
        }
        db.close();

        for(int i : remove) {
            mPresentations.remove(i);
        }

        mPresentationListFragment.deselectAll();
        deselect();
    }

    public void setOnLocationSelectedListener (LocationSelectedListener l) {
        mLocationListener = l;
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == PresentationData.LOCATION_INTENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (mLocationListener != null) {
                    mLocationListener.onLocationSelected(place);
                }
                Log.i("SS", "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: handle this
                Log.i("SS", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // the user canceled the thing
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_presentation_main, menu);

        SharedPreferences sp = getSharedPreferences("presentation_list", 0);
        boolean twoCol = sp.getBoolean("presentation_list_view_type", false);
        if (twoCol) {
            mMenu.findItem(R.id.menu_action_view).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_view_agenda_white_24dp));
        } else {
            mMenu.findItem(R.id.menu_action_view).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_view_quilt_white_24dp));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.menu_action_search:
                Toast.makeText(PresentationMainActivity.this, "Search isn't implemented yet", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_action_view:
                toggleCardViewType();
                break;
            case R.id.menu_action_reset:
                resetPresentation();
                break;
            case R.id.menu_action_delete:
                deleteSelectedPresentations();
                break;
            case R.id.menu_action_edit_outline:
                Toast.makeText(PresentationMainActivity.this, "You can't edit outlines just yet", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_action_outline_view:
                Toast.makeText(PresentationMainActivity.this, "The timeline view isn't ready yet", Toast.LENGTH_SHORT).show();
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO: handle google api connection errors
    }

    public interface LocationSelectedListener {
        public void onLocationSelected(Place p);
    }
}
