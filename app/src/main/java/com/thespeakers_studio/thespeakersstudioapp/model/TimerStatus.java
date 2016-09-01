package com.thespeakers_studio.thespeakersstudioapp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by smcgi_000 on 9/1/2016.
 */
public class TimerStatus implements Parcelable {
    public int currentRemaining;
    public int totalRemaining;
    public int elapsedTime;

    public TimerStatus(int c, int t, int e) {
        currentRemaining = c;
        totalRemaining = t;
        elapsedTime = e;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(currentRemaining);
        dest.writeInt(totalRemaining);
        dest.writeInt(elapsedTime);
    }

    public TimerStatus(Parcel parcel) {
        currentRemaining = parcel.readInt();
        totalRemaining = parcel.readInt();
        elapsedTime = parcel.readInt();
    }

    public static final Parcelable.Creator<TimerStatus> CREATOR = new Creator<TimerStatus>() {
        @Override
        public TimerStatus createFromParcel(Parcel source) {
            return new TimerStatus(source);
        }

        @Override
        public TimerStatus[] newArray(int size) {
            return new TimerStatus[size];
        }
    };
}
