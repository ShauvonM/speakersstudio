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
        db.execSQL(OutlineDataContract.OutlineItemEntry.SQL_DELETE_ENTRIES);
        db.execSQL(OutlineDataContract.OutlineItemEntry.SQL_CREATE_ENTRIES);

        db.execSQL(OutlineDataContract.PracticeEntry.SQL_DELETE_ENTRIES);
        db.execSQL(OutlineDataContract.PracticeEntry.SQL_CREATE_ENTRIES);
    }
}
