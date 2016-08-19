package com.thespeakers_studio.thespeakersstudioapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.model.Practice;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by smcgi_000 on 8/9/2016.
 */
public class OutlineDbHelper {
    public static final int DATABASE_VERSION = OutlineDataContract.DATABASE_VERSION;
    public static final String DATABASE_NAME = OutlineDataContract.DATABASE_NAME;

    private Context mContext;
    private SpeakersStudioDbHelper mDbHelper;

    private String[] OUTLINE_ITEM_PROJECTION = new String[] {
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

    private String[] PRACTICE_ITEM_PROJECTION = new String[] {
            OutlineDataContract.PracticeEntry.COLUMN_NAME_PRESENTATION_ID,
            OutlineDataContract.PracticeEntry.COLUMN_NAME_PRACTICE_ID,
            OutlineDataContract.PracticeEntry.COLUMN_NAME_MESSAGE,
            OutlineDataContract.PracticeEntry.COLUMN_NAME_RATING,
            OutlineDataContract.COLUMN_NAME_DATE_MODIFIED,
            OutlineDataContract.COLUMN_NAME_MODIFIED_BY
    };

    public OutlineDbHelper(Context context) {
        mContext = context;
        mDbHelper = new SpeakersStudioDbHelper(context);
    }

    public ArrayList<Practice> getPractices(String presentationId) {
        ArrayList<Practice> items = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor practCursor = db.query(
                OutlineDataContract.PracticeEntry.TABLE_NAME,
                PRACTICE_ITEM_PROJECTION,
                OutlineDataContract.PracticeEntry.COLUMN_NAME_PRESENTATION_ID + "=?",
                new String[] { presentationId },
                null, null,
                OutlineDataContract.COLUMN_NAME_DATE_MODIFIED + " DESC"
        );

        practCursor.moveToFirst();
        try {
            while (!practCursor.isAfterLast()) {
                String id = practCursor.getString(
                                practCursor.getColumnIndexOrThrow(
                                        OutlineDataContract.PracticeEntry.COLUMN_NAME_PRACTICE_ID)
                        );
                float rating = practCursor.getFloat(
                                practCursor.getColumnIndexOrThrow(
                                        OutlineDataContract.PracticeEntry.COLUMN_NAME_RATING)
                        );
                String message = practCursor.getString(
                                practCursor.getColumnIndexOrThrow(
                                        OutlineDataContract.PracticeEntry.COLUMN_NAME_MESSAGE)
                        );
                String modifiedDate = practCursor.getString(
                                practCursor.getColumnIndexOrThrow(
                                        OutlineDataContract.COLUMN_NAME_DATE_MODIFIED)
                        );

                ArrayList<OutlineItem> outlineItems = getOutlineItemsByPracticeId(id);

                items.add(new Practice(id, presentationId, rating, message, modifiedDate, outlineItems));

                practCursor.moveToNext();
            }
        } finally {
            practCursor.close();
        }
        return items;
    }

    public void resetOutline(String presentationId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(OutlineDataContract.OutlineItemEntry.TABLE_NAME,
                OutlineDataContract.OutlineItemEntry.COLUMN_NAME_PRESENTATION_ID + "=?",
                new String[] {presentationId});
        db.close();
    }

    public ArrayList<OutlineItem> getOutlineItemsByParentId(String parentId) {
        ArrayList<OutlineItem> items = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor presCursor = db.query(
                OutlineDataContract.OutlineItemEntry.TABLE_NAME,
                OUTLINE_ITEM_PROJECTION,
                OutlineDataContract.OutlineItemEntry.COLUMN_NAME_PARENT_ID + "=?",
                new String[] { parentId },
                null, null,
                OutlineDataContract.OutlineItemEntry.COLUMN_NAME_ORDER
        );

        presCursor.close();

        return items;
    }

    public ArrayList<OutlineItem> getOutlineItemsByPracticeId(String practiceId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor itemCursor = db.query(
            OutlineDataContract.OutlineItemEntry.TABLE_NAME,
            OUTLINE_ITEM_PROJECTION,
            OutlineDataContract.OutlineItemEntry.COLUMN_NAME_PRACTICE_ID + "=? ",
            new String[] { practiceId },
            null, null,
            OutlineDataContract.OutlineItemEntry.COLUMN_NAME_ORDER
        );

        return cursorToOutlineItems(itemCursor);
    }

    public ArrayList<OutlineItem> getOutlineItemsByAnswerId(String answerId, String presentationId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor itemCursor = db.query(
                OutlineDataContract.OutlineItemEntry.TABLE_NAME,
                OUTLINE_ITEM_PROJECTION,
                OutlineDataContract.OutlineItemEntry.COLUMN_NAME_ANSWER_ID + "=? " +
                "AND " + OutlineDataContract.OutlineItemEntry.COLUMN_NAME_PRESENTATION_ID + "=?",
                new String[] { answerId, presentationId },
                null, null,
                OutlineDataContract.COLUMN_NAME_DATE_MODIFIED
        );

        return cursorToOutlineItems(itemCursor);
    }

    private ArrayList<OutlineItem> cursorToOutlineItems(Cursor itemCursor) {
        ArrayList<OutlineItem> items = new ArrayList<>();
        itemCursor.moveToFirst();
        try {
            while (!itemCursor.isAfterLast()) {
                String id = itemCursor.getString(
                        itemCursor.getColumnIndexOrThrow(
                                OutlineDataContract.OutlineItemEntry.COLUMN_NAME_OUTLINE_ITEM_ID));
                String parentId = itemCursor.getString(
                        itemCursor.getColumnIndexOrThrow(
                                OutlineDataContract.OutlineItemEntry.COLUMN_NAME_PARENT_ID));
                int order = itemCursor.getInt(
                        itemCursor.getColumnIndexOrThrow(
                                OutlineDataContract.OutlineItemEntry.COLUMN_NAME_ORDER));
                long duration = itemCursor.getLong(
                        itemCursor.getColumnIndexOrThrow(
                                OutlineDataContract.OutlineItemEntry.COLUMN_NAME_DURATION));
                String text = itemCursor.getString(
                        itemCursor.getColumnIndexOrThrow(
                                OutlineDataContract.OutlineItemEntry.COLUMN_NAME_TEXT));
                String modifiedDate = itemCursor.getString(
                        itemCursor.getColumnIndexOrThrow(
                                OutlineDataContract.COLUMN_NAME_DATE_MODIFIED));
                String answerId = itemCursor.getString(
                        itemCursor.getColumnIndexOrThrow(
                                OutlineDataContract.OutlineItemEntry.COLUMN_NAME_ANSWER_ID));
                String presentationId = itemCursor.getString(
                        itemCursor.getColumnIndexOrThrow(
                                OutlineDataContract.OutlineItemEntry.COLUMN_NAME_PRESENTATION_ID));


                items.add(new OutlineItem(
                        id,
                        parentId,
                        order,
                        text,
                        answerId,
                        true,
                        duration,
                        presentationId
                ));

                itemCursor.moveToNext();
            }
        } finally {
            itemCursor.close();
        }

        return items;
    }

    public void saveOutlineItem(OutlineItem item) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        String datetime = Utils.getDateTimeStamp();
        String id = item.getId() == null || item.getId().isEmpty() ?
                java.util.UUID.randomUUID().toString() :
                item.getId();

        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_OUTLINE_ITEM_ID, id);
        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_PARENT_ID, item.getParentId());
        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_PRESENTATION_ID, item.getPresentationId());
        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_ANSWER_ID, item.getAnswerId());
        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_ORDER, item.getOrder());
        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_DURATION, item.getDuration());
        values.put(OutlineDataContract.OutlineItemEntry.COLUMN_NAME_TEXT, item.getText());

        values.put(OutlineDataContract.COLUMN_NAME_DATE_CREATED, datetime);
        values.put(OutlineDataContract.COLUMN_NAME_DATE_MODIFIED, datetime);

        db.insert(OutlineDataContract.OutlineItemEntry.TABLE_NAME, null, values);

        db.close();
    }

    public void savePracticeResponse(String presentationId, float rating, String message, String UUID) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        String datetime = Utils.getDateTimeStamp();
        String id = UUID.isEmpty() ? Utils.getUUID() : UUID;

        values.put(OutlineDataContract.PracticeEntry.COLUMN_NAME_PRACTICE_ID, id);
        values.put(OutlineDataContract.PracticeEntry.COLUMN_NAME_PRESENTATION_ID, presentationId);
        values.put(OutlineDataContract.PracticeEntry.COLUMN_NAME_RATING, rating);
        values.put(OutlineDataContract.PracticeEntry.COLUMN_NAME_MESSAGE, message);

        values.put(OutlineDataContract.COLUMN_NAME_DATE_MODIFIED, datetime);
        values.put(OutlineDataContract.COLUMN_NAME_DATE_CREATED, datetime);

        db.insert(OutlineDataContract.PracticeEntry.TABLE_NAME, null, values);

        db.close();
    }
}
