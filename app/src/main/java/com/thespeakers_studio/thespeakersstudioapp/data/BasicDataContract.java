package com.thespeakers_studio.thespeakersstudioapp.data;

/**
 * Created by smcgi_000 on 8/9/2016.
 */
public class BasicDataContract {
    public static final int DATABASE_VERSION = 9;
    public static final String DATABASE_NAME = "SpeakersStudio.db";

    public BasicDataContract() {}

    protected static final String TEXT_TYPE = " TEXT";
    protected static final String INT_TYPE = " INTEGER";
    protected static final String REAL_TYPE = " REAL";
    protected static final String COMMA_SEP = ", ";
    protected static final String COLUMN_NAME_DATE_CREATED = "created_date";
    protected static final String COLUMN_NAME_DATE_MODIFIED = "modified_date";
    protected static final String COLUMN_NAME_CREATED_BY = "created_by";
    protected static final String COLUMN_NAME_MODIFIED_BY = "modified_by";
}
