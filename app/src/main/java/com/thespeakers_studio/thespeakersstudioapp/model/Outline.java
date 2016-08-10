package com.thespeakers_studio.thespeakersstudioapp.model;

import android.content.Context;
import android.content.res.Resources;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

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

    /*
        default durations (these are probably placeholders)
        INTRO: 12.5%
        TOPICS: 75% / top level item count
        CONCLUSION: 12.5%
    */

    public Outline(Context context) {
        mItems = new ArrayList<>();
        mContext = context;
    }

    public ArrayList<OutlineItem> getItems() {
        Utils.sortOutlineList(mItems);
        return mItems;
    }

    public ArrayList<OutlineItem> getItemsByParentId(String parentId) {
        ArrayList<OutlineItem> items = new ArrayList<>();
        if (parentId.isEmpty()) {
            return items;
        }
        for (OutlineItem item : mItems) {
            if (item.getParentId().equals(parentId)) {
                items.add(item);
            }
        }
        //Utils.sortOutlineList(items); TODO
        return items;
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

    private long loadSubItemsFromPrompts(int[] promptIDs, String parentId, long duration, int order) {
        Resources r = mContext.getResources();

        long durationTally = 0;

        long subItemDuration = Math.round((duration / promptIDs.length) / 1000) * 1000;
        long subItemLeftover = duration - (subItemDuration * promptIDs.length);

        for (int i = 0; i < promptIDs.length; i++) {
            order += i;

            int type = mPresentation.getPromptById(promptIDs[i]).getType();
            String id = parentId + "_prompt_" + promptIDs[i];

            OutlineItem thisItem = new OutlineItem();

            thisItem.setId(id);
            thisItem.setPresentationId(mPresentation.getId());
            thisItem.setOrder(order);

            long thisDuration = subItemDuration;
            if (subItemLeftover >= 1000) {
                thisDuration += 1000;
                subItemLeftover -= 1000;
            }

            thisItem.setDuration(thisDuration);
            durationTally += thisDuration;

            thisItem.setParentId(parentId);

            if (type == PresentationData.TEXT || type == PresentationData.PARAGRAPH) {

                PromptAnswer answer = mPresentation.getAnswerByKey(promptIDs[i], "text");

                thisItem.setText(answer.getValue());
                thisItem.setAnswerId(answer.getId());

                addItem(thisItem);

            } else if (type == PresentationData.LIST) {

                // we have to add all the answers as their own sub-item, so we can treat them
                // individually if we need to
                thisItem.setText(r.getString(R.string.outline_item_mention));
                thisItem.setAnswerId(id);
                addItem(thisItem);

                ArrayList<PromptAnswer> answers = mPresentation.getAnswer(promptIDs[i]);
                for (PromptAnswer answer : answers) {
                    order++;
                    OutlineItem answerItem = new OutlineItem(
                            "",
                            id,
                            order,
                            answer.getValue(),
                            answer.getId(),
                            false,
                            0,
                            mPresentation.getId()
                    );
                    addItem(answerItem);
                }

            }
        }

        return durationTally;
    }


    // this is the biggun - this method generates the outline list
    public static Outline fromPresentation (Context context, PresentationData pres) {
        Outline outline = new Outline(context);
        outline.setPresentation(pres);

        Resources r = context.getResources();

        int durationMinutes = pres.getDuration();
        long durationMillis = (durationMinutes * 60) * 1000;

        ArrayList<PromptAnswer> topics = pres.getAnswer(PresentationData.PRESENTATION_TOPICS);
        int topicCount = topics.size();

        long topicDuration = (long) Math.floor((durationMillis * 0.75) / topicCount);
        long introDuration = (long) Math.floor((durationMillis - (topicDuration * topicCount)) / 2);

        int masterOrder = 0;

        //public OutlineItem (String id, int order, String text, String presentation) {
        OutlineItem introItem = new OutlineItem(
                OutlineItem.INTRO,
                masterOrder,
                r.getString(R.string.outline_item_intro),
                pres.getId()
        );
        masterOrder++;
        // we won't know the duration of the intro until we've gone through the items
        // because there could be some wackyland stuff in there
        introItem.setDuration(
                outline.loadSubItemsFromPrompts(OUTLINE_INTRO_ITEMS,
                        OutlineItem.INTRO, introDuration, masterOrder)
        );
        outline.addItem(introItem);
        masterOrder = outline.getItemCount();

        // load up all of the topic sub items
        ArrayList<Prompt> topicSubItems = new ArrayList<>();
        for (int i = 0; i < Outline.OUTLINE_TOPIC_ITEMS.length; i++) {
            topicSubItems.add(pres.getPromptById(Outline.OUTLINE_TOPIC_ITEMS[i]));
        }

        // loop through the topics and add each one with its sup-items
        for (PromptAnswer t : topics) {
            OutlineItem topicItem = new OutlineItem(
                    t.getKey(),
                    OutlineItem.NO_PARENT,
                    masterOrder,
                    t.getValue(),
                    t.getId(),
                    false,
                    topicDuration,
                    pres.getId());

            outline.addItem(topicItem);
            masterOrder++;
            long durationTally = 0l;

            ArrayList<PromptAnswer> subItemList = new ArrayList<>();
            for (Prompt p : topicSubItems) {
                subItemList.addAll(p.getAnswersByLinkId(t.getId()));
            }

            long subItemDuration = Math.round((topicDuration / subItemList.size()) / 1000) * 1000;
            long subItemLeftover = topicDuration - (subItemDuration * subItemList.size());

            for (PromptAnswer a : subItemList) {
                OutlineItem subitem = new OutlineItem(
                        "", // id?
                        t.getKey(),
                        masterOrder,
                        a.getValue(),
                        a.getId(),
                        false,
                        0,
                        pres.getId()
                );

                long thisDuration = subItemDuration;
                if (subItemLeftover >= 1000) {
                    thisDuration += 1000;
                    subItemLeftover -= 1000;
                }
                subitem.setDuration(thisDuration);

                topicItem.addSubItem(subitem);
                masterOrder++;
            }

            outline.addItem(topicItem);
            order++;
        }

        return outline;
    }

    /*
    public static Outline fromPresentation (Context context, PresentationData pres) {
        Outline outline = new Outline(context);
        outline.setPresentation(pres);
        Resources r = context.getResources();

        int durationMinutes = Integer.parseInt(pres.getAnswerByKey(PresentationData.PRESENTATION_DURATION, "duration"));
        long durationMillis = (durationMinutes * 60) * 1000;

        ArrayList<PromptAnswer> topics = pres.getAnswer(PresentationData.PRESENTATION_TOPICS);

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
    */
}
