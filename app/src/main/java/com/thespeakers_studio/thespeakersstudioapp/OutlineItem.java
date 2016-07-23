package com.thespeakers_studio.thespeakersstudioapp;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 7/18/2016.
 */
public class OutlineItem {
    private ArrayList<OutlineItem> mSubItems;
    private String mText;
    private long mDuration;
    private int mOrder;

    public OutlineItem (String t, int o, ArrayList<OutlineItem> items) {
        mText = t;
        mOrder = o;
        mSubItems = items;
    }
    public OutlineItem (String t, int o) {
        this(t, o, new ArrayList<OutlineItem>());
    }
    public OutlineItem () {
        this("", 0);
    }

    public void setText (String text) {
        mText = text;
    }
    public void setOrder (int order) {
        mOrder = order;
    }

    public void setDuration (long dur) {
        mDuration = dur;
    }

    public String getText() {
        return mText;
    }
    public int getOrder() {
        return mOrder;
    }

    public ArrayList<OutlineItem> getSubItems() {
        return mSubItems;
    }
    public OutlineItem getSubItem(int index) {
        return mSubItems.get(index);
    }
    public int getSubItemCount() {
        return mSubItems.size();
    }

    public long getDuration () {
        return mDuration;
    }

    public void setSubItems(ArrayList<OutlineItem> items) {
        mSubItems = items;
    }

    public void addSubItem(OutlineItem item) {
        mSubItems.add(item);
        Utils.sortOutlineList(mSubItems);
    }
}
