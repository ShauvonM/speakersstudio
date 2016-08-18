package com.thespeakers_studio.thespeakersstudioapp.model;

import android.content.Context;
import android.content.res.Resources;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.data.OutlineDbHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

/**
 * Created by smcgi_000 on 7/19/2016.
 */
public class Outline {
    private ArrayList<OutlineItem> mItems;
    private Context mContext;
    private PresentationData mPresentation;
    private OutlineDbHelper mDbHelper;

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
        mDbHelper = new OutlineDbHelper(context);
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

    // a class built to track the duration and leftover as we iterate through lists of items
    private class DurationTracker {
        // the remainder is extra default time to split between the sub-items
        private long durationRemainder;
        // the leftover is time left over from the previous outline item, because it has a custom time
        // different from its default
        private long durationLeftover;
        private long durationTally;
        private long defaultDuration;

        public DurationTracker (long remainder, long defaultDuration, long leftover) {
            durationRemainder = remainder;
            durationLeftover = leftover;
            durationTally = 0;
            this.defaultDuration = defaultDuration;
        }
        public void addDuration(long duration) {
            durationTally += duration;
        }
        public long getDurationRemainder() {
            return durationRemainder;
        }
        public long getDurationTally() {
            return durationTally;
        }
        public void incrementRemainder(long inc) {
            durationRemainder += inc;
        }
        public long getDefaultDuration() {
            return defaultDuration;
        }
        // once you get the leftover, it resets
        public long getDurationLeftover() {
            long value = durationLeftover;
            durationLeftover = 0;
            return value;
        }
        public void incrementLeftover(long leftover) {
            durationLeftover += leftover;
        }
    }

    // used for the Intro and Closing items to load all of the default sub-items for those things
    private long loadSubItemsFromPrompts(int[] promptIDs, String parentId,
                                         long duration, long durationLeftover) {

        // get the default duration for each sub-item
        long subItemDuration = Utils.roundToThousand(duration / promptIDs.length);
        // collect the leftover amount, which could happen since we rounded the value above
        //      for example, if the time doesn't evenly split between the items, this will make sure
        //      the excess time will be distributed as evenly as possible, by the second
        long subItemRemainder = (duration - (subItemDuration * promptIDs.length));

        // set up the tracker object, which will keep track of our duration figures as we iterate
        DurationTracker tracker = new DurationTracker(
                subItemRemainder, subItemDuration, durationLeftover);

        // iterate through the given prompts and create the items
        for (int i = 0; i < promptIDs.length; i++) {
            String id = parentId + "_prompt_" + promptIDs[i];
            PromptAnswer answer = mPresentation.getPrimaryAnswerForPrompt(promptIDs[i]);

            addItem(id, parentId, tracker, answer);
        }

        // TODO: we should be able to load any custom items by looking for things without answer ids

        // return the total duration of this item
        return tracker.getDurationTally();
    }

    // this is an expanded version of the loadSubItemsFromPrompts method, but since it uses
    // different kinds of things, it can't really be handled with one method
    public long loadSubItemsFromTopics(
            ArrayList<PromptAnswer> topics, ArrayList<Prompt> topicSubItems,
            long topicDuration, long durationLeftover) {

        long durationTally = 0;
        String presentationId = mPresentation.getId();

        // loop through the topics and add each one with its sub-items
        for (PromptAnswer thisTopic : topics) {
            String topicItemId = "topic_" + thisTopic.getKey();
            OutlineItem topicItem = new OutlineItem(
                    topicItemId,
                    OutlineItem.NO_PARENT,
                    getItemCount(),
                    thisTopic.getValue(),
                    thisTopic.getId(),
                    false,
                    0,
                    presentationId);

            // add it here so the order will be preserved - we'll figure out the duration later
            addItem(topicItem);

            // get the sub-items for this topic
            ArrayList<PromptAnswer> subItemList = new ArrayList<>();
            for (Prompt p : topicSubItems) {
                subItemList.addAll(p.getAnswersByLinkId(thisTopic.getId()));
            }

            // calculate the default duration for each sub-item
            long subItemDuration = Utils.roundToThousand(topicDuration / subItemList.size());
            // this will account for any excess time that was created by rounding the calculation above
            long subItemRemainder = topicDuration - (subItemDuration * subItemList.size());

            // set up the tracker object, which will keep track of our duration figures as we iterate
            DurationTracker tracker = new DurationTracker(
                subItemRemainder, subItemDuration, durationLeftover);

            // loop through each sub-item
            for (PromptAnswer thisSubTopic : subItemList) {
                String id = topicItemId + "_prompt_" + thisSubTopic.getPromptId();

                addItem(id, topicItemId, tracker, thisSubTopic);
            }

            long thisTally = tracker.getDurationTally();
            topicItem.setDuration(thisTally);
            durationLeftover = (topicDuration + durationLeftover) - thisTally;
            durationTally += thisTally;
        } // end of topic loop

        return durationTally;
    }

