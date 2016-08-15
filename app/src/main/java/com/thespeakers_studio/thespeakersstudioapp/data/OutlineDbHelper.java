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

    private String[] mOutlineItemProjection = new String[] {
            OutlineDataContract.OutlineItemEntry.COLUMN_NAME_OUTLINE_ITEM_ID,
            OutlineDataContract.OutlineItemEntry.COLUMN_NAME_PARENT_ID,
            OutlineDataContract.OutlineItemEntry.COLUMN_NAME_PRESENTATION_ID,
            OutlineDataContract.OutlineItemEntry.COLUMN_NAME_ANSWER_ID,
            OutlineDataContract.OutlineItemEntry.COLUMN_NAME_ORDER,
            OutlineDataContract.OutlineItemEntry.COLUMN_NAME_DURATION,
            OutlineDataContract.OutlineItemEntry.COLUMN_NAME_TEXT,
            OutlineDataContract.COLUMN_NAME_DATE_MODIFIED,
            OutlineDataContract.COLUMN_NAME_MODIFIED_BY
    };

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
        String id = java.util.UUID.randomUUID().toString();

        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_OUTLINE_ITEM_ID, id);
        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_PARENT_ID, item.getParentId());
        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_PRESENTATION_ID, item.getPresentationId());
        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_ANSWER_ID, item.getAnswerId());
        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_ORDER, item.getOrder());
        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_DURATION, item.getDuration());
        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_TEXT, item.getText());
        values.put(OutlineDataContract.COLUMN_NAME_DATE_MODIFIED, datetime);

        db.insert(OutlineDataContract.OutlineItemEntry.TABLE_NAME, null, values);

        db.close();
    }
}
