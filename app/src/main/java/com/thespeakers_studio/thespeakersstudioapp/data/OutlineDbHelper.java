package com.thespeakers_studio.thespeakersstudioapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

/**
 * Created by smcgi_000 on 8/9/2016.
 */
public class OutlineDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = OutlineDataContract.DATABASE_VERSION;
    public static final String DATABASE_NAME = OutlineDataContract.DATABASE_NAME;

    private Context mContext;

    public OutlineDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(OutlineDataContract.OutlineItemEntry.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(OutlineDataContract.OutlineItemEntry.SQL_DELETE_ENTRIES);
        db.execSQL(OutlineDataContract.OutlineItemEntry.SQL_CREATE_ENTRIES);
    }

    public void saveOutlineItem(OutlineItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        String datetime = Utils.getDateTimeStamp();
    }
}
