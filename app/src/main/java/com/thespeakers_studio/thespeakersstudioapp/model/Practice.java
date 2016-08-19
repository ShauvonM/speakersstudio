package com.thespeakers_studio.thespeakersstudioapp.model;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 8/16/2016.
 */
public class Practice {

    private String id;
    private String message;
    private float rating;
    private String presentationId;
    private String modifiedDate;
    private ArrayList<OutlineItem> mOutlineItems;

    public Practice(String id, String presentationId, float rating, String message,
                    String modifiedDate, ArrayList<OutlineItem> items) {
        this.id = id;
        this.presentationId = presentationId;
        this.rating = rating;
        this.message = message;
        this.modifiedDate = modifiedDate;
        this.mOutlineItems = items;
    }

    public String getMessage() {
        return message;
    }

    public float getRating() {
        return rating;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public String getPresentationId() {
        return presentationId;
    }

    public String getId() {
        return id;
    }

    public ArrayList<OutlineItem> getOutlineItems() {
        return mOutlineItems;
    }
}
