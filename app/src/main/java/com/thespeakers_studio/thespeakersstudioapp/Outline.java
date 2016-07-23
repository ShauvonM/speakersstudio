package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 7/19/2016.
 */
public class Outline {
    private ArrayList<OutlineItem> mItems;
    private Context mContext;
    private PresentationData mPresentation;

    // these point to the IDs of prompts we want to include by default in these items in the outline
    public static final int[] OUTLINE_INTRO_ITEMS = new int[] {18, 12, 14};
    public static final int[] OUTLINE_TOPIC_ITEMS = new int[] {22, 21, 20, 23};
    public static final int[] OUTLINE_CONC_ITEMS = new int[] {13, 19};

    public Outline(Context context) {
        mItems = new ArrayList<>();
        mContext = context;
    }

    public ArrayList<OutlineItem> getItems() {
        Utils.sortOutlineList(mItems);
        return mItems;
    }

    public OutlineItem getItem(int index) {
        if (index >= mItems.size()) {
            return null;
        }
        Utils.sortOutlineList(mItems);
        return mItems.get(index);
    }
    public int getItemCount() {
        return mItems.size();
    }

    public void addItem(OutlineItem item) {
        mItems.add(item);
    }

    public void setPresentation(PresentationData pres) {
        mPresentation = pres;
    }

    public String getTitle() {
        return mPresentation.getTopic();
    }

    public String getDuration() {
        return Utils.getDurationString(mPresentation.getDuration(), mContext.getResources());
    }

    public long getDurationMillis() {
        int mins = mPresentation.getDuration();
        return (mins * 60) * 1000;
    }

    public String getDate() {
        return Utils.getDateTimeString(mPresentation.getDate(), mContext.getResources())
                .replace("\n", " ");
    }

    // this is the biggun - this method generates the outline list
    public static Outline fromPresentation (Context context, PresentationData pres) {
        Outline outline = new Outline(context);
        outline.setPresentation(pres);
        Resources r = context.getResources();

        int durationMinutes = Integer.parseInt(pres.getAnswerByKey(PresentationData.PRESENTATION_DURATION, "duration"));
        long durationMillis = (durationMinutes * 60) * 1000;

        ArrayList<PromptAnswer> topics = pres.getAnswer(PresentationData.PRESENTATION_TOPICS);

        /*
            default durations (these are probably placeholders)
            INTRO: 12.5%
            TOPICS: 75% (divided among topics)
            CONCLUSION: 12.5%
        */
        long topicDuration = (long) Math.floor((durationMillis * 0.75) / topics.size());
        long introDuration = (long) Math.floor((durationMillis - (topicDuration * topics.size())) / 2);

        // add the intro section
        OutlineItem introItem = new OutlineItem(r.getString(R.string.outline_item_intro), 0);
        introItem.setDuration(introDuration);

        // add all of the items we want to throw into the intro
        introItem = outline.loadSubItemsFromPrompts(OUTLINE_INTRO_ITEMS, introItem, introDuration);
        outline.addItem(introItem);

        // now the three topics
        int order = 1;

        ArrayList<Prompt> topicSubItems = new ArrayList<>();
        for (int i = 0; i < Outline.OUTLINE_TOPIC_ITEMS.length; i++) {
            topicSubItems.add(pres.getPromptById(Outline.OUTLINE_TOPIC_ITEMS[i]));
        }

        for (PromptAnswer t : topics) {
            OutlineItem topicItem = new OutlineItem(
                    t.getValue(),
                    order);

            int subOrder = 0;
            topicItem.setDuration(topicDuration);
            long durationTally = 0l;

            ArrayList<PromptAnswer> subItemList = new ArrayList<>();
            for (Prompt p : topicSubItems) {
                subItemList.addAll(p.getAnswersByLinkId(t.getId()));
            }

            long subItemDuration = Math.round((topicDuration / subItemList.size()) / 1000) * 1000;
            long subItemLeftover = topicDuration - (subItemDuration * subItemList.size());

            for (PromptAnswer a : subItemList) {
                OutlineItem subitem = new OutlineItem();
                subitem.setText(a.getValue());
                subitem.setOrder(subOrder);

                long thisDuration = subItemDuration;
                if (subItemLeftover >= 1000) {
                    thisDuration += 1000;
                    subItemLeftover -= 1000;
                }
                subitem.setDuration(thisDuration);

                topicItem.addSubItem(subitem);
                subOrder++;
            }

            outline.addItem(topicItem);
            order++;
        }

        // and the conclusion
        OutlineItem conclusionItem = new OutlineItem(r.getString(R.string.outline_item_conclusion), order);
        conclusionItem.setDuration(introDuration);

        // add all of the items we want to throw into the conclusion
        conclusionItem = outline.loadSubItemsFromPrompts(Outline.OUTLINE_CONC_ITEMS, conclusionItem, introDuration);
        outline.addItem(conclusionItem);

        return outline;
    }

    private OutlineItem loadSubItemsFromPrompts(int[] promptIDs, OutlineItem parent, long duration) {
        Resources r = mContext.getResources();

        long subItemDuration = Math.round((duration / promptIDs.length) / 1000) * 1000;
        long subItemLeftover = duration - (subItemDuration * promptIDs.length);

        for (int i = 0; i < promptIDs.length; i++) {
            int type = mPresentation.getPromptById(promptIDs[i]).getType();
            OutlineItem thisItem = new OutlineItem();
            thisItem.setOrder(i);

            /*
            if (i < promptIDs.length - 1) {
                long thisduration = Math.round((duration / promptIDs.length) / 1000) * 1000;
                thisItem.setDuration(thisduration);
                durationTally += thisduration;
            } else {
                thisItem.setDuration(duration - durationTally);
            }
            */
            long thisDuration = subItemDuration;
            if (subItemLeftover >= 1000) {
                thisDuration += 1000;
                subItemLeftover -= 1000;
            }
            thisItem.setDuration(thisDuration);

            if (type == PresentationData.TEXT || type == PresentationData.PARAGRAPH) {
                thisItem.setText(
                        mPresentation.getAnswerByKey(promptIDs[i], "text"));

                parent.addSubItem(thisItem);
            } else if (type == PresentationData.LIST) {
                thisItem.setText(String.format(r.getString(R.string.outline_item_mention),
                                Utils.processAnswerList(
                                        mPresentation.getAnswer(promptIDs[i]), r)));

                parent.addSubItem(thisItem);
            }
        }
        return parent;
    }
}
