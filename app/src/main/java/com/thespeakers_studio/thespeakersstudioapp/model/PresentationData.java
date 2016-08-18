package com.thespeakers_studio.thespeakersstudioapp.model;

import android.content.Context;
import android.database.Cursor;

import com.thespeakers_studio.thespeakersstudioapp.data.PresentationDataContract;
import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.utils.PresentationUtils;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

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
    private String mModifiedDate;
    private int mColor;
    private boolean mIsSelected;

    // TODO: Is this the best way to remember all these ids?
    public static final int PRESENTATION_TOPIC = 1; // the ID of the prompt that "names" the presentation
    public static final int PRESENTATION_DURATION = 8; // the ID of the duration prompt
    public static final int PRESENTATION_DATE = 4;
    public static final int PRESENTATION_TOPICS = 17;

    public static final int PRESENTATION_HEADER = 0;
    public static final int PRESENTATION_NEXT = 24;
    public static final int PRESENTATION_LIST_ERROR = 25;

    // header and next are for the com.thespeakers_studio.thespeakersstudioapp.view to display cards for the step name and the next button
    public static final int NONE = -1, HEADER = 0, NEXT = 1, TEXT = 2, DATETIME = 3, LOCATION = 4,
            CONTACTINFO = 5, PARAGRAPH = 6, DURATION = 7, LIST = 8;

    // TODO: make constants for answer keys, duh

    public PresentationData(Context context, String id, String modifiedDate, int color) {
        this.mContext = context;
        this.mPresentationId = id;
        this.mColor = color;
        mModifiedDate = modifiedDate;
        mPrompts = new ArrayList<Prompt>();

        // add a generic item to use for the Header and Next buttons on the list of cards
        // this might not be the best way to do this, but it makes sense to me
        mPrompts.add(new Prompt(0, 0, 0, HEADER, ""));
        mPrompts.add(new Prompt(24, 5, 0, NEXT, ""));
        mPrompts.add(new Prompt(25, 5, 0, NONE, "Please complete %s\nto continue."));

        // Prompt(int id, int step, int order, String type, String text, int charLimit)
        mPrompts.add(new Prompt(1, 1, 1, TEXT,        "Presentation Topic", 50));
        mPrompts.add(new Prompt(2, 1, 2, TEXT,        "What is the Event?", 50));
        mPrompts.add(new Prompt(3, 1, 3, TEXT,        "Describe the tone of the event (celebratory, solemn, informal, formal, etc)", 50));
        mPrompts.add(new Prompt(4, 1, 4, DATETIME,    "When is the\nevent?"));
        mPrompts.add(new Prompt(5, 1, 5, LOCATION,    "Where is the event?"));
        mPrompts.add(new Prompt(6, 1, 6, CONTACTINFO, "Who is hosting\nthe event?"));
        mPrompts.add(new Prompt(7, 1, 7, PARAGRAPH,   "What is the\nhost's mission?", 250));
        mPrompts.add(new Prompt(8, 1, 8, DURATION,    "What is the\nPresentation\nduration?"));

        mPrompts.add(new Prompt(9, 2, 1, PARAGRAPH,   "Why is %t\nimportant?", 250,                          2, "this event"));
        mPrompts.add(new Prompt(10, 2, 2, PARAGRAPH,  "Describe the audience.", 250));
        mPrompts.add(new Prompt(11, 2, 3, LIST,       "What recent events are important to acknowledge or mention?", 50));
        mPrompts.add(new Prompt(12, 2, 4, LIST,       "What recent news or announcements are noteworthy to this audience?", 50));
        mPrompts.add(new Prompt(13, 2, 5, TEXT,       "What do you want the audience to know, do, or feel when they leave?", 50));
        mPrompts.add(new Prompt(14, 2, 6, LIST,       "Who in the audience do you need to recognize?", 50));

        mPrompts.add(new Prompt(15, 3, 1, PARAGRAPH,  "What do you want to say to the audience?", 140));
        mPrompts.add(new Prompt(16, 3, 2, PARAGRAPH,  "Why?", 250));
        mPrompts.add(new Prompt(17, 3, 3, LIST,       "What things do you want to talk about (we'll get to why later)?", 50));

        mPrompts.add(new Prompt(18, 4, 1, PARAGRAPH,  "What is your first sentence?", 140));
        mPrompts.add(new Prompt(19, 4, 2, PARAGRAPH,  "What is your last sentence?", 140));
        mPrompts.add(new Prompt(20, 4, 3, PARAGRAPH,  "Why is \"%l\" important?", 140,                          17, ""));
        mPrompts.add(new Prompt(21, 4, 4, PARAGRAPH,  "How does \"%l\" connect to the Audience?", 140,          17, ""));
        mPrompts.add(new Prompt(22, 4, 5, TEXT,       "What story can you connect to \"%l?\"", 50,              17, ""));
        mPrompts.add(new Prompt(23, 4, 6, PARAGRAPH,  "How do you transition from %l to %n ?", 140,             17, ""));

        mIsSelected = false;
    }

    public void select() {
        mIsSelected = true;
    }
    public void deselect() {
        mIsSelected = false;
    }
    public boolean getIsSelected() {
        return mIsSelected;
    }

    public String getId() {
        return mPresentationId;
    }

    public void setColor(int color) {
        mColor = color;
    }
    public int getColor() {
        return mColor;
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

    public ArrayList<PromptAnswer> getAnswer(int promptId) {
        return getPromptById(promptId).getAnswer();
    }

    public PromptAnswer getAnswerByKey (int promptId, String answerKey) {
        Prompt p = getPromptById(promptId);
        return p.getAnswerByKey(answerKey);
    }

    public PromptAnswer getPrimaryAnswerForPrompt (int promptId) {
        Prompt p = getPromptById(promptId);
        switch(p.getType()) {
            case TEXT:
            case PARAGRAPH:
                return getAnswerByKey(promptId, "text");
            case DATETIME:
                return getAnswerByKey(promptId, "timestamp");
            case LOCATION:
                return getAnswerByKey(promptId, "name");
            case CONTACTINFO:
                return getAnswerByKey(promptId, "name");
            case DURATION:
                return getAnswerByKey(promptId, "duration");
            case LIST:
                return getAnswerByKey(promptId, "list_1");
            default:
                return getAnswer(promptId).get(0);
        }
    }

    public String getTopic() {
        String topic = getAnswerByKey(PRESENTATION_TOPIC, "text").getValue();
        if (topic.isEmpty()) {
            return mContext.getResources().getString(R.string.new_presentation);
        } else{
            return topic;
        }
    }

    public int getDuration() {
        return Integer.parseInt(getAnswerByKey(PRESENTATION_DURATION, "duration").getValue());
    }

    public String getDate() {
        return getAnswerByKey(PRESENTATION_DATE, "timestamp").getValue();
    }

    public void setModifiedDate(String date) {
        mModifiedDate = date;
    }

    public String getModifiedDate() {
        return Utils.formatDateTime(mContext, mModifiedDate);
    }

    public Prompt processPrompt (Prompt p) {
        String processedText = p.getText();
        if (p.getReferenceId() > 0) {
            int refId = p.getReferenceId();
            String refDefault = p.getReferenceDefault();
            Prompt refPrompt = getPromptById(refId);

            if (refPrompt.getType() == TEXT || refPrompt.getType() == PARAGRAPH) {
                PromptAnswer refAnswer = refPrompt.getAnswerByKey("text");
                String value = refAnswer.getValue().isEmpty() ? refDefault : refAnswer.getValue();
                processedText = p.getText().replace("%t", value);
            } else if (refPrompt.getType() == LIST) {
                Pattern refRegex = Pattern.compile("%l([0-9]+)");
                Pattern nextRegex = Pattern.compile("%n([0-9]+)");
                Matcher refMatcher = refRegex.matcher(p.getText());
                Matcher nextMatcher = nextRegex.matcher(p.getText());

                processedText = p.getText();
                ArrayList<PromptAnswer> refAnswers = refPrompt.getAnswer();

                if (refMatcher.find()) {
                    int refI = Integer.parseInt(refMatcher.group(1));
                    processedText = processedText.replace("%l" + refI,
                            refAnswers.get(refI).getValue());
                }

                if (nextMatcher.find()) {
                    int nextI = Integer.parseInt(nextMatcher.group(1));
                    processedText = processedText.replace("%n" + nextI,
                            refAnswers.get(nextI).getValue());
                }
            }

        }
        p.setProcessedText(processedText);

        return p;
    }

    public ArrayList<Prompt> getPromptsForStep (int step) {
        ArrayList<Prompt> selection = new ArrayList<>();

        // add the header
        selection.add(getPromptById(PRESENTATION_HEADER));

        int orderPadding = 0;
        boolean skipRedundant = false;
        for (Prompt p : mPrompts) {
            if (p.getStep() == step && p.getId() != PRESENTATION_HEADER) {
                if (p.getReferenceId() > 0 && getPromptById(p.getReferenceId()).getType() == LIST) {
                    Prompt ref = getPromptById(p.getReferenceId());
                    // add a prompt to the list for each answer in the referenced prompt
                    for (PromptAnswer a : ref.getAnswer()) {
                        int index = Integer.parseInt(a.getKey().replace("list_", ""));
                        Prompt np = p.clone(a.getId());
                        np.setText(np.getText().replace("%l", "%l" + index));
                        np.setOrder(np.getOrder() + orderPadding);
                        orderPadding++;

                        // add the prompt if it includes a "%n" in it, but not for the last one,
                        // because there isn't a next answer to include
                        if (np.getText().indexOf("%n") > -1 && index < ref.getAnswer().size() - 1) {
                            np.setText(np.getText().replace("%n", "%n" + (index + 1)));
                            selection.add(np.getOrder(), processPrompt(np));
                        } else if (np.getText().indexOf("%n") == -1) {
                            // if there is no "%n" we can insert it no matter what
                            selection.add(np.getOrder(), processPrompt(np));
                        }
                    }
                    // decrement the order padding because the next item will be one larger anyway
                    orderPadding--;

                    // if no answers have been added to the referenced prompt,
                    //  just show a "complete step x" message instead
                    //  but we only need to do this once per step, so we don't have a bunch of
                    //  "complete step x" messages in a row
                    if (ref.getAnswer().size() == 0 && !skipRedundant) {
                        Prompt np = getPromptById(PRESENTATION_LIST_ERROR).clone();
                        int stepId = ref.getStep();
                        String stepName = PresentationUtils.getStepNameFromId(mContext, stepId);
                        String stepLabel = PresentationUtils.getStepLabelFromId(mContext, stepId);

                        np.setProcessedText(np.getText().replace("%s",
                                stepLabel + ", \"" + stepName + "\""));
                        np.setOrder(p.getOrder());
                        selection.add(np.getOrder(), np);
                        skipRedundant = true;
                    }
                } else {
                    selection.add(p.getOrder() + orderPadding, processPrompt(p));
                }
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

    public void resetAnswers() {
        for (Prompt p : mPrompts) {
            p.resetAnswers();
        }
    }

    public void setAnswers (Cursor answerCursor) {
        answerCursor.moveToFirst();
        try {
            while (!answerCursor.isAfterLast()) {
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
                    String answerLinkId = answerCursor.getString(
                            answerCursor.getColumnIndexOrThrow(PresentationDataContract.PresentationAnswerEntry.COLUMN_NAME_ANSWER_LINK_ID)
                    );

                    PromptAnswer thisAnswer = new PromptAnswer(answerId, answerKey, answerValue, promptId, answerCreatedBy, answerModifiedBy, answerCreatedDate, answerModifiedDate, answerLinkId);
                    Prompt prompt = getPromptById(promptId);

                    thisAnswer.setPromptType(prompt.getType());

                    prompt.addAnswer(thisAnswer);
                }

                answerCursor.moveToNext();
            }
        } finally {
            answerCursor.close();
        }
    }

    public int getCurrentStep() {
        int currentStep = 0;
        for (int step = 1; step < 5; step++) {
            if (getCompletionPercentage(step) < 1 || step == 4) {
                currentStep = step;
                step = 5;
            }
        }
        return currentStep;
    }

    public float getCompletionPercentage(int step) {
        ArrayList<Prompt> prompts = getPromptsForStep(step);
        int count = prompts.size();
        int progress = 0;
        int skips = 0;
        for(int cnt = 0; cnt < count; cnt++) {
            Prompt thisPrompt = prompts.get(cnt);
            int type = thisPrompt.getType();
            if (type == HEADER || type == NEXT || type == NONE) {
                skips++;
            } else {
                if (thisPrompt.getAnswer().size() > 0) {
                    progress++;
                } else {
                    cnt = count + 1;
                }
            }
        }
        // subtract skipped items from the count because we have the header and next items
        return (float) progress / (float) (count - skips);
    }

    // pass no step to get the completion of the entire presentation
    public float getCompletionPercentage() {
        float per = 0;
        for(int c = 1; c < 5; c++) {
            float thisper = getCompletionPercentage(c);
            if (thisper < 1) {
                per += (thisper / 4);
                return per;
            } else {
                per += 0.25;
            }
        }
        return per;
    }


}
