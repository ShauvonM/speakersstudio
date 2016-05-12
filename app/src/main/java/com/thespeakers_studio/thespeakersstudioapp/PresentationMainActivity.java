package com.thespeakers_studio.thespeakersstudioapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.xml.sax.XMLReader;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class PresentationMainActivity extends AppCompatActivity implements PresentationStepListFragment.OnStepSelectedListener {
    private PresentationDbHelper mDbHelper;
    private String mPresentationId;
    private PresentationData mPresentation;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation_main);

        mDbHelper = new PresentationDbHelper(getApplicationContext());

        mPresentation = loadPresentation();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        String topic = mPresentation.getTopic();
        if (topic != null) {
            getSupportActionBar().setTitle(topic);
        }

        if (savedInstanceState != null) {
            return;
        }

        showStepList();
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
        PresentationStepFragment stepFragment = new PresentationStepFragment();
        Bundle args = new Bundle();
        args.putInt("step", step);
        stepFragment.setArguments(args);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, stepFragment);
        transaction.addToBackStack("step" + step);

        transaction.commit();
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
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_TEXT,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_ID
        };

        // set up the clause to use on the answer db
        // TODO: I do that

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

            // TODO: load answers

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
        db.delete(PresentationDataContract.PresentationEntry.TABLE_NAME, PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID + "=?", new String[] { mPresentationId });

        mPresentation = createNewPresentation();
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
}
