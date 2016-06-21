package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by smcgi_000 on 4/21/2016.
 */
public class PresentationData {
    private Context mContext;
    private ArrayList<Prompt> mPrompts;
    private String mPresentationId;

    public static final int PRESENTATION_TOPIC = 1; // the ID of the prompt that "names" the presentation
    public static final int PRESENTATION_HEADER = 0;
    public static final int PRESENTATION_NEXT = 24;

    // header and next are for the view to display cards for the step name and the next button
    public static final int HEADER = 0, NEXT = 1, TEXT = 2, DATETIME = 3, LOCATION = 4, CONTACTINFO = 5, PARAGRAPH = 6, DURATION = 7, LIST = 8;

    public static final int LOCATION_INTENT_REQUEST_CODE = 1;

    public PresentationData(Context context, String id) {
        this.mContext = context;
        this.mPresentationId = id;
        mPrompts = new ArrayList<Prompt>();

        // add a generic item to use for the Header and Next buttons on the list of cards
        // this might not be the best way to do this, but it makes sense to me
        mPrompts.add(new Prompt(0, 0, 0, HEADER, ""));
        mPrompts.add(new Prompt(24, 5, 0, NEXT, ""));

        // Prompt(int id, int step, int order, String type, String text, int charLimit)
        mPrompts.add(new Prompt(1, 1, 1, TEXT,        "What is the Topic?", 50));
        mPrompts.add(new Prompt(2, 1, 2, TEXT,        "What is the Event?", 50));
        mPrompts.add(new Prompt(3, 1, 3, TEXT,        "What is the tone of\nthe event (formal,\ninformal, etc.)?", 50));
        mPrompts.add(new Prompt(4, 1, 4, DATETIME,    "When is the\npresentation?"));
        mPrompts.add(new Prompt(5, 1, 5, LOCATION,    "Where is the event?"));
        mPrompts.add(new Prompt(6, 1, 6, CONTACTINFO, "Who is hosting\nthe event?"));
        mPrompts.add(new Prompt(7, 1, 7, PARAGRAPH,   "What is the\nhost's mission?", 250));
        mPrompts.add(new Prompt(8, 1, 8, DURATION,    "What is the\nPresentation\nduration?"));

        mPrompts.add(new Prompt(9, 2, 1, PARAGRAPH,   "Why is {2} important?", 250,         "this event"));
        mPrompts.add(new Prompt(10, 2, 2, PARAGRAPH,  "Describe the audience.", 250));
        mPrompts.add(new Prompt(11, 2, 3, LIST,       "What recent events are important to acknowledge or mention?", 50));
        mPrompts.add(new Prompt(12, 2, 4, LIST,       "What recent news or announcements are noteworthy to this audience?", 50));
        mPrompts.add(new Prompt(13, 2, 5, TEXT,       "What do you want the audience to know, do, or feel when they leave?", 50));
        mPrompts.add(new Prompt(14, 2, 6, LIST,       "Who in the audience do you need to recognize?", 50));

        mPrompts.add(new Prompt(15, 3, 1, PARAGRAPH,  "What do you want to say to the audience?", 140));
        mPrompts.add(new Prompt(16, 3, 2, PARAGRAPH,  "Why?", 250));
        mPrompts.add(new Prompt(17, 3, 3, LIST,       "What are your topics?", 50));

        mPrompts.add(new Prompt(18, 4, 1, PARAGRAPH,  "What is your first sentence?", 140));
        mPrompts.add(new Prompt(19, 4, 2, PARAGRAPH,  "What is your last sentence?", 140));
        mPrompts.add(new Prompt(20, 4, 3, PARAGRAPH,  "Why is {foreach:17} important?", 140));
        mPrompts.add(new Prompt(21, 4, 4, PARAGRAPH,  "How does {foreach:17} connect to the Audience?", 140));
        mPrompts.add(new Prompt(22, 4, 5, TEXT,       "What story can you connect to {foreach:17}?", 50));
        mPrompts.add(new Prompt(23, 4, 6, PARAGRAPH,  "How do you transition from {foreach:17} to {next:17}?", 140));
    }

