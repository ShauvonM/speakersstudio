package com.thespeakers_studio.thespeakersstudioapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation_main);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

        mDbHelper = new PresentationDbHelper(getApplicationContext());

        mPresentations = loadPresentations();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle();

        if (savedInstanceState != null) {
            return;
        }

        mPresentationId = null;

        mPresentationListFragment = new PresentationListFragment();
        mPresentationListFragment.setPresentationData(mPresentations);

        mStepListFragment = new PresentationStepListFragment();
        mPromptListFragment = new PresentationPromptListFragment();

        getFragmentManager().addOnBackStackChangedListener(this);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, mPresentationListFragment, "presentation_list");
        ft.add(R.id.fragment_container, mStepListFragment, "step_list");
        ft.add(R.id.fragment_container, mPromptListFragment, "prompt_list");
        ft.commit();

        //showStepList();
        showPresentationList();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        Log.d("SS", "Back stack changed " + mPresentationListFragment.isVisible() + " " + mStepListFragment.isVisible());
        Utils.hideKeyboard(this, findViewById(R.id.fragment_container));
        if (mPresentationListFragment.isVisible()) {
            onPresentationListShown();
        } else if (mStepListFragment.isVisible()) {
            onStepListShown();
        } else if (mPromptListFragment.isVisible()) {
            onPromptListShown();
        }
    }

    public void onPresentationListShown() {
        mPresentationListFragment.refresh();
        mPresentationId = null;
        setTitle();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    public void onStepListShown() {
        // we are on the step list
        mStepListFragment.animateProgressHeight();
        mPromptListFragment.clearStep();
        setTitle();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onPromptListShown() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public PresentationData getSelectedPresentation() {
        for (PresentationData pres : mPresentations) {
            if (pres.getId() == mPresentationId) {
                return pres;
            }
        }
        return null;
    }

    public void setTitle() {
        PresentationData pres = getSelectedPresentation();
        if (pres != null) {
            getSupportActionBar().setTitle(pres.getTopic());
        } else {
            getSupportActionBar().setTitle(getResources().getString(R.string.saved_presentations));
        }
    }

    public GoogleApiClient getGoogleApi() {
        return mGoogleApiClient;
    }

    private void showPresentationList() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(mPromptListFragment);
        ft.hide(mStepListFragment);
        ft.show(mPresentationListFragment);
        ft.commit();

        onPresentationListShown();
    }

    @Override
    public void onOpenPresentation(String id) {
        Log.d("SS", "Presentation " + id + " selected");
        mPresentationId = id;
        mStepListFragment.setPresentation(getSelectedPresentation());
        mPromptListFragment.setPresentation(getSelectedPresentation());
        showStepList();
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
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        ft.hide(mPresentationListFragment);
        ft.show(mStepListFragment);

        ft.addToBackStack("presentation_" + mPresentationId);
        ft.commit();
    }

    public void onStepSelected(int step) {
        Log.d("SS", "Step " + step + " selected");
        showStep(step);
    }

    private void showStep(int step) {
        mPromptListFragment.setStep(step);

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        ft.hide(mStepListFragment);
        ft.show(mPromptListFragment);

        ft.addToBackStack("step_" + step);
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
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

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
        /*
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(PresentationDataContract.PresentationEntry.TABLE_NAME, PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID + "=?", new String[]{mPresentationId});
        db.delete(PresentationDataContract.PresentationAnswerEntry.TABLE_NAME, PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PRESENTATION_ID + "=?", new String[]{mPresentationId});

        mPresentation = createNewPresentation();

        mPromptListFragment.setPresentation(mPresentation);
        mStepListFragment.setPresentation(mPresentation);

        // this will reset the view
        onStepListShown();
        */
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
            case R.id.action_reset:
                resetPresentation();
                break;
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
