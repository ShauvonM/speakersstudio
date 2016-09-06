package com.thespeakers_studio.thespeakersstudioapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by smcgi_000 on 8/16/2016.
 */
public class SpeakersStudioDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = BasicDataContract.DATABASE_VERSION;
    public static final String DATABASE_NAME = BasicDataContract.DATABASE_NAME;

    private static SpeakersStudioDbHelper mInstance = null;

    public static SpeakersStudioDbHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SpeakersStudioDbHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    public SpeakersStudioDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PresentationDataContract.PresentationEntry.SQL_CREATE_ENTRIES);
        db.execSQL(PresentationDataContract.PresentationAnswerEntry.SQL_CREATE_ENTRIES);
        db.execSQL(OutlineDataContract.OutlineItemEntry.SQL_CREATE_ENTRIES);
        db.execSQL(OutlineDataContract.PracticeEntry.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL(PresentationDataContract.PresentationEntry.SQL_DELETE_ENTRIES);
            db.execSQL(PresentationDataContract.PresentationEntry.SQL_CREATE_ENTRIES);

            db.execSQL(PresentationDataContract.PresentationAnswerEntry.SQL_DELETE_ENTRIES);
            db.execSQL(PresentationDataContract.PresentationAnswerEntry.SQL_CREATE_ENTRIES);
        }
        if (oldVersion < 8) { // version 8 is the main outline item update
            db.execSQL(OutlineDataContract.OutlineItemEntry.SQL_DELETE_ENTRIES);
            db.execSQL(OutlineDataContract.OutlineItemEntry.SQL_CREATE_ENTRIES);

            db.execSQL(OutlineDataContract.PracticeEntry.SQL_DELETE_ENTRIES);
            db.execSQL(OutlineDataContract.PracticeEntry.SQL_CREATE_ENTRIES);
        }
        if (oldVersion == 8) { // version 9 introduced practice_id into outline items
            db.execSQL(OutlineDataContract.OutlineItemEntry.SQL_ADD_PRACTICE_ID_COLUMN);
        }
    }
}