    private int getPromptIndexById(int id) {
        for (int i = 0; i < mPrompts.size(); i++) {
            if (mPrompts.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    public Prompt getPromptById(int id) {
        int index = getPromptIndexById(id);
        return processPrompt(mPrompts.get(index));
    }

    public String getAnswerByKey(int promptId, String answerKey) {
        Prompt p = getPromptById(promptId);
        return p.getAnswerByKey(answerKey).getValue();
    }

    public String getTopic() {
        String topic = getAnswerByKey(PRESENTATION_TOPIC, "text");
        if (topic.isEmpty()) {
            return mContext.getResources().getString(R.string.new_presentation);
        } else{
            return topic;
        }
    }

    public Prompt processPrompt (Prompt p) {
        String text = p.getText();

        // fill {x} with the answer of the prompt ID specified, or use the default text provided
        String refPattern = "\\{([0-9]*)\\}";
        Pattern refRegex = Pattern.compile(refPattern);
        Matcher refMatcher = refRegex.matcher(text);
        if (refMatcher.find()) {
            int refId = Integer.parseInt(refMatcher.group(1));
            String refDefault = p.getReferenceDefault();

            Prompt refPrompt = getPromptById(refId);
            PromptAnswer refAnswer = refPrompt.getAnswerByKey("text");

            if (refAnswer.getValue().isEmpty()) {
                if (refDefault.isEmpty()) {
                    // TODO: what happens if there's no answer and no default to fall back on?
                } else {
                    p.setProcessedText(refMatcher.replaceAll(refDefault));
                }
            } else {
                p.setProcessedText(refMatcher.replaceAll(refAnswer.getValue()));
            }
        }

        return p;
    }

    public ArrayList<Prompt> getPromptsForStep (int step) {
        ArrayList<Prompt> selection = new ArrayList<>();

        // add the header
        selection.add(getPromptById(PRESENTATION_HEADER));

        for (Prompt p : mPrompts) {
            if (p.getStep() == step && p.getId() != PRESENTATION_HEADER) {
                selection.add(p.getOrder(), processPrompt(p));
            }
        }
        // add the next button
        selection.add(getPromptById(PRESENTATION_NEXT));
        // set the step on the header, so it knows what's up
        selection.get(0).setStep(step);
        // set the order of the next item, so it knows where to be
        selection.get(selection.size() - 1).setOrder(selection.size());
        return selection;
    }

    public void setAnswers (Cursor answerCursor) {
        answerCursor.moveToFirst();
        try {
            while (answerCursor.isAfterLast() == false) {
                String answerValue = answerCursor.getString(
                        answerCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_VALUE)
                );

                // ignore answers with empty strings
                if (!answerValue.isEmpty()) {
                    int promptId = answerCursor.getInt(
                            answerCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_PROMPT_ID)
                    );
                    String answerId = answerCursor.getString(
                            answerCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_ID)
                    );
                    String answerKey = answerCursor.getString(
                            answerCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_KEY)
                    );
                    String answerCreatedDate = answerCursor.getString(
                            answerCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_DATE_CREATED)
                    );
                    String answerCreatedBy = answerCursor.getString(
                            answerCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_CREATED_BY)
                    );
                    String answerModifiedDate = answerCursor.getString(
                            answerCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_DATE_MODIFIED)
                    );
                    String answerModifiedBy = answerCursor.getString(
                            answerCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_MODIFIED_BY)
                    );

                    PromptAnswer thisAnswer = new PromptAnswer(answerId, answerKey, answerValue, promptId, answerCreatedBy, answerModifiedBy, answerCreatedDate, answerModifiedDate);
                    Prompt prompt = getPromptById(promptId);

                    prompt.addAnswer(thisAnswer);
                }

                answerCursor.moveToNext();
            }
        } finally {
            answerCursor.close();
        }
    }

    public float getCompletionPercentage(int step) {
        ArrayList<Prompt> prompts = getPromptsForStep(step);
        int count = prompts.size();
        int progress = 0;
        // skip the first one, because that's the header
        for(int cnt = 1; cnt < count; cnt++) {
            Prompt thisPrompt = prompts.get(cnt);
            if (thisPrompt.getAnswer().size() > 0) {
                progress++;
            } else {
                cnt = count + 1;
            }
        }
        // subtract 2 from the count because we have the header and next items
        return (float) progress / (float) (count - 2);
    }

}
