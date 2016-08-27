package com.thespeakers_studio.thespeakersstudioapp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by smcgi_000 on 6/16/2016.
 */
public class PromptAnswer implements Parcelable {

    private String id;
    private String key;
    private String value;
    private int promptId;
    private String answerLinkId;
    private int promptType;
    private String createdBy;
    private String modifiedBy;
    private String createdDate;
    private String modifiedDate;

    public PromptAnswer (String id, String key, String value, int promptId, String createdBy, String modifiedBy, String createdDate, String modifiedDate, String answerLinkId) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.promptId = promptId;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
        this.createdDate = createdDate;
        this.modifiedBy = modifiedBy;
        this.modifiedDate = modifiedDate;
        this.answerLinkId = answerLinkId;
    }

    // create an answer object from a key value pair, linked to a specific prompt
    public PromptAnswer (String key, String value, int promptId) {
        this("", key, value, promptId, "", "", "", "", "");
    }

    // create an answer object from an existing id
    public PromptAnswer(String id, String key, String value, int promptId) {
        this(id, key, value, promptId, "", "", "", "", "");
    }

    // create an empty answer
    public PromptAnswer () {
        this("", "", "", -1, "", "", "", "", "");
    }

    public boolean existsInDB() {
        return !getId().isEmpty();
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatedDate (String createdDate) {
        this.createdDate = createdDate;
    }
    public void setModifiedDate (String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }

    public void setValue(String val) {
        value = val;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAnswerLinkId() {
        return answerLinkId;
    }
    public void setAnswerLinkId(String answerLinkId) {
        this.answerLinkId = answerLinkId;
    }

    public void setPromptId (int id) {
        this.promptId = id;
    }
    public int getPromptId() {
        return this.promptId;
    }

    public void setPromptType (int type) {
        this.promptType = type;
    }
    public int getPromptType(){
        return this.promptType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(key);
        dest.writeString(value);
        dest.writeInt(promptId);
        dest.writeString(answerLinkId);
        dest.writeInt(promptType);
        dest.writeString(createdBy);
        dest.writeString(modifiedBy);
        dest.writeString(createdDate);
        dest.writeString(modifiedDate);
    }

    public PromptAnswer(Parcel parcel) {
        id = parcel.readString();
        key = parcel.readString();
        value = parcel.readString();
        promptId = parcel.readInt();
        answerLinkId = parcel.readString();
        promptType = parcel.readInt();
        createdBy = parcel.readString();
        modifiedBy = parcel.readString();
        createdDate = parcel.readString();
        modifiedDate = parcel.readString();
    }

    public static final Parcelable.Creator<PromptAnswer> CREATOR = new Creator<PromptAnswer>() {
        @Override
        public PromptAnswer createFromParcel(Parcel source) {
            return new PromptAnswer(source);
        }

        @Override
        public PromptAnswer[] newArray(int size) {
            return new PromptAnswer[size];
        }
    };
}
