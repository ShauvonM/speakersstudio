package com.thespeakers_studio.thespeakersstudioapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 7/18/2016.
 */
public class OutlineItem implements Parcelable {

    private String mId;
    private String mParentId;
    private int mOrder;
    private String mText;
    private String mAnswerId;
    private boolean mFromDB;
    private int mDuration;
    private String mPresentationId;
    private int mTimedDuration;
    private String mPracticeId;

    private String mTopicText = "";

    public static final String INTRO = "intro";
    public static final String CONCLUSION = "conclusion";
    public static final String NO_PARENT = "no_parent";

    public OutlineItem (String id, String parent, int order, String text, String answer,
                        boolean fromDB, int duration, String presentation) {
        mId = id;
        mParentId = parent;
        mOrder = order;
        mText = text;
        mAnswerId = answer;
        mFromDB = fromDB;
        mDuration = duration;
        mPresentationId = presentation;
        mTimedDuration = 0;
    }

    // for the intro / conclusion items
    public OutlineItem (String id, int order, String text, String presentation) {
        this(id, NO_PARENT, order, text, "", false, 0, presentation);
    }

    // for manually setting everything up
    public OutlineItem () {
    }

    public void setIsFromDB() {
        mFromDB = true;
    }
    public boolean getIsFromDB() {
        return mFromDB;
    }

    public String getId() {
        return mId;
    }
    public void setId(String id) {
        mId = id;
    }

    public String getAnswerId() {
        return mAnswerId;
    }
    public void setAnswerId(String answer) {
        mAnswerId = answer;
    }

    public int getDuration() {
        return mDuration;
    }
    public void setDuration(int duration) {
        mDuration = duration;
    }

    public String getParentId () {
        return mParentId;
    }
    public void setParentId(String parent) {
        mParentId = parent;
    }

    public int getOrder () {
        return mOrder;
    }
    public void setOrder(int order) {
        mOrder = order;
    }

    public String getPresentationId() {
        return mPresentationId;
    }
    public void setPresentationId(String presentation) {
        mPresentationId = presentation;
    }

    public String getText() {
        return mText;
    }
    public void setText(String text) {
        mText = text;
    }

    public int getTimedDuration() {
        return mTimedDuration;
    }
    public void setTimedDuration(int duration) {
        mTimedDuration = duration;
    }

    public static OutlineItem createDurationItem (OutlineItem item) {
        OutlineItem durationItem = new OutlineItem();
        durationItem.setPresentationId(item.getPresentationId());
        durationItem.setAnswerId(item.getAnswerId());
        durationItem.setParentId(item.getParentId());
        durationItem.setDuration(item.getTimedDuration());
        durationItem.setIsFromDB();

        return durationItem;
    }

    public void setPracticeId(String practiceId) {
        mPracticeId = practiceId;
    }
    public String getPracticeId() {
        return mPracticeId;
    }

    public String getTopicText() {
        return mTopicText;
    }
    public void setTopicText(String text) {
        mTopicText = text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mParentId);
        dest.writeInt(mOrder);
        dest.writeString(mText);
        dest.writeString(mAnswerId);
        dest.writeByte((byte) (mFromDB ? 1 : 0));
        dest.writeInt(mDuration);
        dest.writeString(mPresentationId);
        dest.writeInt(mTimedDuration);
        dest.writeString(mPracticeId);
        dest.writeString(mTopicText);
    }

    public OutlineItem(Parcel parcel) {
        mId = parcel.readString();
        mParentId = parcel.readString();
        mOrder = parcel.readInt();
        mText = parcel.readString();
        mAnswerId = parcel.readString();
        mFromDB = parcel.readByte() == 1;
        mDuration = parcel.readInt();
        mPresentationId = parcel.readString();
        mTimedDuration = parcel.readInt();
        mPracticeId = parcel.readString();
        mTopicText = parcel.readString();
    }

    public static final Parcelable.Creator<OutlineItem> CREATOR = new Creator<OutlineItem>() {
        @Override
        public OutlineItem createFromParcel(Parcel source) {
            return new OutlineItem(source);
        }

        @Override
        public OutlineItem[] newArray(int size) {
            return new OutlineItem[size];
        }
    };
}
