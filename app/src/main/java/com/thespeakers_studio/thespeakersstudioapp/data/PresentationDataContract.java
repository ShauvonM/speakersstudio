package com.thespeakers_studio.thespeakersstudioapp.data;

import android.provider.BaseColumns;

import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;

/**
 * Created by smcgi_000 on 4/21/2016.
 */
public class PresentationDataContract extends BasicDataContract {

    public PresentationDataContract() {}

    public static abstract class PresentationEntry implements BaseColumns {
        public static final String TABLE_NAME = "presentation";
        public static final String COLUMN_NAME_PRESENTATION_ID = "presentation_id";
        public static final String COLUMN_NAME_DATE_CREATED = PresentationDataContract.COLUMN_NAME_DATE_CREATED;
        public static final String COLUMN_NAME_DATE_MODIFIED = PresentationDataContract.COLUMN_NAME_DATE_MODIFIED;
        public static final String COLUMN_NAME_CREATED_BY = PresentationDataContract.COLUMN_NAME_CREATED_BY;
        public static final String COLUMN_NAME_MODIFIED_BY = PresentationDataContract.COLUMN_NAME_MODIFIED_BY;
        public static final String COLUMN_NAME_COLOR = "color";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + PresentationEntry.TABLE_NAME + " (" +
                        PresentationEntry._ID + " INTEGER PRIMARY KEY, " +
                        PresentationEntry.COLUMN_NAME_PRESENTATION_ID + TEXT_TYPE + COMMA_SEP +
                        PresentationEntry.COLUMN_NAME_CREATED_BY + TEXT_TYPE + COMMA_SEP +
                        PresentationEntry.COLUMN_NAME_DATE_CREATED + TEXT_TYPE + COMMA_SEP +
                        PresentationEntry.COLUMN_NAME_MODIFIED_BY + TEXT_TYPE + COMMA_SEP +
                        PresentationEntry.COLUMN_NAME_DATE_MODIFIED + TEXT_TYPE + COMMA_SEP +
                        PresentationEntry.COLUMN_NAME_COLOR + INT_TYPE +
                        " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + PresentationEntry.TABLE_NAME;
    }

    public static abstract class PresentationAnswerEntry implements BaseColumns {
        public static final String TABLE_NAME = "presentation_answer";
        public static final String COLUMN_NAME_ANSWER_ID = "answer_id";
        public static final String COLUMN_NAME_PRESENTATION_ID = PresentationEntry.COLUMN_NAME_PRESENTATION_ID;
        public static final String COLUMN_NAME_PROMPT_ID = "prompt_id";
        public static final String COLUMN_NAME_ANSWER_KEY = "answer_key";
        public static final String COLUMN_NAME_ANSWER_VALUE = "answer_value";
        public static final String COLUMN_NAME_ANSWER_LINK_ID = "answer_link_id";
        public static final String COLUMN_NAME_DATE_CREATED = PresentationDataContract.COLUMN_NAME_DATE_CREATED;
        public static final String COLUMN_NAME_DATE_MODIFIED = PresentationDataContract.COLUMN_NAME_DATE_MODIFIED;
        public static final String COLUMN_NAME_CREATED_BY = PresentationDataContract.COLUMN_NAME_CREATED_BY;
        public static final String COLUMN_NAME_MODIFIED_BY = PresentationDataContract.COLUMN_NAME_MODIFIED_BY;

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + PresentationAnswerEntry.TABLE_NAME + " (" +
                        PresentationAnswerEntry._ID + " INTEGER PRIMARY KEY, " +
                        PresentationAnswerEntry.COLUMN_NAME_ANSWER_ID + TEXT_TYPE + COMMA_SEP +
                        PresentationAnswerEntry.COLUMN_NAME_ANSWER_KEY + TEXT_TYPE + COMMA_SEP +
                        PresentationAnswerEntry.COLUMN_NAME_ANSWER_VALUE + TEXT_TYPE + COMMA_SEP +
                        PresentationAnswerEntry.COLUMN_NAME_ANSWER_LINK_ID + TEXT_TYPE + COMMA_SEP +
                        PresentationAnswerEntry.COLUMN_NAME_PRESENTATION_ID + TEXT_TYPE + COMMA_SEP +
                        PresentationAnswerEntry.COLUMN_NAME_PROMPT_ID + INT_TYPE + COMMA_SEP +
                        PresentationAnswerEntry.COLUMN_NAME_CREATED_BY + TEXT_TYPE + COMMA_SEP +
                        PresentationAnswerEntry.COLUMN_NAME_DATE_CREATED + TEXT_TYPE + COMMA_SEP +
                        PresentationAnswerEntry.COLUMN_NAME_MODIFIED_BY + TEXT_TYPE + COMMA_SEP +
                        PresentationAnswerEntry.COLUMN_NAME_DATE_MODIFIED + TEXT_TYPE +
                        " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + PresentationAnswerEntry.TABLE_NAME;
    }
}
