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
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class PresentationMainActivity extends AppCompatActivity implements
        PresentationStepListFragment.OnStepSelectedListener,
        PresentationPromptListFragment.PromptSaveListener,
        GoogleApiClient.OnConnectionFailedListener {

    private PresentationDbHelper mDbHelper;
    private String mPresentationId;
    private PresentationData mPresentation;

    private Toolbar mToolbar;

    private GoogleApiClient mGoogleApiClient;
    private LocationSelectedListener mLocationListener;

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

        mPresentation = loadPresentation();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        setTitle();

        if (savedInstanceState != null) {
            return;
        }

        showStepList();
    }

    public void setTitle() {
        String topic = mPresentation.getTopic();
        if (topic != null) {
            getSupportActionBar().setTitle(topic);
        }
    }

    public GoogleApiClient getGoogleApi() {
        return mGoogleApiClient;
    }

    private void showStepList() {
        PresentationStepListFragment StepListFragment = new PresentationStepListFragment();
        StepListFragment.setArguments(getIntent().getExtras());

        getFragmentManager().beginTransaction().add(R.id.fragment_container, StepListFragment).commit();
    }

    public void onStepSelected(int step) {
        Log.d("SS", "Step " + step + " selected");
        showStep(step);
    }

    public PresentationData getPresentation() {
        return mPresentation;
    }

    private void showStep(int step) {
        PresentationPromptListFragment stepFragment = new PresentationPromptListFragment();
        Bundle args = new Bundle();
        args.putInt("step", step);
        stepFragment.setArguments(args);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, stepFragment);
        transaction.addToBackStack("step" + step);

        transaction.commit();

        stepFragment.setOnPromptSaveListener(this);
    }

    @Override
    public void onPromptSave(Prompt prompt) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String id;
        ContentValues values = new ContentValues();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = dateFormat.format(Calendar.getInstance().getTime());

        ArrayList<PromptAnswer> answers = prompt.getAnswer();

        for (PromptAnswer answer : answers) {
            //
            values.put(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_KEY, answer.getKey());
            values.put(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_VALUE, answer.getValue());
            values.put(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_DATE_MODIFIED, datetime);

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
        db.close();

        if (prompt.getId() == PresentationData.PRESENTATION_TOPIC) {
            setTitle();
        }
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

    private PresentationData loadPresentation() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // pick what columns to load
        String[] presProjection = {
                PresentationDataContract.PresentationEntry._ID,
                PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID
        };
        String[] ansProjection = {
                PresentationDataContract.PresentationAnswerEntry._ID,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PROMPT_ID,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PRESENTATION_ID,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_KEY,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_VALUE,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_ID,
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

        // TODO: fetch presentation by id
        presCursor.moveToFirst();
        if (presCursor.isAfterLast()) {
            return createNewPresentation();
        } else {
            String presentationId = presCursor.getString(
                    presCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID)
            );
            PresentationData pres = new PresentationData(getApplicationContext(), presentationId);
            mPresentationId = presentationId;

            // set up the answer clause
            String[] answerSelectionValues = new String[] { String.valueOf(mPresentationId) };

            // fetch the answers
            Cursor answerCursor = db.query(PresentationDataContract.PresentationAnswerEntry.TABLE_NAME, ansProjection, answerSelection, answerSelectionValues, null, null, PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_KEY, null);

            pres.setAnswers(answerCursor);

            presCursor.close();
            return pres;
        }
    }

    private PresentationData createNewPresentation() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String id = UUID.randomUUID().toString();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = dateFormat.format(Calendar.getInstance().getTime());

        int color = R.color.colorPrimary;

        ContentValues values = new ContentValues();
        values.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID, id);
        values.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_CREATED, datetime);
        values.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_MODIFIED, datetime);
        values.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_COLOR, color); // TODO: custom colors

        db.insert(PresentationDataContract.PresentationEntry.TABLE_NAME, null, values);
        mPresentationId = id;
        return new PresentationData(getApplicationContext(), id);
    }

    private void resetPresentation() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(PresentationDataContract.PresentationEntry.TABLE_NAME, PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID + "=?", new String[]{mPresentationId});
        db.delete(PresentationDataContract.PresentationAnswerEntry.TABLE_NAME, PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PRESENTATION_ID + "=?", new String[]{mPresentationId});

        mPresentation = createNewPresentation();
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
