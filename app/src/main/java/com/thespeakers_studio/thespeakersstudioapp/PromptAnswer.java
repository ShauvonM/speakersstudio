package com.thespeakers_studio.thespeakersstudioapp;

/**
 * Created by smcgi_000 on 6/16/2016.
 */
public class PromptAnswer {

    private String id;
    private String key;
    private String value;
    private int promptId;
    private String createdBy;
    private String modifiedBy;
    private String createdDate;
    private String modifiedDate;

    public PromptAnswer (String id, String key, String value, int promptId, String createdBy, String modifiedBy, String createdDate, String modifiedDate) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.promptId = promptId;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
        this.createdDate = createdDate;
        this.modifiedBy = modifiedBy;
    }

    public PromptAnswer (String key, String value, int promptId) {
        this("", key, value, promptId, "", "", "", "");
    }

    public PromptAnswer () {
        this("", "", "", -1, "", "", "", "");
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
}
