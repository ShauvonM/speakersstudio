package com.thespeakers_studio.thespeakersstudioapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.model.Prompt;
import com.thespeakers_studio.thespeakersstudioapp.model.PromptAnswer;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by smcgi_000 on 4/21/2016.
 */
public class PresentationDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = PresentationDataContract.DATABASE_VERSION;
    public static final String DATABASE_NAME = PresentationDataContract.DATABASE_NAME;

    private Context mContext;

    private String[] PRESENTATION_PROJECTION = {
                PresentationDataContract.PresentationEntry._ID,
                PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID,
                PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_MODIFIED
        };
    private String[] ANSWER_PROJECTION = {
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

    // set up the clauses to use on the db
    private final String ANSWER_SELECTION_CLAUSE =
            PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PRESENTATION_ID + "=?";
    private final String PRESENTATION_SELECTION_CLAUSE =
            PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID + "=?";

    // set up sort clause
    private final String PRESENTATION_SORT_ORDER = PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_MODIFIED + " DESC";

    public PresentationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PresentationDataContract.PresentationEntry.SQL_CREATE_ENTRIES);
        db.execSQL(PresentationDataContract.PresentationAnswerEntry.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL(PresentationDataContract.PresentationEntry.SQL_DELETE_ENTRIES);
            db.execSQL(PresentationDataContract.PresentationEntry.SQL_CREATE_ENTRIES);

            db.execSQL(PresentationDataContract.PresentationAnswerEntry.SQL_DELETE_ENTRIES);
            db.execSQL(PresentationDataContract.PresentationAnswerEntry.SQL_CREATE_ENTRIES);
        }
    }

    public String[] getIdSelectionClause (String presentationId) {
        return new String[] { String.valueOf(presentationId) };
    }

    public ArrayList<PresentationData> loadPresentations() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<PresentationData> presies = new ArrayList<>();

        // fetch the presentation
        Cursor presCursor = db.query(PresentationDataContract.PresentationEntry.TABLE_NAME,
                PRESENTATION_PROJECTION, null, null, null, null, PRESENTATION_SORT_ORDER);

        presCursor.moveToFirst();
        try {
            while (!presCursor.isAfterLast()) {
                String presentationId = presCursor.getString(
                        presCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID)
                );
                String modifiedDate = getModifiedDateFromCursor(presCursor);

                PresentationData pres = new PresentationData(mContext, presentationId, modifiedDate);

                // fetch the answers
                Cursor answerCursor = getAnswerCursor(db, presentationId);

                pres.setAnswers(answerCursor);

                presies.add(pres);

                presCursor.moveToNext();
            }
        } finally {
            presCursor.close();
        }
        return presies;
    }

    private String getModifiedDateFromCursor(Cursor presCursor) {
        String modifiedDate = presCursor.getString(
                presCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_MODIFIED)
        );
        return modifiedDate;
    }

    private Cursor getAnswerCursor(SQLiteDatabase db, String presentationId) {
        return db.query(
                PresentationDataContract.PresentationAnswerEntry.TABLE_NAME,
                ANSWER_PROJECTION,
                ANSWER_SELECTION_CLAUSE, getIdSelectionClause(presentationId),
                null, null,
                PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_KEY,
                null);
    }

    public PresentationData loadPresentationById(String presentationId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor presCursor = db.query(
                PresentationDataContract.PresentationEntry.TABLE_NAME,
                PRESENTATION_PROJECTION,
                PRESENTATION_SELECTION_CLAUSE, getIdSelectionClause(presentationId),
                null, null, null
        );
        presCursor.moveToFirst();
        if (presCursor.getCount() > 0) {
            String modifiedDate = getModifiedDateFromCursor(presCursor);
            PresentationData pres = new PresentationData(mContext, presentationId, modifiedDate);
            Cursor answerCursor = getAnswerCursor(db, presentationId);
            pres.setAnswers(answerCursor);

            presCursor.close();
            return pres;
        } else {
            return null;
        }
    }

    public PresentationData createNewPresentation() {
        SQLiteDatabase db = getWritableDatabase();

        String id = UUID.randomUUID().toString();

        String datetime = Utils.getDateTimeStamp();

        int color = R.color.colorPrimary;

        ContentValues values = new ContentValues();
        values.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_PRESENTATION_ID, id);
        values.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_CREATED, datetime);
        values.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_DATE_MODIFIED, datetime);
        values.put(PresentationDataContract.PresentationEntry.COLUMN_NAME_COLOR, color); // TODO: custom colors

        db.insert(PresentationDataContract.PresentationEntry.TABLE_NAME, null, values);
        return new PresentationData(mContext, id, datetime);
    }

    public void resetPresentation(PresentationData presentation) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(PresentationDataContract.PresentationAnswerEntry.TABLE_NAME,
                ANSWER_SELECTION_CLAUSE,
                getIdSelectionClause(presentation.getId()));
        db.close();

        presentation.resetAnswers();
    }

    public void savePrompt(String presentationId, Prompt prompt) {
        SQLiteDatabase db = getWritableDatabase();
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
                values.put(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PRESENTATION_ID, presentationId);
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
                PRESENTATION_SELECTION_CLAUSE,
                getIdSelectionClause(presentationId));
        //getSelectedPresentation().setModifiedDate(datetime);

        db.close();
    }

    public void deletePresentation(ArrayList<PresentationData> presentations) {
        SQLiteDatabase db = getWritableDatabase();
        for (PresentationData pres : presentations) {
            String thisid = pres.getId();

            // delete the answers for this presentation
            db.delete(PresentationDataContract.PresentationAnswerEntry.TABLE_NAME,
                    ANSWER_SELECTION_CLAUSE, getIdSelectionClause(thisid));

            // delete the presentation itself
            db.delete(PresentationDataContract.PresentationEntry.TABLE_NAME,
                    PRESENTATION_SELECTION_CLAUSE, getIdSelectionClause(thisid));
        }
        db.close();
    }
    public void deletePresentation(PresentationData presentation) {
        ArrayList<PresentationData> a = new ArrayList<>();
        a.add(presentation);
        deletePresentation(a);
    }
}
