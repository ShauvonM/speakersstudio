package com.thespeakers_studio.thespeakersstudioapp;

import android.content.Context;
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
        mPrompts.add(new Prompt(3, 1, 3, TEXT,        "What is the tone of the event (formal, informal, etc.)?", 50));
        mPrompts.add(new Prompt(4, 1, 4, DATETIME,    "When is the presentation?"));
        mPrompts.add(new Prompt(5, 1, 5, LOCATION,    "Where is the event?"));
        mPrompts.add(new Prompt(6, 1, 6, CONTACTINFO, "Who is hosting the event?"));
        mPrompts.add(new Prompt(7, 1, 7, PARAGRAPH,   "What is the host's mission?", 250));
        mPrompts.add(new Prompt(8, 1, 8, DURATION,    "What is the Presentation duration?"));

        mPrompts.add(new Prompt(9, 2, 1, PARAGRAPH,   "Why is {ref:2:this event} important?", 250));
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

    public String getAnswer(int id) {
        Prompt p = getPromptById(id);
        return p.getAnswer();
    }

    public String getTopic() {
        String topic = getAnswer(PRESENTATION_TOPIC);
        if (topic.isEmpty()) {
            return mContext.getResources().getString(R.string.new_presentation);
        } else{
            return topic;
        }
    }

    public Prompt processPrompt (Prompt p) {
        String text = p.getText();

        // fill {ref:id:default} with the answer of the prompt ID specified, or use the default text provided
        String refPattern = "\\{ref:([0-9]*)(:(.*))?\\}";
        Pattern refRegex = Pattern.compile(refPattern);
        Matcher refMatcher = refRegex.matcher(text);
        if (refMatcher.find()) {
            int refId = Integer.parseInt(refMatcher.group(1));
            Prompt refPrompt = getPromptById(refId);
            String refAnswer = refPrompt.getAnswer();
            if (refAnswer.equals("")) {
                String refDef = refMatcher.group(2);
                if (refDef.equals("") || refDef.equals(":")) {
                    // TODO: what happens if there's no answer and no default to fall back on?
                } else {
                    p.setProcessedText(refMatcher.replaceAll(refDef.substring(1)));
                }
            } else {
                p.setProcessedText(refMatcher.replaceAll(refAnswer));
            }
        }

        // repeat items with {foreach:id} referencing the answers in the given id (which hopefully is a list type)
        String forPattern = "\\{foreach:([0-9]*)\\}";
        Pattern forRegex = Pattern.compile(forPattern);
        Matcher forMatcher = forRegex.matcher(text);
        if (forMatcher.find()) {
            Log.d("SS", "Foreach found in " + text + " referencing " + forMatcher.group(1));
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
        return selection;
    }

}
