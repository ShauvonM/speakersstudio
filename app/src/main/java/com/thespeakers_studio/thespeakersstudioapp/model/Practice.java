package com.thespeakers_studio.thespeakersstudioapp.model;

/**
 * Created by smcgi_000 on 8/16/2016.
 */
public class Practice {

    private String id;
    private String message;
    private float rating;
    private String presentationId;
    private String modifiedDate;

    public Practice(String id, String presentationId, float rating, String message,
                    String modifiedDate) {
        this.id = id;
        this.presentationId = presentationId;
        this.rating = rating;
        this.message = message;
        this.modifiedDate = modifiedDate;
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

}