    public void addItem (String itemId, String parentId,
                        DurationTracker tracker,
                        PromptAnswer answer) {
        int order = getItemCount();
        String answerId = answer.getId();
        String presentationId = mPresentation.getId();

        OutlineItem thisItem = new OutlineItem();

        // load up any saved entries for this answer id
        ArrayList<OutlineItem> dbItems = mDbHelper
                .getOutlineItemsByAnswerId(answerId, presentationId);

        thisItem.setId(itemId);
        thisItem.setPresentationId(presentationId);
        thisItem.setAnswerId(answerId);
        thisItem.setParentId(parentId);
        thisItem.setOrder(order);

        // set up the duration for this item
        long thisDuration = 0;

        /*
            the rules here are like this:
            custom times saved in the DB always take precedence over default times

            if an item has a custom time, we have to keep track of the leftover time, which might be
            positive or negative

            the leftover time will go to the first item without a custom time, which could be not
            even in this section of the outline
          */

        // set up the default duration for this item, including its portion of the remainder
        long thisDefaultDuration = tracker.getDefaultDuration();
        if (tracker.getDurationRemainder() >= 1000) {
            thisDefaultDuration += 1000;
            tracker.incrementRemainder(-1000);
        }

        // if there are DB items for this thing, we should load up their durations
        if (dbItems.size() > 0) {
            // average the saved durations
            long tally = 0;
            int dbItemCount = 0;
            for (OutlineItem item : dbItems) {
                if (item.getDuration() > 0) {
                    tally += Utils.roundToThousand(item.getDuration());
                    dbItemCount++;
                }
            }
            thisDuration = tally / dbItemCount;
            // since this is a custom duration, save the excess (or remove the overlap)
            tracker.incrementLeftover((thisDefaultDuration - thisDuration));
        }

        // maybe the saved items didn't have durations, or there weren't any at all
        if (thisDuration == 0) {
            // the leftover goes to the first item with no custom time set
            thisDuration = thisDefaultDuration + tracker.getDurationLeftover();
        }

        /* TODO: I don't think this is correct - I think the last item will just handle itself
        // if this is the last item, we should just dump all the remaining time in there
        if (lastItem) {
            thisDuration += tracker.getDurationRemainder() + tracker.getDurationLeftover();
        }
        */

        thisItem.setDuration(thisDuration);
        tracker.addDuration(thisDuration);

        // add this thing to the outline, depending on what type of thing it is
        if (answer.getPromptType() == PresentationData.LIST) {
            // we have to add all the answers as their own sub-item, so we can treat them
            // individually if we need to
            thisItem.setText(mContext.getResources().getString(R.string.outline_item_mention));
            addItem(thisItem);

            ArrayList<PromptAnswer> answers = mPresentation.getAnswer(answer.getPromptId());
            for (PromptAnswer thisAnswer : answers) {
                OutlineItem answerItem = new OutlineItem(
                        "",
                        itemId,
                        getItemCount(),
                        thisAnswer.getValue(),
                        thisAnswer.getId(),
                        false,
                        0,
                        mPresentation.getId()
                );
                addItem(answerItem);
            }
        } else {
            // all other types can just dump the answer up in there
            // some times might need special treatment, which should be added here as necessary
            thisItem.setText(answer.getValue());
            addItem(thisItem);
        }

        // this doesn't have to return anything because the tracker will have the values in it
    }



