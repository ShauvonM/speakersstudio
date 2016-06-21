package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

/**
 * Created by smcgi_000 on 4/21/2016.
 */
public class PresentationDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = PresentationDataContract.DATABASE_VERSION;
    public static final String DATABASE_NAME = PresentationDataContract.DATABASE_NAME;

    public PresentationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PresentationDataContract.PresentationEntry.SQL_CREATE_ENTRIES);
        db.execSQL(PresentationDataContract.PresentationAnswerEntry.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(PresentationDataContract.PresentationEntry.SQL_DELETE_ENTRIES);
        db.execSQL(PresentationDataContract.PresentationEntry.SQL_CREATE_ENTRIES);

        db.execSQL(PresentationDataContract.PresentationAnswerEntry.SQL_DELETE_ENTRIES);
        db.execSQL(PresentationDataContract.PresentationAnswerEntry.SQL_CREATE_ENTRIES);
    }
}