    // this is the biggun - this method generates the outline list
    public static Outline fromPresentation (Context context, PresentationData pres) {
        Outline outline = new Outline(context);
        outline.setPresentation(pres);

        // for convenience
        Resources r = context.getResources();

        // set up the presentation duration, which is stored in the DB in minutes
        int durationMinutes = pres.getDuration();
        long durationMillis = (durationMinutes * 60) * 1000;

        // load the topics
        ArrayList<PromptAnswer> topics = pres.getAnswer(PresentationData.PRESENTATION_TOPICS);
        int topicCount = topics.size();

        // this is the default durations for these things, they might change depending on
        // items saved in the OutlineItem database
        long topicDuration = Utils.roundToThousand(
                (long) Math.floor((durationMillis * 0.75) / topicCount));
        long introDuration = (long) Math.floor((durationMillis - (topicDuration * topicCount)) / 2);

        // all of the items are in one big ol' list, so we will track the ordering throughout

        // the intro item is always first, so put it first
        OutlineItem introItem = new OutlineItem(
                OutlineItem.INTRO,
                0,
                r.getString(R.string.outline_item_intro),
                pres.getId()
        );
        outline.addItem(introItem);
        // we won't know the duration of the intro until we've gone through the items
        // because there could be some wackyland stuff in there
        long totalIntroItemDuration = outline.loadSubItemsFromPrompts(OUTLINE_INTRO_ITEMS,
                        OutlineItem.INTRO, introDuration, 0);

        introItem.setDuration(totalIntroItemDuration);

        // load up all of the topic sub items
        // these all contain multiple answers, which are linked to the different topics
        ArrayList<Prompt> topicSubItems = new ArrayList<>();
        for (int i = 0; i < Outline.OUTLINE_TOPIC_ITEMS.length; i++) {
            topicSubItems.add(pres.getPromptById(Outline.OUTLINE_TOPIC_ITEMS[i]));
        }

        // figure out how far off we are already from putting the intro together
        long durationLeftover = introDuration - totalIntroItemDuration;

        // load up all the topics and their sub-items, which returns the total time for them
        long totalTopicsDuration = outline.loadSubItemsFromTopics(topics, topicSubItems,
                topicDuration, durationLeftover);

        // recalculate the left over time from where we would normally be right now by default
        durationLeftover = (introDuration + (topicDuration * topicCount)) -
                (totalIntroItemDuration + totalTopicsDuration);

        // the conclusion item is always last, so put it last
        OutlineItem concItem = new OutlineItem(
                OutlineItem.CONCLUSION,
                outline.getItemCount(),
                r.getString(R.string.outline_item_conclusion),
                pres.getId()
        );
        outline.addItem(concItem);
        // we won't know the duration of the conclusion until we've gone through the items
        // because there could be some wackyland stuff in there
        long totalConclusionItemDuration = outline.loadSubItemsFromPrompts(OUTLINE_CONC_ITEMS,
                        OutlineItem.CONCLUSION, introDuration, durationLeftover);

        // figure out one last time what we have left over
        durationLeftover = durationMillis -
                (totalIntroItemDuration + totalTopicsDuration + totalConclusionItemDuration);

        // dump any remaining time into the last item, whether it wants it or not
        if (durationLeftover != 0) {
            OutlineItem lastItem = outline.getItem(outline.getItemCount() - 1);
            lastItem.setDuration(lastItem.getDuration() + durationLeftover);
        }

        // set the duration of the conclusion item
        concItem.setDuration(totalConclusionItemDuration + durationLeftover);

        return outline;
    }

}
